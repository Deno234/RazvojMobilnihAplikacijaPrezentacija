package com.example.razvojmobilnihaplikacijaprezentacija

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import android.os.Environment
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.media3.common.Effect
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.media3.transformer.Effects
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import androidx.media3.effect.Contrast
import androidx.compose.material3.Slider
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.PlaybackException
import androidx.media3.effect.Brightness
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigateToScreen = intent.getStringExtra("NAVIGATE_TO_SCREEN")
        enableEdgeToEdge()
        setContent {
            RazvojMobilnihAplikacijaPrezentacijaTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController, startDestinationFromIntent = navigateToScreen)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, startDestinationFromIntent: String?) {
    // Određivanje stvarne početne destinacije
    val actualStartDestination = if (startDestinationFromIntent == "background_audio_screen") {
        "background_audio_screen"
    } else {
        "main_screen"
    }
    Log.d("AppNavigation", "Actual start destination: $actualStartDestination")


    NavHost(navController = navController, startDestination = actualStartDestination) {
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
        composable("image_detail_screen/{imageUri}") { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString("imageUri")
            imageUriString?.let {
                ImageDetailScreen(navController = navController, imageUri = it.toUri()) // Proslijedi i navController za natrag
            }
        }
    }

    LaunchedEffect(startDestinationFromIntent) {
        if (startDestinationFromIntent == "background_audio_screen" && navController.currentDestination?.route != "background_audio_screen") {
            Log.d("AppNavigation", "Navigating to background_audio_screen due to intent.")
            navController.navigate("background_audio_screen") {
                popUpTo("main_screen") { inclusive = false } // Ostavljanje main_screen u backstacku
                launchSingleTop = true // Izbjegavaj višestruke instance istog ekrana
            }
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

fun formatTime(timeMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class) //Zbog TopAppBar-a
@Composable
fun AudioPlayerScreen(navController: NavHostController) {
    val context = LocalContext.current

    var inputUrl by remember { mutableStateOf("") } // URL audio datoteke
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) } // URI lokalno odabrane datoteke
    var currentMediaItemForPlayer by remember { mutableStateOf<MediaItem?>(null) } // Trenutni MediaItem za reprodukciju pomoću ExoPlayer-a

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var isPlaying by remember { mutableStateOf(false) } // Je li reprodukcija aktivna
    var currentPosition by remember { mutableLongStateOf(0L) } // Trenutna pozicija reprodukcije
    var duration by remember { mutableLongStateOf(0L) } // Ukupno trajanje videozapisa
    val coroutineScope = rememberCoroutineScope() // periodično ažurira currentPosition

    // Odabir audio datoteke
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(), // Sustavni preglednik datoteka
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    // Dobivanje trajnog pristupa datoteci
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                    // Postavljanje trenutnog MediaItem-a za reprodukciju
                    selectedFileUri = it
                    inputUrl = ""
                    currentMediaItemForPlayer = MediaItem.fromUri(it)
                } catch (e: SecurityException) {
                    Log.e("AudioPlayer", "Nije moguće dobiti trajnu dozvolu za URI: $uri", e)
                    Toast.makeText(context, "Greška pri odabiru datoteke: dozvola odbijena.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    // Ažuriranje ExoPlayer-a kada se promijeni currentMediaItemForPlayer pomoću LaunchedEffect-a
    LaunchedEffect(currentMediaItemForPlayer) {
        currentMediaItemForPlayer?.let { item ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(item)
            exoPlayer.prepare() // Učita datoteku (ali još ne svira)
            isPlaying = false
            currentPosition = 0L
            duration = 0L // Trajanje će se postaviti kada player bude spreman
        } ?: run { // Ako je currentMediaItemForPlayer null (npr. nakon "Očisti")
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            isPlaying = false
            currentPosition = 0L
            duration = 0L
        }
    }

    /*
    DisposableEffect je Compose funkcija koja:
    se aktivira kad se stvori Composable koji koristi exoPlayer
    se očisti automatski kad taj Composable nestane s ekrana (ili se exoPlayer promijeni)
    */
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener { // listener koji sluša događaje iz ExoPlayer-a i reagira na njih

            // Ažurira Composable varijablu isPlaying kada se promijeni stanje (play/pause)
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    currentPosition = exoPlayer.duration.coerceAtLeast(0L)
                    isPlaying = false
                    exoPlayer.seekTo(0)
                    exoPlayer.pause()
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {

                // EVENT_TIMELINE_CHANGED -> promijenjen redoslijed pjesama, EVENT_PLAYBACK_STATE_CHANGED -> promijenjeno stanje playera
                if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    if (player.playbackState == Player.STATE_READY) {
                        duration = player.duration.coerceAtLeast(0L)
                    }
                }

                // Ažuriraj redovno currentPosition, EVENT_POSITION_DISCONTINUITY -> korisnik je promijenio poziciju ručno
                if (isPlaying || events.contains(Player.EVENT_POSITION_DISCONTINUITY) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    currentPosition = player.currentPosition
                }
            }

        }

        exoPlayer.addListener(listener)

        // Svakih 200 ms se ažurira currentPosition (dok player svira)
        val job = coroutineScope.launch {
            while (true) {
                if (exoPlayer.playbackState == Player.STATE_READY && exoPlayer.isPlaying) {
                    currentPosition = exoPlayer.currentPosition
                }
                delay(200)
            }
        }

        // Oslobađanje resursa kada Composable nestane
        onDispose {
            exoPlayer.removeListener(listener)
            job.cancel()
            exoPlayer.release()
        }

    }

    // Upravljanje reprodukcijom ovisno o životnom ciklusu composable-a
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current //dohvaćanje trenutnog životnog ciklusa ekrana
    DisposableEffect(lifecycleOwner, exoPlayer) { // Pokretanje ovisno o promjeni lifecycleOwner-a i exoPlayer-a
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val displayTitle = currentMediaItemForPlayer?.mediaMetadata?.title?.toString() // Title u metadata (npr. MP3 tag)
        ?: selectedFileUri?.lastPathSegment // Title iz URI-ja ako je lokalno odabrana datoteka
        ?: if (inputUrl.isNotBlank() && currentMediaItemForPlayer?.mediaId == inputUrl) inputUrl.substringAfterLast('/') else null // Naziv iz URL-a
            ?: "Nema učitanog medija"

    Scaffold( // glavni container za ekran s elementima poput TopBar, BottomBar...
        topBar = {
            TopAppBar(
                title = { Text("Audio Player") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                }
            )
        }
    ) { innerPadding -> // uklapanje sadržaja ispod TopBar-a
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Unos URL-a
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("Unesite URL MP3 datoteke") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    if (inputUrl.isNotBlank()) {
                        try {
                            currentMediaItemForPlayer = MediaItem.fromUri(inputUrl)
                            selectedFileUri = null
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Neispravan URL: $inputUrl", e)
                            Toast.makeText(context, "Neispravan URL", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Molimo unesite URL", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Učitaj s URL-a")
            }

            Text("ILI", modifier = Modifier.padding(vertical = 8.dp))

            // Odabir datoteke s uređaja
            Button(
                onClick = {
                    audioPickerLauncher.launch(arrayOf("audio/mpeg", "audio/mp4", "video/mp4"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Odaberi datoteku", modifier = Modifier.padding(end = 8.dp))
                Text("Odaberi datoteku s uređaja")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gumb za čišćenje odabira
            if (currentMediaItemForPlayer != null) {
                Button(
                    onClick = {
                        currentMediaItemForPlayer = null
                        inputUrl = ""
                        selectedFileUri = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Očisti odabir", modifier = Modifier.padding(end = 8.dp))
                    Text("Očisti odabir")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { sliderValue ->
                        if (exoPlayer.playbackState == Player.STATE_READY) {
                            exoPlayer.seekTo((sliderValue * duration).toLong())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentMediaItemForPlayer != null && exoPlayer.playbackState == Player.STATE_READY
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(formatTime(duration), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = {
                        if (currentMediaItemForPlayer != null) {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_ENDED) {
                                    exoPlayer.prepare()
                                    exoPlayer.seekTo(0)
                                }
                                exoPlayer.play()
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = currentMediaItemForPlayer != null
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pauza" else "Play",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@SuppressLint("OpaqueUnitKey") // Za DisposableEffect(Unit)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(navController: NavHostController) {
    val context = LocalContext.current

    var inputUrl by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var currentMediaItemForPlayer by remember { mutableStateOf<MediaItem?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var isPlaying by remember { mutableStateOf(false) }

    // Odabir video datoteke
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                    selectedFileUri = it
                    inputUrl = ""
                    currentMediaItemForPlayer = MediaItem.fromUri(it)
                } catch (e: SecurityException) {
                    Log.e("VideoPlayer", "Nije moguće dobiti trajnu dozvolu za URI: $uri", e)
                    Toast.makeText(context, "Greška pri odabiru datoteke: dozvola odbijena.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    LaunchedEffect(currentMediaItemForPlayer) {
        currentMediaItemForPlayer?.let { item ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(item)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true // Počni reprodukciju čim je spremno
        } ?: run {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, exoPlayer) {

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
        }

        exoPlayer.addListener(listener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            exoPlayer.removeListener(listener)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Oslobađanje playera kada composable napusti stablo (dešava se samo jednom -> Unit kao ključ)
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val displayTitle = currentMediaItemForPlayer?.mediaMetadata?.title?.toString()
        ?: selectedFileUri?.lastPathSegment
        ?: if (inputUrl.isNotBlank() && currentMediaItemForPlayer?.mediaId == inputUrl) inputUrl.substringAfterLast('/') else null
            ?: "Nema učitanog videa"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Player") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("Unesite URL video datoteke") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    if (inputUrl.isNotBlank()) {
                        try {
                            currentMediaItemForPlayer = MediaItem.fromUri(inputUrl)
                            selectedFileUri = null
                        } catch (e: Exception) {
                            Toast.makeText(context, "Neispravan URL", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Molimo unesite URL", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Učitaj s URL-a")
            }

            Text("ILI", modifier = Modifier.padding(vertical = 8.dp))

            Button(
                onClick = {
                    videoPickerLauncher.launch(arrayOf("video/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Odaberi datoteku", modifier = Modifier.padding(end = 8.dp))
                Text("Odaberi video s uređaja")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentMediaItemForPlayer != null) {
                Button(
                    onClick = {
                        currentMediaItemForPlayer = null
                        inputUrl = ""
                        selectedFileUri = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Očisti odabir", modifier = Modifier.padding(end = 8.dp))
                    Text("Očisti odabir")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(displayTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1)

            Spacer(modifier = Modifier.height(8.dp))

            // PlayerView za prikaz videozapisa
            if (currentMediaItemForPlayer != null) {
                AndroidView(
                    factory = { ctx -> // stvaranje novog PlayerView-a
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            // useController = true (default)
                            // controllerShowTimeoutMs = 3000 -> 3 sekundi će ostati kontrole playera vidljive nakon što se pojave
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Odaberite video za reprodukciju.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundAudioScreen(navController: NavHostController) {
    val context = LocalContext.current

    var inputUrl by remember { mutableStateOf("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    var mediaController by remember { mutableStateOf<MediaController?>(null) } // Stanje koje će držati instancu MediaController-a nakon što se uspostavi veza sa servisom
    var controllerFutureHolder by remember { mutableStateOf<ListenableFuture<MediaController>?>(null) } // Budući objekt (asinkrono dobivanje MediaController-a)

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentMediaIdInController by remember { mutableStateOf<String?>(null) } // ID trenutno učitanog medija u MediaController

    // Povezivanje s MediaControllerom (koji kontrolira pozadinsku reprodukciju kroz MediaPlaybackService)
    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, MediaPlaybackService::class.java)) // identificira servis za reprodukciju s kojim želimo komunicirati - MediaPlaybackService
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFutureHolder = future

        future.addListener({
            try {
                val controller = future.get() // Kada je MediaController dostupan
                mediaController = controller

                val playerListener = object : Player.Listener {

                    override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                        isPlaying = isPlayingValue
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                            duration = controller.duration.coerceAtLeast(0L)
                            currentPosition = controller.currentPosition.coerceAtLeast(0L)
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            isPlaying = false
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        currentMediaIdInController = mediaItem?.mediaId
                        duration = controller.duration.coerceAtLeast(0L)
                        currentPosition = controller.currentPosition.coerceAtLeast(0L)
                        isPlaying = controller.isPlaying
                    }

                }

                controller.addListener(playerListener)

                isPlaying = controller.isPlaying
                duration = controller.duration.coerceAtLeast(0L)
                currentPosition = controller.currentPosition.coerceAtLeast(0L)
                currentMediaIdInController = controller.currentMediaItem?.mediaId

            } catch (e: Exception) {
                Log.e("BackgroundAudioScreen", "Greška pri povezivanju MediaController-a", e)
                Toast.makeText(context, "Greška: Audio servis nije dostupan.", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            controllerFutureHolder?.let { MediaController.releaseFuture(it) }
            mediaController = null
            controllerFutureHolder = null
        }
    }

    // Korutina za periodično ažuriranje pozicije slidera
    LaunchedEffect(mediaController, isPlaying) {
        if (mediaController != null && isPlaying) {
            while (isPlaying && mediaController != null) {
                currentPosition = mediaController?.currentPosition?.coerceAtLeast(0L) ?: currentPosition
                delay(250)
            }
        } else if (mediaController != null && !isPlaying) {
            currentPosition = mediaController?.currentPosition?.coerceAtLeast(0L) ?: currentPosition
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                if (mediaController == null) {
                    Toast.makeText(context, "Audio servis nije spreman.", Toast.LENGTH_SHORT).show()
                    return@let
                }
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                    selectedFileUri = it
                    inputUrl = ""

                    val mediaItem = MediaItem.Builder().setUri(it).setMediaId(it.toString()).build()
                    mediaController?.setMediaItem(mediaItem)
                    mediaController?.prepare()
                    // mediaController?.play() -> automatski pokreni
                } catch (e: SecurityException) {
                    Toast.makeText(context, "Greška pri odabiru datoteke: dozvola odbijena.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    val displayTitle = mediaController?.currentMediaItem?.mediaMetadata?.title?.toString()
        ?: mediaController?.currentMediaItem?.mediaId?.substringAfterLast('/')
        ?: "Nema učitanog medija"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Player (Pozadina)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("Unesite URL audio datoteke") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = mediaController != null
            )
            Button(
                onClick = {
                    if (mediaController == null) {
                        Toast.makeText(context, "Audio servis nije spreman.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (inputUrl.isNotBlank()) {
                        try {
                            val mediaItem = MediaItem.Builder().setUri(inputUrl).setMediaId(inputUrl).build()
                            mediaController?.setMediaItem(mediaItem)
                            mediaController?.prepare()
                            selectedFileUri = null
                        } catch (e: Exception) {
                            Toast.makeText(context, "Neispravan URL $inputUrl", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                enabled = mediaController != null
            ) {
                Text("Učitaj s URL-a")
            }

            Text("ILI", modifier = Modifier.padding(vertical = 8.dp))

            Button(
                onClick = {
                    if (mediaController != null) {
                        audioPickerLauncher.launch(arrayOf("audio/*", "video/mp4"))
                    } else {
                        Toast.makeText(context, "Audio servis nije spreman.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mediaController != null
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Odaberi datoteku", modifier = Modifier.padding(end = 8.dp))
                Text("Odaberi audio s uređaja")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mediaController?.currentMediaItem != null || isPlaying) {
                Button(
                    onClick = {
                        mediaController?.stop()
                        mediaController?.clearMediaItems()
                        inputUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                        selectedFileUri = null
                        // Stanja isPlaying, duration, currentPosition će se ažurirati kroz listener na kontroleru
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = mediaController != null
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Očisti odabir", modifier = Modifier.padding(end = 8.dp))
                    Text("Očisti odabir")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { sliderValue ->
                        mediaController?.seekTo((sliderValue * duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = mediaController != null && (mediaController?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) == true) && duration > 0
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(formatTime(duration), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = {
                        mediaController?.let { controller ->
                            if (controller.isPlaying) {
                                controller.pause()
                            } else {
                                if (controller.playbackState == Player.STATE_IDLE && controller.currentMediaItem == null) {
                                    Toast.makeText(context, "Nema medija za reprodukciju.", Toast.LENGTH_SHORT).show()
                                    return@let
                                }
                                controller.play()
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = mediaController != null
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pauza" else "Play",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class) // Zbog korištenja Transformer API-ja i Player.setVideoEffects
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEffectsScreen(navController: NavHostController) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope() // pokretanje asinkronih operacija (prikaz video transformacije)
    val snackbarHostState = remember { SnackbarHostState() }

    var inputVideoUri by remember { mutableStateOf<Uri?>(null) }
    var outputVideoUri by remember { mutableStateOf<Uri?>(null) }

    var transformationProgress by remember { mutableFloatStateOf(0f) }
    var isTransforming by remember { mutableStateOf(false) }

    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var applyContrast by remember { mutableStateOf(false) }
    var contrastValue by remember { mutableFloatStateOf(0f) }
    var applyBrightness by remember { mutableStateOf(false) }
    var brightnessValue by remember { mutableFloatStateOf(0f) }
    var applyMirror by remember { mutableStateOf(false) }

    // ExoPlayer za live preview efekata
    val previewExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE // ponavljaj zauvijek
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Greška pri prikazu previewa: ${error.errorCodeName}",
                            duration = SnackbarDuration.Long
                        )
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d("PreviewExoPlayer", "Playback State: $playbackState")
                }
            })
        }
    }

    // Prikaz live preview-a sa primenjenim efektima (samo nekim)
    LaunchedEffect(inputVideoUri, rotationDegrees) {
        inputVideoUri?.let { uri ->
            val currentEffectsList = mutableListOf<Effect>()
            if (rotationDegrees != 0f) {
                currentEffectsList.add(ScaleAndRotateTransformation.Builder().setRotationDegrees(rotationDegrees).build())
            }

            previewExoPlayer.stop()
            previewExoPlayer.clearMediaItems()

            previewExoPlayer.setVideoEffects(currentEffectsList)
            previewExoPlayer.setMediaItem(MediaItem.fromUri(uri))
            previewExoPlayer.prepare()
            // previewExoPlayer.playWhenReady = true -> automatski pokreni preview
        } ?: run {
            // Ako nema inputVideoUri, čisti se preview player
            previewExoPlayer.stop()
            previewExoPlayer.clearMediaItems()
            previewExoPlayer.setVideoEffects(emptyList())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            previewExoPlayer.release()
        }
    }


    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    inputVideoUri = it
                    outputVideoUri = null
                    coroutineScope.launch { snackbarHostState.showSnackbar("Odabran video: ${it.lastPathSegment?.take(50)}", duration = SnackbarDuration.Short) }
                } catch (e: SecurityException) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Greška s dozvolom za odabir.") }
                }
            }
        }
    )

    val transformerListener = remember {
        object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                isTransforming = false
                transformationProgress = 1f
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Transformacija uspješna! ${outputVideoUri?.lastPathSegment?.take(30)}",
                        duration = SnackbarDuration.Short
                    )
                }
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                isTransforming = false
                Log.e("VideoEffectsScreen", "Greška pri transformaciji. Result: $exportResult", exportException)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Greška: ${exportException.errorCodeName} (${exportException.errorCode})",
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    fun startTransformation() {
        val currentInputUri = inputVideoUri ?: run {
            coroutineScope.launch { snackbarHostState.showSnackbar("Molimo odaberite ulazni video.") }
            return
        }

        isTransforming = true
        transformationProgress = 0f

        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "TransformedVideos")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "transformed_video_$timestamp.mp4")
        val determinedOutputUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            outputFile
        )
        outputVideoUri = determinedOutputUri
        Log.d("VideoEffectsScreen", "Izlazna datoteka: ${outputFile.absolutePath}, URI: $determinedOutputUri")

        coroutineScope.launch {
            var localTransformer: Transformer?
            var progressJob: Job? = null

            try {
                val inputMediaItem = MediaItem.fromUri(currentInputUri)
                val effectsList = mutableListOf<Effect>()

                if (rotationDegrees != 0f) {
                    effectsList.add(
                        ScaleAndRotateTransformation.Builder()
                            .setRotationDegrees(rotationDegrees)
                            .build()
                    )
                }
                if (applyContrast) {
                    effectsList.add(Contrast(contrastValue))
                }

                if (applyBrightness) {
                    effectsList.add(Brightness(brightnessValue))
                }

                if (applyMirror) {
                    effectsList.add(ScaleAndRotateTransformation.Builder().setScale(-1f, 1f).build())
                }

                val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
                    .setEffects(Effects(listOf(), effectsList))
                    .build()

                withContext(Dispatchers.Main) {
                    localTransformer = Transformer.Builder(context)
                        .setLooper(context.mainLooper)
                        .addListener(transformerListener)
                        .setVideoMimeType(MimeTypes.VIDEO_H264)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .build()

                    progressJob = launch {
                        val progressHolder = androidx.media3.transformer.ProgressHolder()
                        while (isActive && isTransforming) { // isActive -> ako je corutina aktivna
                            val progressState = localTransformer?.getProgress(progressHolder) ?: Transformer.PROGRESS_STATE_UNAVAILABLE
                            if (progressState != Transformer.PROGRESS_STATE_UNAVAILABLE) {
                                transformationProgress = progressHolder.progress / 100f
                            }
                            delay(200)
                        }
                    }
                    Log.d("VideoEffectsScreen", "Pokretanje transformacije za: $currentInputUri na ${outputFile.absolutePath}")
                    localTransformer?.start(editedMediaItem, outputFile.absolutePath)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isTransforming = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Greška pri pripremi: ${e.localizedMessage?.take(100)}",
                            duration = SnackbarDuration.Long,
                            withDismissAction = true
                        )
                    }
                    progressJob?.cancel()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Efekti") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Odaberi video", modifier = Modifier.padding(end = 8.dp))
                Text("Odaberi Video")
            }

            if (inputVideoUri != null) {
                Button(
                    onClick = {
                        inputVideoUri = null
                        outputVideoUri = null
                        transformationProgress = 0f
                        isTransforming = false
                        applyBrightness = false
                        brightnessValue = 0f
                        rotationDegrees = 0f
                        applyContrast = false
                        contrastValue = 0f
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Ukloni Odabrani Video")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (inputVideoUri != null) {
                Text("Efekti:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Checkbox(checked = applyMirror, onCheckedChange = { applyMirror = it })
                    Text("Zrcali video horizontalno")
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Checkbox(checked = applyBrightness, onCheckedChange = { applyBrightness = it })
                    Text("Promijeni Svjetlinu")
                }
                if (applyBrightness) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Brightness6, contentDescription = "Svjetlina", modifier = Modifier.padding(end = 8.dp))
                        Slider(
                            value = brightnessValue,
                            onValueChange = { brightnessValue = it },
                            valueRange = -1f..1f,
                            steps = 20,
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format(Locale.US, "%.1f", brightnessValue), modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Rotacija: ${rotationDegrees.toInt()}°")
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { rotationDegrees = (rotationDegrees + 90) % 360 }) {
                        Icon(Icons.Filled.Rotate90DegreesCcw, "Rotiraj")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Checkbox(checked = applyContrast, onCheckedChange = { applyContrast = it })
                    Text("Primijeni Kontrast")
                }
                if (applyContrast) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Contrast, contentDescription = "Kontrast", modifier = Modifier.padding(end = 8.dp))
                        Slider(
                            value = contrastValue,
                            onValueChange = { contrastValue = it },
                            valueRange = -1f..1f, // Ispravan raspon za Contrast efekt
                            steps = 20,
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format(Locale.US, "%.1f", contrastValue), modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { startTransformation() },
                    enabled = !isTransforming,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isTransforming) "Transformiram..." else "Primijeni Efekte i Spremi")
                }
            }

            if (isTransforming) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { transformationProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Napredak: ${(transformationProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Veći razmak
            ) {
                // Pregled s efektima (Live Preview)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pregled s Efektima", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (inputVideoUri != null) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = previewExoPlayer
                                        useController = true
                                        controllerShowTimeoutMs = 2000
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Odaberite video za pregled", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                }

                // Transformirani video
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Spremljen Video", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (outputVideoUri != null && !isTransforming) {
                            VideoPreview(uri = outputVideoUri!!, modifier = Modifier.fillMaxSize())
                        } else {
                            Text(if (isTransforming) "Transformiram..." else "Nema spremljenog videa", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                    if (outputVideoUri != null && !isTransforming) {
                        Button(
                            onClick = {
                                val shareIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, outputVideoUri)
                                    type = context.contentResolver.getType(outputVideoUri!!) ?: "video/mp4"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Podijeli transformirani video"))
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Podijeli")
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPreview(uri: Uri, modifier: Modifier = Modifier, autoPlay: Boolean = false) {
    val context = LocalContext.current

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = autoPlay
            prepare()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                controllerShowTimeoutMs = 2000
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    navController: NavHostController
) {

    val context = LocalContext.current

    val photoViewModel: PhotoViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PhotoViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val imageUris = photoViewModel.imageUris // lista URI-ja slika koje su prikazane u galeriji
    val selectedImagesForDeletion = photoViewModel.selectedImagesForDeletion // lista slika koje je korisnik označio za brisanje

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10), // maksimalno 10 slika za odabiranje iz galerije
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val existingUriStrings = imageUris.map { it.toString() }.toSet()
                val newUris = uris.filter { it.toString() !in existingUriStrings }

                val contentResolver = context.contentResolver
                newUris.forEach { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        Log.e("PhotoPicker", "Dozvola odbijena za: $uri", e)
                    }
                }

                photoViewModel.addUris(newUris)

                when {
                    newUris.isEmpty() -> {
                        Toast.makeText(context, "Sve odabrane slike su već u galeriji.", Toast.LENGTH_SHORT).show()
                    }
                    newUris.size < uris.size -> {
                        Toast.makeText(context, "${newUris.size} slika dodano. Ostale su već u galeriji.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (photoViewModel.isInDeleteMode) "Odaberi za Brisanje (${selectedImagesForDeletion.size})" else "Galerija Slika") },
                navigationIcon = {
                    if (photoViewModel.isInDeleteMode) {
                        IconButton(onClick = { photoViewModel.exitDeleteModeAndClearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Otkaži brisanje")
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                        }
                    }
                },
                actions = {
                    if (photoViewModel.isInDeleteMode && selectedImagesForDeletion.isNotEmpty()) {
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Obriši Odabrano")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!photoViewModel.isInDeleteMode) {
                FloatingActionButton(onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Dodaj Slike")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (imageUris.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nema slika u galeriji",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(imageUris, key = { _, uri -> uri.toString() }) { _, uri ->
                        GalleryImageItem(
                            uri = uri,
                            isInDeleteMode = photoViewModel.isInDeleteMode,
                            isSelectedForDeletion = selectedImagesForDeletion.contains(uri),
                            onImageClick = {
                                if (photoViewModel.isInDeleteMode) {
                                    photoViewModel.toggleImageSelectionForDeletion(uri)
                                } else {
                                    val encodedUri = Uri.encode(uri.toString())
                                    navController.navigate("image_detail_screen/$encodedUri")
                                }
                            },
                            onImageLongClick = {
                                if (!photoViewModel.isInDeleteMode) {
                                    photoViewModel.enterDeleteMode(uri)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text("Potvrdi Brisanje") },
                text = { Text("Jeste li sigurni da želite obrisati odabrane slike (${selectedImagesForDeletion.size}) iz galerije aplikacije?") },
                confirmButton = {
                    Button(
                        onClick = {
                            photoViewModel.removeSelectedUris()
                            showDeleteConfirmationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Obriši")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmationDialog = false }) {
                        Text("Odustani")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryImageItem(
    uri: Uri,
    isInDeleteMode: Boolean,
    isSelectedForDeletion: Boolean,
    onImageClick: () -> Unit,
    onImageLongClick: () -> Unit
) {
    val context = LocalContext.current
    val itemModifier = Modifier
        .aspectRatio(1f) // Kvadratni prikaz za svaku sliku
        .clip(RoundedCornerShape(8.dp))
        .combinedClickable(
            onClick = onImageClick,
            onLongClick = onImageLongClick
        )

    Box(
        modifier = itemModifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Slika iz galerije",
            contentScale = ContentScale.Crop, // Crop da popuni kvadrat
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        if (isInDeleteMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelectedForDeletion) Color.Black.copy(alpha = 0.5f)
                        else Color.Transparent
                    )
            ) {
                if (isSelectedForDeletion) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Odabrano za brisanje",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(navController: NavHostController, imageUri: Uri) {
    val context = LocalContext.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalji Slike") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                },
                actions = { // Gumb za resetiranje zoom-a i pozicije
                    IconButton(onClick = {
                        scale = 1f
                        offset = Offset.Zero
                        rotation = 0f
                    }) {
                        Icon(painterResource(id = android.R.drawable.ic_menu_zoom), contentDescription = "Resetiraj Zoom") // Primjer ikone
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints( // Koristi se BoxWithConstraints za dobivanje maksimalne dimenzije
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val state = rememberTransformableState { zoomChange, panChange, rotationChange ->
                scale = (scale * zoomChange).coerceIn(1f, 5f) // Zoom od 1x do 5x

                rotation += rotationChange

                // Logika za ograničavanje pomaka (panning) da slika ne izađe izvan ekrana
                val extraWidth = (scale - 1) * constraints.maxWidth
                val extraHeight = (scale - 1) * constraints.maxHeight

                val newOffsetXPx = offset.x + panChange.x
                val newOffsetYPx = offset.y + panChange.y

                offset = Offset(
                    x = newOffsetXPx.coerceIn(-extraWidth / 2f, extraWidth / 2f),
                    y = newOffsetYPx.coerceIn(-extraHeight / 2f, extraHeight / 2f)
                )
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Prikaz slike preko cijelog zaslona s mogućnošću zumiranja",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { // Primijenjivanje transformacija
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                        rotationZ = rotation
                    }
                    .transformable(state = state), // Omogućavanje transformacija (pinch, pan, rotate)
                contentScale = ContentScale.Fit
            )
        }
    }
}