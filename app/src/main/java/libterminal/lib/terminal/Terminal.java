package libterminal.lib.terminal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import libterminal.lib.executor.CustomExecutor;
import libterminal.lib.executor.Executor;
import libterminal.lib.executor.PlayerExecutor;
import libterminal.lib.keepalive.KeepAlive;
import libterminal.lib.node.Node;
import libterminal.lib.protocol.QSYPacket;
import libterminal.lib.protocol.QSYPacket.PacketType;
import libterminal.lib.results.Results;
import libterminal.lib.routine.Color;
import libterminal.lib.routine.Routine;
import libterminal.patterns.observer.AsynchronousListener;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.Event.CommandRequestEvent;
import libterminal.patterns.observer.Event.ExecutorDoneExecutingEvent;
import libterminal.patterns.observer.Event.ExecutorRoutineStarted;
import libterminal.patterns.observer.Event.ExecutorStepTimeOutEvent;
import libterminal.patterns.observer.Event.IncomingPacketEvent;
import libterminal.patterns.observer.Event.KeepAliveErrorEvent;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.observer.EventSource;
import libterminal.patterns.visitor.EventHandler;

public final class Terminal extends EventSource implements Runnable, EventListener {

	private final TreeMap<Integer, Node> nodes;

	private final EventHandler eventHandler;

	private final AsynchronousListener internalListener;
	private final KeepAlive keepAlive;
	private volatile Executor executor;
	private final Object executorLock;

	private final AtomicBoolean searchNodes;
	private final AtomicBoolean running;

	private final AtomicBoolean touchEnabled;
	private final AtomicBoolean soundEnabled;

