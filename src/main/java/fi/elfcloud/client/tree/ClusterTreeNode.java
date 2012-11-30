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

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONException;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.sci.exception.ECException;

/**
 * Abstract super class nodes in {@link ClusterHierarchyModel}
 *
 */
public abstract class ClusterTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 2979408126264297257L;
	public ClusterTreeNode(Object object) {
		super(object);
	}
	public ClusterTreeNode() {
		super();
	}
	public abstract String toString();
	public abstract Object getElement();
	public abstract boolean isRoot();
	public abstract void remove() throws ECException, JSONException, IOException;
	public abstract String getName();
	public abstract int getParentId();
	public abstract JPopupMenu popupMenu(BeaverGUI gui);
	public abstract JDialog informationDialog();
}
