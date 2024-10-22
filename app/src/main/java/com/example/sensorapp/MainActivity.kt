package com.example.sensorapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import android.media.MediaPlayer

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var statusTextView: TextView
    private lateinit var resetToggleButton: ToggleButton
    private lateinit var stableSound: MediaPlayer
    private lateinit var movementSound: MediaPlayer
    private var isDetectionEnabled = true
    private var lastState: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        resetToggleButton = findViewById(R.id.resetToggleButton)
        stableSound = MediaPlayer.create(this, R.raw.stable_sound)
        movementSound = MediaPlayer.create(this, R.raw.movement_sound)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        resetToggleButton.setOnCheckedChangeListener { _, isChecked ->
            isDetectionEnabled = isChecked
            if (!isChecked) {
                statusTextView.text = "Detección apagada"
                stableSound.pause()
                movementSound.pause()
            } else {
                statusTextView.text = "Reiniciando detección..."
                lastState = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isDetectionEnabled) {
            return
        }

        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                handleAccelerometerData(it.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun handleAccelerometerData(values: FloatArray) {
        val xAxis = values[0]
        val yAxis = values[1]
        val zAxis = values[2]

        println("xAxis: $xAxis, yAxis: $yAxis, zAxis: $zAxis")

        if (Math.abs(zAxis - 9.8) < 1.0 && Math.abs(xAxis) < 1.0 && Math.abs(yAxis) < 1.0) {
            updateStatus("Estable")
        }
        else if (Math.abs(zAxis - 9.8) >= 1.0) {
            updateStatus("En Movimiento")
        } else {
            println("Estado indefinido (ni estable ni en movimiento)")
        }
    }


    private fun updateStatus(newState: String) {
        if (lastState != newState) {
            statusTextView.text = newState
            println("Cambiando estado a: $newState")

            when (newState) {
                "Estable" -> {
                    if (!stableSound.isPlaying) {
                        println("Reproduciendo sonido de estable")
                        stableSound.start()
                    }
                    if (movementSound.isPlaying) {
                        movementSound.pause()
                    }
                }
                "En Movimiento" -> {
                    if (!movementSound.isPlaying) {
                        println("Reproduciendo sonido de movimiento")
                        movementSound.start()
                    }
                    if (stableSound.isPlaying) {
                        stableSound.pause()
                    }
                }
            }
            lastState = newState
        }
    }
}
