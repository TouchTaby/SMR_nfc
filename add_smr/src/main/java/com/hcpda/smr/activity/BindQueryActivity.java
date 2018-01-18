package com.hcpda.smr.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpda.smr.R;
import com.hcpda.smr.biz.BindData;
import com.hcpda.smr.biz.ISO14443AManager;
import com.hcpda.smr.biz.QueryData;
import com.hcpda.smr.biz.RFIDManage;
import com.hcpda.smr.net.NetUtils;
import com.hcpda.smr.util.Common;
import com.hcpda.smr.util.FileUtils;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.SaveToExcelUtil;
import com.hcpda.smr.util.SoundManager;
import com.hcpda.smr.util.StringUtility;
import com.hcpda.smr.util.Tools;
import com.zebra.adc.decoder.Barcode2DWithSoft;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class BindQueryActivity extends BaseActivity {

    SaveToExcelUtil saveToExcelUtil = null;
    boolean isScaning = false;
    TextView tvMsg;
    TextView tile_info;
    EditText et1D;
    EditText et2D;
    EditText etSN;
    EditText etDeviceType;
    EditText etU;
    Button bt_copy;
    Button btConfirm;
    Button btn_query;
    Button btn_clean;
    Button btnQuery;
    EditText etDeviceType_query;
    EditText etSN_query;
    EditText etUName_query;
    CheckBox checkboxWriteRFid;
    //    Barcode2DWithSoft barcode2DWithSoft;
    Barcode2DWithSoft barcode2DWithSoft;
    String TAG = "BindDataFragment";
    String msg1 = "查询、绑定资产信息";
    String msg2 = "(脱机工作中)";
    String msg3 = "(服务器配置错误，脱机工作中)";
    SoundManager soundManager = null;
    QueryData queryData = new QueryData();
    int flag = 0;
    String[] read_data;
    ISO14443AManager iso14443AManager;//nfc的14443A 管理类
    Tag tag;//nfc 标签对象
    //NFC read
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private IntentFilter ndefIntentFilter;
    SoundPool soundPool;

    class BarcodeBack implements Barcode2DWithSoft.ScanCallback {
        @Override
        public void onScanComplete(int symbology, int length, byte[] data) {
            if (length < 1) {
                if (length == -1) {
                    Logs.Info(TAG, "扫描取消");
                } else if (length == 0) {
                    Logs.Info(TAG, "扫描超时");
                } else {
                    Logs.Info(TAG, "扫描失败");
                }
                soundPool.play(2, 1, 1, 1, 0, 1f);
            } else {
                String barCode = new String(data, 0, length);
                Logs.Info(TAG, "扫描成功symbology=" + symbology + "，数据：" + barCode);

                soundPool.play(1, 1, 1, 1, 0, 1f);
                Log.e("TAG", "onScanComplete: ---------码字是-----" + symbology);
                if (symbology == 115) {
                    et2D.setText(barCode);
                } else {
                    et1D.setText(barCode);
                    et2D.setFocusable(true);
                    barcodeToQuery();
                }
            }
            isScaning = false;
            barcode2DWithSoft.stopScan();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            if (msg.obj.toString().equals("条码不存在!")) {
//                etSN.setText(et1D.getText());
//                etDeviceType.setText("");
//                etU.setText("");
//                et2D.setText("");
//            }
            switch (msg.what) {
                case 1:
                    //全部成功的情况下
                    Toast.makeText(getApplicationContext(), "写标签成功，绑定成功", Toast.LENGTH_SHORT).show();
                    soundPool.play(1, 1, 1, 1, 0, 1f);
                    break;
                case 2:
                    //写标签的时候无网络且写卡成功
                    Toast.makeText(getApplicationContext(), "写标签成功，但绑定失败", Toast.LENGTH_SHORT).show();
                    soundPool.play(1, 1, 1, 1, 0, 1f);
                    break;
                case 3:
                    // 不写标签的时候有网络且绑定成功
                    Toast.makeText(getApplicationContext(), "绑定成功", Toast.LENGTH_SHORT).show();
                    soundPool.play(1, 1, 1, 1, 0, 1f);
                    break;
                case 4:
                    //不写标签但绑定失败
                    Toast.makeText(getApplicationContext(), "绑定失败", Toast.LENGTH_SHORT).show();
                    soundPool.play(2, 1, 1, 1, 0, 1f);
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(), "附近无标签", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bain_query);
        actionBar.setTitle("资产和标签绑定");
        soundManager = SoundManager.getInstance(this);
        barcode2DWithSoft = Barcode2DWithSoft.getInstance();
        initView();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);//nfc 适配器
        if (!nfcAdapter.isEnabled()) {
            Dialog dialog = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告!");
            builder.setMessage("NFC功能未开启,是否前往开启(不开启将无法继续)");
            builder.setPositiveButton("开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //  ACTION_WIRELESS_SETTINGS(即跳入NFC功能开启界面)
                    Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(setnfc);
                }
            }).setNegativeButton("不开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        iso14443AManager = new ISO14443AManager();
        flag = getIntent().getIntExtra("Flag", 0);
        new initTask(this).execute();
        Log.e("TAG", "onCreate:-------------进来 ");
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundPool.load(this, R.raw.suc1, 1);
        soundPool.load(this, R.raw.error, 1);
        queryData.SetOnCallBack(new QueryData.IQueryErrorBack() {
            @Override
            public void errorMessag(String strError) {
//                Message msg = Message.obtain();
//                msg.obj = strError;
//                handler.sendMessage(msg);
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
        Logs.Info(TAG, "---------------------rfid free----------------------");
        RFIDManage.getInstance().rfidFree();
        barcode2DWithSoft.stopScan();
        Logs.Info(TAG, "---------------------barcode close----------------------");
        barcode2DWithSoft.close();
        barcode2DWithSoft.stopHandsFree();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139) {
            if (!isScaning) {
                isScaning = true;
                Log.e("TAG", "onKeyDown: ----------------------------------------");
                barcode2DWithSoft.scan();

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    String str2D;
    String strSN;
    String strType;
    String strU;
    String str1D;

    private void initView() {
        bt_copy = (Button) findViewById(R.id.bt_copy);
        btn_query = (Button) findViewById(R.id.btn_query);
//        btn_clean = (Button) findViewById(R.id.btn_clean);
        btnQuery = (Button) findViewById(R.id.btnQuery);
        etDeviceType_query = (EditText) findViewById(R.id.etDeviceType_query);
        etSN_query = (EditText) findViewById(R.id.etSN_query);
        etUName_query = (EditText) findViewById(R.id.etUName_query);
        checkboxWriteRFid = (CheckBox) findViewById(R.id.checkboxWriteRFid);
        et1D = (EditText) findViewById(R.id.et1D);
        et2D = (EditText) findViewById(R.id.et2D);
        etSN = (EditText) findViewById(R.id.etSN);
        etDeviceType = (EditText) findViewById(R.id.etDeviceType);
        etU = (EditText) findViewById(R.id.etUName);
        tile_info = (TextView) findViewById(R.id.tile_info);
        tvMsg = (TextView) findViewById(R.id.tvMsg);
        // DigSvr2017061601，CE6850，2，E5X016CD  //DIG2017061600001  DigSvr2017061601  R710  2
        // et2D.setText("E5X016CD");
        etSN.setText("");
        etDeviceType.setText("");
        etU.setText("");

        btConfirm = (Button) findViewById(R.id.btConfirm2);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Confirm();
            }
        });
        btn_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rfidToQuery();

            }
        });
//        btn_clean.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                clean();
//            }
//        });
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barcodeToQuery();
            }
        });
        bt_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcode = et1D.getText().toString();
                if (barcode != null) {
                    etSN.setText(barcode);
                } else {
                    Toast.makeText(BindQueryActivity.this, "未扫一维码", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class BindDataTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String str2D = "";
        String strSN = "";
        String strType = "";
        String strU = "";
        String errorTip = "";
        String msg1 = "";
        boolean isWritRFID;
        Message message = new Message();

        public BindDataTask(String str2D, String strSN, String strType, String strU, boolean isWritRFID) {
            this.str2D = str2D;
            this.strSN = strSN;
            this.strType = strType;
            this.strU = strU;
            this.isWritRFID = isWritRFID;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean reuslt = true;
            boolean nfc_result = true;
            String psw = Common.getKey(getApplicationContext());

//                reuslt = RFIDManage.getInstance().writeData(strSN, strType, (byte) Integer.parseInt(strU), new RFID_Error());

            //------------------第一扇区第4块32字节如图所示 写入sn号---------------------------------------------
            //sn
            char[] c_sn = strSN.toCharArray();
            String hex_sn = StringUtility.chars2HexString(c_sn, c_sn.length);
            //间隔
            String jg = StringUtility.chars2HexString("@".toCharArray(), 1);
            char[] c_type = strType.toCharArray();
            //类型
            String type_hex = StringUtility.chars2HexString(c_type, c_type.length);
            String snTypeTemp = hex_sn + jg + type_hex;
            String snType = snTypeTemp;
            for (int k = 0; k < 32 - (snTypeTemp.length() / 2); k++) {
                snType = snType + "00";
            }
            if (isWritRFID) {
                //u数      /* M1卡写入数据长度必须要16个字节，只能补齐长度*/
                String unum = com.rscja.utility.StringUtility.byte2HexString((byte) Integer.parseInt(strU)) + "0000000000000000000000000000000";
                Log.e("TAG", "doInBackground: --------------U数是-----" + StringUtility.hexStringToBytes(unum) + "-----长度是----" + StringUtility.hexStringToBytes(unum).length);
                //写入类型跟sn
                nfc_result = iso14443AManager.writeM1KeyB(tag, 1, 0, Tools.HexString2Bytes(psw), StringUtility.hexStringToBytes(snType.substring(0, 32)));
                if (!nfc_result) {
                    return false;
                }
                Log.e("TAG", "doInBackground: ---------------NFC 1扇 0块写-----" + nfc_result);
                nfc_result = iso14443AManager.writeM1KeyB(tag, 1, 1, Tools.HexString2Bytes(psw), StringUtility.hexStringToBytes(snType.substring(32, 64)));
                Log.e("TAG", "doInBackground: ---------------NFC写 1扇 1块写-----" + nfc_result);
                nfc_result = iso14443AManager.writeM1KeyB(tag, 1, 2, Tools.HexString2Bytes(psw), StringUtility.hexStringToBytes(unum));
                Log.e("TAG", "doInBackground: ---------------NFC写 1扇 2块写-----" + nfc_result);
            } else {
                // 没勾选 不写标签的时候有网络且绑定成功
                if (NetUtils.isNetworkAvailable(BindQueryActivity.this) && (flag == 0)) {
                    boolean no_write_result = false;
                    BindData bindData = new BindData();
                    no_write_result = bindData.binDataToSystem(strSN, strType, (byte) Integer.parseInt(strU), str2D);
                    if (no_write_result) {
                        //绑定成功
                        message.what = 3;
                        handler.sendMessage(message);
                    } else {
                        //绑定失败
                        message.what = 4;
                        handler.sendMessage(message);
                    }
                }
            }
            if (nfc_result) {
                //写标签成功
                if (!(NetUtils.isNetworkAvailable(BindQueryActivity.this)) && (flag == 1)) {
                    // 没有网络的时候写卡成功
                    message.what = 2;
                    handler.sendMessage(message);
                }
                if (NetUtils.isNetworkAvailable(BindQueryActivity.this) && (flag == 0)) {
                    //写标签的时候有网络且绑定成功
                    BindData bindData = new BindData();
                    nfc_result = bindData.binDataToSystem(strSN, strType, (byte) Integer.parseInt(strU), str2D);
                    Log.e("TAG", "doInBackground: ------SN = " + strSN + "----类型 = " + snType + "---U数 = " + strU + "---2D = " + str2D);
                    Log.e("TAG", "doInBackground: -------------向服务器绑定 ------" + nfc_result);
                    if (nfc_result) {
                        //有网络且绑定成功
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {
                        Logs.Info(TAG, "访问服务器失败");
                        errorTip = "访问服务器";
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                } else {
                    Logs.Info(TAG, "登录时有网络，此时网络不通！");
                    errorTip = "访问服务器";
//                    nfc_result = false; // 这里要重置，下次再读不会条件紊乱
                }
            } else {
                //写标签失败
                Logs.Info(TAG, "写卡失败");
                errorTip = "写卡失败!";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (nfc_result) {
                Logs.Info(TAG, "绑定资产成功");
                msg1 = "绑定资产成功!";
            } else {
                Logs.Info(TAG, "绑定资产失败");
                msg1 = "绑定资产失败!";
            }
            publishProgress(0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return nfc_result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Message msg = new Message();
            if (result) {
                etSN.setText("");
                etDeviceType.setText("");
                etU.setText("");
                et2D.setText("");
            } else {
                msg.what = 5;
                handler.sendMessage(msg);
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
            if (values[0] == 0) {
                mypDialog.setMessage(msg1);
            }
        }

        class RFID_Error implements RFIDManage.IErrorMessag {
            @Override
            public void getError(String msg) {
                publishProgress(0);
                msg1 = msg;
            }
        }
    }

    private void barcodeToQuery() {
        if (!NetUtils.isNetworkAvailable(BindQueryActivity.this)) {
            Toast.makeText(BindQueryActivity.this, "请检测网络!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (flag == 1) {
            Toast.makeText(BindQueryActivity.this, "当前处于脱机工作中,无法查询!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (flag == 2) {
            Toast.makeText(BindQueryActivity.this, "请检测服务器设置是否正确!", Toast.LENGTH_SHORT).show();
            return;
        }
        String str1D = et1D.getText().toString();
        if (str1D.isEmpty()) {
            Toast.makeText(this, "一维条码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Logs.Info(TAG, "---------------------一维码查询服务器数据----------------------");
            new QueryDataTask(false, str1D).execute();
        }
    }

    private void rfidToQuery() {
        //进入读标签异步
//        Logs.Info(TAG,"---------------------查询RFID----------------------");
//        new   QueryDataTask(true,"").execute();
//        showDialog();
        if (read_data[0].equals("")) {
            Toast.makeText(getApplicationContext(), "/请重新接触标签", Toast.LENGTH_SHORT).show();
            soundPool.play(2, 1, 1, 1, 0, 1f);
        } else {
            soundPool.play(1, 1, 1, 1, 0, 1f);
        }
        etSN_query.setText(read_data[0]);
        etUName_query.setText(read_data[1]);
        etDeviceType_query.setText(read_data[2]);


    }


    //读标签异步
    public class QueryDataTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String msg1 = "";
        String[] data = null;
        boolean isRFID = false;
        boolean isResult = false;
        String barcode1D = "";

        public QueryDataTask(boolean isRFID, String barcode) {
            this.isRFID = isRFID;
            barcode1D = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result = false;
            if (isRFID) {

////                String[] str = RFIDManage.getInstance().readData(new QueryDataTask.RFID_Error());
//                String psw = "FFFFFFFFFFFF";
//                byte[] nfc_snType1 = iso14443AManager.readM1KeyB(tag,1,0,Tools.HexString2Bytes(psw));//第四块
//                byte[] nfc_snType2 = iso14443AManager.readM1KeyB(tag, 1, 1, Tools.HexString2Bytes(psw));//第五块
//               String snType1 = new String(nfc_snType1);
//               String snType2 = new String(nfc_snType2);
//                String snType=snType1+snType2;
//                int index=snType.indexOf("00");
//                String strData=snType;
//                if(index>0) {
//                    if(index%2!=0)
//                        index=index+1;
//                    strData = snType.substring(0, index);
//                }
//                char[] charSNType= StringUtility.hexString2Chars(strData);
//                String snTypeTemp=new String(charSNType);
//                String sn=snTypeTemp;
//                String type="";
//                if(snTypeTemp.contains("@")) {
//                    sn = snTypeTemp.split("@")[0];
//                    type = snTypeTemp.split("@")[1];
//                }
//                String u_num="-1";//u 数量
//                byte[] nfc_u_num = iso14443AManager.readM1KeyB(tag, 1, 2, Tools.HexString2Bytes(psw));//第六块
//                u_num= String.valueOf(Byte.parseByte(Tools.Bytes2HexString(nfc_u_num,nfc_u_num.length).substring(0,2),16));
//                String[] str=new String[3];
//                data[0]=sn;
//                data[1]=u_num;
//                data[2]=type;
//                Log.e("TAG", "doInBackground: ---sn ="+data[0]+"u数 = "+data[1]+"类型 = "+data[2] );
//                Log.e("TAG", "doInBackground:-------------数据内容------ "+Tools.Bytes2HexString(nfc_snType1,nfc_snType1.length));
//                if (str != null && str.length > 0) {
//                    isResult=true;
//                    data=str;
//                    result= true;
//                }
//                else {
//                    result= false;
//                }
            }
//            else{
            String[] data = queryData.queryAssetsData(barcode1D);
            if (data != null && data.length >= 4) {
                isResult = true;
                this.data = data;
                result = true;
            } else {
                result = false;
            }
//            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if(result){
//                msg1="查询数据成功!";
//            }else {
//                msg1="查询失败!";
//            }
            publishProgress(0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                if (isRFID) {
                    etSN_query.setText(data[0]);
                    etUName_query.setText(data[1]);
                    etDeviceType_query.setText(data[2]);
                } else {
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
            if (values[0] == 0) {
                mypDialog.setMessage(msg1);
            }
        }

        class RFID_Error implements RFIDManage.IErrorMessag {
            @Override
            public void getError(String msg) {
                publishProgress(0);
                msg1 = msg;
            }
        }
    }

    //当设备识别到nfc 会发出一个 intent
    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            onMyNewIntent();

        }

    }

    public void showDialog() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("读卡中，请勿拿开");
        dialog.show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);//让他显示0.5秒后，取消ProgressDialog
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        t.start();
    }

    /**
     * 因14443A-Ultra light前2块(0-2)有保护不能写，
     * 故，我们的“SN+类型(共32个字节，中间用@符分隔)”从第3块开始写，直到第10块，
     * “占用U数(共１个字节）”写第11块的第1个字节。
     */
    public void onMyNewIntent() {
//          Log.e("TAG", "onNewIntent: -------卡号是---" + Tools.Bytes2HexString(tag.getId(), tag.getId().length));

        if (Common.getTagType(this)) {
            //如果是M1 卡
            String psw = "FFFFFFFFFFFF";
            String snType1 = "";
            String snType2 = "";
            /*要同时多次调用读卡写卡必须要先关闭 MifareClassic.close 下次调用就没问题了，不然会出现关闭技术的报错*/
            byte[] nfc_snType1 = new byte[0];//第四块
            try {
                nfc_snType1 = iso14443AManager.readM1KeyB(tag, 1, 0, Tools.HexString2Bytes(psw));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] nfc_snType2 = new byte[0];//第五块
            try {
                nfc_snType2 = iso14443AManager.readM1KeyB(tag, 1, 1, Tools.HexString2Bytes(psw));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (nfc_snType1 != null) {
                snType1 = new String(nfc_snType1);
            }
            if (nfc_snType2 != null) {
                try {
                    snType2 = new String(nfc_snType2, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            String snType = snType1 + snType2;
            int index = snType.indexOf("00");
            String strData = snType;
            if (index > 0) {
                if (index % 2 != 0)
                    index = index + 1;
                strData = snType.substring(0, index);
            }
            String sn = strData;
            String type = "";
            if (snType.contains("@")) {
                try {
                    sn = snType.split("@")[0];
                    type = snType.split("@")[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            String u_num = null;//u 数量
            byte[] nfc_u_num = new byte[0];//第六块
            try {
                nfc_u_num = iso14443AManager.readM1KeyB(tag, 1, 2, Tools.HexString2Bytes(psw));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nfc_u_num != null) {
                u_num = String.valueOf(Byte.parseByte(Tools.Bytes2HexString(nfc_u_num, nfc_u_num.length).substring(0, 2), 16)).trim();
            }
            read_data = new String[4];
            read_data[0] = sn;
            read_data[1] = u_num;
            read_data[2] = type;
        } else {
//            //否则是Ultralight 卡
//            String ultralight_data = iso14443AManager.readUltralightTag(tag, 4);
//            Log.e(TAG, "onMyNewIntent: gad help----------" + ultralight_data);
            Toast.makeText(this, "Ultralight 未开放!", Toast.LENGTH_SHORT).show();

        }


//            Log.e("TAG", "doInBackground: ---sn ="+read_data[0]+"---------u数 = "+read_data[1]+"-------------类型 = "+read_data[2] );
//            Log.e("TAG", "doInBackground:-------------数据内容------ "+Tools.Bytes2HexString(nfc_snType1,nfc_snType1.length));


    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);
        //检测该设备是否支持NFC功能，以及NFC功能是否开启
        Log.d("debug", "onResume()....");

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        getNfcMessage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (barcode2DWithSoft != null) {
            barcode2DWithSoft.close();//屏幕熄灭
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new initTask(this).execute();//屏幕重新启动的时候初始化读头
    }

    private void getNfcMessage() {
        //以下几步都是在为enableForegroundDispatch做准备
        {
            //将被调用的Intent，用于重复被Intent触发后将要执行的跳转
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            //设定要过滤的标签动作，这里只接收ACTION_NDEF_DISCOVERED类型
            ndefIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            ndefIntentFilter.addCategory("*/*");
            mFilters = new IntentFilter[]{ndefIntentFilter};// 过滤器
            //设置参数TechLists
            mTechLists = new String[][]{new String[]{
                    NfcA.class.getName()},// 允许扫描的标签类型
                    new String[]{NfcF.class.getName()},
                    new String[]{NfcB.class.getName()},
                    new String[]{NfcV.class.getName()},
                    new String[]{NfcBarcode.class.getName()},
                    new String[]{MifareUltralight.class.getName()},
                    new String[]{MifareClassic.class.getName()},
                    new String[]{Ndef.class.getName()},
            };
        }

        //解析接收到的数据，主要是函数readTag
        //扫描到标签时，系统会主动将标签信息生成一个tag对象，然后封装到一个intent中。action也是主动设定的。
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(this.getIntent().getAction())) {
            Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            final byte[] id = tag.getId();

        }
    }

    public class initTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String msg1 = "正在初始化...";

        BindQueryActivity bindQueryActivity;

        public initTask(BindQueryActivity mbindQueryActivity) {
            bindQueryActivity = mbindQueryActivity;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result = false;
//---------------------------------------------------------------
//            result= RFIDManage.getInstance().rfidInit();
//            if(!result){
//                Logs.Info(TAG,"RFID初始化失败!正在初始化扫描头...");
//                msg1="RFID初始化失败!正在初始化扫描头...";
//            }else {
//                Logs.Info(TAG,"RFID初始化成功!正在初始化扫描头...");
//                //msg1="RFID初始化成功!正在初始化扫描头...";
//            }
//            publishProgress(0);
//            Log.e("TAG", "doInBackground:----------对象2D "+barcode2DWithSoft );
//-----------------------------------------------------------------------------------------------不用HF要去掉
            result = barcode2DWithSoft.open(bindQueryActivity);
            Log.e("TAG", "doInBackground:--------初始化--- " + result);
            barcode2DWithSoft.setScanCallback(new BarcodeBack());
            msg1 = "扫描头初始化成功!";
            Logs.Info(TAG, "扫描头初始化成功");
            return result;
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
            Logs.Info(TAG, "正在初始化RFID....");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 0) {
                mypDialog.setMessage(msg1);
            }
        }
    }

    // 点击绑定处理
    private void Confirm() {
        str2D = et2D.getText().toString();
        strSN = etSN.getText().toString();
        strType = etDeviceType.getText().toString();
        strU = etU.getText().toString();
        str1D = et1D.getText().toString();
        if (str2D.isEmpty()) {
            Toast.makeText(BindQueryActivity.this, "UTag标签码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (str1D.isEmpty()) {
            Toast.makeText(BindQueryActivity.this, "一维条码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        //没有网络 而且没有选择写卡，就不访问网络
        if (flag == 1 && !checkboxWriteRFid.isChecked()) {
            if (saveToExcelUtil == null) {
                saveToExcelUtil = new SaveToExcelUtil("data.xls");
            }
            if (saveToExcelUtil != null) {
                saveToExcelUtil.writeToExcel(str1D, str2D);
                /*
                String data = str1D + "," + str2D + "\r\n";
                String path = FileUtils.PATH + "data.txt";
                FileUtils.WriterFile(path, data);
                */
                Toast.makeText(BindQueryActivity.this, "保存文件路径:" + (FileUtils.PATH + "data.xls"), Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (strSN.isEmpty()) {
            Toast.makeText(BindQueryActivity.this, "SN不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (strType.isEmpty()) {
            Toast.makeText(BindQueryActivity.this, "类型不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (strU.isEmpty()) {
            Toast.makeText(BindQueryActivity.this, "U数不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkboxWriteRFid.isChecked()) {
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
            Logs.Info(TAG, "---------------------不写标签绑定数据----------------------");
            new BindDataTask(str2D, strSN, strType, strU, false).execute();
        } else {
            Logs.Info(TAG, "---------------------写标签绑定数据----------------------");
            new BindDataTask(str2D, strSN, strType, strU, true).execute();
        }
    }

    private void clean() {
        for (int i = 0; i < read_data.length; i++) {
            read_data[i] = ""; //清除原来保存的信息
        }
        etDeviceType_query.setText("");
        etSN_query.setText("");
        etUName_query.setText("");
    }

    private void setText() {
        if (flag == 1 || flag == 2) {
            // tile_info.setText(msg2);
            if (flag == 1)
                tvMsg.setText(msg2);
            if (flag == 2)
                tvMsg.setText(msg3);
            tvMsg.setVisibility(View.VISIBLE);
        } else {
            // tile_info.setText(msg1);
            tvMsg.setVisibility(View.GONE);
        }
    }
}



