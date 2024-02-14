package de.regasus.report.wizard.common;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventTableComposite;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class EventListWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "de.regasus.report.wizard.common.EventListWizardPage";

	private boolean mandatory = false;

	private IEventListReportParameter eventListReportParameter;
	private EventTableComposite eventTableComposite;


	public EventListWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.Events.getString());
		setDescription(ReportWizardI18N.EventListWizardPage_Description);
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		eventTableComposite = new EventTableComposite(
			container,
			null,	// hideEventPKs
			null,	// initSelectedEventPKs
			true,	// multiSelection,
			SWT.NONE
		);
		eventTableComposite.addModifyListener(tableListener);
	}


	private ModifyListener tableListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			setPageComplete( !mandatory || !eventTableComposite.getSelectedEvents().isEmpty() );
		}
	};


	@Override
	public void init(IReportParameter reportParameter) {
		// init models
		try {
			if (reportParameter instanceof IEventListReportParameter) {
				eventListReportParameter = (IEventListReportParameter) reportParameter;
				eventTableComposite.setSelectedEventIds( eventListReportParameter.getEventPKs() );
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void saveReportParameters() {
		List<EventVO> eventVOs = eventTableComposite.getSelectedEvents();
		List<Long> eventIds = EventVO.getPKs(eventVOs);

		if (eventListReportParameter != null) {
			eventListReportParameter.setEventPKs(eventIds);

			StringBuilder description = new StringBuilder();
			if ( ! eventVOs.isEmpty()) {
				description.append( ParticipantLabel.Events.getString() );
				description.append(": ");

				int i = 0;
				for (EventVO eventVO : eventVOs) {
					if (i++ > 0) {
						description.append(", ");
					}
					description.append( eventVO.getMnemonic() );
				}
			}

			eventListReportParameter.setDescription(
				IEventListReportParameter.DESCRIPTION_ID,
				description.toString()
			);
		}
	}


	public boolean isMandatory() {
		return mandatory;
	}


	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;

		// change description id the current description is still the default
		if (mandatory && getDescription() == ReportWizardI18N.EventListWizardPage_Description) {
			setDescription(ReportWizardI18N.EventListWizardPage_Description_mandatory);
		}
		else if (!mandatory && getDescription() == ReportWizardI18N.EventListWizardPage_Description_mandatory) {
			setDescription(ReportWizardI18N.EventListWizardPage_Description);
		}
	}

}
