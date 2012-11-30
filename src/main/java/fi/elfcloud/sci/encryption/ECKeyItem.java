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

package fi.elfcloud.sci.encryption;

import fi.elfcloud.sci.Client.ENC;
import fi.elfcloud.sci.exception.ECEncryptionException;

/**
 * Model for an encryption key.
 * <p>Used by {@link DataStream}
 */
class ECKeyItem {
	private String keyHash;
	private ENC encMode;
	private byte[] iv;
	private byte[] key;
	
	protected ECKeyItem() {
	}
	
	protected ECKeyItem(byte[] iv, byte[] key, String keyHash) throws ECEncryptionException {
		this.iv = iv;
		this.key = key;
		this.keyHash = keyHash;
		switch (key.length) {
		case 16:
			this.encMode = ENC.AES_128;
			break;
		case 24:
			this.encMode = ENC.AES_192;
			break;
		case 32:
			this.encMode = ENC.AES_256;
			break;
		default:
			throw new ECEncryptionException(0, "Invalid encryption key");
		}
	}
	
	protected String getKeyHash() {
		return this.keyHash;
	}
	
	protected ENC getEncMode() {
		return this.encMode;
	}

	protected byte[] getKey() {
		return this.key;
	}
	
	protected byte[] getIV() {
		return this.iv;
	}
}
