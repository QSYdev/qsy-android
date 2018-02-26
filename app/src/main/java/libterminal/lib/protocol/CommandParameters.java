package libterminal.lib.protocol;

import libterminal.lib.routine.Color;

public final class CommandParameters {

	private final int physicalId;
	private final long delay;
	private final Color color;
	private final int numberOfStep;

	public CommandParameters(final int physicalId, final long delay, final Color color, final int numberOfStep) {
		this.physicalId = physicalId;
		this.delay = delay;
		this.color = color;
		this.numberOfStep = numberOfStep;
	}

	public int getPhysicalId() {
		return physicalId;
	}

	public long getDelay() {
		return delay;
	}

	public Color getColor() {
		return color;
	}

	public int getNumberOfStep() {
		return numberOfStep;
	}
}