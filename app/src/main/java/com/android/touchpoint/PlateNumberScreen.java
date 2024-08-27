package com.android.touchpoint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class PlateNumberScreen extends AppCompatActivity {
    private EditText etplate;
    private Button btncontinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plate_number_screen);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        getWidgetId();
        initWidget();
    }

    private void getWidgetId() {
        etplate = findViewById(R.id.etPlateNumber);
        btncontinue = findViewById(R.id.btnContinue);
    }

    private void initWidget() {
        btncontinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String plateNumber = etplate.getText().toString();
                String url = "http://192.168.1.100/parkingci/handheld/terminalBillRequest?access=Plate&code=" + plateNumber;
                new HttpAsyncTask(PlateNumberScreen.this).execute(url);
            }
        });
    }

    public void displayBillInformation(String id, String accessType, String code, String gate, String vClass, String entryTime, String payTime, String pTime, String bill) {
        Intent intent = new Intent(this, BillInformation.class);
        intent.putExtra("id", id);
        intent.putExtra("accessType", accessType);
        intent.putExtra("code", code);
        intent.putExtra("gate", gate);
        intent.putExtra("vClass", vClass);
        intent.putExtra("entryTime", entryTime);
        intent.putExtra("payTime", payTime);
        intent.putExtra("pTime", pTime);
        intent.putExtra("bill", bill);
        startActivity(intent);
    }
}
