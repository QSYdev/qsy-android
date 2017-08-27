package com.qsy.terminal.executors;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.qsy.terminal.R;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PlayerExecutorFragment extends Fragment {
	private Spinner mAmountOfNodesSpinner;
	private EditText mAmountOfStepsEditText;
	private Button mRoutineDurationButton;
	private SwitchCompat mWaitForAllSwitchCompat;
	private SwitchCompat mStopOnTiemoutSwitchCompat;
	private SwitchCompat mSoundSwitchCompat;
	private SwitchCompat mTouchNodeSwitchCompat;
	private Button mNodeDelayButton;
	private Button mStepTimeoutButton;
	private BigInteger mStepTimeoutValue;
	private BigInteger mNodeDelayValue;
	private int mRoutineDurationValue;
	private Button mStartRoutineButton;
	private boolean mWaitForAllValue;
	private boolean mStopOnTimeoutValue;
	private boolean mSoundValue;
	private boolean mTouchNodeValue;
	private int mAmountOfStepsValue;
	private TextView mNodeDelayTextView;
	private TextView mStepTimeoutTextView;
	private TextView mRoutineDurationTextView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.player_executor_form, container, false);
		mAmountOfNodesSpinner = rootView.findViewById(R.id.amount_of_nodes_spinner);

		mRoutineDurationButton = rootView.findViewById(R.id.routine_duration_bt);
		mNodeDelayButton = rootView.findViewById(R.id.node_delay_bt);
		mStepTimeoutButton = rootView.findViewById(R.id.step_timeout_bt);
		mStartRoutineButton = rootView.findViewById(R.id.start_routine);
		setupOnClickListeners();

		mRoutineDurationTextView = rootView.findViewById(R.id.routine_duration_tv);
		mRoutineDurationTextView.setText(getString(R.string.routine_duration_in_seconds, 0));
		mNodeDelayTextView = rootView.findViewById(R.id.node_delay_tv);
		mNodeDelayTextView.setText(getString(R.string.delay_in_miliseconds, 0));
		mStepTimeoutTextView = rootView.findViewById(R.id.step_timeout_tv);
		mStepTimeoutTextView.setText(getString(R.string.timeout_in_miliseconds, 0));

		mWaitForAllSwitchCompat = rootView.findViewById(R.id.wait_for_all_sc);
		mStopOnTiemoutSwitchCompat = rootView.findViewById(R.id.stop_on_timeout_sc);
		mSoundSwitchCompat = rootView.findViewById(R.id.sound_sc);
		mTouchNodeSwitchCompat = rootView.findViewById(R.id.touch_node_sc);
		setupSwitchCompatListeners();

		mAmountOfStepsEditText = rootView.findViewById(R.id.amount_of_steps_et);

		Integer[] ints = new Integer[5];
		// TODO: el array va a ser construido usando la cantidad de nodos conectados
		for (int i = 0; i < 5; i++) ints[i] = i + 1;

		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
			getActivity(),
			android.R.layout.simple_spinner_item,
			ints);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAmountOfNodesSpinner.setAdapter(adapter);

		mStepTimeoutValue = new BigInteger(String.valueOf(0));
		mNodeDelayValue = new BigInteger(String.valueOf(0));
		mRoutineDurationValue = 0;

		mWaitForAllValue = false;
		mStopOnTimeoutValue = false;
		mSoundValue = false;
		mTouchNodeValue = false;

		mAmountOfStepsValue = 0;

		return rootView;
	}

	private void setupSwitchCompatListeners() {
		mWaitForAllSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mWaitForAllValue = b;
			}
		});
		mStopOnTiemoutSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mStopOnTimeoutValue = b;
			}
		});
		mSoundSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mSoundValue = b;
			}
		});
		mTouchNodeSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mTouchNodeValue = b;
			}
		});
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void setupOnClickListeners() {
		mRoutineDurationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				HmsPickerBuilder hpb = new HmsPickerBuilder()
					.setFragmentManager(getActivity().getSupportFragmentManager())
					.addHmsPickerDialogHandler(new HmsPickerDialogFragment.HmsPickerDialogHandlerV2() {
						@Override
						public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
							mRoutineDurationValue = seconds + (minutes*60);
							mRoutineDurationTextView.setText(getString(
								R.string.routine_duration_in_seconds,
								mRoutineDurationValue
							));
						}
					})
					.setStyleResId(R.style.BetterPickersDialogFragment_Light);
				hpb.show();
			}
		});
		mNodeDelayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: chequear max number
				NumberPickerBuilder npb = new NumberPickerBuilder()
					.setFragmentManager(getActivity().getSupportFragmentManager())
					.setStyleResId(R.style.BetterPickersDialogFragment_Light)
					.addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
						@Override
						public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
							mNodeDelayValue = fullNumber.toBigInteger();
							mNodeDelayTextView.setText(getString(
								R.string.delay_in_miliseconds,
								mNodeDelayValue
							));
						}
					})
					.setMinNumber(BigDecimal.valueOf(0));
				npb.show();
			}
		});
		mStepTimeoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				NumberPickerBuilder npb = new NumberPickerBuilder()
					.setFragmentManager(getActivity().getSupportFragmentManager())
					.setStyleResId(R.style.BetterPickersDialogFragment_Light)
					.addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
						@Override
						public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
							mStepTimeoutValue = fullNumber.toBigInteger();
							mStepTimeoutTextView.setText(getString(
								R.string.timeout_in_miliseconds,
								mStepTimeoutValue
							));
						}
					})
					.setMinNumber(BigDecimal.valueOf(0));
				npb.show();
			}
		});
		mStartRoutineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: aca habria que empezar la rutina
				// TODO: arreglar esto plizzz
				try {
					mAmountOfStepsValue = Integer.parseInt(mAmountOfStepsEditText.getText().toString());
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});

	}

}
