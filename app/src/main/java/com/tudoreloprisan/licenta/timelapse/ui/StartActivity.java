package com.tudoreloprisan.licenta.timelapse.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.tudoreloprisan.licenta.R;

/**
 * Created by Doru on 6/25/2016.
 */
public abstract class StartActivity extends Activity {
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = createFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
//        if(savedInstanceState==null){
//            ConnectionFragment fragment = new ConnectionFragment();
//            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment);
//
//        }
    }
}
