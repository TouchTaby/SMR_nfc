package com.hcpda.smr.biz;

import android.util.Log;

import com.hcpda.smr.net.TcpManage;
import com.hcpda.smr.util.AppContext;
import com.hcpda.smr.util.Common;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;

import java.util.Arrays;

/**
 * Created by Administrator on 2017-6-17.
 */

public class VerificationData {
    private static VerificationData verfication=null;
    private String TAG="VerificationData";
    private boolean result=false;
    private  String  resultStr = "";
    private  boolean isComplete=false;
    byte[] rssultData=null;
    public static VerificationData getInstance(){
        if(verfication==null){
            verfication=new VerificationData();
        }
        return  verfication;
    }
     public String login(String data) {
         resultStr="登录超时";
         isComplete=false;
         byte[] sendData = StringUtility.hexStringToBytes(data);
         new Thread() {
             public void run() {
                 TcpManage.getInstance().setTcpBackData(new ITcpBack() {
                     @Override
                     public void getData(String data) {
                         Logs.Info(TAG, "登录返回验证信息="+data);
                         if (data.contains("01910000")) {
                             Logs.Info(TAG, "登录成功isSendCheckCmd=true");
                             resultStr="0";//登录成功
                             isComplete=true;
                         } else  if (data.contains("01910100"))  {
                             Logs.Info(TAG, "登录失败isSendCheckCmd=false");
                             resultStr = "用户名或密码错误!";
                             TcpManage.getInstance().close2();
                             isComplete=true;
                         }
                     }

                     @Override
                     public void getData(byte[] data, int len) {

                     }
                 });
             }
         }.start();
         int timeOut= Common.getTimeOut(AppContext.context);
         int count=timeOut/7+1;
         int flag=0;
         for (int f = 0; f < count;f++) {
             Logs.Info(TAG, "登录第" + f + "次连接!");
             String str = TcpManage.getInstance().sendTcpBytes(sendData);
             if (str.equals("连接失败")) {
                 resultStr ="连接失败";
                 TcpManage.getInstance().setTcpBackData(null);
                 return resultStr;
             } else {
                 for (int k = 0; k < 7; k++) {
                     if (isComplete) {
                         TcpManage.getInstance().setTcpBackData(null);
                         return resultStr;
                     }
                     flag++;
                     if (flag >= timeOut) {
                         TcpManage.getInstance().setTcpBackData(null);
                         return resultStr;
                     }
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
         TcpManage.getInstance().setTcpBackData(null);
         return resultStr;
     }
     public boolean binData(String data){
        byte[] sendData= StringUtility.hexStringToBytes(data);
         result = false;
         isComplete=false;
         new Thread() {
             public void run() {
                 TcpManage.getInstance().setTcpBackData(new ITcpBack() {
                     @Override
                     public void getData(String data) {
                         Log.e("TAG", "绑定返回数据="+data);
                         if (data.contains("06920000")) {
                             TcpManage.getInstance().setTcpBackData(null);
                             result = true;//
                             Logs.Info(TAG, "绑定成功isSendCheckCmd=true");
                             isComplete=true;
                         } else  if (data.contains("06920100"))  {
                             TcpManage.getInstance().setTcpBackData(null);
                             Logs.Info(TAG, "绑定失败isSendCheckCmd=false");
                             result = false;
                             isComplete=true;
                         }
                     }

                     @Override
                     public void getData(byte[] data, int len) {

                     }
                 });
             }
         }.start();

         int timeOut= Common.getTimeOut(AppContext.context);
         int count=timeOut/7+1;
         int flag=0;

         for (int f = 0; f < count;f++) {
             Log.e("TAG", "第"+f+"次请求绑定!");
             TcpManage.getInstance().sendTcpBytes(sendData);
             for (int k = 0; k < 8; k++) {
                 if(isComplete) {
                     return result;
                 }

                 flag++;
                 if(flag>=timeOut){
                     TcpManage.getInstance().setTcpBackData(null);
                     return false;
                 }

                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
         TcpManage.getInstance().setTcpBackData(null);
         return false;
    }
     public byte[] queryAssets(String barcode1D){
         rssultData=null;
         byte[] sendData= StringUtility.hexStringToBytes(barcode1D);
         result = false;
         isComplete=false;
         new Thread() {
             public void run() {
                 TcpManage.getInstance().setTcpBackData(new ITcpBack() {
                     @Override
                     public void getData(String data) {
                     }

                     @Override
                     public void getData(byte[] data, int len) {

                         if(Logs.isFlag) {
                             Logs.Info(TAG, "查询返回数据长度=" + len);
                             Logs.Info(TAG, "查询返回数据" + new String(data));
                         }
                         for(int k=0;k<len;k++){
                             if((k+4)<=len) {
                                 if (StringUtility.byte2HexString(data[k]).equals("05") && StringUtility.byte2HexString(data[k+1]).equals("92")) {
                                     if (StringUtility.byte2HexString(data[k+2]).equals("01") && StringUtility.byte2HexString(data[k+3]).equals("00")) {
                                         //获取失败
                                         TcpManage.getInstance().setTcpBackData(null);
                                         Logs.Info(TAG, "查询数据失败不存在此条码的数据");
                                         result = false;
                                         isComplete=true;
                                         rssultData=new byte[]{-1};
                                         break;
                                     }
                                   //  if (StringUtility.byte2HexString(data[k+2]).equals("00") && StringUtility.byte2HexString(data[k+3]).equals("00")) {
                                         //获取成功
                                         if(len-k>=99){//获取长度正确
                                             TcpManage.getInstance().setTcpBackData(null);
                                             rssultData=Arrays.copyOfRange(data,k+2,k+2+97);//数据长度99
                                             result = true;
                                             isComplete=true;
                                             break;
                                         }else{
                                             //返回数据长度有误
                                             Logs.Info(TAG, "返回数据长度有误,0x05开始的数据长度："+(len-k));
                                         }
                                   //  }

                                 }
                             }
                         }

                     }
                 });
             }
         }.start();

         int timeOut= Common.getTimeOut(AppContext.context);
         int count=timeOut/7+1;
         int flag=0;
         for (int f = 0; f < count;f++) {
             Logs.Info(TAG, "查询第"+f+"次!");
             TcpManage.getInstance().sendTcpBytes(sendData);
             for (int k = 0; k < 8; k++) {
                 if(isComplete) {
                    return rssultData;
                 }
                 flag++;
                 if(flag>=timeOut){
                     TcpManage.getInstance().setTcpBackData(null);
                     return rssultData;
                 }

                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
         TcpManage.getInstance().setTcpBackData(null);
         return rssultData;
    }



}
