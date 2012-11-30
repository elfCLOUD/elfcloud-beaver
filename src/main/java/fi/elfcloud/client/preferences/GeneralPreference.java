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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;

public class GeneralPreference extends Preference {
	private LanguageComboBox language;
	private JTextField downloadField;
	
	public GeneralPreference() {
		super();
		preferenceLabel = new PreferenceLabel(new ImageIcon(BeaverGUI.class.getResource("icons/general.png")), this); //$NON-NLS-1$
		preferenceLabel.setPreferredSize(new Dimension(70, 70));
		preferenceLabel.setBorder(null);
		preferenceLabel.setOpaque(true);
		preferenceLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		preferenceLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		preferenceLabel.setBackground(Color.WHITE);
		preferenceLabel.setText(Messages.getString("GeneralPreference.label_general")); //$NON-NLS-1$

		preferencePanel = new JPanel();
		preferencePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		preferencePanel.setLayout(new BoxLayout(preferencePanel, BoxLayout.PAGE_AXIS));
		JPanel languagePanel = new JPanel();
		languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.PAGE_AXIS));
		GridBagConstraints cs = new GridBagConstraints();

		language = new LanguageComboBox(Messages.getAvailableLocales());
		JLabel languageLabel = new JLabel(Messages.getString("GeneralPreference.interface_language")); //$NON-NLS-1$
		languagePanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("GeneralPreference.language_border_title"))); //$NON-NLS-1$
		PreferencePanelRow row = new PreferencePanelRow();
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0;
		row.add(languageLabel, cs);
		row.add(Box.createHorizontalStrut(3));
		cs.weightx = 1;
		row.add(language, cs);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, language.getPreferredSize().height+5));
		languagePanel.add(row);
		preferencePanel.add(languagePanel);

		JPanel directoryPanel = new JPanel();
		directoryPanel.setLayout(new BoxLayout(directoryPanel, BoxLayout.PAGE_AXIS));
		directoryPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("GeneralPreference.default_directory_border_title"))); //$NON-NLS-1$
		JLabel downloadLabel = new JLabel(Messages.getString("GeneralPreference.label_download_directory")); //$NON-NLS-1$
		this.downloadField = new JTextField(BeaverGUI.defaultDownloadDir.getAbsolutePath());
		this.downloadField.setEditable(false);
		JButton downloadDirSelect = new JButton(Messages.getString("GeneralPreference.button_browse")); //$NON-NLS-1$
		downloadDirSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(downloadField.getText());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JOptionPane.OK_OPTION) {
					downloadField.setText(fc.getSelectedFile().getAbsolutePath());
					saveDownloadDirectory();
				}
			}
		});
		
		row = new PreferencePanelRow();
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, downloadDirSelect.getPreferredSize().height));
		cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0;
		row.add(downloadLabel, cs);
		row.add(Box.createHorizontalStrut(3));
		cs.weightx = 1;
		row.add(downloadField, cs);
		cs.weightx = 0;
		row.add(downloadDirSelect, cs);
		directoryPanel.add(row);
		directoryPanel.add(Box.createVerticalStrut(10));
		preferencePanel.add(directoryPanel);
	}
	
	private void saveDownloadDirectory() {
		File file = new File(downloadField.getText());
		if (file.isDirectory()) {
			BeaverGUI.getPreferences().put("elfcloud.download.directory", file.getAbsolutePath()); //$NON-NLS-1$
		}
	}
	
	@Override
	public void save() {
		if (!language.getLocale().equals(Messages.getLocale())) {
			BeaverGUI.getPreferences().put("elfcloud.interface.language", language.getLocale().toString()); //$NON-NLS-1$
			Messages.setLocale(language.getLocale());
		}
		saveDownloadDirectory();
	}

	@Override
	public void load() {
	}

	class LanguageComboBox extends JComboBox {
		private static final long serialVersionUID = 4220547783994888233L;
		private Locale[] locales;

		public LanguageComboBox(Locale[] locales) {
			this.locales = locales;
			int n = 0;
			for (int i = 0; i < locales.length; i++) {
				addItem(locales[i].getDisplayLanguage(locales[i]));
				if (locales[i].equals(Messages.getLocale())) {
					setSelectedIndex(n);
					setSelectedItem(locales[n]);
				}
				n += 1;
			}
		}

		public Locale getLocale() {
			int selected = getSelectedIndex();
			if (selected == -1) {
				return null;
			}
			return locales[getSelectedIndex()];
		}
	}
}
