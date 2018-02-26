package libterminal.lib.results;

import libterminal.lib.routine.Color;
import libterminal.lib.routine.Routine;

public class CustomResults extends Results {

	private final Routine routine;

	public CustomResults(final Routine routine) {
		super(CUSTOM_TYPE, routine.getTotalTimeOut());
		this.routine = routine;
	}

	@Override
	public void touche(final int logicId, final int stepId, final Color color, final long delay) {
		getExecutionLog().add(new ActionLog.CustomToucheActionLog(logicId, delay, stepId));
	}

	public Routine getRoutine() {
		return routine;
	}
}
