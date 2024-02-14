package de.regasus.portal.page.editor.react.profile;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collections;
import java.util.List;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.ProgrammePointTypeIdListProvider;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.programme.programmepointtype.ProgrammePointTypeLabelProvider;
import de.regasus.programme.programmepointtype.ProgrammePointTypeProvider;
import de.regasus.programme.programmepointtype.dialog.ProgrammePointTypeSelectionDialog;
import de.regasus.ui.Activator;

public class ProgrammePointTypeListComposite extends Composite {

	private ProgrammePointTypeModel pptModel = ProgrammePointTypeModel.getInstance();

	// the entity
	private ProgrammePointTypeIdListProvider programmePointTypeIdListProvider;

	protected ModifySupport modifySupport = new ModifySupport(this);


	// **************************************************************************
	// * Widgets
	// *

	private ListViewer listViewer;
	private Button editButton;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammePointTypeListComposite(Composite parent, int style) {
		super(parent, style);

		try {
			createWidgets();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void createWidgets() throws Exception {
		setLayout( new GridLayout(3, false) );

		// column 1
		Label label = new Label(this, SWT.NONE);
		label.setText( ParticipantLabel.ProgrammePointTypes.getString() );
		GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.TOP)
			.indent(0, SWTConstants.VERTICAL_INDENT)
			.applyTo(label);


		// column 2
		org.eclipse.swt.widgets.List ppList = new org.eclipse.swt.widgets.List(this, SWT.V_SCROLL | SWT.BORDER);
		ppList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listViewer = new ListViewer(ppList);
		listViewer.setContentProvider( ArrayContentProvider.getInstance() );
		listViewer.setLabelProvider(new ProgrammePointTypeLabelProvider());
		listViewer.getList().addKeyListener(keyListener);


		// column 3
		editButton = new Button(this, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		editButton.setText(UtilI18N.Edit + UtilI18N.Ellipsis);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editProgrammePointTypeSelection();
			}
		});
	}


	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
			// run CopyAction when user presses ctrl+c or ⌘+c
			if (e.keyCode == 'c' && e.stateMask == SWT.MOD1) {
				ClipboardHelper.copyToClipboard( getCopyInfoFromSelection() );
			}
			else if (e.keyCode == 'c' && e.stateMask == (SWT.MOD1 | SWT.SHIFT)) {
				ClipboardHelper.copyToClipboard( getSelectedIdsAsText() );
			}
		};
	};


	private List<ProgrammePointTypeVO> getSelection() {
		List<ProgrammePointTypeVO> selectedEntities = null;
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		if (selection != null) {
			selectedEntities = selection.toList();
		}
		return selectedEntities;

	}


	private String getCopyInfoFromSelection() {
		List<ProgrammePointTypeVO> pptVOs = getSelection();
		StringBuilder text = new StringBuilder();
		for (ProgrammePointTypeVO pptVO : pptVOs) {
			if (text.length() > 0) {
				text.append("\n");
			}
			text.append( pptVO.getCopyInfo() );
		}

		return text.toString();
	}


	private String getSelectedIdsAsText() {
		List<ProgrammePointTypeVO> pptVOs = getSelection();
		StringBuilder text = new StringBuilder();
		for (ProgrammePointTypeVO pptVO : pptVOs) {
			if (text.length() > 0) {
				text.append(", ");
			}
			text.append( pptVO.getPK() );
		}

		return text.toString();
	}


	private void editProgrammePointTypeSelection() {
		// open ProgrammePointTypeSelectionDialog

		List<Long> selectedProgrammePointTypeIds = programmePointTypeIdListProvider.getProgrammePointTypeIdList();

		ProgrammePointTypeProvider programmePointTypeProvider = new ProgrammePointTypeProvider();

		ProgrammePointTypeSelectionDialog dialog = new ProgrammePointTypeSelectionDialog(
			getShell(),
			programmePointTypeProvider,
			new ListSet<>(selectedProgrammePointTypeIds)
		);
		int dialogResult = dialog.open();

		if (dialogResult == ProgrammePointTypeSelectionDialog.OK) {
			// get selected Programme Point Types from dialog
			List<ProgrammePointTypeVO> oldSelection = (List<ProgrammePointTypeVO>) listViewer.getInput();
			List<ProgrammePointTypeVO> newSelection = dialog.getSelectedProgrammePointTypeVOs();

			List<Long> oldSelectionPKs = ProgrammePointTypeVO.getPKs(oldSelection);
			List<Long> newSelectionPKs = ProgrammePointTypeVO.getPKs(newSelection);

			if ( ! EqualsHelper.isEqual(newSelectionPKs, oldSelectionPKs) ) {
    			// set new selection of Programme Points to programmePointListViewer
    			listViewer.setInput(newSelection);
    			modifySupport.fire();
			}
		}
	}


	private void syncWidgetsToEntity() {
		if (programmePointTypeIdListProvider != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						List<ProgrammePointTypeVO> pptVOs = Collections.emptyList();
						List<Long> programmePointTypeIds = programmePointTypeIdListProvider.getProgrammePointTypeIdList();
						if ( notEmpty(programmePointTypeIds) ) {
							pptVOs = pptModel.getProgrammePointTypeVOs(programmePointTypeIds);
						}
						listViewer.setInput(pptVOs);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	public void setProgrammePointTypeIdListProvider(ProgrammePointTypeIdListProvider programmePointTypeIdListProvider) {
		this.programmePointTypeIdListProvider = programmePointTypeIdListProvider;
		syncWidgetsToEntity();
	}


	public List<Long> getProgrammePointTypeIds() {
		List<ProgrammePointTypeVO> pptVOs = (List<ProgrammePointTypeVO>) listViewer.getInput();
		List<Long> programmePointTypeIds = ProgrammePointTypeVO.getPKs(pptVOs);
		return programmePointTypeIds;
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
