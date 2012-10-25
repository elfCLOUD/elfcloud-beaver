package fi.elfcloud.client.dialog;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Label;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;



import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.HolviKeyManager;
import fi.elfcloud.client.HolviXMLKeyItem;
import fi.elfcloud.sci.Utils;

class Topic  {
	String title;
	String url;

	public Topic(String title, String url) {
		this.title = title;
		this.url = url;
	}
}

public class HelpWindow extends JFrame implements WindowListener {
	private static final long serialVersionUID = 559978795029545851L;
	private ArrayList<Topic> topics = new ArrayList<Topic>();
	private JEditorPane topicContent;
	private Preferences prefs;
	private JCheckBox showAgain;
	private JPanel buttonPane;
	
	public HelpWindow() {
		setTitle("elfCLOUD.fi\u2122 Beaver - Help");
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		prefs = BeaverGUI.getPreferences();
		topics.add(new Topic("Welcome", "welcome.html"));
		topics.add(new Topic("Encrypting data", "encryption.html"));
		topics.add(new Topic("Managing folders", "folders.html"));
		topics.add(new Topic("Uploading files", "upload.html"));
		topics.add(new Topic("Downloading files", "download.html"));
		setLayout(new BorderLayout());
		addHelpPanel();
		addButtonPanel();
		setPreferredSize(new Dimension(980, 500));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);
		pack();
		setVisible(true);

	}

	private void addHelpPanel() {
		JPanel contentPanel = new JPanel();
		SpringLayout layout = new SpringLayout();
		contentPanel.setLayout(layout);
		DefaultListModel<String> listModel = new DefaultListModel<String>();

		for (Topic t: topics) {
			listModel.addElement(t.title);
		}

		JList<String> topicList = new JList<String>(listModel);
		topicList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topicList.getSelectionModel().addListSelectionListener(new SelectionListener());
		topicList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane topicScroller = new JScrollPane(topicList);
		contentPanel.add(topicScroller);

		this.topicContent = new JEditorPane();

		this.topicContent.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		this.topicContent.setEditorKit(kit);
		StyleSheet styles = kit.getStyleSheet();
		styles.addRule("body {margin-left: 10px; margin-bottom: 50px;}");
		styles.addRule("p {margin-top: 5px;");
		styles.addRule("li {list-style-type: none;}");
		styles.addRule("img {border: 2px;");
		styles.addRule("h3 {margin-top: 10px; margin-bottom: 10px;");
		try {
			this.topicContent.setPage(BeaverGUI.class.getResource("welcome.html"));
			this.topicContent.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent event) {
					HyperlinkEvent.EventType type = event.getEventType(); 
					if (type == HyperlinkEvent.EventType.ACTIVATED) 
					{ 
						Desktop desktop = Desktop.getDesktop();
						try {
							if (event.getURL().toString().startsWith("https:") || event.getURL().toString().startsWith("http:") ) {
								desktop.browse(event.getURL().toURI());
							} else {
								topicContent.setPage(event.getURL());
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		JScrollPane contentScroller = new JScrollPane(this.topicContent);
		contentScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		contentPanel.add(contentScroller);
		add(contentPanel, BorderLayout.CENTER);
		layout.putConstraint(SpringLayout.WEST, topicScroller, 5, SpringLayout.WEST, contentPanel);
		layout.putConstraint(SpringLayout.NORTH, topicScroller, 5, SpringLayout.NORTH, contentPanel);
		layout.putConstraint(SpringLayout.SOUTH, topicScroller, -5, SpringLayout.SOUTH, contentPanel);

		layout.putConstraint(SpringLayout.WEST, contentScroller, 5, SpringLayout.EAST, topicScroller);
		layout.putConstraint(SpringLayout.EAST, contentScroller, -5, SpringLayout.EAST, contentPanel);
		layout.putConstraint(SpringLayout.NORTH, contentScroller, 5, SpringLayout.NORTH, contentPanel);
		layout.putConstraint(SpringLayout.SOUTH, contentScroller, -5, SpringLayout.SOUTH, contentPanel);
		topicList.setSelectedIndex(0);
	}

	public void addCheckbox() {
		showAgain = new JCheckBox("Don't show this again");
		if (prefs.getBoolean("elfcloud.show.tutorial", true)) {
			showAgain.setSelected(false);
		}
		showAgain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (showAgain.isSelected()) {
					prefs.putBoolean("elfcloud.show.tutorial", false);
				} else {
					prefs.putBoolean("elfcloud.show.tutorial", true);
				}
			}
		});
		buttonPane.add(showAgain);
	}

	private void addButtonPanel() {
		this.buttonPane = new JPanel();

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		if (prefs.getBoolean("elfcloud.show.tutorial", true)) {
			addCheckbox();
		}
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(close);
		add(buttonPane, BorderLayout.PAGE_END);
	}

	class SelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			if (!lsm.isSelectionEmpty()) {
				// Find out which indexes are selected.
				int minIndex = lsm.getMinSelectionIndex();
				int maxIndex = lsm.getMaxSelectionIndex();
				for (int i = minIndex; i <= maxIndex; i++) {
					if (lsm.isSelectedIndex(i)) {
						try {
							topicContent.setPage(BeaverGUI.class.getResource(topics.get(i).url));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			} 
		}	

	}

	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		if (BeaverGUI.getKeyList().size() == 0) {
			String defaultKeyDir = prefs.get("elfcloud.keydir", System.getProperty("user.home") + System.getProperty("file.separator") + "elfcloud");
			File file = null;
			int i = 0;
			while (true) {
				file = new File(defaultKeyDir, "elfcloudkey" + i + ".key");
				if (!file.exists()) {
					break;
				}
				i++;
			}

			HolviXMLKeyItem xmlKey;
			int maxKeyLength = Utils.getMaximumAvailableKeyLength();
			xmlKey = HolviKeyManager.generateKey(file.getAbsolutePath(), "Unique generated encryption key", maxKeyLength);
			prefs.put("selected.key", xmlKey.getKeyHash());
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(new Label("Your key manager did not contain any keys. A new key was generated automatically."));
			panel.add(new Label("The generated key is located in " + xmlKey.getPath()));
			JOptionPane.showMessageDialog(HelpWindow.this, panel, "A new encryption key was generated", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		
	}
	
	@Override
	public void windowDeactivated(WindowEvent arg0) {

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}
}
