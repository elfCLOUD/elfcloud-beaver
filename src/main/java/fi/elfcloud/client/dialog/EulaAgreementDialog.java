package fi.elfcloud.client.dialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EulaAgreementDialog extends JDialog {
	private static final long serialVersionUID = -8382022158097193355L;

	public EulaAgreementDialog() {
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(new JLabel("You need to agree EULA in order to use elfCLOUD.fi services."), BorderLayout.PAGE_START);
		JLabel label = new JLabel("Please login to elfCLOUD.fi web-service using web browser: ");
		JLabel lblLink = new JLabel("<html><u>https://my.holvi.org/</u><html>");
		lblLink.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI("https://my.holvi.org/"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		textPanel.add(label, BorderLayout.WEST);
		textPanel.add(lblLink, BorderLayout.EAST);
		textPanel.add(new JLabel("After you have agreed the EULA you can start using elfCLOUD.fi Beaver!"), BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(this, textPanel, "Action needed", JOptionPane.INFORMATION_MESSAGE);
	}
}
