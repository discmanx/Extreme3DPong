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
import java.util.concurrent.Semaphore;
import android.os.Message;


public class NetClient implements Runnable {

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
    private boolean serverUp = false;
    private Semaphore available = null;



    /**
     * Constructor with Host, Port and MAC Address
     * @param host
     * @param port
     * @param macAddress
     */
    public NetClient(String host, int port, String macAddress, Semaphore available) {
        this.host = host;
        this.port = port;
        this.macAddress = macAddress;
        this.available = available;
    }//

    @Override
    public void run()  {

        while (!Thread.currentThread().isInterrupted()) {

            if (isServerUp() == true) {
                try {
                //System.out.println("netclient run() available.acquire()");
                    available.acquire();
                    String r = receiveDataFromServer();
                    if (r != null) {
                        System.out.print(r);
                        Message msg = new Message();
                        msg.what = 6;
                        msg.obj = r;
                        MainActivity.mHandler.sendMessage(msg);
                    }
                //System.out.println("netclient run() available.release()");

                available.release();
                }
                catch ( InterruptedException e) {
                    //available.release();
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isServerUp() {
        return serverUp;
    }

    public boolean connectWithServer() {
        try {
            if (socket == null) {
                System.out.println("reached thread run() 2");

                socket = new Socket(this.host, this.port);
                System.out.println("reached thread run() 3");

                out = new PrintWriter(socket.getOutputStream());
                System.out.println("reached thread run() 4");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("reached thread run() 5");
                serverUp = true;


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

    public void disConnectWithServer() {
        try {
            available.acquire();

            if (socket != null) {

                if (socket.isConnected()) {

                    try {
                        in.close();
                        out.close();
                        socket.close();
                        //in = null; be careful when connecting() again that in and out arent empty
                        //out = null;
                        socket = null;
                        serverUp = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            available.release();
        }
        catch ( InterruptedException e) {
            //available.release();
            e.printStackTrace();
        }
    }

    public boolean sendDataWithString(String message) {
        if (message != null) {
            try {
                available.acquire();
                System.out.println("netclient senddatawithstring() available.acquire()");

                out.write(message);
                out.flush();
                available.release();
                System.out.println("netclient senddatawithstring()  available.release();");
            } catch (InterruptedException e) {
                System.out.println("netclient senddatawithstring() catch interruptedexcepetion called");
            }
        }
        return true;
    }

    public synchronized String receiveDataFromServer() {
        //System.out.println("netclient receivedatafromserver()  ");

        try {
            //available.acquire();
          //  System.out.println("netclient receivedatafromserver() available.acquire()");


            String message = "";
            /*int charsRead = 0;
            char[] buffer = new char[BUFFER_SIZE];

            while ((charsRead = in.read(buffer)) != -1) {
                message += new String(buffer).substring(0, charsRead);
            }*/
            byte[] messageByte = new byte[1000];
            boolean end = false;
            int charRead;
            /*
            messageByte[0] = (byte)(in.read());
            int bytesToRead = messageByte[0];*/

            /*char cbuf[] = new char[4];
            int bytesToRead = in.read(cbuf, 0, 4);*/
            if (in.ready() == false) {
                //System.out.println("netclient receivedatafromserver() in.ready() == false called, returned null;");
                //available.release();
                return null;
            }

            String bytesToRead = in.readLine();
            System.out.println("netclient receivedatafromserver() bytestoread = " + bytesToRead);

            while ( !end ) {
                charRead = in.read();
                System.out.print(charRead);
                message += (char)charRead;//new String(messageByte, 0, charRead);
                if (message.length() == Integer.parseInt(bytesToRead) )
                {
                    end = true;
                }
            }
            /*while ( (charRead = in.read()) != -1) {
                message += (char)charRead;
            }*/
            //available.release();
            //System.out.println("netclient receivedatafromserver() available.release()");

            return message;
        } catch (IOException e) {
            return "Error receiving response:  " + e.getMessage();
        } /*catch (InterruptedException e) {
            System.out.println("netclient receivedatafromserver() catch interruptedexcepetion called");

            return "Error receiving response:  " + e.getMessage();//printStackTrace();
        }*/
    }
}
