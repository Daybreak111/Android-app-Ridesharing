package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import common.MySQL_client;
import common.Order;
import common.TKRSquery;

public class SubOrderActivity extends AppCompatActivity {

    private String super_order_id;
    private String order_id;

    private LinearLayout suborder1;
    private TextView suborder1_tv1;
    private TextView suborder1_tv2;
    private Button suborder1_btn;

    private LinearLayout suborder2;
    private TextView suborder2_tv1;
    private TextView suborder2_tv2;
    private Button suborder2_btn;

    private LinearLayout suborder3;
    private TextView suborder3_tv1;
    private TextView suborder3_tv2;
    private Button suborder3_btn;


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Intent intent = new Intent(SubOrderActivity.this, DriverGetOrderActivity.class);
            switch (msg.what) {
                case 0:
                    int has_order = msg.getData().getInt("has_order");
                    super_order_id = msg.getData().getString("order_id");
                    if(has_order!=1){
                        Toast.makeText(SubOrderActivity.this, "司机状态错误",  Toast.LENGTH_SHORT).show();
                        break;
                    }
                    getOrderFromDB(super_order_id);
                    break;
                case 1:
                    Order order = new Order();
                    order.origin_location = msg.getData().getString("latitude_from") + "," + msg.getData().getString("longitude_from");
                    order.destination_location = msg.getData().getString("latitude_to") + "," + msg.getData().getString("longitude_to");
                    order.distance = Double.parseDouble(msg.getData().getString("distance"));
                    findSubOrder(order);
                    break;
                case 2:
                    suborder1 = findViewById(R.id.suborder1);
                    suborder1_tv1 = findViewById(R.id.suborder1_tv1);
                    suborder1_tv2 = findViewById(R.id.suborder1_tv2);
                    suborder1_btn = findViewById(R.id.suborder1_btn);
                    suborder1.setVisibility(View.VISIBLE);
                    suborder1_tv1.setText(String.format("%s  ->  %s", msg.getData().getString("address_from"), msg.getData().getString("address_to")));
                    suborder1_tv2.setText(String.format("%s m  %s min  %s", msg.getData().getString("distance"), msg.getData().getString("duration"), String.valueOf(msg.getData().getDouble("detour"))));
                    order_id = msg.getData().getString("order_id");
                    suborder1_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateOrder(order_id);
                            startActivity(intent);
                        }
                    });
                    break;
                case 3:
                    suborder2 = findViewById(R.id.suborder2);
                    suborder2_tv1 = findViewById(R.id.suborder2_tv1);
                    suborder2_tv2 = findViewById(R.id.suborder2_tv2);
                    suborder2_btn = findViewById(R.id.suborder2_btn);
                    suborder2.setVisibility(View.VISIBLE);
                    suborder2_tv1.setText(String.format("%s  ->  %s", msg.getData().getString("address_from"), msg.getData().getString("address_to")));
                    suborder2_tv2.setText(String.format("%s m  %s min  %s", msg.getData().getString("distance"), msg.getData().getString("duration"), String.valueOf(msg.getData().getDouble("detour"))));
                    order_id = msg.getData().getString("order_id");
                    suborder2_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateOrder(order_id);
                            startActivity(intent);
                        }
                    });
                    break;
                case 4:
                    suborder3 = findViewById(R.id.suborder3);
                    suborder3_tv1 = findViewById(R.id.suborder3_tv1);
                    suborder3_tv2 = findViewById(R.id.suborder3_tv2);
                    suborder3_btn = findViewById(R.id.suborder3_btn);
                    suborder3.setVisibility(View.VISIBLE);
                    suborder3_tv1.setText(String.format("%s  ->  %s", msg.getData().getString("address_from"), msg.getData().getString("address_to")));
                    suborder3_tv2.setText(String.format("%s m  %s min  %s", msg.getData().getString("distance"), msg.getData().getString("duration"), String.valueOf(msg.getData().getDouble("detour"))));
                    order_id = msg.getData().getString("order_id");
                    suborder3_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateOrder(order_id);
                            startActivity(intent);
                        }
                    });
                    break;
                case 5:
                    Toast.makeText(SubOrderActivity.this, "没有可用订单，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(SubOrderActivity.this, "订单状态已发生改变，请重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_order);

        findSuperOrder();
    }

    protected  void findSuperOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String account = LoginActivity.account;
                String[] var = {"has_order", "orderId"};
                String sql = String.format("SELECT has_order, orderId from driver WHERE account='%s'", account);
                Log.e("sql", sql);
                Map<String, String> result = MySQL_client.sql_Select(sql, var);
                Message msg = Message.obtain();
                msg.what = 0;
                Bundle bundle = new Bundle();
                bundle.putString("order_id", result.get("orderId"));
                bundle.putInt("has_order", Integer.parseInt(result.get("has_order")));
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 从数据库获取订单信息
     */
    protected void getOrderFromDB(String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql = String.format("SELECT * from rideorder WHERE order_id='%s'", id);
                String[] var = {"origin", "destination", "suborder1", "state", "location_from", "location_to", "distance"};
                Log.e("sql", sql);
                Map<String, String> result = MySQL_client.sql_Select(sql, var);

                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putString("origin", result.get("origin"));
                bundle.putString("destination", result.get("destination"));
                bundle.putInt("state", Integer.parseInt(result.get("state")));
                String[] super_latlng_from = result.get("location_from").split(",");
                String[] super_latlng_to = result.get("location_to").split(",");
                bundle.putString("latitude_from", super_latlng_from[0]);
                bundle.putString("longitude_from", super_latlng_from[1]);
                bundle.putString("latitude_to", super_latlng_to[0]);
                bundle.putString("longitude_to", super_latlng_to[1]);
                bundle.putString("distance", result.get("distance"));
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();
    }

    protected void findSubOrder(Order order){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String account = LoginActivity.account;
                TKRSquery tkrSquery = new TKRSquery();
                tkrSquery.initiTKRS(account, order);
                int num = 1;
                if(tkrSquery.mactiveOrderList==null){
                    Message msg = Message.obtain();
                    msg.what = 5;
                    handler.sendMessage(msg);
                    return;
                }
                for(TKRSquery.ActiveOrder activeOrder : tkrSquery.mactiveOrderList){
                    Message msg = Message.obtain();
                    msg.what = num + 1;
                    num = num + 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("order_id", activeOrder.order_id);
                    bundle.putString("address_from", activeOrder.address_from);
                    bundle.putString("address_to", activeOrder.address_to);
                    bundle.putString("distance", activeOrder.distance);
                    bundle.putString("duration", activeOrder.duration);
                    bundle.putDouble("detour", activeOrder.detour);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 分配订单更新订单状态
     * @param order_id 订单号
     */
    protected void updateOrder(String order_id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql = String.format("SELECT state from rideorder WHERE order_id='%s'", order_id);
                String var[] = {"state"};
                Map<String, String> result = MySQL_client.sql_Select(sql, var);
                if(Integer.parseInt(result.get("state"))!=0){
                    Message msg = Message.obtain();
                    msg.what = 6;
                    handler.sendMessage(msg);
                }
                String account = LoginActivity.account;
                String sql_update1 = String.format("UPDATE rideorder SET state=1, driver_uid='%s' WHERE order_id='%s'", account, order_id);
                Log.e("sql", sql_update1);
                String sql_update2 = String.format("UPDATE driver SET has_order=2 WHERE account='%s'", account);
                Log.e("sql", sql_update2);
                String sql_update3 = String.format("UPDATE rideorder SET suborder1='%s' WHERE order_id='%s'", order_id, super_order_id);
                Log.e("sql", sql_update3);
                String[] sql_execute_list = {} ;
                String[] sql_update_list = {sql_update1, sql_update2, sql_update3} ;
                MySQL_client.sql_Transaction(sql_execute_list, sql_update_list);
            }
        }).start();
    }
}