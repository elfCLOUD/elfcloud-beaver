package fi.elfcloud.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import fi.elfcloud.sci.HolviClient;
import fi.elfcloud.sci.HolviClient.ENC;

/**
 * Main Window for elfCLOUD.fi Beaver. 
 *
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = -6463168887784521424L;
	private BeaverGUI gui;
	private JLabel encryptionLabel;
	private ImageIcon encryptionON = new ImageIcon(BeaverGUI.class.getResource("icons/lock_ok_16x16.png"));
	private ImageIcon encryptionOFF = new ImageIcon(BeaverGUI.class.getResource("icons/lock_error_16x16.png"));
	private JButton uploadButton;
	private JMenuItem uploadMenuItem;
	private JCheckBoxMenuItem cbMenuItem;

	public MainWindow(BeaverGUI gui) {
		super("elfCLOUD.fi\u2122 Beaver - Vault Directory");
		this.gui = gui;
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(650, 450));
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
		this.encryptionLabel.setText("ENCRYPTION DISABLED");
		this.encryptionLabel.setIcon(encryptionOFF);
		statusPanel.add(encryptionLabel);
	}

	/**
	 * Updates {@link #encryptionLabel} to show current encryption status.
	 * @param keyhash hash of the current encryption key
	 * 
	 */
	public void updateEncryptionLabel(String keyhash) {
		if (BeaverGUI.getClient().getEncryptionMode() == HolviClient.ENC.NONE) {
			encryptionLabel.setText("ENCRYPTION DISABLED");
			encryptionLabel.setIcon(encryptionOFF);
		} else {
			for (HolviXMLKeyItem key: BeaverGUI.getKeyList()) {
				if (key.getKeyHash().equals(keyhash) && key.isAvailable()) {
					File f = new File(key.getPath());
					encryptionLabel.setText(f.getName() + (key.getDescription().length() > 0 ? " (" + key.getDescription() +")" : ""));
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

		button = makeButton("home_top_level.png", gui.ACTION_HOME, "Return to top level", "Home");
		pToolBar.add(button);

		button = makeButton("go_back.png", gui.ACTION_PREVIOUS, "Go back one level", "Back");
		pToolBar.add(button);

		button = makeButton("refresh.png", gui.ACTION_REFRESH, "Refresh hierarchy", "Refresh");
		pToolBar.add(button);

		pToolBar.addSeparator();

		button = makeButton("add_folder.png", gui.ACTION_ADD_CONTAINER, "Create a new folder", "New");
		pToolBar.add(button);

		this.uploadButton = makeButton("upload.png", gui.ACTION_ADD_ITEM, "Upload files and folders", "Upload");
		this.uploadButton.setEnabled(false);
		pToolBar.add(this.uploadButton);

		pToolBar.addSeparator();

		button = makeButton("download.png", gui.ACTION_SAVE_ITEM, "<html>Download selected file(s),<br/>or all files and folders without selection</html>", "Download");
		pToolBar.add(button);

		button = makeButton("info_file.png", gui.ACTION_INFORMATION, "Show information", "Details");
		pToolBar.add(button);

		pToolBar.addSeparator();

		button = makeButton("modify_rename.png", gui.ACTION_MODIFY, "Modify", "Modify");
		pToolBar.add(button);

		button = makeButton("move_file.png", gui.ACTION_MOVE, "Move files", "Move");
		pToolBar.add(button);

		button = makeButton("delete.png", gui.ACTION_DELETE, "Delete selected item(s)", "Delete");
		pToolBar.add(button);
		
		pToolBar.addSeparator();
		
		button = makeButton("key.png", gui.ACTION_MANAGE_KEYS, "Select encryption key", "Keys");
		pToolBar.add(button);
		
		pToolBar.add(Box.createHorizontalGlue());
		button = makeButton("help.png", gui.ACTION_HELP, "Help", "Help");
		pToolBar.add(button);
	}

	/**
	 * Returns {@link URL} of an icon by it's name.
	 * @param imageName name of the icon file.
	 * @return {@link URL} pointing to the icon resource.
	 */
	private URL getIconUrl(String imageName) {
		return BeaverGUI.class.getResource("icons/"+imageName);
	}

	/**
	 * Creates new {@link JButton}.
	 * @param imageName name of the icon displayed in this {@link JButton}
	 * @param action action to be bound to this {@link JButton}
	 * @param tooltipText tool tip for this {@link JButton}
	 * @return new {@link JButton}
	 */
	public JButton makeButton(String imageName, int action, String tooltipText, String buttonText) {
		URL imageURL = BeaverGUI.class.getResource("icons/"+imageName);
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
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		this.uploadMenuItem = new JMenuItem("Upload");
		this.uploadMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		this.uploadMenuItem.setActionCommand(Integer.toString(gui.ACTION_ADD_ITEM));
		this.uploadMenuItem.addActionListener(gui);
		this.uploadMenuItem.setEnabled(false);
		this.uploadMenuItem.setIcon(new ImageIcon(getIconUrl("upload16.png")));
		fileMenu.add(this.uploadMenuItem);
		menuItem = new JMenuItem("Add folder");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_ADD_CONTAINER));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(getIconUrl("add_folder16.png")));
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Download");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_SAVE_ITEM));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(getIconUrl("download16.png")));
		fileMenu.add(menuItem);

		if (!BeaverGUI.osName.startsWith("mac os x")) {
			fileMenu.addSeparator();
			menuItem = new JMenuItem("Exit");
			menuItem.setIcon(new ImageIcon(getIconUrl("exit_16x16.png")));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			fileMenu.add(menuItem);
		}
		JMenu encryptionMenu = new JMenu("Encryption");
		encryptionMenu.setMnemonic(KeyEvent.VK_E);
		menuItem = new JMenuItem("Key manager");
		menuItem.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/key16.png")));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_MANAGE_KEYS));
		menuItem.setMnemonic(KeyEvent.VK_M);
		menuItem.addActionListener(gui);
		encryptionMenu.add(menuItem);
		this.cbMenuItem = new JCheckBoxMenuItem("Disable encryption", false);
		this.cbMenuItem.setMnemonic(KeyEvent.VK_D);
		this.cbMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (cbMenuItem.isSelected()) {
					BeaverGUI.getClient().setEncryptionMode(ENC.NONE);
					updateEncryptionLabel("");
				} else {
					BeaverGUI.getClient().setEncryptionMode(ENC.AES_256);
					updateEncryptionLabel(BeaverGUI.getPreferences().get("selected.key", ""));
				}
			}
		});
		encryptionMenu.add(cbMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(encryptionMenu);

		if (!BeaverGUI.osName.startsWith("mac os x")) {
			JMenu helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			menuItem = new JMenuItem("About");
			menuItem.setIcon(new ImageIcon(getIconUrl("information_16x16.png")));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AboutDialog dialog = new AboutDialog((JFrame)getParent());
					dialog.setVisible(true);
				}
			});
			helpMenu.add(menuItem);

			menuItem = new JMenuItem("Help");
			menuItem.setIcon(new ImageIcon(getIconUrl("help16.png")));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					@SuppressWarnings("unused")
					HelpWindow help = new HelpWindow();
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
}

