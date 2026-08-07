package com.artmondo.boxters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.artmondo.boxters.ui.screens.GameScreen
import com.artmondo.boxters.ui.theme.BoxtersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoxtersTheme {
                GameScreen(
                    modifier = Modifier.fillMaxSize(),
                    deepLinkUri = intent?.data?.toString()
                )
            }
        }
    }
}
