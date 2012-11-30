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

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import fi.elfcloud.client.HTMLEscapedJLabel;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.SpringUtilities;
import fi.elfcloud.sci.Utils;
import fi.elfcloud.sci.container.Cluster;

public class ClusterInformationDialog extends JDialog {
	private static final long serialVersionUID = 4467779036913817740L;

	public ClusterInformationDialog(Cluster cluster) {
		setTitle(Messages.getString("ClusterInformationDialog.window_title")); //$NON-NLS-1$
		int rows = 5;
		setResizable(false);
		JPanel contentPanel = new JPanel(new SpringLayout());
		JLabel label = new JLabel();
		label = new JLabel(Messages.getString("ClusterInformationDialog.label_name"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);

		JLabel labelContents = new HTMLEscapedJLabel(cluster.getName());
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel(Messages.getString("ClusterInformationDialog.label_last_modified"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(cluster.getLastModifiedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel(Messages.getString("ClusterInformationDialog.label_last_accessed"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(cluster.getLastAccessedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel(Messages.getString("ClusterInformationDialog.label_container_id"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		labelContents = new JLabel(Integer.toString(cluster.getId()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);
		
		if (cluster.getParentId() > 0) {
			rows++;
			label = new JLabel(Messages.getString("ClusterInformationDialog.label_parent_id"), JLabel.TRAILING); //$NON-NLS-1$
			contentPanel.add(label);
			labelContents = new JLabel(Integer.toString(cluster.getParentId()));
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);
		}
		
		label = new JLabel(Messages.getString("ClusterInformationDialog.label_permissions"), JLabel.TRAILING); //$NON-NLS-1$
		contentPanel.add(label);
		String permissionString = ""; //$NON-NLS-1$
		String[] permissions = cluster.getPermissions();
		for (int i=0; i<permissions.length; i++) {
			permissionString += (i > 0 ? ", " + permissions[i] : permissions[i]); //$NON-NLS-1$
		}
		labelContents = new JLabel(permissionString);
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
