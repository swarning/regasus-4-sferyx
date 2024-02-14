package de.regasus.core.ui.openoffice.pref;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class OpenOfficePreferenceChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		OpenOfficePreference.getInstance().initFileHelper();
	}

}
