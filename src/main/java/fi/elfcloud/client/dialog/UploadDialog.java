package fi.elfcloud.client.dialog;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import org.json.JSONException;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.tree.ClusterNode;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.HolviClient;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.exception.HolviEncryptionException;
import fi.elfcloud.sci.exception.HolviException;

/**
 * 
 *
 */
public class UploadDialog extends TransferFrame implements ActionListener, PropertyChangeListener  {
	private static final long serialVersionUID = 4392195188843021681L;
	private UploadTask task;
	private final Object[] options = {"Replace all", "Replace", "Skip", "Skip all"};

	/**
	 * 
	 *
	 */
	class UploadTask extends SwingWorker<Void, Void> {
		private ArrayList<File> fileArray = new ArrayList<File>();
		private HolviClient client;
		private Cluster cluster;
		private BeaverGUI gui;
		private ClusterNode node;
		private int fileCount = 0;
		private int filesSent = 0;
		private Long totalLength = 0L;
		private Long progress = 0L;
		private boolean autoClose = true;
		private String method = "new";
		private int replaceAnswer = 2; // skip one
		private boolean exists = false;
		int i;

		@Override
		protected Void doInBackground() {
			for (i = 0; i<fileArray.size(); i++) {
				if (task.isCancelled()) {
					break;
				}
				File file = fileArray.get(i);

				if (file.isFile()) {
					processFile(file, cluster, node);
				} else if (file.isDirectory()) {
					processDirectory(file, cluster, node);
				}
			}
			try {
				gui.getTreeModel().refresh(node);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		private void processFile(File file, Cluster parentCluster, ClusterNode parentNode) {
			FileInputStream fi = null;
			DataItem[] dataItemArray = null;
			try {
				dataItemArray = parentCluster.getDataItems(new String[] {file.getName()});
			} catch (HolviException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			DataItem dataItem;
			if (dataItemArray.length == 1) {
				exists = true;
				dataItem = dataItemArray[0];
				if (replaceAnswer != 0 && replaceAnswer != 3) {
					int result = JOptionPane.showOptionDialog(UploadDialog.this, 
							"Data item " + dataItem.getName() + " already exists, how to proceed?", "Replace " + dataItem.getName() + "?", 
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					switch (result) {
					case 0:
						method = "replace";
						replaceAnswer = result;
						break;
					case 1:
						method = "replace";
						break;
					case 2:
						method = "new";
						firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength());

						break;
					case 3:
						method = "new";
						replaceAnswer = result;
						firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength());
						break;
					default:
						break;
					}
				} else {
					if (replaceAnswer == 0) {
						method = "replace";
					}
					if (replaceAnswer == 3) {
						method = "new";
					}
					firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength());
				}
			} else {
				exists = false;
				method = "new";
				dataItem = new DataItem(client, file.getName(), parentCluster.getId());
			}

			try {
				fi = new FileInputStream(file);
				int j = filesSent + 1;
				filesSent++;
				taskOutput.append("Sending " + file.getName() + " (" + j + "/" + fileCount +") ... ");
				ProgressStream prs = new ProgressStream(fi, totalLength, this);
				prs.addPropertyChangeListener(UploadDialog.this);
				int endPosition = taskOutput.getDocument().getLength();
				Rectangle bottom = taskOutput.modelToView(endPosition);
				bottom.width = 0;
				taskOutput.scrollRectToVisible(bottom);
				if (!exists || method.equals("replace")) {
					if(sendFile(dataItem, method, prs)) {
						if (method.equals("new")) {
							dataItem.setKeyLength(file.length());
							gui.getTreeModel().refresh(parentNode);
						} 
					} else {
						autoClose = false;
					}
				} else {
					taskOutput.append("SKIP");
				}
				taskOutput.append("\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fi != null) {
					try {
						fi.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void processDirectory(File directory, Cluster parentCluster, ClusterNode parentNode) {
			File[] fileList = directory.listFiles();

			try {
				Cluster[] clusters = parentCluster.getChildren();
				Cluster cluster = null;
				for (Cluster c: clusters) {
					if (c.getName().equals(directory.getName())) {
						cluster = c;
						break;
					}
				}
				if (cluster == null) {
					taskOutput.append("Creating container " + directory.getName());
					cluster = BeaverGUI.getClient().addCluster(directory.getName(), parentCluster.getId());
					taskOutput.append(" OK\n");
				}

				ClusterNode dirNode = new ClusterNode(cluster);
				gui.getTreeModel().refresh(parentNode);

				for (File f: fileList) {
					if (task.isCancelled()) {
						break;
					}
					if (f.isFile()) {
						processFile(f, cluster, dirNode);
					} else {
						processDirectory(f, cluster, dirNode);
					}
				}
			} catch (HolviException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private boolean sendFile(DataItem dataItem, String method, InputStream is) {
			try {
				dataItem.storeData(method, is);
				if (!task.isCancelled()) {
					taskOutput.append("OK");
				}
				return true;
			} catch (HolviException e) {
				autoClose = false;
				taskOutput.append("[" + e.getMessage() + "] FAILED");
				if (e.getId() == 401) {
					try {
						if (method.equals("replace")) {
							dataItem.remove();
						}
					} catch (HolviException e1) {
					} catch (JSONException e1) {
					} catch (IOException e1) {
					}
					task.cancel(true);
				}
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				return false;
			} catch (InvalidKeyException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				new IllegalKeyDialog();
				return false;
			} catch (InvalidAlgorithmParameterException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				return false;
			} catch (HolviEncryptionException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public void done() {
			setProgress(100);

			if (!autoClose) {
				taskOutput.append("\nCould not send all files.");
			}
			if (!task.isCancelled()) {
				taskOutput.append("\nDone.");
			}
			if (autoClose) {
				dispose();
			}
			cancel.setText("Close");
			cancel.removeActionListener(UploadDialog.this);
			cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
		}

		public void addFile(File file) {
			if (file.isFile()) {
				countFile(file);
				fileArray.add(file);
			} else if (file.isDirectory()) {
				countDirectory(file);
				fileArray.add(file);
			}

		}

		private void countFile(File file) {
			totalLength += file.length();
			fileCount++;
		}

		private void countDirectory(File directory) {
			File[] fileList = directory.listFiles();
			for (File file: fileList) {
				if (file.isFile()) {
					countFile(file);
				} else if (file.isDirectory()) {
					countDirectory(file);
				}
			}
		}

		public void setup(BeaverGUI gui, ClusterNode node) {
			this.client = BeaverGUI.getClient();
			this.cluster = node.getElement();
			this.node = node;
			this.gui = gui;
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int value;
		if ("totalNumBytesRead" == evt.getPropertyName()){
			task.progress += (Long)evt.getNewValue() - (Long)evt.getOldValue();
			value = (int) ((task.progress.doubleValue()/task.totalLength.doubleValue())*100);
			progressBar.setValue(value);
			setTitle("Uploading file " + task.filesSent + " of " + task.fileCount + " - " + Integer.toString(value) + "%");
		}
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			setTitle("Uploading file " + task.filesSent + " of " + task.fileCount + " - " + Integer.toString(progress) + "%");
		} 		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(TransferFrame.CANCEL_ACTION)) {
			task.autoClose = false;
			task.cancel(true);
			taskOutput.append("\nCancelled!");
		}
	}

	public UploadDialog(BeaverGUI gui, ClusterNode root) {
		setTitle("Upload in progress...");
		progressBar.setIndeterminate(true);
		task = new UploadTask();
		task.addPropertyChangeListener(this);
		task.setup(gui, root);
		cancel.addActionListener(this);
		addWindowStateListener(new WindowStateListener() {

			@Override
			public void windowStateChanged(WindowEvent e) {
				switch (e.getID()) {
				case WindowEvent.WINDOW_CLOSING:
					task.cancel(true);
					break;
				default:
					break;
				}
			}
		});
		setVisible(true);
	}

	/**
	 * Add new file to be sent.
	 * @param file file to be sent in this {@link UploadTask}.
	 */
	public void addFile(File file) {
		task.addFile(file);
	}

	public void startTask() {
		task.execute();
	}
}
