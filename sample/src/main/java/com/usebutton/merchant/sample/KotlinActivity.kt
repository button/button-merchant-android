package com.usebutton.merchant.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.usebutton.merchant.ButtonMerchant
import com.usebutton.merchant.ButtonProduct
import com.usebutton.merchant.ButtonProductCompatible
import com.usebutton.merchant.Order
import com.usebutton.merchant.sample.R.id
import java.util.*

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
        initTrackOrderButton()
        initActivityButtons()
        initClearDataButton()
        initAttributionTokenListener()
        initReportOrderButton()
        initReportEventButton()
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
        findViewById<View>(R.id.track_new_intent).setOnClickListener { v ->
            val intent = Intent()
            intent.data = Uri.parse(TEST_URL + Random().nextInt(100000))
            ButtonMerchant.trackIncomingIntent(v.context, intent)
        }
    }

    private fun initTrackOrderButton() {
        findViewById<View>(R.id.track_order).setOnClickListener {
            val order = Order.Builder("order-id-123")
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

    private fun initReportOrderButton() {
        findViewById<View>(R.id.report_order).setOnClickListener {
            val lineItemId = "valid_line_item_id"
            val lineItemTotal: Long = 100
            val lineItemAttributes = Collections.singletonMap("valid_key", "valid_value")
            val lineItemCategory = listOf("valid_line_item_category")
            val lineItemUpc = "valid_line_item_upc"
            val lineItemSku = "valid_line_item_sku"
            val lineItemDescription = "valid_line_item_description"
            val lineItemQuantity = 5
            val lineItem = Order.LineItem.Builder(lineItemId, lineItemTotal)
                    .setAttributes(lineItemAttributes)
                    .setCategory(lineItemCategory)
                    .setUpc(lineItemUpc)
                    .setSku(lineItemSku)
                    .setDescription(lineItemDescription)
                    .setQuantity(lineItemQuantity)
                    .build()

            val customerId = "valid_customer_id"
            val customerEmail = "valid_customer_email"
            val customer = Order.Customer.Builder(customerId)
                    .setEmail(customerEmail)
                    .build()

            val orderId = UUID.randomUUID().toString()
            val purchaseDate = Date()
            val customerOrderId = "valid_customer_order_id"

            val order = Order.Builder(orderId, purchaseDate, listOf(lineItem))
                    .setCustomerOrderId(customerOrderId)
                    .setCustomer(customer)
                    .build()
            ButtonMerchant.reportOrder(this, order) { t ->
                if (t == null) {
                    toastify("Report order success")
                } else {
                    toastify("Report order error")
                }
            }
        }
    }

    private fun initReportEventButton() {
        findViewById<View>(R.id.report_event).setOnClickListener {
            ButtonMerchant.reportEvent(this, "signup", mapOf("is_new" to "false"))
        }
    }

    private fun initActivityButtons() {
        ButtonMerchant.reportEvent(this, "test-event", mapOf("isNewCustomer" to "false"))
        findViewById<View>(id.track_product_viewed).setOnClickListener {
            ButtonMerchant.activity().productViewed(product)
        }

        findViewById<View>(id.track_add_cart).setOnClickListener {
            ButtonMerchant.activity().productAddedToCart(product)
        }

        findViewById<View>(id.track_cart_viewed).setOnClickListener {
            ButtonMerchant.activity().cartViewed(listOf(product, product))
        }
    }

    private fun initClearDataButton() {
        findViewById<View>(R.id.clear_all_data).setOnClickListener {
            ButtonMerchant.clearAllData(this)
            Log.d(TAG, "Cleared all data")

            val token = ButtonMerchant.getAttributionToken(this)
            Log.d(TAG, "Attribution Token is $token")

            val textView = findViewById<TextView>(R.id.attribution_token)
            textView.text = ButtonMerchant.getAttributionToken(this)
        }
    }

    private fun initAttributionTokenListener() {
        ButtonMerchant.addAttributionTokenListener(this) { token ->
            findViewById<TextView>(R.id.attribution_token).text = token
        }
    }

    private fun toastify(message: String) {
        Toast.makeText(this@KotlinActivity, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "KotlinActivity"
        private var TEST_URL = String.format(Locale.getDefault(),
                "https://example.com/p/123?btn_ref=srctok-abc%d&from_landing=true&from_tracking=false&btn_blargh=blergh&other_param=some_val",
                Random().nextInt(100000))
        private val product: ButtonProductCompatible = ButtonProduct().apply {
            id = "prod-123"
            upc = "1234567890"
            categories = listOf("daily-deals", "black-friday")
            name = "Sample Product"
            currency = "USD"
            value = 129900
            quantity = 1
            url = "https://www.bestbuy.com/site/samsung-65-class-qled-q70-series-4k-uhd-tv-smart-led-with-hdr/6402399.p?skuId=6402399"
            attributes = mapOf(Pair("size", "large"), Pair("in_stock", "true"))
        }
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
