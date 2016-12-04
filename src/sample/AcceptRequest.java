package sample;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dominik on 04.12.2016.
 */
public class AcceptRequest implements Runnable {

    private Socket connectionSocket = null;

    public AcceptRequest(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }


    @Override
    public void run() {
        try {
            String clientFormat;
            String responseForClient = "\n";
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            clientFormat = inFromClient.readLine();

            System.out.println("Received: " + clientFormat);
            SimpleDateFormat simpleDateFormat = null;
            try {
                simpleDateFormat = new SimpleDateFormat(clientFormat);
            } catch (IllegalArgumentException e) {
                responseForClient = "bad date format\n";
            }

            if (simpleDateFormat != null) {
                responseForClient = simpleDateFormat.toPattern() + " -> " + simpleDateFormat.format(new Date()) + "\n";
            }

            outToClient.writeBytes(responseForClient);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
