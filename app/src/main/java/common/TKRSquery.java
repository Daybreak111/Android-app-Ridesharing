package common;

import android.util.Log;

import com.example.ridesharing.route.RoutePlanActivity;
import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.object.param.DrivingParam;
import com.tencent.lbssearch.object.result.DrivingResultObject;
import com.tencent.map.tools.net.http.HttpResponseListener;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TKRSquery {

    public String driver_account;
    public List<ActiveOrder> mactiveOrderList;
    final String key = "4GDBZ-NGOL4-VB6UP-XO3YY-XGYVF-XYFAB";

    public class ActiveOrder{
        public String order_id;
        public String address_from;
        public String address_to;
        public String duration;
        public String distance;
        public LatLng latlng_from;
        public LatLng latlng_to;
        public String fare;
        public double detour;
    }

    public void initiTKRS(String driver_account, Order order){
        double distance_origin = order.distance;
        List<ActiveOrder> activeOrderList = getActiveOrderList();
        mactiveOrderList = new ArrayList<ActiveOrder>();
        for(ActiveOrder activeOrder: activeOrderList){
            if(mactiveOrderList.size()<3){
                double distance_final = getDistance(order, activeOrder);
                Log.e("Distance", String.valueOf(distance_final));
                double scaler = distance_origin / distance_final;
                Log.e("scaler", String.valueOf(scaler));
                activeOrder.detour = scaler;
                mactiveOrderList.add(activeOrder);
                Collections.sort(mactiveOrderList, new ActiveOrderComparetor());
            }else{
                double distance_final = getDistance(order, activeOrder);
                double scaler = distance_origin / distance_final;
                if(scaler>mactiveOrderList.get(2).detour){
                    mactiveOrderList.remove(2);
                    mactiveOrderList.add(activeOrder);
                    Collections.sort(mactiveOrderList, new ActiveOrderComparetor());
                }
            }
        }

    }

    public double getDistance(Order order, ActiveOrder activeOrder){
        String[] origin_location_from = order.origin_location.split(",");
        String[] origin_location_to = order.destination_location.split(",");
        LatLng origin_latLng_from = new LatLng(Double.parseDouble(origin_location_from[0]), Double.parseDouble(origin_location_from[1]));
        LatLng origin_latLng_to = new LatLng(Double.parseDouble(origin_location_to[0]), Double.parseDouble(origin_location_to[1]));
        String result = "";
        //拼接URL请求,GET
        String url = "https://apis.map.qq.com/ws/direction/v1/driving/?";
        url = url + "key=" + key;
        url = url + "&from=" + origin_latLng_from.latitude + "," + origin_latLng_from.longitude;
        url = url + "&to=" + origin_latLng_to.latitude + "," + origin_latLng_to.longitude;
        url = url + "&waypoints=" + activeOrder.latlng_from.latitude + "," + activeOrder.latlng_from.longitude + ";" +
                activeOrder.latlng_to.latitude + "," + activeOrder.latlng_to.longitude;
        //发送请求
        System.out.println(url);
        result = Httpclient.doGet(url);
        System.out.println(result);

        Utility u = new Utility();
        List<String> routelist = u.getArrayParam(u.getStringParam(result, "result"), "routes");
        String route = routelist.get(0);
        return Double.parseDouble(u.getStringParam(route, "distance"));
    }

    public double getDistance(LatLng from, LatLng to){
        String result = "";
        //拼接URL请求,GET
        String url = "https://apis.map.qq.com/ws/direction/v1/driving/?";
        url = url + "key=" + key;
        url = url + "&from=" + from.latitude + "," + from.longitude;
        url = url + "&to=" + to.latitude + "," + to.longitude;
        //发送请求
        System.out.println(url);
        result = Httpclient.doGet(url);
        System.out.println(result);

        Utility u = new Utility();
        List<String> routelist = u.getArrayParam(u.getStringParam(result, "result"), "routes");
        String route = routelist.get(0);
        return Double.parseDouble(u.getStringParam(route, "distance"));
    }

    public List<ActiveOrder> getActiveOrderList(){
        String sql = "SELECT * from rideorder WHERE state=0";
        String[] var = {"order_id", "driver_uid", "origin", "destination", "location_from", "location_to", "state", "suborder1", "suborder2", "distance", "duration", "fee" };
        List<Map<String, String>> results = MySQL_client.sql_Select(sql, var, false);
        List<ActiveOrder> activeOrderList = new ArrayList<ActiveOrder>();
        for(Map<String, String> result:results){
            ActiveOrder activeOrder = initActiveOrder(result);
            activeOrderList.add(activeOrder);
        }
        return activeOrderList;
    }

    public ActiveOrder initActiveOrder(Map<String, String> order_info){
        ActiveOrder activeOrder = new ActiveOrder();
        activeOrder.order_id = order_info.get("order_id");
        activeOrder.address_from = order_info.get("origin");
        activeOrder.address_to = order_info.get("destination");
        String[] origin_location_from = order_info.get("location_from").split(",");
        String[] origin_location_to = order_info.get("location_to").split(",");
        activeOrder.latlng_from = new LatLng(Double.parseDouble(origin_location_from[0]), Double.parseDouble(origin_location_from[1]));
        activeOrder.latlng_to = new LatLng(Double.parseDouble(origin_location_to[0]), Double.parseDouble(origin_location_to[1]));
        activeOrder.duration = order_info.get("duration");
        activeOrder.distance = order_info.get("distance");
        activeOrder.fare = order_info.get("fare");
        return activeOrder;
    }

    public class ActiveOrderComparetor implements Comparator<ActiveOrder> {
        @Override
        public int compare(ActiveOrder o1, ActiveOrder o2) {
            if (o1.detour > o2.detour) {
                return 1;
            } else if (o1.detour == o2.detour) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
