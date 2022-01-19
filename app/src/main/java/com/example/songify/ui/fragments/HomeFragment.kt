package com.example.songify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.songify.R
import com.example.songify.adaptors.SongAdaptor
import com.example.songify.databinding.FragmentHomeBinding
import com.example.songify.other.Status
import com.example.songify.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var mainViewModel: MainViewModel
    private var _binding : FragmentHomeBinding? = null
    val binding get() = _binding!!

    @Inject
    lateinit var songAdaptor: SongAdaptor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        subscribeToObserve()


        binding.allSongsProgressBar.visibility = INVISIBLE
        setupRecyclerView()





        songAdaptor.setOnItemClickListener{
            mainViewModel.playOrToggle(it,true)
        }
    }


    private fun setupRecyclerView(){

        Timber.e("RecyclerView Added")
        binding.rvAllSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllSongs.adapter = songAdaptor

    }

    private fun subscribeToObserve(){
        mainViewModel.mediaItem.observe(viewLifecycleOwner){result->
            when(result.status){
                Status.SUCCESS->{
                    binding.allSongsProgressBar.isVisible = false
                    result.data?.let { songs->
                        songAdaptor.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING ->binding.allSongsProgressBar.isVisible = true
            }
        }
    }
}