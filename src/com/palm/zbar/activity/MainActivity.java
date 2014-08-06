package com.palm.zbar.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.palm.zbar.R;
import com.palm.zbar.util.Const;

/**
 * 入口
 * 
 * @author weixiang.qin
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	private Activity mActivity;
	private Button scanBtn;
	private TextView resultTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mActivity = this;
		scanBtn = (Button) findViewById(R.id.scan_btn);
		resultTv = (TextView) findViewById(R.id.result_tv);
		scanBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Const.SCAN_REQ_CODE:
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra(Const.SCAN_RESULT);
				resultTv.setText(result);
			} else if (resultCode == RESULT_CANCELED) {
				resultTv.setText("扫描失败");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == scanBtn.getId()) {
			Intent intent = new Intent(mActivity, ZbarActivity.class);
			startActivityForResult(intent, Const.SCAN_REQ_CODE);
		}
	}

}
