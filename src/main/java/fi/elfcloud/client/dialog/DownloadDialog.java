package fi.elfcloud.client.dialog;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
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



import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.container.Cluster;
import fi.elfcloud.sci.exception.HolviEncryptionException;
import fi.elfcloud.sci.exception.HolviException;

/**
 * 
 *
 */
public class DownloadDialog extends TransferFrame implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = -7919455375814441902L;
	private DownloadTask task;
	private final Object[] options = {"Replace all", "Replace", "Skip", "Skip all"};

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
		boolean autoClose = true;
		int filesSent = 1;
		@Override
		protected Void doInBackground() throws Exception {
			setProgress(0);
			boolean allowReplace = false;
			boolean askReplace = true;
			for (int i=0; i<fileArray.size(); i++) {
				if (task.isCancelled()) {
					break;
				}
				taskOutput.append("Downloading " + dataItemArray.get(i).getName() + " (" + i + "/" + fileArray.size() +") ...");
				if (askReplace) {
					if (fileArray.get(i).exists()) {
						allowReplace = false;
						int result = JOptionPane.showOptionDialog(DownloadDialog.this, 
								"File " + fileArray.get(i).getPath() + " already exists, how to proceed?", "Replace " + fileArray.get(i).getPath() + "?", 
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
						taskOutput.append("OK\n");
					} else {
						autoClose = false;
						taskOutput.append("FAILED\n");
					}
				} else {
					read += dataItemArray.get(i).getKeyLength();
					setProgress((int) ((read / (double)maxSize)*100));
					taskOutput.append("SKIPPED\n");
				}
				filesSent++;
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
								"Could not open the file\n" + 
										fileArray.get(0).getAbsolutePath() + 
								"\nTry opening it manually");
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
			if (file.exists() && file.isDirectory()) {
				taskOutput.append("Destination name was directory, file skipped ");
				return false;
			}

			try {
				bis = dataItem.getData();
				fo = new FileOutputStream(file);
				while((len = bis.read(buff, 0, 20971520)) != -1) {
					if (task.isCancelled()) { 
						break;
					}
					inbuff = len;
					read += inbuff;
					int value = (int)(((double)read/(double)maxSize)*100.0);
					setProgress(value);
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
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				new IllegalKeyDialog();
				return false;
			} catch (HolviException e) {
				return false;
			} catch (HolviEncryptionException e) {
				if (e.getId() == 0) {
					openFileOnComplete = false;
					JOptionPane.showMessageDialog(DownloadDialog.this, e.getMessage());
				}
				return false;
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
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
			if (!task.isCancelled()) {
				taskOutput.append("\nDone!");
			}
			if (autoClose) {
				dispose();
			}
			cancel.setText("Close");
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
				DataItem[] dataitems = (DataItem[]) elements.get("dataitems");
				destination.mkdirs();
				for (DataItem di : dataitems) {
					fileArray.add(new File(destination.getAbsolutePath(), di.getName()));
					dataItemArray.add(di);
					maxSize += di.getKeyLength();
				}
				Cluster[] clusters = (Cluster[]) elements.get("clusters");
				for (Cluster c : clusters) {
					if (task.isCancelled()) {
						break;
					}
					File clusterdestination = new File(destination.getAbsolutePath(), c.getName());
					clusterdestination.mkdir();
					processCluster(c, clusterdestination);
				}
			} catch (HolviException e) {
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
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			setTitle("Downloading file " + task.filesSent + " of " + task.fileArray.size() + " - " + Integer.toString(progress) + "%");
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

	public DownloadDialog() {
		setTitle("Download in progress...");
		task = new DownloadTask();
		task.addPropertyChangeListener(this);
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
