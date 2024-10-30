package com.example.test_run.presentation

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.test_run.presentation.theme.Test_runTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {
    fun send(url: String, data: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlConnection =
                    withContext(Dispatchers.IO) {
                        URL(url).openConnection()
                    } as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "text/plain")
                urlConnection.doOutput = true

                // Send data
                OutputStreamWriter(urlConnection.outputStream).use { writer ->
                    writer.write(data)
                    writer.flush()
                }

                // Check response
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    println("Data sent successfully!")
                } else {
                    println("Error sending data: ${urlConnection.responseCode}")
                }

                urlConnection.disconnect()
            } catch (e: Exception) {
                println("Exception occurred: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }
    val executorService = Executors.newSingleThreadExecutor()
    fun run_it(){
        var i = 10
        while (true) {
            send("http://10.100.51.249:5000/wearos", "heart $heart")
            Thread.sleep(1000)
            i--
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown() // Shutdown the executor service when done
    }

    public var heart = "0";
    public var gyro_x1 = "0"
    var gyro_y1 = "0"
    var gyro_z1 = "0"
    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val heartRateSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }
    private val gyroscopeSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }
    private val accelerometersensor:Sensor? by lazy{
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }



    private var heartRate by mutableStateOf(0f)
    private var gyro_x by mutableStateOf(0f)
    private var gyro_y by mutableStateOf(0f)
    private var gyro_z by mutableStateOf(0f)
    private var acc_x by mutableStateOf(0f)
    private var acc_y by mutableStateOf(0f)
    private var acc_z by mutableStateOf(0f)


    private val heartRateListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                heartRate = event.values[0]
                heart = heartRate.toString()
                
            }
            if(event.sensor.type == Sensor.TYPE_GYROSCOPE){
                gyro_x = event.values[0]
                gyro_x1 = gyro_x.toString()

                gyro_y = event.values[1]
                //gyro_y1 = gyro_y.toString()
                gyro_z = event.values[2]
                //gyro_z1 = gyro_z.toString()
            }
            if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
                acc_x = event.values[0]
                acc_y = event.values[1]
                acc_z = event.values[2]
            }
            if(event.sensor.type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT){
                val send_alert = 1
                println(send_alert)
                send("http://10.100.51.249:5000/wearos", "$send_alert")

            }

        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BODY_SENSORS),
                REQUEST_BODY_SENSORS_PERMISSION)
        }

        executorService.execute {
            run_it()
        }





        setContent {

            WearApp(greetingName =

                    "heart_rate: $heartRate\n" +
                    "GYRO\n"+
                    "$gyro_x\n" +
                    "$gyro_y\n" +
                    "$gyro_z\n" +
                    "accelo\n" +
                    "$acc_x\n" +
                    "$acc_y\n" +
                    "$acc_z\n"

                    )
        }
    }

    override fun onResume() {
        super.onResume()
        heartRateSensor?.let {
            sensorManager.registerListener(heartRateListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometersensor?.let { sensorManager.registerListener(heartRateListener,it,SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscopeSensor?.let { sensorManager.registerListener(heartRateListener,it,SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(heartRateListener)
    }
}

@Composable
fun WearApp(greetingName: String) {
    Test_runTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = greetingName
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Heart Rate: 70")
}

// Request code for body sensor permission
private const val REQUEST_BODY_SENSORS_PERMISSION = 1
