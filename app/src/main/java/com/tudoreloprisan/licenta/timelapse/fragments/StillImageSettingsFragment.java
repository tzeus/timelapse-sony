package com.tudoreloprisan.licenta.timelapse.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.tudoreloprisan.licenta.R;
import com.tudoreloprisan.licenta.sdk.CameraIO;
import com.tudoreloprisan.licenta.sdk.TakePictureListener;
import com.tudoreloprisan.licenta.timelapse.StepFragment;
import com.tudoreloprisan.licenta.timelapse.TimelapseApplication;
import com.tudoreloprisan.licenta.timelapse.ui.SimpleStreamSurfaceView;

/**
 * Created by Doru on 6/24/2016.
 */
public class StillImageSettingsFragment extends StepFragment {
    private static final int TAKE_PICTURE_ACTIVITY_RESULT = 0x1;

    private CameraIO mCameraIO;
    private SimpleStreamSurfaceView liveViewSurfaceView;
    private Spinner apertureSpinner;
    private Spinner shutterSpinner;
    private Spinner isoSpinner;
    private Spinner focusModeSpinner;
    private Switch manualSwitch;
    private TextView exposureTextView;
    private ZoomControls zoomControls;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraIO = ((TimelapseApplication) getActivity().getApplication()).getCameraIO();

        View viewResult = inflater.inflate(R.layout.still_image_fragment, container, false);

        liveViewSurfaceView = (SimpleStreamSurfaceView) viewResult.findViewById(R.id.camera_settings_liveview);

        apertureSpinner = ((Spinner) viewResult.findViewById(R.id.apertureButton));

//        apertureSpinner


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

        return viewResult;    }

    @Override
    public Spanned getInformation() {
        return null;
    }
}
