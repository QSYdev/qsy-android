package com.qsy.terminal.fragments.executors;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.qsy.terminal.ExecutionActivity;
import com.qsy.terminal.R;
import com.qsy.terminal.services.LibterminalService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import libterminal.lib.routine.Color;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandle;
import libterminal.patterns.visitor.EventHandler;

public class PlayerExecutorFragment extends Fragment implements EventListener {

    private final EventHandler eventHandler = new InternalEventHandler();

	private Button mPlayerRedButton;
	private Button mPlayerGreenButton;
	private Button mPlayerBlueButton;
	private boolean mPlayerRedOn;
	private boolean mPlayerGreenOn;
	private boolean mPlayerBlueOn;
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
	private int mSelectedNode;

	private LibterminalService mLibterminalService;

	public static PlayerExecutorFragment newInstance(LibterminalService libterminalService) {
		PlayerExecutorFragment paf = new PlayerExecutorFragment();
		paf.setLibterminalService(libterminalService);
		return paf;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.player_executor_form, container, false);
		mAmountOfNodesSpinner = (Spinner) rootView.findViewById(R.id.amount_of_nodes_spinner);
		setAmountOfNodesSpinnerListener();

		mRoutineDurationButton = (Button) rootView.findViewById(R.id.routine_duration_bt);
		mNodeDelayButton = (Button) rootView.findViewById(R.id.node_delay_bt);
		mStepTimeoutButton = (Button) rootView.findViewById(R.id.step_timeout_bt);
		mStartRoutineButton = (Button) rootView.findViewById(R.id.start_routine);
		mPlayerRedButton = (Button) rootView.findViewById(R.id.player_button_red);
		mPlayerGreenButton = (Button) rootView.findViewById(R.id.player_button_green);
		mPlayerBlueButton = (Button) rootView.findViewById(R.id.player_button_blue);
		setupOnClickListeners();

		mRoutineDurationTextView = (TextView) rootView.findViewById(R.id.routine_duration_tv);
		mRoutineDurationTextView.setText(getString(R.string.routine_duration_in_seconds, 0));
		mNodeDelayTextView = (TextView) rootView.findViewById(R.id.node_delay_tv);
		mNodeDelayTextView.setText(getString(R.string.delay_in_miliseconds, 0));
		mStepTimeoutTextView = (TextView) rootView.findViewById(R.id.step_timeout_tv);
		mStepTimeoutTextView.setText(getString(R.string.timeout_in_miliseconds, 0));

		mWaitForAllSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.wait_for_all_sc);
		mStopOnTiemoutSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.stop_on_timeout_sc);
		mSoundSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.sound_sc);
		mTouchNodeSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.touch_node_sc);
		setupSwitchCompatListeners();

		mAmountOfStepsEditText = (EditText) rootView.findViewById(R.id.amount_of_steps_et);

		setAmountOfNodesSpinner();

		mNodeDelayValue = new BigInteger(String.valueOf(500));
		mNodeDelayTextView.setText(getString(R.string.delay_in_miliseconds, 500));

		mPlayerBlueButton.setText(getString(R.string.button_on));
		mPlayerBlueOn = true;

		mStepTimeoutValue = new BigInteger(String.valueOf(0));

		return rootView;
	}

	private void setAmountOfNodesSpinnerListener() {
		mAmountOfNodesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				mSelectedNode = i + 1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
	}

	@NonNull
	private void setAmountOfNodesSpinner() {
		int connectedNodes = mLibterminalService.getTerminal().connectedNodesAmount();
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
		mPlayerRedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerRedOn) {
					mPlayerRedButton.setText(getString(R.string.button_on));
					mPlayerRedOn = true;
				} else {
					mPlayerRedButton.setText(getString(R.string.button_off));
					mPlayerRedOn = false;
				}
			}
		});
		mPlayerGreenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerGreenOn) {
					mPlayerGreenButton.setText(getString(R.string.button_on));
					mPlayerGreenOn = true;
				} else {
					mPlayerGreenButton.setText(getString(R.string.button_off));
					mPlayerGreenOn = false;
				}
			}
		});
		mPlayerBlueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mPlayerBlueOn) {
					mPlayerBlueButton.setText(getString(R.string.button_on));
					mPlayerBlueOn = true;
				} else {
					mPlayerBlueButton.setText(getString(R.string.button_off));
					mPlayerBlueOn = false;
				}
			}
		});
		mRoutineDurationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				HmsPickerBuilder hpb = new HmsPickerBuilder()
					.setFragmentManager(getActivity().getSupportFragmentManager())
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
				try {
					mAmountOfStepsValue = Integer.parseInt(mAmountOfStepsEditText.getText().toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				ArrayList<Color> playersAndColors = new ArrayList<Color>();
				if (mPlayerRedOn)
					playersAndColors.add(Color.RED);
				if (mPlayerBlueOn)
					playersAndColors.add(Color.BLUE);
				if (mPlayerGreenOn)
					playersAndColors.add(Color.GREEN);

				if (mLibterminalService.getTerminal().connectedNodesAmount() < 1) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.not_enough_connected_nodes),
						Toast.LENGTH_LONG).show();
					return;
				}
				if(!mPlayerBlueOn && !mPlayerGreenOn && !mPlayerRedOn){
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.no_players_selected),
						Toast.LENGTH_LONG).show();
					return;
				}
				// TODO: chequear cual seria el tiempo minimo
				if(mAmountOfStepsValue < 1 && mRoutineDurationValue*1000 < 500) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.wrong_duration_or_steps),
						Toast.LENGTH_LONG).show();
					return;
				}
				try {
					// TODO: el primer parametro es la asociacion de nodos. En un futuro
					// vamos a tener la asociacion a nivel aplicacion
					mLibterminalService.getTerminal().executePlayer(null, mSelectedNode, playersAndColors,
						mWaitForAllValue, mStepTimeoutValue.longValue(), mNodeDelayValue.longValue(),
						mRoutineDurationValue * 1000, mAmountOfStepsValue, mStopOnTimeoutValue,
						mSoundValue, mTouchNodeValue);
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
				intent.putExtra("TYPE", "PLAYER");
				startActivity(intent);
			}
		});

	}

	@Override
	public void receiveEvent(final Event event) {
		if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                event.acceptHandler(eventHandler);
            }
        });
	}

	private void setLibterminalService(LibterminalService libterminalService) {
		this.mLibterminalService = libterminalService;
	}

	private final class InternalEventHandler extends EventHandler {

        @Override
        public void handle(final Event.NewNodeEvent newNodeEvent) {
            super.handle(newNodeEvent);
            setAmountOfNodesSpinner();
        }

        @Override
        public void handle(final Event.DisconnectedNodeEvent disconnectedNodeEvent) {
            super.handle(disconnectedNodeEvent);
            setAmountOfNodesSpinner();
        }
    }
}
