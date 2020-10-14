import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer extends Thread {

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private static int port;
    protected LinkedList threadList = new LinkedList();
    private Iterator iterator;

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println("Ungültige port Angabe :\n" + nfe.toString());
            }
        }
        ChatServer server = new ChatServer();
        server.init();
        server.start();

        StringBuffer sb;
        char c;
        String ausgabe;
        while (true) {
            sb = new StringBuffer();
            try {
                Reader in = new InputStreamReader(System.in);
                while ((c = (char) in.read()) != '\r') {
                    sb.append(c);
                }
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            }
            ausgabe = sb.toString();
            if (ausgabe.equals("exit")) {
                server.closeAll();
            } else {
                System.out.println("\"" + ausgabe + "\" wird nicht ausgwertet.");
            }
        }
    }

    private void init() {
        /**
         * if Port number is 0 than it will be automatically 4444
         */
        if (port == 0) {
            System.out.println("You didn't enter a valid port number. Port number setted to default 4444\n");
            port = 4444;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server horch an port " + port);
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }
    }

    public void run() {
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Verbindung zu Socket " + clientSocket.getRemoteSocketAddress());
                ClientThread clientThread = new ClientThread(clientSocket, this);
                Thread thread = new Thread(clientThread, clientThread.getThreadName());
                threadList.add(clientThread);
                thread.start();
            } catch (IOException ioe) {
                System.out.println("ChatServer.run: " + ioe.toString());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    System.out.println(ie.toString());
                }
            }
        }
    }

    public void closeAll() {
        // Die Streams werden in den ClientThread Objekten geschlossen
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }
    }

    public void generateOutput(String output) {
        iterator = threadList.iterator();
        while (iterator.hasNext()) {
            ClientThread thread = (ClientThread) iterator.next();
            thread.output(output);
        }
    }
}