package com.example.songify.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import com.example.songify.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.songify.exoplayer.callbacks.MusicPlayerEventListener
import com.example.songify.exoplayer.callbacks.MusicPlayerNotificationListener
import com.example.songify.other.Constants.NETWORK_ERROR
import com.example.songify.other.Constants.MEDIA_ROOT_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "Music Service"
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class MusicService :MediaBrowserServiceCompat() {


    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private var serviceJob = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var currentPlayingSong: MediaMetadataCompat? = null
    private var isPlayerInitialized = false

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var musicNotificationManager: MusicNotificationManager

    private lateinit var musicPlayerEventListener : MusicPlayerEventListener

    var isForegroundService = false

    companion object{
        var curSongDuration = 0L
            private set
    }



    override fun onCreate() {
        super.onCreate()

        coroutineScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }

        mediaSession = MediaSessionCompat(this,SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken


        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){
            //Callback lambada function when song changes
            curSongDuration = exoPlayer.duration
        }

        //Used for preparing the player
        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            currentPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }


        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setPlayer(exoPlayer)
        mediaSessionConnector.setQueueNavigator(MediaQueueNavigator())

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)

        musicNotificationManager.showNotification(exoPlayer)


    }

    private inner class MediaQueueNavigator:TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return  firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs:List<MediaMetadataCompat>,
        itemToPlay:MediaMetadataCompat?,
        playNow:Boolean
    ){
        val currentSong = if (itemToPlay == null)   0   else    songs.indexOf(itemToPlay)


        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentSong,0L)
        exoPlayer.playWhenReady = playNow
    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        var res:MutableList<MediaBrowserCompat.MediaItem>? = null
        when(parentId){
            MEDIA_ROOT_ID -> {
                val resultSent = firebaseMusicSource.whenReady {isInitialized->
                    if (isInitialized){
                        res = firebaseMusicSource.asMediaItem()
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                            preparePlayer(firebaseMusicSource.songs,firebaseMusicSource.songs[0],false)
                            isPlayerInitialized = true
                        }
                    }else{
                        mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                    }
                }
                if (!resultSent){
                    result.detach()
                }
            }
        }
        result.sendResult(res)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }


}