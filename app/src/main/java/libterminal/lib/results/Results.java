package libterminal.lib.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import libterminal.lib.routine.Color;

public abstract class Results {

	public static final byte PLAYER_TYPE = 0x00;
	public static final byte CUSTOM_TYPE = 0x01;

	private final byte type;
	private final long totalTimeOut;
	private final ArrayList<ActionLog> executionLog;

	public Results(final byte type, final long totalTimeOut) {
		this.type = type;
		this.totalTimeOut = totalTimeOut;
		this.executionLog = new ArrayList<>();
	}

	public void start() {
		executionLog.add(new ActionLog.StartActionLog(new Date()));
	}

	public abstract void touche(final int logicID, final int stepId, final Color color, final long delay);

	public void stepTimeout(final int stepId) {
		executionLog.add(new ActionLog.StepTimeOutActionLog(stepId));
	}

	public void routineTimeOut(final int stepId) {
		executionLog.add(new ActionLog.RoutineTimeOutActionLog(stepId));
	}

	public void finish() {
		executionLog.add(new ActionLog.StopActionLog(new Date()));
	}

	public List<ActionLog> getExecutionLog() {
		return executionLog;
	}

	public byte getType() {
		return type;
	}

	public long getTotalTimeOut() {
		return totalTimeOut;
	}
}
