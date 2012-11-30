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

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class Preference {
	protected boolean isSelected = false;
	protected PreferenceLabel preferenceLabel;
	protected JPanel preferencePanel;
	private Color selectedColor = new Color(0xc1d2ee);
	private Color hoverColor = new Color(0xe0e8f6);

	public PreferenceLabel getLabel() {
		return this.preferenceLabel;
	}

	public JPanel getPanel() {
		return this.preferencePanel;
	}

	public abstract void save();
	public abstract void load();

	public void setSelected() {
		this.isSelected = true;
		this.preferenceLabel.setBackground(selectedColor);
	}

	public void setSelected(boolean selected) {
		this.isSelected = selected;
		if (selected) {
			this.preferenceLabel.setBackground(selectedColor);
		} else {
			this.preferenceLabel.setBackground(Color.WHITE);
		}
	}

	public boolean isSelected() {
		return this.isSelected;
	}

	public Color getSelectedColor() {
		return this.selectedColor;
	}

	public Color getHoverColor() {
		return this.hoverColor;
	}

	public void addMouseListener(MouseListener mlistener) {
		this.preferenceLabel.addMouseListener(mlistener);
	}

}

class PreferenceLabel extends JLabel {
	private static final long serialVersionUID = 3202216903654770447L;
	private Preference preference;

	public PreferenceLabel(ImageIcon icon, Preference pref) {
		super(icon);
		preference = pref;
	}

	public Preference getPreference() {
		return preference;
	}
}

class PreferencePanelRow extends JPanel {
	private static final long serialVersionUID = -5977684892504328332L;

	public PreferencePanelRow() {
		super();
		setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		setLayout(new GridBagLayout());
	}
}