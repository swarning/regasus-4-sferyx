package de.regasus.portal.page.editor;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.portal.component.EmailField;

public class EmailFieldCombo extends AbstractComboComposite<EmailField> {


	public EmailFieldCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		setWithEmptyElement(false);
		setKeepEntityInList(false);
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				EmailField emailField = (EmailField) element;
				return emailField.getString();
			}
		};
	}


	@Override
	protected Collection<EmailField> getModelData() {
		List<EmailField> emailFields = CollectionsHelper.createArrayList(EmailField.values());
		return emailFields;
	}


	@Override
	protected void initModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected void disposeModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		// return null to keep the original order as in the enum
		return null;
	}


	public EmailField getEmailField() {
		return entity;
	}


	public void setEmailField(EmailField emailField) {
		setEntity(emailField);
	}

}
