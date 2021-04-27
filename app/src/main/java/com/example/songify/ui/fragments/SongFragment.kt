package com.example.songify.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.songify.R
import com.example.songify.data.entities.Song
import com.example.songify.exoplayer.isPlaying
import com.example.songify.exoplayer.toSong
import com.example.songify.other.Status.SUCCESS
import com.example.songify.ui.viewmodels.MainViewModel
import com.example.songify.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment:Fragment(R.layout.fragment_song) {


    private var shouldUpdateSeekBar: Boolean = true
    private  var curPlaybackState: PlaybackStateCompat? = null

    @Inject
    lateinit var glide:RequestManager

    lateinit var mainViewModel: MainViewModel
    private val songViewModel:SongViewModel by viewModels()

    private var curPlayingSong:Song? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    setCurPlayerTimeTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(seekBar.progress.toLong())
                    shouldUpdateSeekBar = true
                }

            }
        })
        subscribeToObservers()

        ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggle(it,true)
            }
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPrevSong()
        }


    }

    private fun updateTitleAndImage(song: Song){
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.imageUrl).into(ivSongImage)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItem.observe(viewLifecycleOwner){result->
            when(result.status){
                SUCCESS->{
                    result.data?.let {songs->
                        if(curPlayingSong == null && songs.isNotEmpty()){
                            curPlayingSong = songs[0]
                            updateTitleAndImage(songs[0])
                        }
                    }
                }
                else-> Unit
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if (it ==null)return@observe
            curPlayingSong = it.toSong()
            updateTitleAndImage(curPlayingSong?:return@observe)
        }


        mainViewModel.playbackState.observe(viewLifecycleOwner){
            curPlaybackState = it
            it?.let {
                ivPlayPauseDetail.setImageResource(if (it.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            }

            seekBar.progress = it?.position?.toInt()?:0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner){
            if(shouldUpdateSeekBar){
                seekBar.progress = it.toInt()
                setCurPlayerTimeTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner){
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it-30*60*1000L)

        }
    }

    private fun setCurPlayerTimeTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms-30*60*1000L)
    }
}