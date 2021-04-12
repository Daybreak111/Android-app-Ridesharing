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
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import com.example.ridesharing.route.RoutePlanActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import common.MySQL_client;
import common.Order;

public class OrderActivity extends AppCompatActivity {

    private TextView order_id;
    private TextView order_from;
    private TextView order_to;
    private TextView order_distance;
    private TextView order_duration;
    private TextView order_fare;
    private TextView order_state;

    private Button btn_cancel;

    private int state;
    private String order_uid;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 0:
                    order_id.append(msg.getData().getString("order_id"));
                    order_from.append(msg.getData().getString("order_from"));
                    order_to.append(msg.getData().getString("order_to"));
                    order_distance.append(msg.getData().getString("order_distance"));
                    order_duration.append(msg.getData().getString("order_duration"));
                    order_fare.append(msg.getData().getString("order_fare"));
                    state = msg.getData().getInt("order_state");
                    order_uid = msg.getData().getString("order_id");
                    if(state==0){
                        order_state.append("正在为您匹配司机，请稍后...");
                        setCancelUI();
                        waitSearchOrder();
                    }else if(state==1){
                        order_state.append("订单进行中");
                    }
                    break;
                case 1:
                    Toast.makeText(OrderActivity.this, "订单已被取消", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(OrderActivity.this, MapActivity.class);
                    startActivity(intent);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        order_id = findViewById(R.id.order_id);
        order_from = findViewById(R.id.order_from);
        order_to = findViewById(R.id.order_to);
        order_distance = findViewById(R.id.order_distance);
        order_duration = findViewById(R.id.order_duration);
        order_fare = findViewById(R.id.order_fare);
        order_state = findViewById(R.id.order_state);
        btn_cancel = findViewById(R.id.order_btn_cancel);

        searchOrder();
    }

    private void searchOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] var = {"order_id", "driver_uid", "origin", "destination", "state", "suborder1", "suborder2", "distance", "duration", "fee" };
                String sql = String.format("SELECT * from rideorder WHERE user_uid='%s' and state < 2;", LoginActivity.account);
                Log.e("SQL", sql);
                Map<String, String> result = MySQL_client.sql_Select(sql, var);

                Message msg = Message.obtain();
                msg.what = 0;
                Bundle bundle = new Bundle();
                bundle.putString("order_id", result.get("order_id"));
                bundle.putString("order_from", result.get("origin"));
                bundle.putString("order_to", result.get("destination"));
                bundle.putString("order_distance", result.get("distance"));
                bundle.putString("order_duration", result.get("duration"));
                bundle.putString("order_fare", result.get("fee"));
                bundle.putInt("order_state", Integer.parseInt(Objects.requireNonNull(result.get("state"))));
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void waitSearchOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String[] var = {"order_id", "driver_uid", "origin", "destination", "state", "suborder1", "suborder2", "distance", "duration", "fee" };
                String sql = String.format("SELECT * from rideorder WHERE user_uid='%s' and state < 2;", LoginActivity.account);
                Map<String, String> result = MySQL_client.sql_Select(sql, var);

                Message msg = Message.obtain();
                msg.what = 0;
                Bundle bundle = new Bundle();
                bundle.putString("order_id", result.get("order_id"));
                bundle.putString("order_from", result.get("origin"));
                bundle.putString("order_to", result.get("destination"));
                bundle.putString("order_distance", result.get("distance"));
                bundle.putString("order_duration", result.get("duration"));
                bundle.putString("order_fare", result.get("fee"));
                bundle.putInt("order_state", Integer.parseInt(result.get("state")));
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
    }

    private void setCancelUI(){
        btn_cancel.setVisibility(View.VISIBLE);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Order order = new Order();
                order.order_id = order_uid;
                order.user_uid = LoginActivity.account;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        order.cancelOrder();
                        Message msg = Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
    }
}