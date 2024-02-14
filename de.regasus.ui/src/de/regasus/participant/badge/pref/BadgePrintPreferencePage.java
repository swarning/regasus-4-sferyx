package de.regasus.participant.badge.pref;

import static de.regasus.participant.badge.pref.BadgePrintPreferenceDefinition.WAIT_TIME;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.lambdalogic.util.rcp.pref.FieldEditorBuilder;

import de.regasus.I18N;


public class BadgePrintPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	public static final String ID ="com.lambdalogic.mi.core.event.preferences.BadgePrintPreferencePage";

	public BadgePrintPreferencePage() {
		super(GRID);
		setPreferenceStore( BadgePrintPreference.getInstance().getPreferenceStore() );
		setDescription(I18N.BadgePrintPreferencePage_Description);
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

		addField( FieldEditorBuilder.buildFieldEditor(parent, WAIT_TIME) );
	}


	@Override
	public void init(IWorkbench workbench) {
	}

}