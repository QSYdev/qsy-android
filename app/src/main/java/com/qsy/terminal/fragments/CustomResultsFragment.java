package com.qsy.terminal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qsy.terminal.R;

import java.util.List;

import libterminal.lib.results.ActionLog;
import libterminal.lib.results.CustomResults;

public class CustomResultsFragment extends Fragment {
	private CustomResults mResults;

	private OnFragmentInteractionListener mListener;

	public static CustomResultsFragment newInstance(CustomResults results) {
		CustomResultsFragment crf = new CustomResultsFragment();
		crf.mResults = results;
		return crf;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.custom_results, container, false);
		LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.custom_results_ll);
		LinearLayout lLayout = (LinearLayout) rootView.findViewById(R.id.custom_results_count_ll);

		int stepId = 1;
		int currentStepId = 0;
		long stepCount = 0;
		double max = -1;
		double min = Double.MAX_VALUE;
		double totalTime = 0;
		// TODO: print the starting time
		List<ActionLog> l = mResults.getExecutionLog();
		for (ActionLog log : l) {
			if (log instanceof ActionLog.CustomToucheActionLog) {
				currentStepId = ((ActionLog.CustomToucheActionLog) log).getStepId();
				if (currentStepId == stepId) {
					Log.d("RESULTS", " current = " + currentStepId + " - step: " + stepId);
					stepCount += ((ActionLog.CustomToucheActionLog) log).getDelay();
				} else {
					TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
					double finalCount = Math.floor((stepCount / 1000.0) * 100) / 100;
					totalTime += finalCount;
					tv.setText("  Paso " + stepId + " - Completado en " + finalCount + " segundos.");
					linearLayout.addView(tv);
					if (finalCount > max) {
						max = finalCount;
					}
					if (finalCount < min) {
						min = finalCount;
					}
					stepId = currentStepId;
					stepCount = ((ActionLog.CustomToucheActionLog) log).getDelay();
				}
			} else if (log instanceof ActionLog.StepTimeOutActionLog) {
				stepId = ((ActionLog.StepTimeOutActionLog) log).getStepId();
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
				tv.setText("  Paso " + stepId + " incompleto.");
				linearLayout.addView(tv);
				stepCount = 0;
			} else if (log instanceof ActionLog.RoutineTimeOutActionLog) {
			} else if (log instanceof ActionLog.StopActionLog) {
				if(l.get(l.size()-2) instanceof ActionLog.CustomToucheActionLog) {
					TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
					double finalCount = Math.floor((stepCount / 1000.0) * 100) / 100;
					totalTime += finalCount;
					tv.setText("  Paso " + currentStepId + " - Completado en " + finalCount + " segundos.");
					linearLayout.addView(tv);
					if (finalCount > max) {
						max = finalCount;
					}
					if (finalCount < min) {
						min = finalCount;
					}
				}
			}
		}

		TextView ttime = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
		ttime.setText("  Tiempo total: " + (Math.floor(totalTime * 100) / 100) + " segundos");
		lLayout.addView(ttime);

		double avr = totalTime / mResults.getRoutine().getSteps().size();
		TextView tarv = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
		tarv.setText("  Tiempo promedio: " + (Math.floor(avr * 100) / 100) + " segundos");
		lLayout.addView(tarv);

		TextView tmax = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
		tmax.setText("  Paso más rápido: " + (Math.floor(min * 100) / 100) + " segundos");
		lLayout.addView(tmax);

		TextView tmin = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
		tmin.setText("  Paso más lento: " + (Math.floor(max * 100) / 100) + " segundos");
		lLayout.addView(tmin);

		Button bt = (Button) linearLayout.inflate(getContext(), R.layout.done_button, null);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.customResultsDone();
			}
		});
		linearLayout.addView(bt);

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
		void customResultsDone();
	}
}
