package com.example.songify.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.songify.data.entities.Song

fun MediaMetadataCompat.toSong():Song?{
    return this.description?.let {
        Song(
            it.mediaId.toString(),
            it.title.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString(),
            it.subtitle.toString()
        )
    }
}