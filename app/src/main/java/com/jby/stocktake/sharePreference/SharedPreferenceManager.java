package com.jby.stocktake.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {


    private static String LanguageId = "language_id";
    private static String UserID = "uid";
    private static String Version = "version";
    private static String UserPassword = "userPassword";
    private static String RememberMe = "rememberMe";
    private static String RememberModule = "rememberModule";
    private static String QuickScan = "quickScan";
    private static String QuickScanQuantity = "quickScanQuantity";
    private static String Reminder = "reminder";
    private static String ScanSound = "scanSound";
    private static String DeviceToken = "deviceToken";
    private static String DeviceName = "deviceName";
    private static String UserPackage = "userPackage";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "MJTaiwanUserLoginSessionDetail";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context){
        getSharedPreferences(context).edit().clear().apply();
    }

    public static boolean stayLogin(Context context){
        return getRememberMe(context).equals("1");
    }

    /*
    *       User Shared Preference
    * */

    public static String getLanguageId(Context context) {
        return getSharedPreferences(context).getString(LanguageId, "default");
    }

    public static void setLanguageId(Context context, String languageId) {
        getSharedPreferences(context).edit().putString(LanguageId, languageId).apply();
    }

    public static String getUserID(Context context) {
        return getSharedPreferences(context).getString(UserID, "default");
    }

    public static void setUserID(Context context, String userID) {
        getSharedPreferences(context).edit().putString(UserID, userID).apply();
    }

    public static String getVersion(Context context) {
        return getSharedPreferences(context).getString(Version, "1.2.1");
    }

    public static void setVersion(Context context, String version) {
        getSharedPreferences(context).edit().putString(Version, version).apply();
    }

    public static String getDeviceToken(Context context) {
        return getSharedPreferences(context).getString(DeviceToken, "default");
    }

    public static void setDeviceToken(Context context, String deviceToken) {
        getSharedPreferences(context).edit().putString(DeviceToken, deviceToken).apply();
    }

    public static String getUserPassword(Context context) {
        return getSharedPreferences(context).getString(UserPassword, "default");
    }

    public static void setUserPassword(Context context, String userPassword) {
        getSharedPreferences(context).edit().putString(UserPassword, userPassword).apply();
    }

    private static String getRememberMe(Context context) {
        return getSharedPreferences(context).getString(RememberMe, "default");
    }

    public static void setRememberMe(Context context, String rememberMe) {
        getSharedPreferences(context).edit().putString(RememberMe, rememberMe).apply();
    }

    public static String getRememberModule(Context context) {
        return getSharedPreferences(context).getString(RememberModule, "ec");
    }

    public static void setRememberModule(Context context, String rememberModule) {
        getSharedPreferences(context).edit().putString(RememberModule, rememberModule).apply();
    }

    public static String getQuickScan(Context context) {
        return getSharedPreferences(context).getString(QuickScan, "0");
    }

    public static void setQuickScan(Context context, String quickScan) {
        getSharedPreferences(context).edit().putString(QuickScan, quickScan).apply();
    }

    public static String getReminder(Context context) {
        return getSharedPreferences(context).getString(Reminder, "0");
    }

    public static void setReminder(Context context, String reminder) {
        getSharedPreferences(context).edit().putString(Reminder, reminder).apply();
    }

    public static String getScanSound(Context context) {
        return getSharedPreferences(context).getString(ScanSound, "0");
    }

    public static void setScanSound(Context context, String scanSound) {
        getSharedPreferences(context).edit().putString(ScanSound, scanSound).apply();
    }

    public static String getQuickScanQuantity(Context context) {
        return getSharedPreferences(context).getString(QuickScanQuantity, "0");
    }

    public static void setQuickScanQuantity(Context context, String quickScanQuantity) {
        getSharedPreferences(context).edit().putString(QuickScanQuantity, quickScanQuantity).apply();
    }

    public static String getDeviceName(Context context) {
        return getSharedPreferences(context).getString(DeviceName, "");
    }

    public static void setDeviceName(Context context, String deviceName) {
        getSharedPreferences(context).edit().putString(DeviceName, deviceName).apply();
    }


    public static int getUserPackage(Context context) {
        return getSharedPreferences(context).getInt(UserPackage, 0);
    }

    public static void setUserPackage(Context context, int userPackage) {
        getSharedPreferences(context).edit().putInt(UserPackage, userPackage).apply();
    }

}
