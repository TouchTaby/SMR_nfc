package com.hcpda.smr.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hcpda.smr.R;
import com.hcpda.smr.biz.BindData;
import com.hcpda.smr.biz.IScanCallBack;
import com.hcpda.smr.biz.RFIDManage;
import com.hcpda.smr.util.Logs;
import com.zebra.adc.decoder.Barcode2DWithSoft;


public class BindDataFragment extends BaseActivity {
    EditText et1D;
    EditText et2D;
    EditText etSN;
    EditText etDeviceType;
    EditText etU;
    Button btConfirm;
    CallBack callBack=new CallBack();
    String TAG="BindDataFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bind_data);
        initView();
        RFIDManage.getInstance().rfidInit();
        Barcode2DWithSoft.getInstance().open(this);
        Barcode2DWithSoft.getInstance().setScanCallback(new Barcode2DWithSoft.ScanCallback() {
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

                }else{
                    String barCode = new String(data, 0, length);
                    Logs.Info(TAG,"扫描成功，数据："+barCode);
                    et2D.setText(barCode);
                }
            }
        });
    }

    @Override
    public void onPause() {

        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RFIDManage.getInstance().rfidFree();
        Barcode2DWithSoft.getInstance().stopScan();
        Barcode2DWithSoft.getInstance().close();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==139){
            Barcode2DWithSoft.getInstance().scan();
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        et1D = (EditText)  findViewById(R.id.et1D);
        et2D = (EditText) findViewById(R.id.et2D);
        etSN = (EditText) findViewById(R.id.etSN);
        etDeviceType = (EditText) findViewById(R.id.etDeviceType);
        etU = (EditText)  findViewById(R.id.etUName);

       // DigSvr2017061601，CE6850，2，E5X016CD  //DIG2017061600001  DigSvr2017061601  R710  2
       // et2D.setText("E5X016CD");
        etSN.setText("DigSvr2017061601");
        etDeviceType.setText("CE6850");
        etU.setText("2");

        btConfirm = (Button)  findViewById(R.id.btConfirm2);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str2D=et2D.getText().toString();
                String strSN=etSN.getText().toString();
                String strType=etDeviceType.getText().toString();
                String strU=etU.getText().toString();

                if(str2D.isEmpty()){
                    Toast.makeText(BindDataFragment.this,"条码不能为空!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(strSN.isEmpty()){
                    Toast.makeText(BindDataFragment.this,"SN不能为空!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(strType.isEmpty()){
                    Toast.makeText(BindDataFragment.this,"类型不能为空!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(strU.isEmpty()){
                    Toast.makeText(BindDataFragment.this,"U数不能为空!",Toast.LENGTH_SHORT).show();
                    return;
                }
                new BindDataTask(str2D,strSN,strType,strU).execute();
            }
        });
    }

   class CallBack implements IScanCallBack {
       @Override
       public void onScanResults(String var1) {
           et2D.setText(var1);
       }
   }


    public class BindDataTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String str2D= "";
        String strSN= "";
        String strType= "";
        String strU= "";
        String errorTip="";
        String msg1="";
        public BindDataTask(String str2D,String strSN,String strType,String strU){
            this.str2D=str2D;
            this.strSN= strSN;
            this.strType=strType;
            this.strU= strU;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean reuslt= RFIDManage.getInstance().writeData(strSN,strType,(byte)Integer.parseInt(strU),new RFID_Error());
            if(!reuslt) {
                msg1="写卡失败!";
            }else{
                msg1="正在连接服务器绑定数据!";
                publishProgress(0);
                BindData bindData = new BindData();
                reuslt= bindData.binDataToSystem(strSN, strType, (byte) Integer.parseInt(strU), str2D);
                if(!reuslt){
                    msg1="服务器绑定数据失败!";
                }
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(reuslt){
                msg1="绑定数成功!";
            }else {
                msg1="绑定数失败!";
            }
            publishProgress(0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return  reuslt;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(BindDataFragment.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在绑定数据.....");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0]==0){
                mypDialog.setMessage(msg1);
            }
        }
        class RFID_Error implements  RFIDManage.IErrorMessag{
            @Override
            public void getError(String msg) {
                publishProgress(0);
                msg1=msg;
            }
        }
    }

}



