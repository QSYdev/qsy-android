package com.qsy.terminal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.qsy.terminal.services.LibterminalService;

import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;

public class ExecutionActivity extends AppCompatActivity implements EventListener {

	private LibterminalService libterminalService;
	private ExecutionActivityConnection mConnection = new ExecutionActivityConnection();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.execution_activity);

		Intent intent = new Intent(ExecutionActivity.this, LibterminalService.class);
		intent.setAction("on");
		// TODO: chequear si hace falta hacer un "startService"
		bindService(intent, mConnection, 0);
	}

	@Override
	public void receiveEvent(Event event) {
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
			// TODO: HAY QUE HACER STOP?? O ALCANZA CON MAIN ACTIVITY???
		}
	}
}
