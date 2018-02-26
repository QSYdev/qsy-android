package libterminal.lib.routine;

import java.util.ArrayList;
import java.util.Iterator;

public final class Routine implements Iterable<Step> {

	private final byte playersCount;
	private final byte numberOfNodes;
	private final long totalTimeOut;
	private String name;
	private String description;
	private final ArrayList<Step> steps;

	public Routine(final byte playersCount, final byte numberOfNodes, final long totalTimeOut, final ArrayList<Step> steps, String name, String description) {
		this.playersCount = playersCount;
		this.numberOfNodes = numberOfNodes;
		this.totalTimeOut = totalTimeOut;
		this.steps = steps;
		this.name = name;
		this.description = description;
	}

	public byte getPlayersCount() {
		return playersCount;
	}

	public byte getNumberOfNodes() {
		return numberOfNodes;
	}

	public long getTotalTimeOut() {
		return totalTimeOut;
	}

	public ArrayList<Step> getSteps() {
		return steps;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public Iterator<Step> iterator() {
		return new RoutineIterator();
	}

	private final class RoutineIterator implements Iterator<Step> {

		private int index;

		public RoutineIterator() {
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < steps.size();
		}

		@Override
		public Step next() {
			return getSteps().get(index++);
		}

		@Override
		public void remove() {
		}

	}
}
