package com.tudoreloprisan.licenta.sdk;

import android.content.Context;
import android.util.Log;

import com.tudoreloprisan.licenta.sdk.core.CameraWS;
import com.tudoreloprisan.licenta.sdk.core.CameraWSListener;
import com.tudoreloprisan.licenta.sdk.core.TestConnectionListener;
import com.tudoreloprisan.licenta.sdk.model.Device;
import com.tudoreloprisan.licenta.timelapse.model.Aperture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Doru
 *
 */
public class CameraIO {

	public enum ZoomDirection { IN, OUT }
	public enum ZoomAction { START, STOP }
	
	public enum ResponseCode {
		NONE(-1), //means no code available
		OK(0),
		LONG_SHOOTING(40403),
		NOT_AVAILABLE_NOW(1);
		
		private int value;
		
		ResponseCode(int value){ this.value = value; }
		public int getValue(){ return value; }
		public static ResponseCode find(int value){
			for(ResponseCode el : ResponseCode.values())
				if(el.getValue() == value)
					return el;
			return NONE; //if not an appropriate found
		}
		
	}

	public static int MIN_TIME_BETWEEN_CAPTURE = 1;

	private CameraWS mCameraWS;

	public CameraIO(Context context) {

		mCameraWS = new CameraWS(context);

	}

	public void setDevice(Device device) {
		mCameraWS.setWSUrl(device.getWebService());
	}

	/**
	 * Sets the shoot mode, "still" or "movie". This needs to be set to "still"
	 * on some camcorders, because they default to video.
	 *
	 * @param mode
	 *            either "still" or "movie".
	 */
	public void setShootMode(String mode) {
		JSONArray params = new JSONArray().put(mode);
		mCameraWS.sendRequest("setShootMode", params, null);
	}


    public void setAperture(final TakePictureListener listener, String fNumber){
        JSONArray parameters = new JSONArray();
        parameters.put(fNumber);
        mCameraWS.sendRequest("setFNumber", parameters, getTakePictureListener(listener));
    }

    public void setShutterSpeed(final TakePictureListener listener, String speed){
        JSONArray parameters = new JSONArray();
        parameters.put(speed);
        mCameraWS.sendRequest("setShutterSpeed", parameters, getTakePictureListener(listener));
    }

    public void setIsoSpeed(final TakePictureListener listener, String iSO){
        JSONArray parameters = new JSONArray();
        parameters.put(iSO);
        mCameraWS.sendRequest("setIsoSpeedRate", parameters, getTakePictureListener(listener));
    }

    public void setFocusMode(final TakePictureListener listener, String focusMode){
        JSONArray parameters = new JSONArray();
        parameters.put(focusMode);
        mCameraWS.sendRequest("setFocusMode", parameters, getTakePictureListener(listener));
    }

    public void setExposureMode(final TakePictureListener listener, String exposureMode){
        JSONArray parameters = new JSONArray();
        parameters.put(exposureMode);
        mCameraWS.sendRequest("setExposureMode", parameters, getTakePictureListener(listener));
    }


	public void getApertures(final TakePictureListener listener){
        mCameraWS.sendRequest("getAvailableFNumber", new JSONArray(), getTakePictureListener(listener));
    }

    public void getShutterSpeeds(final TakePictureListener listener){
        mCameraWS.sendRequest("getAvailableShutterSpeed", new JSONArray(), getTakePictureListener(listener));
    }

    public void getIsoSpeedRates(final TakePictureListener listener){
        mCameraWS.sendRequest("getAvailableIsoSpeedRate", new JSONArray(), getTakePictureListener(listener));
    }

    public void getSupportedShootMode(final TakePictureListener listener){
        mCameraWS.sendRequest("getSupportedShootMode", new JSONArray(), getTakePictureListener(listener));
    }

    public void getAvailableShootMode(final TakePictureListener listener){
        mCameraWS.sendRequest("getAvailableShootMode", new JSONArray(), getTakePictureListener(listener));
    }

    public void getFocusModes(final TakePictureListener listener){
        mCameraWS.sendRequest("getAvailableFocusMode", new JSONArray(), getTakePictureListener(listener));
    }

    public void getExposureModes(final TakePictureListener listener){
        mCameraWS.sendRequest("getAvailableExposureMode", new JSONArray(), getTakePictureListener(listener));
    }

	public void takePicture(final TakePictureListener listener) {
		mCameraWS.sendRequest("actTakePicture", new JSONArray(), getTakePictureListener(listener));
	}
	
	public void awaitTakePicture(final TakePictureListener listener) {
		mCameraWS.sendRequest("awaitTakePicture", new JSONArray(), getTakePictureListener(listener));
	}
	
