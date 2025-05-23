package com.example.razvojmobilnihaplikacijaprezentacija

import android.annotation.SuppressLint
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // Standardni import
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import android.os.Environment
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

// Modificirajte AppNavigation da prihvati startDestinationFromIntent
@Composable
fun AppNavigation(navController: NavHostController, startDestinationFromIntent: String?) {
    // Određivanje stvarne početne destinacije
    val actualStartDestination = if (startDestinationFromIntent == "background_audio_screen") {
        "background_audio_screen"
    } else {
        "main_screen" // Vaša defaultna početna destinacija
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
    }

    // Ako je MainActivity pokrenuta s namjerom da ide na specifični ekran, navigiraj tamo
    // Ovo je korisno ako je aplikacija već bila pokrenuta, ali ne na željenom ekranu
    // OVO SE MOŽE PREMJESTITI U onNewIntent AKO JE POTREBNO
    LaunchedEffect(startDestinationFromIntent) {
        if (startDestinationFromIntent == "background_audio_screen" && navController.currentDestination?.route != "background_audio_screen") {
            Log.d("AppNavigation", "Navigating to background_audio_screen due to intent.")
            navController.navigate("background_audio_screen") {
                // Opcionalno, očisti backstack do main_screen ako je to željeno ponašanje
                popUpTo("main_screen") { inclusive = false } // Ostavi main_screen u backstacku
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

    // Stanje za unos URL-a
    var inputUrl by remember { mutableStateOf("") }
    // Stanje za URI odabrane lokalne datoteke
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    // MediaItem koji se trenutno koristi za reprodukciju
    var currentMediaItemForPlayer by remember { mutableStateOf<MediaItem?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
        // Ne pripremamo player odmah, već kada se postavi MediaItem
    }

    var isPlaying by remember { mutableStateOf(false) } // Inicijalno nije pokrenuto
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    // ActivityResultLauncher za odabir audio datoteke
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(), // Koristimo OpenDocument
        onResult = { uri: Uri? ->
            uri?.let {
                // Važno: Zatražiti trajne dozvole za pristup URI-ju
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    selectedFileUri = it
                    inputUrl = "" // Očisti URL polje ako je datoteka odabrana
                    currentMediaItemForPlayer = MediaItem.fromUri(it)
                } catch (e: SecurityException) {
                    Log.e("AudioPlayer", "Nije moguće dobiti trajnu dozvolu za URI: $uri", e)
                    Toast.makeText(context, "Greška pri odabiru datoteke: dozvola odbijena.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    // Reagiranje na promjenu MediaItem-a koji treba reproducirati
    LaunchedEffect(currentMediaItemForPlayer) {
        currentMediaItemForPlayer?.let { item ->
            exoPlayer.stop() // Zaustavi prethodnu reprodukciju ako je bilo
            exoPlayer.clearMediaItems() // Ukloni stare iteme
            exoPlayer.setMediaItem(item)
            exoPlayer.prepare()
            isPlaying = false // Player je pripremljen, ali ne svira dok korisnik ne klikne play
            currentPosition = 0L
            duration = 0L // Ažurirat će se kada je player spreman
            Log.d("AudioPlayer", "Novi MediaItem postavljen: ${item.mediaId}")
        } ?: run { // Ako je currentMediaItemForPlayer null (npr. nakon "Očisti")
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            isPlaying = false
            currentPosition = 0L
            duration = 0L
            Log.d("AudioPlayer", "MediaItem očišćen.")
        }
    }

    // ExoPlayer listener i životni ciklus
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    Log.d("AudioPlayer", "Player spreman, trajanje: $duration")
                } else if (playbackState == Player.STATE_ENDED) {
                    currentPosition = exoPlayer.duration.coerceAtLeast(0L)
                    isPlaying = false
                    exoPlayer.seekTo(0) // Vrati na početak kad završi
                    exoPlayer.pause()   // Osiguraj da je pauziran
                    Log.d("AudioPlayer", "Reprodukcija završena.")
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    if (player.playbackState == Player.STATE_READY) {
                        duration = player.duration.coerceAtLeast(0L)
                    }
                }
                // Ažuriraj poziciju samo ako player svira ili ako se promijenila pozicija (npr. seek)
                // ili ako se promijenilo stanje (npr. iz IDLE u READY).
                if (isPlaying || events.contains(Player.EVENT_POSITION_DISCONTINUITY) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    currentPosition = player.currentPosition
                }
            }
        }
        exoPlayer.addListener(listener)

        val job = coroutineScope.launch {
            while (true) {
                if (exoPlayer.playbackState == Player.STATE_READY && exoPlayer.isPlaying) {
                    currentPosition = exoPlayer.currentPosition
                }
                delay(200) // Češće ažuriranje za glađi slider
            }
        }

        onDispose {
            exoPlayer.removeListener(listener)
            job.cancel()
            exoPlayer.release() // Oslobađanje ExoPlayera kada composable nestane
            Log.d("AudioPlayer", "ExoPlayer oslobođen.")
        }
    }

    // Upravljanje reprodukcijom ovisno o životnom ciklusu composable-a
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) { // Dodaj exoPlayer kao key ako ovisiš o njemu
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (exoPlayer.isPlaying) { // Pauziraj samo ako trenutno svira
                        exoPlayer.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> { /* Ovdje možete odlučiti želite li nastaviti reprodukciju */ }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Naslov za prikaz (iz URL-a, URI-ja ili metadata)
    val displayTitle = currentMediaItemForPlayer?.mediaMetadata?.title?.toString()
        ?: selectedFileUri?.lastPathSegment // Naziv datoteke iz URI-ja
        ?: if (inputUrl.isNotBlank() && currentMediaItemForPlayer?.mediaId == inputUrl) inputUrl.substringAfterLast('/') else null // Naziv iz URL-a
            ?: "Nema učitanog medija"


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Player") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag")
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
            // verticalArrangement = Arrangement.Center // Uklonjeno da UI bude na vrhu
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
                            selectedFileUri = null // Očisti odabir datoteke
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
                    audioPickerLauncher.launch(arrayOf("audio/mpeg", "audio/mp4", "video/mp4")) // Pokreni odabir za MP3 datoteke
                    // Možete koristiti i "audio/*" za sve audio tipove
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


            Spacer(modifier = Modifier.weight(1f)) // Gura kontrole playera prema dolje ako nema puno sadržaja iznad

            // Player kontrole (vidljive samo ako je nešto učitano)
            // if (currentMediaItemForPlayer != null && (exoPlayer.playbackState == Player.STATE_READY || exoPlayer.playbackState == Player.STATE_BUFFERING)) {
            // Bolje je uvijek prikazati kontrole, ali ih onemogućiti ako player nije spreman
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
                        if (currentMediaItemForPlayer != null) { // Omogući klik samo ako je nešto učitano
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_ENDED) {
                                    // Ako player nije pripremljen (npr. nakon greške ili prvog učitavanja bez auto-play)
                                    // ili je završio, pripremi ga ponovno ili seekaj na početak i pokreni
                                    exoPlayer.prepare() // Osiguraj da je pripremljen
                                    exoPlayer.seekTo(0) // Kreni od početka ako je završio
                                }
                                exoPlayer.play()
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = currentMediaItemForPlayer != null // Gumb je aktivan samo ako je nešto učitano
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pauza" else "Play",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Dodatni razmak na dnu
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
        // Ne pripremamo player odmah, već kada se postavi MediaItem
    }

    var isPlaying by remember { mutableStateOf(false) } // Pratimo je li player trebao svirati

    // ActivityResultLauncher za odabir video datoteke
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    selectedFileUri = it
                    inputUrl = "" // Očisti URL polje
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
            Log.d("VideoPlayer", "Novi MediaItem postavljen: ${item.mediaId}")
        } ?: run {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            Log.d("VideoPlayer", "MediaItem očišćen.")
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
            // Možete dodati i druge evente ako je potrebno, npr. onPlaybackStateChanged
        }
        exoPlayer.addListener(listener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                }
                Lifecycle.Event.ON_STOP -> { // ON_STOP je bolji od ON_PAUSE za potpuno zaustavljanje resursa
                    // Čuvamo stanje playWhenReady prije pauziranja
                    // isPlaying = exoPlayer.playWhenReady
                    // exoPlayer.playWhenReady = false // Pauziraj i oslobodi resurse
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
            // exoPlayer.release() // Oslobađanje će se dogoditi u donjem DisposableEffect(Unit)
        }
    }

    // Oslobađanje playera kada composable napusti stablo
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            Log.d("VideoPlayer", "ExoPlayer oslobođen.")
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag")
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
                            Log.e("VideoPlayer", "Neispravan URL: $inputUrl", e)
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
                    // Pokreni odabir za video datoteke (npr. MP4, MKV, WebM itd.)
                    videoPickerLauncher.launch(arrayOf("video/*"))
                    // Možete biti specifičniji: arrayOf("video/mp4", "video/x-matroska")
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

            // PlayerView za prikaz videa
            if (currentMediaItemForPlayer != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            // Možete prilagoditi PlayerView, npr.:
                            // useController = true (default)
                            // controllerShowTimeoutMs = 3000
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f) // Prilagodite omjer slike prema potrebi
                )
            } else {
                // Prikaz nečega dok video nije učitan
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

    var inputUrl by remember { mutableStateOf("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3") } // Default URL za test
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var controllerFutureHolder by remember { mutableStateOf<ListenableFuture<MediaController>?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentMediaIdInController by remember { mutableStateOf<String?>(null) }

    // Povezivanje s MediaControllerom
    DisposableEffect(context) {
        Log.d("BackgroundAudioScreen", "Pokretanje DisposableEffect za MediaController")
        val sessionToken = SessionToken(context, ComponentName(context, MediaPlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFutureHolder = future

        future.addListener({
            try {
                val controller = future.get()
                Log.d("BackgroundAudioScreen", "MediaController povezan: $controller")
                mediaController = controller

                val playerListener = object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                        Log.d("BackgroundAudioScreen", "UI Listener: isPlaying promijenjeno na $isPlayingValue")
                        isPlaying = isPlayingValue
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d("BackgroundAudioScreen", "UI Listener: playbackState promijenjen na $playbackState")
                        if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                            duration = controller.duration.coerceAtLeast(0L)
                            currentPosition = controller.currentPosition.coerceAtLeast(0L)
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            isPlaying = false
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        Log.d("BackgroundAudioScreen", "UI Listener: mediaItem prebačen na ${mediaItem?.mediaId}")
                        currentMediaIdInController = mediaItem?.mediaId
                        duration = controller.duration.coerceAtLeast(0L)
                        currentPosition = controller.currentPosition.coerceAtLeast(0L)
                        isPlaying = controller.isPlaying
                    }
                }
                controller.addListener(playerListener)

                // Inicijalno dohvaćanje stanja
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
            Log.d("BackgroundAudioScreen", "Otpustanje MediaController-a")
            controllerFutureHolder?.let { MediaController.releaseFuture(it) } // Ispravno otpuštanje
            // mediaController?.release() // Ne treba dvaput, releaseFuture() to rješava ako je future uspješan
            mediaController = null
            controllerFutureHolder = null
        }
    }

    // Korutina za periodično ažuriranje pozicije (slidera)
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
                    // mediaController?.play() // Opcionalno: automatski pokreni
                    Log.d("BackgroundAudioScreen", "Lokalna datoteka odabrana: $it, poslana kontroleru.")
                } catch (e: SecurityException) {
                    Log.e("BackgroundAudioScreen", "Greška s dozvolom za URI: $uri", e)
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag")
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
                            Log.d("BackgroundAudioScreen", "URL poslan: $inputUrl")
                        } catch (e: Exception) {
                            Log.e("BackgroundAudioScreen", "Neispravan URL: $inputUrl", e)
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
                        audioPickerLauncher.launch(arrayOf("audio/*", "video/mp4")) // Dopušta sve audio i mp4
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
                        mediaController?.stop() // Zaustavlja reprodukciju
                        mediaController?.clearMediaItems() // Uklanja sve stavke
                        // Resetiraj UI stanja koja ne dolaze direktno od kontrolera
                        inputUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" // Vrati na default ili ostavi prazno
                        selectedFileUri = null
                        // Stanja isPlaying, duration, currentPosition će se ažurirati kroz listener na kontroleru
                        Log.d("BackgroundAudioScreen", "Očisti odabir poslan kontroleru.")
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
                                // Ako je STATE_ENDED, play() će ga ponovno pokrenuti od početka.
                                // Ako je STATE_IDLE ali ima item, play() će ga pripremiti i pokrenuti.
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

@androidx.annotation.OptIn(UnstableApi::class) // Zbog korištenja Transformer API-ja
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEffectsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputVideoUri by remember { mutableStateOf<Uri?>(null) }
    var outputVideoUri by remember { mutableStateOf<Uri?>(null) } // Za spremljeni video
    var transformationProgress by remember { mutableFloatStateOf(0f) }
    var isTransforming by remember { mutableStateOf(false) }
    var transformationMessage by remember { mutableStateOf("") }

    var applyGrayscale by remember { mutableStateOf(false) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) } // Za rotaciju

    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    // Zatražiti trajne dozvole ako je moguće (neke implementacije to ne podržavaju za OpenDocument)
                    // context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    inputVideoUri = it
                    outputVideoUri = null // Resetiraj output kad se odabere novi input
                    transformationMessage = "Odabran video: ${it.lastPathSegment}"
                    Log.d("VideoEffectsScreen", "Odabran video: $it")
                } catch (e: SecurityException) {
                    Log.e("VideoEffectsScreen", "Nije moguće dobiti trajnu dozvolu za URI: $uri", e)
                    transformationMessage = "Greška s dozvolom za odabir."
                }
            }
        }
    )

    // Transformer instanca - kreira se po potrebi
    val transformerListener = remember {
        object : Transformer.Listener {
            override fun onCompleted(
                composition: Composition,
                exportResult: ExportResult
            ) {
                isTransforming = false
                transformationProgress = 1f
                transformationMessage = "Transformacija uspješna! Spremljeno u: ${outputVideoUri?.path}"
                Log.d("VideoEffectsScreen", "Transformacija uspješna. Output: $outputVideoUri, Result: $exportResult")
                // Ovdje možete automatski pokrenuti pregled videa ili ponuditi opciju
            }

            // ISPRAVLJENA METODA
            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                isTransforming = false
                transformationMessage = "Greška pri transformaciji: ${exportException.message}"
                Log.e("VideoEffectsScreen", "Greška pri transformaciji. Result: $exportResult", exportException)
            }

            // onTransformationProgress je deprecated, koristite query کردن progressa
        }
    }

    // Funkcija za pokretanje transformacije
    fun startTransformation() {
        val currentInputUri = inputVideoUri ?: run {
            transformationMessage = "Molimo odaberite ulazni video."
            return
        }

        isTransforming = true // UI update
        transformationProgress = 0f // UI update
        transformationMessage = "Transformacija u tijeku..." // UI update

        // Kreiranje izlazne datoteke (može ostati ovdje, nije previše zahtjevno)
        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "TransformedVideos")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "transformed_video_$timestamp.mp4")
        // Dohvati URI prije ulaska u korutinu da ne bi bilo problema s contextom
        val determinedOutputUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            outputFile
        )
        outputVideoUri = determinedOutputUri // UI update (za prikaz putanje)
        Log.d("VideoEffectsScreen", "Izlazna datoteka: ${outputFile.absolutePath}, URI: $determinedOutputUri")

        coroutineScope.launch { // Default dispatcher, može biti Main ili Default ovisno o scope-u
            var localTransformer: Transformer? = null // Drži referencu za eventualni cancel
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

                val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
                    .setEffects(Effects(listOf(), effectsList))
                    .build()

                // Kreiranje Transformera mora biti na dretvi s Looperom.
                // Prebacujemo se na Main za kreiranje i start, progress.
                withContext(Dispatchers.Main) {
                    localTransformer = Transformer.Builder(context)
                        .setLooper(context.mainLooper) // Eksplicitno Main Looper
                        .addListener(transformerListener)
                        .setVideoMimeType(MimeTypes.VIDEO_H264)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .build()

                    progressJob = launch { // Ova korutina će se također izvršavati na Dispatchers.Main
                        val progressHolder = androidx.media3.transformer.ProgressHolder()
                        while (isActive && isTransforming) { // isActive provjerava je li korutina aktivna
                            val progressState = localTransformer?.getProgress(progressHolder) ?: Transformer.PROGRESS_STATE_UNAVAILABLE
                            if (progressState != Transformer.PROGRESS_STATE_UNAVAILABLE) {
                                transformationProgress = progressHolder.progress / 100f
                            }
                            delay(200)
                        }
                    }
                    Log.d("VideoEffectsScreen", "Pokretanje transformacije za: $currentInputUri na $determinedOutputUri")
                    localTransformer?.start(editedMediaItem, outputFile.absolutePath)
                }
                // Listeneri (onCompleted, onError) će biti pozvani na context.mainLooper

            } catch (e: Exception) {
                // Ova catch grana hvata iznimke prije ili tijekom poziva withContext(Dispatchers.Main)
                // ili ako sam start baci iznimku na neočekivan način.
                // Listener onError bi trebao uhvatiti greške iz same transformacije.
                withContext(Dispatchers.Main) {
                    isTransforming = false
                    transformationMessage = "Greška pri transformaciji (vanjska): ${e.localizedMessage}"
                    Log.e("VideoEffectsScreen", "Greška pri transformaciji (vanjska)", e)
                    progressJob?.cancel() // Otkaži praćenje napretka
                }
            }
            // Ne treba finally blok za otkazivanje progressJob-a jer isTransforming i isActive to rješavaju
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Efekti") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Natrag")
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
            Button(
                onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Odaberi video", modifier = Modifier.padding(end = 8.dp))
                Text("Odaberi Video")
            }

            inputVideoUri?.let { uri ->
                Text("Odabrano: ${uri.lastPathSegment}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Opcije za efekte
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = applyGrayscale, onCheckedChange = { applyGrayscale = it })
                    Text("Crno-bijeli filter")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rotacija: ${rotationDegrees.toInt()}°")
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { rotationDegrees = (rotationDegrees + 90) % 360 }) {
                        Icon(Icons.Filled.Rotate90DegreesCcw, "Rotiraj")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { startTransformation() },
                    enabled = !isTransforming,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isTransforming) "Transformiram..." else "Primijeni Efekte")
                }
            }

            if (isTransforming) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { transformationProgress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (transformationMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(transformationMessage, style = MaterialTheme.typography.bodyMedium)
            }

            // Prikaz originalnog i transformiranog videa (opcionalno)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Original")
                    inputVideoUri?.let { VideoPreview(uri = it, modifier = Modifier.height(150.dp)) }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Transformiran")
                    outputVideoUri?.let { uri ->
                        VideoPreview(uri = uri, modifier = Modifier.height(150.dp))
                        // Gumb za dijeljenje
                        Button(onClick = {
                            val shareIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = context.contentResolver.getType(uri) ?: "video/mp4"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Podijeli transformirani video"))
                        }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Podijeli")
                        }
                    }
                }
            }
        }
    }
}

// Pomoćna Composable funkcija za prikaz videa
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPreview(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // Omogući kontrole
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
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
