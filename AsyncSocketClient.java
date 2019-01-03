package NIO2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.logging.*;

public class AsyncSocketClient {

    private Logger logger = Logger.getLogger(AsyncSocketClient.class.getName());


    private String serverName= null;
    private int port;
    private String clientName;

    public final static int MESSAGE_INPUT_SIZE= 512;

    private final static int WAIT_TIME = 10;

    public AsyncSocketClient(String clientName, String serverName, int port) throws IOException {
        //logger.info(">>AsynCounterClient(clientName=" + clientName + ",serverName=" +
          //      serverName + ",port=" + port + ")");
        this.clientName = clientName;
        this.serverName = serverName;
        this.port = port;
    }


    private AsynchronousSocketChannel connectToServer(int waitTime)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        AsynchronousSocketChannel asyncSocketChannel = AsynchronousSocketChannel.open();
        Future<Void> connectFuture = null;

        // Connecting to server
        //logger.info("Connecting to server... " + serverName + ",port=" + port);
        connectFuture = asyncSocketChannel.connect(new InetSocketAddress(this.serverName, this.port));

        // You have 10 seconds to connect. This will throw exception if server is not there.
        connectFuture.get(waitTime, TimeUnit.SECONDS);

        asyncSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * MESSAGE_INPUT_SIZE);
        asyncSocketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * MESSAGE_INPUT_SIZE);
        asyncSocketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);

        return asyncSocketChannel;
    }

    public void sendMessage(String request) {
        logger.info(request);
        AsynchronousSocketChannel asyncSocketChannel = null;
        try {
            asyncSocketChannel = connectToServer(WAIT_TIME);
            ByteBuffer messageByteBuffer = ByteBuffer.wrap(request.getBytes());
            Future<Integer> futureWriteResult = asyncSocketChannel.write(messageByteBuffer);
            futureWriteResult.get();
            messageByteBuffer.clear();
        } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
            handleException(e);
        } finally {
            if (asyncSocketChannel.isOpen()) {
                try {
                    asyncSocketChannel.close();
                } catch (IOException e) {
                    // Not really anything we can do here.
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
}