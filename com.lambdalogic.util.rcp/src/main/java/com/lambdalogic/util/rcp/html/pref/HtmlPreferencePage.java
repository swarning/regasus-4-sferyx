package com.lambdalogic.util.rcp.html.pref;

import static com.lambdalogic.util.rcp.html.pref.HtmlPreferenceDefinition.BROWSER;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.pref.FieldEditorBuilder;


public class HtmlPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {


	public HtmlPreferencePage() {
		super(GRID);
		setPreferenceStore( HtmlPreference.getInstance().getPreferenceStore() );
		setDescription(UtilI18N.SelectedBrowserDescription);
	}


	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		addField( FieldEditorBuilder.buildFieldEditor(parent, BROWSER) );
	}


	@Override
	public void init(IWorkbench workbench) {
	}

}