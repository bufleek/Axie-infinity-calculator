package com.axieinfinity.calculator

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.axieinfinity.calculator.FloatingService.Companion.INTENT_COMMAND_FLOAT

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!drawOverOtherAppsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission()
            }
        } else {
            startFloatingService()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (drawOverOtherAppsEnabled()) {
                startFloatingService(INTENT_COMMAND_FLOAT)
            }else{
                Toast.makeText(this, "Permissions not granted. The app will not function properly", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        finish()
    }

    private fun showDialog(titleText: String, messageText: String) {
        with(AlertDialog.Builder(this)) {
            title = titleText
            setMessage(messageText)
            setPositiveButton(R.string.common_ok) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        with(AlertDialog.Builder(this)) {
            title = "Permission required"
            setMessage("Please grant permission to draw over other apps")
            setPositiveButton("Allow") { dialog, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                try {
                    startActivityForResult(intent, PERMISSION_REQUEST_CODE)
                } catch (e: Exception) {
                    showDialog(
                        getString(R.string.permission_error_title),
                        getString(R.string.permission_error_text)
                    )
                }
                dialog.dismiss()
            }
            setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}