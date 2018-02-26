package libterminal.lib.results;

import java.util.ArrayList;

import libterminal.lib.routine.Color;

public class PlayersResults extends Results {

	private final int numberOfNodes, totalSteps;
	private final long stepTimeout, delay;
	private final boolean waitForAllPlayers, stopOnTimeout;
	private final ArrayList<Color> playersAndColors;

	public PlayersResults(final int numberOfNodes, final ArrayList<Color> playersAndColors, final boolean waitForAllPlayers, final long stepTimeout,
			final long delay, final long totalTimeOut, final int totalSteps, final boolean stopOnTimeout) {

		super(PLAYER_TYPE, totalTimeOut);

		this.numberOfNodes = numberOfNodes;
		this.playersAndColors = playersAndColors;
		this.waitForAllPlayers = waitForAllPlayers;
		this.stepTimeout = stepTimeout;
		this.delay = delay;
		this.totalSteps = totalSteps;
		this.stopOnTimeout = stopOnTimeout;
	}

	@Override
	public void touche(final int logicId, final int stepId, final Color color, final long delay) {
		getExecutionLog().add(new ActionLog.PlayerToucheActionLog(logicId, delay, stepId, playersAndColors.indexOf(color)));
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public int getTotalSteps() {
		return totalSteps;
	}

	public long getStepTimeout() {
		return stepTimeout;
	}

	public long getDelay() {
		return delay;
	}

	public boolean isWaitForAllPlayers() {
		return waitForAllPlayers;
	}

	public boolean isStopOnTimeout() {
		return stopOnTimeout;
	}

	public ArrayList<Color> getPlayersAndColors() {
		return playersAndColors;
	}
}
