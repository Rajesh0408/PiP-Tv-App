package com.example.pip_tvapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import coil.ImageLoader
import coil.compose.AsyncImage
import com.example.pip_tvapp.ui.theme.PiP_TvAppTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiP_TvAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    TvLazyColumn(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(15) { i ->
                            Text("Section$i")
                            TvLazyRow(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(35) { i ->
//                                  Image(painter = , contentDescription = )
//                                    AsyncImage(model = "https://img.freepik.com/free-photo/colorful-design-with-spiral-design_188544-9588.jpg", contentDescription = "Image", imageLoader = ImageLoader(context = applicationContext))

                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("OpaqueUnitKey")
@Composable
fun VideoPlayer(
    context: Context,
    uri: String,
    modifier: Modifier=Modifier
) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = com.google.android.exoplayer2.MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady =true
        }
    }
    
    DisposableEffect(
        AndroidView(factory = {
            PlayerView(it).apply {
                player = exoPlayer
            }
        },
            modifier = Modifier),

    ) {
       onDispose {
           exoPlayer.release()
       }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PiP_TvAppTheme {
        Greeting("Android")
    }
}