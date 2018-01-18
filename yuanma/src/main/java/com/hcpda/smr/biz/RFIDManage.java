package com.hcpda.smr.biz;

import android.util.Log;

import com.hcpda.smr.util.AppContext;
import com.hcpda.smr.util.Common;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;
import com.rscja.deviceapi.RFIDWithISO14443A;
import com.rscja.deviceapi.entity.SimpleRFIDEntity;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.deviceapi.exception.RFIDNotFoundException;
import com.rscja.deviceapi.exception.RFIDVerificationException;

/**
 * Created by Administrator on 2017-6-18.
 */

public class RFIDManage {
    String TAG="RFIDManage";
    private static RFIDWithISO14443A mRFID_A=null;
    private static RFIDManage rfidManage=null;
    private IErrorMessag iErrorMessag;

    private RFIDManage(){
        try {
            mRFID_A= RFIDWithISO14443A.getInstance();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
    public  static  RFIDManage getInstance(){
        if(rfidManage==null)
            rfidManage=new RFIDManage();
        return rfidManage;
    }
    public boolean rfidInit(){
        return  mRFID_A.init();
    }
    public void rfidFree(){
        mRFID_A.free();
    }
    public boolean writeData(String sn,String type,byte u_num,IErrorMessag iErrorMessag){
        this.iErrorMessag=iErrorMessag;
//         标准版标签SN号（ascii）:32Byte
//         第一扇区第4块32字节如图所示

//         标准版标签U高度（十六进制数）1Byte读写
//         第一扇区6块第一字节
//
//          标准版标签类别(ascii)读写:
//          第一扇区6块第2字节到第16字节，以及第二扇区
//          第二扇区第8块16字节
//          第二扇区第9块第1字节

        Logs.Info(TAG,"writeData ->sn:"+sn);
        Logs.Info(TAG,"writeData ->type:"+type);
        Logs.Info(TAG,"writeData ->u_num:"+u_num);

        String key=Common.getKey(AppContext.context);
        RFIDWithISO14443A.KeyType nKeyType;
        if(Common.getIsAKey(AppContext.context)){
            nKeyType = RFIDWithISO14443A.KeyType.TypeA;
        }else{
            nKeyType = RFIDWithISO14443A.KeyType.TypeB;
        }
        Logs.Info(TAG,"写卡当前设置的卡片秘钥:"+key);
        Logs.Info(TAG,"写卡当前设置的卡片秘钥类型:"+nKeyType);

        if (mRFID_A != null) {
            String strTemp="00000000000000000000000000000000";
            SimpleRFIDEntity entity = null;
            entity = mRFID_A.request();
            if (entity != null) {
                //------------------第一扇区第4块32字节如图所示 写入sn号---------------------------------------------
                //sn
                char[] c_sn=sn.toCharArray();
                String hex_sn= StringUtility.chars2HexString(c_sn,c_sn.length);
                //间隔
                String jg= StringUtility.chars2HexString("@".toCharArray(),1);
                char[] c_type=type.toCharArray();
                //类型
                String type_hex= StringUtility.chars2HexString(c_type,c_type.length);
                String snTypeTemp=hex_sn + jg + type_hex;
                String snType=snTypeTemp;
                for (int k=0;k<32-(snTypeTemp.length()/2);k++){
                    snType=snType+"00";
                }
                //u数
               String unum= com.rscja.utility.StringUtility.byte2HexString(u_num);
                try {
                    // --------------sn----------------
                        boolean result=mRFID_A.write(key, nKeyType, 1, 0, snType.substring(0,32));//第四块
                        if(result) {
                            if(this.iErrorMessag!=null){
                                this.iErrorMessag.getError("写入第四块成功!");
                            }
                            Logs.Info(TAG, "sn+类型 写入第1个扇区0块（第四块）成功 Data=" + snType.substring(0, 32));
                        }else{
                            if(this.iErrorMessag!=null){
                                this.iErrorMessag.getError("写入第四块失败!");
                            }
                            Logs.Info(TAG,"sn+类型 写入第1个扇区0块（第四块）失败 Data="+ snType.substring(0,32));
                            return false;
                        }
                        result= mRFID_A.write(key, nKeyType, 1, 1, snType.substring(32, 64));//第五块
                        if(result){
                            if(this.iErrorMessag!=null){
                                this.iErrorMessag.getError("写入第五块成功!");
                            }
                            Logs.Info(TAG,"sn+类型 写入第1个扇区1块（第五块）成功 Data="+snType.substring(32, 64));
                        }else{
                            if(this.iErrorMessag!=null){
                                this.iErrorMessag.getError("写入第五块失败!");
                            }
                            Logs.Info(TAG,"sn+类型 写入第1个扇区1块（第五块）失败 Data="+snType.substring(32, 64));
                            return false;
                        }
                        //第二扇区6块第2字节到第16字节
                        result=mRFID_A.write(key, nKeyType, 1, 2, unum);//第六块
                        if(result){
                            if(this.iErrorMessag!=null){
                                this.iErrorMessag.getError("写入第六块成功!");
                            }
                            Logs.Info(TAG,"u数，写入第2个扇区2块（第六块）成功 Data="+ unum);
                        }else{
                            if(this.iErrorMessag!=null){
                                this.iErrorMessag.getError("写入第六块失败!");
                            }
                            Logs.Info(TAG,"u数，写入第2个扇区2块（第六块）失败 Data="+ unum);
                            return false;
                        }
                    return true;
                } catch (Exception e) {
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("写RFID标签异常!");
                    }
                    Logs.Info(TAG,"写RFID标签异常!");
                    return false;
                }
            }else{
                if(this.iErrorMessag!=null){
                    this.iErrorMessag.getError("写RFID标签,寻标签失败!");
                }
                Logs.Info(TAG,"写RFID标签,寻标签失败!");
            }
        }
        return false;
    }

    public String[] readData(IErrorMessag iErrorMessag){
        this.iErrorMessag=iErrorMessag;
        SimpleRFIDEntity entity = null;
        String key=Common.getKey(AppContext.context);
        RFIDWithISO14443A.KeyType nKeyType;
        if(Common.getIsAKey(AppContext.context)){
            nKeyType = RFIDWithISO14443A.KeyType.TypeA;
        }else{
            nKeyType = RFIDWithISO14443A.KeyType.TypeB;
        }
        Logs.Info(TAG,"读卡当前设置的卡片秘钥类型:"+nKeyType);
        Logs.Info(TAG,"读卡当前设置的卡片秘钥:"+key);
        entity = mRFID_A.request();
        if (entity != null) {
            try {
                //-------------------------------sn----------------------------------------------------------
                entity = mRFID_A.read(key, nKeyType, 1, 0);// 第四块
                String snType1="";//sn + 类型
                if(entity!=null){
                    snType1=entity.getData();
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("读取第四块成功!");
                    }
                    Logs.Info(TAG,"sn读取第四块数据="+snType1);
                }else{
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("读取第四块失败!");
                    }
                    Logs.Info(TAG,"sn读取第四块数据失败");
                }

                entity = mRFID_A.read(key, nKeyType, 1, 1);// 第五块
                String snType2="";
                if(entity!=null){
                    snType2=entity.getData();
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("读取第五块成功!");
                    }
                    Logs.Info(TAG,"sn读取第五块数据="+snType2);
                }else{
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("读取第五块失败!");
                    }
                    Logs.Info(TAG,"sn读取第五块数据失败");
                }

