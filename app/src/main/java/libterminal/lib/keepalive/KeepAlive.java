package libterminal.lib.keepalive;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import libterminal.lib.node.Node;
import libterminal.lib.protocol.QSYPacket;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventSource;

public final class KeepAlive extends EventSource {

	// TODO: evaluar cuando funciona y cuando no el periodo del purger
	public static final int MAX_KEEP_ALIVE_DELAY = (int) (10 * QSYPacket.KEEP_ALIVE_MS);

	private final TreeMap<Integer, Node> nodes;
	private final Timer timer;
	private final DeadNodesPurger deadNodesPurgerTask;

	public KeepAlive(final TreeMap<Integer, Node> nodes) {
		this.nodes = nodes;
		this.timer = new Timer("Dead Nodes Purger", false);
		this.timer.schedule(deadNodesPurgerTask = new DeadNodesPurger(), 0, MAX_KEEP_ALIVE_DELAY);
	}

	public void newNodeCreated(final Node node) {
		final long currentTime = System.currentTimeMillis();
		final boolean nodeAlive;

		nodeAlive = node.isAlive(currentTime);
		node.keepAlive(currentTime);

		if (!nodeAlive) {
			sendEvent(new Event.KeepAliveErrorEvent(node));
		}
	}

	public void qsyKeepAlivePacketReceived(final QSYPacket qsyPacket) {
		final long currentTime = System.currentTimeMillis();

		final Node node;
		synchronized (nodes) {
			node = nodes.get(qsyPacket.getId());
		}
		node.keepAlive(currentTime);
	}

	@Override
	public void close() throws Exception {
		timer.cancel();
		deadNodesPurgerTask.close();
		super.close();
	}

	private final class DeadNodesPurger extends TimerTask implements AutoCloseable {

		public DeadNodesPurger() {
		}

		@Override
		public void run() {
			final long currentTime = System.currentTimeMillis();
			boolean nodeAlive = true;
			Node disconnectedNode = null;

			synchronized (nodes) {
				for (final Node node : nodes.values()) {
					if (!node.isAlive(currentTime)) {
						nodeAlive = false;
						disconnectedNode = node;
						break;
					}
				}
			}

			if (!nodeAlive) {
				sendEvent(new Event.KeepAliveErrorEvent(disconnectedNode));
			}
		}

		@Override
		public void close() {
			return;
		}

	}

}
