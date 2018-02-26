package libterminal.lib.routine;

public final class NodeConfiguration {

	private final int id;
	private final Color color;
	private final long delay;

	public NodeConfiguration(final int id, final long delay, final Color color) {
		this.id = id;
		this.color = color;
		this.delay = delay;
	}

	public int getId() {
		return id;
	}

	public Color getColor() {
		return color;
	}

	public long getDelay() {
		return delay;
	}

	@Override
	public String toString() {
		return "ID = " + id + " || COLOR = " + color + " || DELAY = " + delay;
	}

}
