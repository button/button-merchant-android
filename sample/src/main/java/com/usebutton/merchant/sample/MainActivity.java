/*
 * MainActivity.java
 *
 * Copyright (c) 2018 Button, Inc. (https://usebutton.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.usebutton.merchant.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.usebutton.merchant.ButtonMerchant;
import com.usebutton.merchant.Order;
import com.usebutton.merchant.OrderListener;
import com.usebutton.merchant.PostInstallIntentListener;
import com.usebutton.merchant.UserActivityListener;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Sample app main activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButtonMerchant.trackIncomingIntent(this, getIntent());
        Log.d(TAG, String.format("Attribution Token is %s",
                ButtonMerchant.getAttributionToken(this)));

        TextView textView = findViewById(R.id.attribution_token);
        textView.setText(ButtonMerchant.getAttributionToken(this));

        ButtonMerchant.handlePostInstallIntent(this, new PostInstallIntentListener() {
            @Override
            public void onResult(@Nullable Intent intent, @Nullable Throwable t) {
                if (intent != null) {
                    startActivity(intent);
                } else if (t != null) {
                    Log.e(TAG, "Error checking post install intent", t);
                }
            }
        });

        final Context context = this;

        findViewById(R.id.report_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lineItemId = "valid_line_item_id";
                long lineItemTotal = 100;
                Map<String, String>
                        lineItemAttributes = Collections.singletonMap("valid_key", "valid_value");
                List<String> lineItemCategory =
                        Collections.singletonList("valid_line_item_category");
                String lineItemUpc = "valid_line_item_upc";
                String lineItemSku = "valid_line_item_sku";
                String lineItemDescription = "valid_line_item_description";
                int lineItemQuantity = 5;
                Order.LineItem lineItem = new Order.LineItem.Builder(lineItemId, lineItemTotal)
                        .setAttributes(lineItemAttributes)
                        .setCategory(lineItemCategory)
                        .setUpc(lineItemUpc)
                        .setSku(lineItemSku)
                        .setDescription(lineItemDescription)
                        .setQuantity(lineItemQuantity)
                        .build();

                String customerId = "valid_customer_id";
                String customerEmail = "test@usebutton.com";
                Order.Customer customer = new Order.Customer.Builder(customerId)
                        .setEmail(customerEmail)
                        .build();

                String orderId = UUID.randomUUID().toString();
                Date purchaseDate = new Date();
                String customerOrderId = "valid_customer_order_id";

                Order order = new Order.Builder(orderId, purchaseDate,
                        Collections.singletonList(lineItem))
                        .setCustomerOrderId(customerOrderId)
                        .setCustomer(customer)
                        .build();

                ButtonMerchant.reportOrder(context, order, new OrderListener() {
                    @Override
                    public void onResult(@Nullable Throwable throwable) {
                        if (throwable == null) {
                            Toast.makeText(context, "Report order success",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "Report order error",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        findViewById(R.id.track_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Order order = new Order.Builder("order-id-123")
                        .setAmount(8999)
                        .setCurrencyCode("USD")
                        .build();
                ButtonMerchant.trackOrder(context, order, new UserActivityListener() {
                    @Override
                    public void onResult(@Nullable final Throwable t) {
                        if (t == null) {
                            Toast.makeText(context, "Order track success",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Order track error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        findViewById(R.id.clear_all_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonMerchant.clearAllData(context);
                Log.d(TAG, "Cleared all data");
                Log.d(TAG, String.format("Attribution Token is %s",
                        ButtonMerchant.getAttributionToken(context)));

                TextView textView = findViewById(R.id.attribution_token);
                textView.setText(ButtonMerchant.getAttributionToken(context));
            }
        });

        findViewById(R.id.track_new_intent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setData(
                        Uri.parse("https://sample-merchant.usebutton.com/?btn_ref=srctok-test"
                                + new Random().nextInt(100000)));
                ButtonMerchant.trackIncomingIntent(v.getContext(), intent);
            }
        });

        // Add AttributionTokenListener
        ButtonMerchant.addAttributionTokenListener(this,
                new ButtonMerchant.AttributionTokenListener() {
                    @Override
                    public void onAttributionTokenChanged(@NonNull final String token) {
                        TextView textView = findViewById(R.id.attribution_token);
                        textView.setText(token);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem javaItem = menu.findItem(R.id.action_switch_java);
        MenuItem kotlinItem = menu.findItem(R.id.action_switch_kotlin);
        javaItem.setVisible(false);
        kotlinItem.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_java:
                break;
            case R.id.action_switch_kotlin:
                Intent i = new Intent(MainActivity.this, KotlinActivity.class);
                finish();
                startActivity(i);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
