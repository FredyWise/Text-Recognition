package com.fredy.textrecognition

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraAPP()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraAPP() {
    val context = LocalContext.current
    val uri = createImageUri(
        context
    )
//    val imageCropper = rememberImageCropper()
//    val scope = rememberCoroutineScope()
    var capturedImageUri by remember {
        mutableStateOf<Uri>(
            Uri.EMPTY
        )
    }
    var detectedText: String by remember {
        mutableStateOf(
            ""
        )
    }
    val permissionsState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) {result ->
        if (result.isSuccessful) {
            capturedImageUri = result.uriContent!!
            detectTextFromImage(context,
                capturedImageUri,
                { text ->
                    detectedText = text
                },
                { error ->
                    Toast.makeText(
                        context,
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                })
        }
        else {
            val exception = result.error
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val cropOption = CropImageContractOptions(it, CropImageOptions())
                imageCropLauncher.launch(cropOption)
            }
        })
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val cropOption = CropImageContractOptions(uri, CropImageOptions())
            imageCropLauncher.launch(cropOption)

        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(
                context,
                "Permission Granted",
                Toast.LENGTH_SHORT
            ).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                "Permission Denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (capturedImageUri != Uri.EMPTY) {
            Image(
                contentDescription = "Captured Image",
                painter = rememberImagePainter(
                    capturedImageUri
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(
                        1f
                    )
            )
        } else {
            Image(
                contentDescription = "Captured Image",
                imageVector = Icons.Default.AddAPhoto,
                modifier = Modifier
                    .size(100.dp)
                    .weight(
                        1f
                    )
            )
        }
        Row {
            Button(
                onClick = {
                    if (permissionsState.status.isGranted) {
                        cameraLauncher.launch(
                            uri
                        )
                    } else {
                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(
                        48.dp
                    )
            ) {
                Text(text = "Capture Image")
            }
            Button(
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(
                        48.dp
                    )
            ) {
                Text(text = "Gallery")
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        TextBox(
            value = detectedText,
            onValueChanged = { detectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(
                    0.25f
                )
        )
    }
}
