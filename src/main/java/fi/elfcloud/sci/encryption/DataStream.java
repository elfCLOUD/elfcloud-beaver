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

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.Utils;
import fi.elfcloud.sci.exception.ECEncryptionException;
import fi.elfcloud.sci.exception.ECException;

/**
 * Wrapper for {@link InputStream}.
 * Contains available encryption keys for {@link DataItem.storeData()} and <code>DataItem.getData()</code> operations
 * @see DataItem
 */
public class DataStream {
	private static ArrayList<ECKeyItem> keyList = new ArrayList<ECKeyItem>();
	/**
	 * Initialization vector used for new {@link CipherInputStream} when encrypting data.
	 */
	private static byte[] iv = new byte[0];

	/**
	 * Encryption key used for new {@link CipherInputStream} when encrypting data.
	 */
	private static byte[] key = new byte[0];

	/**
	 * Hash of the {@link #iv} and {@link #key}
	 */
	private static String keyHash;
	private IvParameterSpec ips;
	private SecretKey skey;
	private Cipher cipher;
	private InputStream cis;

	/**
	 * 
	 * @param mode <code>link Cipher.DECRYPT_MODE</code> for decryption, <code>Cipher.ENCRYPT_MODE</code> for encryption
	 * @param is {@link InputStream} to be wrapped in {@link CipherInputStream}
	 * @param keyHash hash of the key to be used in decryption. Encryption uses {@link #iv} and {@link #key}
	 * @throws InvalidKeyException
	 * @throws ECException
	 * @throws ECEncryptionException
	 */
	public DataStream(int mode, InputStream is, String keyHash) 
			throws InvalidKeyException, ECException, ECEncryptionException {

		switch (mode) {
		case Cipher.DECRYPT_MODE:
			if (keyHash.equals("")) {
				this.ips = new IvParameterSpec(DataStream.iv);
				this.skey = new SecretKeySpec(DataStream.key, "AES");
			} else {
				ECKeyItem keyItem = selectEncryptionKey(keyHash);
				if (keyItem != null) {
					this.ips = new IvParameterSpec(keyItem.getIV());
					this.skey = new SecretKeySpec(keyItem.getKey(), "AES");
				} else {
					if (is != null) {
						try {
							is.close();
						} catch (IOException exc) {

						}
					}
					throw new ECEncryptionException(0, "No decryption key available");
				}
			}
			try {
				this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
				this.cipher.init(mode, this.skey, this.ips);
				this.cis = new CipherInputStream(is, this.cipher);
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
				if (is!=null) {
					try {
						is.close();
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}
				handleException(e);
			} catch (NoSuchAlgorithmException e) {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}
				handleException(e);
			} catch (NoSuchPaddingException e) {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}
				handleException(e);
			} 
			break;

		case Cipher.ENCRYPT_MODE:
			if (DataStream.iv.length == 0|| DataStream.key.length == 0) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ECEncryptionException exc = new ECEncryptionException(0, "Invalid cipher");
				throw exc;
			}
			this.ips = new IvParameterSpec(DataStream.iv);
			this.skey = new SecretKeySpec(DataStream.key, "AES");
			try {
				this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
				this.cipher.init(mode, this.skey, this.ips);
				this.cis = new CipherInputStream(is, this.cipher);
			} catch (InvalidAlgorithmParameterException e) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				handleException(e);
			} catch (NoSuchAlgorithmException e) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handleException(e);
			} catch (NoSuchPaddingException e) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handleException(e);
			}
			break;
		default:
			this.cis = is;
			break;
		}
	}

	/**
	 * Returns {@link InputStream}
	 * @return {@link #cis}
	 */
	public InputStream getStream() {
		return this.cis;
	}

	/**
	 * Sets {@link #iv}
	 * @param iv new initialization vector
	 */
	public static void setIV(byte[] iv) {
		DataStream.iv = iv;
		DataStream.keyHash = Utils.calculateKeyHash(DataStream.iv, DataStream.key);
	}

	/**
	 * sets {@link #key}
	 * @param key new encryption key
	 */
	public static void setEncryptionKey(byte[] key) {
		DataStream.key = key;
		DataStream.keyHash = Utils.calculateKeyHash(DataStream.iv, DataStream.key);
	}

	/**
	 * Returns length of the {@link #key}
	 * @return length of the {@link #key} in bytes
	 */
	public static int getKeyLength() {
		return key.length;
	}

	/**
	 * Returns calculated hash of the {@link #iv} and {@link #key}
	 * @return 
	 */
	public static String getKeyHash() {
		return keyHash;
	}

	/**
	 * Add new key to available keys
	 * @param iv initialization vector for the encryption key
	 * @param key encryption key
	 * @throws ECEncryptionException
	 */
	public static void addEncryptionKey(byte[] iv, byte[] key) 
			throws ECEncryptionException {
		String keyHash = Utils.calculateKeyHash(iv, key);
		if (selectEncryptionKey(keyHash) != null) {
			throw new ECEncryptionException(1, "Key already exists");
		}

		ECKeyItem encryptionKey = new ECKeyItem(iv, key, keyHash);
		keyList.add(encryptionKey);
	}

	/**
	 * Returns {@link ECKeyItem} for the given <code>keyHash</code>
	 * @param keyHash hash of the wanted key
	 * @return {@link ECKeyItem} with given <code>keyHash</code>, if none found returns <code>null</code>
	 */
	private static ECKeyItem selectEncryptionKey(String keyHash) {
		for (ECKeyItem keyItem : keyList) {
			if (keyItem.getKeyHash().equals(keyHash)) {
				return keyItem;
			}
		}
		return null;
	}

	/**
	 * Wraps thrown exceptions in {@link ECEncryptionException}
	 * @param exc
	 * @throws ECEncryptionException
	 */
	private void handleException(Exception exc) throws ECEncryptionException {
		ECEncryptionException encryptionException = new ECEncryptionException();
		encryptionException.setMessage(exc.getMessage());
		throw encryptionException;
	}

	/**
	 * Removes encryption key from available keys
	 * @param keyHash hash of the {@link ECKeyItem} to be removed
	 */
	public static void removeEncryptionKey(String keyHash) {
		ECKeyItem keyItem = selectEncryptionKey(keyHash);
		if (keyItem != null) {
			keyList.remove(keyItem);
		}
	}
}
