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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.elfcloud.client.dialog.UploadDialog.UploadTask;

/**
 * Wrapper for {@link InputStream} that reports the number of bytes read from the stream.
 */
public class ProgressStream extends FilterInputStream {
	private final PropertyChangeSupport propertyChange;
	private long bytesRead;
	private UploadTask task;

	protected ProgressStream(InputStream is, long maxNumBytes, UploadTask task) {
		super(is);
		this.propertyChange = new PropertyChangeSupport(this);
		this.task = task;
	}


	public long getTotalNumBytesRead() {
		return bytesRead;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChange.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChange.removePropertyChangeListener(l);
	}

	@Override
	public int read() throws IOException {
		return (int)update(super.read());
	}

	@Override
	public int read(byte[] b) throws IOException {
		return (int)update(super.read(b));
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return (int)update(super.read(b, off, len));
	}

	@Override
	public long skip(long n) throws IOException {
		return update(super.skip(n));
	}

	@Override
	public void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	private long update(long numBytesRead) {
		if (task.isCancelled()) { 
			return -1;
		}
		
		if (numBytesRead > 0) {
			long oldTotalNumBytesRead = this.bytesRead;
			this.bytesRead += numBytesRead;
			propertyChange.firePropertyChange("totalNumBytesRead", oldTotalNumBytesRead, this.bytesRead);
		}

		return numBytesRead;
	}
}
