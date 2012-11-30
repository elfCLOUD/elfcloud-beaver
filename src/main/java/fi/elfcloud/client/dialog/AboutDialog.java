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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -5009018726039275476L;

	public AboutDialog(JFrame parent) {
		super(parent, Messages.getString("AboutDialog.window_title"), true); //$NON-NLS-1$
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		URL imageURL = BeaverGUI.class.getResource("images/about.png"); //$NON-NLS-1$
		JLabel picLabel = new JLabel(new ImageIcon(imageURL));
		getContentPane().add(picLabel, BorderLayout.PAGE_START);
		JPanel panel = new JPanel(new BorderLayout());
		
		panel.setBackground(Color.WHITE);
		JPanel buttonPanel = new JPanel();
		JLabel copyright = new JLabel("\u00a9 2011-2012 elfcloud.fi"); //$NON-NLS-1$
		copyright.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel authorInfo = new JLabel("elfcloud.fi\u2122 Beaver (Fall 2012, rev.5)"); //$NON-NLS-1$
		JLabel licenseInfo = new JLabel("For license information, please refer to the help section."); //$NON-NLS-1$

		authorInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		licenseInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(Color.WHITE);
		textPanel.add(Box.createVerticalGlue());
		textPanel.add(authorInfo);
		textPanel.add(copyright);
		textPanel.add(Box.createVerticalStrut(9));
		textPanel.add(licenseInfo);
		textPanel.add(Box.createVerticalGlue());
		panel.add(textPanel, BorderLayout.CENTER);
		buttonPanel.setBackground(Color.WHITE);
		JButton close = new JButton(Messages.getString("AboutDialog.button_close")); //$NON-NLS-1$
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		close.setPreferredSize(new Dimension(100, 25));
		buttonPanel.add(close);
		panel.setPreferredSize(new Dimension(picLabel.getWidth(), 100));
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		getContentPane().add(panel, BorderLayout.PAGE_END);
		setResizable(false);
		pack();
		setLocationByPlatform(true);
		textPanel.requestFocusInWindow();
	}
}
