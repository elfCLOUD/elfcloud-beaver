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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONException;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.dialog.DownloadDialog;
import fi.elfcloud.client.dialog.UploadDialog;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.container.Vault;
import fi.elfcloud.sci.exception.ECClientException;
import fi.elfcloud.sci.exception.ECException;

/**
 * Model for {@link ClusterTree} containing {@link DataItemNode}s and {@link ClusterNode} as elements.
 *
 */
public class ClusterHierarchyModel extends DefaultTreeModel {
	private static final long serialVersionUID = 3158753260522896349L;
	private ClusterNode homeNode;
	private BeaverGUI gui;

	public ClusterHierarchyModel(ClusterNode treenode, BeaverGUI gui) throws ECException, IOException {
		super(treenode);
		homeNode = treenode;
		this.gui = gui;
		populateRoot();
	}

	public ClusterHierarchyModel(TreeNode treenode, boolean bool) {
		super(treenode, bool);
	}

	@Override
	public boolean isLeaf(Object object) {
		if (object instanceof DataItemNode) {
			return true;
		} 
		return false;
	}

	public boolean isHomeNode(ClusterTreeNode node) {
		if (node == homeNode) {
			return true;
		}
		return false;
	}

	public void loadChildren(final ClusterNode node) throws IOException {
		node.removeAllChildren();
		Cluster[] clusters = null;
		DataItem[] dataitems = null;
		try {
			HashMap<String, Object[]> elements = node.getElement().getElements();
			clusters = (Cluster[]) elements.get("clusters"); //$NON-NLS-1$
			dataitems = (DataItem[]) elements.get("dataitems"); //$NON-NLS-1$
			for (Cluster cluster: clusters) {
				node.add(new ClusterNode(cluster));
			}
			for (DataItem dataitem: dataitems) {
				dataitem.setParentId(node.getElement().getId());
				node.add(new DataItemNode(dataitem));
			}
		} catch (ECException e) {
			gui.handleException(e);
		}	
		nodeStructureChanged(node);
	}

	public void populateRoot() throws ECException, IOException {
		nodeStructureChanged(homeNode);
		homeNode.removeAllChildren();
		for (Vault vault: gui.getVaults()) {
			ClusterNode node = new ClusterNode(vault);
			homeNode.add(node);
		}
		nodeStructureChanged(homeNode);
	}


