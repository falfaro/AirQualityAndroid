package com.felipealfaro.airquality

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*
 * JSON data for Polen read samples looks like this:
 *
 * {
 *   "data": [
 *     {
 *       "captador": "ALCA",
 *       "fecha_lectura": "2022-12-31 00:00:00.0",
 *       "tipo_polinico": "Abedul",
 *       "granos_de_polen_x_metro_cubico": "0"
 *     },
 *     ...
 *   ]
 * }
 */

// Maps the root of the JSON data structure for Polen read samples
private data class PolenData(
    val data: List<PolenSample>
)

// Maps a Polen read sample element from the list of samples
data class PolenSample(
    // Sample station name
    val captador: String,
    // Date in the following format: YYYY-MM-DD 00:00:0.0
    val fecha_lectura: String,
    // Polen type (e.g. `Abedul`)
    val tipo_polinico: String,
    // Measurement in cubic meters
    val granos_de_polen_x_metro_cubico: String
)

// Allows constructing a PolenViewModel, while passing a Context reference
// which is used to read data from JSON assets
class PolenViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PolenViewModel::class.java)) {
            Log.i("Polen", "PolenViewModelFactory() called")
            @Suppress("UNCHECKED_CAST")
            return PolenViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Implements a very simple ViewModel for Polen data, that just encapsulates
// the list of Polen samples in a List<>
class PolenViewModel(private val context : Context) : ViewModel() {

    private var _polenData : List<PolenSample> = listOf()
    val data : List<PolenSample>
        get() {
            if (_polenData.isEmpty()) {
                Log.i("PolenViewModel", "Loading polen data from JSON asset")
                _polenData = getPolenSamplesFromJsonAsset(context, "mediciones_polen.json").data
            }
            return _polenData
        }

    private fun parseJson(jsonString: String) : PolenData {
        val gson = Gson()
        val type = object: TypeToken<PolenData>(){}.type
        var data : PolenData = gson.fromJson(jsonString, type)
        return data
    }

    private fun getPolenSamplesFromJsonAsset(context: Context, fileName: String): PolenData {
        val jsonString: String
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return parseJson(jsonString)
    }
}
