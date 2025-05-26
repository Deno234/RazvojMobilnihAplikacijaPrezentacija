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

class MediaPlaybackService : MediaSessionService() { // MediaSessionService - upravlja MediaSession objektom, služi za pokretanje i upravljanje reprodukcijom u pozadini.

    // Omogućava kontrolu i komuniciranje s media playerom
    private var mediaSession: MediaSession? = null

    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true) // postavlja audio atribute za reprodukciju (glasnoća, tip audio toka...) i audio fokus
            .setHandleAudioBecomingNoisy(true) // pauzira ako npr. korisnik isključi slušalice
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("MediaPlaybackService", "Player state: $playbackState")
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d("MediaPlaybackService", "Player isPlaying: $isPlaying")
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason) // prelazak na novi medijski sadržaj
                Log.d("MediaPlaybackService", "Service MediaItem transitioned: ${mediaItem?.mediaId}")
            }
        })

        // Intent za pokretanje MainActivityja kada se klikne na notifikaciju (ili neki UI element vezan za sjednicu)
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
            // FLAG_IMMUTABLE -> intent se ne može mijenjati nakon stvaranja, FLAG_UPDATE_CURRENT -> ako već postoji PendingIntent, ažuriraj ga novim podacima
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent) // Povezivanje PendingIntent-a (za klikanje na notifikaciju ili kontrolu sesije)
            .build()

    }

    // Kada klijent (npr. notifikacija) želi dobiti pristup MediaSession
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            this.player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

}
