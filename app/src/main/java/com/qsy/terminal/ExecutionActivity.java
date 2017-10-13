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

	private String mType;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.execution_activity);

		Intent intent = new Intent(ExecutionActivity.this, LibterminalService.class);
		intent.setAction("on");
		bindService(intent, mConnection, 0);

		if (savedInstanceState == null) {
			// Default to player if we have no savedInstanceState
			mType = "PLAYER";
		} else {
			mType = savedInstanceState.getString("TYPE");
		}

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
		/*
		 * Tener en cuenta que esto se llama desde el hilo de Libterminal, lo cual no se si es
		 * legal. Si algo raro pasa, mirar acá!
		 */
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				event.acceptHandler(eventHandler);
			}
		});
	}

	@Override
	public void routineCanceled() {
		libterminalService.getTerminal().stopExecution();
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
			mExecFragment.stopChronometer();
//			AlertDialog.Builder builder = new AlertDialog.Builder(ExecutionActivity.this);
//			builder.setTitle(R.string.routine_finished);
//			builder.setIcon(android.R.drawable.ic_dialog_alert);
//			builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
			if (mType == "PLAYER") {
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
//				}
//			});
//			AlertDialog d = builder.create();
//			d.show();
		}
	}
}