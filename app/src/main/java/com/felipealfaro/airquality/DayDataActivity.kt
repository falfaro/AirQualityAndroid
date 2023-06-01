package com.felipealfaro.airquality

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.felipealfaro.airquality.databinding.ActivityDayDataBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.Period

class DayDataActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDayDataBinding

    private var captador: String = ""
    private var fecha_lectura: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun showMessage(date: String) {
        Toast.makeText(this, "Selected date is $date", Toast.LENGTH_LONG).show()
    }

    private fun setupChart(captador: String, fecha_lectura: String) {
        val polenData = Polen.getPolenSamplesFromJsonAsset(
            applicationContext,
            "mediciones_polen.json"
        )
        if (polenData == null) {
            throw IOException("Unable to retrieve samples JSON data")
        }

        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        var values: ArrayList<BarEntry>
        var index : Float = 0.toFloat()

        polenData.data.forEach {
            val current_fecha_lectura = "$fecha_lectura 00:00:00.0"
            if (it.fecha_lectura == current_fecha_lectura && it.captador == captador) {
                values = ArrayList()
                if (it.granos_de_polen_x_metro_cubico != "") {
                    values.add(
                        BarEntry(
                            index++,
                            it.granos_de_polen_x_metro_cubico.toFloat()
                        )
                    )
                }
                dataSets.add(BarDataSet(values, it.tipo_polinico))
            }
        }

        binding.chart.description.isEnabled = false
        binding.chart.setPinchZoom(false)
        binding.chart.setDrawBarShadow(false)
        binding.chart.setDrawGridBackground(false)
        binding.chart.axisLeft.setDrawGridLines(false)
        binding.chart.axisRight.setDrawGridLines(false)
        binding.chart.legend.isWordWrapEnabled = true
        binding.chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        binding.chart.legend.orientation = Legend.LegendOrientation.VERTICAL
        binding.chart.legend.setDrawInside(true)
        binding.chart.data = BarData(dataSets)

        binding.selectedDate.text = fecha_lectura
        binding.chart.invalidate()
    }

    private fun scrollDay(period: Period) {
        val date = LocalDate.parse(fecha_lectura)
        fecha_lectura = (date + period).toString()
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                setupChart(captador, fecha_lectura)
            }
        }
    }

    private fun setupButtons() {
        binding.leftButton.setOnClickListener {
            scrollDay(Period.of(0, 0, -1))
        }
        binding.rightButton.setOnClickListener {
            scrollDay(Period.of(0, 0, 1))
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            captador = intent.getStringExtra("captador").toString()
            fecha_lectura = intent.getStringExtra("fecha_lectura").toString()
            Log.i(LOG_TAG, "DayDataActivity: captador = $captador, fecha = $fecha_lectura")

            setupButtons()
            withContext(Dispatchers.Main) {
                try {
                    binding.progressBar.visibility = View.VISIBLE
                    setupChart(captador, fecha_lectura)
                } catch (e: IOException) {
                    showMessage(e.message.toString())
                } finally {
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}