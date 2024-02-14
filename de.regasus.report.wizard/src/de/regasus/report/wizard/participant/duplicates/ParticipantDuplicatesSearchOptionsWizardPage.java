package de.regasus.report.wizard.participant.duplicates;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.report.duplicates.ParticipantDuplicatesReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ui.search.SQLOperatorLabelProvider;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;


public class ParticipantDuplicatesSearchOptionsWizardPage extends WizardPage implements SelectionListener, IReportWizardPage {

	private ComboViewer[] operatorComboViewers;
	private Button[] operatorActiveButtons;

	private String[] labels = {
		Person.FIRST_NAME.getString(),
		Person.LAST_NAME.getString(),
		Communication.EMAIL1.getString(),
		Address.CITY.getString(),
		Address.FUNCTION.getString(),
		Address.ORGANISATION.getString(),
	};

	private SQLOperator[] sqlOperators = {
		SQLOperator.EQUAL,
		SQLOperator.FUZZY_IGNORE_CASE,
		SQLOperator.FUZZY_LOWER_ASCII,
		SQLOperator.FUZZY_REGEXP,
		SQLOperator.FUZZY_SOUNDEX,
		SQLOperator.FUZZY_TRIGRAMM,
	};

	private ParticipantDuplicatesReportParameter duplicatesReportParameter;


	public ParticipantDuplicatesSearchOptionsWizardPage() {
		super(ParticipantDuplicatesSearchOptionsWizardPage.class.getName());
	}


	@Override
	public void createControl(Composite parent) {
		setTitle(UtilI18N.Options);
		setMessage(ReportWizardI18N.DuplicatesReportWizardMessage.replace("<entities>", ParticipantLabel.Participants.getString()));

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));

		Group comboComposite = new Group(mainComposite, SWT.NONE);
		comboComposite.setLayout(new GridLayout(3, false));
		comboComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		comboComposite.setText(ReportWizardI18N.DuplicatesReportWizardOperators);

		Group textComposite = new Group(mainComposite, SWT.NONE);
		textComposite.setLayout(new GridLayout(2, false));
		textComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		textComposite.setText(ReportWizardI18N.DuplicatesReportWizardOperatorDescription);

		operatorComboViewers = new ComboViewer[labels.length];
		operatorActiveButtons = new Button[labels.length];

		for (int i = 0; i < labels.length; i++) {
			SWTHelper.createLabel(comboComposite, labels[i]);

			Combo combo = new Combo(comboComposite, SWT.READ_ONLY);
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint = 40;
			combo.setLayoutData(gd);
			combo.setVisibleItemCount(12);
			combo.addSelectionListener(this);

			operatorComboViewers[i] = new ComboViewer(combo);
			operatorComboViewers[i].setContentProvider(new ArrayContentProvider());
			operatorComboViewers[i].setLabelProvider(new SQLOperatorLabelProvider());
			operatorComboViewers[i].setInput(sqlOperators);
			operatorComboViewers[i].setSelection(new StructuredSelection(SQLOperator.EQUAL));

			operatorActiveButtons[i] = new Button(comboComposite, SWT.CHECK);
			operatorActiveButtons[i].setText(KernelLabel.active.getString());
			operatorActiveButtons[i].addSelectionListener(this);

			// Give the combo a reference to the button so that it can
			// be set to selected when the user selects the value in the combo
			combo.setData(operatorActiveButtons[i]);
		}

		// Description of the operators
		createRow(textComposite, SQLOperator.EQUAL, ReportWizardI18N.DuplicatesReportWizardOperatorDescription_EQUALS);
		createRow(textComposite, SQLOperator.FUZZY_IGNORE_CASE, ReportWizardI18N.DuplicatesReportWizardOperatorDescription_FUZZY_IGNORE_CASE);
		createRow(textComposite, SQLOperator.FUZZY_LOWER_ASCII, ReportWizardI18N.DuplicatesReportWizardOperatorDescription_FUZZY_LOWER_ASCII);
		createRow(textComposite, SQLOperator.FUZZY_REGEXP, ReportWizardI18N.DuplicatesReportWizardOperatorDescription_FUZZY_REGEXP);
		createRow(textComposite, SQLOperator.FUZZY_SOUNDEX, ReportWizardI18N.DuplicatesReportWizardOperatorDescription_FUZZY_SOUNDEX);
		createRow(textComposite, SQLOperator.FUZZY_TRIGRAMM, ReportWizardI18N.DuplicatesReportWizardOperatorDescription_FUZZY_TRIGRAMM);

		setControl(mainComposite);

		evaluatePageComplete();
	}


	private void createRow(Composite textComposite, SQLOperator operator, String description) {
		Label symbol = SWTHelper.createLabel(textComposite, operator.getSymbol());
		symbol.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		Label label = new Label(textComposite, SWT.NONE);
		label.setText(description);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantDuplicatesReportParameter) {
			duplicatesReportParameter = (ParticipantDuplicatesReportParameter) reportParameter;

			initRow(0, duplicatesReportParameter.getFirstNameOperator());
			initRow(1, duplicatesReportParameter.getLastNameOperator());
			initRow(2, duplicatesReportParameter.getEmail1Operator());
			initRow(3, duplicatesReportParameter.getMainCityOperator());
			initRow(4, duplicatesReportParameter.getFunctionOperator());
			initRow(5, duplicatesReportParameter.getOrganisationOperator());
		}

		evaluatePageComplete();
	}


	private void syncReportParameter() {
		duplicatesReportParameter.setFirstNameOperator(getOperatorForRow(0));
		duplicatesReportParameter.setLastNameOperator(getOperatorForRow(1));
		duplicatesReportParameter.setEmail1Operator(getOperatorForRow(2));
		duplicatesReportParameter.setMainCityOperator(getOperatorForRow(3));
		duplicatesReportParameter.setFunctionOperator(getOperatorForRow(4));
		duplicatesReportParameter.setOrganisationOperator(getOperatorForRow(5));

		evaluatePageComplete();
	}


	private void evaluatePageComplete() {
		boolean anyButtonActive = false;
		for (Button button : operatorActiveButtons) {
			if (button.getSelection()) {
				anyButtonActive = true;
				break;
			}
		}
		setPageComplete(anyButtonActive);
	}


	private void initRow(int rowIdx, SQLOperator sqlOperator) {
		operatorActiveButtons[rowIdx].setSelection(sqlOperator != null);
		if (sqlOperator != null) {
			operatorComboViewers[rowIdx].setSelection(new StructuredSelection(sqlOperator));
		}
	}


	private SQLOperator getOperatorForRow(int rowIdx) {
		SQLOperator sqlOperator = null;
		if (operatorActiveButtons[rowIdx].getSelection()) {
			sqlOperator = SelectionHelper.<SQLOperator>getUniqueSelected(operatorComboViewers[rowIdx]);
		}
		return sqlOperator;
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof Combo) {

			// The checkbox belonging to the selected combo gets ticked.
			Combo combo = (Combo) e.widget;
			Button button = (Button)combo.getData();
			button.setSelection(true);
		}

		syncReportParameter();
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
