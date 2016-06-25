package com.tudoreloprisan.licenta.sdk;

/**
 * 
 * @author Doru
 *
 */
public interface TakePictureListener {

	void onResult(String url);
	void onError(CameraIO.ResponseCode responseCode, String responseMsg);
	
}
