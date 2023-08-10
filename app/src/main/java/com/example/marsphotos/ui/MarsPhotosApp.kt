/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.network.MarsPhoto
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.PhotoScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class MarsPhotosScreen {
    Start,
    Photo
}

@Composable
fun MarsPhotosApp(
    modifier: Modifier = Modifier,
    viewModel: MarsViewModel = viewModel(factory = MarsViewModel.Factory),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = MarsPhotosScreen.valueOf(
        backStackEntry?.destination?.route ?: MarsPhotosScreen.Start.name
    )
    val uiState by viewModel.uiState.collectAsState()
    val intentContext = LocalContext.current

    Scaffold(
        topBar = {
            MarsTopAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                onShareButtonClicked = {
                    viewModel.viewModelScope.launch {
                        sharePhoto(
                            intentContext = intentContext,
                            photo = uiState.currentPhoto!!
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MarsPhotosScreen.Start.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(MarsPhotosScreen.Start.name) {
                HomeScreen(
                    marsUiState = uiState.requestStatus,
                    retryAction = viewModel::getMarsPhotos,
                    onItemClick = {
                        navController.navigate(MarsPhotosScreen.Photo.name)
                        viewModel.updateCurrentPhoto(it)
                    }
                )
            }
            composable(MarsPhotosScreen.Photo.name) {
                PhotoScreen(photo = uiState.currentPhoto!!)
            }
        }
    }
}

@Composable
fun MarsTopAppBar(
    currentScreen: MarsPhotosScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    onShareButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        actions = {
            if (currentScreen == MarsPhotosScreen.Photo) {
                IconButton(onClick = onShareButtonClicked) {
                    Icon(imageVector = Icons.Filled.Share, stringResource(R.string.share))
                }
            }
        }
    )
}

private suspend fun sharePhoto(
    intentContext: Context,
    photo: MarsPhoto
) {
    val imageFile = downloadImageFromUrl(
        context = intentContext,
        url = photo.imgSrc
    )
    val imageUri = FileProvider.getUriForFile(
        intentContext,
        "${intentContext.packageName}.fileprovider",
        imageFile
    )

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/jpg"
        putExtra(
            Intent.EXTRA_STREAM,
            imageUri
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    intentContext.startActivity(Intent.createChooser(shareIntent, "Share mars photo!"))
}

private suspend fun downloadImageFromUrl(
    context: Context,
    url: String
): File {
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()

    val drawable = withContext(Dispatchers.IO) {
        loader.execute(request).drawable
    }
    val bitmap = (drawable as BitmapDrawable).bitmap

    val imageFile = File(context.cacheDir, "photo.jpg")
    val outputStream = withContext(Dispatchers.IO) {
        FileOutputStream(imageFile)
    }
    // Hardcoded number '73' because using '100' it makes image file even bigger than original file(?)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 73, outputStream)

    withContext(Dispatchers.IO) {
        outputStream.flush()
    }

    return imageFile
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MarsPhotosAppPreview() {
    MaterialTheme {
        MarsPhotosApp()
    }
}
