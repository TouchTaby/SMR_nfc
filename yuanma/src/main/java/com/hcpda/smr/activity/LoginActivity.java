package com.hcpda.smr.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpda.smr.BaseActivity;
import com.hcpda.smr.R;
import com.hcpda.smr.biz.ITcpBack;
import com.hcpda.smr.biz.Login;
import com.hcpda.smr.net.NetUtils;
import com.hcpda.smr.net.TcpManage;
import com.hcpda.smr.util.Common;
import com.hcpda.smr.util.Logs;
import com.hcpda.smr.util.StringUtility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;


public class LoginActivity extends Activity {
     Button bt_login;
     EditText et_name_login;
     EditText et_psw_login;
     CheckBox cbUserName;
     String TAG="LoginActivity";
     String name="";
     String passWord="";
     Button tvSet;
     Login login=new Login();

    Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj.toString().equals("连接失败")){
                login(2);
            }else {
                Toast.makeText(LoginActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        TcpManage.getInstance().startTcpService();
        login.SetOnCallBack(
        new Login.ILoginErrorBack() {
            @Override
            public void errorMessag(String strError) {
                Message msg=Message.obtain();
                msg.obj=strError;
                handler.sendMessage(msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        TcpManage.getInstance().close();
        System.exit(0);
        super.onDestroy();
    }

    private void initView() {
        Common.port=Common.getPort(this);
        Common.ip=Common.getIP(this);


        tvSet=(Button) findViewById(R.id.tvSet);
        et_name_login= (EditText) findViewById(R.id.et_name_login);
        et_psw_login= (EditText) findViewById(R.id.et_psw_login);
        cbUserName= (CheckBox) findViewById(R.id.cbUserName);
        cbUserName.setChecked(true);

        et_name_login.setText(Common.getLoginName(this));
        et_psw_login.setText(Common.getLoginPassWord(this));

        bt_login = (Button) findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=et_name_login.getText().toString();
                passWord=et_psw_login.getText().toString();
                if(name.isEmpty() || passWord.isEmpty()){
                    Toast.makeText(LoginActivity.this,"用户名和密码不能为空!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!NetUtils.isNetworkAvailable(LoginActivity.this)){
                  //   Toast.makeText(LoginActivity.this,"请检测网络!",Toast.LENGTH_SHORT).show();
                   //   return;
                    login(1);
                    return;
                 }
               new LoinTask().execute();
            }
        });
        tvSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logs.Info(TAG,"进入设置界面");
                Intent intent=new Intent(LoginActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }


    public class LoinTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            if(name.isEmpty() || passWord.isEmpty())
                return false;
            return   login.loginSystem(name,passWord);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if(!result){
               // Toast.makeText(LoginActivity.this,"登录失败!",Toast.LENGTH_SHORT).show();
            }else{
                login(0);
//                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
//                LoginActivity.this.startActivity(intent);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(LoginActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("登录，正在验证...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

    }


    private void login(int flag){
        if(cbUserName.isChecked()) {
            Common.setLoginName(LoginActivity.this, name);
            Common.setLoginPassWord(LoginActivity.this, passWord);
        }else{
            Common.setLoginName(LoginActivity.this, "");
            Common.setLoginPassWord(LoginActivity.this, "");
        }
        Intent intent=new Intent(LoginActivity.this,BindQueryActivity.class);
        intent.putExtra("Flag",flag);
        startActivity(intent);
    }


}
