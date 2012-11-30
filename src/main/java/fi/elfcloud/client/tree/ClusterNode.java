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

package fi.elfcloud.client.tree;

import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.json.JSONException;
import org.apache.commons.lang3.StringEscapeUtils;
import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.dialog.ClusterInformationDialog;
import fi.elfcloud.sci.User;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.container.Vault;
import fi.elfcloud.sci.exception.ECException;

/**
 * Node representing a single {@link Cluster}.
 *
 */
public class ClusterNode extends ClusterTreeNode {
	private static final long serialVersionUID = 5192486455209305678L;
	private Cluster object;
	private boolean isRoot;

	public ClusterNode(Cluster cluster) {
		super(cluster);
		this.object = cluster;
		this.isRoot = false;
		this.allowsChildren = true;
	}

	public ClusterNode() {
		super();
		this.object = null;
		this.isRoot = true;
		this.allowsChildren = true;
	}

	@Override
	public String toString() {
		String name = "<html>";
		if (object == null) {
			return ""; //$NON-NLS-1$
		}
		name += StringEscapeUtils.escapeHtml3(object.getName());
		ClusterNode rootNode;
		if (getPath().length > 1) {
			rootNode = (ClusterNode) getPath()[1];
			if (rootNode.getElement() instanceof Vault) {
				Vault vault = (Vault) rootNode.getElement(); // Vault
				if (vault != null) {
					name += "<font color='#717171'>";
					name += StringEscapeUtils.escapeHtml3(createOwnerInfo(vault));
					name += "</font>";
				}
			}
		}
		name += "</html>";
		return name;
	}

	private String createOwnerInfo(Vault vault) {
		String text = "";
		if (vault.getOwner().getAccount().getId() != BeaverGUI.getClient().getCurrentUser().getAccount().getId()) {
			text += " (owner: ";
			if (vault.getOwner().getAccount().getAccountType().equals("corporate")) {
				text += (vault.getOwner() != null ? vault.getOwner().getAccount().getCompanyName(): "");
				text += " ["; 
				text += createUserInfo(vault.getOwner());
				text += "]";
			} else {
				text += createUserInfo(vault.getOwner());
			}
			text += " [ext])";
		}
		return text;
	}

	private String createUserInfo(User user) {
		String text = "";
		String name = (user.getFirstname().trim().length() > 0 ? user.getFirstname().trim() + " ": "");
		name += (user.getLastname().trim().length() > 0 ? user.getLastname().trim(): "");
		if (name.trim().length() > 0) {
			text += name.trim();
			text += " (" + user.getName().trim() + ")";
		} else {
			text += user.getName().trim();
		}
		return text;
	}
	@Override
	public Cluster getElement() {
		return this.object;
	}

	@Override
	public boolean isRoot() {
		return isRoot;
	}

	@Override
	public void remove() throws ECException, JSONException, IOException {
		this.object.remove();
	}

	@Override
	public String getName() {
		return object.getName();
	}

	@Override
	public int getParentId() {
		return object.getParentId();
	}

	@Override
	public JPopupMenu popupMenu(BeaverGUI gui) {
		JPopupMenu popup = new JPopupMenu();
		populatePopupMenu(popup, gui);
		return popup;
	}

	@Override
	public boolean isLeaf() {
		if (this.object.getChildCount() > 0) {
			return false;
		}
		return true;
	}

	public JDialog informationDialog() {
		return new ClusterInformationDialog(object);
	}

	private void populatePopupMenu(JPopupMenu menu, BeaverGUI gui) {
		JMenuItem item;
		item = new JMenuItem(Messages.getString("ClusterNode.popup_modify")); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_MODIFY));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/modify_rename16.png"))); //$NON-NLS-1$
		menu.add(item);

		item = new JMenuItem(Messages.getString("ClusterNode.popup_information")); //$NON-NLS-1$
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/info_file16.png"))); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_INFORMATION));
		item.addActionListener(gui);
		menu.add(item);
		menu.addSeparator();

		item = new JMenuItem(Messages.getString("ClusterNode.popup_delete")); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_DELETE));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/delete16.png"))); //$NON-NLS-1$
		menu.add(item);
	}
}
