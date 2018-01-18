package com.example.barcode2ds.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.barcode2ds.MainActivity;
import com.example.barcode2ds.R;
import com.example.barcode2ds.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ErDSoftSetFragment extends KeyDwonFragment {

	@ViewInject(R.id.etParamNum)
	private EditText etParamNum;
	@ViewInject(R.id.etParamVal)
	private EditText etParamVal;
	@ViewInject(R.id.btnGet)
	private Button btnGet;
	@ViewInject(R.id.btnSet)
	private Button btnSet;

	private MainActivity mContext;

	@Override
	public void onResume() {
		super.onResume();

		etParamVal.setText("");
		etParamNum.setText("");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_er_dsoft_set, container,
				false);
		ViewUtils.inject(this, v);
		return v;
	}

	@OnClick(R.id.btnGet)
	public void btnGet_onClick(View v) {
		// get param #
		String s = etParamNum.getText().toString();
		try {
			int num = Integer.parseInt(s);
			int val = mContext.mReader.getNumParameter(num);

			etParamVal.setText(Integer.toString(val));

		} catch (NumberFormatException nx) {

		}
	}

	@OnClick(R.id.btnSet)
	public void btnSet_onClick(View v) {
		String sn = etParamNum.getText().toString();
		String sv = etParamVal.getText().toString();
		try {
			int num = Integer.parseInt(sn);
			int val = Integer.parseInt(sv);
			boolean res = mContext.mReader.setParameter(num, val);

			if (res) {
				UIHelper.ToastMessage(mContext, R.string.er_dsoft_Set_succ);
			} else {
				UIHelper.ToastMessage(mContext, R.string.er_dsoft_Set_fail);
			}

		} catch (NumberFormatException nx) {
		}
	}

	@Override
	public void myOnKeyDwon() {

	}
}
