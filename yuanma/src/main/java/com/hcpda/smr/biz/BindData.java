package com.hcpda.smr.biz;

import android.util.Log;

import com.hcpda.smr.net.TcpManage;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;

/**
 * Created by Administrator on 2017-6-17.
 */

public class BindData {

    String TAG="BindData";
    public boolean binDataToSystem(String sn,String type,byte u_num,String  barcode2D){
        Logs.Debug(TAG,String.format("binDataToSystem(sn={%s},type={%s},u_num={%s},barcode2D={%s})",sn,type,u_num,barcode2D));
        TcpManage.isSendCheckCmd=false;
        Log.e("TAG", "binDataToSystem: ----------数据是---"+ getBindData(sn,type,u_num,barcode2D));
        boolean result=  VerificationData.getInstance().binData(getBindData(sn,type,u_num,barcode2D));
        TcpManage.isSendCheckCmd=true;
        return result;
    }
    private String getBindData(String sn,String type,byte u_num,String  barcode2D){
        StringBuilder stringBuilder=new StringBuilder();

        char[] cSn=sn.toCharArray();
        String strSn= StringUtility.chars2HexString(cSn,cSn.length);
        int len=strSn.length();
        stringBuilder.append(strSn);//sn
        for(int k=0;k<(64-len)/2;k++){
            stringBuilder.append("00");
        }


        char[] cType=type.toCharArray();
        String strType= StringUtility.chars2HexString(cType,cType.length);
        len=strType.length();
        stringBuilder.append(strType);//type
        for(int k=0;k<(64-len)/2;k++){
            stringBuilder.append("00");
        }

        stringBuilder.append(StringUtility.byte2HexString(u_num));//u数

        char[] barcode=barcode2D.toCharArray();
        String strBarcode= StringUtility.chars2HexString(barcode,barcode.length);
        len=strBarcode.length();
        stringBuilder.append(strBarcode);//barcode
        for(int k=0;k<(64-len)/2;k++){
            stringBuilder.append("00");
        }

        String sendData="0692"+stringBuilder.toString();
        Logs.Debug(TAG,"绑定数据："+sendData);
        return  sendData;
    }
}
