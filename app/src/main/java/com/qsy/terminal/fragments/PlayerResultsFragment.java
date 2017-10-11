package com.qsy.terminal.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qsy.terminal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import libterminal.lib.results.ActionLog;
import libterminal.lib.results.PlayersResults;
import libterminal.lib.routine.Color;

public class PlayerResultsFragment extends Fragment {

	private PlayersResults mResults;

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

		HashMap<Color, String> colors = new HashMap<Color, String>();
		colors.put(Color.RED, "ROJO");colors.put(Color.BLUE, "AZUL");
		colors.put(Color.YELLOW, "AMARILLO");colors.put(Color.GREEN, "VERDE");
		colors.put(Color.VIOLET, "VIOLETA");colors.put(Color.CYAN, "CYAN");
		colors.put(Color.WHITE, "BLANCO");

		ArrayList<Color> pcs = mResults.getPlayersAndColors();

		List<ActionLog> l = mResults.getExecutionLog();
		int currentStepId = 0;
		for (ActionLog log : l) {
			boolean winner = false;
			if (log instanceof ActionLog.PlayerToucheActionLog) {
				int pid = ((ActionLog.PlayerToucheActionLog) log).getPlayerId();
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
				if(((ActionLog.PlayerToucheActionLog) log).getStepId() != currentStepId) {
					currentStepId = ((ActionLog.PlayerToucheActionLog) log).getStepId();
					tv.setText("Paso "+currentStepId+" - GANO "+colors.get(pcs.get(pid)));
					linearLayout.addView(tv);
					winner = true;
				}

				double sec = ((ActionLog.PlayerToucheActionLog) log).getDelay()/1000.0;
				TextView ttv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
				tv.setText("     Touche "+colors.get(pcs.get(pid))+": "+sec+" segundos");
				linearLayout.addView(ttv);
			} else if (log instanceof ActionLog.StepTimeOutActionLog) {
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
				if(!winner) {
					tv.setText("Paso "+((ActionLog.StepTimeOutActionLog) log).getStepId()+" - TIME OUT");
				} else {
					tv.setText("    TIME OUT EN EL PASO "+((ActionLog.StepTimeOutActionLog) log).getStepId());
				}
				linearLayout.addView(tv);
			} else if (log instanceof ActionLog.StartActionLog) {
				// TODO
			} else if (log instanceof ActionLog.RoutineTimeOutActionLog) {
				// TODO
			} else if (log instanceof ActionLog.StopActionLog) {
				// TODO
			}
		}
		return rootView;
	}
}
