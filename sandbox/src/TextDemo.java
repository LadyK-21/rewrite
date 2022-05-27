
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.Java11Parser;
import org.openrewrite.java.tree.J;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class TextDemo extends JPanel {

    static String source =
            "class C {\n" +
            "   void m() {\n" +
            "       int x = 0, y = 1;\n" +
            "       for(int i=0, j=2*i+1; i<x; i++, j++) {\n" +
            "           f(x); g(x); h(x);\n" +
            "       }\n" +
            "       a();" +
            "       if(x == y+1) { u(x); } else { v(x); }\n" +
            "       b();" +
            "       //while(x == 0) { w(x); }\n" +
            "       o.m(1+x++, 2+(y++ *3));" +
            "       x = 1+(2*y);" +
            "   }\n" +
            "}\n"
            ;

    static J.CompilationUnit ast;

    protected JTextField textField;
    protected JTextArea textArea;
    private final static String newline = "\n";

    public TextDemo() {
        super(new GridBagLayout());

         textField = new JTextField(20);
//         textField.addActionListener(this);

        textArea = new JTextArea(5, 20);
        textArea.setEditable(false);
        textArea.append(source);
        //textArea.addMouseListener(this);
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;
        add(textField, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);
    }

//    public void actionPerformed(ActionEvent evt) {
//        String text = textField.getText();
//        textArea.append(text + newline);
//        textField.selectAll();
//
//        //Make sure the new text is visible, even if there
//        //was a selection in the text area.
//        textArea.setCaretPosition(textArea.getDocument().getLength());
//    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(new TextDemo());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        Java11Parser parser = new Java11Parser.Builder().build();
        ExecutionContext ctx = new InMemoryExecutionContext();
        List<J.CompilationUnit> cus = parser.parse(ctx, source);
        ast = cus.get(0);

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}