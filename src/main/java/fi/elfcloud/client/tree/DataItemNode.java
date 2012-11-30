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
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.dialog.DataItemInformationDialog;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.Utils;
import fi.elfcloud.sci.exception.ECException;

/**
 * Node representing a single {@link DataItem}
 *
 */
public class DataItemNode extends ClusterTreeNode {
	private static final long serialVersionUID = -3712740684567040272L;
	private DataItem object;
	private HashMap<String, String> metaMap;

	public DataItemNode(DataItem dataitem) {
		super(dataitem);
		this.object = dataitem;
		this.setMetaMap(Utils.metaToMap(dataitem.getMeta()));
		this.allowsChildren = false;
		this.children = null;
	}

	@Override
	public String toString() {
		String displayName = "<html>";
		displayName += StringEscapeUtils.escapeHtml3(object.getName());
		displayName += " (" + Utils.humanReadableByteCount(object.getKeyLength(), true) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		displayName += (metaMap.get("DSC") != null && metaMap.get("DSC").length() > 0 ? " - " + StringEscapeUtils.escapeHtml3(metaMap.get("DSC")): ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		if (metaMap.get("TGS") != null && metaMap.get("TGS").length() > 0) { //$NON-NLS-1$ //$NON-NLS-2$
			String tags[] = metaMap.get("TGS").split(","); //$NON-NLS-1$ //$NON-NLS-2$
			displayName += " ["; //$NON-NLS-1$
			for (int i=0; i<tags.length; i++) {
				displayName += (i > 0 ? ", " + StringEscapeUtils.escapeHtml3(tags[i]) : StringEscapeUtils.escapeHtml3(tags[i])); //$NON-NLS-1$
			}
			displayName += "]"; //$NON-NLS-1$
		}
		displayName += "</html>";
		return displayName;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public void remove() throws ECException, JSONException, IOException {
		this.object.remove();
	}

	@Override
	public DataItem getElement() {
		return this.object;
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
	
	public JDialog informationDialog() {
		return new DataItemInformationDialog(object);
	}
	
	private void populatePopupMenu(JPopupMenu menu, BeaverGUI gui) {
		JMenuItem item;
		item = new JMenuItem(Messages.getString("DataItemNode.popup_download")); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_SAVE_ITEM));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/download16.png"))); //$NON-NLS-1$
		menu.add(item);

		item = new JMenuItem(Messages.getString("DataItemNode.popup_modify")); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_MODIFY));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/modify_rename16.png"))); //$NON-NLS-1$
		menu.add(item);
		
		item = new JMenuItem(Messages.getString("DataItemNode.popup_move")); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_MOVE));
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/move_file16.png"))); //$NON-NLS-1$
		item.addActionListener(gui);
		menu.add(item);
		
		item = new JMenuItem(Messages.getString("DataItemNode.popup_information")); //$NON-NLS-1$
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/info_file16.png"))); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_INFORMATION));
		item.addActionListener(gui);
		menu.add(item);
		
		menu.addSeparator();
		item = new JMenuItem(Messages.getString("DataItemNode.popup_delete")); //$NON-NLS-1$
		item.setActionCommand(Integer.toString(gui.ACTION_DELETE));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/delete16.png"))); //$NON-NLS-1$
		menu.add(item);
	}

	public HashMap<String, String> getMetaMap() {
		return metaMap;
	}

	public void setMetaMap(HashMap<String, String> metaMap) {
		this.metaMap = metaMap;
	}
}
