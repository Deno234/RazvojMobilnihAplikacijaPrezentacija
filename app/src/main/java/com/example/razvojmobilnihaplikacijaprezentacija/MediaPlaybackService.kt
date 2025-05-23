package com.example.razvojmobilnihaplikacijaprezentacija

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        Log.d("MediaPlaybackService", "onCreate called")

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true) // handleAudioFocus = true
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("MediaPlaybackService", "Player state: $playbackState")
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d("MediaPlaybackService", "Player isPlaying: $isPlaying")
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Log.d("MediaPlaybackService", "Service MediaItem transitioned: ${mediaItem?.mediaId}")
            }
        })

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("NAVIGATE_TO_SCREEN", "background_audio_screen")
        }

        // Intent za pokretanje UI-a iz notifikacije
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        // Ako želite da se notifikacija odmah pojavi i da servis bude foreground,
        // možete to eksplicitno postaviti ovdje ili kroz MediaNotification.Provider.
        // Media3 to uglavnom rješava automatski kada player počne svirati.
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("MediaPlaybackService", "onGetSession called for ${controllerInfo.packageName}")
        return mediaSession
    }

    override fun onDestroy() {
        Log.d("MediaPlaybackService", "onDestroy called")
        mediaSession?.run {
            this.player.release() // Player je dio session-a
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    // Opcionalno: Prilagodba ponašanja kada se aplikacija ukloni iz nedavnih zadataka
    // override fun onTaskRemoved(rootIntent: Intent?) {
    //     val currentPlayer = mediaSession?.player
    //     if (currentPlayer != null && (!currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0)) {
    //         // Zaustavi servis ako ne svira ili nema pjesama
    //         stopSelf()
    //     }
    //     // Inače, ako svira, servis će nastaviti raditi (defaultno ponašanje)
    // }
}
