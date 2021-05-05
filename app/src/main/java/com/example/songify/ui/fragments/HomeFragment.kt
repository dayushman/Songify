package com.example.songify.ui.fragments

import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.songify.R
import com.example.songify.adaptors.SongAdaptor
import com.example.songify.other.Status
import com.example.songify.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdaptor: SongAdaptor


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        subscribeToObserve()


        allSongsProgressBar.visibility = INVISIBLE
        setupRecyclerView()





        songAdaptor.setOnItemClickListener{
            mainViewModel.playOrToggle(it,true)
        }
    }


    private fun setupRecyclerView(){

        Timber.e("RecyclerView Added")
        rvAllSongs.layoutManager = LinearLayoutManager(requireContext())
        rvAllSongs.adapter = songAdaptor

    }

    private fun subscribeToObserve(){
        mainViewModel.mediaItem.observe(viewLifecycleOwner){result->
            when(result.status){
                Status.SUCCESS->{
                    allSongsProgressBar.isVisible = false
                    result.data?.let { songs->
                        songAdaptor.songs = songs
                    }

                }
                Status.ERROR -> Unit
                Status.LOADING -> allSongsProgressBar.isVisible = true
            }
        }
    }
}