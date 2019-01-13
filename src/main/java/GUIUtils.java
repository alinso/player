import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUIUtils {
    private GUIUtils() {}

    public static void showTextAreaErrorDialog(Component parentComponent, Object message, String title) {
        // Creates and setups the text area.
        JTextArea textArea = new JTextArea(message.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, textArea.getFont().getSize()));
        textArea.setMargin(new Insets(5, 5, 5, 5));

        // Gets the "preferred size" of text area and limits it to max 600x500.
        // The (eventually) corrected size is NOT applied to the text area but
        // to the JViewport contained into JScrollPane.
        Dimension d = textArea.getPreferredSize();
        d.width = Math.min(d.width, 600);
        d.height = Math.min(d.height, 500);

        // Now line wrapping can be set (it influences the preferred size!).
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Creates and setups the scroll pane.
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setPreferredSize(d);

        // Shows the message dialog.
        JOptionPane.showMessageDialog(parentComponent, scrollPane, title, JOptionPane.ERROR_MESSAGE);
    }
}