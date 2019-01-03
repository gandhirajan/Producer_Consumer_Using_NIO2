package NIO2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.logging.*;

public enum ServerManager {
	INSTANCE;

	private ConcurrentHashMap<String, AsyncSocketServer> servers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, InetSocketAddress> serverAddresses = new ConcurrentHashMap<>();
	private ExecutorService executors = Executors.newCachedThreadPool(new NamedThreadFactory("ServerManager_Cache_TP_Thread"));

	private Logger logger = Logger.getLogger(ServerManager.class.getName());

	/**
	 * Starts a Server on a specified addess.
	 * @param serverName
	 * @param netSocketAddress
	 * @return Response from pinging started server.
	 */
	public String startServer(String serverName, InetSocketAddress netSocketAddress) {
		//logger.info(">>startServer(serverName=" + serverName + ",InetSocketAddress=" + netSocketAddress + ")");
		AsyncSocketServer server = null;
		String returnMessage = null;
		try {
			// Is this the first server?
			if (!servers.contains(serverName)) {
				server = new AsyncSocketServer(serverName);
				servers.put(serverName, server);
			} else {
				server = getServer(serverName);
			}

			if (serverAddresses.contains(serverName)) {
				// server already started on another port?
				throw new RuntimeException("Server already started on " + serverAddresses.get(serverName));
			}

			this.serverAddresses.put(serverName, netSocketAddress);
			server.open(netSocketAddress);
			// begin accepting connections
			executors.execute(server);

			// Now give the server a sec
			Thread.sleep(1500);

			// And ping to make sure all ok.
			returnMessage = pingServer(serverName);

		} catch (InterruptedException | IOException | ExecutionException ex) {
			throw new RuntimeException("Problem startig server", ex);
		}
		//logger.info("Server " + serverName + " on " + netSocketAddress + ",open for business");
		return returnMessage;
	}

	/**
	 * Example calling of this API:
	 * <code>
	 * ServerManager.startServer('myServer', startServerInetAddress.getLocalHost().getHostName(), 999);
	 * </code>
	 * @param serverName
	 * @param hostName
	 * @param port
	 */
	public String startServer(String serverName, String hostName, int port)  {
		String returnMessage = null;
		InetSocketAddress socketAddress = new InetSocketAddress(hostName, port);
		returnMessage = startServer(serverName, socketAddress);
		return returnMessage;
	}

	/**
	 * Pings servers to ensure it is up.
	 * @param serverName
	 * @return Response from Ping
	 */
	public String pingServer(String serverName) {
		String returnResult = null;
		if (servers.containsKey(serverName)) {
			try {
				AsynchronousSocketChannel asyncSocketChannel = AsynchronousSocketChannel.open();
				// Connecting to server
				//logger.info("Connecting to server... " + serverName);
				long beginTime = System.currentTimeMillis();
				InetSocketAddress socketAddress = this.serverAddresses.get(serverName);
				Future connectFuture = asyncSocketChannel.connect(socketAddress);
				connectFuture.get();
				ByteBuffer messageByteBuffer = ByteBuffer.wrap("PING".getBytes());

				// wait for the response
				Future<Integer> futureWriteResult = asyncSocketChannel.write(messageByteBuffer);
				futureWriteResult.get();

				//Now wait for return message.
				ByteBuffer returnMessage = ByteBuffer.allocate(100);
				Future<Integer> futureReadResult = asyncSocketChannel.read(returnMessage);
				futureReadResult.get();

				// wait for the response
				returnResult = new String(returnMessage.array());
				messageByteBuffer.clear();

				asyncSocketChannel.close();
				long endTime = System.currentTimeMillis();

			} catch (InterruptedException | ExecutionException | IOException e) {
				throw new RuntimeException("Problem pinging server", e);
			}
		} else {
			// cannot ping a server that does not exist
			returnResult = "SERVER NOT REACHABLE";
		}
		//logger.info("<<ping() return=" + returnResult);
		return returnResult;
	}


	/**
	 * Stops server. If writeToFile is true and this is the last server, counter value
	 * will be written to file.
	 * @param serverName
	 * @param writeToFile
	 */
	public void stopServer(String serverName, boolean writeToFile) {
		try {
			AsyncSocketServer asyncJava7Server = getServer(serverName);
			asyncJava7Server.stopServer();
			String fileString = "Server " + serverName + " stopped at " + new Date().toString();
			logger.info(fileString);

			// Now remove.
			this.servers.remove(serverName);
			this.serverAddresses.remove(serverName);

		} catch (IOException | InterruptedException | ExecutionException ex) {
			throw new RuntimeException("Problem stopping server", ex);
		}
	}

	/**
	 * Lists all Servers running.
	 * @return
	 */
	public List<String> listServersRunning() {
		ArrayList<String> serverNames = Collections.list(servers.keys());
		return serverNames;
	}

	public InetSocketAddress getServerPort(String serverName) {
		return this.serverAddresses.get(serverName);
	}

	/**
	 * Restarts Server.
	 * @param serverName
	 */
	public void restartServer(String serverName){
		InetSocketAddress netSocketAddress = this.serverAddresses.get(serverName);
		stopServer(serverName, true);
		startServer(serverName, netSocketAddress);
	}

	private AsyncSocketServer getServer(String serverName) throws IOException, InterruptedException, ExecutionException  {
		AsyncSocketServer server = servers.get(serverName);
		return server;
	}

}