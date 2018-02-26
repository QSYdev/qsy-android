package libterminal.lib.results;

import java.util.Date;

public abstract class ActionLog {

	public static final byte START_ID = 0x00;
	public static final byte CUSTOM_TOUCHE_ID = 0x01;
	public static final byte PLAYER_TOUCHE_ID = 0x02;
	public static final byte STEP_TIME_OUT_ID = 0x03;
	public static final byte ROUTINE_TIME_OUT_ID = 0x04;
	public static final byte STOP_ID = 0x05;

	private final byte id;

	public ActionLog(final byte id) {
		this.id = id;
	}

	public byte getId() {
		return id;
	}

	public static final class StartActionLog extends ActionLog {

		private final Date start;

		public StartActionLog(final Date start) {
			super(START_ID);
			this.start = start;
		}

		public Date getStart() {
			return start;
		}
	}

	public static abstract class ToucheActionLog extends ActionLog {

		private final int logicId;
		private final long delay;
		private final int stepId;

		public ToucheActionLog(final byte id, final int logicId, final long delay, final int stepId) {
			super(id);
			this.logicId = logicId;
			this.delay = delay;
			this.stepId = stepId;
		}

		public int getLogicId() {
			return logicId;
		}

		public long getDelay() {
			return delay;
		}

		public int getStepId() {
			return stepId;
		}
	}

	public static final class CustomToucheActionLog extends ToucheActionLog {

		public CustomToucheActionLog(final int logicId, final long delay, final int stepId) {
			super(CUSTOM_TOUCHE_ID, logicId, delay, stepId);
		}
	}

	public static final class PlayerToucheActionLog extends ToucheActionLog {

		private final int playerId;

		public PlayerToucheActionLog(final int logicId, final long delay, final int stepId, final int playerId) {
			super(PLAYER_TOUCHE_ID, logicId, delay, stepId);
			this.playerId = playerId;
		}

		public int getPlayerId() {
			return playerId;
		}
	}

	public static final class StepTimeOutActionLog extends ActionLog {

		private final int stepId;

		public StepTimeOutActionLog(final int stepId) {
			super(STEP_TIME_OUT_ID);
			this.stepId = stepId;
		}

		public int getStepId() {
			return stepId;
		}
	}

	public static final class RoutineTimeOutActionLog extends ActionLog {

		private final int stepId;

		public RoutineTimeOutActionLog(final int stepId) {
			super(ROUTINE_TIME_OUT_ID);
			this.stepId = stepId;
		}

		public int getStepId() {
			return stepId;
		}
	}

	public static final class StopActionLog extends ActionLog {

		private final Date stop;

		public StopActionLog(final Date stop) {
			super(STOP_ID);
			this.stop = stop;
		}

		public Date getStop() {
			return stop;
		}
	}

}
