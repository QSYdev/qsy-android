package com.qsy.terminal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.qsy.terminal.R;


public class ExecutionFragment extends Fragment {

	private OnFragmentInteractionListener mListener;
	private boolean routineExecuting;
	private Chronometer mChronometer;
	private TextView mStatus;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_execution, container, false);

		mChronometer = (Chronometer) rootView.findViewById(R.id.chronometer2);
		mChronometer.start();

		mStatus = (TextView) rootView.findViewById(R.id.statusText);

		Button button = (Button) rootView.findViewById(R.id.cancelButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: Poner primero un cuadro de confirmación
				mListener.routineCanceled();
			}
		});

		mStatus.setText(R.string.status_running);
		return rootView;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void routineCanceled();
	}
}
