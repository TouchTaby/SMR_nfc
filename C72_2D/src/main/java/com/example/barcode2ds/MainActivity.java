package com.example.barcode2ds;

import com.example.barcode2ds.fragment.ErDSoftScanFragment;
import com.example.barcode2ds.fragment.ErDSoftSetFragment;
import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;
import com.zebra.adc.decoder.Barcode2DWithSoft;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

/**
 * 二维软解码使用demo
 * 
 * 1、使用前请确认您的机器已安装此模块。 2、要正常使用模块需要在\libs\armeabi\目录放置libDeviceAPI.so文件，同时在\libs\目录下放置libDeviceAPIver20160627.jar文件。
 * 3、在操作设备前需要调用 open()打开设备，使用完后调用 close() 关闭设备
 * 
 * 
 * 更多函数的使用方法请查看API说明文档
 * 
 * @author wushengjun
 * 更新于2016月8月8日
 */
public class MainActivity extends BaseTabFragmentActivity {

	private final static String TAG = "MainActivity";

	public Barcode2DWithSoft mReader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViewPageData();
		initViewPager();
		initTabs();

		try {
			mReader = Barcode2DWithSoft.getInstance();
		} catch (Exception ex) {

			Toast.makeText(MainActivity.this, ex.getMessage(),
					Toast.LENGTH_SHORT).show();

			return;
		}

		
	}

	@Override
	protected void initViewPageData() {
		lstFrg.add(new ErDSoftScanFragment());
		lstFrg.add(new ErDSoftSetFragment());

		lstTitles.add(getString(R.string.er_dsoft_tab_scan));
		lstTitles.add(getString(R.string.er_dsoft_tab_set));
	}
	
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		if (mReader != null) {
			mReader.close();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		
		if (mReader != null) {
			new InitTask().execute();
		}
	}

	@Override
	protected void onDestroy() {

		
		super.onDestroy();
	}

	/**
	 * 设备上电异步类
	 * 
	 * @author liuruifeng
	 */
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;

		@Override
		protected Boolean doInBackground(String... params) {
			
			
			boolean result = false;

            if (mReader != null) {
                result = mReader.open(MainActivity.this);

                if (result) {
                    mReader.setParameter(324, 1);
                    mReader.setParameter(300, 0); // Snapshot Aiming
                    mReader.setParameter(361, 0); // Image Capture Illumination
                }
            }
            return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (!result) {
				
				
				
				Toast.makeText(MainActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
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

	/**
	 * 验证十六进制输入是否正确
	 * 
	 * @param str
	 * @return
	 */
	public boolean vailHexInput(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}

		// 长度必须是偶数
		if (str.length() % 2 == 0) {
			return StringUtility.isHexNumberRex(str);
		}

		return false;
	}
	/**
	 * 将ScrollView根据内部控件高度定位到底端
	 * 
	 * @param scroll
	 * @param inner
	 */
	public void scrollToBottom(final View scroll, final View inner) {

		Handler mHandler = new Handler();

		mHandler.post(new Runnable() {
			public void run() {
				if (scroll == null || inner == null) {
					return;
				}
				int offset = inner.getMeasuredHeight() - scroll.getHeight();
				if (offset < 0) {
					offset = 0;
				}

				scroll.scrollTo(0, offset);
			}
		});
	}
}
