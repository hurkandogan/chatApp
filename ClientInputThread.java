import java.awt.*;
import java.io.*;
import javax.swing.*;

public class ClientInputThread extends Thread {
    private StringBuffer editorString;
    private BufferedReader serverIn;
    private JEditorPane editorPane;

    public ClientInputThread(JEditorPane editorPane, BufferedReader serverIn) {
        this.editorPane = editorPane;
        this.serverIn = serverIn;
    }

    public void run() {
        synchronized (editorPane) {
            editorPane.setText("<b>Beendigung des Chats " + "über den Button \"Disconnect\" </b>");
            String fromServer, fromEditor;
            int beginIndex, endIndex;
            Rectangle rect;
            try { // Readline kann exception werfen
                while ((fromServer = serverIn.readLine()) != null) {
                    if (fromServer.equals("exit")) {
                        break;
                    }
                    fromEditor = editorPane.getText();
                    beginIndex = fromEditor.indexOf("<body>") + 6;
                    endIndex = fromEditor.indexOf("</body>");
                    editorString = new StringBuffer(fromEditor.substring(beginIndex, endIndex));
                    editorString.append("<br>" + fromServer);
                    editorPane.setText(editorString.toString());
                    // an die untere Kante scrollen
                    rect = new Rectangle(new Point(0, editorPane.getSize().height));
                    editorPane.scrollRectToVisible(rect);
                }
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            }
        }
    }
}