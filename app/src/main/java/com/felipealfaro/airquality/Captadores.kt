package com.felipealfaro.airquality

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class CaptadorData(
    val data: List<Captador>
)

data class Captador(
    val descripcion: String,
    val codigo : String,
    val url_infoweb: String,
    val direccion_localidad: String,
    val zbs_geocodigo: Int,
    val y_epsg25830: Float,
    val direccion_ubicacion: String,
    val url_calendario_polinico: String,
    val nombre: String,
    val x_epsg25830: Float,
    val direccion_codigo_postal: Int,
    val zbs_nombre: String,
    val red_esp_aerobiologia: String,
    val numero_tipos_de_polen_registrados: Int,
    val departamento_responsable: String,
    val long_epsg4258: Float,
    val lat_epsg4258: Float,
    val altitud: Float,
    val fecha_inicio_mediciones: String,
    val tipos_de_polen_registrados: String,
    val altura_del_captador: Float,
    val contacto_email: String,
    val tipo_titularidad: String,
    val edificio: String
)

interface Captadores {
    companion object {
        fun getFromJsonAsset(context: Context, fileName: String): CaptadorData? {
            val jsonString: String
            try {
                jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                return null
            }
            return parseJson(jsonString)
        }

        private fun parseJson(jsonString: String) : CaptadorData {
            val gson = Gson()
            val type = object: TypeToken<CaptadorData>(){}.type
            var data : CaptadorData = gson.fromJson(jsonString, type)
            return data
        }
    }
}
