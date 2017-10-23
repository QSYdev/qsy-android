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
import java.util.TreeMap;

import libterminal.api.TerminalAPI;
import libterminal.lib.routine.Routine;
import libterminal.utils.RoutineManager;

public class CustomExecutorFragment extends Fragment {

	private TerminalAPI mTerminal;

	private TextView mNumberOfNodesTV;
	private TextView mNumberOfStepsTV;
	private TextView mTotalTimeOutTV;
	private TextView mDescriptionTV;
	private Spinner mRoutineSP;
	private Button mStartRoutineBT;
	private SwitchCompat mSoundSC;
	private boolean mSoundValue;

	private Routine mRoutine = null;
	private TreeMap<String, Routine> mRoutines;

	public static CustomExecutorFragment newInstance(TerminalAPI api) {
		CustomExecutorFragment cef = new CustomExecutorFragment();
		cef.mTerminal = api;
		return cef;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] names;
		try {
			names = getContext().getApplicationContext().getAssets().list("routines");
		} catch (IOException e) {
			Toast.makeText(getContext().getApplicationContext(),
				getString(R.string.failed_open_file),
				Toast.LENGTH_SHORT).show();
			return;
		}
		mRoutines = new TreeMap<String, Routine>();
		for (String name : names) {
			File f = new File(getContext().getApplicationContext().getCacheDir() + "/" + name);
			if (!f.exists()) try {
				InputStream is = getContext().getApplicationContext().getAssets().open("routines/" + name);
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
			Routine routine = null;
			try {
				routine = RoutineManager.loadRoutine(f.getPath());
				mRoutines.put(routine.getName(), routine);
			} catch (IOException e) {
				e.printStackTrace();
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
		mDescriptionTV = (TextView) rootView.findViewById(R.id.custom_description);

		mSoundSC = (SwitchCompat) rootView.findViewById(R.id.custom_sound_sc);
		mSoundSC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSoundValue = isChecked;
			}
		});

		mStartRoutineBT = (Button) rootView.findViewById(R.id.start_custom_routine);
		setupOnClickListeners();

		mRoutineSP = (Spinner) rootView.findViewById(R.id.custom_routine_spinner);
		setupSpinner();

		mSoundValue = false;

		return rootView;
	}

	private void setupOnClickListeners() {
		mStartRoutineBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTerminal.connectedNodesAmount() < (int) mRoutine.getNumberOfNodes()) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.not_enough_connected_nodes),
						Toast.LENGTH_SHORT).show();
					return;
				}

				mTerminal.stopNodesSearch();
				mTerminal.executeCustom(mRoutine, null, mSoundValue, false);
				Intent intent = new Intent(getContext(), ExecutionActivity.class);
				startActivity(intent);
			}
		});
	}

	private void setupSpinner() {
		String[] names = new String[mRoutines.keySet().size()];
		names = mRoutines.keySet().toArray(names);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
			getActivity(),
			android.R.layout.simple_spinner_item,
			names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mRoutineSP.setAdapter(adapter);
		if (names.length > 0) {
			routineData(names[0]);
		}
		mRoutineSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String[] names = new String[mRoutines.keySet().size()];
				String name = mRoutines.keySet().toArray(names)[position];
				routineData(name);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void routineData(String name) {
		mRoutine = mRoutines.get(name);
		mDescriptionTV.setText(mRoutine.getDescription());
		mDescriptionTV.setVisibility(View.VISIBLE);
		mNumberOfNodesTV.setText(getResources().getQuantityString(R.plurals.custom_number_of_nodes, mRoutine.getNumberOfNodes(), mRoutine.getNumberOfNodes()));
		mNumberOfNodesTV.setVisibility(View.VISIBLE);
		mNumberOfStepsTV.setText(getResources().getQuantityString(R.plurals.custom_number_of_steps, mRoutine.getSteps().size(), mRoutine.getSteps().size()));
		mNumberOfStepsTV.setVisibility(View.VISIBLE);
		int secs = (int) mRoutine.getTotalTimeOut() / 1000;
		if (secs == 0) {
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
