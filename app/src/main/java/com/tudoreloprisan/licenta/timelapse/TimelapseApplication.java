package com.tudoreloprisan.licenta.timelapse;

import android.app.Application;

import com.tudoreloprisan.licenta.io.WifiHandler;
import com.tudoreloprisan.licenta.sdk.CameraIO;
import com.tudoreloprisan.licenta.sdk.model.DeviceManager;

public class TimelapseApplication extends Application {

	private CameraIO mCameraIO;
	private DeviceManager mDeviceManager;
	private WifiHandler mWifiHandler;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mCameraIO = new CameraIO(this);
		mDeviceManager = new DeviceManager(this);
		mWifiHandler = new WifiHandler(this);
	}
	
	
	public CameraIO getCameraIO() {
		return mCameraIO;
	}
	
	public WifiHandler getWifiHandler() {
		return mWifiHandler;
	}
	
	public DeviceManager getDeviceManager() {
		return mDeviceManager;
	}
}
