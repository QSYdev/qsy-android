package com.qsy.terminal.executors;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.qsy.terminal.R;

public class CustomExecutorFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
                addPreferencesFromResource(R.xml.custom_executor_preference);
        }
}
