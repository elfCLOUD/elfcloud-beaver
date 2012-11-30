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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.apple.eawt.Application;

import fi.elfcloud.client.dialog.ClusterDialog;
import fi.elfcloud.client.dialog.HelpWindow;
import fi.elfcloud.client.dialog.BeaverDialog;
import fi.elfcloud.client.dialog.LoginDialog;
import fi.elfcloud.client.dialog.ModifyClusterDialog;
import fi.elfcloud.client.dialog.ModifyDataItemDialog;
import fi.elfcloud.client.dialog.RelocateDataItemDialog;
import fi.elfcloud.client.dialog.VaultDialog;
import fi.elfcloud.client.preferences.PreferencesWindow;
import fi.elfcloud.client.tree.ClusterHierarchyModel;
import fi.elfcloud.client.tree.ClusterNode;
import fi.elfcloud.client.tree.ClusterTree;
import fi.elfcloud.client.tree.ClusterTreeNode;
import fi.elfcloud.client.tree.DataItemNode;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.Client;
import fi.elfcloud.sci.Client.ENC;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.container.Vault;
import fi.elfcloud.sci.exception.ECEncryptionException;
import fi.elfcloud.sci.exception.ECException;

/**
 * elfcloud.fi Beaver main class. <p>
 * Contains all the control logic for the elfcloud.fi Beaver client.
 * 
 */
public class BeaverGUI implements Runnable, ActionListener {
	public final int ACTION_HOME = 0;
	public final int ACTION_PREVIOUS = 1;	
	public final int ACTION_REFRESH = 2;
	public final int ACTION_ADD_CONTAINER = 3;
	public final int ACTION_ADD_ITEM = 4;
	public final int ACTION_SAVE_ITEM = 5;
	//	public final int ACTION_SAVE_ALL = 6; // no longer used
	public final int ACTION_DELETE = 7;
	public final int ACTION_MANAGE_KEYS = 8;
	public final int ACTION_MODIFY = 9;
	public final int ACTION_INFORMATION = 10;
	public final int ACTION_MOVE = 11;
	public final int ACTION_HELP = 12;
	public final int ACTION_GOTO = 13;
	public static final String titlePrefix = Messages.getString("BeaverGUI.title_prefix"); //$NON-NLS-1$
	public static final URL iconUrl = BeaverGUI.class.getResource("icons/icon.png"); //$NON-NLS-1$
	public static String serverUrl = "https://api.elfcloud.fi/"; //$NON-NLS-1$
	public static boolean serverUrlGiven = false;
	public static String apikey = "atk8vzrhnc2by4f"; //$NON-NLS-1$
	public static final String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
	public static boolean allowUnencryptedUpload = false;
	public static File defaultDownloadDir;
	private Vault[] vaults = new Vault[0];
	private static Client client;
	private String configNodeName = "/fi/elfcloud/client"; //$NON-NLS-1$
	private static File keyFileXML;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private MainWindow mainWindow;
	private ClusterHierarchyModel treeModel;
	private ClusterTree treeView;
	private static ArrayList<XMLKeyItem> keyList = new ArrayList<XMLKeyItem>();
	private static Preferences prefs;
	private static String keyDirectoryPath = ""; //$NON-NLS-1$

