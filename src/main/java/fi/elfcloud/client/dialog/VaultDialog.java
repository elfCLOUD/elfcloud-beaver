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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;

public class VaultDialog extends BeaverDialog implements ActionListener {
	private static final long serialVersionUID = -6852338647941744668L;
	private JTextField name;
	private JComboBox type;
	private JButton addButton;
	int returnValue = JOptionPane.CANCEL_OPTION;
	public VaultDialog(BeaverGUI gui) {
		super();
		setTitle(Messages.getString("VaultDialog.window_title")); //$NON-NLS-1$
		String[] allowedTypes = BeaverGUI.getClient().getAllowedTypes();
		List<String> list = new ArrayList<String>(Arrays.asList(allowedTypes));
		allowedTypes = list.toArray(new String[list.size()]);
		JLabel lblName;
		JLabel lblType;
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		lblName = new JLabel(Messages.getString("VaultDialog.label_name")); //$NON-NLS-1$
		cs.insets = new Insets(0, 0, 5, 0);
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;
		panel.add(lblName, cs);

		name = new JTextField();
		name.addKeyListener(new java.awt.event.KeyAdapter() {  
            public void keyReleased(java.awt.event.KeyEvent evt) {  
                if (name.getText().trim().length() > 0) {  
                	addButton.setEnabled(true);  
                } else {  
                	addButton.setEnabled(false);  
                }  
            }  
        });    
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		name.requestFocusInWindow();
		panel.add(name, cs);

		lblType = new JLabel(Messages.getString("VaultDialog.label_type")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		panel.add(lblType, cs);

		type = new JComboBox(allowedTypes);
		if (allowedTypes.length > 0) {
			type.setSelectedIndex(0);
		}
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(type, cs);
		
		
		addButton = new JButton(Messages.getString("VaultDialog.button_add")); //$NON-NLS-1$
		addButton.setEnabled(false);
		addButton.setActionCommand(Integer.toString(JOptionPane.OK_OPTION));
		addButton.addActionListener(this);

		JButton cancelButton = new JButton(Messages.getString("VaultDialog.button_cancel")); //$NON-NLS-1$
		cancelButton.setActionCommand(Integer.toString(JOptionPane.CANCEL_OPTION));
		cancelButton.addActionListener(this);
		JPanel bp = new JPanel();
		bp.add(addButton);
		bp.add(cancelButton);
		
		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 2;
		panel.add(bp, cs);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setDefaultCloseOperation(JOptionPane.CANCEL_OPTION);
	}
	
	public String getVaultType() {
		return type.getSelectedItem().toString();
	}
	
	public int showDialog() {
		name.grabFocus();
		setResizable(false);
		setModal(true);
		setContentPane(panel);
		pack();
		setLocationByPlatform(true);
		setVisible(true);
		return returnValue;
	}

	public String getClusterName() {
		return name.getText().trim();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int action = Integer.parseInt(e.getActionCommand());
		if (name.getText().trim().length() > 0) {
			returnValue = action;
			dispose();
		} else if (action != JOptionPane.OK_OPTION) {
			returnValue = action;
			dispose();
		} else {
			JOptionPane.showMessageDialog(null, Messages.getString("VaultDialog.error_empty_name"), Messages.getString("VaultDialog.error_empty_name_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
