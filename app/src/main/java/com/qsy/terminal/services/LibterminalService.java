package com.qsy.terminal.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

import android.support.annotation.Nullable;

import libterminal.api.TerminalAPI;


public class LibterminalService extends Service {
	private TerminalAPI libterminal;

	private final IBinder binder = new LocalBinder();

	@Override
	public void onCreate() {
		try {
			libterminal = new TerminalAPI();
			libterminal.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals("on")) {
			Notification notif = new Notification();
			startForeground(1, notif);
		} else if (intent.getAction().equals("off")) {
			stopForeground(true);
			stopSelf();
		}

		return START_NOT_STICKY;
	}

	public TerminalAPI getTerminal() {
		return libterminal;
	}

	public final class LocalBinder extends Binder {

		public Service getService() {
			return LibterminalService.this;
		}

	}

	@Nullable
	@Override
	public IBinder onBind(final Intent intent) {
		return binder;
	}

}
