package com.android.touchpoint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentMode extends AppCompatActivity {

    private RadioGroup radioGroup;
    private Button proceedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        radioGroup = findViewById(R.id.radio_group);
        proceedButton = findViewById(R.id.proceed_button);

        proceedButton.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);

            if (selectedRadioButton != null) {
                String selectedText = selectedRadioButton.getText().toString();

                // Get data from intent
                Intent intent = getIntent();
                String gate = intent.getStringExtra("gate");
                String accessType = intent.getStringExtra("accessType");
                String code = intent.getStringExtra("code");
                String entryTime = intent.getStringExtra("entryTime");
                String payTime = intent.getStringExtra("payTime");
                String vehicleClass = intent.getStringExtra("vClass");
                String billAmount = intent.getStringExtra("bill");
                String parkingTime = intent.getStringExtra("pTime");

                Intent newIntent;
                switch (selectedText) {
                    case "Cash":
                        newIntent = new Intent(PaymentMode.this, CashPayment.class);
                        break;
                    case "GCash":
                    case "Paymaya":
                        newIntent = new Intent(PaymentMode.this, EwalletPayment.class);
                        break;
                    case "Complimentary":
                        newIntent = new Intent(PaymentMode.this, ComplimentaryPayment.class);
                        break;
                    default:
                        Toast.makeText(PaymentMode.this, "No valid payment mode selected", Toast.LENGTH_SHORT).show();
                        return;
                }

                // Pass data to the new activity
                newIntent.putExtra("gate", gate);
                newIntent.putExtra("accessType", accessType);
                newIntent.putExtra("code", code);
                newIntent.putExtra("entryTime", entryTime);
                newIntent.putExtra("payTime", payTime);
                newIntent.putExtra("vClass", vehicleClass);
                newIntent.putExtra("bill", billAmount);
                newIntent.putExtra("pTime", parkingTime);

                startActivity(newIntent);
            } else {
                Toast.makeText(PaymentMode.this, "No payment mode selected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
