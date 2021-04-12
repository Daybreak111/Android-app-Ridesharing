package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;
import common.MySQL_client;

public class LoginActivity extends AppCompatActivity {

    public static String account;

    private Button mBtn_confirm;
    private EditText usernameEditText;
    private EditText passwordEditText;


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what==0){
                boolean ischecked = (boolean)  msg.obj;
                if(ischecked){
                    account = usernameEditText.getText().toString();
                    Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                }else{
                    account = usernameEditText.getText().toString();
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        }
    };

    enum Type {User, Driver};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mBtn_confirm = findViewById(R.id.login_btn_confirm);
        mBtn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameEditText = findViewById(R.id.login_username);
                passwordEditText = findViewById(R.id.login_password);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean ischecked = CheckPin(usernameEditText.getText().toString(), passwordEditText.getText().toString(), 1);
                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = ischecked;
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });
    }

    public static boolean CheckPin(String account, String pin, int type)
    {
        String pin_s = "";

        //查询数据
        String[] var = {"pin"};
        String sql = "";
        if(type==Type.User.ordinal()) {
            sql = String.format("Select pin From User WHERE account='%s' Limit 1", account);
        }else if(type==Type.Driver.ordinal()) {
            sql = String.format("Select pin From Driver WHERE account='%s' Limit 1", account);
        }
        System.out.println(sql);
        Map<String, String> result = MySQL_client.sql_Select(sql, var);
        if(result!=null) {
            pin_s = result.get("pin");
        }

        //判断密码是否正确
        return pin.equals(pin_s);

    }


}