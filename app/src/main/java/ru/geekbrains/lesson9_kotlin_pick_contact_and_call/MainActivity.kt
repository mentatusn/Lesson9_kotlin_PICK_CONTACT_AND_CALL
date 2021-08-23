package ru.geekbrains.lesson9_kotlin_pick_contact_and_call

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import ru.geekbrains.lesson9_kotlin_pick_contact_and_call.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var number: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonPick.setOnClickListener {

            val contactPicker: ContactPicker? = ContactPicker.create(
                activity = this,
                onContactPicked = {

                    binding.text.text = "${it.name}: ${it.number}"
                    number=it.number
                },
                onFailure = { binding.buttonPick.text = it.localizedMessage })

            contactPicker?.pick() // call this to open the picker app chooser
        }

        binding.buttonCall.setOnClickListener {
            onCallBtnClick(number)
        }
    }

    private fun onCallBtnClick(number: String) {
        if (Build.VERSION.SDK_INT < 23) {
            phoneCall(number)
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) === PackageManager.PERMISSION_GRANTED
            ) {
                phoneCall(number)
            } else {
                val PERMISSIONS_STORAGE = arrayOf<String>(Manifest.permission.CALL_PHONE)
                //Asking request Permissions
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 9)
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var permissionGranted = false
        when (requestCode) {
            9 -> permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (permissionGranted) {
            phoneCall(number)
        } else {
            Toast.makeText(this, "You don't assign permission.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun phoneCall(number: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            startActivity(callIntent)
        } else {
            Toast.makeText(this, "You don't assign permission.", Toast.LENGTH_SHORT).show()
        }
    }
}