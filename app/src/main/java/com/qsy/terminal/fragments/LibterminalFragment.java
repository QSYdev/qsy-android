package com.qsy.terminal.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.qsy.terminal.MainActivity;
import com.qsy.terminal.R;

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
		return rootView;
	}
}
