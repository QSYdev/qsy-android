package libterminal.patterns.visitor;

import libterminal.patterns.observer.Event.CommandIssuedEvent;
import libterminal.patterns.observer.Event.CommandPacketSentEvent;
import libterminal.patterns.observer.Event.CommandRequestEvent;
import libterminal.patterns.observer.Event.DisconnectedNodeEvent;
import libterminal.patterns.observer.Event.ExecutorDoneExecutingEvent;
import libterminal.patterns.observer.Event.ExecutorRoutineStarted;
import libterminal.patterns.observer.Event.ExecutorStepTimeOutEvent;
import libterminal.patterns.observer.Event.IncomingPacketEvent;
import libterminal.patterns.observer.Event.KeepAliveErrorEvent;
import libterminal.patterns.observer.Event.NewNodeEvent;
import libterminal.patterns.observer.Event.RoutineFinishedEvent;
import libterminal.patterns.observer.Event.RoutineStartedEvent;
import libterminal.patterns.observer.Event.ToucheReceivedEvent;

public abstract class EventHandler {

	public void handle(final IncomingPacketEvent incomingPacketEvent) {
	}

	public void handle(final NewNodeEvent newNodeEvent) {
	}

	public void handle(final CommandPacketSentEvent commandPacketSentEvent) {
	}

	public void handle(final KeepAliveErrorEvent keepAliveErrorEvent) {
	}

	public void handle(final DisconnectedNodeEvent disconnectedNodeEvent) {
	}

	public void handle(final RoutineFinishedEvent routineFinishedEvent) {
	}

	public void handle(final ExecutorDoneExecutingEvent executorDoneExecutingEvent) {
	}

	public void handle(final ExecutorStepTimeOutEvent executorStepTimeOutEvent) {
	}

	public void handle(final CommandRequestEvent commandRequestEvent) {
	}

	public void handle(final RoutineStartedEvent routineStartedEvent) {
	}

	public void handle(final CommandIssuedEvent commandIssuedEvent) {
	}

	public void handle(final ToucheReceivedEvent toucheReceivedEvent) {
	}

	public void handle(final ExecutorRoutineStarted executorRoutineStarted) {
	}

}
