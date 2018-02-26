package libterminal.api;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import libterminal.lib.network.MulticastReceiver;
import libterminal.lib.network.ReceiverSelector;
import libterminal.lib.network.Sender;
import libterminal.lib.protocol.CommandParameters;
import libterminal.lib.protocol.QSYPacket;
import libterminal.lib.routine.Color;
import libterminal.lib.routine.Routine;
import libterminal.lib.terminal.Terminal;
import libterminal.patterns.observer.EventListener;

public final class TerminalAPI {

	private List<EventListener> listeners;

	private Thread threadReceiveSelector;
	private Thread threadTerminal;
	private Thread threadSender;
	private Thread threadMulticastReceiver;
	private MulticastReceiver multicastReceiver;

	private Terminal terminal;

	private Inet4Address multicastAddress;

	private boolean up;

	public TerminalAPI(final Inet4Address multicastAddress) {
		this.multicastAddress = multicastAddress;
		this.listeners = new LinkedList<>();
	}

	public void start() throws IOException {
		multicastReceiver = new MulticastReceiver(multicastAddress, (InetAddress) InetAddress.getByName(QSYPacket.MULTICAST_ADDRESS), QSYPacket.MULTICAST_PORT);
		terminal = new Terminal();
		final ReceiverSelector receiverSelector = new ReceiverSelector();
		final Sender senderSelector = new Sender(terminal.getNodes());
		multicastReceiver.addListener(terminal);
		receiverSelector.addListener(terminal);
		terminal.addListener(receiverSelector);
		terminal.addListener(senderSelector);

		for (final EventListener listener : listeners) {
			terminal.addListener(listener);
		}

		threadReceiveSelector = new Thread(receiverSelector, "Receive Selector");
		threadTerminal = new Thread(terminal, "Terminal");
		threadSender = new Thread(senderSelector, "Sender Selector");
		threadMulticastReceiver = new Thread(multicastReceiver, "Multicast Receiver");

		threadReceiveSelector.start();
		threadTerminal.start();
		threadSender.start();
		threadMulticastReceiver.start();

		up = true;
	}

	public boolean isUp() {
		return up;
	}

	public void startNodesSearch() {
		if (up) {
			terminal.searchNodes();
		} else {
			throw new IllegalStateException("Terminal no está corriendo");
		}
	}

	public void stopNodesSearch() {
		if (up) {
			terminal.finalizeNodesSearch();
		} else {
			throw new IllegalStateException("Terminal no está corriendo");
		}
	}

	public void executeCustom(final Routine routine, final TreeMap<Integer, Integer> nodesIdsAssociations, boolean soundEnabled, boolean touchEnabled) {
		terminal.executeCustom(routine, nodesIdsAssociations, soundEnabled, touchEnabled);
	}

	public void executePlayer(final TreeMap<Integer, Integer> nodesIdsAssociations, final int numberOfNodes, final ArrayList<Color> playersAndColors,
			final boolean waitForAllPlayers, final long timeOut, final long delay, final long totalTimeOut, final int totalStep, final boolean stopOnTimeout,
			boolean soundEnabled, boolean touchEnabled) {
		terminal.executePlayer(nodesIdsAssociations, numberOfNodes, playersAndColors, waitForAllPlayers, timeOut, delay, totalTimeOut, totalStep, stopOnTimeout,
				soundEnabled, touchEnabled);
	}

	public void addListener(final EventListener listener) {
		listeners.add(listener);
		if (up)
			terminal.addListener(listener);
	}

	public void removeListener(final EventListener listener) {
		listeners.remove(listener);
		if (up)
			terminal.removeListener(listener);
	}

	public void stop() throws InterruptedException, Exception {
		if (up) {
			threadReceiveSelector.interrupt();
			threadTerminal.interrupt();
			threadSender.interrupt();
			multicastReceiver.close();
			threadReceiveSelector.join();
			threadTerminal.join();
			threadSender.join();
			threadMulticastReceiver.join();
			terminal = null;
			up = false;
		}
	}

	public int connectedNodesAmount() {
		if (up) {
			return terminal.getNodes().size();
		} else {
			throw new IllegalStateException("Terminal no está corriendo");
		}
	}

	public void stopExecution() {
		if (up) {
			terminal.stopExecution();
		} else {
			throw new IllegalStateException("Terminal no está corriendo");
		}
	}

	public void sendPacket(Integer nodeId, CommandParameters cp, boolean soundEnabled, boolean touchEnabled) {
		terminal.sendQSYPacket(QSYPacket.createCommandPacket(terminal.getNodeAddress(nodeId), cp.getPhysicalId(), cp.getColor(), cp.getDelay(),
				cp.getNumberOfStep(), touchEnabled, soundEnabled));
	}
}
