package com.example.barcode2ds.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.barcode2ds.MainActivity;
import com.example.barcode2ds.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.utility.StringUtility;
import com.zebra.adc.decoder.Barcode2DWithSoft;


public class ErDSoftScanFragment extends KeyDwonFragment {


//    @ViewInject(R.id.btn_Start)
    private Button btn_Start;
    @ViewInject(R.id.btn_Clear)
    private Button btn_Clear;
    @ViewInject(R.id.tv_result)
    private TextView tv_Result;
    @ViewInject(R.id.tv_scan_count)
    private TextView tv_scan_count;
    @ViewInject(R.id.tv_succ_count)
    private TextView tv_succ_count;
    @ViewInject(R.id.tv_fail_count)
    private TextView tv_fail_count;
    @ViewInject(R.id.tv_error_count)
    private TextView tv_error_count;
    @ViewInject(R.id.tv_succ_rate)
    private TextView tv_succ_rate;
    @ViewInject(R.id.tv_fail_rate)
    private TextView tv_fail_rate;
    @ViewInject(R.id.tv_error_rate)
    private TextView tv_error_rate;
    @ViewInject(R.id.cbContinuous)
    private CheckBox cbContinuous;
    @ViewInject(R.id.cbCompare)
    private CheckBox cbCompare;
    @ViewInject(R.id.et_between)
    private EditText et_between;

    private Thread thread;
    @ViewInject(R.id.svResult)
    private ScrollView svResult;
    @ViewInject(R.id.et_init_barcode)
    private EditText et_init_barcode;
    private String init_barcode;
    int sussCount = 0;
    int failCount = 0;
    int errorCount = 0;

    private boolean threadStop = true;

    private boolean isCurrFrag = false;


    private MainActivity mContext;


    public Barcode2DWithSoft.ScanCallback mScanCallback = new Barcode2DWithSoft.ScanCallback() {
        @Override
        public void onScanComplete(int i, int length, byte[] data) {

            Log.i("ErDSoftScanFragment","onScanComplete() i="+i);

            if(!isCurrFrag)
            {
                return;
            }

            String strData = "";

            if ((sussCount + errorCount + failCount) % 1000 == 0) {
                tv_Result.setText("");
            }

            if (length < 1) {

                failCount += 1;
                strData = getString(R.string.yid_msg_scan_fail) + "\n";

                tv_Result.append(strData);


                return;
            }

            mContext.mReader.stopScan();

            String barCode = new String(data);

            if (cbCompare.isChecked()) {

                init_barcode = et_init_barcode.getText().toString();

                if (StringUtility.isEmpty(init_barcode))// 设置初始条码
                {
                    et_init_barcode.setText(barCode);

                    init_barcode = et_init_barcode.getText().toString();
                }

                if (init_barcode.equals(barCode)) {
                    sussCount += 1;

                } else {
                    errorCount += 1;


                    strData = getString(R.string.yid_msg_scan_error) + "：";

                }
            } else {
                sussCount += 1;
                init_barcode = "";
                et_init_barcode.setText("");

            }

            strData += barCode + "\n";

            tv_Result.append(strData);

            mContext.scrollToBottom(svResult, tv_Result);
            stat();
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (MainActivity) getActivity();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_er_dsoft_scan,
                container, false);
        ViewUtils.inject(this, v);

        btn_Start= (Button) v.findViewById(R.id.btn_Start);
        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDecode();
            }
        });

        Log.i("ErDSoftScanFragment", "onCreateView() threadStop=" + threadStop);
        return v;

    }

//    @OnClick(R.id.btn_Start)
//    public void btn_Start_onClick(View v) {
//        doDecode();
//    }

    @OnClick(R.id.btn_Clear)
    public void btn_Clear_onClick(View v) {
        clear();
    }


    private void doDecode() {

        if (mContext.mReader != null) {

            mContext.mReader.setScanCallback(mScanCallback);
        }

        Log.i("ErDSoftScanFragment","doDecode() threadStop="+threadStop);

        if (threadStop) {

            boolean bContinuous = false;

            int iBetween = 0;

            bContinuous = cbContinuous.isChecked();
            if (bContinuous) {
                btn_Start.setText(getString(R.string.title_stop));
                threadStop = false;

                String strBetween = et_between.getText().toString();
                if (StringUtility.isEmpty(strBetween)) {

                } else {
                    iBetween = StringUtility.string2Int(strBetween, 0);// 毫秒
                }

                btn_Clear.setEnabled(false);
                cbContinuous.setEnabled(false);
            }
            init_barcode = et_init_barcode.getText().toString();

            thread = new DecodeThread(bContinuous, iBetween);
            thread.start();

        } else {
            btn_Start.setText(getString(R.string.title_scan));
            threadStop = true;
            cbContinuous.setEnabled(true);
            btn_Clear.setEnabled(true);
        }

    }

    private void stat() {
        int total = sussCount + failCount + errorCount;

        if (total > 0) {
            tv_scan_count.setText(String.valueOf(total));
            tv_succ_count.setText(String.valueOf(sussCount));
            tv_fail_count.setText(String.valueOf(failCount));
            tv_error_count.setText(String.valueOf(errorCount));

            tv_error_rate.setText(String.valueOf(errorCount * 1000 / total)
                    + "‰");
            tv_succ_rate
                    .setText(String.valueOf(sussCount * 1000 / total) + "‰");
            tv_fail_rate
                    .setText(String.valueOf(failCount * 1000 / total) + "‰");
        }
    }

    private void clear() {
        tv_Result.setText("");

        int total = 0;
        sussCount = 0;
        failCount = 0;
        errorCount = 0;

        et_init_barcode.setText("");
        tv_scan_count.setText(String.valueOf(total));
        tv_succ_count.setText(String.valueOf(sussCount));
        tv_fail_count.setText(String.valueOf(failCount));
        tv_error_count.setText(String.valueOf(errorCount));

        tv_error_rate.setText(String.valueOf(0));
        tv_succ_rate.setText(String.valueOf(0));
        tv_fail_rate.setText(String.valueOf(0));

        btn_Start.setText(getString(R.string.title_scan));
        threadStop = true;

    }

    private class DecodeThread extends Thread {
        private boolean isContinuous = false;
        private long sleepTime = 1000;


        public DecodeThread(boolean isContinuous, int sleep) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
        }

        @Override
        public void run() {
            super.run();

            do {

                mContext.mReader.scan();

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (isContinuous && !threadStop);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        isCurrFrag=false;

        threadStop = true;
        cbContinuous.setEnabled(true);
        btn_Start.setText(getString(R.string.title_scan));

        btn_Clear.setEnabled(true);

        if (mContext.mReader != null) {

            mContext.mReader.stopScan();


        }

    }



    @Override
    public void onResume() {
        super.onResume();

        isCurrFrag=true;


    }


    @Override
    public void myOnKeyDwon() {
        doDecode();
    }
}
