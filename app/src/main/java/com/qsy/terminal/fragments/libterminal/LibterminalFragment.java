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

import java.io.IOException;

import libterminal.api.TerminalAPI;
import libterminal.lib.node.Node;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class LibterminalFragment extends Fragment implements EventListener {

	private final EventHandler eventHandler = new InternalEventHandler();

	private SwitchCompat mLibterminalStartStopSW;
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
		mLibterminalStartStopSW = (SwitchCompat) rootView.findViewById(R.id.libterminal_start_sw);
		if (mTerminalAPI != null) {
			mLibterminalStartStopSW.setChecked(mTerminalAPI.isUp());
		}
		setupSwitchCompatListener();
		return rootView;
	}

	private void setupSwitchCompatListener() {
		mLibterminalStartStopSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mTerminalAPI == null) {
					Toast.makeText(getContext().getApplicationContext(),
						"Aun no se ha enlazado con la terminal",
						Toast.LENGTH_LONG).show();
					buttonView.setChecked(false);
					return;
				}
				if (isChecked) {
					try {
						if (mTerminalAPI.isUp()) {
							buttonView.setChecked(true);
							return;
						}
						mTerminalAPI.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mTerminalAPI.addListener(LibterminalFragment.this);
					mTerminalAPI.startNodesSearch();
				} else {
					try {
						if (!mTerminalAPI.isUp()) {
							buttonView.setChecked(false);
							return;
						}
						mTerminalAPI.removeListener(LibterminalFragment.this);
						mTerminalAPI.stop();
						Toast.makeText(getContext().getApplicationContext(),
							"Terminal apagada",
							Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
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

	private final class InternalEventHandler extends EventHandler {

        @Override
        public void handle(final Event.NewNodeEvent newNodeEvent) {
            super.handle(newNodeEvent);
            Node newNode = newNodeEvent.getNode();
            Toast.makeText(
                    getContext().getApplicationContext(),
                    getString(R.string.newNode, newNode.getNodeId()),
                    Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void handle(final Event.DisconnectedNodeEvent disconnectedNodeEvent) {
            super.handle(disconnectedNodeEvent);
            Node disconnectedNode = disconnectedNodeEvent.getNode();
            Toast.makeText(
                    getContext().getApplicationContext(),
                    getString(R.string.disconnectedNode, disconnectedNode.getNodeId()),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
