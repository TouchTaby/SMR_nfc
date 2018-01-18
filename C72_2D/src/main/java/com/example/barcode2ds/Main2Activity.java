package com.example.barcode2ds;

import com.example.barcode2ds.MainActivity.InitTask;
import com.rscja.utility.StringUtility;
import com.zebra.adc.decoder.Barcode2DWithSoft;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class Main2Activity extends Activity {

	EditText editText1;
	public Barcode2DWithSoft mReader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		editText1 = (EditText) findViewById(R.id.editText1);

		try {
			mReader = Barcode2DWithSoft.getInstance();
		} catch (Exception ex) {

			Toast.makeText(Main2Activity.this, ex.getMessage(),
					Toast.LENGTH_SHORT).show();

			return;
		}

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

	public Barcode2DWithSoft.ScanCallback mScanCallback = new Barcode2DWithSoft.ScanCallback() {
		@Override
		public void onScanComplete(int i, int length, byte[] data) {

			Log.i("ErDSoftScanFragment", "onScanComplete() i=" + i);

			if (length < 1) {

				editText1.append(getString(R.string.yid_msg_scan_fail) + "\n");

				return;
			}

			String barCode = new String(data);

			editText1.append(barCode + "\n");

		}
	};

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
				result = mReader.open(Main2Activity.this);

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

				Toast.makeText(Main2Activity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}

			if (mReader != null) {

				mReader.setScanCallback(mScanCallback);
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(Main2Activity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 139) {

			if (event.getRepeatCount() == 0) {
				mReader.scan();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == 139) {

			mReader.stopScan();
		}

		return super.onKeyUp(keyCode, event);
	}

}
