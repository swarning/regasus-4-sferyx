package de.regasus.report.wizard.common;

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
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.report.parameter.IProgrammePointReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointTable;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.common.ProgrammePointWizardPage";

	private String language;

	private Map<Long, ProgrammePointVO> programmePointMap;

	private TableViewer tableViewer;
	private IProgrammePointReportParameter programmePointReportParameter;
	private ProgrammePointTable programmePointTable;

	private SelectionMode selectionMode;


	public ProgrammePointWizardPage(SelectionMode selectionMode) {
		super(ID);
		this.selectionMode = selectionMode;
		setTitle(ParticipantLabel.ProgrammePoint.getString());
		setDescription(ReportWizardI18N.ProgrammePointWizardPage_Description);
		language = Locale.getDefault().getLanguage();
	}


	public ProgrammePointWizardPage() {
		this(SelectionMode.MULTI_SELECTION);
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

		Table table = new Table(container, selectionMode.getSwtStyle() | SWT.BORDER);
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
			tableColumn.setText(ParticipantLabel.ProgrammePoint_StartTime.getString());
		}
		{
			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(100);
			tableColumn.setText(ParticipantLabel.ProgrammePoint_EndTime.getString());
		}


		programmePointTable = new ProgrammePointTable(table);
		tableViewer = programmePointTable.getViewer();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IProgrammePointReportParameter) {
			programmePointReportParameter = (IProgrammePointReportParameter) reportParameter;

			initData();

			if (tableViewer != null) {
				Long programmePointPK = programmePointReportParameter.getProgrammePointPK();
				StructuredSelection selection = null;
				if (programmePointPK != null) {
					ProgrammePointVO programmePointVO = programmePointMap.get(programmePointPK);
					if (programmePointVO != null) {
						selection = new StructuredSelection(programmePointVO);
					}
					else {
						selection = new StructuredSelection();
					}
				}
				else {
					selection = new StructuredSelection();
				}

				tableViewer.setSelection(selection);
			}
		}
	}


	private void initData() {
		final Long eventPK = programmePointReportParameter.getEventPK();

		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						final List<ProgrammePointVO> programmePointVOs = ProgrammePointModel.getInstance().getProgrammePointVOsByEventPK(eventPK);
						programmePointMap = AbstractVO.abstractVOs2Map(programmePointVOs);

						SWTHelper.syncExecDisplayThread(new Runnable() {
							@Override
							public void run() {
								programmePointTable.setInput(programmePointVOs);
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
		ProgrammePointVO programmePointVO = (ProgrammePointVO) selection.getFirstElement();
		if (programmePointReportParameter != null) {
			programmePointReportParameter.setProgrammePointPK(programmePointVO.getID());

			StringBuilder desc = new StringBuilder();
			desc.append(ParticipantLabel.ProgrammePoint.getString());
			desc.append(": ");
			desc.append(programmePointVO.getName(language));

			programmePointReportParameter.setDescription(
				IProgrammePointReportParameter.DESCRIPTION_ID,
				desc.toString()
			);
		}
	}

}
