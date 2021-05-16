package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharing.route.RoutePlanActivity;
import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.object.param.DrivingParam;
import com.tencent.lbssearch.object.result.DrivingResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import common.MySQL_client;
import common.TKRSquery;

public class DriverGetOrderActivity extends AppCompatActivity {
    private static final String TAG = DriverGetOrderActivity.class.getSimpleName();
    private MapView mapView;
    protected TencentMap mtencentMap;

    private LatLng mlatlng;
    private String city;
    private LatLng latLng_from;
    private LatLng latLng_to;
    private DriverGetOrderActivity.InnerLocationListener mLocationListener;

    private String super_order_id;
    private String super_address_from;
    private String super_address_to;
    private int super_state;
    private String sub_order_id;
    private String sub_address_from;
    private String sub_address_to;
    private int sub_state;

    private TextView tv_super_location_from;
    private TextView tv_super_location_to;
    private TextView tv_sub_location_from;
    private TextView tv_sub_location_to;
    private LinearLayout sub_order_get;
    private LinearLayout sub_order;
    private Button super_map_btn_confirm;
    private Button sub_map_btn_confirm;
    private Button btn_get_suborder;


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    double latitude = msg.getData().getDouble("latitude");
                    double longitude = msg.getData().getDouble("longitude");
                    Log.e("Location", String.valueOf(latitude) + String.valueOf(longitude));
                    city = msg.getData().getString("city");
                    mlatlng = new LatLng(latitude, longitude);
                    //根据第一次定位结果显示地图
                    showMap(latitude,longitude);
                    //获取订单
                    getSuperOrder();
                    break;
                case 1:
                    Toast.makeText(DriverGetOrderActivity.this, "没有可用订单，稍后再试", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    latLng_from = new LatLng(msg.getData().getDouble("latitude_from"), msg.getData().getDouble("longitude_from"));
                    latLng_to = new LatLng(msg.getData().getDouble("latitude_to"), msg.getData().getDouble("longitude_to"));
                    super_order_id = msg.getData().getString("order_id");
                    //显示地图
                    showMap((mlatlng.latitude+latLng_to.latitude)/2, (mlatlng.longitude+latLng_to.longitude)/2);
                    LatLng[] latLngs = {latLng_from};
                    getRoutePlan(latLngs);
                    getOrderFromDB(super_order_id);
                    break;
                case 3:
                    int driver_state = msg.getData().getInt("has_order");
                    super_order_id = msg.getData().getString("order_id");
                    getOrderFromDB(super_order_id);
                    break;
                case 4:
                    latLng_from = new LatLng(msg.getData().getDouble("latitude_from"), msg.getData().getDouble("longitude_from"));
                    latLng_to = new LatLng(msg.getData().getDouble("latitude_to"), msg.getData().getDouble("longitude_to"));
                    //显示地图
                    showMap((mlatlng.latitude+latLng_to.latitude)/2, (mlatlng.longitude+latLng_to.longitude)/2);
                    LatLng[] super_latLngs = {latLng_from};
                    getRoutePlan(super_latLngs);

                    super_address_from = msg.getData().getString("origin");
                    super_address_to = msg.getData().getString("destination");
                    super_state = msg.getData().getInt("state");
                    setSuperUI();
                    break;
                case 5:
                    LatLng sub_latLng_from = new LatLng(msg.getData().getDouble("latitude_from"), msg.getData().getDouble("longitude_from"));
                    LatLng sub_latLng_to = new LatLng(msg.getData().getDouble("latitude_to"), msg.getData().getDouble("longitude_to"));
                    LatLng[] sub_latLngs = {latLng_from, sub_latLng_from, sub_latLng_to};
                    getRoutePlan(sub_latLngs);

                    sub_order_id = msg.getData().getString("order_id");
                    sub_address_from = msg.getData().getString("origin");
                    sub_address_to = msg.getData().getString("destination");
                    sub_state = msg.getData().getInt("state");
                    setSubUI();
                    break;
                case 6:
                    AlertDialog alertDialog = new AlertDialog.Builder(DriverGetOrderActivity.this)
                            .setMessage("订单已经全部完成，返回主界面")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(DriverGetOrderActivity.this, DriverHomeActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .create();
                    alertDialog.show();
                    break;
                case 7:
                    releaseDriver();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_get_order);

        sub_order_get = findViewById(R.id.sub_order_get);
        sub_order = findViewById(R.id.sub_order);
        btn_get_suborder = findViewById(R.id.btn_get_suborder);
        mapView = findViewById(R.id.drive_map);
        mtencentMap = mapView.getMap();

        //第一次进入时定位
        singleLocation();
    }

