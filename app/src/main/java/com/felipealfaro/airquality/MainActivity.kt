package com.felipealfaro.airquality

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.felipealfaro.airquality.databinding.ActivityMainBinding
import java.text.SimpleDateFormat

private fun dateToString(date: Long) : String {
    val format = SimpleDateFormat("yyyy-MM-dd 00:00:00.0")
    return format.format(date)
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var selectedDate : String = ""

    private fun setupCalendar() {
        selectedDate = dateToString(binding.calendarView.date)
        binding.calendarView.setOnDateChangeListener { calendarView, year, month, day ->
            // Note that months are indexed from 0. So, 0 means January, 1 means February, etc.
            selectedDate = "%04d-%02d-%02d 00:00:00.0".format(year, month + 1, day)
        }
    }

    private fun setupButton() {
        binding.button.setOnClickListener {
            // Launch the activity used to show air quality data for
            // the given day selected from the calendar view
            Log.i("MainActivity", "selected date is $selectedDate")
            val intent = Intent(this, DayDataActivity::class.java)
            intent.putExtra("captador", binding.textView.text)
            intent.putExtra("fecha_lectura", selectedDate)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.text = "AYTM"
        setupCalendar()
        setupButton()
    }
}