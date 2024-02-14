package de.regasus.core.ui.login.pref;

import static de.regasus.core.ui.login.pref.LoginPreferenceDefinition.*;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.lambdalogic.util.rcp.pref.FieldEditorBuilder;

import de.regasus.core.ui.CoreI18N;

public class LoginPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	public LoginPreferencePage() {
		super(GRID);
		setPreferenceStore( LoginPreference.getInstance().getPreferenceStore() );
		setDescription(CoreI18N.LoginPreferencePage_Description);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		addField( FieldEditorBuilder.buildFieldEditor(parent, USER_NAME) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, PASSWORD) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, HOST) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, AUTO_LOGIN) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, CONFIG_URL) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, DONT_ASK_FOR_CONFIG_URL) );
	}


	@Override
	public void init(IWorkbench workbench) {
	}

}