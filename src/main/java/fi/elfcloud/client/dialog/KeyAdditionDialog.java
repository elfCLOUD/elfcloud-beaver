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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.KeyGenerator;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.XMLKeyItem;
import fi.elfcloud.client.preferences.KeyPreference;
import fi.elfcloud.sci.Client;
import fi.elfcloud.sci.Utils;
import fi.elfcloud.sci.exception.ECEncryptionException;

public class KeyAdditionDialog extends JDialog {
	private static final long serialVersionUID = 7251806453536160081L;
	private JTextField tfLocation;
	private JTextField tfDescription;
	private JCheckBox copyFile;
	private ButtonGroup group;
	private int keyLength = 0;
	private final int ENC_AES_128 = 1;
	private final int ENC_AES_192 = 2;
	private final int ENC_AES_256 = 3;
	private KeyPreference preference;

	public KeyAdditionDialog(KeyPreference preference, boolean isNew) {
		super();
		this.preference = preference;
		setLayout(new BorderLayout());
		if (isNew) {
			setTitle(BeaverGUI.titlePrefix + Messages.getString("KeyAdditionDialog.key_generation_window_title")); //$NON-NLS-1$
		} else {
			setTitle(BeaverGUI.titlePrefix + Messages.getString("KeyAdditionDialog.key_load_window_title")); //$NON-NLS-1$
		}
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel lblFileLocation = new JLabel(Messages.getString("KeyAdditionDialog.label_file")); //$NON-NLS-1$
		JLabel lblDescription = new JLabel(Messages.getString("KeyAdditionDialog.label_description")); //$NON-NLS-1$

		tfLocation = new JTextField(20);
		if (isNew) {
			int i = 1;
			File file = null;
			while (true) {
				file = new File(BeaverGUI.getKeyDirectoryPath(), "elfcloudkey" + i + ".key"); //$NON-NLS-1$ //$NON-NLS-2$
				if (!file.exists()) {
					break;
				}
				i++;
			}
			tfLocation.setText(file.getAbsolutePath());
		}
		tfDescription = new JTextField(20);

		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;

		cs.insets = new Insets(0, 0, 5, 0);
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;
		panel.add(lblFileLocation, cs);

		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 1;
		panel.add(tfLocation, cs);

		JButton button;
		if (isNew) {
			button = new JButton(new BrowseAction());
		} else {
			button = new JButton(new OpenExistingAction());
		}
		
		cs.gridx = 2;
		panel.add(button, cs);

		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		panel.add(lblDescription, cs);

		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(tfDescription, cs);

		if (isNew) {
			cs.gridx = 0;
			cs.gridy = 2;
			cs.gridwidth = 1;
			JLabel lblExtra = new JLabel(Messages.getString("KeyAdditionDialog.label_encryption_mode")); //$NON-NLS-1$
			panel.add(lblExtra, cs);

			JPanel radioPanel = new JPanel(new GridLayout(0, 1));

			JRadioButton aes128 = new JRadioButton(Messages.getString("KeyAdditionDialog.choice_aes128")); //$NON-NLS-1$
			aes128.setActionCommand(Integer.toString(ENC_AES_128));
			aes128.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					KeyAdditionDialog.this.keyLength = 128;						
				}
			});
			JRadioButton aes192 = new JRadioButton(Messages.getString("KeyAdditionDialog.choice_aes192")); //$NON-NLS-1$
			aes192.setActionCommand(Integer.toString(ENC_AES_192));
			aes192.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					KeyAdditionDialog.this.keyLength = 192;						
				}
			});

			JRadioButton aes256 = new JRadioButton(Messages.getString("KeyAdditionDialog.choice_aes256")); //$NON-NLS-1$
			aes256.setActionCommand(Integer.toString(ENC_AES_256));
			aes256.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					KeyAdditionDialog.this.keyLength = 256;						
				}
			});
			aes256.doClick();

			group = new ButtonGroup();
			group.add(aes128);
			group.add(aes192);
			group.add(aes256);
			radioPanel.add(aes128);
			radioPanel.add(aes192);
			radioPanel.add(aes256);

			cs.gridx = 1;
			cs.gridwidth = 2;
			panel.add(radioPanel, cs);
		} else {
			copyFile = new JCheckBox(Messages.getString("KeyAdditionDialog.checkbox_copy_file")); //$NON-NLS-1$
			copyFile.setSelected(true);
			cs.gridx = 1;
			cs.gridy = 2;
			cs.gridwidth = 1;
			panel.add(copyFile, cs);
		}

		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel, BorderLayout.CENTER);

		JButton addButton;
		if (isNew){
			addButton = new JButton(new GenerateAction());
		} else {
			addButton = new JButton(new ImportAction());
		}

		JButton cancelButton = new JButton(Messages.getString("KeyAdditionDialog.button_cancel")); //$NON-NLS-1$
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		JPanel bp = new JPanel();
		bp.add(addButton, BorderLayout.EAST);
		bp.add(cancelButton);
		add(bp, BorderLayout.SOUTH);
		setModal(true);
		pack();
	}

	public String getPath() {
		return tfLocation.getText();
	}

	public String getDescription() {
		return tfDescription.getText();
	}

	public int getKeyLength() {
		return this.keyLength;
	}

	public boolean allowCopy() {
		return copyFile.isSelected();
	}

	private int confirmDialog(File selectedFile) {
		Object[] options = {Messages.getString("KeyAdditionDialog.confirm_yes"), Messages.getString("KeyAdditionDialog.confirm_no")}; //$NON-NLS-1$ //$NON-NLS-2$
		JLabel message = new JLabel(Messages.getString("KeyAdditionDialog.confirm_message_1") + selectedFile.getPath() + Messages.getString("KeyAdditionDialog.confirm_message_2")); //$NON-NLS-1$ //$NON-NLS-2$
		return JOptionPane.showOptionDialog(this, message, 
				Messages.getString("KeyAdditionDialog.confirm_title"),  //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE, 
				null, options, options[1]);
	}

	/**
	 * Generates new encryption key and saves it to a file.
	 * @param path location where the new key should be written
	 * @param description description of the key
	 * @param keyLength length of the key
	 * @throws FileNotFoundException, IOException 
	 */
	static XMLKeyItem generateKey(File file, String description, int keyLength) throws FileNotFoundException, IOException {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("AES"); //$NON-NLS-1$
			keyGen.init(128); // IV is 16 bytes long
			Key iv = keyGen.generateKey();
			keyGen.init(keyLength);
			Key encKey = keyGen.generateKey();
			FileOutputStream fo = new FileOutputStream(file);
			fo.write(iv.getEncoded());
			fo.write(encKey.getEncoded());
			fo.close();
			return addKey(iv.getEncoded(), encKey.getEncoded(), file.getPath(), description, keyLength);
		} catch (NoSuchAlgorithmException e) {
		
		}
		return null;
	}

	private XMLKeyItem loadKey(File sourceFile, File destinationFile, String description) {
		byte[] iv = new byte[0];
		byte[] key = new byte[0];
		HashMap<String, byte[]> keyMap = null;
		XMLKeyItem xmlKey = null;
		FileOutputStream fo = null;

		try {
			keyMap = BeaverGUI.loadKeyFromFile(sourceFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (ECEncryptionException e) {
			JOptionPane.showMessageDialog(this, "Selected file is invalid.", "Invalid encryption key", JOptionPane.INFORMATION_MESSAGE);
		}
		if (keyMap == null) {
			return null;
		}
		
		iv = keyMap.get("iv"); //$NON-NLS-1$
		key = keyMap.get("key"); //$NON-NLS-1$
		
		int keyLength = 0;
		switch (key.length) {
			case 16:
				keyLength = 128;
				break;
			case 24:
				keyLength = 192;
				break;
			case 32:
				keyLength = 256;
				break;
			default:
				JOptionPane.showMessageDialog(this, "Selected file is invalid.", "Invalid encryption key", JOptionPane.INFORMATION_MESSAGE);
				return null;
		}
		try {
			xmlKey = addKey(iv, key, destinationFile.getPath(), description, keyLength);
			if (xmlKey != null) {
				fo = new FileOutputStream(destinationFile);
				fo.write(iv);
				fo.write(key);
			}
			return xmlKey;
		} catch (NullPointerException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (fo != null) {
				try {
					fo.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}	

	/**
	 * Adds new key to {@link Client} and {@link #tableModel}.
	 * @param iv initialization vector of the key
	 * @param key encryption key
	 * @param path path to the key file
	 * @param description description of the key
	 * @param keyLength length of the key
	 */
	private static XMLKeyItem addKey(byte[] iv, byte[] key, String path, String description, int keyLength) {
		XMLKeyItem xmlKey = new XMLKeyItem(path, description, Utils.calculateKeyHash(iv, key), keyLength);
		ArrayList<XMLKeyItem> keyList = BeaverGUI.getKeyList();
		try {
			BeaverGUI.getClient().addEncryptionKey(iv, key);
		} catch (ECEncryptionException e) {
			JOptionPane.showMessageDialog(null, "Key is already added to list", "Key already in list", JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		keyList.add(xmlKey);
		BeaverGUI.writeXML(keyList);
		return xmlKey;
	}
	
	private class GenerateAction extends AbstractAction {
		private static final long serialVersionUID = -7300936925158439807L;

		private GenerateAction() {
			super(Messages.getString("KeyAdditionDialog.button_generate")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			File keyFile = new File(getPath());
			keyFile = checkFileSelection(keyFile);
			XMLKeyItem key;
			if (keyFile != null) {
				try {
					key = generateKey(keyFile, getDescription(), getKeyLength());
				} catch (FileNotFoundException e) {
					key = null;
				} catch (IOException e) {
					key = null;
				}
				if (key != null) {
					preference.addKeyRow(key);
					dispose();
				}
			}
		}
	}

	private class ImportAction extends AbstractAction {
		private static final long serialVersionUID = 5685087874872682001L;

		private ImportAction() {
			super(Messages.getString("KeyAdditionDialog.button_import")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File keyFile = new File(getPath());
			File destinationFile = null;
			if (allowCopy() && keyFile.exists() && !keyFile.isDirectory()) {
				destinationFile = new File(BeaverGUI.getKeyDirectoryPath(), keyFile.getName());
			}
			destinationFile = checkFileSelection(destinationFile);
			XMLKeyItem key = loadKey(keyFile, destinationFile, getDescription());
			if (key != null) {
				preference.addKeyRow(key);
				dispose();
			}
		}
	}
	
	private File checkFileSelection(File file) {
		int dialogResult;
		if (file.getAbsoluteFile().equals(new File(BeaverGUI.getKeyDirectoryPath(), "keylist.xml"))) { //$NON-NLS-1$
			JOptionPane.showMessageDialog(null, Messages.getString("KeyAdditionDialog.key_copy_failed_invalid_name"), Messages.getString("KeyAdditionDialog.key_copy_failed_invalid_name_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		if (file.exists() && ! file.isDirectory()) {
			dialogResult = confirmDialog(file);
			if (dialogResult == JOptionPane.YES_OPTION) {
				return file;
			} else {
				return null;
			}
		}
		return file;
	}
	
	private class OpenExistingAction extends AbstractAction {
		private static final long serialVersionUID = -5794776046629404403L;

		private OpenExistingAction() {
			super(Messages.getString("KeyAdditionDialog.button_browse")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.home")); //$NON-NLS-1$
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JOptionPane.OK_OPTION) {
				tfLocation.setText(fc.getSelectedFile().getAbsolutePath());
			}	
		}
	}

	private class BrowseAction extends AbstractAction {
		private static final long serialVersionUID = 6131277273133422583L;

		private BrowseAction() {
			super(Messages.getString("KeyAdditionDialog.button_browse")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(BeaverGUI.getKeyDirectoryPath());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JOptionPane.OK_OPTION) {
				tfLocation.setText(fc.getSelectedFile().getAbsolutePath());
			}	
		}
	}
}