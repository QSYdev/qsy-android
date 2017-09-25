package com.qsy.terminal;

import android.support.annotation.Nullable;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.qsy.terminal.fragments.ExecutionFragment;
import com.qsy.terminal.services.LibterminalService;

import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;

public class ExecutionActivity extends AppCompatActivity implements EventListener,
		ExecutionFragment.OnFragmentInteractionListener {

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
	public void receiveEvent(Event event) {
		/*
		 * Tener en cuenta que esto se llama desde el hilo de Libterminal, lo cual no se si es
		 * legal. Si algo raro pasa, mirar acá!
		 */
		switch (event.getEventType()) {
			case routineFinished:
				// TODO: inicializar ResultsFragment
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO: transaction de ResultsFragment
					}
				});
				break;
		}
	}

	@Override
	public void routineCanceled() {
		libterminalService.getTerminal().stopExecution();
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
}
