package com.qsy.terminal.fragments.libterminal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.qsy.terminal.R;
import com.qsy.terminal.services.LibterminalService;
import com.qsy.terminal.utils.QSYUtils;

import java.io.IOException;

import libterminal.lib.node.Node;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;

public class LibterminalFragment extends Fragment implements EventListener {
	private SwitchCompat mLibterminalStartStopSW;
	private LibterminalService mLibterminalService;

	public static LibterminalFragment newInstance(LibterminalService libterminalService) {
		LibterminalFragment libterminalFragment = new LibterminalFragment();
		libterminalFragment.setLibterminalService(libterminalService);
		return libterminalFragment;
	}

	public void setLibterminalService(LibterminalService libterminalService) {
		this.mLibterminalService = libterminalService;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.libterminal_connection, container, false);

		mLibterminalStartStopSW = (SwitchCompat) rootView.findViewById(R.id.libterminal_start_sw);
		setupSwitchCompatListener();
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		QSYUtils.checkWifiEnabled(getContext().getApplicationContext());
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mLibterminalService != null && mLibterminalService.getTerminal() != null) {
			mLibterminalStartStopSW.setChecked(mLibterminalService.getTerminal().isUp());
		}
		setupSwitchCompatListener();
	}

	private void setupSwitchCompatListener() {
		mLibterminalStartStopSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (mLibterminalService != null) {
						try {
							mLibterminalService.getTerminal().start();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mLibterminalService.getTerminal().addListener(LibterminalFragment.this);
						mLibterminalService.getTerminal().startNodesSearch();
					} else {
						Toast.makeText(getContext().getApplicationContext(),
							"Aun no se ha enlazado con la terminal",
							Toast.LENGTH_LONG).show();
						buttonView.setChecked(!isChecked);
					}
				} else {
					try {
						if(mLibterminalService != null) {
							mLibterminalService.getTerminal()
								.removeListener(LibterminalFragment.this);
							mLibterminalService.getTerminal().stop();
							Toast.makeText(getContext().getApplicationContext(),
								"Terminal apagada",
								Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getContext().getApplicationContext(),
								"Aun no se ha enlazado con la terminal",
								Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void receiveEvent(final Event event) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (event.getEventType()) {
					case newNode:
						Node newNode = (Node) event.getContent();
						Toast.makeText(
							getContext().getApplicationContext(),
							getString(R.string.newNode, newNode.getNodeId()),
							Toast.LENGTH_SHORT)
							.show();
						break;
					case disconnectedNode:
						Node disconnectedNode = (Node) event.getContent();
						Toast.makeText(
							getContext().getApplicationContext(),
							getString(R.string.disconnectedNode, disconnectedNode.getNodeId()),
							Toast.LENGTH_SHORT)
							.show();

				}
			}
		});
	}
}
