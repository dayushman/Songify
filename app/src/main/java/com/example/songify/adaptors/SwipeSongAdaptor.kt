package com.example.songify.adaptors

import androidx.recyclerview.widget.AsyncListDiffer
import com.example.songify.R
import com.example.songify.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.tvPrimary

class SwipeSongAdaptor:BaseSongAdaptor(R.layout.swipe_item) {


    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.itemView.apply {
            val text = "${song.title} - ${song.subtitle}"
            tvPrimary.text = text

            setOnClickListener {
                itemClickListener?.let { click->
                    click(song)
                }
            }
        }
    }
}