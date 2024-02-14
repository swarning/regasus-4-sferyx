package de.regasus.impex.eivfobi.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;

import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointTable;
import de.regasus.programme.programmepoint.dialog.ProgrammePointWizardPage;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;


/**
 * WizardPage that shows Programme Points that have a cmeEventNumber.
 *
 * This implementation is similar to {@link ProgrammePointWizardPage}, but more simple and it shows only
 * Programme Points that have a cmeEventNumber.
 */
public class EIVFoBiProgrammePointWizardPage extends WizardPage{

	public static final String ID = "EIVFoBiProgrammePointWizardPage";

	private ProgrammePointModel programmePointModel;
	private TableViewer tableViewer;
	private ProgrammePointTable programmePointTable;

	private Long eventPK;


	private List<ProgrammePointVO> selectedProgrammePointVOs = new ArrayList<>();


	public EIVFoBiProgrammePointWizardPage(Long eventPK) {
		super(ID);

		setTitle(ParticipantLabel.ProgrammePoint.getString());
		setDescription(ImpexI18N.EIVFoBiCreateWizard_programmePointPageDecription);

		this.eventPK = eventPK;

		programmePointModel = ProgrammePointModel.getInstance();
	}


	@Override
	public void createControl(Composite parent) {
		try {
			Composite container = new Composite(parent, SWT.NULL);
			container.setLayout(new FillLayout());
			//
			setControl(container);

			// set tableStyle depending on parameter multiSelection
			int tableStyle = SWT.FULL_SELECTION | SWT.BORDER;

			final Table table = new Table(container, tableStyle);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			{
				final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
				tableColumn.setWidth(400);
				tableColumn.setText(UtilI18N.Name);
			}
			{
				final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
				tableColumn.setWidth(100);
				tableColumn.setText(ParticipantLabel.ProgrammePoint_StartTime.getString());
			}
			{
				final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
				tableColumn.setWidth(100);
				tableColumn.setText(ParticipantLabel.ProgrammePoint_EndTime.getString());
			}

			programmePointTable = new ProgrammePointTable(table);
			tableViewer = programmePointTable.getViewer();

			tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					selectedProgrammePointVOs.clear();
					if (selection != null && !selection.isEmpty()) {
						for (Iterator<?> it = selection.iterator(); it.hasNext();) {
							ProgrammePointVO programmePointVO = (ProgrammePointVO) it.next();
							selectedProgrammePointVOs.add(programmePointVO);
						}
					}
					checkPageComplete();
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public List<ProgrammePointVO> getProgrammePointVOs() {
		return selectedProgrammePointVOs;
	}


	public ProgrammePointVO getProgrammePointVO() {
		ProgrammePointVO programmePointVO = null;
		if (selectedProgrammePointVOs != null && selectedProgrammePointVOs.size() == 1) {
			programmePointVO = selectedProgrammePointVOs.get(0);
		}
		return programmePointVO;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			init();
		}
		super.setVisible(visible);
	}


	private void init() {
		try {
			if (eventPK != null) {
				// load and show data
				Collection<ProgrammePointVO> programmePointVOs = programmePointModel.getProgrammePointVOsByEventPK(eventPK);

				if (CollectionsHelper.notEmpty(programmePointVOs)) {
					// filter for programme points, that have a cmeEventNo
					for (Iterator<ProgrammePointVO> it = programmePointVOs.iterator(); it.hasNext();) {
						ProgrammePointVO programmePointVO = it.next();
						if (StringHelper.isEmpty(programmePointVO.getCmeEventNo())) {
							it.remove();
						}
					}
				}

				programmePointTable.setInput(programmePointVOs);
			}
			checkPageComplete();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean isPageComplete() {
		boolean complete= !selectedProgrammePointVOs.isEmpty();
		return complete;
	}


	private void checkPageComplete() {
		setPageComplete(isPageComplete());
	}

}