    protected void getSuperOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> checkDriverState = checkDriverState();
                if(Integer.parseInt(checkDriverState.get("has_order"))==0){
                    //没有订单自动分配订单
                    TKRSquery tkrSquery = new TKRSquery();
                    double min_distance = Double.MAX_VALUE;
                    TKRSquery.ActiveOrder min_activeorder = null;
                    List<TKRSquery.ActiveOrder> activeOrderList = tkrSquery.getActiveOrderList();
                    for(TKRSquery.ActiveOrder activeOrder : activeOrderList){
                        double distance = tkrSquery.getDistance(mlatlng, activeOrder.latlng_from);
                        if(distance < min_distance){
                            min_distance = distance;
                            min_activeorder = activeOrder;
                        }
                    }
                    if(min_activeorder==null){
                        Message msg = Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                        return;
                    }

                    updateOrder(min_activeorder.order_id);
                    Message msg = Message.obtain();
                    msg.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("order_id", min_activeorder.order_id);
                    bundle.putDouble("latitude_from", min_activeorder.latlng_from.latitude);
                    bundle.putDouble("longitude_from", min_activeorder.latlng_from.longitude);
                    bundle.putDouble("latitude_to", min_activeorder.latlng_to.latitude);
                    bundle.putDouble("longitude_to", min_activeorder.latlng_to.longitude);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }else if(Integer.parseInt(checkDriverState.get("has_order"))>0){
                    //已有订单自动载入订单
                    Message msg = Message.obtain();
                    msg.what = 3;
                    Bundle bundle = new Bundle();
                    bundle.putInt("has_order", Integer.parseInt(checkDriverState.get("has_order")));
                    bundle.putString("order_id", checkDriverState.get(("orderId")));
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
        String account = LoginActivity.account;
        String sql_update1 = String.format("UPDATE rideorder SET state=1, driver_uid='%s' WHERE order_id='%s'", account, order_id);
        Log.e("sql", sql_update1);
        String sql_update2 = String.format("UPDATE driver SET has_order=1, orderId='%s' WHERE account='%s'", order_id, account);
        Log.e("sql", sql_update2);
        String[] sql_execute_list = {} ;
        String[] sql_update_list = {sql_update1, sql_update2} ;
        MySQL_client.sql_Transaction(sql_execute_list, sql_update_list);
    }

