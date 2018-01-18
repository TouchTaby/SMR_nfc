package com.hcpda.smr.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.hcpda.smr.R;
import com.hcpda.smr.util.AppContext;
import com.orhanobut.logger.Logger;

public class BaseActivity extends AppCompatActivity {
   protected android.support.v7.app.ActionBar actionBar ;
    protected AppContext appContext;// 全局Context
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         appContext = (AppContext) getApplication();
         actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
        Logger.e("lihaidelogkuangjia",appContext);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bain, menu);
        MenuItem actionSettings = menu.findItem(R.id.action_settings);
        actionSettings.setTitle("版本号:1.0.0");
        actionSettings.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
