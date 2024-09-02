package com.example.phonepeoptions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val myFragment = PaymentOptionsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, myFragment)
                .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //RESULT_OK does not mean payment is successful
                        Toast.makeText(this, "RESULT_OK", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        Toast.makeText(this, "RESULT_CANCELED", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 725
    }
}