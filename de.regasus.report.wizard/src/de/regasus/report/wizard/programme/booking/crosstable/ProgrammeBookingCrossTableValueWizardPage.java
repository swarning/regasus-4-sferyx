package de.regasus.report.wizard.programme.booking.crosstable;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.report.programmeBookingCrossTable.AValue;
import com.lambdalogic.messeinfo.participant.report.programmeBookingCrossTable.BValue;
import com.lambdalogic.messeinfo.participant.report.programmeBookingCrossTable.ProgrammeBookingCrossTableReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ProgrammeBookingCrossTableValueWizardPage
	extends WizardPage
	implements IReportWizardPage {

//	private static Logger log = Logger.getLogger("ui.ProgrammeBookingCrossTableValueWizardPage");
	public static final String ID = "de.regasus.report.wizard.programme.booking.crosstable.ProgrammeBookingCrossTableValueWizardPage"; 

	private ProgrammeBookingCrossTableReportParameter reportParameter;


	// Widgets
	private ListViewer aValueListViewer;
	private ListViewer bValueListViewer;


	public ProgrammeBookingCrossTableValueWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.ProgrammeBookingCrossTableValueWizardPage_Title);
		setDescription(ReportWizardI18N.ProgrammeBookingCrossTableValueWizardPage_Description);
	}


	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		//
		setControl(container);

		final SashForm sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite leftComposite = new Composite(sashForm, SWT.NONE);
		leftComposite.setLayout(new GridLayout());

		final Label leftValueLabel = new Label(leftComposite, SWT.NONE);
		leftValueLabel.setText(ReportWizardI18N.ProgrammeBookingCrossTableValueWizardPage_AValue);

		aValueListViewer = new ListViewer(leftComposite, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		aValueListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		aValueListViewer.setContentProvider(new ArrayContentProvider());
		aValueListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AValue) element).getLabel();
			}
		});
		aValueListViewer.setSorter(new ViewerSorter());
		aValueListViewer.setInput(AValue.values());
		aValueListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				AValue aValue = null;
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					aValue = (AValue) selection.getFirstElement();
				}
				setSelectedAValue(aValue);
			}
		});

		final Composite rightComposite = new Composite(sashForm, SWT.NONE);
		rightComposite.setLayout(new GridLayout());

		final Label rightValueLabel = new Label(rightComposite, SWT.NONE);
		rightValueLabel.setText(ReportWizardI18N.ProgrammeBookingCrossTableValueWizardPage_BValue);

		bValueListViewer = new ListViewer(rightComposite, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		bValueListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bValueListViewer.setContentProvider(new ArrayContentProvider());
		bValueListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((BValue) element).getLabel();
			}
		});
		bValueListViewer.setSorter(new ViewerSorter());
		bValueListViewer.setInput(BValue.values());
		bValueListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				BValue bValue = null;
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					bValue = (BValue) selection.getFirstElement();
				}
				setSelectedBValue(bValue);
			}
		});


		sashForm.setWeights(new int[] {1, 1 });
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ProgrammeBookingCrossTableReportParameter) {
			this.reportParameter = (ProgrammeBookingCrossTableReportParameter) reportParameter;

			AValue aValue = this.reportParameter.getAValue();
			if (aValue != null) {
				aValueListViewer.setSelection(new StructuredSelection(aValue), true);
				setSelectedAValue(aValue);
			}
			else {
				aValueListViewer.setSelection(new StructuredSelection());
				setSelectedAValue(null);
			}


			BValue bValue = this.reportParameter.getBValue();
			if (bValue != null) {
				bValueListViewer.setSelection(new StructuredSelection(bValue), true);
				setSelectedBValue(bValue);
			}
			else {
				bValueListViewer.setSelection(new StructuredSelection());
				setSelectedBValue(null);
			}

		}
	}


	public void setSelectedAValue(AValue aValue) {
		if (reportParameter != null) {
			reportParameter.setAValue(aValue);

			StringBuilder desc = new StringBuilder();
			if (aValue != null) {
				desc.append(ReportWizardI18N.ProgrammeBookingCrossTableValueWizardPage_AValue);
				desc.append(": "); 
				desc.append(aValue.getLabel());
			}

			reportParameter.setDescription(
				ProgrammeBookingCrossTableReportParameter.DESCRIPTION_A_VALUE,
				desc.toString()
			);
		}

		setPageComplete(isPageComplete());
	}


	public void setSelectedBValue(BValue bValue) {
		if (reportParameter != null) {
			reportParameter.setBValue(bValue);

			StringBuilder desc = new StringBuilder();
			if (bValue != null) {
				desc.append(ReportWizardI18N.ProgrammeBookingCrossTableValueWizardPage_BValue);
				desc.append(": "); 
				desc.append(bValue.getLabel());
			}

			reportParameter.setDescription(
				ProgrammeBookingCrossTableReportParameter.DESCRIPTION_B_VALUE,
				desc.toString()
			);
		}

		setPageComplete(isPageComplete());
	}


	@Override
	public boolean isPageComplete() {
		return reportParameter.getAValue() != null && reportParameter.getBValue() != null;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
