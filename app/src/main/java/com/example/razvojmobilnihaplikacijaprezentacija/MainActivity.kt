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
