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

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.HTMLEscapedJLabel;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.XMLKeyItem;
import fi.elfcloud.client.SpringUtilities;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.Utils;

public class DataItemInformationDialog extends JDialog {
	private static final long serialVersionUID = 4132565251099958709L;

	public DataItemInformationDialog(DataItem dataItem) {
		setTitle(Messages.getString("DataItemInformationDialog.window_title")); //$NON-NLS-1$
		setResizable(false);
		JPanel contentPanel = new JPanel(new SpringLayout());
		int rows = 8;
		HashMap<String, String> metaMap = Utils.metaToMap(dataItem.getMeta());
		JLabel label = new JLabel();
		label = new JLabel(Messages.getString("DataItemInformationDialog.label_name"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);

		JLabel labelContents = new HTMLEscapedJLabel(dataItem.getName());
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		if (metaMap.containsKey("DSC") && metaMap.get("DSC").length() > 0) { //$NON-NLS-1$ //$NON-NLS-2$
			label = new JLabel(Messages.getString("DataItemInformationDialog.label_description"), JLabel.TRAILING); //$NON-NLS-1$
			contentPanel.add(label);
			labelContents = new HTMLEscapedJLabel(metaMap.get("DSC")); //$NON-NLS-1$
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);
		} else {
			rows--;
		}
		
		if (metaMap.containsKey("TGS") && metaMap.get("TGS").length() > 0) { //$NON-NLS-1$ //$NON-NLS-2$
			label = new JLabel(Messages.getString("DataItemInformationDialog.label_tags"), JLabel.TRAILING); //$NON-NLS-1$
			contentPanel.add(label);
			String[] tags = (metaMap.get("TGS") != null ? metaMap.get("TGS").split(","): new String[0]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String tagString = ""; //$NON-NLS-1$
			for (int i=0; i<tags.length; i++) {
				tagString += (i > 0 ? ", " + tags[i] : tags[i]); //$NON-NLS-1$
			}
			labelContents = new HTMLEscapedJLabel(tagString);
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);
		} else {
			rows--;
		}
		label = new JLabel(Messages.getString("DataItemInformationDialog.label_size"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Utils.humanReadableByteCount(dataItem.getKeyLength(), true));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel(Messages.getString("DataItemInformationDialog.label_last_modified"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(dataItem.getLastModifiedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel(Messages.getString("DataItemInformationDialog.label_last_accessed"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(dataItem.getLastAccessedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel(Messages.getString("DataItemInformationDialog.label_encryption_mode"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(metaMap.get("ENC")); //$NON-NLS-1$
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		if (!metaMap.get("ENC").equalsIgnoreCase("NONE") && metaMap.containsKey("KHA")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			rows++;
			label = new JLabel(Messages.getString("DataItemInformationDialog.label_key_hash"), JLabel.TRAILING); //$NON-NLS-1$
			contentPanel.add(label);
			labelContents = new JLabel(metaMap.get("KHA")); //$NON-NLS-1$
			XMLKeyItem keyItem = BeaverGUI.findKeyFromList(metaMap.get("KHA")); //$NON-NLS-1$
			ImageIcon icon = null;
			if (keyItem != null) {
				icon = new ImageIcon(BeaverGUI.class.getResource("icons/lock_ok_16x16.png")); //$NON-NLS-1$
				labelContents.setToolTipText(Messages.getString("DataItemInformationDialog.encryption_key_description_available")); //$NON-NLS-1$
			} else {
				icon = new ImageIcon(BeaverGUI.class.getResource("icons/lock_error_16x16.png")); //$NON-NLS-1$
				labelContents.setToolTipText(Messages.getString("DataItemInformationDialog.encryption_key_description_unavailable")); //$NON-NLS-1$
			}
			labelContents.setIcon(icon);
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);

			if (keyItem != null) {
				rows++;
				label = new JLabel(Messages.getString("DataItemInformationDialog.label_key_description"), JLabel.TRAILING); //$NON-NLS-1$
				contentPanel.add(label);
				labelContents = new HTMLEscapedJLabel(keyItem.getDescription());
				label.setLabelFor(labelContents);
				contentPanel.add(labelContents);
			}
		}
		label = new JLabel(Messages.getString("DataItemInformationDialog.label_container_id"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Integer.toString(dataItem.getParentId()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		SpringUtilities.makeCompactGrid(contentPanel,
				rows, 2, 
				6, 6,        
				6, 6);
		setContentPane(contentPanel);
		pack();
		setLocationByPlatform(true);
	}
}
