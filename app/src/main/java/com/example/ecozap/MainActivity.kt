package com.example.ecozap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.ecozap.ui.theme.EcoZapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoZapTheme {
                val navController = rememberNavController()
                NavGraph(navController)

            }
        }
    }
}
