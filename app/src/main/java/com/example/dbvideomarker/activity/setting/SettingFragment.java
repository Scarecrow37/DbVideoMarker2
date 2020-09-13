package com.example.dbvideomarker.activity.setting;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.dbvideomarker.R;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }
}



