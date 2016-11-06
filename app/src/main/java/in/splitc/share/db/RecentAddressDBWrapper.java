package in.splitc.share.db;

import android.content.Context;

import java.util.ArrayList;

import in.splitc.share.data.Address;

/**
 * Created by neo on 06/11/16.
 */
public class RecentAddressDBWrapper {


    public static RecentAddressDBManager helper;

    public static void Initialize(Context context) {
        helper = new RecentAddressDBManager(context);
    }

    public static int addAddress(Address location, int userId, long timestamp) {
        return helper.addAddress(location, userId, timestamp);
    }

    public static ArrayList<Address> getAddresses(int userId) {
        return helper.getAddresses(userId);
    }

    public static int deleteAddresses(int userId) {
        return helper.deleteAddresses(userId);
    }
}
