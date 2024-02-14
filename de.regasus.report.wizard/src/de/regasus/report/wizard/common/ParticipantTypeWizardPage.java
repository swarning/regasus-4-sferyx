package de.regasus.report.wizard.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventReportParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.IParticipantTypesReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantTypeWizardPage extends WizardPage implements IReportWizardPage {
//	private static Logger log = Logger.getLogger("ui.ParticipantStateWizardPage");

	public static final String ID = "de.regasus.report.wizard.common.ParticipantTypeWizardPage";

	private ParticipantTypeModel participantTypeModel;
	private ListViewer listViewer;
	private IParticipantTypesReportParameter participantTypesReportParameter;


	public ParticipantTypeWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.ParticipantTypes.getString());
		setDescription(ReportWizardI18N.ParticipantTypeWizardPage_Description_Participant);
		participantTypeModel = ParticipantTypeModel.getInstance();
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

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				ParticipantType participantType = (ParticipantType) element;
				return participantType.getName().getString(Locale.getDefault());
			}
		});
		listViewer.setSorter(new ViewerSorter());
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IParticipantTypesReportParameter) {
			try {
				participantTypesReportParameter = (IParticipantTypesReportParameter) reportParameter;

				// init models
				initModel(reportParameter);


				if (listViewer != null) {
					List<Long> participantTypePKs = participantTypesReportParameter.getParticipantTypePKs();
					if (participantTypePKs == null || participantTypePKs.isEmpty()) {
						listViewer.setSelection(new StructuredSelection());
					}
					else {
						List<ParticipantType> participantTypes = new ArrayList<>(participantTypePKs.size());

						for (Long pk : participantTypePKs) {
							ParticipantType participantType = participantTypeModel.getParticipantType(pk);
							participantTypes.add(participantType);
						}

						listViewer.setSelection(new StructuredSelection(participantTypes), true);
					}
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	}


	private void initModel(IReportParameter reportParameter) {
		/* Wenn die reportParameter auch vom Typ IEventReportParameter sind und einen eventPK enthalten,
		 * werden nur die ParticipantTypes dieses Events geladen, sonst alle.
		 */
		Long eventPK = null;
		Collection<ParticipantType> participantTypes = null;
		if (reportParameter instanceof IEventReportParameter) {
			IEventReportParameter eventReportParameter = (IEventReportParameter) reportParameter;
			eventPK = eventReportParameter.getEventPK();
		}

		try {
			if (eventPK == null) {
				participantTypes = participantTypeModel.getAllUndeletedParticipantTypes();
			}
			else {
				participantTypes = participantTypeModel.getParticipantTypesByEvent(eventPK);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		listViewer.setInput(participantTypes);
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		List<ParticipantType> participantTypes = new ArrayList<>( selection.size() );

		for (Iterator<ParticipantType> it = selection.iterator(); it.hasNext();) {
			ParticipantType participantType = it.next();
			participantTypes.add(participantType);
		}

		if (participantTypesReportParameter != null) {
			List<Long> participantTypePKs = null;
			String description = null;

			if ( ! participantTypes.isEmpty()) {
				participantTypePKs = new ArrayList<>(participantTypes.size());

				StringBuilder desc = new StringBuilder();
				desc.append(ParticipantLabel.ParticipantTypes.getString());
				desc.append(": ");

				String language = Locale.getDefault().getLanguage();

				int i = 0;
				for (ParticipantType participantType : participantTypes) {
					participantTypePKs.add(participantType.getId());
					if (i++ > 0) {
						desc.append(", ");
					}
					desc.append(participantType.getName().getString(language));
				}

				description = desc.toString();
			}

			participantTypesReportParameter.setParticipantTypePKs(participantTypePKs);
			participantTypesReportParameter.setDescription(
				IParticipantTypesReportParameter.DESCRIPTION_ID,
				description
			);
		}
	}

}
