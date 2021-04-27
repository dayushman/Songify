package com.example.songify.data.remote

import com.example.songify.data.entities.Song

import com.example.songify.other.Constants.COLLECTION_SONGS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(COLLECTION_SONGS)

    suspend fun getAllSongs():List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        }
        catch (e:Exception){
            emptyList()
        }
    }
}