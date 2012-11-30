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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.HTMLEscapedJLabel;
import fi.elfcloud.client.Messages;
import fi.elfcloud.sci.User;
import fi.elfcloud.sci.container.Vault;

public class QuotaExceededDialog extends JDialog {
	private static final long serialVersionUID = 4875497645142170475L;

	public QuotaExceededDialog(Vault vault) {
		super();
		setTitle(Messages.getString("QuotaExceededDialog.window_title")); //$NON-NLS-1$
		setModal(true);
		setLayout(new BorderLayout());
		User owner = vault.getOwner();
		User accountAdmin = BeaverGUI.getClient().getAccountAdmin();
		User currentUser = BeaverGUI.getClient().getCurrentUser();

		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		JLabel infoLabel = new JLabel(Messages.getString("QuotaExceededDialog.explanation_1_no_capacity")); //$NON-NLS-1$
		contentPanel.add(infoLabel);
		contentPanel.add(Box.createVerticalStrut(15));
		JLabel contactLabel = new JLabel();
		if (owner.getAccount().getId() == currentUser.getAccount().getId()) {
			if (currentUser.getId() == accountAdmin.getId()) {
				contactLabel.setText(Messages.getString("QuotaExceededDialog.explanation_account_admin_purchase")); //$NON-NLS-1$
				contentPanel.add(contactLabel);
				JLabel linkLabel = new JLabel(Messages.getString("QuotaExceededDialog.link_purchase")); //$NON-NLS-1$
				linkLabel.setForeground(Color.BLUE);
				linkLabel.addMouseListener(new MouseListener() {
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
							desktop.browse(new URI(Messages.getString("QuotaExceededDialog.subscription_link"))); //$NON-NLS-1$
						} catch (IOException e) {
							e.printStackTrace();
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				});
				contentPanel.add(linkLabel);
			} else {
				contactLabel.setText(Messages.getString("QuotaExceededDialog.explanation_2_normal_user_own_account")); //$NON-NLS-1$
				contentPanel.add(contactLabel);
				contentPanel.add(Box.createVerticalStrut(10));
				contentPanel.add(new ContactInformationPanel(accountAdmin));
			}
		} else {
			contactLabel.setText(Messages.getString("QuotaExceededDialog.explanation_2_external_account")); //$NON-NLS-1$
			contentPanel.add(contactLabel);
			contentPanel.add(Box.createVerticalStrut(10));
			contentPanel.add(new ContactInformationPanel(owner));
		}
		add(contentPanel, BorderLayout.CENTER);
		contentPanel.setPreferredSize(new Dimension(300, 150));
		JButton closeButton = new JButton(Messages.getString("QuotaExceededDialog.button_close")); //$NON-NLS-1$
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		add(closeButton, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}
	
	private class ContactInformationPanel extends JPanel {
		private static final long serialVersionUID = 3125766633575494571L;

		private ContactInformationPanel(User user) {
			super();
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			JLabel lblName;
			JLabel lblPhone;
			JLabel lblEmail;
			String text = ""; //$NON-NLS-1$
			String name = (user.getFirstname().trim().length() > 0 ? user.getFirstname().trim() + " ": ""); //$NON-NLS-1$ //$NON-NLS-2$
			name += (user.getLastname().trim().length() > 0 ? user.getLastname().trim(): ""); //$NON-NLS-1$
			if (name.trim().length() > 0) {
				text += name.trim();
				text += " (" + user.getName().trim() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				text += user.getName().trim();
			}
			lblName = new HTMLEscapedJLabel(text);
			add(lblName);
			if (user.getTelephone().length() > 0) {
				lblPhone = new HTMLEscapedJLabel(user.getTelephone());
				add(lblPhone);
			}
			lblEmail = new HTMLEscapedJLabel(user.getEmail());
			add(lblEmail);
			setBorder(BorderFactory.createTitledBorder(Messages.getString("QuotaExceededDialog.border_title_contact_information"))); //$NON-NLS-1$
		}
	}
}
