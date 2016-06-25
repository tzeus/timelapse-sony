package com.tudoreloprisan.licenta.sdk;

import org.json.JSONArray;

/**
 * Created by Doru on 6/25/2016.
 */
public interface CameraListener {
    void onResult(JSONArray response);

    void onError(CameraIO.ResponseCode responseCode, String responseMsg);
}
