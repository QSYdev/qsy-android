package com.qsy.terminal.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.qsy.terminal.R;

import static android.content.Context.WIFI_SERVICE;

public class QSYUtils {
	private static String ENABLE_WIFI_MSG =
		"No se encontro una red QSY habilitada. Por favor habilita wifi y conectate a la red de las tortugas.";

	public static void checkWifiEnabled(final Context context) {
		WifiManager mgr = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
		if (!mgr.isWifiEnabled() || mgr.getConnectionInfo() == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(QSYUtils.ENABLE_WIFI_MSG);
			builder.setCancelable(false);
			builder.setNeutralButton("ACEPTAR", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					context.getApplicationContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				}
			});
			builder.create().show();
		}
	}

}