	public Terminal() {
		this.nodes = new TreeMap<>();
		this.eventHandler = new InternalEventHandler();
		this.internalListener = new AsynchronousListener();
		this.keepAlive = new KeepAlive(nodes);
		this.keepAlive.addListener(this);
		this.searchNodes = new AtomicBoolean(false);
		this.running = new AtomicBoolean(true);
		this.executor = null;
		this.executorLock = new Object();
		this.touchEnabled = new AtomicBoolean(false);
		this.soundEnabled = new AtomicBoolean(false);
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				final Event event = internalListener.getEvent();
				event.acceptHandler(eventHandler);
			} catch (final InterruptedException e) {
				try {
					this.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void processPacket(final QSYPacket qsyPacket) {
		switch (qsyPacket.getType()) {
		case Hello:
			if (searchNodes.get()) {
				final boolean contains;
				synchronized (nodes) {
					contains = nodes.containsKey(qsyPacket.getId());
				}
				if (!contains) {
					Node node;
					try {
						node = new Node(qsyPacket);
						synchronized (nodes) {
							nodes.put(node.getNodeId(), node);
						}
						keepAlive.newNodeCreated(node);
						sendEvent(new Event.NewNodeEvent(node));
					} catch (final IllegalArgumentException e) {
						System.err.println(e.getMessage());
					} catch (final IOException e) {
						this.running.set(false);
						e.printStackTrace();
					}
				}
			}
			break;
		case Keepalive:
			keepAlive.qsyKeepAlivePacketReceived(qsyPacket);
			break;
		case Touche:
			synchronized (executorLock) {
				if (executor != null) {
					executor.touche(qsyPacket.getId(), qsyPacket.getNumberOfStep(), qsyPacket.getColor(), qsyPacket.getDelay());
				}
			}
			sendEvent(new Event.ToucheReceivedEvent(qsyPacket.getId()));
			break;
		default:
			break;
		}
	}

	public TreeMap<Integer, Node> getNodes() {
		return nodes;
	}

	public void searchNodes() {
		searchNodes.set(true);
	}

	public void finalizeNodesSearch() {
		searchNodes.set(false);
	}

	public void executeCustom(final Routine routine, final TreeMap<Integer, Integer> nodesIdsAssociations, boolean soundEnabled, boolean touchEnabled) {
		synchronized (executorLock) {
			if (executor == null) {
				final TreeMap<Integer, Integer> associations = associateNodes(nodesIdsAssociations, routine.getNumberOfNodes());
				this.soundEnabled.set(soundEnabled);
				this.touchEnabled.set(touchEnabled);
				executor = new CustomExecutor(routine, associations);
				executor.addListener(this);
				executor.start();
			} else {
				throw new IllegalStateException("<< Terminal >> Hay una rutina activa. Finalizala antes de inciar otra.");
			}
		}
	}

	public void executePlayer(final TreeMap<Integer, Integer> nodesIdsAssociations, final int numberOfNodes, final ArrayList<Color> playersAndColors,
			final boolean waitForAllPlayers, final long timeOut, final long delay, final long maxExecTime, final int totalStep, final boolean stopOnTimeout,
			boolean soundEnabled, boolean touchEnabled) {

		synchronized (executorLock) {
			if (executor == null) {
				if (timeOut < 0 || delay < 0 || maxExecTime < 0 || totalStep < 0 || (maxExecTime == 0 && totalStep == 0)
						|| playersAndColors.size() > numberOfNodes || playersAndColors.size() <= 0) {
					throw new IllegalArgumentException();
				}
				final TreeMap<Integer, Integer> associations = associateNodes(nodesIdsAssociations, numberOfNodes);
				this.soundEnabled.set(soundEnabled);
				this.touchEnabled.set(touchEnabled);
				executor = new PlayerExecutor(associations, numberOfNodes, playersAndColors, waitForAllPlayers, timeOut, delay, maxExecTime, totalStep,
						stopOnTimeout);
				executor.addListener(this);
				executor.start();
			} else {
				throw new IllegalStateException("<< Terminal >> Hay una rutina activa. Finalizala antes de iniciar otra.");
			}
		}
	}

	public void stopExecution() {
		synchronized (executorLock) {
			if (executor != null && executor.canStop()) {
				executor.stop();
				executor.removeListener(this);
				this.soundEnabled.set(false);
				this.touchEnabled.set(false);
				executor = null;
			} else {
				throw new IllegalStateException(
						"<< Terminal >> No se ha podido finalizar la rutina. Es posible que no exista la rutina o que se encuentre en el estado de preparación.");
			}
		}
	}

	private TreeMap<Integer, Integer> associateNodes(final TreeMap<Integer, Integer> nodesIdsAssociations, final int numberOfNodes) {
		TreeMap<Integer, Integer> nodesAddresses;
		if (nodesIdsAssociations == null) {
			synchronized (nodes) {
				nodesAddresses = getNodesAssociationsInOrder(numberOfNodes);
			}
		} else {
			synchronized (nodes) {
				nodesAddresses = getNodesAssociationsFromIds(nodesIdsAssociations);
			}
		}
		return nodesAddresses;
	}

	private TreeMap<Integer, Integer> getNodesAssociationsInOrder(final int numberOfNodes) {
		if (nodes.size() >= numberOfNodes) {
			final TreeMap<Integer, Integer> nodesAddresses = new TreeMap<>();
			int i = 1;
			for (final Entry<Integer, Node> entry : nodes.entrySet()) {
				nodesAddresses.put(i++, entry.getValue().getNodeId());
				if (i > numberOfNodes) {
					break;
				}
			}
			return nodesAddresses;
		} else {
			throw new IllegalArgumentException("<< Terminal >> No hay suficientes nodos para hacer la asociacion");
		}
	}

	private TreeMap<Integer, Integer> getNodesAssociationsFromIds(final TreeMap<Integer, Integer> nodesIdsAssociations) {
		if (nodesIdsAssociations.size() <= nodes.size()) {
			final TreeMap<Integer, Integer> nodesAddresses = new TreeMap<>();
			for (final Entry<Integer, Integer> entry : nodesIdsAssociations.entrySet()) {
				if (nodes.containsKey(entry.getValue())) {
					nodesAddresses.put(entry.getKey(), entry.getValue());
				} else {
					throw new IllegalStateException("<< Terminal >> No hay suficientes nodos registrados para hacer la asociacion.");
				}
			}
			return nodesAddresses;
		} else {
			throw new IllegalStateException("<< Terminal >> No hay suficientes nodos registrados para hacer la asociacion.");
		}

	}

	public void sendQSYPacket(final QSYPacket qsyPacket) {
		if (qsyPacket.getType() == PacketType.Command) {
			sendEvent(new Event.CommandPacketSentEvent(qsyPacket));
			sendEvent(new Event.CommandIssuedEvent(qsyPacket.getId(), qsyPacket.getColor(), qsyPacket.getDelay()));
		}
	}

	@Override
	public void close() throws Exception {
		running.set(false);
		keepAlive.close();
		synchronized (nodes) {
			for (Entry<Integer, Node> entry : nodes.entrySet()) {
				try {
					entry.getValue().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			nodes.clear();
		}
		super.close();
	}

	@Override
	public void receiveEvent(final Event event) {
		internalListener.receiveEvent(event);
	}

	public InetAddress getNodeAddress(final Integer nodeId) {
		return nodes.get(nodeId).getNodeAddress();
	}

	private final class InternalEventHandler extends EventHandler {

		@Override
		public void handle(final IncomingPacketEvent event) {
			super.handle(event);
			processPacket(event.getPacket());
		}

		@Override
		public void handle(final KeepAliveErrorEvent event) {
			super.handle(event);
			try {
				final Node node = event.getNode();
				synchronized (nodes) {
					nodes.remove(node.getNodeId());
				}
				node.close();
				sendEvent(new Event.DisconnectedNodeEvent(node));
				System.err.println("Se ha desconectado el nodo id = " + node.getNodeId());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void handle(final CommandRequestEvent event) {
			super.handle(event);
			final InetAddress nodeAddress;
			final Node node;
			synchronized (nodes) {
				node = nodes.get(event.getPhysicalId());
			}
			if(node == null) {
				return;
			}
			nodeAddress = node.getNodeAddress();
			final QSYPacket commandPacket = QSYPacket.createCommandPacket(nodeAddress, event.getPhysicalId(), event.getColor(), event.getDelay(),
					event.getNumberOfStep(), touchEnabled.get(), soundEnabled.get());
			sendQSYPacket(commandPacket);
		}

		@Override
		public void handle(final ExecutorRoutineStarted event) {
			super.handle(event);
			sendEvent(new Event.RoutineStartedEvent());
		}

		@Override
		public void handle(final ExecutorStepTimeOutEvent event) {
			super.handle(event);
			System.out.println("Timeout de step.");
		}

		@Override
		public void handle(final ExecutorDoneExecutingEvent event) {
			super.handle(event);
			Results results = null;
			synchronized (executorLock) {
				if (executor != null) {
					executor.stop();
					executor.removeListener(Terminal.this);
					results = executor.getResults();
					executor = null;
					soundEnabled.set(false);
					touchEnabled.set(false);
				}
			}
			System.out.println("Se termino la rutina");
			sendEvent(new Event.RoutineFinishedEvent(results));
		}

	}
}
