package de.regasus.programme.programmepoint.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointTable;
import de.regasus.ui.Activator;


public class ProgrammePointWizardPage extends WizardPage{

	public static final String ID = "de.regasus.event.wizard.ProgrammePointWizardPage";

	private ProgrammePointModel programmePointModel;
	private TableViewer tableViewer;
	private ProgrammePointTable programmePointTable;

	private ModifySupport modifySupport = new ModifySupport();

	private Long eventPK;
	private List<Long> initialProgrammePointPKs;
	private boolean multiSelection;
	private boolean allowNoSelection;


	private List<ProgrammePointVO> selectedProgrammePointVOs = new ArrayList<>();


	public ProgrammePointWizardPage(
		String title,
		String description,
		Long eventPK,
		List<Long> initialProgrammePointPKs,
		boolean multiSelection,
		boolean allowNoSelection
	) {
		super(ID);
		setTitle(title);
		setDescription(description);

		this.eventPK = eventPK;
		this.initialProgrammePointPKs = initialProgrammePointPKs;
		this.multiSelection = multiSelection;
		this.allowNoSelection = allowNoSelection;

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
			if (multiSelection) {
				tableStyle = tableStyle | SWT.MULTI;
			}
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
					selectedProgrammePointVOs = SelectionHelper.toList( event.getSelection() );
					checkPageComplete();
					modifySupport.fire(table);
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
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
				programmePointTable.setInput(programmePointVOs);

				if (initialProgrammePointPKs != null && !initialProgrammePointPKs.isEmpty()) {
					List<ProgrammePointVO> initialProgrammePointVOs = programmePointModel.getProgrammePointVOs(initialProgrammePointPKs);
					if (initialProgrammePointVOs != null) {
						tableViewer.setSelection(new StructuredSelection(initialProgrammePointVOs), true);
					}
				}
			}
			checkPageComplete();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean isPageComplete() {
		boolean complete= allowNoSelection || !selectedProgrammePointVOs.isEmpty();
		return complete;
	}


	private void checkPageComplete() {
		setPageComplete(isPageComplete());
	}

}
