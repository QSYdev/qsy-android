package libterminal.patterns.observer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsynchronousListener implements EventListener {

	private final BlockingQueue<Event> eventQueue;

	public AsynchronousListener() {
		this.eventQueue = new LinkedBlockingQueue<>();
	}

	@Override
	public void receiveEvent(final Event event) {
		this.eventQueue.add(event);
	}

	public final Event getEvent() throws InterruptedException {
		return eventQueue.take();
	}

	public final Event peekEvent() {
		return eventQueue.peek();
	}

}
