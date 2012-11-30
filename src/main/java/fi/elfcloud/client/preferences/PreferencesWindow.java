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

package fi.elfcloud.client.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fi.elfcloud.client.BeaverGUI;
import fi.elfcloud.client.Messages;
import fi.elfcloud.client.dialog.HelpWindow;

public class PreferencesWindow extends JFrame implements PropertyChangeListener, WindowListener {
	private static PreferencesWindow preferencesWindow;
	private static final long serialVersionUID = -2145927301044637961L;
	private String title; //$NON-NLS-1$
	private JPanel prefPanel;
	private TabPanel tabPanel;
	
	private PreferencesWindow() {
		super();
		initUI();
	}
	
	public static PreferencesWindow getInstance() {
		if (preferencesWindow == null) {
			preferencesWindow = new PreferencesWindow();
		}
		preferencesWindow.tabPanel.setSelected(preferencesWindow.tabPanel.preferences[0]);
		preferencesWindow.setVisible(true);
		return preferencesWindow;
	}
	
	public static PreferencesWindow getInstance(int tabIndex) {
		if (preferencesWindow == null) {
			preferencesWindow = new PreferencesWindow();
		}
		preferencesWindow.tabPanel.setSelected(preferencesWindow.tabPanel.preferences[tabIndex]);
		preferencesWindow.setVisible(true);
		return preferencesWindow;
	}
	
	private void initUI() {
		title = Messages.getString("PreferencesWindow.window_title");
		setTitle(BeaverGUI.titlePrefix + title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		tabPanel = new TabPanel();
		tabPanel.addPropertyChangeListener(this);
		setMinimumSize(new Dimension(tabPanel.getPreferredSize().width, 300));
		setPreferredSize(new Dimension(400, 500));
		add(tabPanel, BorderLayout.NORTH);
		prefPanel = tabPanel.getSelected().getPanel();
		add(prefPanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(new JButton(new HelpAction()));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(new JButton(new CloseAction()));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().setFocusable(true);
		getContentPane().requestFocusInWindow();
		addWindowListener(this);
		pack();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("pageChange" == evt.getPropertyName()){ //$NON-NLS-1$
			remove(prefPanel);
			if (((Preference) evt.getNewValue()) instanceof KeyPreference) {
				((KeyPreference) evt.getNewValue()).reloadKeys();
			}
			prefPanel = ((Preference) evt.getNewValue()).getPanel();

			add(prefPanel, BorderLayout.CENTER);
			validate();
			repaint();
		}
	}
	

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		preferencesWindow = null;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		for (Preference p: tabPanel.preferences) {
			p.save();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	class CloseAction extends AbstractAction {
		private static final long serialVersionUID = -6247258298883666433L;

		public CloseAction() {
			super(Messages.getString("PreferencesWindow.button_close")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			WindowEvent event = new WindowEvent(PreferencesWindow.this, WindowEvent.WINDOW_CLOSING);
			dispatchEvent(event);
			dispose();
		}
	}

	class HelpAction extends AbstractAction {
		private static final long serialVersionUID = 7403934875347814858L;

		public HelpAction() {
			super(Messages.getString("PreferencesWindow.button_help")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			@SuppressWarnings("unused")
			HelpWindow helpWindow = HelpWindow.getInstance();
		}
	}

	class TabPanel extends JPanel implements MouseListener {
		private static final long serialVersionUID = 592846089765390652L;
		private Preference[] preferences = new Preference[]{
				new GeneralPreference(),
				new SecurityPreference(),
				new KeyPreference(),
				new TransferPreference(),
		};
		private Preference selected;
		private final PropertyChangeSupport propertyChange;

		public TabPanel() {
			this.propertyChange = new PropertyChangeSupport(this);
			setPreferredSize(new Dimension(500, 71));
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SystemColor.controlShadow));
			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			for (Preference preference: preferences) {
				preference.addMouseListener(this);
				add(preference.getLabel());
			}
			setSelected(preferences[0]);
		}

		public void addPropertyChangeListener(PropertyChangeListener l) {
			if (propertyChange != null) {
				propertyChange.addPropertyChangeListener(l);
			}
		}

		public void removePropertyChangeListener(PropertyChangeListener l) {
			propertyChange.removePropertyChangeListener(l);
		}

		public Preference getSelected() {
			return this.selected;
		}

		public void setSelected(Preference pref) {
			Preference oldValue = this.selected;
			try {
				oldValue.setSelected(false);
			} catch (NullPointerException npe) {
			}
			this.selected = pref;
			pref.setSelected();
			propertyChange.firePropertyChange("pageChange", oldValue, pref); //$NON-NLS-1$
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			PreferenceLabel label = (PreferenceLabel) event.getSource();
			Preference comp = label.getPreference();
			setSelected(comp);
		}

		@Override
		public void mousePressed(MouseEvent event) {
			PreferenceLabel label = (PreferenceLabel) event.getSource();
			Preference comp = label.getPreference();
			label.setBackground(comp.getSelectedColor());
		}

		@Override
		public void mouseExited(MouseEvent event) {
			PreferenceLabel label = (PreferenceLabel) event.getSource();
			Preference comp = label.getPreference();
			if (!comp.isSelected()) {
				label.setBackground(Color.WHITE);
			} else {
				label.setBackground(comp.getSelectedColor());
			}
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			PreferenceLabel label = (PreferenceLabel) event.getSource();
			Preference comp = label.getPreference();
			if (!comp.isSelected()) {
				label.setBackground(comp.getHoverColor());
			}
		}

		@Override
		public void mouseClicked(MouseEvent event) {
		}
	}
}
