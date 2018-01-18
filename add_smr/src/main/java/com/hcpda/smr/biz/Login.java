package com.hcpda.smr.biz;

import android.util.Log;
import android.widget.Toast;

import com.hcpda.smr.net.TcpManage;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2017-6-17.
 */

public class Login {
     String TAG="Login";
     private  ILoginErrorBack iLoginErrorBack;
     public boolean loginSystem(String name,String  password){
        Logs.Debug(TAG,String.format("loginSystem({0},{1})",name,password));
        TcpManage.isSendCheckCmd=false;
        String result  =VerificationData.getInstance().login(getLogData(name,password));
        TcpManage.isSendCheckCmd=true;
         if(result.equals("0")){
             return  true;
         }else{
             if(iLoginErrorBack!=null){
                 iLoginErrorBack.errorMessag(result);
             }
         }
        return false;
    }
     private String getLogData(String name,String password){
         StringBuilder stringBuilder=new StringBuilder();

         char[] cName=name.toCharArray();
         String strName=StringUtility.chars2HexString(cName,cName.length);
         int len=strName.length();
         stringBuilder.append(strName);
         for(int k=0;k<(20-len)/2;k++){
             stringBuilder.append("00");
         }
        // stringBuilder.append(strName);

         char[] cPassword=password.toCharArray();//new char[]{'1','2','3'};
         String strPassword= StringUtility.chars2HexString(cPassword,cPassword.length);
         len=strPassword.length();
         for(int k=0;k<(24-len)/2;k++){
             strPassword=strPassword+"00";
         }
         stringBuilder.append(strPassword);
         stringBuilder.append("0001020304050607");
         String sendData="0191"+stringBuilder.toString();
         Logs.Debug(TAG,"登录数据："+sendData);
         return  sendData;
     }
     public interface IScanCallBack {
        void onScanResults(String var1);
    }

    public interface ILoginErrorBack{
        public void errorMessag(String strError);
    }
    public void SetOnCallBack(ILoginErrorBack iLoginErrorBack){
        this.iLoginErrorBack=iLoginErrorBack;
    }
/*

    private void start3(){
        connect();
        new Thread(){
            public void run(){
                while (true) {
                    //---------------------------------
                    char[] cName=new char[]{'a','a','a'};
                    String strName= StringUtility.chars2HexString(cName,cName.length);
                    int len=strName.length();
                    for(int k=0;k<20-len;k++){
                        strName="0"+strName;
                    }
                    //---------------------------------
                    char[] cPassword=new char[]{'1','2','3'};
                    String strPassword= StringUtility.chars2HexString(cPassword,cPassword.length);
                    len=strPassword.length();
                    for(int k=0;k<24-len;k++){
                        strPassword="0"+strPassword;
                    }
                    String strRandom="0011223344556677";

                    String data="9101"+strName+strPassword+strRandom;
                    Log.i(TAG, "data="+data);
                    byte[] sendData= StringUtility.hexStringToBytes(data);
                    sendTcpBytes(sendData);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();



    }

    private  void start2(){
        try {

            //---------------------------------
            char[] cName=new char[]{'a','a','a'};
            String strName= StringUtility.chars2HexString(cName,cName.length);
            int len=strName.length();
            for(int k=0;k<20-len;k++){
                strName="0"+strName;
            }
            //---------------------------------
            char[] cPassword=new char[]{'1','2','3'};
            String strPassword= StringUtility.chars2HexString(cPassword,cPassword.length);
            len=strPassword.length();
            for(int k=0;k<24-len;k++){
                strPassword="0"+strPassword;
            }
            String strRandom="0011223344556677";

            String data=strName+strPassword+strRandom;
            //--------------------------------------------

            //创建Socket对象
            Socket socket = new Socket("116.204.106.81",834);//"116.204.106.81",834     "183.238.0.26", 29898
            //根据输入输出流和服务端连接
            OutputStream outputStream = socket.getOutputStream();//获取一个输出流，向服务端发送信息
            PrintWriter printWriter = new PrintWriter(outputStream);//将输出流包装成打印流
            printWriter.print(data);
            printWriter.flush();
            socket.shutdownOutput();//关闭输出流
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            BufferedReader bufferedReader;

            inputStream = socket.getInputStream();//获取一个输入流，接收服务端的信息
            inputStreamReader = new InputStreamReader(inputStream);//包装成字符流，提高效率
            bufferedReader = new BufferedReader(inputStreamReader);//缓冲区

            for(int k=0;k<20;k++) {
                String info = "";
                String temp = bufferedReader.readLine();//临时变量
                while (temp != null) {
                    info += temp;
                    System.out.println("客户端接收服务端发送信息：" + info);
                }
                try {
                    Thread.sleep(200);
                }catch (Exception ex){}
            }
            //关闭相对应的资源

            printWriter.close();
            outputStream.close();
            socket.close();
        } catch (UnknownHostException e) {
            //Toast.makeText(getApplicationContext(),"连接失败1",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
           // Toast.makeText(getApplicationContext(),"连接失败2",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    */
}
