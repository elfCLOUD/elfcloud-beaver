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
import java.awt.Desktop;
import java.awt.Label;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
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
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.XMLKeyItem;
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
	private static HelpWindow helpWindow;
	private static final long serialVersionUID = 559978795029545851L;
	private static final String title = Messages.getString("HelpWindow.window_title"); //$NON-NLS-1$
	private ArrayList<Topic> topics = new ArrayList<Topic>();
	private JEditorPane topicContent;
	private Preferences prefs;
	private JCheckBox showAgain;
	private JPanel buttonPane;
	
	private HelpWindow() {
		setTitle(BeaverGUI.titlePrefix + title);
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		prefs = BeaverGUI.getPreferences();
		topics.add(new Topic(Messages.getString("HelpWindow.topic_welcome"), Messages.getString("HelpWindow.content_welcome"))); //$NON-NLS-1$ //$NON-NLS-2$
		topics.add(new Topic(Messages.getString("HelpWindow.topic_encryption"), Messages.getString("HelpWindow.content_encryption"))); //$NON-NLS-1$ //$NON-NLS-2$
		topics.add(new Topic(Messages.getString("HelpWindow.topic_folders"), Messages.getString("HelpWindow.content_folders"))); //$NON-NLS-1$ //$NON-NLS-2$
		topics.add(new Topic(Messages.getString("HelpWindow.topic_uploading"), Messages.getString("HelpWindow.content_uploading"))); //$NON-NLS-1$ //$NON-NLS-2$
		topics.add(new Topic(Messages.getString("HelpWindow.topic_downloading"), Messages.getString("HelpWindow.content_downloading"))); //$NON-NLS-1$ //$NON-NLS-2$
		topics.add(new Topic(Messages.getString("HelpWindow.topic_license"), Messages.getString("HelpWindow.content_license")));  //$NON-NLS-1$ //$NON-NLS-2$
		setLayout(new BorderLayout());
		addHelpPanel();
		addButtonPanel();
		setPreferredSize(new Dimension(980, 500));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);
		pack();

	}
	public static HelpWindow getInstance() {
		if (helpWindow == null) {
			helpWindow = new HelpWindow();
		}
		helpWindow.setVisible(true);
		return helpWindow;
	}
	private void addHelpPanel() {
		JPanel contentPanel = new JPanel();
		SpringLayout layout = new SpringLayout();
		contentPanel.setLayout(layout);
		DefaultListModel listModel = new DefaultListModel();

		for (Topic t: topics) {
			listModel.addElement(t.title);
		}

		JList topicList = new JList(listModel);
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
		styles.addRule("body {margin-left: 10px; margin-bottom: 50px;}"); //$NON-NLS-1$
		styles.addRule("p {margin-top: 5px;"); //$NON-NLS-1$
		styles.addRule("li {list-style-type: none;}"); //$NON-NLS-1$
		styles.addRule("img {border: 2px;"); //$NON-NLS-1$
		styles.addRule("h3 {margin-top: 10px; margin-bottom: 10px;"); //$NON-NLS-1$
		try {
			this.topicContent.setPage(BeaverGUI.class.getResource(Messages.getString("HelpWindow.content_default"))); //$NON-NLS-1$
			this.topicContent.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent event) {
					HyperlinkEvent.EventType type = event.getEventType(); 
					if (type == HyperlinkEvent.EventType.ACTIVATED) 
					{ 
						Desktop desktop = Desktop.getDesktop();
						try {
							if (event.getURL().toString().startsWith("https:") || event.getURL().toString().startsWith("http:") ) { //$NON-NLS-1$ //$NON-NLS-2$
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
		showAgain = new JCheckBox(Messages.getString("HelpWindow.checkbox_show_again")); //$NON-NLS-1$
		if (prefs.getBoolean("elfcloud.show.tutorial", true)) { //$NON-NLS-1$
			showAgain.setSelected(false);
		}
		showAgain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (showAgain.isSelected()) {
					prefs.putBoolean("elfcloud.show.tutorial", false); //$NON-NLS-1$
				} else {
					prefs.putBoolean("elfcloud.show.tutorial", true); //$NON-NLS-1$
				}
			}
		});
		buttonPane.add(showAgain);
	}

	private void addButtonPanel() {
		this.buttonPane = new JPanel();

		JButton close = new JButton(Messages.getString("HelpWindow.button_close")); //$NON-NLS-1$
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		if (prefs.getBoolean("elfcloud.show.tutorial", true)) { //$NON-NLS-1$
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
			String defaultKeyDir = BeaverGUI.getKeyDirectoryPath();
			File file = null;
			int i = 0;
			while (true) {
				file = new File(defaultKeyDir, "elfcloudkey" + i + ".key"); //$NON-NLS-1$ //$NON-NLS-2$
				if (!file.exists()) {
					break;
				}
				i++;
			}

			XMLKeyItem xmlKey;
			int maxKeyLength = Utils.getMaximumAvailableKeyLength();
			try {
				xmlKey = KeyAdditionDialog.generateKey(file, Messages.getString("HelpWindow.description_generated_key"), maxKeyLength); //$NON-NLS-1$
				prefs.put("selected.key", xmlKey.getKeyHash()); //$NON-NLS-1$
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new Label(Messages.getString("HelpWindow.key_generated_message"))); //$NON-NLS-1$
				panel.add(new Label(Messages.getString("HelpWindow.key_generated_message_2") + xmlKey.getPath())); //$NON-NLS-1$
				JOptionPane.showMessageDialog(HelpWindow.this, panel, Messages.getString("HelpWindow.key_generated_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
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
