package com.hcpda.smr.biz;

import android.content.Context;
import android.util.Log;

import com.hcpda.smr.util.Logs;
import com.zebra.adc.decoder.Barcode2DWithSoft;

/**
 * Created by Administrator on 2017-6-17.
 */

public class Barcode2D {

    private  Barcode2DWithSoft barcode2DWithSoft=Barcode2DWithSoft.getInstance();
    private  String TAG="Barcode2D";
    private static Barcode2D barcode2D=null;
    private IScanCallBack scanCallBack=null;
    public static  Barcode2D getInstance(){
      if(barcode2D==null)
          barcode2D= new Barcode2D();
        return barcode2D;
    }

    public Barcode2DWithSoft.ScanCallback mScanCallback = new Barcode2DWithSoft.ScanCallback() {
        @Override
        public void onScanComplete(int symbology, int length, byte[] data) {

            if (length < 1) {
                if (length == -1) {
                     Logs.Info(TAG,"扫描取消");
                } else if (length == 0) {
                    Logs.Info(TAG,"扫描超时");
                } else {
                    Logs.Info(TAG,"扫描失败");
                }
                if(scanCallBack!=null){
                    scanCallBack.onScanResults("");
                }
            }else{
                String barCode = new String(data, 0, length);
                Logs.Info(TAG,"扫描成功，数据："+barCode);
                if(scanCallBack!=null){
                    scanCallBack.onScanResults(barCode);
                }
            }
        }
    };
    public boolean Open(Context context){
        Logs.Info(TAG,"打开2D");
        barcode2DWithSoft.setScanCallback( mScanCallback);
       return barcode2DWithSoft.open(context);
    }
    public boolean Close(){
        Logs.Info(TAG,"关闭2D");
        return barcode2DWithSoft.close();
    }
    public void Scan(IScanCallBack scanCallBack){
        Logs.Info(TAG,"开始扫描");
        this.scanCallBack=scanCallBack;
         barcode2DWithSoft.scan();
    }
    public void StopScan(){
        Logs.Info(TAG,"停止扫描");
        barcode2DWithSoft.stopScan();
    }
}
