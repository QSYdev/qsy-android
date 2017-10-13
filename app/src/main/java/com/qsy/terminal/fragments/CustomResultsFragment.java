package com.qsy.terminal.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qsy.terminal.R;

import java.util.List;

import libterminal.lib.results.ActionLog;
import libterminal.lib.results.CustomResults;

public class CustomResultsFragment extends Fragment {
	private CustomResults mResults;

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
		View rootView = inflater.inflate(R.layout.player_results, container, false);

		List<ActionLog> l = mResults.getExecutionLog();

		for(ActionLog log : l) {

		}

		return rootView;
	}
}