	public static void main(String[] args) throws IOException {
		if (BeaverGUI.osName.startsWith("mac os x")) { //$NON-NLS-1$
			System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Beaver"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {

		}

		if (args.length > 0) {
			BeaverGUI.serverUrl = args[0];
			BeaverGUI.serverUrlGiven = true;
		}
		if (args.length == 2) {
			apikey = args[1];
		}
		EventQueue.invokeLater(new BeaverGUI());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		// User preferences
		prefs = Preferences.userRoot().node(configNodeName);
		String[] lang = prefs.get("elfcloud.interface.language", "en_GB").split("_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		Locale l = new Locale(lang[0], lang[1]);
		Messages.setLocale(l);
		setDirectories();
		initializeKeyDirectory();

		// Initialize Client
		client = new Client();
		mainWindow = new MainWindow(this);
		if (BeaverGUI.osName.startsWith("mac os x")) { //$NON-NLS-1$
			Application macApplication = Application.getApplication();
			ImageIcon icon = new ImageIcon(iconUrl);
			macApplication.setDockIconImage(icon.getImage());
			MacApplicationAdapter macAdapter = new MacApplicationAdapter(mainWindow);
			macApplication.addApplicationListener(macAdapter);
		}
		mainWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		// Ask for login
		// Modal dialog, application will not proceed without succesful authentication
		LoginDialog loginDlg = new LoginDialog(mainWindow, this);
		loginDlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		loginDlg.setVisible(true);

		// Preference change listener for encryption key changes
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if (event.getKey().equals("selected.key")) { //$NON-NLS-1$
					changeEncryptionKey();
				}
				if (event.getKey().equals("elfcloud.download.directory")) {
					setDirectories();
				}
				if (event.getKey().equals("elfcloud.allow.unencrypted.upload")) {
					allowUnencryptedUpload = event.getNewValue().equals("true");
				}
				if (event.getKey().equals("elfcloud.interface.language")) {
					try {
						MainWindow newWindow = new MainWindow(BeaverGUI.this);
						initializeMainWindow(newWindow);
						mainWindow.dispose();
						mainWindow = newWindow;
						mainWindow.updateEncryptionLabel(prefs.get("selected.key", ""));
						mainWindow.setVisible(true);
					} catch (ECException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void setDirectories() {
		String downloadPath = prefs.get("elfcloud.download.directory", System.getProperty("user.home"));
		File file = new File(downloadPath);
		if (file.isDirectory()) {
			BeaverGUI.defaultDownloadDir = new File(downloadPath);
		} else {
			BeaverGUI.defaultDownloadDir = new File(System.getProperty("user.home"));
		}
	}

	/**
	 * Notifies {@link #mainWindow} about upload availability status
	 * @param allow state declaring if uploading is possible
	 * @see MainWindow
	 */
	public void allowUpload(boolean allow) {
		mainWindow.allowUpload(allow);
	}

	/**
	 * Returns updated array of available vaults.
	 * <p>
	 * Calls for {@link #listVaults()} to update available {@link #vaults} and returns.
	 * @return available {@link #vaults}
	 * @throws ECException
	 * @throws IOException
	 * @see Vault
	 */
	public Vault[] getVaults() throws ECException, IOException {
		listVaults();
		return vaults;
	}

	/**
	 * Updates {@link #mainWindow} title to show current cluster
	 * @param title new suffix for the window title
	 * @see MainWindow
	 */
	public void updateTitle(String title) {
		mainWindow.setTitle(BeaverGUI.titlePrefix + (title.length() > 0 ? title : Messages.getString("BeaverGUI.window_title_top_level"))); //$NON-NLS-1$
	}

	/**
	 * Loads cipher key and IV from file.
	 * @param path path to the file
	 * @return {@link HashMap} that contains 'iv' and 'key' keys and corresponding byte[] values.
	 * @throws IOException
	 * @throws ECEncryptionException 
	 */
	public static HashMap<String, byte[]> loadKeyFromFile(String path) throws IOException, ECEncryptionException {
		HashMap<String, byte[]> returnValues = new HashMap<String, byte[]>();
		byte[] buffer = new byte[32];
		byte[] iv = new byte[16];
		byte[] key = new byte[0];
		File f = new File(path);
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(f);
			fi.read(iv);
			int read = fi.read(buffer);
			if (read != -1) {
				key = new byte[read];
				System.arraycopy(buffer, 0, key, 0, read);
			}
			if (fi.available() > 0) {
				throw new ECEncryptionException(0, "Invalid encryption key");
			}
		} finally {
			try {
				if (fi != null) {
					fi.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		returnValues.put("iv", iv); //$NON-NLS-1$
		returnValues.put("key", key); //$NON-NLS-1$
		return returnValues;
	}

	/**
	 * Changes encryption key used by {@link #client} and updates {@link #mainWindow}.
	 * @see Client
	 * @see MainWindow
	 */
	public void changeEncryptionKey() {
		String keyHash = prefs.get("selected.key", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (keyHash.equals("")) { //$NON-NLS-1$
			mainWindow.updateEncryptionLabel(""); //$NON-NLS-1$
			return;
		}
		XMLKeyItem keyItem = findKeyFromList(keyHash);
		if (keyItem != null) {
			HashMap<String, byte[]> keyMap;
			int keyLength = 0;
			try {
				keyMap = loadKeyFromFile(keyItem.getPath());
				byte[] iv = keyMap.get("iv"); //$NON-NLS-1$
				byte[] key = keyMap.get("key"); //$NON-NLS-1$
				client.setIV(iv);
				client.setEncryptionKey(key);
				mainWindow.updateEncryptionCheckbox(false);
				keyLength = key.length;
				keyItem.exists(true);
			} catch (FileNotFoundException e) {
				keyItem.exists(false);
				mainWindow.updateEncryptionCheckbox(true);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(mainWindow, Messages.getString("BeaverGUI.error_key_not_available_text"), Messages.getString("BeaverGUI.error_key_not_available_title"), JOptionPane.OK_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ECEncryptionException e) {
				e.printStackTrace();
			}
			switch (keyLength) {
			case 16:
				client.setEncryptionMode(ENC.AES_128);
				break;
			case 24:
				client.setEncryptionMode(ENC.AES_192);
				break;
			case 32:
				client.setEncryptionMode(ENC.AES_256);
				break;
			default:
				client.setEncryptionMode(ENC.NONE);
				break;
			}
			mainWindow.updateEncryptionLabel(keyItem.getKeyHash());
		}
	}

	/***
	 * Finds a {@link XMLKeyItem} from {@link XMLKeyList} that matches with given hash value.
	 * @param keyHash hash to be searched
	 * @return {@link XMLKeyItem} that matches with keyHash, otherwise null
	 */
	public static XMLKeyItem findKeyFromList(String keyHash) {
		if (keyList != null) {
			for (XMLKeyItem keyItem: keyList) {
				if (keyItem.getKeyHash().equals(keyHash)) {
					return keyItem;
				}
			}
		}
		return null;
	}

	/**
	 * Initializes {@link MainWindow} with {@link JMenuBar}, {@link JToolBar}, {@link ClusterTree} and status panel.
	 * @param main 
	 * @throws IOException 
	 * @throws ECException 
	 */
	public void initializeMainWindow(MainWindow main) throws ECException, IOException {
		// Init and populate Menu
		menuBar = new JMenuBar();
		main.populateMenubar(menuBar);
		main.setJMenuBar(menuBar);
		// Init and populate Toolbar
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		main.populateToolbar(toolBar);
		main.add(toolBar, BorderLayout.PAGE_START);

		// Init ScrollView
		ClusterNode homeNode = new ClusterNode();
		treeModel = new ClusterHierarchyModel(homeNode, this);
		treeView = new ClusterTree(treeModel, this);
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(treeView);
		main.add(scrollPanel, BorderLayout.CENTER);

		JPanel statusPanel = new JPanel();
		main.add(statusPanel, BorderLayout.PAGE_END);
		statusPanel.setPreferredSize(new Dimension(main.getWidth(), 24));
		main.populateStatusPanel(statusPanel);
		scrollPanel.requestFocusInWindow();
		main.setMinimumSize(new Dimension(toolBar.getPreferredSize().width, 450));
	}

	/**
	 * Returns the {@link #client} instance used by elfcloud.fi Beaver
	 * @return current client
	 * @see Client
	 */
	public static Client getClient() {
		return client;
	}

	/**
	 * Loads available keys from XML-file located in user's home directory.<p>
	 * Populates {@link #keyList} with available {@link XMLKeyItem}s
	 * Updates {@link #client} with available keys.
	 */
	public void parseKeyFile() {
		File keyDir = new File(BeaverGUI.keyDirectoryPath);;
		keyDir.mkdirs();
		keyFileXML = new File(keyDir, "keylist.xml"); //$NON-NLS-1$
		try {
			keyFileXML.createNewFile();
		} catch (IOException e) {

		}
		JAXBContext context;
		Unmarshaller um;
		XMLKeyList xmlkeyList = null;
		try {
			context = JAXBContext.newInstance(XMLKeyList.class);
			um = context.createUnmarshaller();
			xmlkeyList = (XMLKeyList) um.unmarshal(new InputStreamReader(new FileInputStream(keyFileXML), "UTF-8")); //$NON-NLS-1$
		} catch (JAXBException e1) {
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		}

		if (xmlkeyList != null) {
			keyList = xmlkeyList.getKeysList();
			if (keyList != null) {
				for (int i = 0; i < keyList.size(); i++) {
					XMLKeyItem keyItem = keyList.get(i);
					try {
						HashMap<String, byte[]> keyMap = loadKeyFromFile(keyItem.getPath());
						client.addEncryptionKey(keyMap.get("iv"), keyMap.get("key")); //$NON-NLS-1$ //$NON-NLS-2$
						keyItem.exists(true);
					} catch (FileNotFoundException e) {
						keyItem.exists(false);
						continue;
					} catch (IOException e) {
						keyItem.exists(false);
						continue;
					} catch (ECEncryptionException e) {
						keyItem.exists(false);
						continue;
					}
				}
			} else {
				keyList = new ArrayList<XMLKeyItem>();
			}
		}
	}

	/**
	 * Authenticates {@link #client} with given parameters and sets the {@link #mainWindow} visible.
	 * @param username username for authentication.
	 * @param password password for authentication.
	 * @param rememberUsername should the username be stored in {@link #prefs}
	 * @throws ECException
	 * @throws IOException 
	 * @see {@link Client}
	 */
	public void authenticate(String username, String password, boolean rememberUsername) throws ECException, IOException {
		client.setUsername(username);
		client.setPassword(password);
		client.setServerUrl(BeaverGUI.serverUrl);
		client.setApikey(BeaverGUI.apikey);
		client.setEncryptionMode(Client.ENC.NONE);
		client.auth();
		if (rememberUsername) {
			prefs.putBoolean("elfcloud.remember.username", true); //$NON-NLS-1$
			prefs.put("elfcloud.username", username); //$NON-NLS-1$
		} else {
			prefs.remove("elfcloud.remember.username"); //$NON-NLS-1$
			prefs.remove("elfcloud.username"); //$NON-NLS-1$
		}
		initializeMainWindow(mainWindow);
		parseKeyFile();
		changeEncryptionKey();
		mainWindow.setVisible(true);
		if (prefs.getBoolean("elfcloud.show.tutorial", true)) { //$NON-NLS-1$
			@SuppressWarnings("unused")
			HelpWindow help = HelpWindow.getInstance();
		}
	}

	/**
	 * Updates {@link #vaults} 
	 * @throws ECException
	 * @throws IOException
	 * @see Client
	 */
	public void listVaults() throws ECException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		vaults = client.listVaults(map);
	}

	/**
	 * Returns {@link Preferences} used by {@link BeaverGUI}.
	 * @return application {@link #prefs}
	 */
	public static Preferences getPreferences() {
		return prefs;
	}

	/**
	 * Return {@link ArrayList} of available {@link XMLKeyItem}s
	 * @return available cipher keys
	 */
	public static ArrayList<XMLKeyItem> getKeyList() {
		return keyList;
	}

	/**
	 * Sets {@link #keyList}
	 * @param keylist new encryption keys
	 */
	public void setKeyList(ArrayList<XMLKeyItem> keylist) {
		BeaverGUI.keyList = keylist;
	}

	/**
	 * Returns {@link File} {@link #keyFileXML}
	 * @return file containing available cipher keys in XML format
	 */
	public static File getKeyFileXML() {
		return keyFileXML;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		int cmd = Integer.parseInt(e.getActionCommand());
		int returnVal;
		TreePath[] paths;
		ClusterTreeNode selection;
		ClusterNode rootNode;
		ClusterTreeNode node = null;
		try {
			switch (cmd) {
			case ACTION_HOME:
				treeModel.home();
				break;
			case ACTION_PREVIOUS:
				treeModel.previous();
				break;
			case ACTION_REFRESH:
				treeModel.refresh();
				break;
			case ACTION_ADD_CONTAINER:
				rootNode = (ClusterNode) treeModel.getRoot();
				BeaverDialog dialog;
				if (treeModel.isHomeNode(rootNode)) {
					dialog = new VaultDialog(this);
					if (showClusterModifyDialog(dialog)) {
						Cluster cluster = client.addVault(((VaultDialog) dialog).getClusterName(), ((VaultDialog) dialog).getVaultType());
						treeModel.addNode(rootNode, cluster);
					}
				} else {
					dialog = new ClusterDialog();
					if (showClusterModifyDialog(dialog)) {
						Cluster cluster = client.addCluster(((ClusterDialog) dialog).getClusterName(), rootNode.getElement().getId());
						treeModel.addNode(rootNode, cluster);
					}
				}
				break;
			case ACTION_ADD_ITEM:
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setMultiSelectionEnabled(true);
				returnVal = fc.showOpenDialog(this.mainWindow);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fc.getSelectedFiles();
					treeModel.storeData(files);
				}
				break;
			case ACTION_SAVE_ITEM:
				int selectionCount = treeView.getSelectionCount();
				paths = treeView.getSelectionPaths();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (BeaverGUI.defaultDownloadDir.isDirectory()) {
					fc.setCurrentDirectory(BeaverGUI.defaultDownloadDir); //$NON-NLS-1$
				} else {
					fc.setCurrentDirectory(new File(System.getProperty("user.home"))); //$NON-NLS-1$
				}
				if (selectionCount == 0) {
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					returnVal = fc.showSaveDialog(mainWindow);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						treeModel.fetchAll(file);
					}
					break;
				}
				if (selectionCount > 1) {
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				} else if (selectionCount == 1){
					selection = treeView.getSelection();
					if (selection instanceof DataItemNode) {
						DataItemNode diNode = (DataItemNode) selection;
						fc.setSelectedFile(new File(diNode.getElement().getName()));
					} else if (selection instanceof ClusterNode) {
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					}
				}
				returnVal = fc.showSaveDialog(mainWindow);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					treeModel.fetchData(file, paths);
				}
				break;
			case ACTION_DELETE:
				paths = treeView.getSelectionPaths();
				try {
					if (paths.length > 0) {
						treeModel.removeSelected(paths);
					}
				} catch (NullPointerException npe) {					
				}
				break;
			case ACTION_MANAGE_KEYS:
				@SuppressWarnings("unused")
				PreferencesWindow prefsWindow = PreferencesWindow.getInstance(2);
				break;
			case ACTION_MODIFY: 
				paths = treeView.getSelectionPaths();
				try {
					if (paths.length == 1) {
						selection = (ClusterTreeNode) paths[0].getLastPathComponent();
						if (selection instanceof DataItemNode) {
							DataItem di = (DataItem)selection.getElement();
							HashMap<String, String> metamap = ((DataItemNode)selection).getMetaMap();
							ModifyDataItemDialog mdid = new ModifyDataItemDialog(mainWindow, metamap, di.getName());
							returnVal = mdid.showDialog();
							if (returnVal == JOptionPane.OK_OPTION) {
								if (!di.getName().equals(mdid.getName())) {
									di.rename(mdid.getName());
								}
								metamap.put("TGS", mdid.getTags()); //$NON-NLS-1$
								metamap.put("DSC", mdid.getDesc()); //$NON-NLS-1$
								di.updateMeta(metamap);
								treeModel.nodeChanged(selection);
								treeView.repaint();

							}
						} else {
							Cluster cluster = (Cluster)selection.getElement();
							ModifyClusterDialog mcd = new ModifyClusterDialog(mainWindow, cluster.getName());
							returnVal = mcd.showDialog();
							if (returnVal == JOptionPane.OK_OPTION) {
								if (!mcd.getName().equals(cluster.getName())) {
									cluster.rename(mcd.getName());
									treeModel.nodeChanged(selection);
									treeView.repaint();
								}
							}
						}
					}
				} catch (NullPointerException npe) {
				}
				break;
			case ACTION_INFORMATION:
				paths = treeView.getSelectionPaths();
				try {
					if (paths.length == 1) {
						selection = (ClusterTreeNode) paths[0].getLastPathComponent();
						selection.informationDialog().setVisible(true);
					} 
				} catch (NullPointerException npe) {
				}
				break;
			case ACTION_MOVE:
				rootNode = (ClusterNode) treeModel.getRoot();
				try {
					if (rootNode.getPath().length > 1) {
						Cluster cluster = (Cluster) rootNode.getElement(); // Current root cluster
						rootNode = (ClusterNode)rootNode.getPath()[1]; 
						Vault vault = (Vault) rootNode.getElement(); // Vault for current root cluster
						if (treeView.getSelectionPaths().length > 0) {
							paths = treeView.getSelectionPaths();
							for (TreePath path: paths) {
								node = (ClusterTreeNode) path.getLastPathComponent();
								if (!(node instanceof DataItemNode)) {
									JOptionPane.showMessageDialog(mainWindow, Messages.getString("BeaverGUI.error_file_move_text"), Messages.getString("BeaverGUI.error_file_move_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
									return;
								}
							}
							RelocateDataItemDialog relocateDialog = new RelocateDataItemDialog(this, vault);
							if (relocateDialog.showDialog() == JOptionPane.OK_OPTION) {
								if (relocateDialog.getSelection().getId() != cluster.getId()) { // Relocate if the destination is not the current root cluster
									treeModel.relocateDataItems(relocateDialog.getSelection(), paths);
									treeModel.refresh();
								} else {
									JOptionPane.showMessageDialog(mainWindow, Messages.getString("BeaverGUI.error_file_move_destination_text"), Messages.getString("BeaverGUI.error_file_move_destination_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
						}
					} 
				} catch (NullPointerException npe) {

				}
				break;
			case ACTION_HELP:
				@SuppressWarnings("unused")
				HelpWindow helpWindow = HelpWindow.getInstance();
				break;
			case ACTION_GOTO:
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI(Messages.getString("LoginDialog.link_register_new_account"))); //$NON-NLS-1$
				} catch (IOException exc) {
					exc.printStackTrace();
				} catch (URISyntaxException exc) {
					exc.printStackTrace();
				}		
				break;
			default:
				break;
			}
		} catch (Exception exc) {
			handleException(exc);
		}
	}

	/**
	 * Shows a dialog to modify selected {@link ClusterNode} or {@link DataItemNode}.
	 * @param dialog dialog to be shown.
	 * @return <code>true</code> on approved action, otherwise <code>false</code>.
	 * @see ClusterNode
	 * @see DataItemNode
	 * @see BeaverDialog
	 */
	public boolean showClusterModifyDialog(BeaverDialog dialog) {
		int result = dialog.showDialog();
		if (result == JOptionPane.OK_OPTION) {
			if (dialog instanceof VaultDialog) {
				if (((VaultDialog) dialog).getClusterName().length() > 256) {
					JOptionPane.showMessageDialog(dialog.getParent(), Messages.getString("BeaverGUI.error_vault_name_too_long") + //$NON-NLS-1$
							Messages.getString("BeaverGUI.error_vault_name_too_long_line2"),  //$NON-NLS-1$
							Messages.getString("BeaverGUI.error_vault_name_too_long_title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return showClusterModifyDialog(dialog);
				} 
			} else if (dialog instanceof ClusterDialog) {
				if (((ClusterDialog) dialog).getClusterName().length() > 256) {
					JOptionPane.showMessageDialog(dialog.getParent(), Messages.getString("BeaverGUI.error_cluster_name_too_long_text") + //$NON-NLS-1$
							Messages.getString("BeaverGUI.error_cluster_name_too_long_text_line2"),  //$NON-NLS-1$
							Messages.getString("BeaverGUI.error_cluster_name_too_long_title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return showClusterModifyDialog(dialog);
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Shows dialog of encountered {@link Exception}
	 * @param e {@link Exception} encountered
	 */
	public void handleException(Exception e) {
		e.printStackTrace();
		if (e instanceof NullPointerException) {
			e = new Exception(Messages.getString("BeaverGUI.generic_client_error_message")); //$NON-NLS-1$
		} 
		JOptionPane.showMessageDialog(mainWindow,
				e.getMessage(),
				Messages.getString("BeaverGUI.error_dialog_title"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 *
	 */
	public ClusterHierarchyModel getTreeModel() {
		return this.treeModel;	
	}

	/**
	 * Shows confirm dialog to send files without encryption. 
	 * @return <code>true</code> if sending is allowed, <code>false</code> otherwise.
	 */
	public boolean confirmSend() {
		if (BeaverGUI.getClient().getEncryptionMode() == ENC.NONE && !BeaverGUI.allowUnencryptedUpload) {
			Object[] options = {Messages.getString("BeaverGUI.unencrypted_file_send_remember"), Messages.getString("BeaverGUI.unencrypted_file_send_once"), Messages.getString("BeaverGUI.unencrypted_file_deny_send")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String message = Messages.getString("BeaverGUI.unencrypted_file_send_question"); //$NON-NLS-1$
			int result = JOptionPane.showOptionDialog(null, message,
					Messages.getString("BeaverGUI.unencrypted_file_send_remember_title"), //$NON-NLS-1$
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[2]);
			if (result == JOptionPane.CANCEL_OPTION || result == -1) {
				return false;
			} else if (result == JOptionPane.YES_OPTION){
				BeaverGUI.allowUnencryptedUpload = true;
			} 
		}
		return true;
	}

	private void initializeKeyDirectory() {
		String path = ""; //$NON-NLS-1$
		if (BeaverGUI.osName.startsWith("win")) { //$NON-NLS-1$
			path = prefs.get("elfcloud.keydir", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (path.equals("")) { //$NON-NLS-1$
				path = System.getProperty("user.home") + System.getProperty("file.separator") + "elfcloud"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				prefs.put("elfcloud.keydir", path); //$NON-NLS-1$
			}
		} else {
			path = prefs.get("elfcloud.keydir", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (path.equals("")) { //$NON-NLS-1$
				path = System.getProperty("user.home") + System.getProperty("file.separator") + ".elfcloud"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				prefs.put("elfcloud.keydir", path); //$NON-NLS-1$
			}
		}
		BeaverGUI.keyDirectoryPath = path;
	}

	public static String getKeyDirectoryPath() {
		return keyDirectoryPath;
	}

	/**
	 * Writes all {@link XMLKeyItem}s to a XML file.
	 * @param keyList keys to be saved.
	 */
	public static synchronized void writeXML(ArrayList<XMLKeyItem> keyList) {
		BufferedWriter fo = null;
		XMLKeyList list = new XMLKeyList();
		list.setKeyList(keyList);
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(XMLKeyList.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			fo = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(BeaverGUI.getKeyFileXML()), "UTF-8") //$NON-NLS-1$
					);
			m.marshal(list, fo);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (fo != null) {
				try {
					fo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

