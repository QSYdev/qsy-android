package com.qsy.terminal.fragments.executors;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qsy.terminal.ExecutionActivity;
import com.qsy.terminal.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import libterminal.api.TerminalAPI;
import libterminal.lib.routine.Routine;
import libterminal.utils.RoutineManager;

public class CustomExecutorFragment extends Fragment {

	private TerminalAPI mTerminal;

	private TextView mNumberOfNodesTV;
	private TextView mNumberOfStepsTV;
	private TextView mTotalTimeOutTV;
	private Spinner mRoutineSP;
	private Button mStartRoutineBT;
	private SwitchCompat mSoundSC;
	private SwitchCompat mTouchSC;

	private Routine mRoutine = null;
	private String[] names;

	public static CustomExecutorFragment newInstance(TerminalAPI api) {
		CustomExecutorFragment cef = new CustomExecutorFragment();
		cef.mTerminal = api;
		return cef;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			names = getContext().getApplicationContext().getAssets().list("routines");
		} catch (IOException e) {
			Toast.makeText(getContext().getApplicationContext(),
				getString(R.string.failed_open_file),
				Toast.LENGTH_SHORT).show();
			return;
		}
		for (int i = 0; i < names.length; i++) {
			File f = new File(getContext().getApplicationContext().getCacheDir() + "/" + names[i]);
			if (!f.exists()) try {
				InputStream is = getContext().getApplicationContext().getAssets().open("routines/"+names[i]);
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
				 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_custom_executor, container, false);
		mNumberOfNodesTV = (TextView) rootView.findViewById(R.id.custom_number_of_nodes);
		mNumberOfStepsTV = (TextView) rootView.findViewById(R.id.custom_number_of_steps);
		mTotalTimeOutTV = (TextView) rootView.findViewById(R.id.custom_total_time_out);

		mSoundSC = (SwitchCompat)  rootView.findViewById(R.id.custom_sound_sc);
		mTouchSC = (SwitchCompat)  rootView.findViewById(R.id.custom_touch_node_sc);

		mStartRoutineBT = (Button) rootView.findViewById(R.id.start_custom_routine);
		setupOnClickListeners();

		mRoutineSP = (Spinner) rootView.findViewById(R.id.custom_routine_spinner);
		setupSpinner();

		return rootView;
	}

	private void setupOnClickListeners() {
		mStartRoutineBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mTerminal.connectedNodesAmount() < (int)mRoutine.getNumberOfNodes()) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.not_enough_connected_nodes),
						Toast.LENGTH_SHORT).show();
					return;
				}

				mTerminal.executeCustom(mRoutine, null, mSoundSC.isChecked(), mTouchSC.isChecked());
				Intent intent = new Intent(getContext(), ExecutionActivity.class);
				startActivity(intent);
			}
		});
	}

	private void setupSpinner() {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
			getActivity(),
			android.R.layout.simple_spinner_item,
			names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mRoutineSP.setAdapter(adapter);
		if (names.length > 0) {
			routineData(0);
		}
		mRoutineSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				routineData(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void routineData(int position) {
		try {
			mRoutine = RoutineManager.loadRoutine(getContext().getApplicationContext().getCacheDir() + "/" + names[position]);
		} catch (IOException e) {
			Toast.makeText(getContext().getApplicationContext(),
				getString(R.string.failed_open_file),
				Toast.LENGTH_SHORT).show();
			return;
		}


		mNumberOfNodesTV.setText(getResources().getQuantityString(R.plurals.custom_number_of_nodes, mRoutine.getNumberOfNodes(), mRoutine.getNumberOfNodes()));
		mNumberOfNodesTV.setVisibility(View.VISIBLE);
		mNumberOfStepsTV.setText(getResources().getQuantityString(R.plurals.custom_number_of_steps, mRoutine.getSteps().size(), mRoutine.getSteps().size()));
		mNumberOfStepsTV.setVisibility(View.VISIBLE);
		int secs = (int)mRoutine.getTotalTimeOut()/1000;
		if(secs == 0) {
			mTotalTimeOutTV.setText(getString(R.string.time_not_configured));
		} else {
			mTotalTimeOutTV.setText(getResources().getQuantityString(R.plurals.custom_total_time_out, secs, secs));
		}
		mTotalTimeOutTV.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}