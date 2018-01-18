package com.hcpda.smr.net;

import com.hcpda.smr.biz.ITcpBack;
import com.hcpda.smr.util.Common;
import com.hcpda.smr.util.Logs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by Administrator on 2017-6-17.
 */

public class TcpManage {

    private static boolean tcpBack_valid=true;//
    private Object lock_object=new Object();//同步锁变量
    private String cmd1="0291";//心跳包
    private boolean isConnect=false;//是否连接
    private static TcpManage tcpManage=null;
    private static boolean connecting=false;
    private String TAG="tcpManage";
    private Socket client ;
    private OutputStream out_put;
    private BufferedInputStream in_put;
    private ITcpBack tcpBackData=null;
    private byte[] read_byte = new byte[2048];
    private static  ReadDataThread tcpThread=null;
    private static CheckedConnectThread checkedConnectThread=null;
    public  static boolean isRuning =true;
    public static boolean isSendCheckCmd=true;//是否发送心跳包

    private TcpManage(){

    }
    public static TcpManage getInstance(){
        if(tcpManage==null){
            tcpManage=new TcpManage();
        }
        return  tcpManage;
    }
    public   boolean connect() {
          if(!isRuning)
              return  false;
        try {
            if(connecting) {
                Logs.Info(TAG, "还在连接中！");
                return false;
            }
            connecting=true;

            if(client!=null) {
                try {
                    client.shutdownInput();
                }catch (Exception ex){
                }
                try {
                    client.shutdownOutput();
                }catch (Exception ex){
                }
                try {
                    client.close();
                }catch (Exception ex){

                }
            }
            Logs.Info(TAG, "开始连接");
         //   etIP.setText(Common.getIP(this));
          //  etPort.setText(Common.getPort(this)+"");
            String strIP= Common.ip;
            int port=Common.port;
            Logs.Info(TAG, "开始连接ip="+strIP+"  port="+port);
            SocketAddress socketaddress = new InetSocketAddress(strIP,port);//     "183.238.0.26", 29898
            client = new Socket();

            client.connect(socketaddress, 3000);

            client.setTcpNoDelay(true);

            client.setSendBufferSize(4096);

            client.setReceiveBufferSize(4096);

            client.setKeepAlive(true);

            client.setOOBInline(true);

            out_put = client.getOutputStream();

            in_put = new BufferedInputStream(client.getInputStream());
            Logs.Info(TAG, "连接成功");

            connecting=false;
            return  true;
        } catch (UnknownHostException e) {
            Logs.Info(TAG, "连接错误UnknownHostException");

        } catch (IOException e) {
            Logs.Info(TAG, "连接服务器io错误");

        } catch (IllegalArgumentException ex) {
            Logs.Info(TAG, "连接服务器无效服务配置参数");

        } catch (Exception e) {
            Logs.Info(TAG, "连接服务器错误Exception");

        }
        connecting=false;
        return false;
    }