                String snType=snType1+snType2;
                int index=snType.indexOf("00");
                String strData=snType;
                if(index>0) {
                      if(index%2!=0)
                          index=index+1;
                      strData = snType.substring(0, index);
                }
                char[] charSNType= StringUtility.hexString2Chars(strData);
                String snTypeTemp=new String(charSNType);

                  String sn=snTypeTemp;
                  String type="";
                  if(snTypeTemp.contains("@")) {
                      sn = snTypeTemp.split("@")[0];
                      type = snTypeTemp.split("@")[1];
                  }
            //------------------------类型------u数----------------------------------------------------------
                String u_num="-1";//u 数量
                entity=mRFID_A.read(key,nKeyType,1,2);//第六个块
                if(entity!=null){
                    u_num= String.valueOf(Byte.parseByte(entity.getData().substring(0,2),16));
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("读取第六块成功!");
                    }
                    Logs.Info(TAG,"sn读取第六块数据="+entity.getData());
                }else{
                    if(this.iErrorMessag!=null){
                        this.iErrorMessag.getError("读取第六块失败!");
                    }
                    Logs.Info(TAG,"sn读取第六块数据失败");
                }

                String[] data=new String[3];
                data[0]=sn;
                data[1]=u_num;
                data[2]=type;
                return  data;
            }catch (Exception ex){
                 return null;
            }
        }else{
            if(this.iErrorMessag!=null){
                this.iErrorMessag.getError("寻标签失败!");
            }
            Logs.Info(TAG,"寻卡失败!");
            return null;
        }

    }

    public interface IErrorMessag{
        public void getError(String msg);
    }
}
