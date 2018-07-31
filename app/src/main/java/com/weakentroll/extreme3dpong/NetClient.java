package com.weakentroll.extreme3dpong;

/**
 * Created by Ryan on 3/4/2017.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetClient {

    /**
     * Maximum size of buffer
     */
    public static final int BUFFER_SIZE = 2048;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private String host = null;
    private String macAddress = null;
    private int port = 60001;


    /**
     * Constructor with Host, Port and MAC Address
     * @param host
     * @param port
     * @param macAddress
     */
    public NetClient(String host, int port, String macAddress) {
        this.host = host;
        this.port = port;
        this.macAddress = macAddress;
    }//

    private boolean connectWithServer() {
        try {
            if (socket == null) {
                System.out.println("reached thread run() 2");

                socket = new Socket(this.host, this.port);
                System.out.println("reached thread run() 3");

                out = new PrintWriter(socket.getOutputStream());
                System.out.println("reached thread run() 4");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("reached thread run() 5");

            }
        } catch (UnknownHostException e1) {
            System.out.println("reached thread run() 6");
            e1.printStackTrace();
            return false;
        }
        catch (IOException e) {
            System.out.println("reached thread run() 7");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void disConnectWithServer() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                    in = null;
                    out = null;
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean sendDataWithString(String message) {
        if (message != null) {
            if (connectWithServer() == false)
                return false;
            out.write(message);
            out.flush();
        }
        return true;
    }

    public String receiveDataFromServer() {
        try {
            String message = "";
            int charsRead = 0;
            char[] buffer = new char[BUFFER_SIZE];

            while ((charsRead = in.read(buffer)) != -1) {
                message += new String(buffer).substring(0, charsRead);
            }
            // only disconnect when the player exits multiplayer lobby
            disConnectWithServer(); // disconnect server
            return message;
        } catch (IOException e) {
            return "Error receiving response:  " + e.getMessage();
        }
    }


}
