package libterminal.lib.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import libterminal.lib.keepalive.KeepAlive;
import libterminal.lib.protocol.QSYPacket;

public class Node implements Comparable<Node>, AutoCloseable {

	private final int nodeId;
	private final InetAddress nodeAddress;
	private final SocketChannel nodeSocketChannel;
	private boolean keepAliveIsUp = false;

	private long previousKeepalive;

	public Node(final QSYPacket qsyPacket) throws IOException, IllegalArgumentException {
		if (qsyPacket.getType() == QSYPacket.PacketType.Hello) {
			final InetSocketAddress hostAddress = new InetSocketAddress(qsyPacket.getNodeAddress().getHostAddress(), QSYPacket.TCP_PORT);
			final SocketChannel nodeSocketChannel = SocketChannel.open(hostAddress);
			nodeSocketChannel.socket().setTcpNoDelay(true);
			nodeSocketChannel.configureBlocking(false);
			this.nodeId = qsyPacket.getId();
			this.nodeAddress = qsyPacket.getNodeAddress();
			this.nodeSocketChannel = nodeSocketChannel;
		} else {
			throw new IllegalArgumentException("<< NODE >> El QSYPacket recibido no es un QSYHelloPacket.");
		}
	}

	public int getNodeId() {
		return nodeId;
	}

	public InetAddress getNodeAddress() {
		return nodeAddress;
	}

	public SocketChannel getNodeSocketChannel() {
		return nodeSocketChannel;
	}

	public synchronized void keepAlive(final long now) {
		keepAliveIsUp = true;
		this.previousKeepalive = now;
	}

	public synchronized boolean isAlive(final long now) {
		return !keepAliveIsUp || (now - this.previousKeepalive < KeepAlive.MAX_KEEP_ALIVE_DELAY);
	}

	@Override
	public int compareTo(final Node node) {
		return nodeId - node.nodeId;
	}

	@Override
	public void close() throws IOException {
		nodeSocketChannel.close();
	}

}
