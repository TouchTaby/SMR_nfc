package com.hcpda.smr.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.hcpda.smr.BaseActivity;
import com.hcpda.smr.R;
import com.hcpda.smr.biz.Barcode2D;
import com.hcpda.smr.biz.BindData;
import com.hcpda.smr.biz.IScanCallBack;
import com.hcpda.smr.biz.QueryData;
import com.hcpda.smr.biz.RFIDManage;
import com.hcpda.smr.util.Logs;
import com.zebra.adc.decoder.Barcode2DWithSoft;


public class QueryFragment extends BaseActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    EditText et1D;
    EditText et2D;
    EditText etSN;
    EditText etDeviceType;
    EditText etUnum;
    Button btConfirm;
    RadioButton rbBarcode;
    RadioButton rbRFID;
    String TAG="QueryFragment";
    QueryData queryData=new QueryData();

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
          //  super.handleMessage(msg);
            if(msg.obj.toString().equals("条码不能存在!")) {
                etSN.setText(et1D.getText());
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_query);
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
                    et1D.setText(barCode);
                }
            }
        });
        queryData.SetOnCallBack(new QueryData.IQueryErrorBack() {
            @Override
            public void errorMessag(String strError) {
                Message msg=Message.obtain();
                msg.obj=strError;
                handler.sendMessage(msg);
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
        et1D = (EditText) findViewById(R.id.et1D);
        et2D = (EditText)  findViewById(R.id.et2D);
        etSN = (EditText) findViewById(R.id.etSN);
        rbBarcode = (RadioButton) findViewById(R.id.rbBarode);
        rbRFID = (RadioButton)  findViewById(R.id.rbRFID);
        etSN.setEnabled(false);
        etDeviceType = (EditText) findViewById(R.id.etDeviceType);
        etDeviceType.setEnabled(false);
        etUnum = (EditText)  findViewById(R.id.etUName);
        etUnum.setEnabled(false);
        btConfirm = (Button)  findViewById(R.id.btConfirm2);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rbBarcode.isChecked()){
                    barcodeToQuery();
                }else if(rbRFID.isChecked()){
                    rfidToQuery();
                }
            }
        });
    }





    private void barcodeToQuery(){
        String str1D=et1D.getText().toString();
        if(str1D.isEmpty()) {
            Toast.makeText(this, "一维条码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }else{
            new QueryDataTask(false,str1D).execute();
        }
    }
    private void rfidToQuery(){
       new QueryDataTask(true,"").execute();
    }

    public class QueryDataTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String msg1="";
        String[] data=null;
        boolean isRFID=false;
        boolean isResult=false;
        String barcode1D="";
        public  QueryDataTask(boolean isRFID,String barcode){
              this.isRFID=isRFID;
              barcode1D=barcode;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result=false;
            if(isRFID) {
                String[] str = RFIDManage.getInstance().readData(new RFID_Error());
                if (str != null && str.length > 0) {
                    isResult=true;
                    data=str;
                    result= true;
                }else {
                    result= false;
                }
            }else{

                String[] data=queryData.queryAssetsData(barcode1D);
                if(data!=null && data.length>=4){
                    isResult=true;
                    this.data=data;
                    result= true;
                }else {
                    result= false;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(result){
                msg1="查询数据成功!";
            }else {
                msg1="查询失败!";
            }
            publishProgress(0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return  result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result){
                if(isRFID){
                    etSN.setText(data[0]);
                    etUnum.setText(data[1]);
                    etDeviceType.setText(data[2]);
                }else {
                    et2D.setText(data[0]);
                    etSN.setText(data[1]);
                    etDeviceType.setText(data[2]);
                    etUnum.setText(data[3]);
                }
            }
            mypDialog.cancel();

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(QueryFragment.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在查询数据.....");
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
