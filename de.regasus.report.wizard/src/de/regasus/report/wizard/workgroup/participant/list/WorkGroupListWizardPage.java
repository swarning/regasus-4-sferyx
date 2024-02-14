package de.regasus.report.wizard.workgroup.participant.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.WorkGroupCVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.WorkGroupCVOSettings;
import com.lambdalogic.messeinfo.participant.report.parameter.IWorkGroupListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.programme.WorkGroupModel;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class WorkGroupListWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.common.WorkGroupListWizardPage";

	private static final WorkGroupCVOSettings workGroupCVOSettings;

	private Map<Long, WorkGroupCVO> workGroupMap;

	private TableViewer tableViewer;
	private IWorkGroupListReportParameter workGroupListReportParameter;
	private WorkGroupTable workGroupTable;


	static {
		workGroupCVOSettings = new WorkGroupCVOSettings();
		workGroupCVOSettings.programmePointCVOSettings = new ProgrammePointCVOSettings();
	}

	public WorkGroupListWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.WorkGroups.getString());
		setDescription(ReportWizardI18N.WorkGroupListWizardPage_Description);
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

		final Table table = new Table(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);


		{
			final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(200);
			tableColumn.setText(ParticipantLabel.WorkGroup.getString());
		}
		{
			final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(300);
			tableColumn.setText(ParticipantLabel.ProgrammePoint.getString());
		}
		{
			final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(100);
			tableColumn.setText(UtilI18N.BeginTime);
		}
		{
			final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(100);
			tableColumn.setText(UtilI18N.EndTime);
		}
		{
			final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setWidth(100);
			tableColumn.setText(ParticipantLabel.WorkGroup_Location.getString());
		}

		workGroupTable = new WorkGroupTable(table);
		tableViewer = workGroupTable.getViewer();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IWorkGroupListReportParameter) {
			workGroupListReportParameter = (IWorkGroupListReportParameter) reportParameter;

			initData();

			if (tableViewer != null) {
				List<Long> workGroupPKs = workGroupListReportParameter.getWorkGroupPKs();
				StructuredSelection selection = null;
				if (workGroupPKs != null) {
					List<WorkGroupCVO> workGroupCVOs = new ArrayList<>(workGroupPKs.size());
					for (Long pk : workGroupPKs) {
						WorkGroupCVO workGroupCVO = workGroupMap.get(pk);
						if (workGroupCVO != null) {
							workGroupCVOs.add(workGroupCVO);
						}
					}
					selection = new StructuredSelection(workGroupCVOs);
				}
				else {
					selection = new StructuredSelection();
				}

				tableViewer.setSelection(selection);
			}
		}
	}


	private void initData() {
		final Long eventPK = workGroupListReportParameter.getEventPK();

		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						final List<WorkGroupCVO> workGroupCVOs = WorkGroupModel.getInstance().getWorkGroupCVOsByEventPK(
							eventPK,
							workGroupCVOSettings
						);
						workGroupMap = AbstractCVO.abstractCVOs2Map(workGroupCVOs);

						SWTHelper.syncExecDisplayThread(new Runnable() {
							@Override
							public void run() {
								workGroupTable.setInput(workGroupCVOs);
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
		List<WorkGroupCVO> workGroupCVOs = new ArrayList<>( selection.size() );
		List<Long> workGroupPKs = new ArrayList<>( selection.size() );
		for (Iterator<WorkGroupCVO> it = selection.iterator(); it.hasNext();) {
			WorkGroupCVO workGroupCVO = it.next();
			workGroupCVOs.add(workGroupCVO);
			workGroupPKs.add( workGroupCVO.getPK() );
		}

		if (workGroupListReportParameter != null) {
			workGroupListReportParameter.setWorkGroupPKs(workGroupPKs);

			StringBuilder desc = new StringBuilder();
			desc.append(ParticipantLabel.WorkGroups.getString());
			desc.append(": ");
			int i = 0;
			for (WorkGroupCVO workGroupCVO : workGroupCVOs) {
				if (i++ > 0) {
					desc.append(", ");
				}
				desc.append(workGroupCVO.getVO().getName());
			}


			workGroupListReportParameter.setDescription(
				IWorkGroupListReportParameter.DESCRIPTION_ID,
				desc.toString()
			);
		}
	}

}
