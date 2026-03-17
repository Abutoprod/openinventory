package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.openinventory.app.ui.scanner.ScannerScreen
import com.openinventory.app.ui.scanner.ScannerViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = ScannerViewModel()

            ScannerScreen(viewModel)
        }
        /*setContent {
            OpeninventoryTheme {
                MainScreen()
            }
        }*/
    }

}