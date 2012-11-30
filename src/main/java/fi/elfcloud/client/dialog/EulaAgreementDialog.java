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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import fi.elfcloud.client.Messages;

public class EulaAgreementDialog extends JDialog {
	private static final long serialVersionUID = -8382022158097193355L;

	public EulaAgreementDialog(JFrame parent) {
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(new JLabel(Messages.getString("EulaAgreementDialog.0")), BorderLayout.PAGE_START); //$NON-NLS-1$
		JLabel label = new JLabel(Messages.getString("EulaAgreementDialog.1")); //$NON-NLS-1$
		JLabel lblLink = new JLabel(Messages.getString("EulaAgreementDialog.2")); //$NON-NLS-1$
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
					desktop.browse(new URI(Messages.getString("EulaAgreementDialog.link"))); //$NON-NLS-1$
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		textPanel.add(label, BorderLayout.WEST);
		textPanel.add(lblLink, BorderLayout.CENTER);
		textPanel.add(new JLabel(Messages.getString("EulaAgreementDialog.3")), BorderLayout.SOUTH); //$NON-NLS-1$
		JOptionPane.showMessageDialog(parent, textPanel, Messages.getString("EulaAgreementDialog.error_title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}
}
