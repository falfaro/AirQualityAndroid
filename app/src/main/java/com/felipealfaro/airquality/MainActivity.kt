package com.felipealfaro.airquality

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.felipealfaro.airquality.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    private var selectedDate : String = ""
    private var selectedCaptador : String = ""

    private fun showMessage(date: String) {
        Toast.makeText(this, "Selected date is $date", Toast.LENGTH_LONG).show()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            // Note that months are indexed from 0. So, 0 means January, 1 means February, etc.
            selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
        }
    }

    private fun setupButton() {
        binding.button.setOnClickListener {
            // Launch the activity used to show air quality data for
            // the given day selected from the calendar view
            Log.i("MainActivity", "selected date is $selectedDate")
            if (selectedCaptador == "") {
                showMessage("foo")
            }
            val intent = Intent(this, DayDataActivity::class.java)
            intent.putExtra("captador", selectedCaptador)
            intent.putExtra("fecha_lectura", selectedDate)
            startActivity(intent)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        selectedCaptador = parent.getItemAtPosition(pos).toString()
        Log.i("MainActivity", "Selected captador $selectedCaptador")
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        selectedCaptador = ""
    }

    private fun setupSpinner() {
        val array : ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item
        )
        Log.i("MainActivity", "Loading data about captadores...")
        val data = Captadores.getFromJsonAsset(applicationContext, "captadores_polen.json")
        if (data == null) {
            throw IOException()
        }
        data.data.forEach {
            array.add(it.codigo)
        }
        array.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = array
        binding.spinner.onItemSelectedListener = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSpinner()
        setupCalendar()
        setupButton()
    }
}