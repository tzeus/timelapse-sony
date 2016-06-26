package com.tudoreloprisan.licenta.timelapse;

import android.app.Application;

import com.tudoreloprisan.licenta.io.WifiHandler;
import com.tudoreloprisan.licenta.sdk.CameraIO;
import com.tudoreloprisan.licenta.sdk.model.DeviceManager;
import com.tudoreloprisan.licenta.sdk.sample.RemoteApi;
import com.tudoreloprisan.licenta.sdk.sample.ServerDevice;
import com.tudoreloprisan.licenta.sdk.sample.SimpleCameraEventObserver;

import java.util.Set;

public class TimelapseApplication extends Application {

	private CameraIO mCameraIO;
	private DeviceManager mDeviceManager;
	private WifiHandler mWifiHandler;

	private ServerDevice mTargetDevice;

	private RemoteApi mRemoteApi;

	private SimpleCameraEventObserver mEventObserver;

	private Set<String> mSupportedApiSet;

	/**
	 * Sets a target ServerDevice object.
	 *
	 * @param device
	 */
	public void setTargetServerDevice(ServerDevice device) {
		mTargetDevice = device;
	}

	/**
	 * Returns a target ServerDevice object.
	 *
	 * @return return ServiceDevice
	 */
	public ServerDevice getTargetServerDevice() {
		return mTargetDevice;
	}

	/**
	 * Sets a SimpleRemoteApi object to transmit to Activity.
	 *
	 * @param remoteApi
	 */
	public void setRemoteApi(RemoteApi remoteApi) {
		mRemoteApi = remoteApi;
	}

	/**
	 * Returns a SimpleRemoteApi object.
	 *
	 * @return return SimpleRemoteApi
	 */
	public RemoteApi getRemoteApi() {
		return mRemoteApi;
	}

	/**
	 * Sets a List of supported APIs.
	 *
	 * @param apiList
	 */
	public void setSupportedApiList(Set<String> apiList) {
		mSupportedApiSet = apiList;
	}

	/**
	 * Returns a list of supported APIs.
	 *
	 * @return Returns a list of supported APIs.
	 */
	public Set<String> getSupportedApiList() {
		return mSupportedApiSet;
	}

	/**
	 * Sets a SimpleCameraEventObserver object to transmit to Activity.
	 *
	 * @param observer
	 */
	public void setCameraEventObserver(SimpleCameraEventObserver observer) {
		mEventObserver = observer;
	}

	/**
	 * Returns a SimpleCameraEventObserver object.
	 *
	 * @return return SimpleCameraEventObserver
	 */
	public SimpleCameraEventObserver getCameraEventObserver() {
		return mEventObserver;
	}

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
