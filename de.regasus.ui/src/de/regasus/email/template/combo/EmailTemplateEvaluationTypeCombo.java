package de.regasus.email.template.combo;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.email.EmailTemplateEvaluationType;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;


public class EmailTemplateEvaluationTypeCombo extends AbstractComboComposite<EmailTemplateEvaluationType> {

	protected List<EmailTemplateEvaluationType> emailTemplateEvaluationTypes = null;


	/**
	 * Create EmailTemplateEvaluationTypeCombo with all {@link EmailTemplateEvaluationType}s.
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public EmailTemplateEvaluationTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected void disposeModel() {
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
				EmailTemplateEvaluationType type = (EmailTemplateEvaluationType) element;
				return type.getString();
			}
		};
	}


	@Override
	@SuppressWarnings("unchecked")
	protected Collection<EmailTemplateEvaluationType> getModelData() throws Exception {
		if (emailTemplateEvaluationTypes == null) {
			if (modelDataDiscriminator != null) {
				emailTemplateEvaluationTypes = (List<EmailTemplateEvaluationType>) modelDataDiscriminator;
			}
			else {
				emailTemplateEvaluationTypes = createArrayList(
					// add only those PaymentTypes the users are allowed to choose manually
					EmailTemplateEvaluationType.Groovy,
					EmailTemplateEvaluationType.Template
				);
			}
		}

		return emailTemplateEvaluationTypes;
	}


	@Override
	protected void initModel() {
	}

}
