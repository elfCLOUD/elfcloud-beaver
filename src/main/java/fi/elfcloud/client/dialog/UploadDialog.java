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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.tree.ClusterNode;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.Client;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.container.Vault;
import fi.elfcloud.sci.exception.ECClientException;
import fi.elfcloud.sci.exception.ECEncryptionException;
import fi.elfcloud.sci.exception.ECException;

/**
 * 
 *
 */
public class UploadDialog extends TransferFrame implements ActionListener, PropertyChangeListener  {
	private static final long serialVersionUID = 4392195188843021681L;
	private UploadTask task;
	private final Object[] options = {Messages.getString("UploadDialog.button_replace_all"), Messages.getString("UploadDialog.button_replace"), Messages.getString("UploadDialog.button_skip"), Messages.getString("UploadDialog.button_skip_all")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * 
	 *
	 */
	class UploadTask extends SwingWorker<Void, Void> {
		private ArrayList<File> fileArray = new ArrayList<File>();
		private Client client;
		private Vault vault;
		private Cluster cluster;
		private BeaverGUI gui;
		private ClusterNode node;
		private int fileCount = 0;
		private int filesSent = 0;
		private Long totalLength = 0L;
		private Long progress = 0L;
		private String method = "new"; //$NON-NLS-1$
		private int replaceAnswer = 2; // skip one
		private boolean exists = false;
		private boolean allOk = true;
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
			return null;
		}

		private void processFile(File file, Cluster parentCluster, ClusterNode parentNode) {
			FileInputStream fi = null;
			DataItem[] dataItemArray = null;
			try {
				dataItemArray = parentCluster.getDataItems(new String[] {file.getName()});
			} catch (ECException e) {
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
							Messages.getString("UploadDialog.dataitem_exists_message_1") + dataItem.getName() + Messages.getString("UploadDialog.dataitem_exists_message_2"), Messages.getString("UploadDialog.dataitem_exists_title") + dataItem.getName() + "?",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					switch (result) {
					case 0:
						method = "replace"; //$NON-NLS-1$
						replaceAnswer = result;
						break;
					case 1:
						method = "replace"; //$NON-NLS-1$
						break;
					case 2:
						method = "new"; //$NON-NLS-1$
						firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength()); //$NON-NLS-1$
						break;
					case 3:
						method = "new"; //$NON-NLS-1$
						replaceAnswer = result;
						firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength()); //$NON-NLS-1$
						break;
					default:
						break;
					}
				} else {
					if (replaceAnswer == 0) {
						method = "replace"; //$NON-NLS-1$
					}
					if (replaceAnswer == 3) {
						method = "new"; //$NON-NLS-1$
					}
					firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength()); //$NON-NLS-1$
				}
			} else {
				exists = false;
				method = "new"; //$NON-NLS-1$
				dataItem = new DataItem(client, file.getName(), parentCluster.getId());
			}

			try {
				fi = new FileInputStream(file);
				int j = filesSent + 1;
				filesSent++;
				taskOutput.append(Messages.getString("UploadDialog.task_sending") + file.getName() + " (" + j + "/" + fileCount +") ... "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				ProgressStream prs = new ProgressStream(fi, totalLength, this);
				prs.addPropertyChangeListener(UploadDialog.this);
				int endPosition = taskOutput.getDocument().getLength();
				Rectangle bottom = taskOutput.modelToView(endPosition);
				bottom.width = 0;
				taskOutput.scrollRectToVisible(bottom);
				if (!exists || method.equals("replace")) { //$NON-NLS-1$
					if(sendFile(dataItem, method, prs)) {
						if (method.equals("new")) { //$NON-NLS-1$
							dataItem.setKeyLength(file.length());
							gui.getTreeModel().refresh(parentNode);
						} 
					} else {
						firePropertyChange("totalNumBytesRead", progress, progress+file.length()); //$NON-NLS-1$
						autoClose = false;
						if (stopOnError) {
							task.cancel(true);
						}
					}
				} else {
					taskOutput.append(Messages.getString("UploadDialog.task_skip")); //$NON-NLS-1$
				}
				taskOutput.append("\n"); //$NON-NLS-1$
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ECException e) {
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
					taskOutput.append(Messages.getString("UploadDialog.task_add_cluster") + directory.getName()); //$NON-NLS-1$
					cluster = BeaverGUI.getClient().addCluster(directory.getName(), parentCluster.getId());
					taskOutput.append(Messages.getString("UploadDialog.task_OK")); //$NON-NLS-1$
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
			} catch (ECException e) {
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
					taskOutput.append(Messages.getString("UploadDialog.task_OK_2")); //$NON-NLS-1$
				}
				return true;
			} catch (ECException e) {
				allOk = false;
				taskOutput.append("[" + e.getMessage() + Messages.getString("UploadDialog.task_failed_2")); //$NON-NLS-1$ //$NON-NLS-2$
				if (e.getId() == 401) {
					QuotaExceededDialog dialog = new QuotaExceededDialog(vault);
					try {
						dataItem.remove();
					} catch (ECException e1) {
					} catch (JSONException e1) {
					} catch (IOException e1) {
					}
					task.cancel(true);
				} 
				
				if (e.getId() == 301) {
					task.cancel(true);
				} 
				
				if (e instanceof ECClientException) {
					task.cancel(true);
				}
				
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				allOk = false;
				taskOutput.append(Messages.getString("UploadDialog.task_failed")); //$NON-NLS-1$
				e.printStackTrace();
				return false;
			} catch (InvalidKeyException e) {
				allOk = false;
				taskOutput.append(Messages.getString("UploadDialog.task_failed")); //$NON-NLS-1$
				e.printStackTrace();
				new IllegalKeyDialog();
				return false;
			} catch (InvalidAlgorithmParameterException e) {
				allOk = false;
				taskOutput.append(Messages.getString("UploadDialog.task_failed")); //$NON-NLS-1$
				e.printStackTrace();
				return false;
			} catch (ECEncryptionException e) {
				allOk = false;
				taskOutput.append(Messages.getString("UploadDialog.task_failed")); //$NON-NLS-1$
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public void done() {
			filesSent = fileCount;
			setProgress(100);
			if (!allOk) {
				taskOutput.append(Messages.getString("UploadDialog.task_could_not_send")); //$NON-NLS-1$
			}
			if (!task.isCancelled()) {
				taskOutput.append(Messages.getString("UploadDialog.task_done")); //$NON-NLS-1$
			}
			if (autoClose) {
				dispose();
			}
			setTitle(Messages.getString("UploadDialog.window_title_complete")); //$NON-NLS-1$
			cancel.setText(Messages.getString("UploadDialog.button_close")); //$NON-NLS-1$
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
			if (node.getPath().length > 1) {
				ClusterNode rootNode = (ClusterNode) node.getPath()[1]; 
				this.vault = (Vault) rootNode.getElement(); // Vault
			}
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int value;
		if ("totalNumBytesRead" == evt.getPropertyName()){ //$NON-NLS-1$
			task.progress += (Long)evt.getNewValue() - (Long)evt.getOldValue();
			value = (int) ((task.progress.doubleValue()/task.totalLength.doubleValue())*100);
			progressBar.setValue(value);
			setTitle(Messages.getString("UploadDialog.window_title_uploading_1") + task.filesSent + Messages.getString("UploadDialog.window_title_uploading_2") + task.fileCount + " - " + Integer.toString(value) + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		if ("progress" == evt.getPropertyName()) { //$NON-NLS-1$
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			setTitle(Messages.getString("UploadDialog.window_title_uploading_1") + task.filesSent + Messages.getString("UploadDialog.window_title_uploading_2") + task.fileCount + " - " + Integer.toString(progress) + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} 		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(TransferFrame.CANCEL_ACTION)) {
			autoClose = false;
			task.cancel(true);
			taskOutput.append(Messages.getString("UploadDialog.task_cancelled")); //$NON-NLS-1$
		}
	}

	public UploadDialog(BeaverGUI gui, ClusterNode root) {
		setTitle(Messages.getString("UploadDialog.window_title_init")); //$NON-NLS-1$
		progressBar.setIndeterminate(true);
		task = new UploadTask();
		task.addPropertyChangeListener(this);
		task.setup(gui, root);
		cancel.addActionListener(this);
		addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				task.cancel(true);				
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
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
