package com.tudoreloprisan.licenta.timelapse.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.tudoreloprisan.licenta.R;
import com.tudoreloprisan.licenta.sdk.CameraIO;
import com.tudoreloprisan.licenta.sdk.CameraListener;
import com.tudoreloprisan.licenta.sdk.StartLiveviewListener;
import com.tudoreloprisan.licenta.sdk.TakePictureListener;
import com.tudoreloprisan.licenta.sdk.model.AvailableCameraSettings;
import com.tudoreloprisan.licenta.sdk.model.ExposureMode;
import com.tudoreloprisan.licenta.sdk.model.SettingType;
import com.tudoreloprisan.licenta.sdk.sample.DisplayHelper;
import com.tudoreloprisan.licenta.sdk.sample.RemoteApi;
import com.tudoreloprisan.licenta.sdk.sample.ServerDevice;
import com.tudoreloprisan.licenta.sdk.sample.SimpleCameraEventObserver;
import com.tudoreloprisan.licenta.timelapse.StepFragment;
import com.tudoreloprisan.licenta.timelapse.TimelapseApplication;
import com.tudoreloprisan.licenta.timelapse.ui.SimpleStreamSurfaceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Doru on 6/24/2016.
 */
public class StillImageSettingsFragment extends StepFragment {
    public static final String DEVICES = "devices";
    private static final int TAKE_PICTURE_ACTIVITY_RESULT = 0x1;
    private static final String TAG = StillImageSettingsFragment.class.getSimpleName();

    private CameraIO mCameraIO;
    private final Set<String> mAvailableCameraApiSet = new HashSet<String>();
    private SimpleStreamSurfaceView liveViewSurfaceView;
    private ServerDevice mTargetServer;
    private SimpleCameraEventObserver mEventObserver;
    private RemoteApi mRemoteApi;
    private SimpleCameraEventObserver.ChangeListener mEventListener;

    private Spinner apertureSpinner;
    private Spinner shutterSpinner;
    private Spinner isoSpinner;
    private Spinner focusModeSpinner;
    private Switch manualSwitch;
    private TextView exposureTextView;
    private ZoomControls zoomControls;
    private AvailableCameraSettings apertureSettings = new AvailableCameraSettings(SettingType.APERTURE);
    private AvailableCameraSettings shutterSpeedSettings = new AvailableCameraSettings(SettingType.SHUTTER_SPEED);
    private AvailableCameraSettings isoSettings = new AvailableCameraSettings(SettingType.ISO);
    private AvailableCameraSettings focusModeSettings = new AvailableCameraSettings(SettingType.FOCUS_MODE);
    private final Set<String> mSupportedApiSet = new HashSet<String>();
    private SpinnerAdapter appertureSpinnerAdapter;
    private AvailableCameraSettings exposureModeSettings = new AvailableCameraSettings(SettingType.EXPOSURE_MODES);


    public StillImageSettingsFragment() {
    }

