package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                onPickImage = { onImagePicked ->
                    val context = LocalContext.current
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let {
                            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                            if (bytes != null) onImagePicked(bytes)
                        }
                    }

                    // Usamos return etiquetado para evitar el error de Type Mismatch
                    return@App {
                        launcher.launch("image/*")
                    }
                }
            )
        }
    }
}