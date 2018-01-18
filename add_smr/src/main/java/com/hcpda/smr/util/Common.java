package com.hcpda.smr.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017-6-18.
 */

public class Common {

    public static boolean isLOG=false;
    private static String name="SMR";
    public static int ScanKey=139;
    private static String loginName="loginName";
    private static String loginPassword="loginPassword";

    public static String ip="183.238.0.26";
    public static int port=29898;
    public static String strIsAkey="isAkey";
    public static String strkey="key";
    public static boolean isM1 = true;
    //增加防爆标签  Ultralight
    public static void setKeyType(Context context,boolean isM1){
        setSharedPreferences(context,"isM1",isM1);
    }
    public static boolean getTagType(Context context){
        return getSharedPreferences_Boolean(context,"isM1",true);
    }
    public static String getIP(Context mContext){
        return  getSharedPreferences_String(mContext,"ip","183.238.0.26");
    }
    public static void setTimeOut(Context mContext,int timeOut){
        setSharedPreferences(mContext,"timeOut",timeOut);
    }
    public static int getTimeOut(Context mContext){
       return  getSharedPreferences_Int(mContext,"timeOut",25);
    }
    public static void setKey(Context mContext,String key){
        setSharedPreferences(mContext,strkey,key);
    }
    public static String getKey(Context mContext){
        return  getSharedPreferences_String(mContext,strkey,"FFFFFFFFFFFF");
    }
    public static void setIP(Context mContext,String ip){
        setSharedPreferences(mContext,"ip",ip);
    }
    public static int getPort(Context mContext){
        return  getSharedPreferences_Int(mContext,"port",29898);
    }
    public static void setPort(Context mContext,int port){
        setSharedPreferences(mContext,"port",port);
    }

    public static String getLoginName(Context mContext){
        return  getSharedPreferences_String(mContext,loginName,"");
    }
    public static void setLoginName(Context mContext,String name){
        setSharedPreferences(mContext,loginName,name);
    }
    public static String getLoginPassWord(Context mContext){
        return  getSharedPreferences_String(mContext,loginPassword,"");
    }
    public static void setLoginPassWord(Context mContext,String password){
        setSharedPreferences(mContext,loginPassword,password);
    }
    public static boolean  getIsAKey(Context mContext){
        return  getSharedPreferences_Boolean(mContext,strIsAkey,true);
    }
    public static void setIsAKey(Context mContext,boolean isAkey){
        setSharedPreferences(mContext,strIsAkey,isAkey);
    }





    private static  boolean getSharedPreferences_Boolean(Context mContext, String key, boolean def) {
        SharedPreferences preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        boolean result = preferences.getBoolean(key, def);
        return result;
    }
    private static  String getSharedPreferences_String(Context mContext, String key, String def) {
        SharedPreferences preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        String result = preferences.getString(key, def);
        return result;
    }

    private static int getSharedPreferences_Int(Context mContext, String key, int def) {
        SharedPreferences preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        int result = preferences.getInt(key, def);
        return result;
    }

    private static void setSharedPreferences(Context mContext, String key, int value) {
        SharedPreferences preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    private  static void setSharedPreferences(Context mContext, String key, String value) {
        SharedPreferences preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    private  static void setSharedPreferences(Context mContext, String key, boolean value) {
        SharedPreferences preferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
