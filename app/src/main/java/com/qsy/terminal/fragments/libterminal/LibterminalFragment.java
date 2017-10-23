package com.qsy.terminal.fragments.libterminal;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.qsy.terminal.R;
import com.qsy.terminal.services.LibterminalService;

import java.io.IOException;

import libterminal.api.TerminalAPI;
import libterminal.lib.node.Node;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class LibterminalFragment extends Fragment {

	private OnFragmentInteractionListener mListener;

	private TextView mSearchStateTV;
	private Button mLibterminalStartStopButton;
	private TerminalAPI mTerminalAPI;

	public static LibterminalFragment newInstance(TerminalAPI terminal) {
		LibterminalFragment libterminalFragment = new LibterminalFragment();
		libterminalFragment.setTerminalAPI(terminal);
		return libterminalFragment;
	}

	public void setTerminalAPI(TerminalAPI terminal) {
		this.mTerminalAPI = terminal;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.libterminal_connection, container, false);
		mLibterminalStartStopButton = (Button) rootView.findViewById(R.id.libterminal_start_bt);
		setupOnClickListener();
		mSearchStateTV = (TextView) rootView.findViewById(R.id.libterminal_search_tv);
		if (mTerminalAPI != null) {
			if(!mTerminalAPI.isUp()) {
				mSearchStateTV.setText(getString(R.string.nodes_search));
				mLibterminalStartStopButton.setBackgroundColor(getResources().getColor(R.color.player_green));
				mLibterminalStartStopButton.setText(getString(R.string.start_search));
			} else {
				mSearchStateTV.setText(getString(R.string.searching_nodes));
				mLibterminalStartStopButton.setBackgroundColor(getResources().getColor(R.color.player_red));
				mLibterminalStartStopButton.setText(getString(R.string.stop_search));
			}
		} else {
			mSearchStateTV.setText(getString(R.string.nodes_search));
			mLibterminalStartStopButton.setBackgroundColor(getResources().getColor(R.color.player_green));
			mLibterminalStartStopButton.setText(getString(R.string.start_search));
		}
		return rootView;
	}

	private void setupOnClickListener() {
		mLibterminalStartStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mTerminalAPI == null) {
					Toast.makeText(getContext().getApplicationContext(),
						"Aun no se ha enlazado con la terminal",
						Toast.LENGTH_LONG).show();
					return;
				}
				if(mTerminalAPI.isUp()) {
					mSearchStateTV.setText(getString(R.string.nodes_search));
					mLibterminalStartStopButton.setBackgroundColor(getResources().getColor(R.color.player_green));
					mLibterminalStartStopButton.setText(getString(R.string.start_search));
					mListener.terminalOff();
					try {
						mTerminalAPI.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					mSearchStateTV.setText(getString(R.string.searching_nodes));
					mLibterminalStartStopButton.setBackgroundColor(getResources().getColor(R.color.player_red));
					mLibterminalStartStopButton.setText(getString(R.string.stop_search));
					try {
						mTerminalAPI.start();
						mTerminalAPI.startNodesSearch();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
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

	public interface OnFragmentInteractionListener {
		void terminalOff();
	}

}
