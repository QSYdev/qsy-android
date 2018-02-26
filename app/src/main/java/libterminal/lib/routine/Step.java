package libterminal.lib.routine;

import java.util.LinkedList;

public final class Step {

	private final String expression;
	private final long timeOut;
	private final boolean stopOnTimeout;
	private final LinkedList<NodeConfiguration> nodesConfiguration;

	public Step(final LinkedList<NodeConfiguration> nodesConfiguration, final long timeOut, final String expression, final boolean stopOnTimeout) {
		this.expression = expression;
		this.timeOut = timeOut;
		this.nodesConfiguration = nodesConfiguration;
		this.stopOnTimeout = stopOnTimeout;
	}

	public String getExpression() {
		return expression;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public boolean getStopOnTimeout() {
		return stopOnTimeout;
	}

	public LinkedList<NodeConfiguration> getNodesConfiguration() {
		return nodesConfiguration;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("EXPRESSION = " + expression + " || TIMEOUT = " + timeOut + "\n");
		for (final NodeConfiguration nodeConfig : nodesConfiguration) {
			sb.append(nodeConfig + "\n");
		}
		return sb.toString();
	}

}
