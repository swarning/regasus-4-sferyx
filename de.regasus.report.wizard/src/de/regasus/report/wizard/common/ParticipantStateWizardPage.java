package de.regasus.report.wizard.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.report.parameter.IParticipantStatesReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantStateWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "de.regasus.report.wizard.common.ParticipantStateWizardPage";

	private ParticipantStateModel participantStateModel;
	private ListViewer listViewer;
	private IParticipantStatesReportParameter participantStatesReportParameter;


	public ParticipantStateWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.ParticipantStates.getString());
		setDescription(ReportWizardI18N.ParticipantStateWizardPage_Description_Participant);
		participantStateModel = ParticipantStateModel.getInstance();
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
				ParticipantState participantState = (ParticipantState) element;
				return participantState.getString();
			}
		});
		listViewer.setSorter(new ViewerSorter());
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IParticipantStatesReportParameter) {
			try {
				participantStatesReportParameter = (IParticipantStatesReportParameter) reportParameter;

				// init models
				try {
					listViewer.setInput(participantStateModel.getParticipantStates());
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}

				if (listViewer != null) {
					List<Long> participantStatePKs = participantStatesReportParameter.getParticipantStatePKs();
					if (participantStatePKs == null || participantStatePKs.isEmpty()) {
						listViewer.setSelection(new StructuredSelection());
					}
					else {
						List<ParticipantState> participantStates = new ArrayList<>(participantStatePKs.size());

						for (Long pk : participantStatePKs) {
							ParticipantState participantState = participantStateModel.getParticipantState(pk);
							participantStates.add(participantState);
						}

						listViewer.setSelection(new StructuredSelection(participantStates), true);
					}
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		List<ParticipantState> participantStates = new ArrayList<>(selection.size());

		for (Iterator<ParticipantState> it = selection.iterator(); it.hasNext();) {
			ParticipantState participantState = it.next();
			participantStates.add(participantState);
		}

		if (participantStatesReportParameter != null) {
			List<Long> participantStatePKs = null;
			String description = null;

			if ( ! participantStates.isEmpty()) {
				participantStatePKs = new ArrayList<>( participantStates.size() );

				StringBuilder desc = new StringBuilder();
				desc.append(ParticipantLabel.ParticipantStates.getString());
				desc.append(": ");

				int i = 0;
				for (ParticipantState participantState : participantStates) {
					participantStatePKs.add( participantState.getID() );
					if (i++ > 0) {
						desc.append(", ");
					}
					desc.append(participantState.getString());
				}

				description = desc.toString();
			}

			participantStatesReportParameter.setParticipantStatePKs(participantStatePKs);
			participantStatesReportParameter.setDescription(
				IParticipantStatesReportParameter.DESCRIPTION_ID,
				description
			);
		}
	}

}
