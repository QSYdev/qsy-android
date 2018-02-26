package libterminal.patterns.observer;

import java.util.LinkedList;
import java.util.List;

public abstract class EventSource implements AutoCloseable {

	private final List<EventListener> listeners;
	private final List<Runnable> pendingActions;

	public EventSource() {
		this.listeners = new LinkedList<>();
		this.pendingActions = new LinkedList<>();
	}

	public final void addListener(final EventListener eventListener) {
		synchronized (pendingActions) {
			pendingActions.add(new Runnable() {
				@Override
				public void run() {
					listeners.add(eventListener);
				}
			});
		}
	}

	public final void removeListener(final EventListener eventListener) {
		synchronized (pendingActions) {
			pendingActions.add(new Runnable() {
				@Override
				public void run() {
					listeners.remove(eventListener);
				}
			});
		}
	}

	public final void removeAllListeners() {
		synchronized (pendingActions) {
			pendingActions.add(new Runnable() {
				@Override
				public void run() {
					listeners.clear();
				}
			});
		}
	}

	public final void sendEvent(final Event event) {
		synchronized (pendingActions) {
			for (final Runnable task : pendingActions) {
				task.run();
			}
			pendingActions.clear();
		}
		for (final EventListener eventListener : listeners) {
			eventListener.receiveEvent(event);
		}
	}

	@Override
	public void close() throws Exception {
		for (final Runnable task : pendingActions) {
			task.run();
		}
		pendingActions.clear();
		listeners.clear();
	}

}
