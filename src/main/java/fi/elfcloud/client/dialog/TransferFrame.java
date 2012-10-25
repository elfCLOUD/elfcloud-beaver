package fi.elfcloud.client.dialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Abstract superclass for transfer dialogs
 *
 */
public abstract class TransferFrame extends JFrame {
	private static final long serialVersionUID = 7002696194952153516L;
	protected static final String CANCEL_ACTION = "CANCEL";
	JTextArea taskOutput;
    JProgressBar progressBar;
    JButton cancel;
    
	public TransferFrame() {
		super();
		JPanel panel = new JPanel(new BorderLayout());
		progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        
        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setLineWrap(true);
        
        cancel = new JButton("Cancel");
        cancel.setActionCommand(CANCEL_ACTION);
        setContentPane(panel);
        panel.add(progressBar, BorderLayout.PAGE_START);
        panel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        panel.add(cancel, BorderLayout.PAGE_END);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(400, 300));
        pack();
	}
}
