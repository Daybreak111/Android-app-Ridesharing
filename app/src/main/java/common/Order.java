package common;

import android.annotation.SuppressLint;

public class Order {

    public String order_id;
    public String user_uid;
    public String driver_uid;
    public String origin_address;
    public String destination_address;
    public double distance;
    public double duration;
    public String suborder1;
    public String suborder2;
    public String suborder3;
    public double fee;

    public void addOrder(){
        @SuppressLint("DefaultLocale") String sql = String.format("INSERT INTO rideorder (order_id, user_uid, origin, destination, fee, distance, duration, state) VALUES" +
                "('%s', '%s', '%s', '%s', %f, %f, %f, 0)", order_id, user_uid, origin_address, destination_address, fee, distance, duration);
    }
}
