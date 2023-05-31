package com.felipealfaro.airquality

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DayDataActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDayDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun showMessage(date: String) {
        Toast.makeText(this, "Selected date is $date", Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()

        val captador = intent.getStringExtra("captador").toString()
        val fecha_lectura = intent.getStringExtra("fecha_lectura").toString()
        Log.i(LOG_TAG, "DayDataActivity: captador = $captador, fecha = $fecha_lectura")

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val polenData = Polen.getPolenSamplesFromJsonAsset(
                applicationContext,
                "mediciones_polen.json"
            )
            if (polenData == null) {
                withContext(Dispatchers.Main) {
                    showMessage("Error parsing polen data from the JSON asset")
                }
                return@launch
            }
            val dataSets: ArrayList<IBarDataSet> = ArrayList()
            var values: ArrayList<BarEntry>
            var index : Float = 0.toFloat()

            polenData.data.forEach {
                if (it.fecha_lectura == fecha_lectura && it.captador == captador) {
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
                showMessage("Graph for $fecha_lectura")
            }
        }
    }
}