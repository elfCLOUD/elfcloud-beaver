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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringEscapeUtils;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.WrapLayout;

public class ModifyDataItemDialog extends BeaverDialog {
	private static final long serialVersionUID = -8065189591501016726L;
	private JTextField name;
	private JLabel lblName;
	private JTextField description;
	private JLabel lblDescription;
	private JTextField tags;
	private JLabel lblTags;
	private JPanel checkboxPanel;
	private JScrollPane scrollPane;
	private URL imageURL = BeaverGUI.class.getResource("icons/error_16x16.png"); //$NON-NLS-1$
	private Vector<String> tagVector;
	private HashMap<String, String> metamap;
	private boolean answer = false;
	
	public ModifyDataItemDialog(JFrame parent, HashMap<String, String> metaMap, String diName) {
		super(parent, true);
		setTitle(Messages.getString("ModifyDataItemDialog.window_title") + diName); //$NON-NLS-1$
		this.metamap = metaMap;
		tagVector = new Vector<String>();
		setLocationRelativeTo(parent);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.insets = new Insets(0, 0, 5, 0);

		lblName = new JLabel(Messages.getString("ModifyDataItemDialog.label_name")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 1;
		cs.weightx = 0;
		panel.add(lblName, cs);

		name = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 2;
		cs.weightx = 1.0;
		name.setText(diName);
		panel.add(name, cs);

		lblDescription = new JLabel(Messages.getString("ModifyDataItemDialog.label_description")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0;
		panel.add(lblDescription, cs);

		description = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 1;
		cs.gridwidth = 2;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 1.0;
		description.setText(metaMap.get("DSC")); //$NON-NLS-1$
		panel.add(description, cs);

		lblTags = new JLabel(Messages.getString("ModifyDataItemDialog.label_tags")); //$NON-NLS-1$
		cs.gridx = 0;
		cs.gridy = 2;
		cs.gridwidth = 1;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0;
		panel.add(lblTags, cs);

		tags = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 2;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 1.0;
		panel.add(tags, cs);

		JButton addButton = new JButton(Messages.getString("ModifyDataItemDialog.button_add_tag")); //$NON-NLS-1$
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 2;
		cs.gridwidth = 1;
		cs.weightx = 0;
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tagText = tags.getText().trim();
				if (!tagText.equals("")) { //$NON-NLS-1$
					addTags(tagText);
				}
				tags.setText(""); //$NON-NLS-1$
				
				checkboxPanel.revalidate();
				checkboxPanel.repaint();
			}
		});
		panel.add(addButton, cs);

		checkboxPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		checkboxPanel.setSize(new Dimension(320, 1));
		try {
			addTags(metamap.get("TGS")); //$NON-NLS-1$
		} catch (NullPointerException e) {
			// No existing tags
		}
		panel.setBorder(new EmptyBorder(5, 5, 0, 5));
		getContentPane().add(panel, BorderLayout.NORTH);
		scrollPane = new JScrollPane(checkboxPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		
		JButton generateButton = new JButton(Messages.getString("ModifyDataItemDialog.button_save")); //$NON-NLS-1$
		generateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tags.getText().length() > 0) {
					addTags(tags.getText());
				}
				answer = true;
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton(Messages.getString("ModifyDataItemDialog.button_cancel")); //$NON-NLS-1$
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				answer = false;
				setVisible(false);
			}
		});
		JPanel bp = new JPanel();
		bp.add(generateButton);
		bp.add(cancelButton);
		getContentPane().add(bp, BorderLayout.PAGE_END);
		
		setMinimumSize(new Dimension(320, 350));
		pack();
	}
	
	private void addTags(String tag) {
		String[] tagArray = tag.split(","); //$NON-NLS-1$
		for (String str: tagArray) {
			if (str.trim().length() == 0) {
				continue;
			}
			final HTMLEscapedJCheckBox check = new HTMLEscapedJCheckBox(str.trim());
			check.setIcon(new ImageIcon(imageURL));
			check.setSelectedIcon(new ImageIcon(imageURL));
			check.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkboxPanel.remove(check);
					tagVector.remove(check.getOriginalText());
					checkboxPanel.revalidate();
					checkboxPanel.repaint();
				}
			});
			checkboxPanel.add(check);
			tagVector.add(str);
		}
	}
	@Override
	public int showDialog() {
		setVisible(true);
		if (answer) {
			return JOptionPane.OK_OPTION;
		}
		return JOptionPane.CANCEL_OPTION;
	}
	
	public String getTags() {
		StringBuilder sb = new StringBuilder();
		for (String tag: tagVector) {
			sb.append(tag.trim() + ","); //$NON-NLS-1$
		}
		String tagString = sb.toString();
		
		if (tagString.length() == 0) {
			return ""; //$NON-NLS-1$
		}
		return tagString.substring(0, tagString.length()-1);
	}

	public String getDesc() {
		return description.getText().trim();
	}
	
	public String getName() {
		return name.getText().trim();
	}
	
	private class HTMLEscapedJCheckBox extends JCheckBox{
		private static final long serialVersionUID = -8253789726580515609L;
		private String originalText;
		
		public HTMLEscapedJCheckBox(String text) {
			super(text);
			originalText = text;
		}
		
		@Override
		public void setText(String text) {
			super.setText("<html>"+StringEscapeUtils.escapeHtml3(text)+"</html>");
		}

		public String getOriginalText() {
			return originalText;
		}
	}
}
