package NIO2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class LoadTestingClient {
    public static void main(String[] args) {
        LoadTestingClient timerApp = new LoadTestingClient();
        timerApp.showHelp();
        String serverHostName = null;
        String serverPortNumber = null;
        String noOfIterations = null;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input = bufferedReader.readLine().trim();
            String [] tokens = input.split(" ");
            String commandLowerCase = tokens[0].toLowerCase();

            switch(commandLowerCase) {
                case "loadtest":
                    serverHostName = tokens[1];
                    serverPortNumber = tokens[2];
                    noOfIterations = tokens[3];
                    for(int clientCtr=0;clientCtr<Integer.parseInt(noOfIterations);clientCtr++) {
                        TimerTask task = new NIO2Task("CLIENT_"+clientCtr, serverHostName, serverPortNumber);
                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(task, 1000, 10000);
                    }
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showHelp() {
        System.out.println("Enter the details of the server");
        System.out.println("loadtest %hostname% %port% %number_of_gps_devices%-- loadtest client calls with 80  e.g. loadtest 192.168.111.122 9999 80");
    }
}
