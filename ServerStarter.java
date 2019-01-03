package NIO2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;


public class ServerStarter {
	public static void main(String args[]) throws IOException {
		boolean running = true;
		// begin by showing help.
		showHelp();
		while (running) {
	        // Note uppercase and trim used to minimise typos.
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	        String input = bufferedReader.readLine().trim();
	        String commandLowerCase = null;
	        String serverName = null;
	        String serverPortNumber = null;
	        String serverHostName = null;
	        if (input.indexOf(" ") > 0) {
	        	String [] tokens = input.split(" ");
	        	commandLowerCase = tokens[0].toLowerCase();
	        	serverName = tokens[1];
	        	if (tokens.length > 2) {
	        		serverHostName = tokens[2];
	        	}
	        	if (tokens.length > 3) {
					serverPortNumber = tokens[3];
	        	}
	        } else {
	        	commandLowerCase = input.toLowerCase();
	        }
	        switch(commandLowerCase) {
        		case "start":
        			ServerManager.INSTANCE.startServer(serverName, serverHostName,
        					Integer.parseInt(serverPortNumber));
        			break;
        		case "stop":
        			ServerManager.INSTANCE.stopServer(serverName, true);
        			break;
        		case "restart":
        			ServerManager.INSTANCE.restartServer(serverName);
        			break;
        		case "list":
        			List<String> servers = ServerManager.INSTANCE.listServersRunning();
        			StringBuffer sb = new StringBuffer();
        			sb.append("server running on " + InetAddress.getLocalHost());
        			for (String server: servers) {
            			sb.append("\n" + "Server:" + server + "," +
            						"address=" + ServerManager.INSTANCE.getServerPort(server));
        			}
        			break;
        		case "exit":
        			running = false;
        			break;
        		case "help":
        			showHelp();
        			break;
        		default:
        			break;
	        }
		}
	}

	private static void showHelp() {
		System.out.println("Enter one of the following...");
		System.out.println("help -- to display this message");
		System.out.println("start %servername% %hostname% %port% -- start server on a port e.g. start srv1 192.168.111.122 9999  ");
		System.out.println("stop %servername% -- stop server e.g. stop srv1");
		System.out.println("list -- list all servers running");
		System.out.println("restart %servername% -- restart %servername% e.g. restart srv1");
		System.out.println("reset -- resets counter to 0");
		System.out.println("exit -- to exit");
	}
}