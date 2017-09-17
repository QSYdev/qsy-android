package com.qsy.terminal.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;

import libterminal.api.TerminalAPI;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;


public class LibterminalService extends Service {
	private TerminalAPI libterminal;
	private WakelockManager wakelockManager;

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
		libterminal = new TerminalAPI(address);
		wakelockManager = new WakelockManager();
		// TODO: descomentar una vez que TerminalAPI no pierda sus listeners
		// libterminal.addListener(wakelockManager);
		Log.d("LibterminalService", "inside onCreate");
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
		Log.d("LibterminalService", "getTerminal");
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

	final private class WakelockManager implements EventListener {

		private PowerManager.WakeLock cpuWakeLock;
		private WifiManager.WifiLock wifiLock;

		public WakelockManager() {
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

			cpuWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RoutineWakeLock");
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RoutineWifiLock");

			/*
			 * Esto hace que las las llamadas a acquire() y release() sean idempotentes. Considerar
			 * sacar una vez que esté el evento de comienzo de rutina. Esto haría que se lance
			 * una excepción si se adquiere o libera un lock más de una vez.
			 */
			wifiLock.setReferenceCounted(false);
			cpuWakeLock.setReferenceCounted(false);
		}

		@Override
		public void receiveEvent(Event event) {
			switch(event.getEventType()) {
			    // TODO
				/*case routineStarted:
					cpuWakeLock.acquire();
					wifiLock.release();
					break;*/
				case routineFinished:
					cpuWakeLock.release();
					wifiLock.release();
					break;
			}
		}
	}

}
