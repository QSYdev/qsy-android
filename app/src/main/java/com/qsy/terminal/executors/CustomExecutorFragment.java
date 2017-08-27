package com.qsy.terminal.executors;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.qsy.terminal.R;

public class CustomExecutorFragment extends PreferenceFragmentCompat implements AdapterView.OnItemSelectedListener {
        private AppCompatSpinner mAmountOfNodes;
        private PreferenceScreen mPreferenceScreen;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
                addPreferencesFromResource(R.xml.custom_executor_preference);
                mPreferenceScreen = this.getPreferenceScreen();
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
}
