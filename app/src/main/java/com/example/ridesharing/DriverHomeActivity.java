package com.example.ridesharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DriverHomeActivity extends AppCompatActivity {

    private Button mBtn_getOrder;
    private Button mBtn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        mBtn_getOrder = findViewById(R.id.home_btn_getOrder);
        mBtn_logout = findViewById(R.id.driver_logout);

        setListeners();
    }

    private void setListeners(){
        OnClick onClick = new OnClick();
        mBtn_getOrder.setOnClickListener(onClick);
        mBtn_logout.setOnClickListener(onClick);
    }

    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.driver_logout:
                    intent = new Intent(DriverHomeActivity.this, LoginActivity.class);
                    break;
                case R.id.home_btn_getOrder:
                    intent = new Intent(DriverHomeActivity.this, DriverGetOrderActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }
}