package com.qsy.terminal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qsy.terminal.R;

import java.util.List;

import libterminal.lib.results.ActionLog;
import libterminal.lib.results.PlayersResults;

public class PlayerResultsFragment extends Fragment {

	private PlayersResults mResults;
	private OnFragmentInteractionListener mListener;

	public static PlayerResultsFragment newInstance(PlayersResults results) {
		PlayerResultsFragment prs = new PlayerResultsFragment();
		prs.mResults = results;
		return prs;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.player_results, container, false);
		LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.results_ll);

		List<ActionLog> l = mResults.getExecutionLog();
		int stepId = 0;
		int currentStepId;
		// TODO: print the starting time
		for (ActionLog log : l) {
			if (log instanceof ActionLog.PlayerToucheActionLog) {
				int pid = ((ActionLog.PlayerToucheActionLog) log).getPlayerId();
				String color = mResults.getPlayersAndColors().get(pid).toString();
				currentStepId = ((ActionLog.PlayerToucheActionLog) log).getStepId();
				if (currentStepId != stepId) {
					stepId = currentStepId;
					TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
					tv.setText("Paso " + stepId + " - Gano jugador " + color);
					linearLayout.addView(tv);
				}
				TextView timeTv = (TextView) linearLayout.inflate(getContext(), R.layout.touche_log, null);
				double seconds = ((ActionLog.PlayerToucheActionLog) log).getDelay()/1000.0;
				timeTv.setText(color+": "+seconds);
				linearLayout.addView(timeTv);
			}
			if (log instanceof ActionLog.StepTimeOutActionLog) {
				stepId = ((ActionLog.StepTimeOutActionLog) log).getStepId();
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
				tv.setText("Paso "+stepId+" incompleto.");
				linearLayout.addView(tv);
				if(mResults.isStopOnTimeout()) {
					TextView ntv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
					ntv.setText("Rutina terminada por paso incompleto.");
					linearLayout.addView(ntv);
				}
			}
			if (log instanceof ActionLog.RoutineTimeOutActionLog) {
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.routine_timeout, null);
				tv.setText("Terminó el tiempo de la rutina.");
				linearLayout.addView(tv);
			}
			if (log instanceof ActionLog.StopActionLog) {
				// TODO: print time stuff
			}
		}

		Button bt = (Button) linearLayout.inflate(getContext(), R.layout.done_button, null);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.resultsDone();
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
		void resultsDone();
	}
}
