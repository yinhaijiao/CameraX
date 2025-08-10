package com.infinitysoftware.mvvm_demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.infinitysoftware.mvvm_demo.ui.theme.MVVM_DemoTheme
import com.infinitysoftware.mvvm_demo.view.MainScreen
import com.infinitysoftware.mvvm_demo.viewmodel.myViewModel

// Suppresses Warning About Unused Scaffold Padding Parameter.
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Gets ViewModel Scoped To This Activity's Lifecycle.
        val myViewModel = ViewModelProvider(this)[myViewModel::class]

        setContent {
            MVVM_DemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Calls The MainScreen Composable Passing The ViewModel Instance.
                    MainScreen(myViewModel)
                }
            }
        }
    }
}