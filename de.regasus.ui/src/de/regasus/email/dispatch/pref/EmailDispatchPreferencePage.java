package de.regasus.email.dispatch.pref;

import static de.regasus.email.dispatch.pref.EmailDispatchPreferenceDefinition.DISPATCH_MODE;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.lambdalogic.util.rcp.pref.FieldEditorBuilder;

import de.regasus.email.EmailI18N;

public class EmailDispatchPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	public EmailDispatchPreferencePage() {
		super(GRID);
		setPreferenceStore( EmailDispatchPreference.getInstance().getPreferenceStore() );
		setDescription(EmailI18N.DispatchModePreferencePage_Description);
	}


	@Override
	public void init(IWorkbench workbench) {
	}


	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		addField( FieldEditorBuilder.buildFieldEditor(parent, DISPATCH_MODE) );
	}

}
