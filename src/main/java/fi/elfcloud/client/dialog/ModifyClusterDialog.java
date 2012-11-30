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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import fi.elfcloud.client.Messages;

public class ModifyClusterDialog extends BeaverDialog {
	private static final long serialVersionUID = 5558089668105493820L;
	private JTextField name;
	private JLabel lblName;
	private boolean answer = false;
	private JButton saveButton;
	
	public ModifyClusterDialog(JFrame parent, String clusterName) {
		super(parent, true);
		setTitle(Messages.getString("ModifyClusterDialog.window_title") +clusterName); //$NON-NLS-1$
		setLocationRelativeTo(parent);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.insets = new Insets(0, 0, 5, 0);

		lblName = new JLabel(Messages.getString("ModifyClusterDialog.label_name")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 1;
		cs.weightx = 0;
		panel.add(lblName, cs);

		name = new JTextField(getWidth());
		name.addKeyListener(new java.awt.event.KeyAdapter() {  
            public void keyReleased(java.awt.event.KeyEvent evt) {  
                if (name.getText().trim().length() > 0) {  
                	saveButton.setEnabled(true);  
                } else {  
                	saveButton.setEnabled(false);  
                }  
            }  
        });    
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 2;
		cs.weightx = 1.0;
		name.setText(clusterName);
		panel.add(name, cs);
		panel.setBorder(new EmptyBorder(5, 5, 0, 5));
		saveButton = new JButton(Messages.getString("ModifyClusterDialog.button_save")); //$NON-NLS-1$
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (name.getText().trim().length() > 0) {
					answer = true;
					setVisible(false);
				} else {
					JOptionPane.showMessageDialog(null, Messages.getString("ModifyClusterDialog.error_no_name"), Messages.getString("ModifyClusterDialog.error_no_name_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});
		JButton cancelButton = new JButton(Messages.getString("ModifyClusterDialog.button_cancel")); //$NON-NLS-1$
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				answer = false;
				setVisible(false);
			}
		});
		JPanel bp = new JPanel();
		bp.add(saveButton);
		bp.add(cancelButton);
		getContentPane().add(panel, BorderLayout.NORTH);
		getContentPane().add(bp, BorderLayout.PAGE_END);
		
		setMinimumSize(new Dimension(320, 100));
		pack();
	}
	
	@Override
	public int showDialog() {
		setVisible(true);
		if (answer) {
			return JOptionPane.OK_OPTION;
		}
		return JOptionPane.CANCEL_OPTION;
	}
	
	public String getName() {
		return name.getText().trim();
	}
}

