package com.android.touchpoint;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CashPayment extends AppCompatActivity {

    private EditText etCashAmount;
    private GridLayout gridLayout;
    private TextView tvBillAmount;
    private double billAmount;  // Store bill amount as a double

    // Variables for other required data
    private String id;
    private String access;
    private String plate;
    private String gate;
    private String entryTime;
    private String vehicle;
    private String payTime;
    private String payMode;
    private String pTime; // Parking time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cash_payment);

        // Initialize views
        etCashAmount = findViewById(R.id.etCashAmount);
        gridLayout = findViewById(R.id.gridLayout);
        tvBillAmount = findViewById(R.id.tvBillAmount);

        // Retrieve and set bill amount from intent
        Intent intent = getIntent();
        String billAmountString = intent.getStringExtra("bill");
        id = intent.getStringExtra("id");
        access = intent.getStringExtra("access");
        plate = intent.getStringExtra("plate");
        gate = intent.getStringExtra("gate");
        entryTime = intent.getStringExtra("entry_time");
        vehicle = intent.getStringExtra("vehicle");
        payTime = intent.getStringExtra("pay_time");
        payMode = intent.getStringExtra("pay_mode");
        pTime = intent.getStringExtra("ptime");

        if (billAmountString != null) {
            try {
                billAmount = Double.parseDouble(billAmountString);
                tvBillAmount.setText("Bill Amount: PHP " + String.format("%.2f", billAmount));
            } catch (NumberFormatException e) {
                // Handle exception if the bill amount is not a valid number
                tvBillAmount.setText("Bill Amount: PHP 0.00");
                billAmount = 0.00;
            }
        } else {
            tvBillAmount.setText("Bill Amount: PHP 0.00");
            billAmount = 0.00;
        }

        setupKeypad();

        // Proceed button
        Button btnProceed = findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(v -> {
            String amountString = etCashAmount.getText().toString();
            if (amountString.isEmpty()) {
                Toast.makeText(CashPayment.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double enteredAmount = Double.parseDouble(amountString);
                    if (enteredAmount >= billAmount) {
                        // Send data
                        new SendDataTask().execute(
                                id, access, plate, gate, entryTime, vehicle, payTime, payMode, pTime, String.valueOf(billAmount)
                        );
                    } else {
                        Toast.makeText(CashPayment.this, "Entered amount is less than the bill amount", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(CashPayment.this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupKeypad() {
        // Numeric keypad buttons
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnClear
        };

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(this::onKeypadButtonClick);
        }
    }

    private void onKeypadButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();
        String currentText = etCashAmount.getText().toString();

        switch (buttonText) {
            case "C":
                etCashAmount.setText("");
                break;
            default:
                etCashAmount.setText(currentText + buttonText);
                break;
        }
    }

    private class SendDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String access = params[1];
            String plate = params[2];
            String gate = params[3];
            String entryTime = params[4];
            String vehicle = params[5];
            String payTime = params[6];
            String payMode = params[7];
            String pTime = params[8];
            String bill = params[9];

            try {
                // Prepare URL and connection
                URL url = new URL("http://192.168.1.100W/parkingci/handheld/terminalBillPaid");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                // Prepare JSON payload
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", id);
                jsonObject.put("access", access);
                jsonObject.put("plate", plate);
                jsonObject.put("gate", gate);
                jsonObject.put("entry_time", entryTime);
                jsonObject.put("vehicle", vehicle);
                jsonObject.put("pay_time", payTime);
                jsonObject.put("paymode", payMode);
                jsonObject.put("Ptime", pTime);
                jsonObject.put("bill", bill);

                // Send POST request
                OutputStream os = connection.getOutputStream();
                os.write(jsonObject.toString().getBytes("UTF-8"));
                os.close();

                // Get response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Success";
                } else {
                    return "Failed";
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(CashPayment.this, "Data sent: " + result, Toast.LENGTH_SHORT).show();
        }
    }
}
