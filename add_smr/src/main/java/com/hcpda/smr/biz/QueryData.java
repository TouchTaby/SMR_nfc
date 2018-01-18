package com.hcpda.smr.biz;

import com.hcpda.smr.net.TcpManage;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;

import java.util.Arrays;

/**
 * Created by Administrator on 2017-6-18.
 */

public class QueryData {
    String TAG="QueryData";
    public String[] queryAssetsData(String barcode1D){
        Logs.Debug(TAG,String.format("queryAssetsData(barcode1D={0})",barcode1D));
        TcpManage.isSendCheckCmd=false;
        char[] cBarcode=barcode1D.toCharArray();
        String hex= StringUtility.chars2HexString(cBarcode,cBarcode.length);
        String cmd= "0592"+hex;
        Logs.Debug(TAG,"queryAssetsData 查询命令:"+cmd);
        byte[] data=VerificationData.getInstance().queryAssets(cmd);
        TcpManage.isSendCheckCmd=true;
        if(data!=null){
            if(data.length==1 && data[0]==-1){
                    if(queryErrorBack!=null){
                        queryErrorBack.errorMessag("条码不存在!");
                    }
                 return null;
            }else {
                return getAssets(data);
            }
        }
        return null;
    }

    /*
    05 92 44 49 47 32 30 31 37 30 36 31 36 30 30 30 30 31 00 00 00 00 00 00 00 00 00 00 00 00 00
     00 00 00 44 69 67 53 76 72 32 30 31 37 30 36 31 36 30 31 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 43 45 36 38 35 30 00 00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00 00 02

     */

//    1Code  :  array[1..32] of Char;   //资产上的一维码信息
//    RFID  :  array[1..32] of Char;   //资产SN号
//    Type  :  array[1..32] of Char;   //资产类型
//    U  :  Byte;  //资产占用的U数
    private String[] getAssets(byte[] data){
        String[] assetsArray= new String[4];
        if(data==null || data.length<97)
            return null;
      //  byte[] result= Arrays.copyOfRange(data,0,2);
        byte[] barcode=Arrays.copyOfRange(data,0,32);
        byte[] sn=Arrays.copyOfRange(data,32,64);
        byte[] type=Arrays.copyOfRange(data,64,96);
        byte[] u=Arrays.copyOfRange(data,96,97);
        int barcodeLeng=0;
        int snLeng=0;
        int typeLeng=0;

        //检查barcode有效数据长度
        for(int k=0;k<barcode.length;k++){
            if(barcode[k]==0x00){
             break;
            }
            barcodeLeng++;
        }


        //检查sn有效数据长度
        for(int k=0;k<sn.length;k++){
            if(sn[k]==0x00){
                break;
            }
            snLeng++;
        }


        //检查type有效数据长度
        for(int k=0;k<type.length;k++){
            if(type[k]==0x00){
                break;
            }
            typeLeng++;
        }
        assetsArray[0]=new String(barcode,0,barcodeLeng);
        assetsArray[1]=new String(sn,0,snLeng);
        assetsArray[2]=new String(type,0,typeLeng);
        assetsArray[3]=String.valueOf(u[0]);
        Logs.Debug(TAG,"查询返回的条码数据:"+assetsArray[0]);
        Logs.Debug(TAG,"查询返回的SN数据:"+assetsArray[1]);
        Logs.Debug(TAG,"查询返回的类型数据:"+assetsArray[2]);
        Logs.Debug(TAG,"查询返回的U数据:"+assetsArray[3]);
        return assetsArray;
    }
    public interface IQueryErrorBack{
        public void errorMessag(String strError);
    }
    private IQueryErrorBack queryErrorBack;
    public void SetOnCallBack(IQueryErrorBack iqueryErrorBack){
        this.queryErrorBack=iqueryErrorBack;
    }

}
