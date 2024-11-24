package com.nguyen.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.nguyen.image.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.concurrent.thread

private const val TAG = "MainActivity"
private const val URL = "https://rkpandey.com/images/rkpDavidson.jpg"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d(TAG, "Loading image from URL into ImageView")
        // Glide provides features that useStrictMode(), useHandler() and useCoroutines don't, which
        // are: image caching, downsampling, placeholder, Transformations, etc.
        // useGlide(binding)

        // useStrictMode(binding)
        // useHandler(binding)
        useCoroutines(binding)
    }

    private fun useGlide(binding: ActivityMainBinding) {
        Glide.with(this).load(URL).into(binding.image)
    }

    private fun useStrictMode(binding: ActivityMainBinding) {
        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        // call StrictMode.setThreadPolicy() to allow calling downloadBitmap() on main thread
        StrictMode.setThreadPolicy(policy)
        val bitmap = downloadBitmap(URL)
        binding.image.setImageBitmap(bitmap)
    }

    private fun useHandler(binding: ActivityMainBinding) {
        val handler = Handler(Looper.getMainLooper())
        // start a background thread
        thread(true) {
            Log.d(TAG, "Current thread ${Thread.currentThread().name}")
            val bitmap = downloadBitmap(URL)
            handler.post {
                Log.d(TAG, "Current thread in the UI handler: ${Thread.currentThread().name}")
                binding.image.setImageBitmap(bitmap)
            }
        }
    }

    private fun useCoroutines(binding: ActivityMainBinding) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Current thread ${Thread.currentThread().name}")
            val bitmap = downloadBitmap(URL)
            withContext(Dispatchers.Main) {
                Log.d(TAG, "Current thread in the main dispatcher: ${Thread.currentThread().name}")
                binding.image.setImageBitmap(bitmap)
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val stream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Exception $e")
            null
        }
    }
}