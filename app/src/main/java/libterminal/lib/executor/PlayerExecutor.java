package libterminal.lib.executor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import libterminal.lib.results.PlayersResults;
import libterminal.lib.routine.Color;
import libterminal.lib.routine.NodeConfiguration;
import libterminal.lib.routine.Step;

public class PlayerExecutor extends Executor {

	private final ArrayList<Color> playersAndColors;
	private final ArrayList<Color> stepsWinners;
	private final TreeMap<Integer, Color> logicalIdsAndColors;

	private final boolean waitForAllPlayers;
	private final long timeOut;
	private final long delay;
	private final int totalStep;
	private final boolean stopOnTimeout;
	private final int numberOfNodes;

	private int stepIndex;

	public PlayerExecutor(final TreeMap<Integer, Integer> nodesIdsAssociations, final int numberOfNodes, final ArrayList<Color> playersAndColors, final boolean waitForAllPlayers, final long timeOut,
			final long delay, final long totalTimeOut, final int totalStep, final boolean stopOnTimeout) {

		super(nodesIdsAssociations, numberOfNodes, new PlayersResults(numberOfNodes, playersAndColors,
				waitForAllPlayers, timeOut, delay, totalTimeOut, totalStep, stopOnTimeout), totalTimeOut);

		this.playersAndColors = playersAndColors;
		this.stepsWinners = new ArrayList<>();
		this.logicalIdsAndColors = new TreeMap<>();

		this.waitForAllPlayers = waitForAllPlayers;
		this.timeOut = timeOut;
		this.delay = delay;
		this.totalStep = totalStep;
		this.stopOnTimeout = stopOnTimeout;
		this.numberOfNodes = numberOfNodes;

		this.stepIndex = 0;
	}

	@Override
	public synchronized void touche(int physicalIdOfNode, final int stepId, final Color toucheColor, final long toucheDelay) {
		if (stepsWinners.size() < stepIndex) {
			final int logicalId = getBiMap().getLogicalId(physicalIdOfNode);
			final Color colorWinner = logicalIdsAndColors.get(logicalId);
			stepsWinners.add(colorWinner);
			//TODO: en este caso solo guarda el ganador del paso
		}
		super.touche(physicalIdOfNode, stepId, toucheColor, toucheDelay);
	}

	@Override
	protected Step getNextStep() {
		logicalIdsAndColors.clear();

		final char booleanOperator = (waitForAllPlayers) ? '&' : '|';
		final LinkedList<Integer> usedIds = new LinkedList<>();
		for (int i = 1; i <= numberOfNodes; i++) {
			usedIds.add(i);
		}
		final LinkedList<NodeConfiguration> currentNodesConfiguration = new LinkedList<>();
		final StringBuilder sb = new StringBuilder();

		for (final Color color : playersAndColors) {
			final int id = usedIds.remove(ThreadLocalRandom.current().nextInt(0, usedIds.size()));
			currentNodesConfiguration.add(new NodeConfiguration(id, delay, color));
			logicalIdsAndColors.put(id, color);
			sb.append(id);
			sb.append(booleanOperator);
		}
		sb.deleteCharAt(sb.length() - 1);

		final String expression = sb.toString();

		++stepIndex;
		return new Step(currentNodesConfiguration, timeOut, expression, stopOnTimeout);
	}

	@Override
	protected boolean hasNextStep() {
		return totalStep == 0 || stepIndex < totalStep;
	}

	@Override
	protected synchronized void stepTimeout() {
		stepsWinners.add(null);
		super.stepTimeout();
	}

}
