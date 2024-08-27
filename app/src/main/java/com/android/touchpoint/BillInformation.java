package com.android.touchpoint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BillInformation extends AppCompatActivity {

    private TextView tvGate, tvAccessType, tvCode, tvEntryTime, tvPayTime, tvVehicleClass, tvBillAmount, tvParkingTime;
    private Button btnContinue, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_information);

        // Make the activity full screen and immersive
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        tvGate = findViewById(R.id.tvGate);
        tvAccessType = findViewById(R.id.tvAccessType);
        tvCode = findViewById(R.id.tvCode);
        tvEntryTime = findViewById(R.id.tvEntryTime);
        tvPayTime = findViewById(R.id.tvPayTime);
        tvVehicleClass = findViewById(R.id.tvVehicleClass);
        tvBillAmount = findViewById(R.id.tvAmount);
        tvParkingTime = findViewById(R.id.tvParkingTime);
        btnContinue = findViewById(R.id.btnContinue);
        btnCancel = findViewById(R.id.btnCancel);

        // Get data from intent and set to TextViews
        Intent intent = getIntent();
        tvGate.setText(intent.getStringExtra("gate"));
        tvAccessType.setText(intent.getStringExtra("accessType"));
        tvCode.setText(intent.getStringExtra("code"));
        tvEntryTime.setText(intent.getStringExtra("entryTime"));
        tvPayTime.setText(intent.getStringExtra("payTime"));
        tvVehicleClass.setText(intent.getStringExtra("vClass"));
        tvBillAmount.setText(intent.getStringExtra("bill"));
        tvParkingTime.setText(intent.getStringExtra("pTime"));

        btnContinue.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(BillInformation.this, PaymentMode.class);
            paymentIntent.putExtra("gate", tvGate.getText().toString());
            paymentIntent.putExtra("accessType", tvAccessType.getText().toString());
            paymentIntent.putExtra("code", tvCode.getText().toString());
            paymentIntent.putExtra("entryTime", tvEntryTime.getText().toString());
            paymentIntent.putExtra("payTime", tvPayTime.getText().toString());
            paymentIntent.putExtra("vClass", tvVehicleClass.getText().toString());
            paymentIntent.putExtra("bill", tvBillAmount.getText().toString());
            paymentIntent.putExtra("pTime", tvParkingTime.getText().toString());
            startActivity(paymentIntent);
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
