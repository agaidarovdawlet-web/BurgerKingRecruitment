package com.example.bkrecruitment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bkrecruitment.ui.BkHiringApp
import com.example.bkrecruitment.ui.theme.BkHiringTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BkHiringTheme {
                BkHiringApp()
            }
        }
    }
}
