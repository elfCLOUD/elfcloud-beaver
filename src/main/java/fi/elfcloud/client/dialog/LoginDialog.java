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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.preferences.PreferencesWindow;
import fi.elfcloud.sci.exception.ECException;

public class LoginDialog extends JFrame implements KeyListener {

	private static final long serialVersionUID = 6541434171800705107L;
	private static final String title = Messages.getString("LoginDialog.window_title"); //$NON-NLS-1$
	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private JCheckBox cbRemember;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JLabel lblRegister;
	private JLabel lblPreferences;
	private JLabel lblLogin;
	private JButton btnLogin;
	private boolean succeeded;
	private BeaverGUI gui;
	public LoginDialog(JFrame parent, BeaverGUI gui) {
		super(BeaverGUI.titlePrefix + title);
		this.gui = gui;
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		URL imageURL = BeaverGUI.class.getResource("images/login.png"); //$NON-NLS-1$
		JLabel picLabel = new JLabel(new ImageIcon(imageURL));
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		Preferences prefs = BeaverGUI.getPreferences();
		boolean rememberUsername = prefs.getBoolean("elfcloud.remember.username", false); //$NON-NLS-1$
		String username = prefs.get("elfcloud.username", ""); //$NON-NLS-1$ //$NON-NLS-2$
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.insets = new Insets(15, 0, 0, 0);
		lblUsername = new JLabel(Messages.getString("LoginDialog.label_username")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;

		panel.add(lblUsername, cs);

		tfUsername = new JTextField();
		tfUsername.setText(username);
		tfUsername.addKeyListener(this);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		panel.add(tfUsername, cs);

		lblPassword = new JLabel(Messages.getString("LoginDialog.label_password")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		cs.insets = new Insets(10, 0, 0, 0);
		panel.add(lblPassword, cs);

		tfPassword = new JPasswordField();
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		tfPassword.addKeyListener(this);
		panel.add(tfPassword, cs);

		cbRemember = new JCheckBox(Messages.getString("LoginDialog.checkbox_remember_username")); //$NON-NLS-1$
		cbRemember.setSelected(rememberUsername);
		cbRemember.setBackground(Color.WHITE);
		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 1;
		panel.add(cbRemember, cs);

		ImageIcon icon = new ImageIcon(BeaverGUI.class.getResource("images/load.gif")); //$NON-NLS-1$
		lblLogin = new JLabel(icon);
		cs.gridx = 0;
		cs.gridy = 3;
		cs.gridwidth = 1;
		lblLogin.setVisible(false);
		panel.add(lblLogin, cs);

		btnLogin = new JButton(Messages.getString("LoginDialog.button_login")); //$NON-NLS-1$
		btnLogin.registerKeyboardAction(btnLogin.getActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
				JComponent.WHEN_FOCUSED);
		btnLogin.registerKeyboardAction(btnLogin.getActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
				JComponent.WHEN_FOCUSED);
		btnLogin.setPreferredSize(new Dimension(150, 25));
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
		cs.gridx = 1;
		cs.gridy = 3;
		cs.gridwidth = 1;
		cs.insets = new Insets(8, 0, 0, 0);
		panel.add(btnLogin, cs);

		lblRegister = new JLabel(Messages.getString("LoginDialog.label_register_new_account")); //$NON-NLS-1$
		lblRegister.setForeground(Color.BLUE);
		lblRegister.addMouseListener(new MouseListener() {
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
					desktop.browse(new URI(Messages.getString("LoginDialog.link_register_new_account"))); //$NON-NLS-1$
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		cs.gridx = 1;
		cs.gridy = 4;
		cs.gridwidth = 2;
		cs.insets = new Insets(7, 0, 0, 60);
		panel.add(lblRegister, cs);

		lblPreferences = new JLabel(Messages.getString("LoginDialog.link_preferences")); //$NON-NLS-1$
		lblPreferences.setForeground(Color.BLUE);
		lblPreferences.addMouseListener(new MouseListener() {
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
				PreferencesWindow prefsWindow = PreferencesWindow.getInstance();
			}
		});
		cs.gridx = 1;
		cs.gridy = 5;
		cs.gridwidth = 2;
		cs.insets = new Insets(7, 0, 15, 60);
		panel.add(lblPreferences, cs);
		
		getContentPane().add(picLabel, BorderLayout.PAGE_START );
		getContentPane().add(panel, BorderLayout.CENTER);
		setResizable(false);
		pack();

		if (!tfUsername.getText().equals("")) { //$NON-NLS-1$
			tfPassword.requestFocusInWindow();
		}
		setLocationByPlatform(true);
	}

	private void login() {
		btnLogin.setEnabled(false);
		lblLogin.setVisible(true);
		tfPassword.removeKeyListener(this);
		tfUsername.removeKeyListener(this);
		Thread worker = new Thread() {
			public void run() {
				try {
					gui.authenticate(getUsername(), getPassword(), getRememberUsername());
					dispose();
				} catch (ECException exc) {
					lblLogin.setVisible(false);
					exc.printStackTrace();
					if (exc.getId() == 204) {
						@SuppressWarnings("unused")
						EulaAgreementDialog dialog = new EulaAgreementDialog(LoginDialog.this);
					} else {
						JOptionPane.showMessageDialog(LoginDialog.this,
								exc.getMessage(),
								Messages.getString("LoginDialog.error_dialog_title"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
					}
					tfPassword.setText(""); //$NON-NLS-1$
					succeeded = false;
				} catch (MalformedURLException e) {
					lblLogin.setVisible(false);
					e.printStackTrace();
				} catch (IOException e) {
					lblLogin.setVisible(false);
					e.printStackTrace();
				}
				lblLogin.setVisible(false);
				btnLogin.setEnabled(true);
				tfPassword.addKeyListener(LoginDialog.this);
				tfUsername.removeKeyListener(LoginDialog.this);
			}
		};
		worker.start();
	}

	public String getUsername() {
		return tfUsername.getText().trim();
	}

	public String getPassword() {
		return new String(tfPassword.getPassword());
	}

	public boolean getRememberUsername() {
		return cbRemember.isSelected();
	}
	public boolean isSucceeded() {
		return succeeded;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_ENTER) {
			login();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}