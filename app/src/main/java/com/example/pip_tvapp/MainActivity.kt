package com.example.pip_tvapp

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.pip_tvapp.ui.theme.PiP_TvAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiP_TvAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    VideoPlayer(
                        context = applicationContext,
                        uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(pipParams)
        } else {
            super.onBackPressed()
        }
    }
}

@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("OpaqueUnitKey")
@Composable
fun VideoPlayer(
    context: Context,
    uri: String,
    modifier: Modifier = Modifier,
) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    val playButtonFocusRequester = remember { FocusRequester() }
    val pipButtonFocusRequester = remember { FocusRequester() }

    // Use LaunchedEffect to request focus when the composable is first displayed
    LaunchedEffect(Unit) {
        playButtonFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    controllerAutoShow = true
                    controllerShowTimeoutMs = 3000
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusable()
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.TopEnd)
                .focusable()
        ) {
            Button(
                onClick = {
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                    Log.d("VideoPlayer", "Play button clicked, isPlaying: ${exoPlayer.isPlaying}")
                    // Request focus on Play/Pause button
                    playButtonFocusRequester.requestFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(playButtonFocusRequester)
                    .focusProperties {
                        next = pipButtonFocusRequester
                    }
                    .focusable()
            ) {
                Text(text = if (exoPlayer.isPlaying) "Pause" else "Play")
            }

            val currentContext = LocalContext.current
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        currentContext.findActivity().enterPictureInPictureMode(
                            PictureInPictureParams.Builder().build()
                        )
                        Log.d("VideoPlayer", "Enter PiP button clicked")
                    } else {
                        Log.i("PIP_TAG", "API does not support PiP")
                    }
                    // Request focus on PiP button
                    pipButtonFocusRequester.requestFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(pipButtonFocusRequester)
                    .focusProperties {
                        previous = playButtonFocusRequester
                    }
                    .focusable()
            ) {
                Text(text = "Enter PiP mode!")
            }
        }
    }

    // Observe playback state changes and manage focus accordingly
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // This callback can be used to handle focus changes based on playback state
                if (isPlaying) {
                    playButtonFocusRequester.requestFocus()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }
}

fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}
