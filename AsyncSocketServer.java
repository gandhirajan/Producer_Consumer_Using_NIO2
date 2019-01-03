package NIO2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.logging.*;

public final class AsyncSocketServer implements Runnable{
	private AsynchronousChannelGroup asyncChannelGroup;
	private String name;
	private AsynchronousServerSocketChannel asyncServerSocketChannel;

	public final static int READ_MESSAGE_WAIT_TIME = 15;
    public final static int MESSAGE_INPUT_SIZE= 512;

	Logger logger = Logger.getLogger(AsyncSocketServer.class.getName());

	private AsyncSocketServer() {}

	AsyncSocketServer(String name) throws IOException, InterruptedException, ExecutionException {
    	this.name = name;
		asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(
				Executors.newCachedThreadPool(new NamedThreadFactory(name + "_Group_Thread")));
    }


    void open(InetSocketAddress serverAddress) throws IOException{
   		// open a server channel and bind to a free address, then accept a connection
    	//logger.info("Opening Aysnc ServerSocket channel at " + serverAddress);
   		asyncServerSocketChannel = AsynchronousServerSocketChannel.open(asyncChannelGroup).bind(
   				serverAddress);
   		asyncServerSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, MESSAGE_INPUT_SIZE);
   		asyncServerSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    }

    public void run() {
    	try {
    		if (asyncServerSocketChannel.isOpen()) {
	    		// The accept method does not block it sets up the CompletionHandler callback and moves on.
				asyncServerSocketChannel.accept(null, new CompletionHandler <AsynchronousSocketChannel, Object>() {
					@Override
					public void completed(final AsynchronousSocketChannel asyncSocketChannel, Object attachment) {
						if (asyncServerSocketChannel.isOpen()) {
							asyncServerSocketChannel.accept(null, this);
						}
						handleAcceptConnection(asyncSocketChannel);
					}
					@Override
					public void failed(Throwable exc, Object attachment) {
						if (asyncServerSocketChannel.isOpen()) {
							asyncServerSocketChannel.accept(null, this);
							System.out.println("***********" + exc  + " statement=" + attachment);
						}
					}
				});
				//logger.info("Server "+ getName() + " reading to accept first connection...");
    		}
    	} catch (AcceptPendingException ex) {
    		ex.printStackTrace();
    	}
    }

    public void stopServer() throws IOException {
    	logger.info(">>stopingServer()...");
		this.asyncServerSocketChannel.close();
		this.asyncChannelGroup.shutdown();
    }

    private void handleAcceptConnection(AsynchronousSocketChannel asyncSocketChannel) {
    	//logger.info(">>handleAcceptConnection(), asyncSocketChannel=" +asyncSocketChannel);
		ByteBuffer messageByteBuffer = ByteBuffer.allocate(MESSAGE_INPUT_SIZE);
		try {
			// read a message from the client, timeout after 10 seconds
			Future<Integer> futureReadResult = asyncSocketChannel.read(messageByteBuffer);
			futureReadResult.get(READ_MESSAGE_WAIT_TIME, TimeUnit.SECONDS);

			String clientMessage = new String(messageByteBuffer.array()).trim();

			messageByteBuffer.clear();
			messageByteBuffer.flip();
			logger.info(clientMessage);
			String responseString = clientMessage;
			messageByteBuffer = ByteBuffer.wrap((responseString.getBytes()));
			Future<Integer> futureWriteResult = asyncSocketChannel.write(messageByteBuffer);
			futureWriteResult.get(READ_MESSAGE_WAIT_TIME, TimeUnit.SECONDS);
			if (messageByteBuffer.hasRemaining()) {
				messageByteBuffer.compact();
			} else {
				messageByteBuffer.clear();
			}
		} catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
			logger.severe(e.getMessage());
		} finally {
			try {
				asyncSocketChannel.close();
			} catch (IOException ioEx) {
				logger.severe(ioEx.getMessage());
			}
		}
    }


	public String getName() {
		return this.name;
	}

}