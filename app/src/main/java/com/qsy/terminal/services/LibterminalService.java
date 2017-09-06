package com.qsy.terminal.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import android.support.annotation.Nullable;
import android.text.format.Formatter;

import libterminal.api.TerminalAPI;


public class LibterminalService extends Service {
	private TerminalAPI libterminal;

	private final IBinder binder = new LocalBinder();

	@Override
	public void onCreate() {
		WifiManager mgr = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
		String ipAddress = Formatter.formatIpAddress(mgr.getConnectionInfo().getIpAddress());
		Inet4Address address = null;
		try {
			address = (Inet4Address) Inet4Address.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			libterminal = new TerminalAPI(address);
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

	@Override
	public void onDestroy() {
		super.onDestroy();
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
