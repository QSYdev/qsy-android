package com.qsy.terminal.fragments.executors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.qsy.terminal.ExecutionActivity;
import com.qsy.terminal.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import libterminal.api.TerminalAPI;
import libterminal.lib.routine.Color;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class PlayerExecutorFragment extends Fragment implements EventListener {

	private final EventHandler eventHandler = new InternalEventHandler();

	private Button mPlayerRedButton;
	private Button mPlayerGreenButton;
	private Button mPlayerBlueButton;
	private Button mPlayerCyanButton;
	private Button mPlayerMagentaButton;
	private Button mAmountOfStepsButton;
	private boolean mPlayerRedOn;
	private boolean mPlayerGreenOn;
	private boolean mPlayerBlueOn;
	private boolean mPlayerCyanOn;
	private boolean mPlayerMagentaOn;
	private Spinner mAmountOfNodesSpinner;
	private Button mRoutineDurationButton;
	private SwitchCompat mWaitForAllSwitchCompat;
	private SwitchCompat mStopOnTimeOutSwitchCompat;
	private SwitchCompat mSoundSwitchCompat;
	private Button mNodeDelayButton;
	private Button mStepTimeoutButton;
	private BigInteger mStepTimeoutValue;
	private BigInteger mNodeDelayValue;
	private int mRoutineDurationValue;
	private Button mStartRoutineButton;
	private boolean mWaitForAllValue;
	private boolean mStopOnTimeoutValue;
	private boolean mSoundValue;
	private int mAmountOfStepsValue;
	private TextView mAmountOfStepsTV;
	private TextView mNodeDelayTextView;
	private TextView mStepTimeoutTextView;
	private TextView mRoutineDurationTextView;
	private int mSelectedNode;
	private TerminalAPI mTerminal;
	private List<Color> selectedColorsQueue;

	public static PlayerExecutorFragment newInstance(TerminalAPI terminalAPI) {
        if (terminalAPI == null)
			throw new IllegalArgumentException("<< PlayerExecutorFragment >> terminalAPI es nula!");
		PlayerExecutorFragment pef = new PlayerExecutorFragment();
		pef.mTerminal = terminalAPI;
		terminalAPI.addListener(pef);
		return pef;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.player_executor_form, container, false);
		mAmountOfNodesSpinner = (Spinner) rootView.findViewById(R.id.amount_of_nodes_spinner);
		selectedColorsQueue = new ArrayList<Color>();
		setAmountOfNodesSpinnerListener();

		mAmountOfStepsButton = (Button) rootView.findViewById(R.id.amount_of_steps_bt);
		mRoutineDurationButton = (Button) rootView.findViewById(R.id.routine_duration_bt);
		mNodeDelayButton = (Button) rootView.findViewById(R.id.node_delay_bt);
		mStepTimeoutButton = (Button) rootView.findViewById(R.id.step_timeout_bt);
		mStartRoutineButton = (Button) rootView.findViewById(R.id.start_routine);
		mPlayerRedButton = (Button) rootView.findViewById(R.id.player_button_red);
		mPlayerGreenButton = (Button) rootView.findViewById(R.id.player_button_green);
		mPlayerBlueButton = (Button) rootView.findViewById(R.id.player_button_blue);
		mPlayerCyanButton = (Button) rootView.findViewById(R.id.player_button_cyan);
		mPlayerMagentaButton = (Button) rootView.findViewById(R.id.player_button_magenta);
		setupOnClickListeners();

		mAmountOfStepsTV = (TextView) rootView.findViewById(R.id.amount_of_steps_tv);
		mAmountOfStepsTV.setText(getResources().getQuantityString(R.plurals.amount_of_steps,
			mAmountOfStepsValue, mAmountOfStepsValue));
		mAmountOfStepsValue = 0;
		mRoutineDurationTextView = (TextView) rootView.findViewById(R.id.routine_duration_tv);
		mRoutineDurationTextView.setText(getString(R.string.routine_duration_in_seconds, 0));
		mNodeDelayTextView = (TextView) rootView.findViewById(R.id.node_delay_tv);
		mNodeDelayTextView.setText(getString(R.string.delay_in_miliseconds, 0));
		mStepTimeoutTextView = (TextView) rootView.findViewById(R.id.step_timeout_tv);
		mStepTimeoutTextView.setText(getString(R.string.timeout_in_miliseconds, 0));

		mWaitForAllSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.wait_for_all_sc);
		mStopOnTimeOutSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.stop_on_timeout_sc);
		mSoundSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.sound_sc);
		setupSwitchCompatListeners();

		setAmountOfNodesSpinner();

		mRoutineDurationValue = 0;

		mNodeDelayValue = new BigInteger(String.valueOf(500));
		mNodeDelayTextView.setText(getString(R.string.delay_in_miliseconds, 500));

		mStepTimeoutValue = new BigInteger(String.valueOf(0));

		mSoundValue = false;
		return rootView;
	}

	private void setAmountOfNodesSpinnerListener() {
		mAmountOfNodesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				mSelectedNode = i + 1;
				while(selectedColorsQueue.size() > mSelectedNode){
					removeFirstColorQueue();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
	}

	@NonNull
	private void setAmountOfNodesSpinner() {
		int connectedNodes = mTerminal.connectedNodesAmount();
		Integer[] ints = new Integer[connectedNodes];
		for (int i = 0; i < connectedNodes; i++) ints[i] = i + 1;

		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
			getActivity(),
			android.R.layout.simple_spinner_item,
			ints);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAmountOfNodesSpinner.setAdapter(adapter);

	}

	private void setupSwitchCompatListeners() {
		mWaitForAllSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mWaitForAllValue = b;
			}
		});
		mStopOnTimeOutSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void addColorQueue(Color color){
		selectedColorsQueue.add(color);
		removeFirstColorQueue();
	}

	private void removeFirstColorQueue(){
		if(selectedColorsQueue.size() > mSelectedNode){
			Color aux = selectedColorsQueue.get(0);
			selectedColorsQueue.remove(0);
			if(aux.equals(Color.RED)){
				mPlayerRedButton.setText(getString(R.string.button_off));
				mPlayerRedOn = false;
			}else if(aux.equals(Color.GREEN)){
				mPlayerGreenButton.setText(getString(R.string.button_off));
				mPlayerGreenOn = false;
			}else if(aux.equals(Color.BLUE)){
				mPlayerBlueButton.setText(getString(R.string.button_off));
				mPlayerBlueOn = false;
			}else if(aux.equals(Color.CYAN)){
				mPlayerCyanButton.setText(getString(R.string.button_off));
				mPlayerCyanOn = false;
			}else if(aux.equals(Color.MAGENTA)){
				mPlayerMagentaButton.setText(getString(R.string.button_off));
				mPlayerMagentaOn = false;
			}
		}
	}

	private void setupOnClickListeners() {
		mPlayerRedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerRedOn) {
					addColorQueue(Color.RED);
					mPlayerRedButton.setText(getString(R.string.button_on));
					mPlayerRedOn = true;
				} else {
					selectedColorsQueue.remove(Color.RED);
					mPlayerRedButton.setText(getString(R.string.button_off));
					mPlayerRedOn = false;
				}
			}
		});
		mPlayerGreenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerGreenOn) {
					addColorQueue(Color.GREEN);
					mPlayerGreenButton.setText(getString(R.string.button_on));
					mPlayerGreenOn = true;
				} else {
					selectedColorsQueue.remove(Color.GREEN);
					mPlayerGreenButton.setText(getString(R.string.button_off));
					mPlayerGreenOn = false;
				}
			}
		});
		mPlayerBlueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerBlueOn) {
					addColorQueue(Color.BLUE);
					mPlayerBlueButton.setText(getString(R.string.button_on));
					mPlayerBlueOn = true;
				} else {
					selectedColorsQueue.remove(Color.BLUE);
					mPlayerBlueButton.setText(getString(R.string.button_off));
					mPlayerBlueOn = false;
				}
			}
		});
		mPlayerCyanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerCyanOn) {
					addColorQueue(Color.CYAN);
					mPlayerCyanButton.setText(getString(R.string.button_on));
					mPlayerCyanOn = true;
				} else {
					selectedColorsQueue.remove(Color.CYAN);
					mPlayerCyanButton.setText(getString(R.string.button_off));
					mPlayerCyanOn = false;
				}
			}
		});
		mPlayerMagentaButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerMagentaOn) {
					addColorQueue(Color.MAGENTA);
					mPlayerMagentaButton.setText(getString(R.string.button_on));
					mPlayerMagentaOn = true;
				} else {
					selectedColorsQueue.remove(Color.MAGENTA);
					mPlayerMagentaButton.setText(getString(R.string.button_off));
					mPlayerMagentaOn = false;
				}
			}
		});
		mRoutineDurationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				HmsPickerBuilder hpb = new HmsPickerBuilder()
					.setFragmentManager(getActivity().getSupportFragmentManager())
					.setTimeInSeconds(mRoutineDurationValue)
					.addHmsPickerDialogHandler(new HmsPickerDialogFragment.HmsPickerDialogHandlerV2() {
						@Override
						public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
							mRoutineDurationValue = seconds + (minutes * 60);
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
					.setCurrentNumber(mNodeDelayValue.intValue())
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
					.setCurrentNumber(mStepTimeoutValue.intValue())
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
		mAmountOfStepsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final NumberPicker picker = new NumberPicker(getContext());
				picker.setMinValue(0);
				picker.setMaxValue(250);
				picker.setValue(mAmountOfStepsValue);
				final FrameLayout layout = new FrameLayout(getContext());
				layout.addView(picker, new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER));
				new AlertDialog.Builder(getContext())
					.setView(layout)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							mAmountOfStepsValue = picker.getValue();
							mAmountOfStepsTV.setText(getResources().getQuantityString(R.plurals.amount_of_steps,
								mAmountOfStepsValue, mAmountOfStepsValue));
						}
					})
					.setTitle(getString(R.string.amount_of_steps))
					.setNegativeButton(android.R.string.cancel, null)
					.show();
			}
		});
		mStartRoutineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ArrayList<Color> playersAndColors = new ArrayList<Color>();
				if (mPlayerRedOn)
					playersAndColors.add(Color.RED);
				if (mPlayerBlueOn)
					playersAndColors.add(Color.BLUE);
				if (mPlayerGreenOn)
					playersAndColors.add(Color.GREEN);
				if (mPlayerCyanOn)
					playersAndColors.add(Color.CYAN);
				if (mPlayerMagentaOn)
					playersAndColors.add(Color.MAGENTA);

				if (mTerminal.connectedNodesAmount() < 1) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.not_enough_connected_nodes),
						Toast.LENGTH_LONG).show();
					return;
				}
				if (!mPlayerBlueOn && !mPlayerGreenOn && !mPlayerRedOn && !mPlayerCyanOn && !mPlayerMagentaOn) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.no_players_selected),
						Toast.LENGTH_LONG).show();
					return;
				}
				// TODO: chequear cual seria el tiempo minimo
				if (mAmountOfStepsValue < 1 && mRoutineDurationValue * 1000 < 500) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.wrong_duration_or_steps),
						Toast.LENGTH_LONG).show();
					return;
				}
				try {
					// TODO: el primer parametro es la asociacion de nodos. En un futuro
					// vamos a tener la asociacion a nivel aplicacion
					mTerminal.executePlayer(null, mSelectedNode, playersAndColors,
						mWaitForAllValue, mStepTimeoutValue.longValue(), mNodeDelayValue.longValue(),
						mRoutineDurationValue * 1000, mAmountOfStepsValue, mStopOnTimeoutValue,
						mSoundValue, false);
				} catch (IllegalStateException e) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.routine_currently_executing),
						Toast.LENGTH_SHORT).show();
					return;
				} catch (IllegalArgumentException e) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.wrong_player_arguments),
						Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent(getContext(), ExecutionActivity.class);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTerminal.removeListener(this);
	}

	@Override
	public void receiveEvent(final Event event) {
		if (getActivity() == null) return;
		event.acceptHandler(eventHandler);
	}

	private final class InternalEventHandler extends EventHandler {

		@Override
		public void handle(final Event.NewNodeEvent newNodeEvent) {
			super.handle(newNodeEvent);
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setAmountOfNodesSpinner();
				}
			});
		}

		@Override
		public void handle(final Event.DisconnectedNodeEvent disconnectedNodeEvent) {
			super.handle(disconnectedNodeEvent);
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setAmountOfNodesSpinner();
				}
			});
		}
	}
}
