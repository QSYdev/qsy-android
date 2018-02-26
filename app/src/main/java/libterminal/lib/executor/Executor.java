package libterminal.lib.executor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import libterminal.lib.results.Results;
import libterminal.lib.routine.Color;
import libterminal.lib.routine.NodeConfiguration;
import libterminal.lib.routine.Step;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventSource;
import libterminal.utils.BiMap;
import libterminal.utils.ExpressionTree;

public abstract class Executor extends EventSource {

	private final AtomicBoolean running;

	private final AtomicBoolean canStop;
	private final Thread preinitThread;

	private final BiMap biMap;

	private final boolean[] touchedNodes;
	private ExpressionTree expressionTree;

	private Step currentStep;
	private int numberOfStep;
	private final long totalTimeOut;

	private final Timer stepTimer;
	private StepTimeOutTimerTask stepTimerTask;

	private final Timer timer;
	private RoutineTimerTask timerTask;

	private final Results results;

	public Executor(final TreeMap<Integer, Integer> nodesIdsAssociations, final int numberOfNodes, final Results results, final long totalTimeOut) {
		this.running = new AtomicBoolean(false);

		this.canStop = new AtomicBoolean(false);
		this.preinitThread = new Thread(new PreinitTask(), "Preinit Task");

		this.biMap = new BiMap(numberOfNodes, nodesIdsAssociations);
		this.touchedNodes = new boolean[numberOfNodes + 1];
		this.expressionTree = null;

		this.currentStep = null;
		this.numberOfStep = 0;
		this.totalTimeOut = totalTimeOut;

		this.stepTimer = new Timer("Step Time Out", false);
		this.stepTimerTask = null;

		this.timer = new Timer("Routine Time Out", false);
		this.timerTask = null;

		this.results = results;
	}

	public synchronized void start() {
		preinitThread.start();
	}

	private synchronized void startExecution() {
		running.set(true);
		canStop.set(true);
		sendEvent(new Event.ExecutorRoutineStarted());
		if (totalTimeOut > 0) {
			timer.schedule(timerTask = new RoutineTimerTask(), totalTimeOut);
		}
		currentStep = getNextStep();
		final Color noColor = Color.NO_COLOR;
		for (int i = 0; i < touchedNodes.length - 1; i++) {
			sendEvent(new Event.CommandRequestEvent(biMap.getPhysicalId(i + 1), 0, noColor, 0));
		}
		prepareStep();
		results.start();
	}

	public synchronized boolean canStop() {
		return canStop.get();
	}

	public synchronized void stop() {
		if (isRunning()) {
			if (timerTask != null) {
				timerTask.cancel();
			}
			timer.cancel();
		}
		if (running.get()) {
			finalizeStep();
			stepTimer.cancel();
			running.set(false);
		}

	}

	public synchronized void touche(final int physicalIdOfNode, final int stepId, final Color toucheColor, final long toucheDelay) {
		if (running.get()) {
			final int logicalId = biMap.getLogicalId(physicalIdOfNode);
			if (stepId == numberOfStep) {
				touchedNodes[logicalId] = true;
				results.touche(logicalId, stepId, toucheColor, toucheDelay);
				if (expressionTree.evaluateExpressionTree(touchedNodes)) {
					finalizeStep();
					if (hasNextStep()) {
						currentStep = getNextStep();
						prepareStep();
					} else {
						results.finish();
						sendEvent(new Event.ExecutorDoneExecutingEvent());
					}
				}
			}
		}
	}

	protected synchronized void stepTimeout() {
		if (running.get()) {
			results.stepTimeout(numberOfStep);
			sendEvent(new Event.ExecutorStepTimeOutEvent());
			if (currentStep.getStopOnTimeout()) {
				results.finish();
				sendEvent(new Event.ExecutorDoneExecutingEvent());
			} else if (!hasNextStep()) {
				results.finish();
				sendEvent(new Event.ExecutorDoneExecutingEvent());
			} else {
				finalizeStep();
				currentStep = getNextStep();
				prepareStep();
			}
		}
	}

	public synchronized boolean isRunning() {
		return running.get();
	}

	private void prepareStep() {
		++numberOfStep;
		long maxDelay = 0;
		for (final NodeConfiguration nodeConfiguration : currentStep.getNodesConfiguration()) {
			final int physicalId = biMap.getPhysicalId(nodeConfiguration.getId());
			final long delay = nodeConfiguration.getDelay();
			if (delay > maxDelay) {
				maxDelay = delay;
			}
			final Color color = nodeConfiguration.getColor();
			sendEvent(new Event.CommandRequestEvent(physicalId, delay, color, numberOfStep));
		}
		if (currentStep.getTimeOut() > 0) {
			stepTimer.schedule(stepTimerTask = new StepTimeOutTimerTask(), currentStep.getTimeOut() + maxDelay);
		}
		expressionTree = new ExpressionTree(currentStep.getExpression());
	}

	private void finalizeStep() {
		final Color noColor = Color.NO_COLOR;
		for (final NodeConfiguration nodeConfiguration : currentStep.getNodesConfiguration()) {
			final int logicalId = nodeConfiguration.getId();
			if (!touchedNodes[logicalId]) {
				final int physicalId = biMap.getPhysicalId(nodeConfiguration.getId());
				sendEvent(new Event.CommandRequestEvent(physicalId, 0, noColor, 0));
			}
		}
		for (int i = 0; i < touchedNodes.length; i++) {
			touchedNodes[i] = false;
		}
		if (stepTimerTask != null) {
			stepTimerTask.cancel();
		}
		stepTimer.purge();
		expressionTree = null;
	}

	protected BiMap getBiMap() {
		return biMap;
	}

	protected abstract Step getNextStep();

	protected abstract boolean hasNextStep();

	public synchronized Results getResults() {
		return this.results;
	}

	private final class PreinitTask implements Runnable {

		public PreinitTask() {
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					turnAllNodes(Color.RED);
					Thread.sleep(500);
					turnAllNodes(Color.NO_COLOR);
					Thread.sleep(500);
				}
				for (int i = 0; i < 2; i++) {
					turnAllNodes(Color.GREEN);
					Thread.sleep(150);
					turnAllNodes(Color.NO_COLOR);
					Thread.sleep(150);
				}
				startExecution();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void turnAllNodes(final Color color) {
			for (int i = 1; i <= biMap.size(); i++) {
				sendEvent(new Event.CommandRequestEvent(biMap.getPhysicalId(i), 0, color, 0));
			}
		}

	}

	private final class StepTimeOutTimerTask extends TimerTask {

		public StepTimeOutTimerTask() {
		}

		@Override
		public void run() {
			try {
				stepTimeout();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

	}

	private final class RoutineTimerTask extends TimerTask {

		public RoutineTimerTask() {
		}

		@Override
		public void run() {
			if (isRunning()) {
				results.routineTimeOut(numberOfStep);
				sendEvent(new Event.ExecutorDoneExecutingEvent());
			}
		}

	}

}
