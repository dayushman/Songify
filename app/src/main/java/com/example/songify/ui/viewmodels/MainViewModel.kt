package com.example.songify.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.songify.data.entities.Song
import com.example.songify.exoplayer.MusicServiceConnection
import com.example.songify.exoplayer.isPlayEnabled
import com.example.songify.exoplayer.isPlaying
import com.example.songify.exoplayer.isPrepared
import com.example.songify.other.Constants.MEDIA_ROOT_ID
import com.example.songify.other.Resource

class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel(){

    private var _mediaItem = MutableLiveData<Resource<List<Song>>>()
    val mediaItem : LiveData<Resource<List<Song>>>
        get() = _mediaItem

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState


    init {
        _mediaItem.postValue(Resource.loading(null))

        musicServiceConnection.subscribe(MEDIA_ROOT_ID,object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString(),
                        it.description.subtitle.toString()
                    )
                }

                _mediaItem.postValue(Resource.success(items))
            }
        })
    }



    fun skipToNextSong() = musicServiceConnection.transportControls.skipToNext()
    fun skipToPrevSong() = musicServiceConnection.transportControls.skipToPrevious()
    fun seekTo(pos:Long) = musicServiceConnection.transportControls.seekTo(pos)

    fun playOrToggle(mediaItem:Song,toggle:Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.id == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let { playbackStateCompat ->
                when{
                    playbackStateCompat.isPlaying -> if (toggle)   musicServiceConnection.transportControls.pause()
                    playbackStateCompat.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }
        else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.id,null)
        }
    }






    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,object  : MediaBrowserCompat.SubscriptionCallback(){})
    }
}