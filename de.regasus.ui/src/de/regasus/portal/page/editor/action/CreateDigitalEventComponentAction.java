package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;

import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.DigitalEventComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateDigitalEventComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateDigitalEventComponent;


	public CreateDigitalEventComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		// build default value for buttonLabel
		LanguageString buttonLabel = new LanguageString();
		for (String lang : getLanguageList()) {
			buttonLabel.put(lang, ParticipantLabel.DigitalEvent.getString(lang));
		}

		DigitalEventComponent component = DigitalEventComponent.build();
		component.setButtonLabel(buttonLabel);

		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}
