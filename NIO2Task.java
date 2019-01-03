package NIO2;

import java.io.IOException;
import java.util.TimerTask;

public class NIO2Task extends TimerTask {
    String clientNumber = null;
    String hostName = null;
    String port = null;

    public NIO2Task(String clientNumber, String hostName, String port) {
        this.clientNumber = clientNumber;
        this.hostName = hostName;
        this.port = port;
    }

    private int getRandomNumber(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }

    @Override
    public void run() {
        try {
            String gpsMessage = null;
            // send your gps message here. To do a load test dummy message injected
            gpsMessage = "$$"+clientNumber+","+getRandomNumber(10000000,99999999)+",1,13.004469,77.712280,170421062208,A,20,0,462794,72,2,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,*23";
            testAutomatedLoadMessage("connectandsend "+hostName+" "+port, gpsMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testAutomatedLoadMessage(String input, String gpsMessage) throws IOException {
        AsyncSocketClient client = null;
        // Note uppercase and trim used to minimise typos.
        String serverHostname = null;
        String command = null;
        if (input.indexOf(" ") > 0) {
            String [] tokens = input.split(" ");
            command = tokens[0];
            if (command.equalsIgnoreCase("CONNECTANDSEND")) {
                serverHostname = tokens[1];
                String serverPortNumber = tokens[2];
                try {
                    int port = Integer.parseInt(serverPortNumber.trim());
                    client = new AsyncSocketClient("cli_client", serverHostname,
                            port);
                    client.sendMessage(gpsMessage);
                } catch (Exception e) {
                    System.out.println("************** Cannot connect to server on host=" +
                            serverHostname + ",port=" + serverPortNumber +
                            ", check you have no typos and server is running");
                    e.printStackTrace();
                }
            }
            // have successfully tested client.
        }
    }
}