    public static StillImageSettingsFragment newInstance(ArrayList<ServerDevice> serverDeviceList) {
        StillImageSettingsFragment stillImageSettingsFragment = new StillImageSettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(DEVICES, serverDeviceList);
        stillImageSettingsFragment.setArguments(arguments);
        return stillImageSettingsFragment;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraIO = ((TimelapseApplication) getActivity().getApplication()).getCameraIO();
        //FIXME DRAGONS AHEAD
        //enableStrictMode();
        //END OF DRAGONS
        View viewResult = inflater.inflate(R.layout.still_image_fragment, container, false);

        //NEW ADDED CODE
        TimelapseApplication app = (TimelapseApplication) getActivity().getApplication();
        ArrayList<ServerDevice> servers = new ArrayList<>();
        servers = ((ArrayList<ServerDevice>) getArguments().get(DEVICES));
        if (!servers.isEmpty()) {
            app.setTargetServerDevice(servers.get(0));
        }
        mTargetServer = app.getTargetServerDevice();
        mRemoteApi = new RemoteApi(mTargetServer);
        app.setRemoteApi(mRemoteApi);
        mEventObserver = new SimpleCameraEventObserver(getActivity().getApplicationContext(), mRemoteApi);
        app.setCameraEventObserver(mEventObserver);

        mEventListener = new SimpleCameraEventObserver.ChangeListenerTmpl() {

            @Override
            public void onShootModeChanged(String shootMode) {
                Log.d(TAG, "onShootModeChanged() called: " + shootMode);
                refreshUi();
            }

            @Override
            public void onCameraStatusChanged(String status) {
                Log.d(TAG, "onCameraStatusChanged() called: " + status);
                refreshUi();
            }

            @Override
            public void onApiListModified(List<String> apis) {
                Log.d(TAG, "onApiListModified() called");
                synchronized (mAvailableCameraApiSet) {
                    mAvailableCameraApiSet.clear();
                    for (String api : apis) {
                        mAvailableCameraApiSet.add(api);
                    }
                    if (!mEventObserver.getLiveviewStatus() //
                            && isCameraApiAvailable("startLiveview")) {
                        if (liveViewSurfaceView != null && !liveViewSurfaceView.isStarted()) {
                            startLiveview();
                        }
                    }

//                    FOR ZOOM
//                    if (isCameraApiAvailable("actZoom")) {
//                        Log.d(TAG, "onApiListModified(): prepareActZoomButtons()");
//                        prepareActZoomButtons(true);
//                    } else {
//                        prepareActZoomButtons(false);
//                    }
                }
            }

//            @Override
//            public void onZoomPositionChanged(int zoomPosition) {
//                Log.d(TAG, "onZoomPositionChanged() called = " + zoomPosition);
//                if (zoomPosition == 0) {
//                    mButtonZoomIn.setEnabled(true);
//                    mButtonZoomOut.setEnabled(false);
//                } else if (zoomPosition == 100) {
//                    mButtonZoomIn.setEnabled(false);
//                    mButtonZoomOut.setEnabled(true);
//                } else {
//                    mButtonZoomIn.setEnabled(true);
//                    mButtonZoomOut.setEnabled(true);
//                }
//            }

            @Override
            public void onLiveviewStatusChanged(boolean status) {
                Log.d(TAG, "onLiveviewStatusChanged() called = " + status);
            }

            @Override
            public void onStorageIdChanged(String storageId) {
                Log.d(TAG, "onStorageIdChanged() called: " + storageId);
                refreshUi();
            }
        };

        liveViewSurfaceView = (SimpleStreamSurfaceView) viewResult.findViewById(R.id.camera_settings_liveview);
        apertureSpinner = ((Spinner) viewResult.findViewById(R.id.apertureButton));
        shutterSpinner = ((Spinner) viewResult.findViewById(R.id.shutteSpeedButton));
        isoSpinner = ((Spinner) viewResult.findViewById(R.id.iSObutton));
        focusModeSpinner = ((Spinner) viewResult.findViewById(R.id.focusModeButton));
        manualSwitch = ((Switch) viewResult.findViewById(R.id.manualSwitch));

        getInfoForScreenApi();
        exposureTextView = ((TextView) viewResult.findViewById(R.id.exposureLabel));

        zoomControls = ((ZoomControls) viewResult.findViewById(R.id.zoomControls));
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraIO.actZoom(CameraIO.ZoomDirection.IN);
            }
        });

        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraIO.actZoom(CameraIO.ZoomDirection.OUT);
            }
        });


        Button takePictureButton = (Button) viewResult.findViewById(R.id.cameraSettingsTakePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraIO.stopLiveView();
                mCameraIO.takePicture(new TakePictureListener() {

                    @Override
                    public void onResult(String url) {

                        mCameraIO.startLiveView(null);

                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(url), "image/jpeg");
                        startActivityForResult(intent, TAKE_PICTURE_ACTIVITY_RESULT);

                    }

                    @Override
                    public void onError(CameraIO.ResponseCode responseCode, String responseMsg) {
                        //TODO
                    }
                });
            }
        });

        return viewResult;
    }

    private void enableStrictMode() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    @Override
    public void onResume() {
        super.onResume();
        mEventObserver.activate();
        liveViewSurfaceView = (SimpleStreamSurfaceView) getActivity().findViewById(R.id.camera_settings_liveview);
//        mSpinnerShootMode.setFocusable(false);
//        mButtonContentsListMode.setEnabled(false);

//        mButtonTakePicture.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                takeAndFetchPicture();
//            }
//        });
//        mButtonRecStartStop.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if ("MovieRecording".equals(mEventObserver.getCameraStatus())) {
//                    stopMovieRec();
//                } else if ("IDLE".equals(mEventObserver.getCameraStatus())) {
//                    startMovieRec();
//                }
//            }
//        });
//
//        mImagePictureWipe.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mImagePictureWipe.setVisibility(View.INVISIBLE);
//            }
//        });
//
//
//        mButtonContentsListMode.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "Clicked contents list mode button");
//                prepareToStartContentsListMode();
//            }
//        });

        prepareOpenConnection();

        Log.d(TAG, "onResume() completed.");


    }

    /**
     * Check if the specified API is supported. This is for camera and avContent
     * service API. The result of this method does not change dynamically.
     *
     * @param apiName
     * @return
     */
    private boolean isApiSupported(String apiName) {
        boolean isAvailable = false;
        synchronized (mSupportedApiSet) {
            isAvailable = mSupportedApiSet.contains(apiName);
        }
        return isAvailable;
    }

    private static boolean isShootingStatus(String currentStatus) {
        Set<String> shootingStatus = new HashSet<String>();
        shootingStatus.add("IDLE");
        shootingStatus.add("NotReady");
        shootingStatus.add("StillCapturing");
        shootingStatus.add("StillSaving");
        shootingStatus.add("MovieWaitRecStart");
        shootingStatus.add("MovieRecording");
        shootingStatus.add("MovieWaitRecStop");
        shootingStatus.add("MovieSaving");
        shootingStatus.add("IntervalWaitRecStart");
        shootingStatus.add("IntervalRecording");
        shootingStatus.add("IntervalWaitRecStop");
        shootingStatus.add("AudioWaitRecStart");
        shootingStatus.add("AudioRecording");
        shootingStatus.add("AudioWaitRecStop");
        shootingStatus.add("AudioSaving");

        return shootingStatus.contains(currentStatus);
    }

    private void prepareOpenConnection() {
        Log.d(TAG, "prepareToOpenConection() exec");


        new Thread() {

            @Override
            public void run() {
                try {
                    // Get supported API list (Camera API)
                    JSONObject replyJsonCamera = mRemoteApi.getCameraMethodTypes();
                    loadSupportedApiList(replyJsonCamera);

                    try {
                        // Get supported API list (AvContent API)
                        JSONObject replyJsonAvcontent = mRemoteApi.getAvcontentMethodTypes();
                        loadSupportedApiList(replyJsonAvcontent);
                    } catch (IOException e) {
                        Log.d(TAG, "AvContent is not support.");
                    }

                    TimelapseApplication app = (TimelapseApplication) getActivity().getApplication();
                    app.setSupportedApiList(mSupportedApiSet);

                    if (!isApiSupported("setCameraFunction")) {

                        // this device does not support setCameraFunction.
                        // No need to check camera status.

                        openConnection();

                    } else {

                        // this device supports setCameraFunction.
                        // after confirmation of camera state, open connection.
                        Log.d(TAG, "this device support set camera function");

                        if (!isApiSupported("getEvent")) {
                            Log.e(TAG, "this device is not support getEvent");
                            openConnection();
                            return;
                        }

                        // confirm current camera status
                        String cameraStatus = null;
                        JSONObject replyJson = mRemoteApi.getEvent(false);
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                        String type = cameraStatusObj.getString("type");
                        if ("cameraStatus".equals(type)) {
                            cameraStatus = cameraStatusObj.getString("cameraStatus");
                        } else {
                            throw new IOException();
                        }

                        if (isShootingStatus(cameraStatus)) {
                            Log.d(TAG, "camera function is Remote Shooting.");
                            openConnection();
                        } else {
                            // set Listener
                            startOpenConnectionAfterChangeCameraState();

                            // set Camera function to Remote Shooting
                            replyJson = mRemoteApi.setCameraFunction("Remote Shooting");
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());
                    DisplayHelper.toast(getActivity().getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(getActivity(), false);
                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());
                    DisplayHelper.toast(getActivity().getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(getActivity(), false);
                }
            }
        }.start();
    }


    private void startOpenConnectionAfterChangeCameraState() {
        Log.d(TAG, "startOpenConectiontAfterChangeCameraState() exec");

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mEventObserver
                        .setEventChangeListener(new SimpleCameraEventObserver.ChangeListenerTmpl() {

                            @Override
                            public void onCameraStatusChanged(String status) {
                                Log.d(TAG, "onCameraStatusChanged:" + status);
                                if ("IDLE".equals(status) || "NotReady".equals(status)) {
                                    openConnection();
                                }
                                refreshUi();
                            }

                            @Override
                            public void onShootModeChanged(String shootMode) {
                                refreshUi();
                            }

                            @Override
                            public void onStorageIdChanged(String storageId) {
                                refreshUi();
                            }
                        });

                mEventObserver.start();
            }
        });
    }

    /**
     * Retrieve a list of APIs that are supported by the target device.
     *
     * @param replyJson
     */
    private void loadSupportedApiList(JSONObject replyJson) {
        synchronized (mSupportedApiSet) {
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray("results");
                for (int i = 0; i < resultArrayJson.length(); i++) {
                    mSupportedApiSet.add(resultArrayJson.getJSONArray(i).getString(0));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadSupportedApiList: JSON format error.");
            }
        }
    }

    /**
     * Take a picture and retrieve the image data.
     */
    private void takeAndFetchPicture() {
        if (liveViewSurfaceView == null || !liveViewSurfaceView.isStarted()) {
            DisplayHelper.toast(getActivity().getApplicationContext(), R.string.msg_error_take_picture);
            return;
        }

        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;
                    if (1 <= imageUrlsObj.length()) {
                        postImageUrl = imageUrlsObj.getString(0);
                    }
                    if (postImageUrl == null) {
                        Log.w(TAG, "takeAndFetchPicture: post image URL is null.");
                        DisplayHelper.toast(getActivity().getApplicationContext(), //
                                R.string.msg_error_take_picture);
                        return;
                    }
                    // Show progress indicator
//                    DisplayHelper.setProgressIndicator(SampleCameraActivity.this, true);

                    URL url = new URL(postImageUrl);
                    InputStream istream = new BufferedInputStream(url.openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // irresponsible value
                    final Drawable pictureDrawable =
                            new BitmapDrawable(getResources(), //
                                    BitmapFactory.decodeStream(istream, null, options));
                    istream.close();
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            mImagePictureWipe.setVisibility(View.VISIBLE);
//                            mImagePictureWipe.setImageDrawable(pictureDrawable);
                        }
                    });

                } catch (IOException e) {
                    Log.w(TAG, "IOException while closing slicer: " + e.getMessage());
                    DisplayHelper.toast(getActivity().getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } catch (JSONException e) {
                    Log.w(TAG, "JSONException while closing slicer");
                    DisplayHelper.toast(getActivity().getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } finally {
//                    DisplayHelper.setProgressIndicator(SampleCameraActivity.this, false);
                }
            }
        }.start();
    }

    @Override
    public void onEnterFragment() {
        setStepCompleted(true);
        startLiveView();
    }

    private void startLiveView() {

        if (liveViewSurfaceView.isStarted() && mCameraIO != null) {
            return;
        }

        mCameraIO.startLiveView(new StartLiveviewListener() {
            @Override
            public void onResult(String liveviewUrl) {
                liveViewSurfaceView.start(liveviewUrl);
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    private class CameraSettings extends AsyncTask<AvailableCameraSettings, Void, AvailableCameraSettings> {
        AvailableCameraSettings availableCameraSettings = new AvailableCameraSettings();


        @Override
        protected void onPostExecute(AvailableCameraSettings availableCameraSettings) {
            if (availableCameraSettings.getSetting().equals(SettingType.APERTURE)) {
                apertureSettings = availableCameraSettings;
                ArrayAdapter<String> apertureAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
                apertureAdapter.addAll(apertureSettings.getAvailableSettings());
                apertureSpinner.setAdapter(apertureAdapter);
                apertureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String itemAtPosition = ((String) parent.getItemAtPosition(position));
                        mCameraIO.setAperture(itemAtPosition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else if (availableCameraSettings.getSetting().equals(SettingType.ISO)) {
                isoSettings = availableCameraSettings;
                ArrayAdapter<String> isoSpeedAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
                isoSpeedAdapter.addAll(isoSettings.getAvailableSettings());
                isoSpinner.setAdapter(isoSpeedAdapter);
                isoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String itemAtPosition = ((String) parent.getItemAtPosition(position));
                        mCameraIO.setIsoSpeed(itemAtPosition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else if (availableCameraSettings.getSetting().equals(SettingType.SHUTTER_SPEED)) {
                shutterSpeedSettings = availableCameraSettings;
                ArrayAdapter<String> shutterSpeedAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
                shutterSpeedAdapter.addAll(shutterSpeedSettings.getAvailableSettings());
                shutterSpinner.setAdapter(shutterSpeedAdapter);
                shutterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String itemAtPosition = ((String) parent.getItemAtPosition(position));
                        mCameraIO.setShutterSpeed(itemAtPosition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else if (availableCameraSettings.getSetting().equals(SettingType.EXPOSURE_MODES)) {
                exposureModeSettings = availableCameraSettings;
                if (exposureModeSettings.getCurrentSetting().equals(ExposureMode.MANUAL)) {
                    manualSwitch.setChecked(false);
                }
                manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mCameraIO.setExposureMode(isChecked == true ? ExposureMode.MANUAL.getName() : ExposureMode.INTELLIGENT_AUTO.getName());
                    }
                });
            } else if (availableCameraSettings.getSetting().equals(SettingType.FOCUS_MODE)) {
                focusModeSettings = availableCameraSettings;

                ArrayAdapter<String> focusModeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
                focusModeAdapter.addAll(focusModeSettings.getAvailableSettings());
                focusModeSpinner.setAdapter(focusModeAdapter);
                focusModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String itemAtPosition = ((String) parent.getItemAtPosition(position));
                        mCameraIO.setFocusMode(itemAtPosition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }

        @Override
        protected AvailableCameraSettings doInBackground(AvailableCameraSettings... params) {
            JSONObject replyJson;
            try {
                if (params[0] == null) {

                    replyJson = mRemoteApi.getAvailableApiList();
                    JSONArray result = replyJson.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        Log.d(TAG, "Api Function no. " + i + "is:  " + result.getString(i));
                    }
                    replyJson = mRemoteApi.startRecMode();
                    result = replyJson.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        Log.d(TAG, "Api Function no. " + i + "is:  " + result.getString(i));
                    }
                } else {

                    AvailableCameraSettings parameter = params[0];

                    if (parameter.getSetting().equals(SettingType.APERTURE)) {
                        replyJson = mRemoteApi.getAvailableFNumber();
//                result = replyJson.getJSONArray("result");
                        availableCameraSettings.setSetting(SettingType.APERTURE);
                        populateSettingsObjects(replyJson, availableCameraSettings);
                    } else if (parameter.getSetting().equals(SettingType.EXPOSURE_MODES)) {
                        replyJson = mRemoteApi.getAvailableExposureMode();
//                result = replyJson.getJSONArray("result");
                        availableCameraSettings.setSetting(SettingType.EXPOSURE_MODES);
                        populateSettingsObjects(replyJson, availableCameraSettings);
                    } else if (parameter.getSetting().equals(SettingType.SHUTTER_SPEED)) {
                        replyJson = mRemoteApi.getAvailableShutterSpeed();
//                result = replyJson.getJSONArray("result");
                        availableCameraSettings.setSetting(SettingType.SHUTTER_SPEED);
                        populateSettingsObjects(replyJson, availableCameraSettings);
                    } else if (parameter.getSetting().equals(SettingType.FOCUS_MODE)) {
                        replyJson = mRemoteApi.getAvailableShutterSpeed();
//                result = replyJson.getJSONArray("result");
                        availableCameraSettings.setSetting(SettingType.FOCUS_MODE);
                        populateSettingsObjects(replyJson, availableCameraSettings);
                    } else if (parameter.getSetting().equals(SettingType.ISO)) {
                        replyJson = mRemoteApi.getAvailableShutterSpeed();
//                result = replyJson.getJSONArray("result");
                        availableCameraSettings.setSetting(SettingType.ISO);
                        populateSettingsObjects(replyJson, availableCameraSettings);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            return availableCameraSettings;
        }

        private void populateSettingsObjects(JSONObject replyJson, AvailableCameraSettings apertureSettings) throws JSONException {
            if (replyJson != null) {
                extractAvailableSettings(replyJson.getJSONArray("result"), apertureSettings);
            }
        }
    }


    private void getInfoForScreenApi() {
        //WILL BE REPLACED BY ASYNC TASK
        new CameraSettings().execute();
        new CameraSettings().execute();
        new CameraSettings().execute(apertureSettings);
        new CameraSettings().execute(isoSettings);
        new CameraSettings().execute(shutterSpeedSettings);
        new CameraSettings().execute(focusModeSettings);
        new CameraSettings().execute(exposureModeSettings);
//            replyJson = mRemoteApi.getAvailableFNumber();
//
//            populateSettingsObjects(replyJson, apertureSettings);
//            replyJson = mRemoteApi.getAvailableFocusModes();
//            populateSettingsObjects(replyJson, focusModeSettings);
//            replyJson = mRemoteApi.getAvailableIsoSpeed();
//            populateSettingsObjects(replyJson, isoSettings);
//            replyJson = mRemoteApi.getAvailableShutterSpeed();
//            populateSettingsObjects(replyJson, shutterSpeedSettings);
//            replyJson = mRemoteApi.getAvailableExposureMode();
//            populateSettingsObjects(replyJson, exposureModeSettings);


    }


    private void getInfoForScreen() {

        mCameraIO.getExposureModes(new CameraListener() {
            @Override
            public void onResult(JSONArray response) {
                if (response == null) {
                    return;
                }
                try {
                    extractAvailableSettings(response, exposureModeSettings);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(CameraIO.ResponseCode responseCode, String responseMsg) {

            }
        });

        mCameraIO.getApertures(new CameraListener() {
            @Override
            public void onResult(JSONArray response) {
                if (response == null) {
                    return;
                }
                try {
                    extractAvailableSettings(response, apertureSettings);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(CameraIO.ResponseCode responseCode, String responseMsg) {

            }
        });

        mCameraIO.getIsoSpeedRates(new CameraListener() {
            @Override
            public void onResult(JSONArray response) {
                if (response == null) {
                    return;
                }
                try {
                    extractAvailableSettings(response, isoSettings);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(CameraIO.ResponseCode responseCode, String responseMsg) {

            }
        });

        mCameraIO.getShutterSpeeds(new CameraListener() {
            @Override
            public void onResult(JSONArray response) {
                if (response == null) {
                    return;
                }
                try {
                    extractAvailableSettings(response, shutterSpeedSettings);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(CameraIO.ResponseCode responseCode, String responseMsg) {

            }
        });

        mCameraIO.getFocusModes(new CameraListener() {
            @Override
            public void onResult(JSONArray response) {
                if (response == null) {
                    return;
                }
                try {
                    extractAvailableSettings(response.getJSONArray(0), focusModeSettings);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(CameraIO.ResponseCode responseCode, String responseMsg) {

            }
        });
    }

    private void extractAvailableSettings(JSONArray response, AvailableCameraSettings availableCameraSettings) throws JSONException {
        availableCameraSettings.setCurrentSetting(response.getString(0));
        JSONArray availableSettings = response.getJSONArray(1);
        List<String> availableSettingsList = new ArrayList<String>();
        for (int i = 0; i < availableSettings.length(); i++) {
            availableSettingsList.add(availableSettings.getString(i));
        }
        availableCameraSettings.setAvailableSettings(availableSettingsList);
        //Display current and available settings
        Log.d(TAG, "The current setting for " + availableCameraSettings.getSetting().getName() +
                " is: " + availableCameraSettings.getCurrentSetting());
        Log.d(TAG, "The available settings for this mode are: ");
        for (String s : availableCameraSettings.getAvailableSettings()) {
            Log.d(TAG, "s \n");
        }
    }

    /**
     * Check if the specified API is available at present. This works correctly
     * only for Camera API.
     *
     * @param apiName
     * @return
     */
    private boolean isCameraApiAvailable(String apiName) {
        boolean isAvailable = false;
        synchronized (mAvailableCameraApiSet) {
            isAvailable = mAvailableCameraApiSet.contains(apiName);
        }
        return isAvailable;
    }

    private void refreshUi() {
        String cameraStatus = mEventObserver.getCameraStatus();
        String shootMode = mEventObserver.getShootMode();
        List<String> availableShootModes = mEventObserver.getAvailableShootModes();

//        // CameraStatus TextView
//        mTextCameraStatus.setText(cameraStatus);
//
//        // Recording Start/Stop Button
//        if ("MovieRecording".equals(cameraStatus)) {
//            mButtonRecStartStop.setEnabled(true);
//            mButtonRecStartStop.setText(R.string.button_rec_stop);
//        } else if ("IDLE".equals(cameraStatus) && "movie".equals(shootMode)) {
//            mButtonRecStartStop.setEnabled(true);
//            mButtonRecStartStop.setText(R.string.button_rec_start);
//        } else {
//            mButtonRecStartStop.setEnabled(false);
//        }
//
//        // Take picture Button
//        if ("still".equals(shootMode) && "IDLE".equals(cameraStatus)) {
//            mButtonTakePicture.setEnabled(true);
//        } else {
//            mButtonTakePicture.setEnabled(false);
//        }
//
//        // Picture wipe Image
//        if (!"still".equals(shootMode)) {
//            mImagePictureWipe.setVisibility(View.INVISIBLE);
//        }
//
//        // Update Shoot Modes List
//        ArrayAdapter<String> adapter = (ArrayAdapter<String>) mSpinnerShootMode.getAdapter();
//        if (adapter != null) {
//            adapter.clear();
//            for (String mode : availableShootModes) {
//                if (isSupportedShootMode(mode)) {
//                    adapter.add(mode);
//                }
//            }
//            selectionShootModeSpinner(mSpinnerShootMode, shootMode);
//        }
//
//        // Shoot Mode Buttons
//        if ("IDLE".equals(cameraStatus)) {
//            mSpinnerShootMode.setEnabled(true);
//        } else {
//            mSpinnerShootMode.setEnabled(false);
//        }
//
//        // Contents List Button
//        if (isApiSupported("getContentList") //
//                && isApiSupported("getSchemeList") //
//                && isApiSupported("getSourceList")) {
//            String storageId = mEventObserver.getStorageId();
//            if (storageId == null) {
//                Log.d(TAG, "not update ContentsList button ");
//            } else if ("No Media".equals(storageId)) {
//                mButtonContentsListMode.setEnabled(false);
//            } else {
//                mButtonContentsListMode.setEnabled(true);
//            }
//        }
    }

    private void startLiveview() {
        if (liveViewSurfaceView == null) {
            Log.w(TAG, "startLiveview liveViewSurfaceView is null.");
            return;
        }
        new Thread() {
            @Override
            public void run() {

                try {
                    JSONObject replyJson = null;
                    replyJson = mRemoteApi.startLiveview();

                    if (!RemoteApi.isErrorReply(replyJson)) {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (1 <= resultsObj.length()) {
                            // Obtain liveview URL from the result.
                            final String liveviewUrl = resultsObj.getString(0);
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    liveViewSurfaceView.start(liveviewUrl);
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startLiveview IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startLiveview JSONException: " + e.getMessage());
                }
            }
        }.start();
    }

    private void stopLiveview() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mRemoteApi.stopLiveview();
                } catch (IOException e) {
                    Log.w(TAG, "stopLiveview IOException: " + e.getMessage());
                }
            }
        }.start();
    }


    /**
     * Open connection to the camera device to start monitoring Camera events
     * and showing liveview.
     */
    private void openConnection() {

        mEventObserver.setEventChangeListener(mEventListener);
        new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "openConnection(): exec.");

                try {
                    JSONObject replyJson = null;

                    // getAvailableApiList
                    replyJson = mRemoteApi.getAvailableApiList();
                    loadAvailableCameraApiList(replyJson);

                    // check version of the server device
                    if (isCameraApiAvailable("getApplicationInfo")) {
                        Log.d(TAG, "openConnection(): getApplicationInfo()");
                        replyJson = mRemoteApi.getApplicationInfo();
                        if (!isSupportedServerVersion(replyJson)) {
                            DisplayHelper.toast(getActivity().getApplicationContext(), //
                                    R.string.msg_error_non_supported_device);
//                            SampleCameraActivity.this.finish();
                            return;
                        }
                    } else {
                        // never happens;
                        return;
                    }

                    // startRecMode if necessary.
                    if (isCameraApiAvailable("startRecMode")) {
                        Log.d(TAG, "openConnection(): startRecMode()");
                        replyJson = mRemoteApi.startRecMode();

                        // Call again.
                        replyJson = mRemoteApi.getAvailableApiList();
                        loadAvailableCameraApiList(replyJson);
                    }

                    // getEvent start
                    if (isCameraApiAvailable("getEvent")) {
                        Log.d(TAG, "openConnection(): EventObserver.start()");
                        mEventObserver.start();
                    }

                    // Liveview start
                    if (isCameraApiAvailable("startLiveview")) {
                        Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                        startLiveview();
                    }

                    // prepare UIs
                    if (isCameraApiAvailable("getAvailableShootMode")) {
                        Log.d(TAG, "openConnection(): prepareShootModeSpinner()");
//                        prepareShootModeSpinner();
                        // Note: hide progress bar on title after this calling.
                    }

//                    // prepare UIs
//                    if (isCameraApiAvailable("actZoom")) {
//                        Log.d(TAG, "openConnection(): prepareActZoomButtons()");
//                        prepareActZoomButtons(true);
//                    } else {
//                        prepareActZoomButtons(false);
//                    }

                    Log.d(TAG, "openConnection(): completed.");
                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                    //DisplayHelper.setProgressIndicator(SampleCameraActivity.this, false);
                    DisplayHelper.toast(getActivity().getApplicationContext(), R.string.msg_error_connection);
                }
            }
        }.start();

    }

    /**
     * Stop monitoring Camera events and close liveview connection.
     */
    private void closeConnection() {

        Log.d(TAG, "closeConnection(): exec.");
        // Liveview stop
        Log.d(TAG, "closeConnection(): LiveviewSurface.stop()");
        if (liveViewSurfaceView != null) {
            liveViewSurfaceView.stop();
            liveViewSurfaceView = null;
            stopLiveview();
        }

        // getEvent stop
        Log.d(TAG, "closeConnection(): EventObserver.release()");
        mEventObserver.release();

        Log.d(TAG, "closeConnection(): completed.");
    }

    /**
     * Check if the version of the server is supported in this application.
     *
     * @param replyJson
     * @return
     */
    private boolean isSupportedServerVersion(JSONObject replyJson) {
        try {
            JSONArray resultArrayJson = replyJson.getJSONArray("result");
            String version = resultArrayJson.getString(1);
            String[] separated = version.split("\\.");
            int major = Integer.valueOf(separated[0]);
            if (2 <= major) {
                return true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "isSupportedServerVersion: JSON format error.");
        } catch (NumberFormatException e) {
            Log.w(TAG, "isSupportedServerVersion: Number format error.");
        }
        return false;
    }

    /**
     * Retrieve a list of APIs that are available at present.
     *
     * @param replyJson
     */
    private void loadAvailableCameraApiList(JSONObject replyJson) {
        synchronized (mAvailableCameraApiSet) {
            mAvailableCameraApiSet.clear();
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray("result");
                JSONArray apiListJson = resultArrayJson.getJSONArray(0);
                for (int i = 0; i < apiListJson.length(); i++) {
                    mAvailableCameraApiSet.add(apiListJson.getString(i));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadAvailableCameraApiList: JSON format error.");
            }
        }
    }

    @Override
    public Spanned getInformation() {
        return null;
    }
}
