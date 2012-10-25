package fi.elfcloud.client.tree;

import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONException;


import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.sci.exception.HolviException;

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
	public abstract void remove() throws HolviException, JSONException, IOException;
	public abstract String getName();
	public abstract int getParentId();
	public abstract JPopupMenu popupMenu(BeaverGUI gui);
	public abstract JDialog informationDialog();
}
