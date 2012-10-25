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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.sci.exception.HolviException;
import fi.elfcloud.sci.exception.HolviClientException;

public class LoginDialog extends JFrame {

	private static final long serialVersionUID = 6541434171800705107L;
	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private JCheckBox cbRemember;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JLabel lblRegister;
	private JLabel lblLogin;
	private JButton btnLogin;
	private boolean succeeded;

	public LoginDialog(JFrame parent, final BeaverGUI guiClient) {
		super("elfCLOUD.fi\u2122 Beaver - Login");
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		URL imageURL = BeaverGUI.class.getResource("images/login.png");
		JLabel picLabel = new JLabel(new ImageIcon(imageURL));
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		Preferences prefs = BeaverGUI.getPreferences();
		boolean rememberUsername = prefs.getBoolean("elfcloud.remember.username", false);
		String username = prefs.get("elfcloud.username", "");
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.insets = new Insets(15, 0, 0, 0);
		lblUsername = new JLabel("Username: ");
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;

		panel.add(lblUsername, cs);

		tfUsername = new JTextField();
		tfUsername.setText(username);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		panel.add(tfUsername, cs);

		lblPassword = new JLabel("Password: ");
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		cs.insets = new Insets(10, 0, 0, 0);
		panel.add(lblPassword, cs);

		tfPassword = new JPasswordField();
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(tfPassword, cs);

		cbRemember = new JCheckBox("Remember me");
		cbRemember.setSelected(rememberUsername);
		cbRemember.setBackground(Color.WHITE);
		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 1;
		panel.add(cbRemember, cs);
		
		ImageIcon icon = new ImageIcon(BeaverGUI.class.getResource("images/load.gif"));
		lblLogin = new JLabel(icon);
		cs.gridx = 0;
		cs.gridy = 3;
		cs.gridwidth = 1;
		lblLogin.setVisible(false);
		panel.add(lblLogin, cs);
		
		btnLogin = new JButton("Login");
		btnLogin.setPreferredSize(new Dimension(150, 25));
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread worker = new Thread() {
					public void run() {
						btnLogin.setEnabled(false);
						lblLogin.setVisible(true);
						try {
							if (!BeaverGUI.serverUrlGiven) {
								URL apiUrl = new URL("http://elfcloud.fi/api.url");
								BufferedReader in = new BufferedReader(new InputStreamReader(apiUrl.openStream()));
								String url = in.readLine();
								in.close();
								apiUrl = new URL(url);
								if (!apiUrl.getProtocol().equals("https")) {
									HolviClientException exc = new HolviClientException();
									exc.setMessage("Invalid protocol");
									throw exc;
								}
								if (!apiUrl.getHost().endsWith(".elfcloud.fi") && !apiUrl.getHost().endsWith(".holvi.org")) {
									HolviClientException exc = new HolviClientException();
									exc.setMessage("Invalid host");
									throw exc;
								}
								BeaverGUI.serverUrl = apiUrl.toString();
							}
							guiClient.authenticate(getUsername(), getPassword(), getRememberUsername());
							dispose();
						} catch (HolviException exc) {
							lblLogin.setVisible(false);
							exc.printStackTrace();
							if (exc.getId() == 204) {
								@SuppressWarnings("unused")
								EulaAgreementDialog dialog = new EulaAgreementDialog();
							} else {
								JOptionPane.showMessageDialog(LoginDialog.this,
										exc.getMessage(),
										"Login",
										JOptionPane.ERROR_MESSAGE);
								tfPassword.setText("");
								succeeded = false;
							}
						} catch (MalformedURLException e) {
							lblLogin.setVisible(false);
							e.printStackTrace();
						} catch (IOException e) {
							lblLogin.setVisible(false);
							e.printStackTrace();
						}
						lblLogin.setVisible(false);
						btnLogin.setEnabled(true);
					}
				};
				worker.start();
			}
		});
		cs.gridx = 1;
		cs.gridy = 3;
		cs.gridwidth = 1;
		cs.insets = new Insets(8, 0, 0, 0);
		panel.add(btnLogin, cs);

		lblRegister = new JLabel("<html><u>Register new account</u><html>");
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
					desktop.browse(new URI("https://my.holvi.org?lang=en"));
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
		cs.insets = new Insets(7, 0, 15, 60);
		panel.add(lblRegister, cs);

		getContentPane().add(picLabel, BorderLayout.PAGE_START );
		getContentPane().add(panel, BorderLayout.CENTER);
		setResizable(false);
		pack();
		if (!tfUsername.getText().equals("")) {
			tfPassword.requestFocusInWindow();
		}
		setLocationByPlatform(true);
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
}