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
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;

public class TransferPreference extends Preference {
	JCheckBox autoClose = new JCheckBox(Messages.getString("TransferPreference.checkbox_transfer_window_autoclose")); //$NON-NLS-1$
	JCheckBox stopOperation = new JCheckBox(Messages.getString("TransferPreference.checkbox_transfer_stop_on_error")); //$NON-NLS-1$

	public TransferPreference() {
		super();
		preferenceLabel = new PreferenceLabel(new ImageIcon(BeaverGUI.class.getResource("icons/transfers.png")), this); //$NON-NLS-1$
		preferenceLabel.setPreferredSize(new Dimension(70, 70));
		preferenceLabel.setBorder(null);
		preferenceLabel.setOpaque(true);
		preferenceLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		preferenceLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		preferenceLabel.setBackground(Color.WHITE);
		preferenceLabel.setText(Messages.getString("TransferPreference.option_transfers")); //$NON-NLS-1$

		preferencePanel = new JPanel();
		preferencePanel.setLayout(new BoxLayout(preferencePanel, BoxLayout.PAGE_AXIS));
		preferencePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		preferencePanel.add(createTransferPanel());
	}
	
	private Component createTransferPanel() {
		JPanel transferPanel = new JPanel();
		transferPanel.setLayout(new BoxLayout(transferPanel, BoxLayout.PAGE_AXIS));
		transferPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("TransferPreference.border_title_transfer_preferences"))); //$NON-NLS-1$

		GridBagConstraints cs = new GridBagConstraints();
		PreferencePanelRow row = new PreferencePanelRow();
		cs.fill = GridBagConstraints.HORIZONTAL;
		autoClose.setSelected(BeaverGUI.getPreferences().getBoolean("elfcloud.transfer.window.autoclose", true)); //$NON-NLS-1$
		row.add(autoClose, cs);
		cs.weightx = 1;
		row.add(Box.createHorizontalGlue(), cs);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, autoClose.getPreferredSize().height+5));
		transferPanel.add(row);
		
		cs = new GridBagConstraints();
		row = new PreferencePanelRow();
		cs.fill = GridBagConstraints.HORIZONTAL;
		stopOperation.setSelected(BeaverGUI.getPreferences().getBoolean("elfcloud.transfer.stop.on.error", false)); //$NON-NLS-1$
		row.add(stopOperation, cs);
		cs.weightx = 1;
		row.add(Box.createHorizontalGlue(), cs);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, stopOperation.getPreferredSize().height+5));
		transferPanel.add(row);
		
		return transferPanel;
	}

	@Override
	public void save() {
		BeaverGUI.getPreferences().putBoolean("elfcloud.transfer.window.autoclose", autoClose.isSelected()); //$NON-NLS-1$
		BeaverGUI.getPreferences().putBoolean("elfcloud.transfer.stop.on.error", stopOperation.isSelected()); //$NON-NLS-1$
	}

	@Override
	public void load() {

	}

}
