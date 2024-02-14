package de.regasus.core.ui.openoffice.pref;

import static de.regasus.core.ui.openoffice.pref.OpenOfficePreferenceDefinition.PATH;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.lambdalogic.util.rcp.pref.FieldEditorBuilder;

import de.regasus.core.ui.CoreI18N;


public class OpenOfficePreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	public OpenOfficePreferencePage() {
		super(GRID);

		setPreferenceStore( OpenOfficePreference.getInstance().getPreferenceStore() );
		setDescription(CoreI18N.OpenOfficePreferencePage_Description);
	}


	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		addField( FieldEditorBuilder.buildFieldEditor(parent, PATH) );
	}


	@Override
	public void init(IWorkbench workbench) {
	}

}
