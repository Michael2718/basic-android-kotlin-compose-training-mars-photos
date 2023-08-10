package com.example.marsphotos.ui.screens

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.network.MarsPhoto

@Composable
fun PhotoScreen(
    photo: MarsPhoto,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ZoomableImage(photo)
    }
}

@Composable
fun ZoomableImage(
    photo: MarsPhoto,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var rotationState by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .clip(RectangleShape) // Clip the box content
            .fillMaxSize() // Give the size you want...
//            .background(Color.Gray)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    offsetX += pan.x
                    offsetY += pan.y
                    scale *= zoom
                    rotationState += rotation
                }
//                detectDragGestures { change, dragAmount ->
//                    change.consume()
//                    offsetX += dragAmount.x
//                    offsetY += dragAmount.y
//                }
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(photo.imgSrc)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.mars_photo),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY,
                    rotationZ = rotationState
                ),
            placeholder = painterResource(R.drawable.loading_img),
            error = painterResource(R.drawable.ic_broken_image),
            contentScale = ContentScale.Fit
        )
    }
}
