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

package fi.elfcloud.client.preferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.HTMLEscapedJLabel;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.XMLKeyItem;
import fi.elfcloud.client.dialog.KeyAdditionDialog;
import fi.elfcloud.sci.Client.ENC;

public class KeyPreference extends Preference {
	private ArrayList<XMLKeyItem> keys;
	private KeyPanelItem activeKey;
	private KeyPanelItem selectedKey;
	private KeyPanelItem mouseOverKey;
	private JPanel keyPanel;
	private JScrollPane keyScrollPanel;
	private JButton importButton = new JButton(new ImportAction());
	private JButton generateButton = new JButton(new GenerateAction());
	private JButton deleteButton = new JButton(new DeleteAction());

	public KeyPreference() {
		super();
		keys = BeaverGUI.getKeyList();
		preferenceLabel = new PreferenceLabel(new ImageIcon(BeaverGUI.class.getResource("icons/key32.png")), this); //$NON-NLS-1$
		preferenceLabel.setPreferredSize(new Dimension(70, 70));
		preferenceLabel.setBorder(null);
		preferenceLabel.setOpaque(true);
		preferenceLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		preferenceLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		preferenceLabel.setBackground(Color.WHITE);
		preferenceLabel.setText(Messages.getString("KeyPreference.option_keys")); //$NON-NLS-1$
		initPrefPanel();
	}
	private void initPrefPanel() {
		preferencePanel = new JPanel();
		preferencePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		preferencePanel.setLayout(new BoxLayout(preferencePanel, BoxLayout.PAGE_AXIS));
		preferencePanel.add(createKeyPanel());
		preferencePanel.add(Box.createVerticalStrut(5));
		deleteButton.setEnabled(false);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(generateButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(importButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(deleteButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, generateButton.getPreferredSize().height));
		preferencePanel.add(buttonPanel);
	}
	private JScrollPane createKeyPanel() {
		keyPanel = new JPanel();
		keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.PAGE_AXIS));
		keyPanel.setBackground(Color.WHITE);
		keyPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.WHITE));
		keyScrollPanel = new JScrollPane(keyPanel);
		keyScrollPanel.getViewport().setBackground(Color.WHITE);
		keyScrollPanel.setPreferredSize(new Dimension(300, 300));
		keyScrollPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SystemColor.controlShadow));
		if (!BeaverGUI.osName.startsWith("mac os x")) { //$NON-NLS-1$
			keyScrollPanel.getVerticalScrollBar().setUnitIncrement(16);
		}

		for (int i=0; i<keys.size(); i++) {
			XMLKeyItem key = keys.get(i);
			KeyPanelItem keyElement = new KeyPanelItem(key);
			keyPanel.add(keyElement);
			if (key.getKeyHash().equals(BeaverGUI.getPreferences().get("selected.key", ""))) { //$NON-NLS-1$ //$NON-NLS-2$
				activeKey = keyElement;
				keyElement.setCurrent(true);
			}
		}
		return keyScrollPanel;
	}
	
	
	@Override
	public void save() {
	}

	@Override
	public void load() {
	}
	
	public void addKeyRow(XMLKeyItem key) {
		KeyPanelItem keyElement = new KeyPanelItem(key);
		for (Component component: keyPanel.getComponents()) {
			KeyPanelItem componentKeyElement = (KeyPanelItem)component;
			if (componentKeyElement.getKey().getPath().equals(keyElement.getKey().getPath())) {
				deleteKeyPanelItem(componentKeyElement);
				deleteKeyByHash(componentKeyElement.getKey().getKeyHash());
			}
		}
		keyPanel.add(keyElement);
		keyPanel.validate();
		keyPanel.repaint();
		keyScrollPanel.validate();
		keyScrollPanel.repaint();
	}
	
	private void deleteSelectedKeyRow() {
		try {
			XMLKeyItem deletedKey = deleteKeyByHash(selectedKey.getKey().getKeyHash());
			deleteKeyPanelItem(selectedKey);
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(new JLabel(Messages.getString("KeyPreference.key_removed_text"))); //$NON-NLS-1$
			panel.add(new JLabel(Messages.getString("KeyPreference.key_removed_text2") + deletedKey.getPath() + Messages.getString("KeyPreference.key_removed_text3"))); //$NON-NLS-1$ //$NON-NLS-2$
			JOptionPane.showMessageDialog(null, panel, Messages.getString("KeyPreference.key_removed_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
			selectedKey = null;
			deleteButton.setEnabled(false);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}
	
	private XMLKeyItem deleteKeyByHash(String keyHash) {
		ArrayList<XMLKeyItem> keyList = BeaverGUI.getKeyList();
		int i = 0;
		for (XMLKeyItem key: keyList) {
			if (key.getKeyHash().equals(keyHash)) {
				keyList.remove(i);
				if (key.getKeyHash().equals(BeaverGUI.getPreferences().get("selected.key", ""))) { //$NON-NLS-1$ //$NON-NLS-2$
					BeaverGUI.getClient().setEncryptionKey(new byte[0]);
					BeaverGUI.getClient().setIV(new byte[0]);
					BeaverGUI.getClient().setEncryptionMode(ENC.NONE);
					BeaverGUI.getPreferences().put("selected.key", ""); //$NON-NLS-1$ //$NON-NLS-2$
					activeKey = null;
				}
				BeaverGUI.getClient().removeEncryptionKey(key.getKeyHash());
				BeaverGUI.writeXML(keyList);
				return key;
			}
			i++;
		}
		return null;
	}
	
	private void deleteKeyPanelItem(KeyPanelItem panel) {
		if (panel.equals(selectedKey)) {
			selectedKey = null;
		}
		keyPanel.remove(panel);
		keyPanel.validate();
		keyPanel.repaint();
		keyScrollPanel.validate();
		keyScrollPanel.repaint();
	}

	private class KeyDescription extends JPanel {
		private static final long serialVersionUID = 1700391207769014871L;
		private JLabel description;
		private JLabel smallDescription;
		private JLabel encryptionMode;

		private KeyDescription(KeyPanelItem key) {
			super();
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.description = new HTMLEscapedJLabel(key.getKey().getDescription());
			this.description.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			Font f = new Font("Large", Font.BOLD, 16); //$NON-NLS-1$
			this.description.setFont(f);
			this.smallDescription = new JLabel("<html>" + key.getKey().getKeyHash() + "<br>" + key.getKey().getPath()); //$NON-NLS-1$ //$NON-NLS-2$
			f = new Font("small", Font.PLAIN, 12); //$NON-NLS-1$
			this.smallDescription.setFont(f);
			this.encryptionMode = new JLabel(key.getKey().getEncryptionMode());
			f = new Font("small", Font.PLAIN, 10); //$NON-NLS-1$
			this.encryptionMode.setFont(f);
			this.encryptionMode.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
			this.smallDescription.setBackground(Color.WHITE);
			this.encryptionMode.setBackground(Color.WHITE);
			this.description.setBackground(Color.WHITE);
			setBackground(Color.WHITE);
			this.smallDescription.setOpaque(false);
			this.encryptionMode.setOpaque(false);
			this.description.setOpaque(false);
			setOpaque(false);
			add(this.description);
			add(this.smallDescription);
			add(this.encryptionMode);
		}
	}

	private class KeyPanelItem extends JPanel implements MouseListener {
		private static final long serialVersionUID = 7480726755825072567L;
		private Color selectedColor = new Color(0xc1d2ee);
		private Color hoverColor = new Color(0xe0e8f6);
		private XMLKeyItem key;
		private KeyDescription descriptionPanel;
		private JPanel buttonPanel;
		private JButton addButton;

		private KeyPanelItem(XMLKeyItem key) {
			this.key = key;
			initComponent();
			addMouseListener(this);
		}

		private void initComponent() {
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(120, 90));
			setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			add(Box.createHorizontalStrut(5));
			descriptionPanel = new KeyDescription(this);
			add(descriptionPanel);
			add(Box.createHorizontalGlue());
			buttonPanel = new JPanel();
			buttonPanel.setOpaque(false);
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
			addButton = new JButton(Messages.getString("KeyPreference.button_activate_key")); //$NON-NLS-1$
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (activeKey != null) {
						activeKey.setCurrent(false);
					}
					activeKey = KeyPanelItem.this;
					activeKey.setCurrent(true);
					activeKey.setSelected();
				}
			});
			buttonPanel.add(addButton);
			add(buttonPanel);
			addBorder();
		}
		
		private void addBorder() {
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SystemColor.controlShadow));
		}
		private void setCurrent(boolean selected) {
			if (selected) {
				BeaverGUI.getPreferences().put("selected.key", activeKey.getKey().getKeyHash()); //$NON-NLS-1$
				buttonPanel.remove(addButton);
				JLabel imageLabel = new JLabel();
				imageLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
				imageLabel.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/check32.png"))); //$NON-NLS-1$
				imageLabel.setToolTipText(Messages.getString("KeyPreference.tick_tooltip_active_key")); //$NON-NLS-1$
				buttonPanel.add(imageLabel);
			} else {
				removeAll();
				initComponent();
			}
			revalidate();
		}

		private void setSelected() {
			if (selectedKey != null) {
				selectedKey.setBackground(Color.WHITE);
				deleteButton.setEnabled(true);
			}
			selectedKey = this;
			selectedKey.setBackground(selectedColor);
		}

		public XMLKeyItem getKey() {
			return this.key;
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			setSelected();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseOverKey = this;
			if (selectedKey != this) {
				setBackground(hoverColor);
			}
			e.consume();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (selectedKey == this) {
				setBackground(selectedColor);
			} else {
				setBackground(Color.WHITE);
			}
			e.consume();
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource().equals(mouseOverKey)) {
				setSelected();
			}
		}
	}

	private class DeleteAction extends AbstractAction {
		private static final long serialVersionUID = -6247258298883666433L;

		private DeleteAction() {
			super(Messages.getString("KeyPreference.button_delete_key")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.YES_OPTION ==JOptionPane.showConfirmDialog(null, Messages.getString("KeyPreference.confirmation_remove_text"), Messages.getString("KeyPreference.confirmation_remove_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) { //$NON-NLS-1$ //$NON-NLS-2$
				deleteSelectedKeyRow();
			}
		}
	}

	private class ImportAction extends AbstractAction {
		private static final long serialVersionUID = 8092501456675094060L;

		private ImportAction() {
			super(Messages.getString("KeyPreference.button_import_key")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			KeyAdditionDialog dialog = new KeyAdditionDialog(KeyPreference.this, false);
			dialog.setVisible(true);
		}
	}

	private class GenerateAction extends AbstractAction {
		private static final long serialVersionUID = 2093718743324237467L;

		private GenerateAction() {
			super(Messages.getString("KeyPreference.button_generate_key")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			KeyAdditionDialog dialog = new KeyAdditionDialog(KeyPreference.this, true);
			dialog.setVisible(true);
		}
	}

	public void reloadKeys() {
		keys = BeaverGUI.getKeyList();
		initPrefPanel();
	}
	
}
