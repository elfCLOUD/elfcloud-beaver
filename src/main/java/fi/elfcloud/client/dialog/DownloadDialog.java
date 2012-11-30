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

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import fi.elfcloud.client.Messages;
import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.exception.ECEncryptionException;
import fi.elfcloud.sci.exception.ECException;

/**
 * 
 *
 */
public class DownloadDialog extends TransferFrame implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = -7919455375814441902L;
	private DownloadTask task;
	private final Object[] options = {Messages.getString("DownloadDialog.button_replace_all"), Messages.getString("DownloadDialog.button_replace"), Messages.getString("DownloadDialog.button_skip"), Messages.getString("DownloadDialog.button_skip_all")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * 
	 *
	 */
	class DownloadTask extends SwingWorker<Void, Void> {
		List<File> fileArray = new ArrayList<File>();
		List<DataItem> dataItemArray = new ArrayList<DataItem>();
		long maxSize = 0;
		long read = 0;
		boolean openFileOnComplete = false;
		int filesSent = 0;
		@Override
		protected Void doInBackground() throws Exception {
			setProgress(0);
			boolean allowReplace = false;
			boolean askReplace = true;
			for (int i=0; i<fileArray.size(); i++) {
				filesSent++;
				if (task.isCancelled()) {
					break;
				}
				taskOutput.append(Messages.getString("DownloadDialog.task_downloading") + dataItemArray.get(i).getName() + " (" + filesSent + "/" + fileArray.size() +") ... "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if (askReplace) {
					if (fileArray.get(i).exists()) {
						allowReplace = false;
						int result = JOptionPane.showOptionDialog(DownloadDialog.this, 
								Messages.getString("DownloadDialog.error_file_exists_text_1") + fileArray.get(i).getPath() + Messages.getString("DownloadDialog.error_file_exists_text_2"), Messages.getString("DownloadDialog.error_file_exists_title") + fileArray.get(i).getPath() + "?",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
						switch (result) {
						case 0:
							allowReplace = true;
							askReplace = false;
							break;
						case 1:
							allowReplace = true;
							break;
						case 2:
							allowReplace = false;
							break;
						case 3:
							allowReplace = false;
							askReplace = false;
							break;
						default:
							break;
						}
					}
				}
				if (!fileArray.get(i).exists() || allowReplace) {
					if (saveFile(fileArray.get(i), dataItemArray.get(i))) {
						taskOutput.append(Messages.getString("DownloadDialog.task_ok")); //$NON-NLS-1$
					} else {
						autoClose = false;
						taskOutput.append(Messages.getString("DownloadDialog.task_failed")); //$NON-NLS-1$
						if (stopOnError) {
							task.cancel(true);
						}
					}
				} else {
					read += dataItemArray.get(i).getKeyLength();
					setProgress((int) ((read / (double)maxSize)*100));
					taskOutput.append(Messages.getString("DownloadDialog.task_skipped")); //$NON-NLS-1$
				}
				int endPosition = taskOutput.getDocument().getLength();
				Rectangle bottom = taskOutput.modelToView(endPosition);
				bottom.width = 0;
				taskOutput.scrollRectToVisible(bottom);
			}
			if (fileArray.size() == 1 && openFileOnComplete) {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					try {
						desktop.open(fileArray.get(0).getAbsoluteFile());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(DownloadDialog.this, 
								Messages.getString("DownloadDialog.auto_open_failed_text_1") +  //$NON-NLS-1$
										fileArray.get(0).getAbsolutePath() + 
								Messages.getString("DownloadDialog.auto_open_failed_text_2")); //$NON-NLS-1$
					}
				}
			}
			return null;
		}

		public boolean saveFile(File file, DataItem dataItem) {
			InputStream bis = null;
			FileOutputStream fo = null;
			byte[] buff = new byte[20971520];
			int len = 0;
			int inbuff = 0;
			try {
				bis = dataItem.getData();
				File parentFolder = file.getParentFile();
				parentFolder.mkdirs();
				if (file.exists() && file.isDirectory()) {
					read += dataItem.getKeyLength();
					setProgress((int)(((double)read/(double)maxSize)*100.0));
					taskOutput.append(Messages.getString("DownloadDialog.destination_directory_skip")); //$NON-NLS-1$
					return false;
				}
				fo = new FileOutputStream(file);
				while((len = bis.read(buff, 0, 20971520)) != -1) {
					if (task.isCancelled()) { 
						break;
					}
					inbuff = len;
					read += inbuff;
					setProgress((int)(((double)read/(double)maxSize)*100.0));
					while ((inbuff < 20971520)) {
						len = bis.read(buff, inbuff, 20971520-inbuff);
						if (len == -1) {
							break;
						}
						inbuff += len;
						read += len;
						setProgress((int)(((double)read/(double)maxSize)*100.0));
					}
					fo.write(buff, 0, inbuff);

				}
			} catch (InvalidKeyException e) {
				read += dataItem.getKeyLength();
				setProgress((int)(((double)read/(double)maxSize)*100.0));
				autoClose = false;
				e.printStackTrace();
				new IllegalKeyDialog();
				return false;
			} catch (ECException e) {
				read += dataItem.getKeyLength();
				setProgress((int)(((double)read/(double)maxSize)*100.0));
				taskOutput.append("[" + e.getMessage() + "] "); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			} catch (ECEncryptionException e) {
				read += dataItem.getKeyLength();
				setProgress((int)(((double)read/(double)maxSize)*100.0));
				taskOutput.append("[" + e.getMessage() + "] "); //$NON-NLS-1$ //$NON-NLS-2$
				if (e.getId() == 0) {
					openFileOnComplete = false;
				}
				return false;
			} catch (FileNotFoundException e) {
				read += dataItem.getKeyLength();
				taskOutput.append("[" + e.getMessage() + "] "); //$NON-NLS-1$ //$NON-NLS-2$
				setProgress((int)(((double)read/(double)maxSize)*100.0));
				return false;
			} catch (IOException e) {
				read += dataItem.getKeyLength();
				setProgress((int)(((double)read/(double)maxSize)*100.0));
				return false;
			} finally {
				if (fo != null) {
					try {
						fo.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				buff = null;
			}
			return true;
		}

		@Override
		public void done() {
			setTitle(Messages.getString("DownloadDialog.window_title_complete")); //$NON-NLS-1$
			if (!task.isCancelled()) {
				taskOutput.append(Messages.getString("DownloadDialog.task_status_done")); //$NON-NLS-1$
			}
			if (autoClose) {
				dispose();
			}
			cancel.setText(Messages.getString("DownloadDialog.button_close")); //$NON-NLS-1$
			cancel.removeActionListener(DownloadDialog.this);
			cancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
			
			fileArray = null;
			dataItemArray = null;
			System.gc();
		}

		public void addFile(DataItem dataItem, File file) {
			fileArray.add(file);
			dataItemArray.add(dataItem);
			maxSize += dataItem.getKeyLength();
		}

		private void processCluster(Cluster cluster, File destination) {
			try {
				HashMap <String, Object[]> elements = cluster.getElements();
				DataItem[] dataitems = (DataItem[]) elements.get("dataitems"); //$NON-NLS-1$
				for (DataItem di : dataitems) {
					fileArray.add(new File(destination.getAbsolutePath(), di.getName()));
					dataItemArray.add(di);
					maxSize += di.getKeyLength();
				}
				Cluster[] clusters = (Cluster[]) elements.get("clusters"); //$NON-NLS-1$
				for (Cluster c : clusters) {
					if (task.isCancelled()) {
						break;
					}
					File clusterFolder = new File(destination.getAbsolutePath(), c.getName());
					processCluster(c, clusterFolder);
				}
			} catch (ECException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void addFile(Cluster cluster, File destination) {
			processCluster(cluster, destination);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) { //$NON-NLS-1$
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			setTitle(Messages.getString("DownloadDialog.window_title_downloading") + task.filesSent + Messages.getString("DownloadDialog.window_title_downloading_2") + task.fileArray.size() + " - " + Integer.toString(progress) + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} 		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(TransferFrame.CANCEL_ACTION)) {
			autoClose = false;
			task.cancel(true);
			taskOutput.append(Messages.getString("DownloadDialog.download_status_cancelled")); //$NON-NLS-1$
		}
	}

	public DownloadDialog() {
		setTitle(Messages.getString("DownloadDialog.window_title_init")); //$NON-NLS-1$
		task = new DownloadTask();
		task.addPropertyChangeListener(this);
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
	}

	/**
	 * Add new data item to {@link DownloadTask} related.
	 * @param dataItem data item to be retrieved
	 * @param file file where the data will be saved
	 */
	public void addFile(DataItem dataItem, File file) {
		task.addFile(dataItem, file);
	}

	/**
	 * Start {@link DownloadTask}.
	 */
	public void startTask() {
		task.execute();
	}

	public void openFileOnComplete() {
		task.openFileOnComplete = true;
	}

	public void addFile(Cluster cluster, File destination) {
		task.addFile(cluster, destination);
	}
}
