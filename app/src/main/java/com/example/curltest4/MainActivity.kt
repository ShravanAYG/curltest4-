package com.example.curltest4

import android.content.DialogInterface
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.curltest4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.progressBar.setProgress(0, false)
        binding.sampleText.typeface = Typeface.MONOSPACE
        if(!binding.checkBox.isActivated)
            binding.scrollView2.isHorizontalScrollBarEnabled = true;

        binding.btnSearch.setOnClickListener {
            val url = binding.etURL.text.toString().trim()

            if (url.isNotEmpty()) {
                // Start progress slowly to indicate "starting"
                binding.progressBar.setProgress(10, true)

                Thread {
                    // Do the actual JNI/network work
                    val result = stringFromJNI(url)

                    runOnUiThread {
                        binding.progressBar.setProgress(90, true)
                        binding.sampleText.text = result
                        binding.progressBar.setProgress(100, true) // done
                    }
                }.start()
            } else {
                binding.progressBar.setProgress(20, true)
                binding.sampleText.text = "Please enter a URL"
                AlertDialog.Builder(this)
                    .setTitle("No URL Specified")
                    .setMessage("Please enter a URL to search")
                    .setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }
                    .show()
                binding.progressBar.setProgress(0, true)
            }
        }
        binding.btnClear.setOnClickListener {
            binding.sampleText.text = ""
            binding.progressBar.setProgress(0, true)
        }
    }

    /**
     * A native method that is implemented by the 'curltest4' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(URL: String): String

    companion object {
        // Used to load the 'curltest4' library on application startup.
        init {
            System.loadLibrary("curltest4")
        }
    }
}