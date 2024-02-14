package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;
import com.lambdalogic.i18n.LanguageString;
import de.regasus.I18N;
import de.regasus.common.CommonI18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.UploadComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateUploadComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateUploadComponent;


	public CreateUploadComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		// build default value for buttonLabel
		LanguageString buttonLabel = new LanguageString();
		for (String lang : getLanguageList()) {
			buttonLabel.put(lang, CommonI18N.SelectFile.getString(lang));
		}

		UploadComponent component = UploadComponent.build();
		component.setButtonLabel(buttonLabel);

		return component;
	}

}
