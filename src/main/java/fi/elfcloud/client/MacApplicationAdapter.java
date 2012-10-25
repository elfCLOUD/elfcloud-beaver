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
