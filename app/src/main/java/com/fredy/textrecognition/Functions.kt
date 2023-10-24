package com.fredy.textrecognition

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat(
        "yyyy_MM_dd_HH_mm_ss", Locale.getDefault()
    ).format(
        Date()
    )
    val imageFileName = "JPEG_${timeStamp}_"
    val imageFile = createImageFile(
        context, imageFileName
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

fun createImageFile(
    context: Context,
    imageFileName: String,
    environment: String? = null
): File {
    val storageDir = context.getExternalFilesDir(
        environment
    ) // Use the cache directory to avoid using external storage
    return File.createTempFile(
        imageFileName, ".jpg", storageDir
    )
}

fun detectTextFromImage(
    context: Context,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val firebaseImage = FirebaseVisionImage.fromFilePath(
        context, uri
    )
    val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
    val resultTask = recognizer.processImage(
        firebaseImage
    )

    resultTask.addOnSuccessListener { result ->
        // Text recognition succeeded
        val text = result.text
        onSuccess(text)
    }.addOnFailureListener { e ->
        // Text recognition failed, handle the error
        e.printStackTrace()
        val error = "Error: ${e.message}"
        onFailure(error)
    }
}
