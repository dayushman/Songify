package com.example.songify.exoplayer

import android.content.ComponentName
import android.content.Context

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.songify.other.Constants.NETWORK_ERROR
import com.example.songify.other.Event
import com.example.songify.other.Resource

class MusicServiceConnection(
    context: Context
) {

    private var _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected : LiveData<Event<Resource<Boolean>>> = _isConnected


    private var _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError : LiveData<Event<Resource<Boolean>>> = _networkError


    private var _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState : LiveData<PlaybackStateCompat?> = _playbackState


    private var _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSong : LiveData<MediaMetadataCompat?> = _curPlayingSong

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls









    fun subscribe(parentID:String,callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentID,callback)
    }

    fun unsubscribe(parentID: String,callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentID,callback)
    }










    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback(){


        override fun onConnected() {
            mediaController = MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(
                Event(Resource.success(true))
            )
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(Resource.error(
                    "The Connection was Suspended!",false
                ))
            )
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(Resource.error("Couldn't Connect to MediaBrowser",false))
            )
        }
    }









    private inner class MediaControllerCallback: MediaControllerCompat.Callback(){

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)

            when(event){
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error("Couldn't connect to Network! Please Check your Internet Connection",null)
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}