package com.example.songify.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.songify.data.remote.MusicDatabase
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    var songs = emptyList<MediaMetadataCompat>()

    private val onReadyListener = mutableListOf<(Boolean)->Unit>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = State.STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()


        songs = allSongs.map { song->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.id)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()

        }
        state  = State.STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory):ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()

        songs.forEach { song->
            val media = ProgressiveMediaSource.Factory(dataSourceFactory).
                        createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(media)
        }
        return concatenatingMediaSource
    }

    fun asMediaItem() = songs.map { song->
        val desc = MediaDescriptionCompat.Builder()
            .setTitle(song.description.title)
            .setIconUri(song.description.iconUri)
            .setMediaId(song.description.mediaId)
            .setSubtitle(song.description.subtitle)
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .build()

        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private var state = State.STATE_CREATED
        set(value){
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR){
                synchronized(onReadyListener){
                    field = value
                    onReadyListener.forEach {listener->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            }
            else{
                field = value
            }
        }

    fun whenReady(action:(Boolean) -> Unit):Boolean{
        return if (state == State.STATE_INITIALIZING || state == State.STATE_CREATED){
            onReadyListener+=action
            true
        } else{
            action(state == State.STATE_INITIALIZED)
            true
        }
    }
}

enum class State{
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_CREATED,
    STATE_ERROR
}