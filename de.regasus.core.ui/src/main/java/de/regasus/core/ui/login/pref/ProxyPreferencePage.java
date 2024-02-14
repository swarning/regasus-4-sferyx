package de.regasus.core.ui.login.pref;

import static de.regasus.core.ui.login.pref.LoginPreferenceDefinition.*;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.lambdalogic.util.rcp.pref.FieldEditorBuilder;

import de.regasus.core.ui.CoreI18N;

public class ProxyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID ="ProxyPreferencePage";

	public ProxyPreferencePage() {
		super(GRID);
		setPreferenceStore( LoginPreference.getInstance().getPreferenceStore() );
		setDescription(CoreI18N.ProxyPreferencePage_Description);
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

		addField( FieldEditorBuilder.buildFieldEditor(parent, PROXY_HOST) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, PROXY_PORT) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, PROXY_USER) );
		addField( FieldEditorBuilder.buildFieldEditor(parent, PROXY_PASSWORD) );
	}


	@Override
	public void init(IWorkbench workbench) {
	}

}