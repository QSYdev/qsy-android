package com.qsy.terminal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.qsy.terminal.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import libterminal.api.TerminalAPI;
import libterminal.lib.node.Node;
import libterminal.lib.protocol.CommandParameters;
import libterminal.lib.routine.Color;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class DebugFragment extends Fragment implements EventListener {

	private OnFragmentInteractionListener mListener;

	private final EventHandler eventHandler = new InternalEventHandler();
	private TerminalAPI mTerminal;
	private ArrayList<Node> mNodes;

	private Spinner mNodesSpinner;
	private Button mSendButton;
	private Button mTurnOffButton;
	private Button mButtonRed;
	private Button mButtonGreen;
	private Button mButtonBlue;
	private Button mButtonCyan;
	private Button mButtonMagenta;
	private boolean mRedV;
	private boolean mBlueV;
	private boolean mCyanV;
	private boolean mMagentaV;
	private boolean mGreenV;
	private List<Color> colors = new LinkedList<Color>();

	public static DebugFragment newInstance(TerminalAPI terminal) {
		DebugFragment df = new DebugFragment();
		terminal.addListener(df);
		df.mTerminal = terminal;
		return df;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		mNodes = mListener.getNodes();
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
				 @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_debug, container, false);

		mNodesSpinner = (Spinner) rootView.findViewById(R.id.nodes_spinner);
		setNodesSpinner();
		mSendButton = (Button) rootView.findViewById(R.id.send_packet);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Color color = colors.get(colors.size()-1);
				Integer nodeId = (Integer) mNodesSpinner.getSelectedItem();
				if(nodeId == null) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.not_enough_connected_nodes),
						Toast.LENGTH_SHORT).show();
					return;
				}
				CommandParameters cmd = new CommandParameters(nodeId, 0L, color, 0);
				mTerminal.sendPacket(nodeId, cmd, false, false);
			}
		});
		mTurnOffButton = (Button) rootView.findViewById(R.id.turn_node_off);
		mTurnOffButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer nodeId = (Integer) mNodesSpinner.getSelectedItem();
				if(nodeId == null) {
					Toast.makeText(getContext().getApplicationContext(),
						getString(R.string.not_enough_connected_nodes),
						Toast.LENGTH_SHORT).show();
					return;
				}
				CommandParameters cmd = new CommandParameters(nodeId, 0L, Color.NO_COLOR, 0);
				mTerminal.sendPacket(nodeId, cmd, false, false);
			}
		});
		mButtonBlue = (Button) rootView.findViewById(R.id.button_blue);
		mButtonBlue.setText(getString(R.string.switch_off));
		mButtonRed = (Button) rootView.findViewById(R.id.button_red);
		mButtonRed.setText(getString(R.string.switch_off));
		mButtonGreen = (Button) rootView.findViewById(R.id.button_green);
		mButtonGreen.setText(getString(R.string.switch_off));
		mButtonCyan = (Button) rootView.findViewById(R.id.button_cyan);
		mButtonCyan.setText(getString(R.string.switch_off));
		mButtonMagenta = (Button) rootView.findViewById(R.id.button_magenta);
		mButtonMagenta.setText(getString(R.string.switch_off));
		mBlueV = false;
		mRedV = false;
		mGreenV = false;
		mMagentaV = false;
		mCyanV = false;
		setupOnClickListeners();

		return rootView;
	}


	private void setupOnClickListeners() {
		mButtonBlue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mBlueV) {
					mButtonBlue.setText(getString(R.string.switch_off));
					mBlueV = false;
				} else {
					turnOffAll();
					mButtonBlue.setText(getString(R.string.switch_on));
					mBlueV = true;
					colors.add(Color.BLUE);
				}
			}
		});
		mButtonRed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mRedV) {
					mButtonRed.setText(getString(R.string.switch_off));
					mRedV = false;
				} else {
					turnOffAll();
					mButtonRed.setText(getString(R.string.switch_on));
					mRedV = true;
					colors.add(Color.RED);
				}
			}
		});
		mButtonCyan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mBlueV) {
					mButtonCyan.setText(getString(R.string.switch_off));
					mCyanV = false;
				} else {
					turnOffAll();
					mButtonCyan.setText(getString(R.string.switch_on));
					mCyanV = true;
					colors.add(Color.CYAN);
				}
			}
		});
		mButtonMagenta.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mMagentaV) {
					mButtonMagenta.setText(getString(R.string.switch_off));
					mMagentaV = false;
				} else {
					turnOffAll();
					mButtonMagenta.setText(getString(R.string.switch_on));
					mMagentaV = true;
					colors.add(Color.MAGENTA);
				}
			}
		});
		mButtonGreen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mGreenV) {
					mButtonGreen.setText(getString(R.string.switch_off));
					mGreenV = false;
				} else {
					turnOffAll();
					mButtonGreen.setText(getString(R.string.switch_on));
					mGreenV = true;
					colors.add(Color.GREEN);
				}
			}
		});
	}

	private void turnOffAll() {
		mGreenV = false;mRedV = false;mBlueV = false;mCyanV = false;mMagentaV = false;
		mButtonBlue.setText(getString(R.string.switch_off));
		mButtonRed.setText(getString(R.string.switch_off));
		mButtonCyan.setText(getString(R.string.switch_off));
		mButtonGreen.setText(getString(R.string.switch_off));
		mButtonMagenta.setText(getString(R.string.switch_off));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void receiveEvent(Event event) {
		event.acceptHandler(eventHandler);
	}

	public interface OnFragmentInteractionListener {
		ArrayList<Node> getNodes();
	}

	private Integer[] getNodeIds() {
		Integer[] ints = new Integer[mNodes.size()];
		int i = 0;
		for (Node node : mNodes) {
			ints[i] = node.getNodeId();
		}
		return ints;
	}

	private void setNodesSpinner() {
		Integer[] ints = getNodeIds();
		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
			getActivity(),
			android.R.layout.simple_spinner_item,
			ints);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mNodesSpinner.setAdapter(adapter);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
				+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private final class InternalEventHandler extends EventHandler {

		@Override
		public void handle(final Event.NewNodeEvent newNodeEvent) {
			super.handle(newNodeEvent);
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setNodesSpinner();
				}
			});
		}

		@Override
		public void handle(final Event.DisconnectedNodeEvent disconnectedNodeEvent) {
			super.handle(disconnectedNodeEvent);
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setNodesSpinner();
				}
			});
		}
	}
}
