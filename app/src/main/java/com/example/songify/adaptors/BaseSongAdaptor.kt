package com.example.songify.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.songify.data.entities.Song

abstract class BaseSongAdaptor(private val layoutId: Int) : RecyclerView.Adapter<BaseSongAdaptor.SongViewHolder>(){


    protected abstract val differ:AsyncListDiffer<Song>
    var songs : List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    protected val diffCallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(LayoutInflater.from(parent.context).inflate(layoutId,parent,false))
    }



    override fun getItemCount(): Int {
        return songs.size
    }



    protected var itemClickListener : ((Song) -> Unit)? = null


    fun setOnItemClickListener(listener : (Song) -> Unit){
        itemClickListener = listener
    }
}