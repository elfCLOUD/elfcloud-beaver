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

package fi.elfcloud.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

import fi.elfcloud.client.tree.ClusterHierarchyModel;
import fi.elfcloud.client.tree.ClusterNode;
import fi.elfcloud.client.tree.ClusterTree;

/**
 * Handles all copy and paste file operations in {@link BeaverGUI} instance.
 *
 */
public class DropHandler extends TransferHandler {
	private static final long serialVersionUID = -6611620362488703404L;

	public DropHandler() {
	}

	@SuppressWarnings("rawtypes")
	public boolean importData(JComponent comp, Transferable t) {
		if (!(comp instanceof JTree)) {
			return false;
		}

		if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		ClusterTree tree = (ClusterTree) comp;
		final ClusterHierarchyModel model = (ClusterHierarchyModel) tree.getModel();
		final ClusterNode root = (ClusterNode) model.getRoot();
		if (root == model.getHomeNode()) {
			return false;
		}
		List data = null;
		try {
			data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		model.storeData(data);
		return true;
	}

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		if (comp instanceof JTree) {
			for (int i = 0; i < transferFlavors.length; i++) {
				if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
