package com.example.razvojmobilnihaplikacijaprezentacija

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // Standardni import
import androidx.compose.material.icons.filled.Pause // Import za Pause ikonu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.razvojmobilnihaplikacijaprezentacija.ui.theme.RazvojMobilnihAplikacijaPrezentacijaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch // Osigurajte da je MainScope importiran ako ga koristite ili koristite rememberCoroutineScope
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RazvojMobilnihAplikacijaPrezentacijaTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(navController = navController)
        }
        composable("audio_player_screen") {
            AudioPlayerScreen(navController = navController)
        }
        composable("video_player_screen") {
            VideoPlayerScreen(navController = navController)
        }
        composable("background_audio_screen") {
            BackgroundAudioScreen(navController = navController)
        }
        composable("video_effects_screen") {
            VideoEffectsScreen(navController = navController)
        }
        composable("photo_viewer_screen") {
            PhotoViewerScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Multimedija Demo") }) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("audio_player_screen") }, modifier = Modifier.fillMaxWidth()) {
                Text("1. Jednostavan Audio Player")
            }
            Button(onClick = { navController.navigate("video_player_screen") }, modifier = Modifier.fillMaxWidth()) {
                Text("2. Jednostavan Video Player")
            }
            Button(onClick = { navController.navigate("background_audio_screen") }, modifier = Modifier.fillMaxWidth()) {
                Text("3. Audio Player (Pozadinska reprodukcija - Koncept)")
            }
            Button(onClick = { navController.navigate("video_effects_screen") }, modifier = Modifier.fillMaxWidth()) {
                Text("4. Video Efekti (Koncept)")
            }
            Button(onClick = { navController.navigate("photo_viewer_screen") }, modifier = Modifier.fillMaxWidth()) {
                Text("5. Prikazivač Fotografija")
            }
        }
    }
}

// Helper funkcija za formatiranje vremena (premještena na top-level)
fun formatTime(timeMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    // val audioUri = RawResourceDataSource.buildRawResourceUri(R.raw.sample_audio)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            // setMediaItem(MediaItem.fromUri(audioUri))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope() // Koristite rememberCoroutineScope

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                currentPosition = player.currentPosition
                duration = player.duration.coerceAtLeast(0L)
            }
        }
        exoPlayer.addListener(listener)

        val job = coroutineScope.launch { // Koristite scope od rememberCoroutineScope
            while (true) {
                if (exoPlayer.isPlaying) {
                    currentPosition = exoPlayer.currentPosition
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
                delay(1000)
            }
        }

        onDispose {
            exoPlayer.removeListener(listener)
            job.cancel()
            exoPlayer.release()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> { /* Možda želite nastaviti ako je prethodno sviralo */ }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Player") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag") // Koristi standardnu ikonu
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = MediaItem.fromUri(audioUrl).mediaMetadata.title?.toString() ?: audioUrl.substringAfterLast('/'), // Prikaz naziva datoteke kao fallback
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                onValueChange = { sliderValue ->
                    exoPlayer.seekTo((sliderValue * duration).toLong())
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text("${formatTime(currentPosition)} / ${formatTime(duration)}")
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, // Ispravljena ikona
                    contentDescription = if (isPlaying) "Pauza" else "Play",
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@SuppressLint("OpaqueUnitKey")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            // playWhenReady = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> { /* exoPlayer.play() */ }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Player") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag") // Koristi standardnu ikonu
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundAudioScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pozadinska Reprodukcija (Koncept)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag") // Koristi standardnu ikonu
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Za potpunu pozadinsku reprodukciju, potrebno je implementirati MediaSessionService i MediaSession.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Ovo omogućuje da reprodukcija nastavi raditi kada je aplikacija u pozadini te pruža kontrole putem sistemskih notifikacija i vanjskih uređaja (npr. Bluetooth slušalice, pametni satovi).",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Ovdje bi se pokrenuo servis */ }) {
                Text("Pokreni pozadinsku reprodukciju (simulacija)")
            }
            Text(
                "Media3 biblioteka (media3-session) znatno pojednostavljuje ovu implementaciju.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEffectsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Efekti (Koncept)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag") // Koristi standardnu ikonu
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Media3 Transformer API omogućuje primjenu audio i video efekata, promjenu formata, rezanje i spajanje medijskih datoteka.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Primjer korištenja Transformer API-ja:\n" +
                        "1. Stvorite `EditedMediaItem` s izvornim `MediaItem` i popisom efekata.\n" +
                        "2. Stvorite `Transformer` instancu.\n" +
                        "3. Pozovite `transformer.start(editedMediaItem, outputPath)`.\n" +
                        "4. Pratite napredak pomoću `Transformer.Listener`.",
                textAlign = TextAlign.Justify
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Ovdje bi se pokrenula transformacija */ }) {
                Text("Primijeni efekt (simulacija)")
            }
            Text(
                "Potrebna je ovisnost: implementation(\"androidx.media3:media3-transformer:1.X.X\")",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(navController: NavHostController) {
    val imageResId = try {
        R.drawable.sample_image
    } catch (e: Exception) {
        android.R.drawable.ic_menu_gallery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prikazivač Fotografija") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag") // Koristi standardnu ikonu
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Prikaz slike iz resursa aplikacije:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Primjer fotografije",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (imageResId == android.R.drawable.ic_menu_gallery) {
                Text("Napomena: Prikazuje se zamjenska ikona. Dodajte 'sample_image.jpg' u res/drawable.", color = Color.Red)
            }
            Text(
                "Za prikaz slika s interneta, koristite biblioteke poput Coil ili Glide. Za prikaz slika s uređaja, potrebno je rukovati dozvolama (npr. READ_MEDIA_IMAGES).",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
