package com.psydrite.bugsnap

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.lang.ref.WeakReference
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private val contextRef = WeakReference(context)
    private val sensorManager: SensorManager?
        get() = contextRef.get()?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // tuning constants
    private val SHAKE_THRESHOLD = 25f        // m/s² — increase if too sensitive
    private val SHAKE_COOLDOWN_MS = 1000L    // prevent multiple triggers per shake
    private val MIN_SHAKES = 2              // number of spikes needed to count as a shake

    private var lastShakeTime = 0L
    private var shakeCount = 0
    private var lastSpike = 0L

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // subtract gravity to get actual movement force
        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

        val now = System.currentTimeMillis()

        if (acceleration > SHAKE_THRESHOLD) {
            // count spikes within a 500ms window as one shake motion
            if (now - lastSpike > 500) {
                shakeCount = 0
            }
            shakeCount++
            lastSpike = now

            if (shakeCount >= MIN_SHAKES) {
                // enforce cooldown so one shake = one trigger
                if (now - lastShakeTime > SHAKE_COOLDOWN_MS) {
                    lastShakeTime = now
                    shakeCount = 0
                    onShakeDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}