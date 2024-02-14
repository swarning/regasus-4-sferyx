package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.react.certificate.CertificateComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.type.react.certificate.ReactCertificatePortalPageConfig;

public class CreateCertificateComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateCertificateComponent;


	public CreateCertificateComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		CertificateComponent component = CertificateComponent.build(getLanguageList());

		return component;
	}

	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled =
			pageContentTreeComposite.getSelectedItem() != null

			// CertificateComponent must only appear on Certificate Page
			&& pageContentTreeComposite.getPage().getKey().equals( ReactCertificatePortalPageConfig.CERTIFICATE_PAGE.getKey() );

		setEnabled(enabled);
	}
}
