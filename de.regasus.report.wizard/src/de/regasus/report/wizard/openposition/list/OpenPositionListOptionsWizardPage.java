package de.regasus.report.wizard.openposition.list;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.invoice.report.openPositionList.OpenPositionListOrder;
import com.lambdalogic.messeinfo.invoice.report.openPositionList.OpenPositionListReportParameter;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class OpenPositionListOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "OpenPositionListOptionsWizardPage";

	private OpenPositionListReportParameter parameter;


	// Widgets
	private Button participantNumberButton;
	private Button participantNameButton;
	private Button totalAmountButton;


	public OpenPositionListOptionsWizardPage() {
		super(ID);
		setTitle("Data Options");
		setDescription(ReportWizardI18N.OpenPositionListOptionsWizardPage_Description);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		//
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);


		final Group sortingGroup = new Group(composite, SWT.NONE);
		final GridData gd_sortingGroup = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		sortingGroup.setLayoutData(gd_sortingGroup);
		sortingGroup.setText(UtilI18N.Sorting);
		final GridLayout sortingGridLayout = new GridLayout();
		sortingGroup.setLayout(sortingGridLayout);

		participantNumberButton = new Button(sortingGroup, SWT.RADIO);
		final String participantNumberLabel = ParticipantSearch.NO.getLabel();
		participantNumberButton.setText(participantNumberLabel);
		participantNumberButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (!ModifySupport.isDeselectedRadioButton(event)) {
						parameter.setOpenPositionListOrder(OpenPositionListOrder.PARTICIPANT_NUMBER);

						I18NPattern description = new I18NPattern();
						description.append(UtilI18N.Sorting);
						description.append(": ");
						description.append(ParticipantSearch.NO.getLabel());
						parameter.setDescription(OpenPositionListReportParameter.DESCRIPTION_ID_ORDER, description.getString());
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		participantNameButton = new Button(sortingGroup, SWT.RADIO);
		final String participantNameLabel = ParticipantSearch.LAST_NAME.getLabel() + ", " + ParticipantSearch.FIRST_NAME.getLabel();
		participantNameButton.setText(participantNameLabel);
		participantNameButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (!ModifySupport.isDeselectedRadioButton(event)) {
						parameter.setOpenPositionListOrder(OpenPositionListOrder.PARTICIPANT_NAME);

						I18NPattern description = new I18NPattern();
						description.append(UtilI18N.Sorting);
						description.append(": ");
						description.append(participantNameLabel);
						parameter.setDescription(OpenPositionListReportParameter.DESCRIPTION_ID_ORDER, description.getString());
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		totalAmountButton = new Button(sortingGroup, SWT.RADIO);
		final String totalAmountLabel = ParticipantSearch.BALANCE_AMOUNT.getLabel();
		totalAmountButton.setText(totalAmountLabel);
		totalAmountButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (!ModifySupport.isDeselectedRadioButton(event)) {
						parameter.setOpenPositionListOrder(OpenPositionListOrder.TOTAL_AMOUNT);

						I18NPattern description = new I18NPattern();
						description.append(UtilI18N.Sorting);
						description.append(": ");
						description.append(ParticipantSearch.BALANCE_AMOUNT.getLabel());
						parameter.setDescription(OpenPositionListReportParameter.DESCRIPTION_ID_ORDER, description.getString());
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof OpenPositionListReportParameter) {
			parameter = (OpenPositionListReportParameter) reportParameter;

			OpenPositionListOrder oplOrder = parameter.getOpenPositionListOrder();

			if (oplOrder != null) {
				switch (oplOrder) {
				case PARTICIPANT_NUMBER:
					participantNumberButton.setSelection(true);
					break;

				case PARTICIPANT_NAME:
					participantNameButton.setSelection(true);
					break;

				case TOTAL_AMOUNT:
					totalAmountButton.setSelection(true);
					break;
				}
			}
		}
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
