package com.example.ridesharing.route;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ridesharing.LoginActivity;
import com.example.ridesharing.MapActivity;
import com.example.ridesharing.OrderActivity;
import com.example.ridesharing.R;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.Poi;
import com.tencent.lbssearch.object.param.DrivingParam;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.DrivingResultObject;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.tools.net.http.HttpResponseListener;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import common.Order;

public class RoutePlanActivity extends AppCompatActivity {

    private DrivingResultObject obj;
    private TencentMap mMap;
    private MapView mapView;
    private TextView pre_distance;
    private TextView pre_duration;
    private TextView pre_fare;
    private Button route_btn;

    private LatLng latLng_from;
    private LatLng latLng_to;
    private String address_from;
    private String address_to;

    private double distance;
    private double duration;
    private double fare;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 0:
                    distance = msg.getData().getDouble("distance");
                    duration = msg.getData().getDouble("duration");
                    fare = msg.getData().getDouble("fare");

                    pre_distance.append(String.valueOf(distance) + " m");
                    pre_duration.append(String.valueOf(duration) + " min");
                    pre_fare.append(String.valueOf(fare) + " RMB");

                    placeOrder();
                    break;

                case 1:
                    int has_order = msg.getData().getInt("has_order");
                    Intent intent = new Intent(RoutePlanActivity.this, OrderActivity.class);
                    if(has_order==1){
                        Toast.makeText(RoutePlanActivity.this, "您有一个订单正在进行中", Toast.LENGTH_LONG).show();
                        startActivity(intent);
                        break;
                    }
                    startActivity(intent);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan);

        route_btn = findViewById(R.id.route_btn_confirm);
        pre_distance = findViewById(R.id.pre_distance);
        pre_duration = findViewById(R.id.pre_duration);
        pre_fare = findViewById(R.id.pre_fare);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        //设置起点和终点经纬度
        latLng_from = new LatLng(bundle.getDouble("latitude_from"), bundle.getDouble("longitude_from"));
        latLng_to = new LatLng(bundle.getDouble("latitude_to"), bundle.getDouble("longitude_to"));
        address_from = bundle.getString("address_from");
        address_to = bundle.getString("address_to");

        //显示地图
        showMap();
        //获取路线
        getRoutePlan();
    }

    protected void getRoutePlan(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DrivingParam drive = new DrivingParam(latLng_from, latLng_to);
                TencentSearch tencentSearch = new TencentSearch(RoutePlanActivity.this);
                tencentSearch.getRoutePlan(drive, new HttpResponseListener<BaseObject>() {
                    @Override
                    public void onSuccess(int arg0, BaseObject arg1) {
                        if (arg1 == null) {
                            Log.w("RoutePlan", "Warning: arg1 is null");
                            return;
                        }
                        DrivingResultObject obj = (DrivingResultObject) arg1;
                        if(obj.result == null){
                            Log.w("RoutePlan", "Warning: object.result is null");
                            return;
                        }

                        //取第一条路线
                        DrivingResultObject.Route route = obj.result.routes.get(0);
                        Message msg = Message.obtain();
                        msg.what = 0;
                        Bundle bundle = new Bundle();
                        bundle.putDouble("distance", route.distance);
                        bundle.putDouble("duration", route.duration);
                        bundle.putDouble("fare", route.taxi_fare.fare);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        Log.v("RoutePlan","title:"+ route.mode + "; Distance:" + route.distance + "; Duration: " + route.duration + route.polyline);

                        List<LatLng> mCarLatLngList = route.polyline;
                        LatLng[] mCarLatLngArray;

                        mCarLatLngArray = new LatLng[mCarLatLngList.size()];
                        for (int i = 0; i < mCarLatLngArray.length; i++) {
                            double latitude = mCarLatLngList.get(i).latitude;
                            double longitude = mCarLatLngList.get(i).longitude;
                            mCarLatLngArray[i] = new LatLng(latitude, longitude);
                        }
                        mMap.addPolyline(new PolylineOptions().add(mCarLatLngArray));

                        Marker marker1 = mMap.addMarker(new MarkerOptions(latLng_from));
                        Marker marker2 = mMap.addMarker(new MarkerOptions(latLng_to));


                        LatLng carLatLng = mCarLatLngArray[0];
                        Marker mCarMarker = mMap.addMarker(
                                new MarkerOptions(carLatLng)
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.taxi))
                                        .flat(true)
                                        .clockwise(false));
                    }


                    @Override
                    public void onFailure(int arg0, String arg2, Throwable arg3) {
                        Toast.makeText(getApplicationContext(), arg2, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void showMap(){
        mapView = findViewById(R.id.mapview);
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                new LatLng((latLng_from.latitude + latLng_to.latitude)/2,(latLng_from.longitude + latLng_to.longitude)/2), //中心点坐标，地图目标经纬度
                14,  //目标缩放级别
                0, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0)); //目标旋转角 0~360° (正北方为0)
        mMap = mapView.getMap();
        mMap.moveCamera(cameraSigma);
    }

    /**
     * 下订单
     */
    private void placeOrder(){
        String account = LoginActivity.account;

        route_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Time t=new Time();
                t.setToNow();
                Order order = new Order();
                order.order_id = "" + t.year + (t.month+1) + t.monthDay + t.hour + t.minute + t.second + account;
                order.origin_location = String.valueOf(latLng_from.latitude) + "," + String.valueOf(latLng_from.longitude);
                order.destination_location = String.valueOf(latLng_to.latitude) + "," + String.valueOf(latLng_to.longitude);
                order.origin_address = address_from;
                order.destination_address = address_to;
                order.fee = fare;
                order.distance = distance;
                order.duration = duration;
                order.user_uid = account;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        if(order.checkUserState()==0){
                            order.addOrder();
                            bundle.putInt("has_order", 0);
                        }else if(order.checkUserState()==1){
                            bundle.putInt("has_order", 1);
                        }
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });
    }

    /**
     * mapview的生命周期管理
     */
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onRestart();
    }
}