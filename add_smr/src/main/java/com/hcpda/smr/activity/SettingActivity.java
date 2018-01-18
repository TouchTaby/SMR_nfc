package com.hcpda.smr.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.hcpda.smr.R;
import com.hcpda.smr.net.TcpManage;
import com.hcpda.smr.util.Common;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;

public class SettingActivity extends BaseActivity implements View.OnClickListener{

    Button bbtn_setting ;
    Button btn_settingKey;
    EditText etPort ;
    EditText etIP ;
    EditText etKey;
    EditText ettimeOut;
    Button btn_settingTimeOut;
    String TAG="SettingActivity";
    RadioButton rbAKEY;
    RadioButton rbBKEY;
    CheckBox cbLog;
    /**
     * *********
     */
    Button bt_set_tag_type;
    RadioButton rb_m1_tag;
    RadioButton rb_ultralight_tag;
    /**
     * ******************
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.Info(TAG,"onCreate");
        setContentView(R.layout.activity_setting);
           actionBar.setTitle("设置");
          bbtn_setting =(Button)findViewById(R.id.btn_setting);
        btn_settingKey=(Button)findViewById(R.id.btn_settingKey);
          btn_settingTimeOut=(Button)findViewById(R.id.btn_settingTimeOut);
          etPort=(EditText)findViewById(R.id.etPort);
          etIP=(EditText)findViewById(R.id.etIP);
        etKey=(EditText)findViewById(R.id.etKey);
        btn_settingKey.setOnClickListener(this);
         ettimeOut=(EditText)findViewById(R.id.ettimeOut);
        rbAKEY=(RadioButton)findViewById(R.id.rbAKEY);
        rbBKEY=(RadioButton)findViewById(R.id.rbBKEY);
        cbLog=(CheckBox) findViewById(R.id.cbLog);

        bt_set_tag_type = (Button) findViewById(R.id.bt_set_tag_type);
        bt_set_tag_type.setOnClickListener(this);
        rb_m1_tag = (RadioButton) findViewById(R.id.rb_m1_tag);
        rb_ultralight_tag = (RadioButton) findViewById(R.id.rb_ultralight_tag);

        cbLog.setOnClickListener(this);
        rbAKEY.setOnClickListener(this);
        rbBKEY.setOnClickListener(this);
          etIP.setText(Common.getIP(this));
          etPort.setText(Common.getPort(this)+"");
          ettimeOut.setText(Common.getTimeOut(this)+"");
           bbtn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIP();
              // finish();
            }
        });
        btn_settingTimeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimeOut();
                //finish();
            }
        });
        init();

    }

    private  void setTimeOut(){
        int timeOut= Integer.parseInt(ettimeOut .getText().toString());
        if(timeOut<=0){
            Toast.makeText(SettingActivity.this,"超时时间必须大于0",Toast.LENGTH_SHORT).show();
            return;
        }
        Common.setTimeOut(SettingActivity.this,timeOut);
        Toast.makeText(SettingActivity.this,"设置成功!",Toast.LENGTH_SHORT).show();
    }

    private  void setIP(){
        Object port=etPort .getText();
        Object ip= etIP .getText();
        if(port==null || ip==null){
            Toast.makeText(SettingActivity.this,"端口和IP不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if(port.toString().length()<=0 || etIP.toString().length()<=0){
            Toast.makeText(SettingActivity.this,"端口和IP不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isIP(ip.toString())){
            Toast.makeText(SettingActivity.this,"无效的IP地址",Toast.LENGTH_SHORT).show();
            return;
        }
        Common.setIP(SettingActivity.this,ip.toString());
        Common.setPort(SettingActivity.this,Integer.parseInt( port.toString()));

        Common.port=Common.getPort(SettingActivity.this);
        Common.ip=Common.getIP(SettingActivity.this);

        Toast.makeText(SettingActivity.this,"设置成功!",Toast.LENGTH_SHORT).show();
        TcpManage.getInstance().connect();
    }

    private boolean isIP(String ip){
        if(ip.contains(".")){
            int count=0;
            for(int k=0;k<ip.length();k++){
                if(ip.substring(k,k+1).equals(".")){
                    count++;
                }
            }
            if(count!=3)
                return false;

            String[] str=ip.split("\\.");
            if(str==null)
                return false;

            if(str.length!=4){
                return false;
            }
            if(str[0].isEmpty()||str[1].isEmpty()||str[2].isEmpty()||str[3].isEmpty()){
                return  false;
            }
            int str1 =0;
            int str2 =0;
            int str3 = 0;
            int str4 =0;
            try {
                  str1 = Integer.parseInt(str[0]);
                  str2 = Integer.parseInt(str[1]);
                  str3 = Integer.parseInt(str[2]);
                  str4 = Integer.parseInt(str[3]);
            }catch (Exception ex){
                return  false;
            }

            if(255<str1 || str1<0){
                return  false;
            }
            if(255<str2 || str2<0){
                return  false;
            }
            if(255<str3 || str3<0){
                return  false;
            }
            if(255<str4 || str4<0){
                return  false;
            }
           return  true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rbAKEY:
                Common.setIsAKey(this, true);
                break;
            case R.id.rbBKEY:
                Common.setIsAKey(this, false);
                break;
            case R.id.btn_settingKey:
//                RFIDManage.getInstance().writeData("sss","22",(byte)Integer.parseInt("2"),null);
//                RFIDManage.getInstance().readData(null);

                String key=etKey.getText().toString();
                if(key.isEmpty()){
                    Toast.makeText(this,"秘钥不能为空!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(key.length()!=12){
                    Toast.makeText(this,"请输入12位秘钥!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!StringUtility.isHexNumber(key)){
                    Toast.makeText(this,"秘钥必须是十六进制数据!",Toast.LENGTH_SHORT).show();
                    return;
                }
                Common.setKey(this,key.toUpperCase());
                Toast.makeText(SettingActivity.this,"设置成功!",Toast.LENGTH_SHORT).show();
                break;
            case  R.id.cbLog:
                Common.isLOG=cbLog.isChecked();
                break;
            case R.id.bt_set_tag_type:
                Common.setKeyType(this, rb_m1_tag.isChecked());
                Toast.makeText(SettingActivity.this,"设置成功!",Toast.LENGTH_SHORT).show();
                break;

        }
    }
    private void init(){
         if(Common.getIsAKey(this)){
             rbAKEY.setChecked(true);
         }else{
             rbBKEY.setChecked(true);
         }
        if (Common.getTagType(this)) {
             rb_m1_tag.setChecked(true);
        }else {
             rb_ultralight_tag.setChecked(true);
        }
        etKey.setText(Common.getKey(this));
        cbLog.setChecked(Common.isLOG);
    }




}

