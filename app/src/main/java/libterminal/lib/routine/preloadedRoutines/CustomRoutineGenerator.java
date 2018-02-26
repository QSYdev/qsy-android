package libterminal.lib.routine.preloadedRoutines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import libterminal.lib.routine.Color;
import libterminal.lib.routine.NodeConfiguration;
import libterminal.lib.routine.Routine;
import libterminal.lib.routine.Step;
import libterminal.utils.RoutineManager;

public class CustomRoutineGenerator {

	private static final String PATH = "src/main/java/libterminal/resources/";

	public static void main(String[] args) throws IOException {

		final ArrayList<Step> steps = new ArrayList<>();
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(1, 3000L, Color.RED));
			steps.add(new Step(nodesConfig, 0L, "1", false));
		}
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(3, 0L, Color.GREEN));
			steps.add(new Step(nodesConfig, 0L, "3", false));
		}
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(2, 0L, Color.BLUE));
			steps.add(new Step(nodesConfig, 0L, "2", false));
		}
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(5, 0L, Color.CYAN));
			steps.add(new Step(nodesConfig, 0L, "5", false));
		}
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(3, 5000L, Color.MAGENTA));
			steps.add(new Step(nodesConfig, 0L, "3", false));
		}
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(2, 0L, Color.MAGENTA));
			steps.add(new Step(nodesConfig, 0L, "2", false));
		}
		{
			final LinkedList<NodeConfiguration> nodesConfig = new LinkedList<>();
			nodesConfig.add(new NodeConfiguration(1, 0L, Color.MAGENTA));
			steps.add(new Step(nodesConfig, 0L, "1", false));
		}

		String description = "Rutina para evaluar la velocidad de arranque de los jugadores, tanto frontal como lateral.";

		final Routine routine = new Routine((byte) 1, (byte) 5, 0L, steps, "Reacción corta", description);
		RoutineManager.storeRoutine(PATH + "rcorta.json", routine);
	}

}
