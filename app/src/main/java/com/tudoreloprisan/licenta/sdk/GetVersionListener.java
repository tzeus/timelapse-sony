package com.tudoreloprisan.licenta.sdk;

/**
 * 
 * @author Doru
 *
 */
public interface GetVersionListener {

	void onResult(int version);
	void onError(String error);
	
}
