package com.qsy.terminal;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.qsy.terminal.fragments.CustomResultsFragment;
import com.qsy.terminal.fragments.ExecutionFragment;
import com.qsy.terminal.fragments.PlayerResultsFragment;
import com.qsy.terminal.services.LibterminalService;

import libterminal.lib.results.CustomResults;
import libterminal.lib.results.PlayersResults;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class ExecutionActivity extends AppCompatActivity implements EventListener,
	ExecutionFragment.OnFragmentInteractionListener, PlayerResultsFragment.OnFragmentInteractionListener,
	CustomResultsFragment.OnFragmentInteractionListener {

	private final EventHandler eventHandler = new InternalEventHandler();

	private LibterminalService libterminalService;
	private ExecutionActivityConnection mConnection = new ExecutionActivityConnection();
	private ExecutionFragment mExecFragment;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.execution_activity);

		Intent intent = new Intent(ExecutionActivity.this, LibterminalService.class);
		intent.setAction("on");
		bindService(intent, mConnection, 0);

		mExecFragment = new ExecutionFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mExecFragment)
			.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (libterminalService != null) {
			libterminalService.getTerminal().addListener(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mConnection);
		if (libterminalService != null) {
			libterminalService.getTerminal().removeListener(this);
		}
	}

	@Override
	public void receiveEvent(final Event event) {
		event.acceptHandler(eventHandler);
	}

	@Override
	public void routineCanceled() {
		try {
			libterminalService.getTerminal().stopExecution();
		} catch(IllegalStateException e) {
			Log.d("ExecutionActivity", e.getMessage());
		}
		libterminalService.getTerminal().startNodesSearch();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mExecFragment.stopChronometer();
			}
		});
	}

	private void resultsDone() {
		ExecutionActivity.this.finish();
	}

	@Override
	public void playerResultsDone() {
		resultsDone();
	}

	@Override
	public void customResultsDone() {
		resultsDone();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		try {
			libterminalService.getTerminal().stopExecution();
		} catch(IllegalStateException e) {
			Log.d("ExecutionActivity", e.getMessage());
		}
		libterminalService.getTerminal().startNodesSearch();
		finish();
	}

	private final class ExecutionActivityConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LibterminalService.LocalBinder binder = (LibterminalService.LocalBinder) service;
			libterminalService = (LibterminalService) binder.getService();
			libterminalService.getTerminal().addListener(ExecutionActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			libterminalService.getTerminal().removeListener(ExecutionActivity.this);
			/*
			 * TODO: este método se ejecuta cuando por alguna razón se perdio el Service. Habría que
			 * manejar esto
			 */
		}
	}

	private final class InternalEventHandler extends EventHandler {

		@Override
		public void handle(final Event.RoutineFinishedEvent routineFinishedEvent) {
			super.handle(routineFinishedEvent);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mExecFragment.stopChronometer();
					if (routineFinishedEvent.getResults() instanceof PlayersResults) {
						PlayersResults r = (PlayersResults) routineFinishedEvent.getResults();
						getSupportFragmentManager().
							beginTransaction().
							replace(R.id.fragment_frame, PlayerResultsFragment.newInstance(r)).
							commit();
					} else {
						CustomResults r = (CustomResults) routineFinishedEvent.getResults();
						getSupportFragmentManager().
							beginTransaction().
							replace(R.id.fragment_frame, CustomResultsFragment.newInstance(r)).
							commit();
					}
				}
			});
		}

		@Override
		public void handle(final Event.DisconnectedNodeEvent disconnectedNodeEvent) {
			super.handle(disconnectedNodeEvent);

			try {
				libterminalService.getTerminal().stopExecution();
			} catch(IllegalStateException e) {
				Log.d("ExeuctionActivity", e.getMessage());
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mExecFragment.stopChronometer();
					libterminalService.getTerminal().startNodesSearch();
				}
			});
		}
	}
}