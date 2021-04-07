package com.example.ridesharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.home_btn_go:
                    break;
                case R.id.home_btn_map:
                    break;
                case R.id.home_btn_order:
                    break;
                case R.id.home_logout:
                    intent = new Intent(HomeActivity.this, LoginActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }
}

