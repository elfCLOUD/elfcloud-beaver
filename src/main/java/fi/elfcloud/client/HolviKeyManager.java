package fi.elfcloud.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.crypto.KeyGenerator;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import fi.elfcloud.client.dialog.KeyAdditionDialog;
import fi.elfcloud.sci.HolviClient;
import fi.elfcloud.sci.Utils;
import fi.elfcloud.sci.HolviClient.ENC;
import fi.elfcloud.sci.exception.HolviEncryptionException;

/**
 * {@link HolviKeyManager} provides UI for managing and selecting encryption keys.
 *
 */
public class HolviKeyManager extends JDialog implements ActionListener {
	private static final long serialVersionUID = -1332311688918774905L;
	static final int ACTION_APPLY = 0;
	static final int ACTION_CANCEL = 1;
	static final int ACTION_ADDKEY = 2;
	static final int ACTION_DELETE = 3;
	private JTable table;
	private DefaultTableModel tableModel;
	private JScrollPane scrollPane;
	private Preferences prefs;

	public HolviKeyManager(BeaverGUI gui) {
		super();
		this.prefs = BeaverGUI.getPreferences();
		this.table = new JTable();
		this.table.setFillsViewportHeight(true);
		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(table);
		this.scrollPane.setBorder(BorderFactory.createMatteBorder(0,0,1,0, SystemColor.activeCaptionBorder));
		this.tableModel = new DefaultTableModel(new Object[]{"Key file", "Description", "Key hash"},0) {
			private static final long serialVersionUID = 3626096149731297335L;
			public boolean isCellEditable(int rowIndex, int mColIndex) {
				return false;
			}
		};
		this.table.setModel(tableModel);
		this.table.setShowVerticalLines(false);
		this.table.getTableHeader().setReorderingAllowed(false);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
					selectKey();
				}
			}
		});
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		setTitle("elfCLOUD.fi\u2122 Beaver - Key Manager");
		setModal(true);
		setMinimumSize(new Dimension(500, 500));

		JPanel contentPanel = new JPanel(new BorderLayout());
		populateTable();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		JPanel buttonPanelLeft = new JPanel();
		JPanel buttonPanelRight = new JPanel();

		buttonPanel.add(buttonPanelLeft, BorderLayout.LINE_START);
		buttonPanel.add(buttonPanelRight, BorderLayout.LINE_END);
		JButton button = new JButton("Add key");
		button.setActionCommand(Integer.toString(ACTION_ADDKEY));
		button.addActionListener(this);
		buttonPanelLeft.add(button);

		button = new JButton("Delete key");
		button.setActionCommand(Integer.toString(3));
		button.addActionListener(this);
		buttonPanelLeft.add(button);

		button = new JButton("Select");
		button.setActionCommand(Integer.toString(ACTION_APPLY));
		button.addActionListener(this);
		buttonPanelRight.add(button);
		button = new JButton("Cancel");
		button.setActionCommand(Integer.toString(ACTION_CANCEL));
		button.addActionListener(this);
		buttonPanelRight.add(button);

		contentPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.PAGE_END);
		setContentPane(contentPanel);
		pack();
		setLocationByPlatform(true);
	}

	/**
	 * Updates the {@link #tableModel} with new keys.
	 * 
	 */
	private void populateTable() {
		ArrayList<HolviXMLKeyItem> keyList = BeaverGUI.getKeyList();
		if (keyList != null) {
			for (int i = 0; i < keyList.size(); i++) {
				this.tableModel.addRow(new Object[]{keyList.get(i).getPath(),
						keyList.get(i).getTableDescription(),
						keyList.get(i).getKeyHash()});
				if (keyList.get(i).getKeyHash().equals(prefs.get("selected.key", ""))) {
					this.table.setRowSelectionInterval(i, i);
				}
			}
			this.invalidate();
			this.repaint();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (Integer.parseInt(e.getActionCommand())) {
		case ACTION_APPLY: // Select new key
			selectKey();
			break;
		case ACTION_CANCEL: // Cancel (hide window)
			this.dispose();
			break;
		case ACTION_ADDKEY: // Add new key
			KeyAdditionDialog dialog = new KeyAdditionDialog(this);
			dialog.setVisible(true);
			dialog.dispose();
			break;
		case ACTION_DELETE: // Delete key
			if (this.table.getSelectedRow() != -1 && JOptionPane.YES_OPTION ==JOptionPane.showConfirmDialog(this, "Are you sure you want to remove the selected key?", "Remove selected key", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
				deleteKey();
			}
			break;
		}

	}
	
	private void selectKey() {
		if (this.table.getSelectedRow() != -1) {
			this.prefs.put("selected.key", this.tableModel.getValueAt(this.table.getSelectedRow(), 2).toString());
			this.dispose();
		} else {
			JOptionPane.showMessageDialog(this, "Select a key from the list or generate a new key", "Selection missing", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void deleteKey(String path) {
		try {
			ArrayList<HolviXMLKeyItem> keyList = BeaverGUI.getKeyList();
			int i = 0;
			for (HolviXMLKeyItem key: keyList) {
				if (key.getPath().equals(path)) {
					keyList.remove(i);
					if (key.getKeyHash().equals(this.prefs.get("selected.key", ""))) {
						BeaverGUI.getClient().setEncryptionKey(new byte[0]);
						BeaverGUI.getClient().setIV(new byte[0]);
						BeaverGUI.getClient().setEncryptionMode(ENC.NONE);
						this.prefs.put("selected.key", "");
					}
					BeaverGUI.getClient().removeEncryptionKey(key.getKeyHash());
					this.tableModel.removeRow(i);
					writeXML(keyList);
					break;
				}
				i++;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Deletes selected key from {@link #tableModel} and from {@link HolviClient}
	 * 
	 */
	private void deleteKey() {
		try {
			String selectedKey = this.tableModel.getValueAt(this.table.getSelectedRow(), 2).toString();
			ArrayList<HolviXMLKeyItem> keyList = BeaverGUI.getKeyList();
			int i = 0;
			for (HolviXMLKeyItem key: keyList) {
				if (key.getKeyHash().equals(selectedKey)) {
					keyList.remove(i);
					if (key.getKeyHash().equals(this.prefs.get("selected.key", ""))) {
						BeaverGUI.getClient().setEncryptionKey(new byte[0]);
						BeaverGUI.getClient().setIV(new byte[0]);
						BeaverGUI.getClient().setEncryptionMode(ENC.NONE);
						this.prefs.put("selected.key", "");
					}
					BeaverGUI.getClient().removeEncryptionKey(selectedKey);
					this.tableModel.removeRow(i);
					writeXML(keyList);
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.add(new JLabel("The key has been removed from the Cloud Beaver key manager."));
					panel.add(new JLabel("<html>The key file was NOT deleted and can still be found at" + key.getPath() + ".<br>If you do not need the key anymore, please delete the file manually.</html>"));
					JOptionPane.showMessageDialog(this, panel, "Encryption key deleted", JOptionPane.INFORMATION_MESSAGE);
					break;
				}
				i++;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Writes all {@link HolviXMLKeyItem}s to a XML file.
	 * @param keyList keys to be saved.
	 */
	private synchronized static void writeXML(ArrayList<HolviXMLKeyItem> keyList) {
		BufferedWriter fo = null;
		HolviXMLKeyList list = new HolviXMLKeyList();
		list.setKeyList(keyList);
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(HolviXMLKeyList.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			fo = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(BeaverGUI.getKeyFileXML()), "UTF-8")
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

	/**
	 * Adds new key to {@link HolviClient} and {@link #tableModel}.
	 * @param iv initialization vector of the key
	 * @param key encryption key
	 * @param path path to the key file
	 * @param description description of the key
	 * @param keyLength length of the key
	 */
	public static HolviXMLKeyItem addKey(byte[] iv, byte[] key, String path, String description, int keyLength) {
		try {
			ArrayList<HolviXMLKeyItem> keyList = BeaverGUI.getKeyList();
			BeaverGUI.getClient().addEncryptionKey(iv, key);
			HolviXMLKeyItem xmlKey = new HolviXMLKeyItem(path, description, Utils.calculateKeyHash(iv, key), keyLength);
			keyList.add(xmlKey);
			writeXML(keyList);
			return xmlKey;
		} catch (HolviEncryptionException e) {
			
		}
		return null;
	}

	public void addRow(String path, String description, String keyHash) {
		this.tableModel.addRow(new String[]{path, description, keyHash}); 
		this.table.invalidate();
		this.table.repaint();
	}

	/**
	 * Generates new encryption key and saves it to a file.
	 * @param path location where the new key should be writte
	 * @param description description of the key
	 * @param keyLength length of the key
	 */
	public static HolviXMLKeyItem generateKey(String path, String description, int keyLength) {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128); // IV is 16 bytes long
			Key iv = keyGen.generateKey();
			keyGen.init(keyLength);
			Key encKey = keyGen.generateKey();
			File keyfile = new File(path);
			FileOutputStream fo = new FileOutputStream(keyfile);
			fo.write(iv.getEncoded());
			fo.write(encKey.getEncoded());
			fo.close();
			return addKey(iv.getEncoded(), encKey.getEncoded(), path, description, keyLength);
		} catch (NoSuchAlgorithmException e) {
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Error creating key file: " + e.getMessage(), "File not found", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

