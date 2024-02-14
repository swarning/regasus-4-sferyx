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
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.report.parameter.IProgrammePointListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointTable;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointListWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.common.ProgrammePointListWizardPage";

	private static ProgrammePointModel ppModel = ProgrammePointModel.getInstance();

	private String language;

	private Map<Long, ProgrammePointVO> programmePointMap;

	private TableViewer tableViewer;
	private IProgrammePointListReportParameter programmePointListReportParameter;
	private ProgrammePointTable programmePointTable;


	public ProgrammePointListWizardPage() {
		super(ID);
		setTitle(ParticipantLabel.ProgrammePoints.getString());
		setDescription(ReportWizardI18N.ProgrammePointListWizardPage_Description);
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
				setPageComplete( ! event.getSelection().isEmpty() );
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IProgrammePointListReportParameter) {
			programmePointListReportParameter = (IProgrammePointListReportParameter) reportParameter;

			initData();

			if (tableViewer != null) {
				List<Long> programmePointPKs = programmePointListReportParameter.getProgrammePointPKs();
				StructuredSelection selection = null;
				if (programmePointPKs != null) {
					List<ProgrammePointVO> programmePointVOs = new ArrayList<>(programmePointPKs.size());
					for (Long pk : programmePointPKs) {
						ProgrammePointVO programmePointVO = programmePointMap.get(pk);
						if (programmePointVO != null) {
							programmePointVOs.add(programmePointVO);
						}
					}
					selection = new StructuredSelection(programmePointVOs);
				}
				else {
					selection = new StructuredSelection();
				}

				tableViewer.setSelection(selection);
			}
		}
	}


	private void initData() {
		final Long eventPK = programmePointListReportParameter.getEventPK();

		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						final List<ProgrammePointVO> ppVOs = ppModel.getProgrammePointVOsByEventPK(eventPK);
						ProgrammePointListWizardPage.this.programmePointMap = AbstractVO.abstractVOs2Map(ppVOs);

						SWTHelper.syncExecDisplayThread(new Runnable() {
							@Override
							public void run() {
								programmePointTable.setInput(ppVOs);
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

		List<ProgrammePointVO> programmePointVOs = new ArrayList<>( selection.size() );
		List<Long> programmePointPKs = new ArrayList<>( selection.size() );

		for (Iterator<ProgrammePointVO> it = selection.iterator(); it.hasNext();) {
			ProgrammePointVO programmePointVO = it.next();
			programmePointVOs.add(programmePointVO);
			programmePointPKs.add( programmePointVO.getID() );
		}

		if (programmePointListReportParameter != null) {
			programmePointListReportParameter.setProgrammePointPKs(programmePointPKs);

			StringBuilder description = new StringBuilder();
			description.append(ParticipantLabel.ProgrammePoints.getString());
			description.append(": ");
			int i = 0;
			for (ProgrammePointVO programmePointVO : programmePointVOs) {
				if (i++ > 0) {
					description.append(", ");
				}
				description.append(programmePointVO.getName(language));
			}

			programmePointListReportParameter.setDescription(
				IProgrammePointListReportParameter.DESCRIPTION_ID,
				description.toString()
			);
		}
	}

}
