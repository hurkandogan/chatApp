import java.io.*;
import java.net.Socket;
import java.util.Iterator;

public class ClientThread implements Runnable {
    private ChatServer parent;
    private Socket clientSocket;
    private BufferedReader clientIn;
    private PrintWriter serverOut;
    private String threadName;
    private Iterator onlineUsers;

    public String getThreadName() {
        return threadName;
    }

    public ClientThread(Socket clientSocket, ChatServer parent) {
        this.parent = parent;
        this.clientSocket = clientSocket;
        try {
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverOut = new PrintWriter(clientSocket.getOutputStream(), true);
            // Die erste Meldung soll der Benutzername sein!
            threadName = clientIn.readLine();
            serverOut.println("SERVER: Connection von " + getThreadName() + " angenommen.");
            // Prints Online Users
            serverOut.println("<b>Am Chat nehmen bereits teil:<b>");
            onlineUsers = parent.threadList.iterator();
            if (onlineUsers.hasNext()) {
                while (onlineUsers.hasNext()) {
                    ClientThread userThread = (ClientThread) onlineUsers.next();
                    serverOut.println(userThread.getThreadName());
                }
            } else {
                serverOut.println("<b>Niemand!<b><br>");
            }
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }
    }

    public void run() {
        try {
            String fromClient;
            while ((fromClient = clientIn.readLine()) != null) {
                if (fromClient.equals("exit")) {
                    output(fromClient);
                    // Die Streams schließen
                    clientIn.close();
                    serverOut.close();
                    // delete() Methode aufruf
                    deleteFromList(this);
                    break;
                } else {
                    parent.generateOutput("<b>" + getThreadName() + ":</b> " + fromClient);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Client Thread run: " + ioe.toString());
        }
    }

    public void output(String output) {
        System.out.println("Output an: " + clientSocket.getPort());
        serverOut.println(output);
    }

    /**
     * Removes the closed windows from threadList = java.util.LinkedList
     * 
     * @param ct ClientThread Object
     */
    public void deleteFromList(ClientThread ct) {
        parent.threadList.remove(ct);
    }
}