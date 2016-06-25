package com.tudoreloprisan.licenta.timelapse.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.tudoreloprisan.licenta.R;
import com.tudoreloprisan.licenta.timelapse.fragments.ConnectionFragment;

/**
 * Created by Doru on 6/25/2016.
 */
public abstract class StartActivity extends FragmentActivity {
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
//        FragmentManager fragmentManager = getFragmentManager();
//        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
//        if (fragment == null) {
//            fragment = createFragment();
//            fragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
//        }
        if(savedInstanceState==null){
            ConnectionFragment fragment = new ConnectionFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment);

        }
    }
}
