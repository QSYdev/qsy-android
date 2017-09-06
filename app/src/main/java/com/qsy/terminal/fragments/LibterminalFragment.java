package com.qsy.terminal.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.qsy.terminal.MainActivity;
import com.qsy.terminal.R;

import static android.content.Context.WIFI_SERVICE;

public class LibterminalFragment extends Fragment {
	private Button mStartServiceButton;
	private Button mStopServiceButton;
	private Button mSearchNodesButton;

	private MainActivity mActivity;

	public LibterminalFragment() {
		super();
	}

	@SuppressLint("ValidFragment")
	public LibterminalFragment(MainActivity activity) {
		super();
		mActivity = activity;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.libterminal_connection, container, false);

		mStartServiceButton = rootView.findViewById(R.id.startForegroundService);
		mStartServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mActivity.startServiceListener(view);
			}
		});

		mStopServiceButton = rootView.findViewById(R.id.stopForegroundService);
		mStopServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mActivity.stopServiceListener(view);
			}
		});

		mSearchNodesButton = rootView.findViewById(R.id.searchNodes);
		mSearchNodesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mActivity.searchNodesListener(view);
			}
		});

		// TODO get me the hell outta here
		WifiManager mgr = (WifiManager) this.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
		if (!mgr.isWifiEnabled() || mgr.getConnectionInfo() == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			// TODO string!
			builder.setMessage("No hay red WiFi presente. La aplicación va a cerrar.");
			builder.setCancelable(false);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				}
			});
			builder.create().show();
		}
		return rootView;
	}
}
