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

package fi.elfcloud.sci;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Cipher;

/**
 * Provides useful functions
 *
 */
public class Utils {

	/**
	 * Transforms given {@link DataItem} meta header map to String
	 * @param map meta header {@link Map}
	 * @return String representation of the <code>map</code>
	 */
	public static String metaToString(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		sb.append(map.get("v") + ":");
		map.remove("v");
		for (String key: map.keySet()) {
			String value = map.get(key);
			value = Utils.removeTrailingBackslashes(value);
			value = value.replace("\\", "\\\\");
			value = value.replace(":", "\\:");
			sb.append(key + ":"+ value + ":");
		}
		sb.append(":");
		return sb.toString();
	}
	
	private static String removeTrailingBackslashes(String value) {
		if (value.endsWith("\\")) {
			value = removeTrailingBackslashes(value.substring(0, value.length()-1));
		}
		return value;
	}
	
	/**
	 * Transforms given meta String to {@link HashMap}
	 * @param meta meta String to be parsed
	 * @return {@link HashMap} of the given meta String
	 */
	public static HashMap<String, String> metaToMap(String meta) {
		HashMap<String, String> metaMap = new HashMap<String, String>();
		try {
			if (meta.endsWith("::")) {
				meta = meta.substring(0, meta.length()-2);
			}
			String regex = "(?<!\\\\):";
			String[] metaArray = meta.split(regex, -1);
			metaMap.put("v", metaArray[0]);
			for (int i=1; i<metaArray.length; i+=2) {
				String value = metaArray[i+1];
				value = value.replace("\\:", ":");
				value = value.replace("\\\\", "\\");
				metaMap.put(metaArray[i], value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metaMap;
	}
	
	/**
	 * Returns hex String of the give byte array 
	 * @param b
	 * @return hex String of the given byte array
	 */
	public static String getHex(byte[] b) {
		StringBuffer hex = new StringBuffer();
		for (int i=0; i<b.length; i++) {
			hex.append(String.format("%02x", b[i]));
		}
		return hex.toString();
	}
	
	/**
	 * Calculates hash of the encryption key
	 * @param iv initialization vector 
	 * @param key encryption key
	 * @return hash of the encryption cipher
	 */
	public static String calculateKeyHash(byte[] iv, byte[] key) {
		String keyHash = null;
		try {
			MessageDigest keymd = MessageDigest.getInstance("MD5");
			byte[] keyBytes = new byte[iv.length + key.length];
			System.arraycopy(iv, 0, keyBytes, 0, iv.length);
			System.arraycopy(key, 0, keyBytes, iv.length, key.length);

			keymd.update(keyBytes);
			int i = 10000-1;
			while (i > 0) {
				byte[] digest = keymd.digest();
				keymd.update(digest);
				i--;
			}
			keyHash = getHex(keymd.digest());
		} catch (NoSuchAlgorithmException e) {
			
		}
		return keyHash;
	}
	
	/**
	 * Returns bytes in readable format
	 * @param bytes amount of bytes
	 * @param si <code>true</code> for SI values, <code>false</code> for binary values
	 * @return
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * Parse time stamp from elfcloud.fi server 
	 * @param dateTime time stamp returned from elfcloud.fi server
	 * @return time stamp represented in local time
	 */
	public static String parseDateTimeString(String dateTime) {
		if (dateTime == null || dateTime.length() == 0) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		TimeZone z = c.getTimeZone();
		// 2012-09-26T06:58:43.446859+00:00
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");
		outputFormat.setTimeZone(z);
		String[] parts = dateTime.split("[T.]");
		try {
			Date date = inputFormat.parse(parts[0] + " " + parts[1]);
			String outputText = outputFormat.format(date);
			return outputText;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "";
	}
	
	public static int getMaximumAvailableKeyLength() {
		try {
			return (Cipher.getMaxAllowedKeyLength("AES") >= 256 ? 256 : Cipher.getMaxAllowedKeyLength("AES")); 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return 128; // 128 is available on all platforms for AES
	}
}
