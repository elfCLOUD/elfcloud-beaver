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

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import fi.elfcloud.client.dialog.AboutDialog;

@SuppressWarnings("deprecation")
public class MacApplicationAdapter extends ApplicationAdapter {
	private MainWindow parent;

	public MacApplicationAdapter(MainWindow parent)
	{
		this.parent = parent;
	}

	public void handleQuit(ApplicationEvent e)
	{
		System.exit(0);
	}

	public void handleAbout(ApplicationEvent e)
	{
		e.setHandled(true);
		AboutDialog dialog = new AboutDialog(parent);
		dialog.setVisible(true);
	}

	public void handlePreferences(ApplicationEvent e)
	{
		e.setHandled(true);
	}
}
