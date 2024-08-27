package com.android.touchpoint;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class DemoScreen extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private ListView listView;
    private BluetoothDevice selectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_demo_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.device_list);
        Button btnTestPrint = findViewById(R.id.btn_test_print);
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(deviceListAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            checkLocationPermission();
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            String address = item.substring(item.length() - 17);
            selectedDevice = bluetoothAdapter.getRemoteDevice(address);
            Toast.makeText(DemoScreen.this, "Device selected: " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
        });

        btnTestPrint.setOnClickListener(v -> {
            if (selectedDevice != null) {
                printData(selectedDevice, generateReceipt());
            } else {
                Toast.makeText(DemoScreen.this, "No device selected", Toast.LENGTH_SHORT).show();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            discoverDevices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                discoverDevices();
            } else {
                Toast.makeText(this, "Location permission is required to discover devices", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceInfo = device.getName() + "\n" + device.getAddress();
                    deviceListAdapter.add(deviceInfo);
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void discoverDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void printData(BluetoothDevice device, String data) {
        BluetoothPrinterService printerService = new BluetoothPrinterService(device);
        try {
            printerService.print(data); // Use print method directly
        } finally {
            printerService.close();
        }
    }

    private String generateReceipt() {
        StringBuilder receipt = new StringBuilder();

        // ESC/POS commands as byte arrays
        byte[] largeTextOn = new byte[]{0x1D, 0x21, 0x11}; // Command to set text size to large
        byte[] largeTextOff = new byte[]{0x1D, 0x21, 0x00}; // Command to reset text size to normal
        byte[] centerTextOn = new byte[]{0x1B, 0x61, 0x01}; // Command to center text
        byte[] alignLeft = new byte[]{0x1B, 0x61, 0x00}; // Command to align text to left

        receipt.append(new String(centerTextOn)); // Center text
        receipt.append(new String(largeTextOn)); // Enable large text
        receipt.append("PICC\n"); // Header text
        receipt.append(new String(largeTextOff)); // Reset text size to normal
        receipt.append(new String(alignLeft)); // Align text to left

        receipt.append("Sub Header: Philippine International Convention Center\n");
        receipt.append("Date: Date Printed\n");
        receipt.append("Address: PICC Complex, 1307 Pasay City, Metro Manila, Philippines\n");
        receipt.append("Contact: (+63)9602177347\n");
        receipt.append("VAT-REG-TIN: 000-000-000-0000\n");
        receipt.append("MIN: 000000000\n");
        receipt.append("OR Number: 0000000\n");
        receipt.append("Vehicle: Car\n\n");

        receipt.append("Additional Header: Receipt\n");
        receipt.append("Cashier Name: Cashier\n");
        receipt.append("Divider: =======================\n");
        receipt.append("Gate In: 2024-07-16 12:07:16\n");
        receipt.append("Bill Time: 07-17-2024 13:45:50 PM\n");
        receipt.append("Parking Time: 25:37\n");
        receipt.append("Amount Due: PHP 178.57\n");
        receipt.append("Vat(12%): PHP 21.43\n");
        receipt.append("Total Amount Due: PHP 200\n\n");

        receipt.append("Divider: =======================\n");
        receipt.append("Vatable Sales: PHP 178.57\n");
        receipt.append("Vat-Exempt: PHP 0.0\n");
        receipt.append("Discount: PHP 0\n");
        receipt.append("Payment Method: Cash\n\n");

        receipt.append("Footer Content\n");
        receipt.append("NTEKSYSTEMS Incorporation\n");
        receipt.append("ACCREDITATION: 000000000000000\n");
        receipt.append("Date Issued: 12/12/2020\n");
        receipt.append("Valid Until: 12/12/2024\n");
        receipt.append("BIR PTU ISSUED: AA0000000-00000000\n");
        receipt.append("PTU DATE ISSUED: 11/24/2024\n");
        receipt.append("THIS SERVES AS AN OFFICIAL RECEIPT\n");

        // Adding approximately 40dp space at the bottom
        int numberOfLines = (int) (40 * getResources().getDisplayMetrics().density / 30); // Adjust for DPI
        for (int i = 0; i < numberOfLines; i++) {
            receipt.append("\n");
        }

        return receipt.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private class BluetoothPrinterService {
        private final BluetoothDevice printerDevice;
        private BluetoothSocket bluetoothSocket;
        private OutputStream outputStream;

        public BluetoothPrinterService(BluetoothDevice device) {
            this.printerDevice = device;
            try {
                bluetoothSocket = printerDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void print(String data) {
            try {
                outputStream.write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                outputStream.close();
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
