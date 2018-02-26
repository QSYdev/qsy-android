package libterminal.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import libterminal.lib.results.ActionLog;
import libterminal.lib.results.ActionLog.CustomToucheActionLog;
import libterminal.lib.results.ActionLog.PlayerToucheActionLog;
import libterminal.lib.results.ActionLog.RoutineTimeOutActionLog;
import libterminal.lib.results.ActionLog.StartActionLog;
import libterminal.lib.results.ActionLog.StepTimeOutActionLog;
import libterminal.lib.results.ActionLog.StopActionLog;
import libterminal.lib.results.CustomResults;
import libterminal.lib.results.PlayersResults;
import libterminal.lib.results.Results;
import libterminal.lib.routine.Color;
import libterminal.lib.routine.NodeConfiguration;
import libterminal.lib.routine.Routine;
import libterminal.lib.routine.Step;
import libterminal.utils.RoutineManager.ColorSerializer;
import libterminal.utils.RoutineManager.NodeConfigurationSerializer;
import libterminal.utils.RoutineManager.StepSerializer;

public class ResultsManager {

	static {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Results.class, new ResultsManager.ResultsSerializer());
		gsonBuilder.registerTypeAdapter(ActionLog.class, new ResultsManager.ActionLogsSerializer());
		gsonBuilder.registerTypeAdapter(Routine.class, new RoutineManager.RoutineSerializer());
		gsonBuilder.registerTypeAdapter(Step.class, new StepSerializer());
		gsonBuilder.registerTypeAdapter(NodeConfiguration.class, new NodeConfigurationSerializer());
		gsonBuilder.registerTypeAdapter(Color.class, new ColorSerializer());
		gsonBuilder.setPrettyPrinting();
		gson = gsonBuilder.create();
	}

	private static final Gson gson;

	public static Results loadResults(final String path) throws UnsupportedEncodingException, IOException {
		Reader reader = null;
		Results results = null;
		try {
			reader = new FileReader(path);
			results = gson.fromJson(reader, Results.class);
		} finally {
			if (reader != null)
				reader.close();
		}

		return results;
	}

	public static void storeResults(final String path, final Results results) throws IOException {
		Writer writer = null;
		try {
			writer = new FileWriter(path);
			gson.toJson(results, writer);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public static String storeResults(final Results results) {
		return gson.toJson(results);
	}

	static final class ResultsSerializer implements JsonDeserializer<Results>, JsonSerializer<Results> {

		private static final String TYPE_ATT = "type";
		private static final String TOTAL_TIME_OUT_ATT = "totalTimeOut";
		private static final String ACTION_LOGS_ATT = "executionLog";

		private static final String DELAY_ATT = "delay";
		private static final String NUMBER_OF_NODES_ATT = "numberOfNodes";
		private static final String STEP_TIME_OUT_ATT = "stepTimeout";
		private static final String TOTAL_STEPS_ATT = "totalSteps";
		private static final String STOP_ON_TIME_OUT_ATT = "stopOnTimeout";
		private static final String WAIT_FOR_ALL_PLAYERS_ATT = "waitForAllPlayers";
		private static final String COLORS_ATT = "playersAndColors";

		private static final String ROUTINE_ATT = "routine";

		public ResultsSerializer() {
		}

		@Override
		public Results deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final JsonObject jsonObject = json.getAsJsonObject();
			final byte type = jsonObject.get(TYPE_ATT).getAsByte();
			final long totalTimeOut = jsonObject.get(TOTAL_TIME_OUT_ATT).getAsLong();

			Results results = null;

			switch (type) {
			case Results.PLAYER_TYPE:
				final Color[] colors = context.deserialize(jsonObject.get(COLORS_ATT), Color[].class);
				final ArrayList<Color> playersAndColors = new ArrayList<>(Arrays.asList(colors));

				final long delay = jsonObject.get(DELAY_ATT).getAsLong();
				final int numberOfNodes = jsonObject.get(NUMBER_OF_NODES_ATT).getAsInt();
				final long stepTimeout = jsonObject.get(STEP_TIME_OUT_ATT).getAsLong();
				final int totalSteps = jsonObject.get(TOTAL_STEPS_ATT).getAsInt();
				final boolean stopOnTimeout = jsonObject.get(STOP_ON_TIME_OUT_ATT).getAsBoolean();
				final boolean waitForAllPlayers = jsonObject.get(WAIT_FOR_ALL_PLAYERS_ATT).getAsBoolean();

				results = new PlayersResults(numberOfNodes, playersAndColors, waitForAllPlayers, stepTimeout, delay, totalTimeOut, totalSteps, stopOnTimeout);
				break;
			case Results.CUSTOM_TYPE:
				final Routine routine = context.deserialize(jsonObject.get(ROUTINE_ATT), Routine.class);

				results = new CustomResults(routine);
				break;
			}

			final ActionLog[] actionLogs = context.deserialize(jsonObject.get(ACTION_LOGS_ATT), ActionLog[].class);
			final ArrayList<ActionLog> executionLog = new ArrayList<>(Arrays.asList(actionLogs));

			final Field executionLogField;
			try {
				executionLogField = results.getClass().getSuperclass().getDeclaredField("executionLog");
				executionLogField.setAccessible(true);
				executionLogField.set(results, executionLog);
			} catch (final NoSuchFieldException e) {
				e.printStackTrace();
			} catch (final SecurityException e) {
				e.printStackTrace();
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}

			return results;
		}

		@Override
		public JsonElement serialize(final Results results, final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty(TYPE_ATT, results.getType());
			jsonObject.addProperty(TOTAL_TIME_OUT_ATT, results.getTotalTimeOut());

			switch (results.getType()) {
			case Results.PLAYER_TYPE:
				serialize((PlayersResults) results, jsonObject, context);
				break;
			case Results.CUSTOM_TYPE:
				serialize((CustomResults) results, jsonObject, context);
				break;
			}

			jsonObject.add(ACTION_LOGS_ATT, context.serialize(results.getExecutionLog()));

			return jsonObject;
		}

		private void serialize(final PlayersResults results, final JsonObject jsonObject, final JsonSerializationContext context) {
			jsonObject.add(COLORS_ATT, context.serialize(results.getPlayersAndColors()));

			jsonObject.addProperty(DELAY_ATT, results.getDelay());
			jsonObject.addProperty(NUMBER_OF_NODES_ATT, results.getNumberOfNodes());
			jsonObject.addProperty(STEP_TIME_OUT_ATT, results.getStepTimeout());
			jsonObject.addProperty(TOTAL_STEPS_ATT, results.getTotalSteps());
			jsonObject.addProperty(STOP_ON_TIME_OUT_ATT, results.isStopOnTimeout());
			jsonObject.addProperty(WAIT_FOR_ALL_PLAYERS_ATT, results.isWaitForAllPlayers());
		}

		private void serialize(final CustomResults results, final JsonObject jsonObject, final JsonSerializationContext context) {
			jsonObject.add(ROUTINE_ATT, context.serialize(results.getRoutine()));
		}
	}

	static final class ActionLogsSerializer implements JsonDeserializer<ActionLog>, JsonSerializer<ActionLog> {

		private static final String ID_ATT = "id";
		private static final String START_ATT = "start";
		private static final String STOP_ATT = "stop";
		private static final String LOGIC_ID_ATT = "logicId";
		private static final String DELAY_ATT = "delay";
		private static final String STEP_ID_ATT = "stepId";
		private static final String PLAYER_ID_ATT = "playerId";

		public ActionLogsSerializer() {
		}

		@Override
		public ActionLog deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final JsonObject jsonObject = json.getAsJsonObject();

			final byte id = jsonObject.get(ID_ATT).getAsByte();

			switch (id) {
			case ActionLog.START_ID:
				final Date start = new Date(jsonObject.get(START_ATT).getAsLong());
				return new ActionLog.StartActionLog(start);
			case ActionLog.CUSTOM_TOUCHE_ID: {
				final int logicId = jsonObject.get(LOGIC_ID_ATT).getAsInt();
				final long delay = jsonObject.get(DELAY_ATT).getAsLong();
				final int stepId = jsonObject.get(STEP_ID_ATT).getAsInt();
				return new ActionLog.CustomToucheActionLog(logicId, delay, stepId);
			}
			case ActionLog.PLAYER_TOUCHE_ID: {
				final int logicId = jsonObject.get(LOGIC_ID_ATT).getAsInt();
				final long delay = jsonObject.get(DELAY_ATT).getAsLong();
				final int stepId = jsonObject.get(STEP_ID_ATT).getAsInt();
				final int playerId = jsonObject.get(PLAYER_ID_ATT).getAsInt();
				return new ActionLog.PlayerToucheActionLog(logicId, delay, stepId, playerId);
			}
			case ActionLog.STEP_TIME_OUT_ID:
				return new ActionLog.StepTimeOutActionLog(jsonObject.get(STEP_ID_ATT).getAsInt());
			case ActionLog.ROUTINE_TIME_OUT_ID:
				return new ActionLog.RoutineTimeOutActionLog(jsonObject.get(STEP_ID_ATT).getAsInt());
			case ActionLog.STOP_ID:
				final Date stop = new Date(jsonObject.get(STOP_ATT).getAsLong());
				return new ActionLog.StopActionLog(stop);
			default:
				return null;
			}
		}

		@Override
		public JsonElement serialize(final ActionLog actionLog, final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty(ID_ATT, actionLog.getId());

			switch (actionLog.getId()) {
			case ActionLog.START_ID:
				serialize((StartActionLog) actionLog, jsonObject);
				break;
			case ActionLog.CUSTOM_TOUCHE_ID:
				serialize((CustomToucheActionLog) actionLog, jsonObject);
				break;
			case ActionLog.PLAYER_TOUCHE_ID:
				serialize((PlayerToucheActionLog) actionLog, jsonObject);
				break;
			case ActionLog.STEP_TIME_OUT_ID:
				serialize((StepTimeOutActionLog) actionLog, jsonObject);
				break;
			case ActionLog.ROUTINE_TIME_OUT_ID:
				serialize((RoutineTimeOutActionLog) actionLog, jsonObject);
				break;
			case ActionLog.STOP_ID:
				serialize((StopActionLog) actionLog, jsonObject);
				break;
			}

			return jsonObject;
		}

		private void serialize(final StopActionLog actionLog, final JsonObject jsonObject) {
			jsonObject.addProperty(STOP_ATT, actionLog.getStop().getTime());
		}

		private void serialize(final RoutineTimeOutActionLog actionLog, final JsonObject jsonObject) {
			jsonObject.addProperty(STEP_ID_ATT, actionLog.getStepId());
		}

		private void serialize(final StepTimeOutActionLog actionLog, final JsonObject jsonObject) {
			jsonObject.addProperty(STEP_ID_ATT, actionLog.getStepId());
		}

		private void serialize(final PlayerToucheActionLog actionLog, final JsonObject jsonObject) {
			jsonObject.addProperty(LOGIC_ID_ATT, actionLog.getLogicId());
			jsonObject.addProperty(DELAY_ATT, actionLog.getDelay());
			jsonObject.addProperty(STEP_ID_ATT, actionLog.getStepId());
			jsonObject.addProperty(PLAYER_ID_ATT, actionLog.getPlayerId());
		}

		private void serialize(final CustomToucheActionLog actionLog, final JsonObject jsonObject) {
			jsonObject.addProperty(LOGIC_ID_ATT, actionLog.getLogicId());
			jsonObject.addProperty(DELAY_ATT, actionLog.getDelay());
			jsonObject.addProperty(STEP_ID_ATT, actionLog.getStepId());
		}

		private void serialize(final StartActionLog actionLog, final JsonObject jsonObject) {
			jsonObject.addProperty(START_ATT, actionLog.getStart().getTime());
		}
	}
}
