package libterminal.lib.executor;

import java.util.Iterator;
import java.util.TreeMap;

import libterminal.lib.results.CustomResults;
import libterminal.lib.routine.Routine;
import libterminal.lib.routine.Step;

public class CustomExecutor extends Executor {

	private final Iterator<Step> routineIterator;

	public CustomExecutor(final Routine routine, final TreeMap<Integer, Integer> nodesIdsAssociations) {
		super(nodesIdsAssociations, routine.getNumberOfNodes(), new CustomResults(routine), routine.getTotalTimeOut());
		this.routineIterator = routine.iterator();
	}

	@Override
	protected Step getNextStep() {
		return routineIterator.next();
	}

	@Override
	protected boolean hasNextStep() {
		return routineIterator.hasNext();
	}

}
