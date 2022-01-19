package com.example.songify.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.songify.R
import com.example.songify.adaptors.SwipeSongAdaptor
import com.example.songify.data.entities.Song
import com.example.songify.databinding.ActivityMainBinding
import com.example.songify.exoplayer.isPlaying
import com.example.songify.exoplayer.toSong
import com.example.songify.other.Status.*
import com.example.songify.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }*/

    private val mainViewModel :MainViewModel by viewModels()

    @Inject
    lateinit var  swipeSongAdaptor: SwipeSongAdaptor

    @Inject
    lateinit var glide:RequestManager

    private lateinit var binding: ActivityMainBinding

    private var curPlayingSong : Song? = null
    private var curPlaybackState : PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObserver()
        binding.vpSong.adapter = swipeSongAdaptor
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController


        binding.ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggle(it,true)
            }
        }
        swipeSongAdaptor.setOnItemClickListener{
            navController.navigate(R.id.globalActionToSongFragment)

        }

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
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
        navController.addOnDestinationChangedListener {  _,destination,_->
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
        binding.ivPlayPause.visibility = INVISIBLE
        binding.ivCurSongImage.visibility = INVISIBLE
        binding.vpSong.visibility = INVISIBLE
    }

    private fun showBottomBar(){
        binding.ivPlayPause.visibility = VISIBLE
        binding.ivCurSongImage.visibility = VISIBLE
        binding.vpSong.visibility = VISIBLE
    }

    private fun switchViewPagerToCurrentSong(song: Song){
        val currentIndex = swipeSongAdaptor.songs.indexOf(song)

        if (currentIndex!=-1){
            binding.vpSong.currentItem = currentIndex
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
                                glide.load((curPlayingSong?:songs[0]).imageUrl).into(binding.ivCurSongImage)
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
            glide.load(curPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
        }

        mainViewModel.playbackState.observe(this){
            it?.let {
                curPlaybackState = it
                binding.ivPlayPause.setImageResource(if (it.isPlaying)R.drawable.ic_pause else R.drawable.ic_play)
            }
        }

        mainViewModel.isConnected.observe(this){
            it.getContentIfNotHandled()?.let { resource->
                when(resource.status){
                    ERROR->Snackbar.make(binding.rootLayout,resource.message?:"Some Error Occurred!",Snackbar.LENGTH_LONG).show()
                    else->Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){

        }
    }










}