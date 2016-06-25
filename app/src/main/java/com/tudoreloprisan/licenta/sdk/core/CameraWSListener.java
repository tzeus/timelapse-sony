package com.tudoreloprisan.licenta.sdk.core;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Doru
 *
 */
public interface CameraWSListener {

	void cameraResponse(JSONArray jsonResponse);

	void cameraError(JSONObject jsonResponse);
	
}
