package com.felipealfaro.airquality

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.felipealfaro.airquality.databinding.ActivityDayDataBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
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

fun parseJson(jsonString: String) : PolenData {
    val gson = Gson()
    val type = object: TypeToken<PolenData> (){}.type
    var data : PolenData = gson.fromJson(jsonString, type)
    return data
}

fun getPolenSamplesFromJsonAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

class AirQualityDataResponse(val data: List<PolenSample>)

class AirQualityData constructor(private val airQualityDataService: AirQualityDataService) {

    suspend fun getData() = airQualityDataService.getData(
        "7bc9d9fd-16ec-4ce9-aa21-91ca0254d06e",
        "8285490e-3e65-4617-8240-c808e37c3933"
    )
}

interface AirQualityDataService {
    //@GET("/catalogo/dataset/7bc9d9fd-16ec-4ce9-aa21-91ca0254d06e/resource/8285490e-3e65-4617-8240-c808e37c3933/download/mediciones_polen.json")
    @GET("/catalogo/dataset/{dataset}/resource/{resource}/download/mediciones_polen.json")
    suspend fun getData(@Path("dataset") dataset: String, @Path("resource") resource: String): Response<AirQualityDataResponse>

    companion object {
        var retrofitService: AirQualityDataService? = null
        fun getInstance() : AirQualityDataService {
            if (retrofitService == null) {
                val interceptor= HttpLoggingInterceptor()
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://datos.comunidad.madrid/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                retrofitService = retrofit.create(AirQualityDataService::class.java)
            }
            return retrofitService!!
        }
    }
}

class DayDataActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDayDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun factorial(input: Int): Long {
        return if (input == 1) input.toLong() else input * factorial(input - 1)
    }

    private fun showMessage(date: String) {
        Toast.makeText(this, "Selected date is $date", Toast.LENGTH_LONG).show()
    }

    suspend private fun getDaySamples(stringDate: String) {
        Log.i("getDaySamples", "Loading data...")
        val airQualityDataService = AirQualityDataService.getInstance()
        val airQualityData = AirQualityData(airQualityDataService)
        val response = airQualityData.getData()

        Log.i("getDaySamples", "Processing data...")
        val data = response.body()?.data
        data?.forEach{
            if (it.fecha_lectura == stringDate) {
                Log.i("DayDataActivity", it.toString())
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val captador = intent.getStringExtra("captador").toString()
        val stringDate = intent.getStringExtra("fecha_lectura").toString()
        Log.i(LOG_TAG, "DayDataActivity: captador = $captador, fecha = $stringDate")

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val jsonString = getPolenSamplesFromJsonAsset(
                applicationContext,
                "mediciones_polen.json"
            )
            if (jsonString == null) {
                throw IOException()
            }
            val polenData = parseJson(jsonString)
            val dataSets: ArrayList<IBarDataSet> = ArrayList()
            var values: ArrayList<BarEntry>
            var index : Float = 0.toFloat()

            polenData.data.forEach {
                if (it.fecha_lectura == stringDate && it.captador == captador) {
                    Log.i("DayDataActivity", it.toString())
                    values = ArrayList()
                    values.add(
                        BarEntry(
                            index++,
                            it.granos_de_polen_x_metro_cubico.toFloat()
                        )
                    )
                    dataSets.add(BarDataSet(values, it.tipo_polinico))
                }
            }

            binding.chart.data = BarData(dataSets)
            binding.chart.invalidate()
            binding.progressBar.visibility = View.INVISIBLE

            withContext(Dispatchers.Main) {
                showMessage("Graph for the day of $stringDate")
            }
        }
    }
}