    /**
     * 结束订单
     */
    protected void finishOrder(String order_id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String account = LoginActivity.account;
                String sql_update1 = String.format("UPDATE rideorder SET state=2 WHERE order_id='%s'", order_id);
                Log.e("sql", sql_update1);
                String sql_update2 = String.format("UPDATE driver SET has_order=3 WHERE account='%s'", account);
                Log.e("sql", sql_update2);
                String sql_update3 = String.format("UPDATE user SET has_order=0 WHERE orderid='%s'", order_id);
                Log.e("sql", sql_update3);
                String[] sql_execute_list = {} ;
                String[] sql_update_list = {sql_update1, sql_update2, sql_update3} ;
                MySQL_client.sql_Transaction(sql_execute_list, sql_update_list);

                Message msg = Message.obtain();
                msg.what = 7;
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     *检查司机状态
     * @return 司机信息：订单号，接单数
     */
    protected Map<String, String> checkDriverState(){
        String account = LoginActivity.account;
        String sql = String.format("SELECT orderId, has_order From driver WHERE account='%s'",account);
        Log.e("sql", sql);
        String[] var = {"orderId", "has_order"};
        Map<String, String> result = MySQL_client.sql_Select(sql, var);
        return result;
    }

    /**
     * 从数据库获取订单信息
     */
    protected void getOrderFromDB(String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql = String.format("SELECT * from rideorder WHERE order_id='%s'", id);
                String[] var = {"origin", "destination", "suborder1", "state", "location_from", "location_to"};
                Log.e("sql", sql);
                Map<String, String> result = MySQL_client.sql_Select(sql, var);

                Message msg = Message.obtain();
                msg.what = 4;
                Bundle bundle = new Bundle();
                bundle.putString("origin", result.get("origin"));
                bundle.putString("destination", result.get("destination"));
                bundle.putInt("state", Integer.parseInt(result.get("state")));
                String[] super_latlng_from = result.get("location_from").split(",");
                String[] super_latlng_to = result.get("location_to").split(",");
                bundle.putDouble("latitude_from", Double.parseDouble(super_latlng_from[0]));
                bundle.putDouble("longitude_from", Double.parseDouble(super_latlng_from[1]));
                bundle.putDouble("latitude_to", Double.parseDouble(super_latlng_to[0]));
                bundle.putDouble("longitude_to", Double.parseDouble(super_latlng_to[1]));
                msg.setData(bundle);
                handler.sendMessage(msg);

                if(result.get("suborder1")!=null){
                    String sub_sql = String.format("SELECT * from rideorder WHERE order_id='%s'", result.get("suborder1"));
                    Log.e("Sub_sql", sub_sql);
                    Map<String, String> sub_result = MySQL_client.sql_Select(sub_sql, var);

                    Message message = Message.obtain();
                    message.what = 5;
                    Bundle subbundle = new Bundle();
                    subbundle.putString("order_id", result.get("suborder1"));
                    subbundle.putString("origin", sub_result.get("origin"));
                    subbundle.putString("destination", sub_result.get("destination"));
                    subbundle.putInt("state", Integer.parseInt(sub_result.get("state")));
                    String[] sub_latlng_from = result.get("location_from").split(",");
                    String[] sub_latlng_to = result.get("location_to").split(",");
                    subbundle.putDouble("latitude_from", Double.parseDouble(sub_latlng_from[0]));
                    subbundle.putDouble("longitude_from", Double.parseDouble(sub_latlng_from[1]));
                    subbundle.putDouble("latitude_to", Double.parseDouble(sub_latlng_to[0]));
                    subbundle.putDouble("longitude_to", Double.parseDouble(sub_latlng_to[1]));
                    message.setData(subbundle);
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 释放司机
     */
    protected void releaseDriver(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String account = LoginActivity.account;
                String[] var = {"state"};

                if(sub_order_id!=null){
                    String sql_a = String.format("SELECT state from rideorder WHERE order_id='%s'", sub_order_id);
                    Log.e("sql", sql_a);
                    Map<String, String> sub_result = MySQL_client.sql_Select(sql_a, var);
                    if(Integer.parseInt(sub_result.get("state"))!=2) {
                        return;
                    }
                }

                String sql_b = String.format("SELECT state from rideorder WHERE order_id='%s'", super_order_id);
                Log.e("sql", sql_b);
                Map<String, String> super_result = MySQL_client.sql_Select(sql_b, var);
                if(Integer.parseInt(super_result.get("state"))!=2) {
                    return;
                }

                String sql_c = String.format("Update driver SET has_order=0 WHERE account='%s'", account);
                Log.e("sql", sql_c);
                String[] sql_execute_list = {} ;
                String[] sql_update_list = {sql_c} ;
                MySQL_client.sql_Transaction(sql_execute_list, sql_update_list);

                Message msg = Message.obtain();
                msg.what = 6;
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 展示只有大订单的UI
     */
    protected void setSuperUI(){
        tv_super_location_from = findViewById(R.id.super_location_from);
        tv_super_location_to = findViewById(R.id.super_location_to);
        super_map_btn_confirm = findViewById(R.id.super_map_btn_confirm);

        tv_super_location_from.append(super_address_from);
        tv_super_location_to.append(super_address_to);

        sub_order_get.setVisibility(View.VISIBLE);
        btn_get_suborder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverGetOrderActivity.this, SubOrderActivity.class);
                startActivity(intent);
            }
        });

        if(super_state==1){
            super_map_btn_confirm.setVisibility(View.VISIBLE);
            super_map_btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //结束订单
                    finishOrder(super_order_id);
                    super_map_btn_confirm.setEnabled(false);
                }
            });
        }
    }

    /**
     * 展示只有子订单的UI
     */
    protected void setSubUI(){
        sub_order.setVisibility(View.VISIBLE);
        tv_sub_location_from = findViewById(R.id.sub_location_from);
        tv_sub_location_to = findViewById(R.id.sub_location_to);
        sub_map_btn_confirm = findViewById(R.id.sub_map_btn_confirm);

        tv_sub_location_from.append(sub_address_from);
        tv_sub_location_to.append(sub_address_to);
        if(sub_state==1){
            sub_order_get.setVisibility(View.INVISIBLE);
            sub_map_btn_confirm.setVisibility(View.VISIBLE);
            sub_map_btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //结束订单
                    finishOrder(sub_order_id);
                    sub_map_btn_confirm.setEnabled(false);
                }
            });
        }
    }

    /**
     * 单次定位
     */
    private void singleLocation(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mLocationListener = new InnerLocationListener(new WeakReference<DriverGetOrderActivity>(DriverGetOrderActivity.this));
                TencentLocationManager mLocationManager = TencentLocationManager.getInstance(DriverGetOrderActivity.this);
                mLocationManager.requestSingleFreshLocation(null, mLocationListener, Looper.getMainLooper());
            }
        }).start();
    }

    private void showMap(double latitude, double longitude){
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                new LatLng(latitude,longitude), //中心点坐标，地图目标经纬度
                12,  //目标缩放级别
                0, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0)); //目标旋转角 0~360° (正北方为0)
        mtencentMap.moveCamera(cameraSigma);
    }

    /**
     * 设置监听器
     */
    private class InnerLocationListener implements TencentLocationListener {
        private WeakReference<DriverGetOrderActivity> mMainActivityWRF;

        public InnerLocationListener(WeakReference<DriverGetOrderActivity> mainActivityWRF) {
            mMainActivityWRF = mainActivityWRF;
        }

        @Override
        public void onLocationChanged(TencentLocation location, int error, String reason) {
            if (mMainActivityWRF != null) {
                DriverGetOrderActivity mainActivity = mMainActivityWRF.get();
                if (mainActivity != null) {
//                  double latitude = location.getLatitude();
//                  double longitude = location.getLongitude();
//                  String geo = location.getCity();
                    String geo = "上海";
                    double latitude = 31.18334;
                    double longitude = 121.43348;

                    Message msg = Message.obtain();
                    msg.what = 0;
                    Bundle bundle = new Bundle();
                    bundle.putDouble("latitude", latitude);
                    bundle.putDouble("longitude", longitude);
                    bundle.putString("city", geo);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }

        @Override
        public void onStatusUpdate(String name, int status, String desc) {
            Log.i(TAG, "name: " + name + "status: " + status + "desc: " + desc);
        }
    }

    protected void getRoutePlan(LatLng[] latLngs){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DrivingParam drive = new DrivingParam(mlatlng, latLng_to);
                for(int i = 0; i < latLngs.length; i++){
                    drive.addWayPoint(latLngs[i]);
                    Log.e("Tag", latLngs[i].toString());
                }
                Log.e("URL", drive.getUrl());
                TencentSearch tencentSearch = new TencentSearch(DriverGetOrderActivity.this);
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
                        Log.e("RoutePlan","title:"+ route.mode + "; Distance:" + route.distance + "; Duration: " + route.duration + route.polyline);

                        List<LatLng> mCarLatLngList = route.polyline;
                        LatLng[] mCarLatLngArray;
                        Log.e("attention", route.waypoints.toString());

                        mCarLatLngArray = new LatLng[mCarLatLngList.size()];
                        for (int i = 0; i < mCarLatLngArray.length; i++) {
                            double latitude = mCarLatLngList.get(i).latitude;
                            double longitude = mCarLatLngList.get(i).longitude;
                            mCarLatLngArray[i] = new LatLng(latitude, longitude);
                        }
                        mtencentMap.addPolyline(new PolylineOptions().add(mCarLatLngArray));

                        LatLng carLatLng = mCarLatLngArray[0];
                        Marker mCarMarker = mtencentMap.addMarker(
                                new MarkerOptions(carLatLng)
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.taxi))
                                        .flat(true)
                                        .clockwise(false));

                        Marker marker1 = mtencentMap.addMarker(new MarkerOptions(mlatlng));
                        Marker marker2 = mtencentMap.addMarker(new MarkerOptions(latLng_to));
                        for(LatLng latLng : latLngs){
                            Marker marker = mtencentMap.addMarker(new MarkerOptions(latLng));
                        }

                        Message msg = Message.obtain();
                        msg.what = 8;
                        Bundle bundle = new Bundle();
                        bundle.putDouble("distance", route.distance);
                        bundle.putDouble("duration", route.duration);
                        bundle.putDouble("fare", route.taxi_fare.fare);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }


                    @Override
                    public void onFailure(int arg0, String arg2, Throwable arg3) {
                        Toast.makeText(getApplicationContext(), arg2, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
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