	public void removeSelected(TreePath[] paths) {
		if (paths.length == 1) {
			ClusterTreeNode node = (ClusterTreeNode)paths[0].getLastPathComponent();
			String dialogTitle = Messages.getString("ClusterHierarchyModel.title_vault_remove_dialog"); //$NON-NLS-1$

			if (node instanceof DataItemNode) {
				dialogTitle = Messages.getString("ClusterHierarchyModel.title_dataitem_remove_dialog"); //$NON-NLS-1$
			} else if (node.getParentId() > 0) {
				dialogTitle = Messages.getString("ClusterHierarchyModel.title_cluster_remove_dialog"); //$NON-NLS-1$
			}

			int n = JOptionPane.showConfirmDialog(
					null,
					Messages.getString("ClusterHierarchyModel.dialog_remove_question") + node.getName() + "?", //$NON-NLS-1$ //$NON-NLS-2$
					dialogTitle,
					JOptionPane.YES_NO_OPTION);
			switch (n) {
			case JOptionPane.YES_OPTION:
				removePaths(paths);
				break;
			default: break;
			}
		} else if (paths.length > 1){
			int n = JOptionPane.showConfirmDialog(
					null,
					Messages.getString("ClusterHierarchyModel.dialog_remove_multiple_items_question"), //$NON-NLS-1$
					Messages.getString("ClusterHierarchyModel.dialog_remove_multiple_items_title_1") + Integer.toString(paths.length) + Messages.getString("ClusterHierarchyModel.dialog_remove_multiple_items_title_2"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_OPTION);
			switch (n) {
			case JOptionPane.YES_OPTION:
				removePaths(paths);
				break;
			default: break;
			}
		}
	}

	public void removePaths(final TreePath[] paths) {
		Thread worker = new Thread() {
			public void run() {
				ClusterTreeNode node;
				try {
					for (int i = 0; i < paths.length; i++) {
						node = (ClusterTreeNode)paths[i].getLastPathComponent();
						node.remove();
						removeNodeFromParent(node);
					}
				} catch (ECException e) {
					gui.handleException(e);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}}
		};
		worker.start();
	}

	public void downloadPaths(TreePath[] paths, final File destination) {
		ClusterTreeNode node;
		DownloadDialog dialog = new DownloadDialog();
		if (paths.length == 1) {
			node = (ClusterTreeNode)paths[0].getLastPathComponent();
			if (node instanceof DataItemNode) {
				dialog.addFile((DataItem)node.getElement(), destination);
			} else if (node instanceof ClusterNode) {
				File clusterFolder = new File(destination.getPath(), node.getName());
				dialog.addFile((Cluster)node.getElement(), clusterFolder);
			}
		} else {
			for (int i = 0; i < paths.length; i++) {
				node = (ClusterTreeNode)paths[i].getLastPathComponent();
				if (node instanceof DataItemNode) {
					File f = new File(destination, node.getName());
					dialog.addFile((DataItem)node.getElement(), f);
				} else if (node instanceof ClusterNode) {
					File f = new File(destination, node.getName());
					dialog.addFile((Cluster)node.getElement(), f);
				}
			}
		}
		dialog.pack();
		dialog.setVisible(true);
		dialog.startTask();
	}

	public void downloadDataItem(DataItemNode node, File destination) {
		DataItem di = (DataItem) node.getElement();
		DownloadDialog dialog = new DownloadDialog();
		dialog.addFile(di, destination);
		dialog.pack();
		dialog.setVisible(true);
		dialog.startTask();
	}

	public void addNode(DefaultMutableTreeNode root, Cluster c) {
		ClusterNode clusterNode = new ClusterNode(c);
		insertNodeInto(clusterNode, root, root.getChildCount());
	}

	public void previous() {
		ClusterNode root = (ClusterNode) getRoot();
		TreeNode parent = root.getParent();
		if (parent != null) {
			setRoot(parent);
			root.removeAllChildren();
			gui.updateTitle(parent.toString());
			gui.allowUpload(parent != homeNode);
		}
	}

	public void home() throws ECException, IOException {
		this.homeNode.removeAllChildren();
		populateRoot();
		setRoot(this.homeNode);
		gui.updateTitle(this.homeNode.toString());
		gui.allowUpload(false);
	}

	public void storeData(File[] files) {
		ClusterNode node = (ClusterNode) getRoot();
		if (!gui.confirmSend()) {
			return;
		}
		UploadDialog dialog = new UploadDialog(gui, node);
		for (File file: files) {
			dialog.addFile(file);
		}
		dialog.startTask();
	}

	public void storeData(List<File> filelist) {
		ClusterNode node = (ClusterNode) getRoot();
		if (!gui.confirmSend()) {
			return;
		}
		final Iterator<File> i = filelist.iterator();
		UploadDialog dialog = new UploadDialog(gui, node);
		while (i.hasNext()) {
			File f = (File) i.next();
			dialog.addFile(f);
		}
		
		dialog.startTask();
	}
	
	public void fetchData(final File destination, final TreePath[] paths) {
		Thread worker = new Thread() {
			public void run() {
				downloadPaths(paths, destination);
			}
		};
		worker.start();
	}

	public void relocateDataItems(Cluster destination, TreePath[] paths) {
		try {
			HashMap<String, Object[]> elements = destination.getElements();
			DataItem[] dataitems = (DataItem[]) elements.get("dataitems"); //$NON-NLS-1$
			Cluster[] clusters = (Cluster[]) elements.get("clusters"); //$NON-NLS-1$
			for (TreePath path : paths) {
				ClusterTreeNode node = (ClusterTreeNode) path.getLastPathComponent();
				
				if (!(node instanceof DataItemNode)) {
					continue;
				}
				DataItem di = (DataItem) node.getElement();
				String name = di.getName();
					for (DataItem dataitem: dataitems) {
						if (dataitem.getName().equals(di.getName())) {
							name = renameDataItem(name);
							break;
						}
					}
					for (Cluster cluster: clusters) {
						if (cluster.getName().equals(name)) {
							name = renameDataItem(name);
							break;
						}
					}
					if (name != null) {
						relocateDataItem(di, destination.getId(), name);
					}
			} 
		} catch (ECException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void relocateDataItem(DataItem di, int clusterId, String name) {
		try {
			di.relocate(clusterId, name);
		} catch (ECClientException e) {
			gui.handleException(e);
		} catch (ECException e) {
			if (e.getId() == 404) {
				name = renameDataItem(name);
				if (name != null) {
					relocateDataItem(di, clusterId, name);
				}
			} else {
				gui.handleException(e);
			}
		}
	}
	
	private String renameDataItem(String name) {
		name = (String)JOptionPane.showInputDialog(
				null,
				Messages.getString("ClusterHierarchyModel.dialog_rename_dataitem_text_1") + //$NON-NLS-1$
						Messages.getString("ClusterHierarchyModel.dialog_rename_dataitem_text_2") + name, //$NON-NLS-1$
						Messages.getString("ClusterHierarchyModel.dialog_rename_dataitem_title"), //$NON-NLS-1$
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						name);
		return name;
	}
	
	public void fetchAll(final File destination) {
		ClusterNode node = (ClusterNode) getRoot();
		if (node.getChildCount() >= 0) {
			TreePath[] paths = new TreePath[node.getChildCount()];
			for (int i=0; i<node.getChildCount(); i++) {
				ClusterTreeNode child = (ClusterTreeNode) node.getChildAt(i);
				paths[i] = new TreePath(child.getPath());
			}
			downloadPaths(paths, destination);
		}
	}

	public void refresh() throws IOException, ECException {
		ClusterNode node = (ClusterNode) getRoot();
		if (node == homeNode) {
			populateRoot();
		} else {
			loadChildren(node);
		}
	}

	public void refresh(ClusterNode node) throws IOException, ECException {
		if (node == getRoot()) {
			refresh();
		}
	}
	public ClusterTreeNode getHomeNode() {
		return this.homeNode;
	}

}

