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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Map;
import common.MySQL_client;

public class LoginActivity extends AppCompatActivity {

    public static String account;

    private Button mBtn_confirm;
    private RadioGroup mRg_type;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private int mtype;


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    Intent intent = null;
                    boolean ischecked = msg.getData().getBoolean("ischecked");
                    if(ischecked){
                        account = usernameEditText.getText().toString();
                        Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                        if(mtype==0){
                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                        }else if(mtype==1){
                            intent = new Intent(LoginActivity.this, DriverHomeActivity.class);
                        }
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mtype = -1;

        mBtn_confirm = findViewById(R.id.login_btn_confirm);
        mBtn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameEditText = findViewById(R.id.login_username);
                passwordEditText = findViewById(R.id.login_password);
                if(mtype==-1){
                    Toast.makeText(LoginActivity.this, "您还未选择角色", Toast.LENGTH_SHORT).show();
                    return;
                    }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean ischecked = CheckPin(usernameEditText.getText().toString(), passwordEditText.getText().toString(), mtype);
                        Message msg = Message.obtain();
                        msg.what = 0;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("ischecked", ischecked);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });

        mRg_type = findViewById(R.id.login_type);
        mRg_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                if(radioButton.getText().equals("User")){
                    mtype = Type.User.ordinal();
                }else if(radioButton.getText().equals("Driver")){
                    mtype = Type.Driver.ordinal();
                }
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

    enum Type {User, Driver};
}