	private CameraWSListener getTakePictureListener(final TakePictureListener listener){
		return new CameraWSListener() {

			@Override
			public void cameraResponse(JSONArray jsonResponse) {

				if(listener == null) {
					return;
				}

				String url;
				try {
					url = jsonResponse.getJSONArray(0).getString(0);
					listener.onResult(url);
				} catch (Exception e) {
					listener.onError(ResponseCode.NONE, e.getMessage());
				}
			}

			@Override
			public void cameraError(JSONObject jsonResponse) {
				if(listener != null) {
					
					if(jsonResponse == null) {
						listener.onError(ResponseCode.NONE, "json response is null");
						return;
					}
					
					int responseCode = -1;
					String responseMsg = null;
					// whole JSON is of format {"id":38,"error":[1,"Not Available Now"]}
					try {
						if(jsonResponse.has("error")){
							JSONArray arr = jsonResponse.getJSONArray("error");
							responseCode = arr.getInt(0);
							responseMsg = arr.getString(1);
						}
						listener.onError(ResponseCode.find(responseCode),responseMsg);
					} catch (JSONException err){
						listener.onError(ResponseCode.NONE, err.toString());
					}
				}
			}
		};
	}

	public void initWebService(final InitWebServiceListener listener) {

		mCameraWS.sendRequest("startRecMode", new JSONArray(), new CameraWSListener() {

			@Override
			public void cameraResponse(JSONArray jsonResponse) {
				if(listener == null) {
					return;
				}

				listener.onResult();
			}

			@Override
			public void cameraError(JSONObject jsonResponse) {
				if(listener == null) {
					return;
				}

				listener.onError("Error");
			}
		});

	}

	public void getVersion(final GetVersionListener listener) {

		mCameraWS.sendRequest("getVersions", new JSONArray(), new CameraWSListener() {

			@Override
			public void cameraResponse(JSONArray jsonResponse) {
				if(listener == null) {
					return;
				}

				int version;
				try {
					version = jsonResponse.getJSONArray(0).getInt(0);
					listener.onResult(version);
				} catch (Exception e) {
					listener.onError(e.getMessage());
				}
			}

			@Override
			public void cameraError(JSONObject jsonResponse) {
				if(listener == null) {
					return;
				}

				listener.onError("Error");
			}
		});

	}

	public void testConnection(final TestConnectionListener listener) {

		this.getVersion(new GetVersionListener() {
			@Override
			public void onResult(int version) {
				listener.cameraConnected(true);
			}

			@Override
			public void onError(String error) {
				listener.cameraConnected(false);
			}
		});

	}
	
	public void closeConnection() {

		// Not enough
		// mCameraWS.testConnection(timeout, listener);

		mCameraWS.sendRequest("stopRecMode", new JSONArray(), new CameraWSListener() {

			@Override
			public void cameraResponse(JSONArray jsonResponse) {
				Log.w("DEBUG","success closing connection.");			
			}

			@Override
			public void cameraError(JSONObject jsonResponse) {	
				Log.w("DEBUG","error closing connection.");				
			}
		}, 200);

	}


	public void startLiveView(final StartLiveviewListener listener) {

		mCameraWS.sendRequest("startLiveview", new JSONArray(), new CameraWSListener() {

			@Override
			public void cameraResponse(JSONArray jsonResponse) {
				if(listener == null) {
					return;
				}

				try {
					listener.onResult(jsonResponse.getString(0));
				} catch (Exception e) {
					listener.onError(e.getMessage());
				}
			}

			@Override
			public void cameraError(JSONObject jsonResponse) {
				if(listener == null) {
					return;
				}

				listener.onError("Error");
			}
		});
	}

	public void stopLiveView() {

		mCameraWS.sendRequest("stopLiveview", new JSONArray(), null);
	}


	public void actZoom(final ZoomDirection zoomDir) {

		JSONArray params = new JSONArray().put(zoomDir == ZoomDirection.IN ? "in" : "out").put("1shot");
		mCameraWS.sendRequest("actZoom", params, null);
	}

	public void actZoom(final ZoomDirection zoomDir, final ZoomAction zoomAct) {

		JSONArray params = new JSONArray().put(zoomDir == ZoomDirection.IN ? "in" : "out").
				put(zoomAct == ZoomAction.START ? "start" : "stop");
		mCameraWS.sendRequest("actZoom", params, null);
	}

	
	public void setFlash(final boolean enableFlash) {

		JSONArray params = new JSONArray().put(enableFlash ? "true" : "false");
		mCameraWS.sendRequest("setFlashMode", params, new CameraWSListener() {
			
			@Override
			public void cameraResponse(JSONArray jsonResponse) {
				Log.v("DEBUG", "ok: "+jsonResponse);
			}
			
			@Override
			public void cameraError(JSONObject jsonResponse) {
				Log.v("DEBUG", "err: "+jsonResponse);				
			}
		});
	}
}
