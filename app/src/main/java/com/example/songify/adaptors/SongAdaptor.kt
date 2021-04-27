package com.example.songify.adaptors

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.songify.R
import com.example.songify.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SongAdaptor @Inject constructor(
    private val glide : RequestManager
):BaseSongAdaptor(R.layout.list_item) {


    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.itemView.apply {
            tvPrimary.text = songs[position].title

            tvSecondary.text = songs[position].subtitle
            glide.load(song.imageUrl)
                .into(ivItemImage)

            setOnClickListener {
                itemClickListener?.let { click->
                    click(song)
                }

            }
        }
    }
}