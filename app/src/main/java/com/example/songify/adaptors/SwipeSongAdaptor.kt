package com.example.songify.adaptors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.songify.data.entities.Song
import com.example.songify.databinding.SwipeItemBinding

class SwipeSongAdaptor : RecyclerView.Adapter<SwipeSongAdaptor.SwipeSongViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ: AsyncListDiffer<Song> = AsyncListDiffer(this,diffCallback)

    var songs : List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SwipeSongViewHolder(val binding: SwipeItemBinding) : RecyclerView.ViewHolder(binding.root)



    private var itemClickListener : ((Song) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeSongViewHolder {
        val binding = SwipeItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SwipeSongViewHolder(binding)
    }


    fun setOnItemClickListener(listener : (Song) -> Unit){
        itemClickListener = listener
    }
    override fun onBindViewHolder(holder: SwipeSongViewHolder, position: Int) {
        val song = songs[position]

        holder.binding.apply {
            val text = "${song.title} - ${song.subtitle}"
            tvPrimary.text = text

            root.setOnClickListener {
                itemClickListener?.let { click->
                    click(song)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

}