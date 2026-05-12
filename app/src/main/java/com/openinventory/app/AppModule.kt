package com.openinventory.app

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openinventory.app.ui.viewmodel.ProductViewModel
val appModule = module {

    viewModel { ProductViewModel(get()) }

}