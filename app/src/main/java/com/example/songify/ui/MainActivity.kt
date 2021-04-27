package com.example.songify.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.songify.R
import com.example.songify.adaptors.SwipeSongAdaptor
import com.example.songify.data.entities.Song
import com.example.songify.exoplayer.isPlaying
import com.example.songify.exoplayer.toSong
import com.example.songify.other.Status.*
import com.example.songify.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private val mainViewModel :MainViewModel by viewModels()

    @Inject
    lateinit var  swipeSongAdaptor: SwipeSongAdaptor

    @Inject
    lateinit var glide:RequestManager

    private var curPlayingSong : Song? = null
    private var curPlaybackState : PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObserver()
        vpSong.adapter = swipeSongAdaptor


        ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggle(it,true)
            }
        }
        swipeSongAdaptor.setOnItemClickListener{
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (curPlaybackState?.isPlaying == true){
                    mainViewModel.playOrToggle(swipeSongAdaptor.songs[position])
                }
                else{
                    curPlayingSong = swipeSongAdaptor.songs[position]
                }
            }
        })
        navHostFragment.findNavController().addOnDestinationChangedListener {  _,destination,_->
            when(destination.id){
                R.id.songFragment -> {
                    hideBottomBar()
                    supportActionBar?.hide()
                }
                R.id.homeFragment -> {
                    showBottomBar()
                    supportActionBar?.show()
                }
                else -> showBottomBar()
            }
        }
    }

    private fun hideBottomBar(){
        ivPlayPause.visibility = INVISIBLE
        ivCurSongImage.visibility = INVISIBLE
        vpSong.visibility = INVISIBLE
    }

    private fun showBottomBar(){
        ivPlayPause.visibility = VISIBLE
        ivCurSongImage.visibility = VISIBLE
        vpSong.visibility = VISIBLE
    }

    private fun switchViewPagerToCurrentSong(song: Song){
        val currentIndex = swipeSongAdaptor.songs.indexOf(song)

        if (currentIndex!=-1){
            vpSong.currentItem = currentIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObserver(){
        mainViewModel.mediaItem.observe(this){
            it?.let { result->
                when(result.status){
                    SUCCESS->{
                        result.data?.let {songs->
                            swipeSongAdaptor.songs = songs
                            if (songs.isNotEmpty())
                                glide.load((curPlayingSong?:songs[0]).imageUrl).into(ivCurSongImage)
                            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
                        }
                    }
                    ERROR->Unit
                    LOADING->Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(this){
            if (it ==null)return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
        }

        mainViewModel.playbackState.observe(this){
            it?.let {
                curPlaybackState = it
                ivPlayPause.setImageResource(if (it.isPlaying)R.drawable.ic_pause else R.drawable.ic_play)
            }
        }

        mainViewModel.isConnected.observe(this){
            it.getContentIfNotHandled()?.let { resource->
                when(resource.status){
                    ERROR->Snackbar.make(rootLayout,resource.message?:"Some Error Occurred!",Snackbar.LENGTH_LONG).show()
                    else->Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){

        }
    }










}