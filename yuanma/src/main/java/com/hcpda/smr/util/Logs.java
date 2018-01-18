package com.hcpda.smr.util;

import android.util.Log;

/**
 * Created by Administrator on 2017-6-17.
 */

public class Logs{


    public  static  boolean isFlag=true;
    public static void Info(String tag,String log){
        if(Common.isLOG)
          FileUtils.writerLog(tag+"   "+log);
    }
    public static void Debug(String tag,String log){
        if(Common.isLOG)
          FileUtils.writerLog(tag+"   "+log);
    }

}
