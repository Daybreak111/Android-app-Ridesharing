package common;

import android.annotation.SuppressLint;
import android.util.Log;

import java.sql.SQLException;

public class Order {

    public String order_id;
    public String user_uid;
    public String driver_uid;
    public String origin_address;
    public String destination_address;
    public String origin_location;
    public String destination_location;
    public double distance;
    public double duration;
    public String suborder1;
    public String suborder2;
    public String suborder3;
    public double fee;

    @SuppressLint("DefaultLocale")
    public void addOrder() {
        String sql_execute = String.format("INSERT INTO rideorder (order_id, user_uid, driver_uid, origin, destination, location_from, location_to, fee, distance, duration, state) VALUES" +
                "('%s', '%s', '%s', '%s', '%s', '%s', '%s', %f, %f, %f, 0);", order_id, user_uid, driver_uid, origin_address, destination_address, origin_location, destination_location, fee, distance, duration);
        String sql_update = String.format("Update user SET has_order=1, orderid='%s' WHERE account='%s';", order_id, user_uid);
        Log.e("SQL", sql_execute);
        Log.e("SQL", sql_update);
        String[] sql_execute_list = {sql_execute} ;
        String[] sql_update_list = {sql_update} ;
        MySQL_client.sql_Transaction(sql_execute_list, sql_update_list);
    }

    public void cancelOrder() {
        String sql_update1 = String.format("Update user SET has_order=0, orderid='' WHERE account='%s';", user_uid);
        String sql_update2 = String.format("Update rideorder SET state=4 WHERE order_id='%s';", order_id);
        Log.e("SQL", sql_update1);
        Log.e("SQL", sql_update2);
        String[] sql_execute_list = {} ;
        String[] sql_update_list = {sql_update1, sql_update2} ;
        MySQL_client.sql_Transaction(sql_execute_list, sql_update_list);
    }
}
