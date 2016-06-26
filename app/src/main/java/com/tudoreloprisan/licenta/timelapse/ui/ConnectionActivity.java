package com.tudoreloprisan.licenta.timelapse.ui;

import android.app.Fragment;

import com.tudoreloprisan.licenta.timelapse.fragments.ConnectionFragment;

/**
 * Created by Doru on 6/25/2016.
 */
public class ConnectionActivity extends StartActivity {
    @Override
    protected Fragment createFragment() {
        return new ConnectionFragment();
    }
}
