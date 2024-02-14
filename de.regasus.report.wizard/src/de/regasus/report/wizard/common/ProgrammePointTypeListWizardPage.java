package de.regasus.report.wizard.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.messeinfo.participant.report.parameter.IProgrammePointTypeListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.programme.programmepointtype.ProgrammePointTypeTable;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointTypeListWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.common.ProgrammePointTypeListWizardPage";

	private static ProgrammePointTypeModel pptModel = ProgrammePointTypeModel.getInstance();
	private static ProgrammePointModel ppModel = ProgrammePointModel.getInstance();

	private String language;

	private Map<Long, ProgrammePointTypeVO> programmePointTypeMap;

	private TableViewer tableViewer;
	private IProgrammePointTypeListReportParameter programmePointTypeListReportParameter;
	private ProgrammePointTypeTable programmePointTypeTable;


	public ProgrammePointTypeListWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.ProgrammePointTypes.getString());
		setDescription(ReportWizardI18N.ProgrammePointTypeListWizardPage_Description);
		language = Locale.getDefault().getLanguage();
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

		Table table = new Table(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		{
			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(400);
			tableColumn.setText(UtilI18N.Name);
		}
		{
			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(100);
			tableColumn.setText(ParticipantLabel.ProgrammePointType_ReferenceCode.getString());
		}

		programmePointTypeTable = new ProgrammePointTypeTable(table);
		tableViewer = programmePointTypeTable.getViewer();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( ! event.getSelection().isEmpty() );
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IProgrammePointTypeListReportParameter) {
			programmePointTypeListReportParameter = (IProgrammePointTypeListReportParameter) reportParameter;

			initData();

			if (tableViewer != null) {
				List<Long> programmePointTypePKs = programmePointTypeListReportParameter.getProgrammePointTypePKs();
				StructuredSelection selection = null;
				if (programmePointTypePKs != null) {
					List<ProgrammePointTypeVO> programmePointTypeVOs = new ArrayList<>( programmePointTypePKs.size() );
					for (Long pk : programmePointTypePKs) {
						ProgrammePointTypeVO programmePointTypeVO = programmePointTypeMap.get(pk);
						if (programmePointTypeVO != null) {
							programmePointTypeVOs.add(programmePointTypeVO);
						}
					}
					selection = new StructuredSelection(programmePointTypeVOs);
				}
				else {
					selection = new StructuredSelection();
				}

				tableViewer.setSelection(selection);
			}
		}
	}


	private void initData() {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						final List<ProgrammePointTypeVO> ppVOs = pptModel.getAllUndeletedProgrammePointTypeVOs();
						ProgrammePointTypeListWizardPage.this.programmePointTypeMap = AbstractVO.abstractVOs2Map(ppVOs);

						SWTHelper.syncExecDisplayThread(new Runnable() {
							@Override
							public void run() {
								programmePointTypeTable.setInput(ppVOs);
							}
						});
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

		List<ProgrammePointTypeVO> programmePointTypeVOs = new ArrayList<>( selection.size() );
		List<Long> programmePointTypePKs = new ArrayList<>( selection.size() );

		for (Iterator<ProgrammePointTypeVO> it = selection.iterator(); it.hasNext();) {
			ProgrammePointTypeVO programmePointTypeVO = it.next();
			programmePointTypeVOs.add(programmePointTypeVO);
			programmePointTypePKs.add( programmePointTypeVO.getID() );
		}

		if (programmePointTypeListReportParameter != null) {
			programmePointTypeListReportParameter.setProgrammePointTypePKs(programmePointTypePKs);

			StringBuilder description = new StringBuilder();
			description.append(ParticipantLabel.ProgrammePointTypes.getString());
			description.append(": ");
			int i = 0;
			for (ProgrammePointTypeVO programmePointTypeVO : programmePointTypeVOs) {
				if (i++ > 0) {
					description.append(", ");
				}
				description.append( programmePointTypeVO.getName().getString(language) );
			}

			programmePointTypeListReportParameter.setDescription(
				IProgrammePointTypeListReportParameter.DESCRIPTION_ID,
				description.toString()
			);
		}
	}

}
