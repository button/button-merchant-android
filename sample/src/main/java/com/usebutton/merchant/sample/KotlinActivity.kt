package com.usebutton.merchant.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.usebutton.merchant.ButtonMerchant
import com.usebutton.merchant.Order.Builder
import com.usebutton.merchant.sample.R.id
import java.util.Random

class KotlinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ButtonMerchant.trackIncomingIntent(this, intent)
        val token = ButtonMerchant.getAttributionToken(this)
        Log.d(TAG, "Attribution Token is $token")

        val textView = findViewById<TextView>(R.id.attribution_token)
        textView.text = ButtonMerchant.getAttributionToken(this)

        checkForPostInstallIntent()
        initTrackNewIntentButton()
        initTackOrderButton()
        initClearDataButton()
        initAttributionTokenListener()
    }

    private fun checkForPostInstallIntent() {
        ButtonMerchant.handlePostInstallIntent(this) { intent, t ->
            if (intent != null) {
                startActivity(intent)
            } else if (t != null) {
                Log.e(TAG, "Error checking post install intent", t)
            }
        }
    }

    private fun initTrackNewIntentButton() {
        findViewById<View>(id.track_new_intent).setOnClickListener { v ->
            val intent = Intent()
            intent.data = Uri.parse(TEST_URL + Random().nextInt(100000))
            ButtonMerchant.trackIncomingIntent(v.context, intent)
        }
    }

    private fun initTackOrderButton() {
        findViewById<View>(id.track_order).setOnClickListener {
            val order = Builder("order-id-123")
                    .setAmount(8999)
                    .setCurrencyCode("USD")
                    .build()
            ButtonMerchant.trackOrder(this, order) { t ->
                if (t == null) {
                    toastify("Order track success")
                } else {
                    toastify("Order track error")
                }
            }
        }
    }

    private fun initClearDataButton() {
        findViewById<View>(id.clear_all_data).setOnClickListener {
            ButtonMerchant.clearAllData(this)
            Log.d(TAG, "Cleared all data")

            val token = ButtonMerchant.getAttributionToken(this)
            Log.d(TAG, "Attribution Token is $token")

            val textView = findViewById<TextView>(id.attribution_token)
            textView.text = ButtonMerchant.getAttributionToken(this)
        }
    }

    private fun initAttributionTokenListener() {
        ButtonMerchant.addAttributionTokenListener(this) { token ->
            findViewById<TextView>(id.attribution_token).text = token
        }
    }

    private fun toastify(message: String) {
        Toast.makeText(this@KotlinActivity, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "KotlinActivity"
        private const val TEST_URL = "https://sample-merchant.usebutton.com/?btn_ref=srctok-test"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val kotlinItem = menu.findItem(R.id.action_switch_kotlin)
        val javaItem = menu.findItem(R.id.action_switch_java)
        kotlinItem.isVisible = false
        javaItem.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_kotlin -> {
            }
            R.id.action_switch_java -> {
                val i = Intent(this@KotlinActivity, MainActivity::class.java)
                finish()
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
