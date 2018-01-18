package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.adc.decoder.Barcode2DWithSoft;

public class MainActivity extends AppCompatActivity {
    Button btnScan;
    Button btnStop;
    Button btninit;
    TextView tvData;
    String TAG="MainActivity";
    boolean scaning=false;
    Barcode2DWithSoft barcode2DWithSoft=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         btnScan=(Button)findViewById(R.id.btnScan);
         tvData=(TextView)findViewById(R.id.tvData);

         btnStop=(Button)findViewById(R.id.btnStop);
         btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanBarcode();
            }
         });
         btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(barcode2DWithSoft!=null)
                barcode2DWithSoft.stopScan();
            }
        });

        new InitTask().execute();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        if(barcode2DWithSoft!=null){
            barcode2DWithSoft.stopScan();
            barcode2DWithSoft.close();
        }
        super.onDestroy();
        android.os.Process.killProcess(Process.myPid());
    }



    private  class ScanBack implements  Barcode2DWithSoft.ScanCallback{
        @Override
        public void onScanComplete(int i, int length, byte[] bytes) {
            if (length < 1) {
                if (length == -1) {
                    tvData.setText("Scan cancel");
                } else if (length == 0) {
                    tvData.setText("Scan TimeOut");

                } else {
                   Log.i(TAG,"Scan fail");
                }
            }else{
                String barCode = new String(bytes, 0, length);
                tvData.setText(barCode);
                tvData.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                tvData.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));

            }
            scaning=false;
        }
    }

    private void ScanBarcode(){
        if(barcode2DWithSoft!=null && !scaning) {
            Log.i(TAG,"ScanBarcode");
            scaning=true;
            barcode2DWithSoft.scan();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==139){
            if(event.getRepeatCount()==0) {
                ScanBarcode();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==139){
            if(event.getRepeatCount()==0) {
                barcode2DWithSoft.stopScan();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            if(barcode2DWithSoft==null){
                barcode2DWithSoft=Barcode2DWithSoft.getInstance();
            }
            boolean reuslt=false;
            if(barcode2DWithSoft!=null) {
                  reuslt=  barcode2DWithSoft.open(MainActivity.this);
                Log.i(TAG,"open="+reuslt);
                barcode2DWithSoft.setScanCallback(new ScanBack());
            }
            return reuslt;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
             if(result){
                 Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_SHORT).show();
             }else{
                 Toast.makeText(MainActivity.this,"fail",Toast.LENGTH_SHORT).show();
             }
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }

}
