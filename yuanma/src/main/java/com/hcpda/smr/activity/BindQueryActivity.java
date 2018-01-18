package com.hcpda.smr.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpda.smr.BaseActivity;
import com.hcpda.smr.R;
import com.hcpda.smr.biz.BindData;
import com.hcpda.smr.biz.QueryData;
import com.hcpda.smr.biz.RFIDManage;
import com.hcpda.smr.net.NetUtils;
import com.hcpda.smr.util.FileUtils;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.SaveToExcelUtil;
import com.hcpda.smr.util.SoundManager;
import com.zebra.adc.decoder.Barcode2DWithSoft;


public class BindQueryActivity extends BaseActivity {

    SaveToExcelUtil  saveToExcelUtil=null;
    boolean isScaning=false;
    TextView tvMsg;
    TextView tile_info;
    EditText et1D;
    EditText et2D;
    EditText etSN;
    EditText etDeviceType;
    EditText etU;
    Button btConfirm;
    Button btn_query;
    Button btn_clean;
    Button btnQuery;
    EditText etDeviceType_query;
    EditText etSN_query;
    EditText etUName_query;
    CheckBox checkboxWriteRFid;
    Barcode2DWithSoft barcode2DWithSoft;
    String TAG="BindDataFragment";
    String msg1="查询、绑定资产信息";
    String msg2="(脱机工作中)";
    String msg3="(服务器配置错误，脱机工作中)";
    SoundManager soundManager=null;
    QueryData queryData=new QueryData();
    int flag=0;
    class BarcodeBack implements  Barcode2DWithSoft.ScanCallback{
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
                soundManager.playSound(0);
            }else{
                String barCode = new String(data, 0, length);
                Logs.Info(TAG,"扫描成功symbology="+symbology+"，数据："+barCode);

                soundManager.playSound(1);

                if(symbology==28){
                    et2D.setText(barCode);
                }else {
                    et1D.setText(barCode);
                    et2D.setFocusable(true);
                    barcodeToQuery();
                }
            }
            isScaning=false;
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //  super.handleMessage(msg);
            if(msg.obj.toString().equals("条码不存在!")) {
                etSN.setText(et1D.getText());
                etDeviceType.setText("");
                etU.setText("");
                et2D.setText("");
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bain_query);
        actionBar.setTitle("资产和标签绑定");
        soundManager=SoundManager.getInstance(this);
        barcode2DWithSoft=Barcode2DWithSoft.getInstance();
        initView();
        flag=getIntent().getIntExtra("Flag",0);
        new initTask(this).execute();
        queryData.SetOnCallBack(new QueryData.IQueryErrorBack() {
            @Override
            public void errorMessag(String strError) {
                Message msg=Message.obtain();
                msg.obj=strError;
                handler.sendMessage(msg);
            }
        });
        setText();
    }

    @Override
    public void onPause() {

        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.Info(TAG,"---------------------rfid free----------------------");
        RFIDManage.getInstance().rfidFree();
        barcode2DWithSoft.stopScan();
        Logs.Info(TAG,"---------------------barcode close----------------------");
        barcode2DWithSoft.close();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==139){
            if(!isScaning) {
                isScaning=true;
                barcode2DWithSoft.scan();
            }
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    String str2D ;
    String strSN ;
    String strType ;
    String strU ;
    String str1D ;
    private void initView() {
        btn_query = (Button)  findViewById(R.id.btn_query);
        btn_clean = (Button) findViewById(R.id.btn_clean);
        btnQuery= (Button) findViewById(R.id.btnQuery);
        etDeviceType_query = (EditText)  findViewById(R.id.etDeviceType_query);
        etSN_query = (EditText) findViewById(R.id.etSN_query);
        etUName_query = (EditText) findViewById(R.id.etUName_query);
        checkboxWriteRFid=(CheckBox)findViewById(R.id.checkboxWriteRFid);
        et1D = (EditText)  findViewById(R.id.et1D);
        et2D = (EditText) findViewById(R.id.et2D);
        etSN = (EditText) findViewById(R.id.etSN);
        etDeviceType = (EditText) findViewById(R.id.etDeviceType);
        etU = (EditText)  findViewById(R.id.etUName);
        tile_info = (TextView)  findViewById(R.id.tile_info);
        tvMsg = (TextView)  findViewById(R.id.tvMsg);
       // DigSvr2017061601，CE6850，2，E5X016CD  //DIG2017061600001  DigSvr2017061601  R710  2
       // et2D.setText("E5X016CD");
        etSN.setText("");
        etDeviceType.setText("");
        etU.setText("");

        btConfirm = (Button)  findViewById(R.id.btConfirm2);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Confirm();
            }
        });
        btn_query .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rfidToQuery();
            }
        });
        btn_clean .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clean();
            }});
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barcodeToQuery();
            }
        });
    }

    public class BindDataTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String str2D= "";
        String strSN= "";
        String strType= "";
        String strU= "";
        String errorTip="";
        String msg1="";
        boolean isWritRFID;
        public BindDataTask(String str2D,String strSN,String strType,String strU,boolean isWritRFID){
            this.str2D=str2D;
            this.strSN= strSN;
            this.strType=strType;
            this.strU= strU;
            this.isWritRFID=isWritRFID;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean reuslt=true;
            if(isWritRFID)
                 reuslt= RFIDManage.getInstance().writeData(strSN,strType,(byte)Integer.parseInt(strU),new RFID_Error());

            if(!reuslt) {
                Logs.Info(TAG,"写卡失败");
                errorTip="写卡失败!";
            }else{
                if(NetUtils.isNetworkAvailable(BindQueryActivity.this) && (flag==0)){
                    BindData bindData = new BindData();
                    reuslt= bindData.binDataToSystem(strSN, strType, (byte) Integer.parseInt(strU), str2D);
                    Log.e("TAG", "doInBackground-----绑定数据-----: ------SN = "+strSN+"----类型 = "+strType+"---U数 = "+strU+"---2D = "+str2D );
                    Log.e("TAG", "doInBackground: -------------向服务器绑定 ------"+reuslt );
                    if(!reuslt){
                        Logs.Info(TAG,"访问服务器失败");
                        errorTip="访问服务器";
                    }
                }else {
                       Logs.Info(TAG,"登录时有网络，此时网络不通！");
                       errorTip="访问服务器";
                      reuslt=false;
                }
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(reuslt){
                Logs.Info(TAG,"绑定资产成功");
                msg1="绑定资产成功!";
            }else {
                Logs.Info(TAG,"绑定资产失败");
                msg1="绑定资产失败!";
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
            if(result){
                etSN.setText("");
                etDeviceType.setText("");
                etU.setText("");
                et2D.setText("");
            }
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(BindQueryActivity.this);
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
    private void barcodeToQuery(){
        if(!NetUtils.isNetworkAvailable(BindQueryActivity.this)){
            Toast.makeText(BindQueryActivity.this,"请检测网络!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(flag==1){
            Toast.makeText(BindQueryActivity.this,"当前处于脱机工作中,无法查询!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(flag==2){
            Toast.makeText(BindQueryActivity.this,"请检测服务器设置是否正确!",Toast.LENGTH_SHORT).show();
            return;
        }
        String str1D=et1D.getText().toString();
        if(str1D.isEmpty()) {
            Toast.makeText(this, "一维条码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Logs.Info(TAG,"---------------------一维码查询服务器数据----------------------");
            new  QueryDataTask(false,str1D).execute();
        }
    }
    private void rfidToQuery(){
        Logs.Info(TAG,"---------------------查询RFID----------------------");
        new   QueryDataTask(true,"").execute();
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
                String[] str = RFIDManage.getInstance().readData(new QueryDataTask.RFID_Error());
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
                    etSN_query.setText(data[0]);
                    etUName_query.setText(data[1]);
                    etDeviceType_query.setText(data[2]);
                }else {
                 //   et2D.setText(data[0]);
                    etSN.setText(data[1]);
                    etDeviceType.setText(data[2]);
                    etU.setText(data[3]);
                }
            }
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(BindQueryActivity.this);
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
    public class initTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String msg1="正在初始化...";

        BindQueryActivity bindQueryActivity;
        public  initTask(BindQueryActivity mbindQueryActivity){
            bindQueryActivity=mbindQueryActivity;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result=false;

            result= RFIDManage.getInstance().rfidInit();
            if(!result){
                Logs.Info(TAG,"RFID初始化失败!正在初始化扫描头...");
                msg1="RFID初始化失败!正在初始化扫描头...";
            }else {
                Logs.Info(TAG,"RFID初始化成功!正在初始化扫描头...");
                //msg1="RFID初始化成功!正在初始化扫描头...";
            }
            publishProgress(0);
            result= barcode2DWithSoft.open(bindQueryActivity);
            barcode2DWithSoft.setScanCallback(new BarcodeBack());
            msg1="扫描头初始化成功!";
            Logs.Info(TAG,"扫描头初始化成功");
            return  result;
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
            mypDialog = new ProgressDialog(bindQueryActivity);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在初始化RFID.....");
            Logs.Info(TAG,"正在初始化RFID....");
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

    }
    private void Confirm(){
        str2D=et2D.getText().toString();
        strSN=etSN.getText().toString();
        strType=etDeviceType.getText().toString();
        strU=etU.getText().toString();
        str1D=et1D.getText().toString();
        if(str2D.isEmpty()){
            Toast.makeText(BindQueryActivity.this,"UTag标签码不能为空!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(str1D.isEmpty()){
            Toast.makeText(BindQueryActivity.this,"一维条码不能为空!",Toast.LENGTH_SHORT).show();
            return;
        }
        //没有网络 而且没有选择写卡，就不访问网络
        if(flag==1 && !checkboxWriteRFid.isChecked()){
            if(saveToExcelUtil==null){
                saveToExcelUtil=new SaveToExcelUtil("data.xls");
            }
            if(saveToExcelUtil!=null) {
                saveToExcelUtil.writeToExcel(str1D,str2D);
                /*
                String data = str1D + "," + str2D + "\r\n";
                String path = FileUtils.PATH + "data.txt";
                FileUtils.WriterFile(path, data);
                */
                Toast.makeText(BindQueryActivity.this, "保存文件路径:" + (FileUtils.PATH + "data.xls"), Toast.LENGTH_LONG).show();
            }
            return;
        }
        if(strSN.isEmpty()){
            Toast.makeText(BindQueryActivity.this,"SN不能为空!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(strType.isEmpty()){
            Toast.makeText(BindQueryActivity.this,"类型不能为空!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(strU.isEmpty()){
            Toast.makeText(BindQueryActivity.this,"U数不能为空!",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!checkboxWriteRFid.isChecked()){
            /*
            AlertDialog.Builder dlg = new AlertDialog.Builder(BindQueryActivity.this);
            dlg.setTitle("没有进行写卡是否绑定!");
            dlg.setCancelable(false);
            // dlg .setView(DialogView);//设置自定义对话框的样式，2、自定义布局放入dialog中显示
            dlg .setPositiveButton("确认",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Logs.Info(TAG,"---------------------绑定数据----------------------");
                            new BindDataTask(str2D,strSN,strType,strU,false).execute();
                        }
                    });
            dlg.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            return;
                        }
                    });
            dlg.show();
            */
            Logs.Info(TAG,"---------------------绑定数据----------------------");
            new BindDataTask(str2D,strSN,strType,strU,false).execute();
        }else{
            Logs.Info(TAG,"---------------------绑定数据----------------------");
            new BindDataTask(str2D,strSN,strType,strU,true).execute();
        }
    }
    private void clean(){
        etDeviceType_query.setText("");
        etSN_query.setText("");
        etUName_query.setText("");
    }

    private void setText(){
        if(flag==1 || flag==2){
           // tile_info.setText(msg2);
            if(flag==1)
                tvMsg.setText(msg2);
            if (flag==2)
                tvMsg.setText(msg3);
            tvMsg.setVisibility(View.VISIBLE);
        }else{
           // tile_info.setText(msg1);
            tvMsg.setVisibility(View.GONE);
        }
    }
}



