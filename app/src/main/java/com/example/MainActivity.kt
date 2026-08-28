package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.DetectorScreen
import com.example.ui.theme.BambooBackground
import com.example.ui.theme.NushuDetectorTheme
import com.example.ui.viewmodel.DetectorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DetectorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NushuDetectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BambooBackground
                ) {
                    DetectorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
