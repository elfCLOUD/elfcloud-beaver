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

import javax.swing.JLabel;

import org.apache.commons.lang3.StringEscapeUtils;

public class HTMLEscapedJLabel extends JLabel {
	private static final long serialVersionUID = 1668953289035886625L;

	public HTMLEscapedJLabel(String text) {
		super(text);
	}
	
	@Override
	public void setText(String text) {
		super.setText("<html>"+StringEscapeUtils.escapeHtml3(text)+"</html>");
	}
}
