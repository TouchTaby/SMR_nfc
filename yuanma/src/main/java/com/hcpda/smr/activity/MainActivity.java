package com.hcpda.smr.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hcpda.smr.BaseActivity;
import com.hcpda.smr.R;

import com.hcpda.smr.activity.QueryFragment;

import com.hcpda.smr.biz.Barcode2D;
import com.hcpda.smr.biz.RFIDManage;
import com.hcpda.smr.util.Common;
import com.zebra.adc.decoder.Barcode2DWithSoft;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private ImageView ivBind;
    private ImageView ivQuery;
    private LinearLayout llAssetsManage=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        actionBar.setTitle("数码人自动识别终端");
        ivBind=(ImageView)findViewById(R.id.ivBind);
        ivQuery=(ImageView)findViewById(R.id.ivQuery);
        llAssetsManage=(LinearLayout)findViewById(R.id.llAssetsManage);
        llAssetsManage.setOnClickListener(this);
        ivBind.setOnClickListener(this);
        ivQuery.setOnClickListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case  R.id.llAssetsManage:
                 intent=new Intent(MainActivity.this,BindQueryActivity.class);
                startActivity(intent);
                break;
            case  R.id.ivBind:
                  intent=new Intent(MainActivity.this,BindQueryActivity.class);
                startActivity(intent);
                break;
          //  case R.id.llAssetsManage:
             //   Intent intent2=new Intent(MainActivity.this,QueryFragment.class);
            ///    startActivity(intent2);
            //    break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bain, menu);
        MenuItem actionSettings = menu.findItem(R.id.action_settings);
        actionSettings.setVisible(true);
        return true;
    }

}
