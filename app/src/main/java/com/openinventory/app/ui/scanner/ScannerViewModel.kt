package com.openinventory.app.ui.scanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.scanner.ScannerManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import com.openinventory.app.core.scanner.ScannedProduct
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.*
import android.media.AudioManager
import android.media.ToneGenerator
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.data.database.entity.ProductEntity

class ScannerViewModel(
    private val scannerManager: ScannerManager,
    private val repository: ProductRepository
) : ViewModel() {

    private var lastScannedCode: String? = null
    private var lastScanTime: Long = 0

    var produtoEncontrado = mutableStateOf<ProductEntity?>(null)
        private set
    var erroScanner = mutableStateOf("")
        private set
    var scannedProducts = mutableStateListOf<ScannedProduct>()
        private set
    var scanResult = mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            scannerManager.scanFlow.collect { barcode ->
                buscarEProcessar(barcode)
            }
        }
    }

     fun buscarEProcessar(barcode: String) {
        val currentTime = System.currentTimeMillis()
        if (barcode == lastScannedCode && (currentTime - lastScanTime) < 2500) return

        lastScannedCode = barcode
        lastScanTime = currentTime

        viewModelScope.launch(Dispatchers.IO) {
            val produto = repository.getProductBySku(barcode)

            withContext(Dispatchers.Main) {
                val timestampStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                if (produto != null) {
                    tocarSomBipSucesso()
                    erroScanner.value = ""
                    produtoEncontrado.value = produto

                    // Adicionando na lista (Verifique se ScannedProduct usa 'code' ou 'barcode')
                    scannedProducts.add(0, ScannedProduct(
                        code = barcode,
                        name = produto.description, // Se der erro aqui, mude para produto.name
                        timestamp = timestampStr
                    ))
                } else {
                    tocarSomErro()
                    erroScanner.value = "Produto $barcode não cadastrado!"
                    // Mesmo sem cadastro, adicionamos na lista para o usuário ver que bipou
                    scannedProducts.add(0, ScannedProduct(
                        code = barcode,
                        name = "Não Cadastrado",
                        timestamp = timestampStr
                    ))
                }
                scanResult.value = barcode
            }
        }
    }

    fun deleteProduct(product: ScannedProduct) {
        scannedProducts.remove(product)
    }

    fun tocarSomBipSucesso() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    fun tocarSomErro() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300)
    }
}