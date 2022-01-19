package com.example.songify.adaptors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.songify.data.entities.Song
import com.example.songify.databinding.ListItemBinding
import javax.inject.Inject

class SongAdaptor @Inject constructor(
    private val glide : RequestManager
) : RecyclerView.Adapter<SongAdaptor.ListItemViewHolder>(){


    private var itemClickListener : ((Song) -> Unit)? = null
    class ListItemViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)



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

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val song = songs[position]

        holder.binding.apply {
            tvPrimary.text = songs[position].title

            tvSecondary.text = songs[position].subtitle
            glide.load(song.imageUrl)
                .into(ivItemImage)

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

    fun setOnItemClickListener(listener : (Song) -> Unit){
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ListItemViewHolder(binding)
    }
}