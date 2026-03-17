package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.openinventory.app.ui.inventory.MainScreen
import com.openinventory.app.ui.theme.OpeninventoryTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OpeninventoryTheme {
                MainScreen()
            }
        }
    }

}