    public synchronized String sendTcpBytes(byte[] bt) {
        try {
            if(out_put==null) {
                if(!isRuning) {
                    Logs.Info(TAG, "sendTcpBytes isRuning=false 返回！");
                    return "";
                }
                if(connecting) {
                    Logs.Info(TAG, "sendTcpBytes 还在连接中 返回！");
                    return "";
                }
                if(!connect()) {
                    Logs.Info(TAG, "sendTcpBytes 连接失败 返回！");
                    return "连接失败";
                }
            }
            Logs.Info(TAG, "发送数据");
            out_put.write(bt);
        } catch (IOException e) {
            Logs.Info(TAG, "连接出错，重新尝试连接...");
            if(!connect()) {
                Logs.Info(TAG, "sendTcpBytes,IOException 连接失败 返回！");
                notifyMsg();
                return "连接失败";
            }
            try {
                out_put.write(bt);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            notifyMsg();
        }
        return "";
    }



    public void  setTcpBackData(ITcpBack iTcpBack){
        if(iTcpBack==null){
            TcpManage.getInstance().tcpBack_valid=false;
        }else {
            TcpManage.getInstance().tcpBack_valid=true;
        }
        tcpBackData = iTcpBack;
    }

     //获取tcp数据线程
   private class ReadDataThread extends Thread{
        public void run(){
                while (isRuning) {
                    try {
                        if(client!=null){
                           // Logs.Info(TAG, "client.isInputShutdown()="+client.isInputShutdown());
                           // Logs.Info(TAG, "client.isOutputShutdown()="+client.isOutputShutdown());
                            if(!client.isInputShutdown()|| !client.isOutputShutdown()){
                             //   Logs.Info(TAG, "读取数据(!client.isInputShutdown()|| !client.isOutputShutdown() 重连");
                            //    connect();
                            }
                        }
                        int len=0;
                        if(in_put!=null)
                         len = in_put.read(read_byte);
                     //   Logs.Info(TAG, "读取数据返回长度："+len);
                        if (len>0) {
                            String hex = com.hcpda.smr.util.StringUtility.bytes2HexString(read_byte, len);
                            if (hex.contains(cmd1)) {
                                isConnect = true;
                                notifyMsg();
                            }

                            if (tcpBackData != null) {
                                try {
                                    tcpBackData.getData(hex);
                                    tcpBackData.getData(read_byte, len);
                                }catch(Exception ex) {
                                    Logs.Info(TAG, "ReadDataThread：异常"+ex.getMessage());
                                }
                            }

                            Logs.Info(TAG, "获取数据返回hex="+hex);
                        }
                        if(in_put==null){
                            Logs.Info(TAG, "读取数据 in_put == null");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        Logs.Info(TAG, "读取数据异常,重连接，IOException:"+e.getMessage());
                        connect();
                        notifyMsg();
                    } catch (Exception e) {
                        Logs.Info(TAG, "读取数据异常2");
                    }
                }
        }
    }
    //发送心跳包检测网络
    private class CheckedConnectThread extends Thread{
        public void run(){

                if(1==1)return;
                while (isRuning) {
                    Logs.Info(TAG, "isSendCheckCmd="+isSendCheckCmd);
                    try {
                    if(!isSendCheckCmd){
                       // Logs.Info(TAG, "不发送心跳包");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                        isConnect = false;
                        String hex = cmd1;
                        StringBuilder stringBuilder = new StringBuilder();
                        Random random = new Random();
                        for (int k = 0; k < 16; k++) {
                            stringBuilder.append(String.valueOf(random.nextInt(10)));
                        }
                        hex = hex + stringBuilder.toString();
                        byte[] sendData = com.hcpda.smr.util.StringUtility.hexStringToBytes(hex);
                       // Logs.Info(TAG, "发送心跳包命令" + hex);
                        sendTcpBytes(sendData);
                        waitMsg(5000);
                        Logs.Info(TAG, "isConnect="+isConnect);
                        if (!isConnect) {//断开了连接重新连接
                            Logs.Info(TAG, "连接断开重新连接");
                            connect();
                        } else {
                            Logs.Info(TAG, "目前处于连接状态");
                            waitMsg(10000);
                        }
                    }catch(Exception ex){
                        Logs.Info(TAG, "CheckedConnectThread ->Exception"+ex.getMessage());
                    }
                }
        }
    }


    public void startTcpService(){
        new Thread(){
            public void run(){
                connect();
            }
        }.start();

        if(tcpThread==null){
            tcpThread =new ReadDataThread();
            tcpThread.start();
        }
        cheakTCP();
    }
    public void cheakTCP(){
        if(checkedConnectThread==null){
            checkedConnectThread =new CheckedConnectThread();
            checkedConnectThread.start();
        }
    }

    private void waitMsg(int time){
        synchronized (lock_object){
            try {
                lock_object.wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void notifyMsg(){
        synchronized (lock_object){
            lock_object.notifyAll();
        }
    }

   public void close(){
       isRuning=false;
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       notifyMsg();
       try{
         client.close();
       }catch (Exception ex){}
       connecting=false;
       tcpThread=null;
       checkedConnectThread=null;
       Logs.Info(TAG,"关闭TCP");
   }

    public void close2(){
        try{
            client.close();
        }catch (Exception ex){}
        out_put=null;
    }
}
