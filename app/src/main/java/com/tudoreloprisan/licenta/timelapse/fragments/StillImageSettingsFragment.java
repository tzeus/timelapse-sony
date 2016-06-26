package com.tudoreloprisan.licenta.timelapse.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
    private RemoteApi remoteApi;
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //END OF DRAGONS
        View viewResult = inflater.inflate(R.layout.still_image_fragment, container, false);

        //NEW ADDED CODE
        TimelapseApplication app = (TimelapseApplication) getActivity().getApplication();
        ArrayList<ServerDevice> servers = new ArrayList<>();
//        getArguments()
        servers= ((ArrayList<ServerDevice>) getArguments().get(DEVICES));
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
//        getInfoForScreen();
        getInfoForScreenApi();
        apertureSpinner = ((Spinner) viewResult.findViewById(R.id.apertureButton));
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

        shutterSpinner = ((Spinner) viewResult.findViewById(R.id.shutteSpeedButton));
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

        isoSpinner = ((Spinner) viewResult.findViewById(R.id.iSObutton));
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


        focusModeSpinner = ((Spinner) viewResult.findViewById(R.id.focusModeButton));
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

        manualSwitch = ((Switch) viewResult.findViewById(R.id.manualSwitch));
        if (exposureModeSettings.getCurrentSetting().equals(ExposureMode.MANUAL)) {
            manualSwitch.setChecked(false);
        }
        manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCameraIO.setExposureMode(isChecked == true ? ExposureMode.MANUAL.getName() : ExposureMode.INTELLIGENT_AUTO.getName());
            }
        });

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


//        zoomInButton.setOnLongClickListener(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View arg0) {
//                mCameraIO.actZoom(CameraIO.ZoomDirection.IN, CameraIO.ZoomAction.START);
//                return true;
//            }
//        });
//
//        zoomOutButton.setOnLongClickListener(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View arg0) {
//                mCameraIO.actZoom(CameraIO.ZoomDirection.OUT, CameraIO.ZoomAction.START);
//                return true;
//            }
//        });

//        zoomInButton.setOnTouchListener(new View.OnTouchListener() {
//
//            long downTime = -1;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (System.currentTimeMillis() - downTime > 500) {
//                        mCameraIO.actZoom(CameraIO.ZoomDirection.IN, CameraIO.ZoomAction.STOP);
//                    }
//                }
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    downTime = System.currentTimeMillis();
//                }
//                return false;
//            }
//        });
//
//        zoomOutButton.setOnTouchListener(new View.OnTouchListener() {
//
//            long downTime = -1;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (System.currentTimeMillis() - downTime > 500) {
//                        mCameraIO.actZoom(CameraIO.ZoomDirection.OUT, CameraIO.ZoomAction.STOP);
//                    }
//                }
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    downTime = System.currentTimeMillis();
//                }
//                return false;
//            }
//        });


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


    private void getInfoForScreenApi() {
        try {
            JSONObject replyJson = null;
            replyJson = mRemoteApi.getAvailableFNumber();
            populateSettingsObjects(replyJson, apertureSettings);
            replyJson = mRemoteApi.getAvailableFocusModes();
            populateSettingsObjects(replyJson, focusModeSettings);
            replyJson = mRemoteApi.getAvailableIsoSpeed();
            populateSettingsObjects(replyJson, isoSettings);
            replyJson = mRemoteApi.getAvailableShutterSpeed();
            populateSettingsObjects(replyJson, shutterSpeedSettings);
            replyJson = mRemoteApi.getAvailableExposureMode();
            populateSettingsObjects(replyJson, exposureModeSettings);


        } catch (IOException | JSONException exc) {
            Log.e(TAG, exc.getMessage());
        }
    }

    private void populateSettingsObjects(JSONObject replyJson, AvailableCameraSettings apertureSettings) throws JSONException {
        if (replyJson != null) {
            extractAvailableSettings(replyJson.getJSONArray("result"), apertureSettings);
        }
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
            Log.w(TAG, "startLiveview mLiveviewSurface is null.");
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
