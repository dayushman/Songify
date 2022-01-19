package com.example.songify.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.songify.R
import com.example.songify.adaptors.SwipeSongAdaptor
import com.example.songify.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)


    @Singleton
    @Provides
    fun provideSwipeSongAdaptor() = SwipeSongAdaptor()



    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .error(R.drawable.image_not_found)
            .placeholder(R.drawable.image_loading)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )
}