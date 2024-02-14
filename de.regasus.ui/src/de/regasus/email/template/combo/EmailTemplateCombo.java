package de.regasus.email.template.combo;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;

import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.event.ParticipantType;


@SuppressWarnings("rawtypes")
public class EmailTemplateCombo extends AbstractComboComposite<EmailTemplate> {

	// Model
	private EmailTemplateModel model;

	/**
	 * PK of the Event whose EmailTemplates shall be presented.
	 */
	private Long eventID;


	public EmailTemplateCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	private CacheModelListener<Long> emailTemplateModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent event) {
			try {
				handleModelChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				EmailTemplate emailTemplate = (EmailTemplate) element;
				return avoidNull( emailTemplate.getName() );
			}
		};
	}


	@Override
	protected Collection<EmailTemplate> getModelData() throws Exception {
		List<EmailTemplate> modelData = null;
		if (eventID != null) {
			modelData = model.getEmailTemplateSearchDataByEvent(eventID);
		}
		return modelData;
	}


	public boolean containsEmailTemplatePK(Long emailTemplatePK) {
		boolean result = false;

		List list = (List) comboViewer.getInput();
		if (list != null) {
			for (Object element : list) {
				// list contains EMPTY_ELEMENT that is no ParticipantTypeVO
				if (element instanceof ParticipantType) {
					EmailTemplate emailTemplate = (EmailTemplate) element;
					if ( emailTemplatePK.equals(emailTemplate.getID()) ) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}


	@Override
	protected void initModel() {
		model = EmailTemplateModel.getInstance();
		// do not observe the model now, because we don't know the Event yet
	}


	@Override
	protected void disposeModel() {
		model.removeListener(emailTemplateModelListener);
	}


	public Long getEmailTemplatePK() {
		Long id = null;
		if (entity != null) {
			id = entity.getID();
		}
		return id;
	}


	public void setEmailTemplatePK(Long emailTemplatePK) {
		try {
			EmailTemplate emailTemplate = null;
			if (emailTemplatePK != null) {
				emailTemplate = model.getEmailTemplate(emailTemplatePK);
			}
			setEntity(emailTemplate);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public Long getEventID() {
		return eventID;
	}


	public void setEventID(Long eventID) throws Exception {
		if ( ! EqualsHelper.isEqual(this.eventID, eventID)) {
			Long oldEventID = this.eventID;
			this.eventID = eventID;

			// save old selection
			final ISelection selection = comboViewer.getSelection();
			entity = null;

			model.removeForeignKeyListener(emailTemplateModelListener, oldEventID);

			// register at the model before getting its data, so the data will be put to the models cache
			model.addForeignKeyListener(emailTemplateModelListener, eventID);

			// update combo
			handleModelChange();


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);
						comboViewer.setSelection(selection);
						entity = getEntityFromComboViewer();
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}

}
