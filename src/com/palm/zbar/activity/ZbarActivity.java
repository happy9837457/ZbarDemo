package com.palm.zbar.activity;

import java.io.IOException;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.palm.zbar.R;
import com.palm.zbar.util.Const;
import com.palm.zbar.view.FinderView;

/**
 * Zbar使用
 * 
 * @author weixiang.qin
 * 
 */
public class ZbarActivity extends Activity implements SurfaceHolder.Callback {
	private Activity mActivity;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private FinderView finderView;
	private Camera mCamera;
	private ImageScanner scanner;
	private Handler autoFocusHandler;
	private boolean previewing = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zbar);
		initView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}

	private void initView() {
		mActivity = this;
		finderView = (FinderView) findViewById(R.id.finder_view);
		surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		mCamera = getCameraInstance();
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
		autoFocusHandler = new Handler();
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			Image barcode = new Image(size.width, size.height, "Y800");
			Rect scanImageRect = finderView.getScanImageRect(size.height,
					size.width);
			barcode.setCrop(scanImageRect.top, scanImageRect.left,
					scanImageRect.bottom, scanImageRect.right);
			barcode.setData(data);
			int result = scanner.scanImage(barcode);
			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				SymbolSet syms = scanner.getResults();
				if (syms.isEmpty() || syms.size() > 1) {
					setResult(RESULT_CANCELED);
				} else {
					Intent intent = new Intent();
					intent.putExtra(Const.SCAN_RESULT, syms.iterator().next()
							.getData());
					setResult(RESULT_OK, intent);
				}
				mActivity.finish();
			}
		}
	};

	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (holder.getSurface() == null) {
			return;
		}
		try {
			mCamera.stopPreview();
		} catch (Exception e) {

		}
		try {
			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(previewCb);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCB);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	static {
		System.loadLibrary("iconv");
	}

}
