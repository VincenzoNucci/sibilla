package quasylab.sibilla.core.simulator.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.nustaq.net.TCPObjectSocket;
import org.nustaq.serialization.FSTConfiguration;

public class TCPFSTNetworkManager implements TCPNetworkManager {
	private Socket socket;
	private TCPObjectSocket FSTSocket;
	private FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();

	public TCPFSTNetworkManager(Socket socket) throws IOException {
		this.socket = socket;
		FSTSocket = new TCPObjectSocket(socket, conf);
	}

	public TCPFSTNetworkManager(Socket socket, ClassLoader classLoader) throws IOException {
		this.socket = socket;
		conf.setClassLoader(classLoader);
		FSTSocket = new TCPObjectSocket(socket, conf);
	}

	@Override
	public byte[] readObject() throws Exception {
		return (byte[]) FSTSocket.readObject();
	}

	@Override
	public void writeObject(byte[] toWrite) throws Exception {
		FSTSocket.writeObject(toWrite);
		FSTSocket.flush();
	}

	@Override
	public void setTimeout(long timeout) throws SocketException {
		socket.setSoTimeout((int) (timeout / 1000000));
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

}