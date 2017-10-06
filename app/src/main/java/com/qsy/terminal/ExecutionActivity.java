package com.qsy.terminal;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.qsy.terminal.services.LibterminalService;

import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class ExecutionActivity extends AppCompatActivity implements EventListener {

	private final EventHandler eventHandler = new InternalEventHandler();

	private LibterminalService libterminalService;
	private ExecutionActivityConnection mConnection = new ExecutionActivityConnection();
	private boolean mRoutineWasCanceled = false;
	private Chronometer mChronometer;
	private TextView mStatus;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.execution_activity);

		Intent intent = new Intent(ExecutionActivity.this, LibterminalService.class);
		intent.setAction("on");
		bindService(intent, mConnection, 0);

		Button button = (Button) findViewById(R.id.cancelButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: Poner primero un cuadro de confirmación
				libterminalService.getTerminal().stopExecution();
				doStopRoutine(true);
				ExecutionActivity.this.finish();
			}
		});
		mChronometer = (Chronometer) findViewById(R.id.chronometer2);
		mChronometer.start();

		mStatus = (TextView) findViewById(R.id.statusText);
		mStatus.setText(R.string.status_running);
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

	private void doStopRoutine(boolean cancel) {
		mChronometer.stop();
		mStatus.setText(R.string.status_finished);
		mRoutineWasCanceled = cancel;
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
            doStopRoutine(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(ExecutionActivity.this);
            builder.setTitle(R.string.routine_finished);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ExecutionActivity.this.finish();
                }
            });
            builder.create().show();
        }
    }
}
