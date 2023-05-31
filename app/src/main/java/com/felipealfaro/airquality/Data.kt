package com.felipealfaro.airquality

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class PolenData(
    val data: List<PolenSample>
)

data class PolenSample(
    val captador: String,
    val fecha_lectura: String,
    val tipo_polinico: String,
    val granos_de_polen_x_metro_cubico: String
)

interface Polen {
    companion object {
        fun getPolenSamplesFromJsonAsset(context: Context, fileName: String): PolenData? {
            val jsonString: String
            try {
                jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                return null
            }
            return parseJson(jsonString)
        }

        private fun parseJson(jsonString: String) : PolenData {
            val gson = Gson()
            val type = object: TypeToken<PolenData>(){}.type
            var data : PolenData = gson.fromJson(jsonString, type)
            return data
        }
    }
}
