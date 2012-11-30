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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML element of a single encryption key.<p>
 * Provides location of the encryption key as well as description, length and hash of the key.
 *
 */
@XmlRootElement(name = "key")
public class XMLKeyItem {
	private String path;
	private String description;
	private String keyHash;
	private int keyLength;
	private boolean exists;
	
	public XMLKeyItem() {
	}
	
	public XMLKeyItem(String path, String description) {
		this.path = path;
		this.description = description;
	}
	
	public XMLKeyItem(String path, String description, String keyHash, int keyLength) {
		this.path = path;
		this.description = description;
		this.keyHash = keyHash;
		this.keyLength = keyLength;
	}
	
	@XmlElement(name = "keyPath")
	public String getPath() {
		return this.path;
	}

	@XmlElement(name = "keyHash")
	public String getKeyHash() {
		return this.keyHash;
	}
	
	public void setKeyHash(String keyhash) {
		this.keyHash = keyhash;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	@XmlElement(name = "keyDescription")
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "keyLength")
	public int getKeyLength() {
		return this.keyLength;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}
	
	public void exists(boolean exists) {
		this.exists = exists;
	}
	
	public boolean isAvailable() {
		return this.exists;
	}

	public String getEncryptionMode() {
		return "AES" +this.keyLength; //$NON-NLS-1$
	}
}