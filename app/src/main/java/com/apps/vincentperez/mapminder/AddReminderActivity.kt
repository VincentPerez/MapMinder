package com.apps.vincentperez.mapminder

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class AddReminderActivity : AppCompatActivity() {

    lateinit var infoText:EditText
    lateinit var titleText:EditText
    lateinit var addressText:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        val editAddress = findViewById<EditText>(R.id.editAddress)
        if (intent.hasExtra("EXTRA_ADDRESS")) {
            val address: String = intent.getStringExtra("EXTRA_ADDRESS")
            editAddress.setText(address)
        }

        val savebtn = findViewById<Button>(R.id.buttonSave)
        savebtn.setOnClickListener {
            if (intent.hasExtra("EXTRA_TITLE")) {
                modifyReminder()
            } else {
                saveReminder()
            }
        }

        titleText = findViewById<EditText>(R.id.editTitle)
        infoText = findViewById<EditText>(R.id.editInfo)
        addressText = findViewById<EditText>(R.id.editAddress)

        if (intent.hasExtra("EXTRA_TITLE")) {
            titleText.setText(intent.getStringExtra("EXTRA_TITLE"))
        }
        if (intent.hasExtra("EXTRA_CONTENT")) {
            infoText.setText(intent.getStringExtra("EXTRA_CONTENT"))
        }

    }

    private fun saveReminder() {

        val values = ContentValues()
        values.put("title", titleText.text.toString())
        values.put("lat", intent.getStringExtra("EXTRA_LAT").toDouble())
        values.put("lon", intent.getStringExtra("EXTRA_LON").toDouble())
        values.put("content", infoText.text.toString())
        values.put("address", addressText.text.toString())

        val DB:DatabaseHandler = DatabaseHandler(this);
        val id: Long = DB.AddReminder(values)


        val data:Intent = Intent()
        data.putExtra("EXTRA_TITLE", titleText.text.toString())
        data.putExtra("EXTRA_LAT", intent.getStringExtra("EXTRA_LAT"))
        data.putExtra("EXTRA_LON", intent.getStringExtra("EXTRA_LON"))
        data.putExtra("EXTRA_TAG", id.toString())

        setResult(Activity.RESULT_OK, data);
        finish()
    }

    private fun modifyReminder() {

        val values = ContentValues()
        values.put("title", titleText.text.toString())
        values.put("content", infoText.text.toString())
        values.put("address", addressText.text.toString())

        val DB:DatabaseHandler = DatabaseHandler(this);
        val ret: Int = DB.UpdateMarker(values, intent.getStringExtra("EXTRA_TAG").toLong())


        val data:Intent = Intent()
        data.putExtra("EXTRA_TITLE", titleText.text.toString())
        data.putExtra("EXTRA_TAG", intent.getStringExtra("EXTRA_TAG"))
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}

