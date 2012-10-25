package fi.elfcloud.client.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.tree.ClusterNode;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.exception.HolviException;

public class RelocateDataItemDialog extends JDialog implements TreeExpansionListener {
	private static final long serialVersionUID = -8210635235934493067L;
	private BeaverGUI gui;
	private TreeModel model;
	private static final ImageIcon folderIcon = new ImageIcon(BeaverGUI.class.getResource("icons/folder.png"));
	private boolean move = false;
	private ClusterNode selectedNode = null;
	private JTree tree;
	
	public RelocateDataItemDialog(BeaverGUI gui, Cluster cluster) {
		setModal(true);
		setTitle("Select destination location");
		ClusterNode rootNode = new ClusterNode(cluster);
		
		this.gui = gui;
		this.model = new DefaultTreeModel(rootNode);
		populateRoot();
		this.tree = new JTree(model);
		tree.addTreeExpansionListener(this);
		tree.setRootVisible(true);
		tree.setShowsRootHandles(true);
		tree.setExpandsSelectedPaths(true);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(folderIcon);
		renderer.setClosedIcon(folderIcon);
		renderer.setOpenIcon(folderIcon);
		tree.setCellRenderer(renderer);
		JScrollPane treeView = new JScrollPane(tree);
		add(treeView, BorderLayout.CENTER);
		JButton button = new JButton("Select");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
				selectedNode = (ClusterNode) tree.getSelectionPath().getLastPathComponent();
				move = true;
				dispose();
				} catch (NullPointerException exc) {
					JOptionPane.showMessageDialog(RelocateDataItemDialog.this, "Please select the destination first.", "Invalid selection", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		add(button, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 450));
		pack();
		tree.expandRow(0);
	}

	public int showDialog() {
		setVisible(true);
		if (move) {
			return JOptionPane.OK_OPTION;
		}
		else {
			return JOptionPane.CANCEL_OPTION;
		}
	}
	
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
	}
	
	private void populateRoot() {
		ClusterNode root = (ClusterNode)model.getRoot();
		try {
			Cluster[] clusters = (Cluster[])root.getElement().getChildren();
			for (Cluster c : clusters) {
				ClusterNode newNode = new ClusterNode(c);
				((DefaultTreeModel) model).insertNodeInto(newNode, root, root.getChildCount());
			}
		} catch (HolviException e) {
			gui.handleException(e);
		} catch (IOException e) {
			gui.handleException(e);
		}
	}
	
	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		ClusterNode node;
        node = (ClusterNode)(event.getPath().getLastPathComponent());
        node.removeAllChildren();
        try {
			Cluster[] clusters = node.getElement().getChildren();
			
			for (Cluster c : clusters) {
				ClusterNode newNode = new ClusterNode(c);
				node.insert(newNode, node.getChildCount());
			}
		} catch (HolviException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        ((DefaultTreeModel) model).nodeStructureChanged(node);
	}
	
	public Cluster getSelection() {
		return selectedNode.getElement();
	}

}
