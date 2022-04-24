package az.zero.azchat.di.media

import android.content.Context
import android.media.MediaPlayer
import az.zero.azchat.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext


@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun getMediaPlayer(@ActivityContext context: Context): MediaPlayer =
        MediaPlayer.create(context, R.raw.send_message_sound)

}