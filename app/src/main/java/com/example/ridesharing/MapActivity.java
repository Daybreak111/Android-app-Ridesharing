package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharing.route.RoutePlanActivity;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.httpresponse.Poi;
import com.tencent.lbssearch.object.param.Address2GeoParam;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Address2GeoResultObject;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = MapActivity.class.getSimpleName();

    private MapView mapView;
    private Button mBtn_confirm;

    protected TencentMap tencentMap;
    private TextView location_from;
    private TextView location_to;
    private String address_from;
    private String address_to;
    private InnerLocationListener mLocationListener;
    private Bundle mbundle = new Bundle();

    private String address;
    private String city;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    double latitude = msg.getData().getDouble("latitude");
                    double longitude = msg.getData().getDouble("longitude");
                    city = msg.getData().getString("city");
                    //根据第一次定位结果显示地图
                    showMap(latitude, longitude);
                    //解析地址
                    geo2address(latitude, longitude);
                    break;
                case 1:
                    address = msg.getData().getString("address");
                    location_from.setText(address);
                    getRoutePlan();
                    break;
                case 2:
                    double latitude_from = msg.getData().getDouble("latitude_from");
                    double longitude_from = msg.getData().getDouble("longitude_from");
                    if(latitude_from==0 || longitude_from==0){
                        Toast.makeText(MapActivity.this, "Address from is invalid", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    mbundle.putDouble("latitude_from", latitude_from);
                    mbundle.putDouble("longitude_from", longitude_from);
                    mbundle.putString("address_from", address_from);
                    checkAddress(address_to, AddressType.TO);
                    break;
                case 3:
                    double latitude_to = msg.getData().getDouble("latitude_to");
                    double longitude_to = msg.getData().getDouble("longitude_to");
                    if(latitude_to==0 || longitude_to==0){
                        Log.e("test", String.valueOf(latitude_to) + String.valueOf(longitude_to));
                        Toast.makeText(MapActivity.this, "Address to is invalid", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    Intent intent = new Intent(MapActivity.this, RoutePlanActivity.class);
                    mbundle.putDouble("latitude_to", latitude_to);
                    mbundle.putDouble("longitude_to", longitude_to);
                    mbundle.putString("address_to", address_to);
                    intent.putExtra("bundle", mbundle);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mBtn_confirm = findViewById(R.id.map_btn_confirm);
        mapView = findViewById(R.id.mapview);
        tencentMap = mapView.getMap();

        //第一次进入时定位
        location_from = findViewById(R.id.location_from);
        location_to = findViewById(R.id.location_to);
        singleLocation();

        //隐藏软键盘
        setupUI(mapView);

    }

    /**
    * 单次定位
     */
    private void singleLocation(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mLocationListener = new InnerLocationListener(new WeakReference<MapActivity>(MapActivity.this));
                TencentLocationManager mLocationManager = TencentLocationManager.getInstance(MapActivity.this);
                mLocationManager.requestSingleFreshLocation(null, mLocationListener, Looper.getMainLooper());
            }
        }).start();
    }

    private void showMap(double latitude, double longitude){
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                new LatLng(latitude,longitude), //中心点坐标，地图目标经纬度
                16,  //目标缩放级别
                0, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0)); //目标旋转角 0~360° (正北方为0)
        tencentMap.moveCamera(cameraSigma);
    }

    /**
     * 设置监听器
     */
    private class InnerLocationListener implements TencentLocationListener {
        private WeakReference<MapActivity> mMainActivityWRF;

        public InnerLocationListener(WeakReference<MapActivity> mainActivityWRF) {
            mMainActivityWRF = mainActivityWRF;
        }

        @Override
        public void onLocationChanged(TencentLocation location, int error, String reason) {
            if (mMainActivityWRF != null) {
                MapActivity mainActivity = mMainActivityWRF.get();
                if (mainActivity != null) {
//                  double latitude = location.getLatitude();
//                  double longitude = location.getLongitude();
//                  String geo = location.getCity();
                    String geo = "上海";
                    double latitude = 31.149860448696764;
                    double longitude = 121.33877429456082;

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

    /**
     * 逆地址解析
     */
    protected void geo2address(double latitude, double longitude){
        TencentSearch tencentSearch = new TencentSearch(MapActivity.this);
        LatLng latLng = new LatLng(latitude, longitude);
        Geo2AddressParam geo2AddressParam = new Geo2AddressParam(latLng).getPoi(true)
                        .setPoiOptions(new Geo2AddressParam.PoiOptions()
                        .setRadius(1000).setPolicy(Geo2AddressParam.PoiOptions.POLICY_DEFAULT));
        tencentSearch.geo2address(geo2AddressParam, new HttpResponseListener<BaseObject>() {

            @Override
            public void onSuccess(int arg0, BaseObject arg1) {
                if (arg1 == null) {
                    return;
                }
                Geo2AddressResultObject obj = (Geo2AddressResultObject)arg1;
                StringBuilder sb = new StringBuilder();
                sb.append("逆地址解析");
                sb.append("\n地址：" + obj.result.address);
                sb.append("\npois:");
                for (Poi poi : obj.result.pois) {
                    sb.append("\n\t" + poi.title);
                    tencentMap.addMarker(new MarkerOptions()
                            .position(poi.latLng)  //标注的位置
                            .title(poi.title)     //标注的InfoWindow的标题
                            .snippet(poi.address) //标注的InfoWindow的内容
                    );
                }
                Log.e("test", sb.toString());

                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putString("address", obj.result.address);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(int arg0, String arg1, Throwable arg2) {
                Log.e("test", "error code:" + arg0 + ", msg:" + arg1);
            }
        });
    }

    /**
     *地理编码
     */
    protected void geoCoder(String address, String city, AddressType addressType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TencentSearch tencentSearch = new TencentSearch(MapActivity.this);
                Address2GeoParam address2GeoParam = new Address2GeoParam(address).region(city);
                tencentSearch.address2geo(address2GeoParam, new HttpResponseListener<BaseObject>() {

                    @Override
                    public void onSuccess(int arg0, BaseObject arg1) {
                        if (arg1 == null) {
                            return;
                        }
                        Address2GeoResultObject obj = (Address2GeoResultObject)arg1;
                        StringBuilder sb = new StringBuilder();
                        sb.append("地址解析");
                        if (obj.result.latLng != null) {
                            sb.append("\n坐标：" + obj.result.latLng.toString());
                        } else {
                            sb.append("\n无坐标");
                        }
                        Log.e("test", sb.toString());

                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        if (addressType == AddressType.FROM){
                            msg.what = 2;
                            bundle.putDouble("latitude_from", obj.result.latLng.latitude);
                            bundle.putDouble("longitude_from", obj.result.latLng.longitude);
                        }else if(addressType == AddressType.TO){
                            msg.what = 3;
                            bundle.putDouble("latitude_to", obj.result.latLng.latitude);
                            bundle.putDouble("longitude_to", obj.result.latLng.longitude);
                        }
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1, Throwable arg2) {
                        Log.e("test", "error code:" + arg0 + ", msg:" + arg1);
                        Message msg = Message.obtain();
                        if (addressType == AddressType.FROM){
                            msg.what = 2;
                        }else if(addressType == AddressType.TO){
                            msg.what = 3;
                        }
                        Bundle bundle = new Bundle();
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                });
            }
        }).start();
    }

    /**
     * 路线规划和跳转
     */
    private void getRoutePlan(){
        mBtn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address_from = location_from.getText().toString().trim();
                address_to = location_to.getText().toString().trim();
                checkAddress(address_from, AddressType.FROM);
            }
        });
    }

    private void checkAddress(String address, AddressType addressType){
        geoCoder(address, city, addressType);
    }

    /**
     * 关闭软键盘
     */
    public static void closeSoftKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager)context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && ((Activity)context).getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(((Activity)context).getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    closeSoftKeyboard(MapActivity.this);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
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

    private enum AddressType{
        FROM, TO
    }
}