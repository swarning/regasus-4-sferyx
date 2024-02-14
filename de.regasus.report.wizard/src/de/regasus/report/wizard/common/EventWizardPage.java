package de.regasus.report.wizard.common;

import java.util.Locale;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventTableComposite;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class EventWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "de.regasus.report.wizard.common.EventWizardPage";

	private IEventReportParameter eventReportParameter;
	private EventTableComposite eventTableComposite;


	public EventWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.Event.getString());
		setDescription(ReportWizardI18N.EventWizardPage_Description);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);

		eventTableComposite = new EventTableComposite(
			container,
			null,	// hideEventPKs
			null,	// initSelectedEventPKs
			false,	// multiSelection,
			SWT.NONE
		);
		eventTableComposite.addModifyListener(tableListener);
	}


	private ModifyListener tableListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			setPageComplete( !eventTableComposite.getSelectedEvents().isEmpty() );
		}
	};


	@Override
	public void init(IReportParameter reportParameter) {
		// init models
		try {
			if (reportParameter instanceof IEventReportParameter) {
				eventReportParameter = (IEventReportParameter) reportParameter;

				Long eventPK = eventReportParameter.getEventPK();
				eventTableComposite.setSelectedEventId(eventPK);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void saveReportParameters() {
		EventVO eventVO = eventTableComposite.getSelectedEvent();

		if (eventReportParameter != null) {
			Long eventPK = null;
			String eventName = null;
			if (eventVO != null) {
				eventPK = eventVO.getID();
				eventName = eventVO.getName(Locale.getDefault());
			}

			// set parameter
			eventReportParameter.setEventPK(eventPK);

			// set description
			StringBuilder desc = new StringBuilder();
			if (eventName != null) {
    			desc.append(ParticipantLabel.Event.getString());
    			desc.append(": ");
    			desc.append(eventName);
			}

			eventReportParameter.setDescription(
				IEventReportParameter.DESCRIPTION_ID,
				desc.toString()
			);
		}
	}

}
