package com.example.ridesharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ridesharing.route.RoutePlanActivity;

public class HomeActivity extends AppCompatActivity {

    private Button mBtn_map;
    private Button mBtn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBtn_map = findViewById(R.id.home_btn_map);
        mBtn_logout = findViewById(R.id.home_logout);

        setListeners();
    }

    private void setListeners(){
        OnClick onClick = new OnClick();
        mBtn_map.setOnClickListener(onClick);
        mBtn_logout.setOnClickListener(onClick);
    }

    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.home_btn_map:
                    intent = new Intent(HomeActivity.this, MapActivity.class);
                    break;
                case R.id.home_logout:
                    intent = new Intent(HomeActivity.this, LoginActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }
}

