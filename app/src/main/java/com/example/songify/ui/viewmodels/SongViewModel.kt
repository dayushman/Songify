package com.example.songify.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.songify.exoplayer.MusicService
import com.example.songify.exoplayer.MusicServiceConnection
import com.example.songify.exoplayer.currentPlaybackPosition
import com.example.songify.other.Constants.PLAYER_POSITION_DELAY
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(
    musicServiceConnection: MusicServiceConnection
):ViewModel() {
    private val playbackState = musicServiceConnection.playbackState

    init {
        updateCurrentPlayerPosition()
    }
    private var _curSongDuration =  MutableLiveData<Long>()
    val curSongDuration : LiveData<Long>  = _curSongDuration

    private var _curPlayerPosition =  MutableLiveData<Long>()
    val curPlayerPosition : LiveData<Long>  = _curPlayerPosition

    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch {
            while (true){
                val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition!=null && curPlayerPosition.value != pos){
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(PLAYER_POSITION_DELAY)
            }
        }
    }
}
