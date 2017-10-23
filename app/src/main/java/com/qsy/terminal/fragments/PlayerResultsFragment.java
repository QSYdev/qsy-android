package com.qsy.terminal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qsy.terminal.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import libterminal.lib.results.ActionLog;
import libterminal.lib.results.PlayersResults;
import libterminal.lib.routine.Color;

public class PlayerResultsFragment extends Fragment {

	private PlayersResults mResults;
	private OnFragmentInteractionListener mListener;
	private Map<String, Integer> playersCount;
	private Map<String, Long> playersTime;
	private Map<String, Integer> playersTimeouts;

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
		LinearLayout lLayout = (LinearLayout) rootView.findViewById(R.id.results_count_ll);
		LinearLayout lWinner = (LinearLayout) rootView.findViewById(R.id.winner);

		playersCount = new TreeMap<String, Integer>();
		playersTime = new TreeMap<String, Long>();
		playersTimeouts = new TreeMap<String, Integer>();

		for (Color color : mResults.getPlayersAndColors()) {
			playersCount.put(color.toString(), 0);
			playersTime.put(color.toString(), 0L);
			playersTimeouts.put(color.toString(), 0);
		}
		ArrayList<String> currentStepColors = new ArrayList<String>();

		List<ActionLog> l = mResults.getExecutionLog();
		int stepId = 0;
		int currentStepId;
		long timeSum = 0;
		int totalSteps = 0;
		long max = 0;

		for (ActionLog log : l) {
			if (log instanceof ActionLog.PlayerToucheActionLog) {
				int pid = ((ActionLog.PlayerToucheActionLog) log).getPlayerId();
				String color = mResults.getPlayersAndColors().get(pid).toString();
				incrementTime(color, ((ActionLog.PlayerToucheActionLog) log).getDelay());
				incrementCount(color);
				currentStepId = ((ActionLog.PlayerToucheActionLog) log).getStepId();
				if (currentStepId != stepId) {
					timeSum += max;
					max = 0;
					currentStepColors = new ArrayList<String>();
					totalSteps++;
					stepId = currentStepId;
					TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
					tv.setText("  Paso " + stepId + " - Gano jugador " + color);
					setTextColor(tv, color);
					linearLayout.addView(tv);
				}
				currentStepColors.add(color);
				TextView timeTv = (TextView) linearLayout.inflate(getContext(), R.layout.touche_log, null);
				if (((ActionLog.PlayerToucheActionLog) log).getDelay() > max) {
					max = ((ActionLog.PlayerToucheActionLog) log).getDelay();
				}
				double seconds = ((ActionLog.PlayerToucheActionLog) log).getDelay() / 1000.0;
				timeTv.setText("      " + color + ": " + seconds + " segundos");
				linearLayout.addView(timeTv);
			} else if (log instanceof ActionLog.StepTimeOutActionLog) {
				if (((ActionLog.StepTimeOutActionLog) log).getStepId() != stepId) {
					currentStepColors = new ArrayList<String>();
					timeSum += max;
				}
				stepId = ((ActionLog.StepTimeOutActionLog) log).getStepId();
				for (Map.Entry<String, Integer> entry : playersCount.entrySet()) {
					if (currentStepColors.indexOf(entry.getKey()) == -1) {
						incrementTimeout(entry.getKey());
						incrementTime(entry.getKey(), mResults.getStepTimeout());
					}
				}
				if (!mResults.isWaitForAllPlayers()) {
					totalSteps++;
				} else {
					if (currentStepColors.isEmpty()) {
						totalSteps++;
					}
				}
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
				tv.setText("  Paso " + stepId + " incompleto.");
				linearLayout.addView(tv);
				timeSum += mResults.getStepTimeout();
				max = 0;
				if (mResults.isStopOnTimeout()) {
					TextView ntv = (TextView) linearLayout.inflate(getContext(), R.layout.step_done, null);
					ntv.setText("  Rutina terminada por paso incompleto.");
					linearLayout.addView(ntv);
				}
			} else if (log instanceof ActionLog.RoutineTimeOutActionLog) {
				TextView tv = (TextView) linearLayout.inflate(getContext(), R.layout.routine_timeout, null);
				tv.setText("  Terminó el tiempo de la rutina.");
				linearLayout.addView(tv);
				timeSum = mResults.getTotalTimeOut() - mResults.getDelay() * totalSteps;
			} else if (log instanceof ActionLog.StopActionLog) {
				timeSum += max;
			}
		}

		TextView nodes = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
		if (totalSteps == 0) {
			nodes.setText("  No hubieron pasos.");
		} else {
			nodes.setText("  Pasos: " + totalSteps);
		}
		lLayout.addView(nodes);
		double total = (timeSum + mResults.getDelay() * totalSteps) / 1000.0;
		TextView duration = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
		duration.setText("  Tiempo total: " + total + " segundos");
		lLayout.addView(duration);
		boolean w = false;
		if (totalSteps != 0) {
			while (!playersCount.isEmpty()) {
				int maxaux = -1;
				String mstring = "";
				for (Map.Entry<String, Integer> entry : playersCount.entrySet()) {
					if (entry.getValue() > maxaux) {
						maxaux = entry.getValue();
						mstring = entry.getKey();
					}
				}
				TextView tv = (TextView) lLayout.inflate(getContext(), R.layout.step_done, null);
				if ((playersTimeouts.get(mstring) + playersCount.get(mstring)) != 0) {
					tv.setText("  " + mstring + ": " + maxaux + " / " + ((playersTime.get(mstring) / (playersTimeouts.get(mstring) + playersCount.get(mstring))) / 1000.0) + " segundos");
				} else {
					tv.setText("  " + mstring + ": " + maxaux + " / -");
				}
				lLayout.addView(tv);
				if (!w) {
					TextView winner = (TextView) lWinner.inflate(getContext(), R.layout.step_done, null);
					winner.setText(mstring);
					winner.setGravity(Gravity.CENTER);
					setTextColor(winner, mstring);
					lWinner.addView(winner);
					w = true;
				}
				playersCount.remove(mstring);
			}
		}

		Button bt = (Button) linearLayout.inflate(getContext(), R.layout.done_button, null);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.playerResultsDone();
			}
		});
		linearLayout.addView(bt);

		return rootView;
	}

	private void setTextColor(TextView textView, String color) {
		switch (color) {
			case "Azul":
				textView.setTextColor(getResources().getColor(R.color.player_blue));
				break;
			case "Rojo":
				textView.setTextColor(getResources().getColor(R.color.player_red));
				break;
			case "Cyan":
				textView.setTextColor(getResources().getColor(R.color.player_cyan));
				break;
			case "Verde":
				textView.setTextColor(getResources().getColor(R.color.player_green));
				break;
			case "Magenta":
				textView.setTextColor(getResources().getColor(R.color.player_magenta));
				break;
			default:
				break;
		}
	}

	private void incrementTime(String color, long delay) {
		Long v = playersTime.get(color);
		if (v == null) {
			playersTime.put(color, delay);
		} else {
			playersTime.remove(color);
			playersTime.put(color, v + delay);
		}
	}

	private void incrementTimeout(String color) {
		Integer count = playersTimeouts.get(color);
		playersTimeouts.remove(color);
		playersTimeouts.put(color, ++count);
	}

	private void incrementCount(String color) {
		Integer count = playersCount.get(color);
		playersCount.remove(color);
		playersCount.put(color, ++count);
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
		void playerResultsDone();
	}
}
