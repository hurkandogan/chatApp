import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.security.Key;
import java.io.*;

public class ChatClientFrame extends JFrame {

    String host, username;
    int port;
    ClientInputThread inputThread;
    Socket clientSocket;
    BufferedReader serverIn;
    PrintWriter serverOut;
    private JTextField jtfEingabe;
    private JEditorPane editorPane;
    private Action actConnect, actDisconnect, actClose, actInfo, actHelp, actStop, actImport, actExport;
    private KeyStroke keyStroke;

    // Tooltip constants
    private final String TTT_CONNECT = "Connect to Server";
    private final String TTT_DISCONNECT = "Disconnect from Server";
    private final String TTT_CLOSE = "Disconnect and Close the Window";
    private final String TTT_HELP = "Help";
    private final String TTT_INFO = "Info";

    public ChatClientFrame(String host, int port, String username) {
        super("Chat-Client-Frame");

        this.host = host;
        this.port = port;
        this.username = username;

        this.setSize(500, 500);
        /**
         * Instead of this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); I used a
         * WindowListener. So this way I can call the disconnectServer() method when
         * user closes the window.
         */
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (clientSocket != null)
                    disconnectServer();
                dispose();
                System.exit(0);
            }
        });
        Container contentPane = this.getContentPane();

        createMenu();
        getJToolBar();

        jtfEingabe = new JTextField();
        jtfEingabe.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                // bei Enter Text übermitteln
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = jtfEingabe.getText().trim();
                    if (text.length() > 0) {
                        if (serverOut != null) {
                            serverOut.println(text);
                            jtfEingabe.setText("");
                        } else {
                            jtfEingabe.setText("Zunächst Verbindung zu einem Server herstellen");
                        }
                    } else {
                        jtfEingabe.setText("Bitte Text eingeben!");
                    }
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.SOUTH, jtfEingabe);
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        JScrollPane editorSP = new JScrollPane(editorPane);
        panel.add(BorderLayout.CENTER, editorSP);
        contentPane.add(BorderLayout.CENTER, panel);
        contentPane.add(BorderLayout.NORTH, this.getJToolBar());

        this.setTitle(username);
        // layeredPane = getLayeredPane();
        this.setVisible(true);
    }// End of the Constructor

    private void createMenu() {

        // Menu construction
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        JMenu menuOperationen = new JMenu("Operationen");
        menuBar.add(menuOperationen);
        JMenu menuHelp = new JMenu("Help");
        menuBar.add(menuHelp);

        JMenuItem jmiConnect = new JMenuItem("Connect");
        actConnect = new AbstractAction("Connect", new ImageIcon("./icons/connect.png")) {
            public void actionPerformed(ActionEvent e) {
                connectServer();
            }
        };
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK);
        actConnect.putValue(Action.ACCELERATOR_KEY, keyStroke);
        actConnect.putValue(Action.SHORT_DESCRIPTION, TTT_CONNECT);
        jmiConnect = menuOperationen.add(actConnect);

        JMenuItem jmiDisconnect = new JMenuItem("Disconnect");
        actDisconnect = new AbstractAction("Disconnect", new ImageIcon("./icons/disconnect.png")) {
            public void actionPerformed(ActionEvent e) {
                if (clientSocket != null) {
                    disconnectServer();
                }
            }
        };
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
        actDisconnect.putValue(Action.ACCELERATOR_KEY, keyStroke);
        actDisconnect.putValue(Action.SHORT_DESCRIPTION, TTT_DISCONNECT);
        jmiDisconnect = menuOperationen.add(actDisconnect);

        JMenuItem jmiClose = new JMenuItem("Close");
        actClose = new AbstractAction("Close", new ImageIcon("./icons/close.png")) {
            public void actionPerformed(ActionEvent e) {
                if (clientSocket != null) {
                    disconnectServer();
                }
                System.exit(0);
            }
        };
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK);
        actClose.putValue(Action.ACCELERATOR_KEY, keyStroke);
        actClose.putValue(Action.SHORT_DESCRIPTION, TTT_CLOSE);
        jmiClose = menuOperationen.add(actClose);

        JMenuItem jmiHelp = new JMenuItem("Help");
        actHelp = new AbstractAction("Help", new ImageIcon("./icons/help.png")) {
            public void actionPerformed(ActionEvent e) {
                jtfEingabe.setText("I'm not helpful :)");
            }
        };
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        actHelp.putValue(Action.ACCELERATOR_KEY, keyStroke);
        actHelp.putValue(Action.SHORT_DESCRIPTION, TTT_HELP);
        jmiHelp = menuHelp.add(actHelp);

        JMenuItem jmiInfo = new JMenuItem("Info");
        actInfo = new AbstractAction("Info", new ImageIcon("./icons/info.png")) {
            public void actionPerformed(ActionEvent e) {
                jtfEingabe.setText("JAV06-XX1-N01 Einsendeaufgabe - Hurkan Dogan");
            }
        };
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK);
        actInfo.putValue(Action.ACCELERATOR_KEY, keyStroke);
        actInfo.putValue(Action.SHORT_DESCRIPTION, TTT_INFO);
        jmiInfo = menuHelp.add(actInfo);
    }

    private JToolBar getJToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.add(actClose);
        toolBar.add(actConnect);
        toolBar.add(actDisconnect);
        toolBar.addSeparator();
        toolBar.add(actHelp);
        toolBar.add(actInfo);
        return toolBar;
    }

    private void connectServer() {
        try {
            clientSocket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverOut = new PrintWriter(clientSocket.getOutputStream(), true);
            // Benutzername an Server:
            serverOut.println(username);
            inputThread = new ClientInputThread(editorPane, serverIn);
            inputThread.start();
            jtfEingabe.setText("");
        } catch (UnknownHostException uhe) {
            System.out.println("***" + uhe.toString());
            System.exit(-1);
        } catch (IOException ioe) {
            System.out.println("***" + ioe.toString());
            System.exit(-1);
        }
    }

    private void disconnectServer() {
        serverOut.println("exit");
        inputThread = null;
        try {
            serverIn.close();
            serverOut.close();
            clientSocket.close();
            jtfEingabe.setText("Server connection disconnected.");
        } catch (IOException ioe) {
            System.out.println("*** " + ioe.toString());
        }
    }

    public static void main(String[] args) {
        // ChatClientFrame parameter values
        String host, username;
        int port;
        if (args.length == 3) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            username = args[2];
            new ChatClientFrame(host, port, username);
        } else {
            System.out.println("You should enter a valid Host IP, port number and username.");
            System.exit(0);
        }
    }
}