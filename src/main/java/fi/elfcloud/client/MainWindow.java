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

package fi.elfcloud.client;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.net.ssl.KeyManager;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import fi.elfcloud.client.dialog.AboutDialog;
import fi.elfcloud.client.dialog.HelpWindow;
import fi.elfcloud.client.preferences.PreferencesWindow;
import fi.elfcloud.sci.Client;
import fi.elfcloud.sci.Client.ENC;

/**
 * Main Window for elfcloud.fi Beaver. 
 *
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = -6463168887784521424L;
	private BeaverGUI gui;
	private JLabel encryptionLabel;
	private ImageIcon encryptionON = new ImageIcon(MainWindow.class.getResource("icons/lock_ok_16x16.png")); //$NON-NLS-1$
	private ImageIcon encryptionOFF = new ImageIcon(MainWindow.class.getResource("icons/lock_error_16x16.png")); //$NON-NLS-1$
	private static String encryptionDisabled = Messages.getString("MainWindow.encryption_disabled_status"); //$NON-NLS-1$
	private JButton uploadButton;
	private JMenuItem uploadMenuItem;
	private JCheckBoxMenuItem cbMenuItem;
	private static final String title = Messages.getString("MainWindow.window_title"); //$NON-NLS-1$

	public MainWindow(BeaverGUI gui) {
		super(BeaverGUI.titlePrefix + title);
		this.gui = gui;
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		pack();
		setLocationByPlatform(true);
	}

	/**
	 * Populates a panel with a button to {@link KeyManager} and label displaying encryption status.
	 * @param statusPanel panel to be populated
	 */
	public void populateStatusPanel(JPanel statusPanel) {
		statusPanel.setLayout(new BorderLayout());
		this.encryptionLabel = new JLabel();
		this.encryptionLabel.setText(encryptionDisabled);
		this.encryptionLabel.setIcon(encryptionOFF);
		statusPanel.add(encryptionLabel);
	}

	/**
	 * Updates {@link #encryptionLabel} to show current encryption status.
	 * @param keyhash hash of the current encryption key
	 * 
	 */
	public void updateEncryptionLabel(String keyhash) {
		if (BeaverGUI.getClient().getEncryptionMode() == Client.ENC.NONE) {
			encryptionLabel.setText(encryptionDisabled);
			encryptionLabel.setIcon(encryptionOFF);
		} else {
			for (XMLKeyItem key: BeaverGUI.getKeyList()) {
				if (key.getKeyHash().equals(keyhash) && key.isAvailable()) {
					File f = new File(key.getPath());
					encryptionLabel.setText(f.getName() + (key.getDescription().length() > 0 ? " (" + key.getDescription() +")" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					encryptionLabel.setIcon(encryptionON);
				}
			}
		}
		encryptionLabel.invalidate();
		encryptionLabel.repaint();
	}

	/**
	 * Updates {@link #cbMenuItem} with current encryption status.
	 * @param status current status. <code>true</code> when encryption is disabled, <code>false</code> when enabled.
	 * 
	 */
	public void updateEncryptionCheckbox(boolean status) {
		cbMenuItem.setSelected(status);
	}

	/**
	 * Populates {@link JToolBar} with buttons.
	 * @param pToolBar 
	 */
	public void populateToolbar(JToolBar pToolBar) {
		JButton button = null;
		button = makeButton("home_top_level.png", gui.ACTION_HOME, Messages.getString("MainWindow.button_home_description"), Messages.getString("MainWindow.button_home_text")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		button = makeButton("go_back.png", gui.ACTION_PREVIOUS, Messages.getString("MainWindow.button_back_description"), Messages.getString("MainWindow.button_back_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		button = makeButton("refresh.png", gui.ACTION_REFRESH, Messages.getString("MainWindow.button_refresh_description"), Messages.getString("MainWindow.button_refresh_text")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		pToolBar.addSeparator();

		button = makeButton("add_folder.png", gui.ACTION_ADD_CONTAINER, Messages.getString("MainWindow.button_create_folder_description"), Messages.getString("MainWindow.button_create_folder_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		this.uploadButton = makeButton("upload.png", gui.ACTION_ADD_ITEM, Messages.getString("MainWindow.button_upload_description"), Messages.getString("MainWindow.button_upload_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.uploadButton.setEnabled(false);
		pToolBar.add(this.uploadButton);

		pToolBar.addSeparator();

		button = makeButton("download.png", gui.ACTION_SAVE_ITEM, Messages.getString("MainWindow.button_download_description"), Messages.getString("MainWindow.button_download_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		button = makeButton("info_file.png", gui.ACTION_INFORMATION, Messages.getString("MainWindow.button_information_description"), Messages.getString("MainWindow.button_information_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		pToolBar.addSeparator();

		button = makeButton("modify_rename.png", gui.ACTION_MODIFY, Messages.getString("MainWindow.button_modify_description"), Messages.getString("MainWindow.button_modify_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		button = makeButton("move_file.png", gui.ACTION_MOVE, Messages.getString("MainWindow.button_move_description"), Messages.getString("MainWindow.button_move_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		button = makeButton("delete.png", gui.ACTION_DELETE, Messages.getString("MainWindow.button_delete_description"), Messages.getString("MainWindow.button_delete_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		pToolBar.addSeparator();

		button = makeButton("key.png", gui.ACTION_MANAGE_KEYS, Messages.getString("MainWindow.button_keymanager_description"), Messages.getString("MainWindow.button_keymanager_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);

		pToolBar.add(Box.createHorizontalGlue());
		button = makeButton("goto_browser.png", gui.ACTION_GOTO, "Go to My elfcloud.fi", "My elfcloud.fi"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		pToolBar.add(button);

		button = makeButton("help.png", gui.ACTION_HELP, Messages.getString("MainWindow.button_help_description"), Messages.getString("MainWindow.button_help_title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pToolBar.add(button);
	}

	/**
	 * Creates new {@link JButton}.
	 * @param imageName name of the icon displayed in this {@link JButton}
	 * @param action action to be bound to this {@link JButton}
	 * @param tooltipText tool tip for this {@link JButton}
	 * @return new {@link JButton}
	 */
	public JButton makeButton(String imageName, int action, String tooltipText, String buttonText) {
		URL imageURL = MainWindow.class.getResource("icons/"+imageName); //$NON-NLS-1$
		JButton button = new JButton();
		button.setActionCommand(Integer.toString(action));
		button.setToolTipText(tooltipText);
		button.setText(buttonText);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(JButton.CENTER);
		button.setIcon(new ImageIcon(imageURL));
		button.setBorderPainted(false);
		button.addActionListener(gui);
		return button;
	}

	/**
	 * Populates {@link JMenuBar} with items.
	 * @param menuBar
	 */
	public void populateMenubar(JMenuBar menuBar) {
		JMenuItem menuItem;
		JMenu fileMenu = new JMenu(Messages.getString("MainWindow.menu_file")); //$NON-NLS-1$
		fileMenu.setMnemonic(KeyEvent.VK_F);
		this.uploadMenuItem = new JMenuItem(Messages.getString("MainWindow.menu_upload")); //$NON-NLS-1$
		addAccelerator(uploadMenuItem, KeyEvent.VK_U);
		this.uploadMenuItem.setActionCommand(Integer.toString(gui.ACTION_ADD_ITEM));
		this.uploadMenuItem.addActionListener(gui);
		this.uploadMenuItem.setEnabled(false);
		this.uploadMenuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/upload16.png"))); //$NON-NLS-1$
		fileMenu.add(this.uploadMenuItem);
		menuItem = new JMenuItem(Messages.getString("MainWindow.menu_folder_add")); //$NON-NLS-1$
		addAccelerator(menuItem, KeyEvent.VK_C);
		menuItem.setActionCommand(Integer.toString(gui.ACTION_ADD_CONTAINER));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/add_folder16.png"))); //$NON-NLS-1$
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
		menuItem = new JMenuItem(Messages.getString("MainWindow.menu_download")); //$NON-NLS-1$
		addAccelerator(menuItem, KeyEvent.VK_D);
		menuItem.setActionCommand(Integer.toString(gui.ACTION_SAVE_ITEM));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/download16.png"))); //$NON-NLS-1$
		fileMenu.add(menuItem);
		menuItem = new JMenuItem(Messages.getString("MainWindow.menu_preferences")); //$NON-NLS-1$
		addAccelerator(menuItem, KeyEvent.VK_COMMA);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unused")
				PreferencesWindow prefWindow = PreferencesWindow.getInstance();
			}
		});
		fileMenu.add(menuItem);
		if (!BeaverGUI.osName.startsWith("mac os x")) { //$NON-NLS-1$
			fileMenu.addSeparator();
			menuItem = new JMenuItem(Messages.getString("MainWindow.menu_exit")); //$NON-NLS-1$
			menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/exit_16x16.png"))); //$NON-NLS-1$
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			fileMenu.add(menuItem);
		}
		JMenu encryptionMenu = new JMenu(Messages.getString("MainWindow.menu_encryption")); //$NON-NLS-1$
		encryptionMenu.setMnemonic(KeyEvent.VK_E);
		menuItem = new JMenuItem(Messages.getString("MainWindow.menu_keymanager")); //$NON-NLS-1$
		menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/key16.png"))); //$NON-NLS-1$
		menuItem.setActionCommand(Integer.toString(gui.ACTION_MANAGE_KEYS));
		menuItem.setMnemonic(KeyEvent.VK_M);
		menuItem.addActionListener(gui);
		encryptionMenu.add(menuItem);
		this.cbMenuItem = new JCheckBoxMenuItem(Messages.getString("MainWindow.menu_disable_encryption"), false); //$NON-NLS-1$
		this.cbMenuItem.setMnemonic(KeyEvent.VK_D);
		this.cbMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (cbMenuItem.isSelected()) {
					BeaverGUI.getClient().setEncryptionMode(ENC.NONE);
					updateEncryptionLabel(""); //$NON-NLS-1$
				} else {
					BeaverGUI.getClient().setEncryptionMode(ENC.AES_256);
					updateEncryptionLabel(BeaverGUI.getPreferences().get("selected.key", "")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});
		encryptionMenu.add(cbMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(encryptionMenu);

		if (!BeaverGUI.osName.startsWith("mac os x")) { //$NON-NLS-1$
			JMenu helpMenu = new JMenu(Messages.getString("MainWindow.menu_help")); //$NON-NLS-1$
			helpMenu.setMnemonic(KeyEvent.VK_H);
			menuItem = new JMenuItem(Messages.getString("MainWindow.menu_about")); //$NON-NLS-1$
			menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/information_16x16.png"))); //$NON-NLS-1$
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AboutDialog dialog = new AboutDialog((JFrame)getParent());
					dialog.setVisible(true);
				}
			});
			helpMenu.add(menuItem);

			menuItem = new JMenuItem(Messages.getString("MainWindow.menuitem_help")); //$NON-NLS-1$
			menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("icons/help16.png"))); //$NON-NLS-1$
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					@SuppressWarnings("unused")
					HelpWindow help = HelpWindow.getInstance();
				}
			});
			helpMenu.add(menuItem);
			menuBar.add(helpMenu);
		}

	}

	/**
	 * Sets state of {@link #uploadButton} and {@link #uploadMenuItem}.
	 * @param allow <code>true</code> if uploading is allowed, otherwise <code>false</code> 
	 */
	public void allowUpload(boolean allow) {
		uploadButton.setEnabled(allow);
		uploadMenuItem.setEnabled(allow);
	}

	private void addAccelerator(JMenuItem item, int keyEvent) {
		if (!BeaverGUI.osName.startsWith("mac os x")) { //$NON-NLS-1$
			item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, Event.CTRL_MASK));
		} else {
			item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, Event.META_MASK));
		}
	}
}

