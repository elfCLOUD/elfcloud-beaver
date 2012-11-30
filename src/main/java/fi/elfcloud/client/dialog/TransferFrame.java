/*
 * Copyright 2010-2012 elfCLOUD / elfcloud.fi - SCIS Secure Cloud Infrastructure Services
 *	
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *	
 *			http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	   	Unless required by applicable law or agreed to in writing, software
 *	   	distributed under the License is distributed on an "AS IS" BASIS,
 *	   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	   	See the License for the specific language governing permissions and
 *	   	limitations under the License.
 */

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

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;

/**
 * Abstract superclass for transfer dialogs
 *
 */
public abstract class TransferFrame extends JFrame {
	private static final long serialVersionUID = 7002696194952153516L;
	protected static final String CANCEL_ACTION = "CANCEL"; //$NON-NLS-1$
	protected boolean autoClose = BeaverGUI.getPreferences().getBoolean("elfcloud.transfer.window.autoclose", true);
	protected boolean stopOnError = BeaverGUI.getPreferences().getBoolean("elfcloud.transfer.stop.on.error", false);
	protected JTextArea taskOutput;
	protected JProgressBar progressBar;
	protected JButton cancel;
    
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
        
        cancel = new JButton(Messages.getString("TransferFrame.button_cancel")); //$NON-NLS-1$
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
