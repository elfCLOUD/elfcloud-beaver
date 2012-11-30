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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;

public class SecurityPreference extends Preference {
	JTextField hostField = new JTextField();
	JTextField portField = new JTextField();
	JRadioButton disableButton = new JRadioButton(Messages.getString("SecurityPreference.radio_button_disable")); //$NON-NLS-1$
	JRadioButton httpProxyButton = new JRadioButton(Messages.getString("SecurityPreference.radio_button_https")); //$NON-NLS-1$
	JRadioButton socksProxyButton = new JRadioButton(Messages.getString("SecurityPreference.radio_button_socks")); //$NON-NLS-1$
	JCheckBox uploadCheckbox = new JCheckBox(Messages.getString("SecurityPreference.checkbox_allow_upload_unencrypted")); //$NON-NLS-1$
	
	public SecurityPreference() {
		super();
		preferenceLabel = new PreferenceLabel(new ImageIcon(BeaverGUI.class.getResource("icons/security.png")), this); //$NON-NLS-1$
		preferenceLabel.setPreferredSize(new Dimension(70, 70));
		preferenceLabel.setBorder(null);
		preferenceLabel.setOpaque(true);
		preferenceLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		preferenceLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		preferenceLabel.setBackground(Color.WHITE);
		preferenceLabel.setText(Messages.getString("SecurityPreference.option_security")); //$NON-NLS-1$
		
		preferencePanel = new JPanel();
		preferencePanel.setLayout(new BoxLayout(preferencePanel, BoxLayout.PAGE_AXIS));
		preferencePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		preferencePanel.add(createProxyPanel());
		preferencePanel.add(createUnencryptedUploadPanel());
	}
	
	private JPanel createUnencryptedUploadPanel() {
		JPanel uploadPanel = new JPanel();
		uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.PAGE_AXIS));
		uploadPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("SecurityPreference.border_title_uploading"))); //$NON-NLS-1$
		
		GridBagConstraints cs = new GridBagConstraints();
		PreferencePanelRow row = new PreferencePanelRow();
		cs.fill = GridBagConstraints.HORIZONTAL;
		uploadCheckbox.setSelected(BeaverGUI.getPreferences().getBoolean("elfcloud.allow.unencrypted.upload", false)); //$NON-NLS-1$
		row.add(uploadCheckbox, cs);
		cs.weightx = 1;
		row.add(Box.createHorizontalGlue(), cs);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, uploadCheckbox.getPreferredSize().height+5));
		uploadPanel.add(row);
		return uploadPanel;
	}

	private JPanel createProxyPanel() {
		JPanel proxyPanel = new JPanel();
		proxyPanel.setLayout(new BoxLayout(proxyPanel, BoxLayout.PAGE_AXIS));
		proxyPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("SecurityPreference.border_title_proxy_settings"))); //$NON-NLS-1$
		
		GridBagConstraints cs = new GridBagConstraints();
		PreferencePanelRow row = new PreferencePanelRow();
		JLabel proxyTypeLabel = new JLabel(Messages.getString("SecurityPreference.label_proxy_type")); //$NON-NLS-1$
		ButtonGroup proxyGroup = new ButtonGroup();
		proxyGroup.add(disableButton);
		proxyGroup.add(httpProxyButton);
		proxyGroup.add(socksProxyButton);
		cs.fill = GridBagConstraints.HORIZONTAL;
		row.add(proxyTypeLabel, cs);
		row.add(Box.createHorizontalStrut(3));
		row.add(disableButton, cs);
		row.add(httpProxyButton, cs);
		row.add(socksProxyButton, cs);
		
		cs.weightx = 1;
		row.add(Box.createHorizontalGlue(), cs);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, disableButton.getPreferredSize().height+5));
		proxyPanel.add(row);
		
		row = new PreferencePanelRow();
		JLabel hostLabel = new JLabel(Messages.getString("SecurityPreference.label_hostname")); //$NON-NLS-1$
		JLabel portLabel = new JLabel(Messages.getString("SecurityPreference.label_port")); //$NON-NLS-1$
		
		cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		row.add(hostLabel, cs);
		row.add(Box.createHorizontalStrut(3));
		cs.weightx = 1;
		row.add(hostField, cs);
		row.add(Box.createHorizontalStrut(5));
		cs.weightx = 0;
		row.add(portLabel, cs);
		row.add(Box.createHorizontalStrut(3));
		cs.weightx = 0.5;
		row.add(portField, cs);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, hostField.getPreferredSize().height+5));
		proxyPanel.add(row);
		
		disableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hostField.setEnabled(false);
				portField.setEnabled(false);
			}
		});
		httpProxyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hostField.setEnabled(true);
				portField.setEnabled(true);
			}
		});
		socksProxyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hostField.setEnabled(true);
				portField.setEnabled(true);
			}
		});
		int proxyMode = BeaverGUI.getPreferences().getInt("elfcloud.proxy.mode", 0);
		if ( proxyMode == 0) {
			disableButton.doClick();
		} else {
			hostField.setText(BeaverGUI.getPreferences().get("elfcloud.proxy.host", ""));
			portField.setText(BeaverGUI.getPreferences().get("elfcloud.proxy.port", ""));
			if (proxyMode == 1) {
				httpProxyButton.doClick();
			}
			if (proxyMode == 2) {
				socksProxyButton.doClick();
			}
		}
		
		return proxyPanel;
	}
	
	@Override
	public void save() {
		if (socksProxyButton.isSelected()) {
			System.setProperty("socksProxyHost", hostField.getText().trim()); //$NON-NLS-1$
			System.setProperty("socksProxyPort", portField.getText().trim()); //$NON-NLS-1$
			System.setProperty("https.proxyHost", ""); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("https.proxyPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
			BeaverGUI.getPreferences().put("elfcloud.proxy.host", hostField.getText().trim());
			BeaverGUI.getPreferences().put("elfcloud.proxy.port", portField.getText().trim());
			BeaverGUI.getPreferences().putInt("elfcloud.proxy.mode", 2);
		}
		if (httpProxyButton.isSelected()) {
			System.setProperty("https.proxyHost", hostField.getText().trim()); //$NON-NLS-1$
			System.setProperty("https.proxyPort", portField.getText().trim()); //$NON-NLS-1$
			System.setProperty("socksProxyHost", ""); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("socksProxyPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
			BeaverGUI.getPreferences().put("elfcloud.proxy.host", hostField.getText().trim());
			BeaverGUI.getPreferences().put("elfcloud.proxy.port", portField.getText().trim());
			BeaverGUI.getPreferences().putInt("elfcloud.proxy.mode", 1);
		}
		if (disableButton.isSelected()) {
			System.setProperty("https.proxyHost", ""); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("https.proxyPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("socksProxyHost", ""); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("socksProxyPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
			BeaverGUI.getPreferences().put("elfcloud.proxy.host", "");
			BeaverGUI.getPreferences().put("elfcloud.proxy.port", "");
			BeaverGUI.getPreferences().putInt("elfcloud.proxy.mode", 0);
		}
		BeaverGUI.getPreferences().putBoolean("elfcloud.allow.unencrypted.upload", uploadCheckbox.isSelected()); //$NON-NLS-1$
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub

	}

}
