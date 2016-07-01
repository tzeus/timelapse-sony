package com.tudoreloprisan.licenta.timelapse.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.tudoreloprisan.licenta.sdk.TakePictureListener;
import com.tudoreloprisan.licenta.sdk.model.AvailableCameraSettings;
import com.tudoreloprisan.licenta.sdk.model.ExposureMode;
import com.tudoreloprisan.licenta.sdk.model.SettingType;
import com.tudoreloprisan.licenta.timelapse.StepFragment;
import com.tudoreloprisan.licenta.timelapse.TimelapseApplication;
import com.tudoreloprisan.licenta.timelapse.ui.SimpleStreamSurfaceView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Doru on 6/24/2016.
 */
public class StillImageSettingsFragment extends StepFragment {
    private static final int TAKE_PICTURE_ACTIVITY_RESULT = 0x1;
    private static final String TAG = StillImageSettingsFragment.class.getSimpleName();

    private CameraIO mCameraIO;
    private SimpleStreamSurfaceView liveViewSurfaceView;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraIO = ((TimelapseApplication) getActivity().getApplication()).getCameraIO();

        View viewResult = inflater.inflate(R.layout.still_image_fragment, container, false);

        liveViewSurfaceView = (SimpleStreamSurfaceView) viewResult.findViewById(R.id.camera_settings_liveview);
        getInfoForScreen();

        apertureSpinner = ((Spinner) viewResult.findViewById(R.id.apertureButton));
        shutterSpinner = ((Spinner) viewResult.findViewById(R.id.shutteSpeedButton));
        isoSpinner = ((Spinner) viewResult.findViewById(R.id.iSObutton));
        focusModeSpinner = ((Spinner) viewResult.findViewById(R.id.focusModeButton));
        manualSwitch = ((Switch) viewResult.findViewById(R.id.manualSwitch));


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

    @Override
    public void onResume() {
        super.onResume();
        //Adding adapters for spinners
        ArrayAdapter<String> apertureAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        if (!apertureAdapter.isEmpty()) {
            apertureAdapter.addAll(apertureSettings.getAvailableSettings());
            apertureSpinner.setAdapter(apertureAdapter);
        }
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

        ArrayAdapter<String> shutterSpeedAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        if (!shutterSpeedAdapter.isEmpty()) {
            shutterSpeedAdapter.addAll(shutterSpeedSettings.getAvailableSettings());
            shutterSpinner.setAdapter(shutterSpeedAdapter);
        }
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

        ArrayAdapter<String> isoSpeedAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        if (!isoSpeedAdapter.isEmpty()) {
            isoSpeedAdapter.addAll(isoSettings.getAvailableSettings());
            isoSpinner.setAdapter(isoSpeedAdapter);
        }
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

        ArrayAdapter<String> focusModeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        if (!focusModeAdapter.isEmpty()) {
            focusModeAdapter.addAll(focusModeSettings.getAvailableSettings());
            focusModeSpinner.setAdapter(focusModeAdapter);
        }
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

        if (exposureModeSettings.getCurrentSetting().equals(ExposureMode.MANUAL.getName())) {
            manualSwitch.setChecked(false);
        }
        manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCameraIO.setExposureMode(isChecked ? ExposureMode.MANUAL.getName() : ExposureMode.INTELLIGENT_AUTO.getName());
            }
        });
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
                    extractAvailableSettings(response, focusModeSettings);
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

    @Override
    public Spanned getInformation() {
        return null;
    }
}
