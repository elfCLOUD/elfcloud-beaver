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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "fi.elfcloud.client.MessagesBundle"; //$NON-NLS-1$
	private static Locale locale = new Locale("en", "GB");
	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
	private static final Locale[] availableLocales = new Locale[] {
		new Locale("en", "GB"),  //$NON-NLS-1$ //$NON-NLS-2$
		new Locale("fi", "FI") //$NON-NLS-1$ //$NON-NLS-2$
	};

	private Messages() {
	}

	public static void setLocale(String language, String country) {
		Locale newLocale = new Locale(language, country);
		setLocale(newLocale);
	}

	public static void setLocale(Locale locale) {
		boolean found = false;
		for (int i = 0; i < availableLocales.length; i++) {
			if (availableLocales[i].equals(locale)) {
				Messages.locale = locale;
				found = true;
				break;
			}
		}
		if (!found) {
			Messages.locale = new Locale("en", "GB"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Messages.RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Messages.locale);
	}

	public static Locale[] getAvailableLocales() {
		return Messages.availableLocales;
	}
	
	public static Locale getLocale() {
		return Messages.locale;
	}
	
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
