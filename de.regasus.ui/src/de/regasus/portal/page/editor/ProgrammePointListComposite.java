package de.regasus.portal.page.editor;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.portal.Page;
import de.regasus.portal.PageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.ProgrammePointIdListProvider;
import de.regasus.portal.Section;
import de.regasus.portal.component.Component;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointLabelProvider;
import de.regasus.programme.programmepoint.ProgrammePointProvider;
import de.regasus.programme.programmepoint.dialog.ProgrammePointSelectionDialog;
import de.regasus.ui.Activator;

public class ProgrammePointListComposite extends Composite {

	private ProgrammePointModel ppModel = ProgrammePointModel.getInstance();

	// the entity
	private ProgrammePointIdListProvider programmePointIdListProvider;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Long portalPK;
	private Long eventPK;


	// **************************************************************************
	// * Widgets
	// *

	private ListViewer listViewer;
	private Button editButton;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammePointListComposite(
		Composite parent,
		int style,
		Long portalPK
	) {
		super(parent, style);

		this.portalPK = Objects.requireNonNull(portalPK);

		try {
    		// load Portal to get eventPK
    		Portal portal = PortalModel.getInstance().getPortal(portalPK);
    		eventPK = portal.getEventId();

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
		label.setText( ParticipantLabel.ProgrammePoints.getString() );
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
		listViewer.setLabelProvider(new ProgrammePointLabelProvider());
		listViewer.getList().addKeyListener(keyListener);


		// column 3
		editButton = new Button(this, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		editButton.setText(UtilI18N.Edit + UtilI18N.Ellipsis);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editProgrammePointSelection();
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


	private List<ProgrammePointCVO> getSelection() {
		List<ProgrammePointCVO> selectedEntities = null;
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		if (selection != null) {
			selectedEntities = selection.toList();
		}
		return selectedEntities;

	}


	private String getCopyInfoFromSelection() {
		List<ProgrammePointCVO> ppCVOs = getSelection();
		StringBuilder text = new StringBuilder();
		for (ProgrammePointCVO ppCVO : ppCVOs) {
			if (text.length() > 0) {
				text.append("\n");
			}
			text.append( ppCVO.getCopyInfo() );
		}

		return text.toString();
	}


	private String getSelectedIdsAsText() {
		List<ProgrammePointCVO> ppCVOs = getSelection();
		StringBuilder text = new StringBuilder();
		for (ProgrammePointCVO ppCVO : ppCVOs) {
			if (text.length() > 0) {
				text.append(", ");
			}
			text.append( ppCVO.getPK() );
		}

		return text.toString();
	}


	private void editProgrammePointSelection() {
		// open ProgrammePointSelectionDialog
		//	with eventPK and List of ProgrammePointPKs that must not be shown,
		//	cause they are already selected in another ProgrammeBookingComponent

		List<Long> selectedProgrammePointIds = programmePointIdListProvider.getProgrammePointIdList();

		ProgrammePointProvider programmePointProvider = new ProgrammePointProvider(eventPK);

		programmePointProvider.setBlackListProgrammePointPKs( getUsedProgrammePointPKs() );

		ProgrammePointSelectionDialog dialog = new ProgrammePointSelectionDialog(
			getShell(),
			programmePointProvider,
			new ListSet<>(selectedProgrammePointIds)
		);
		int dialogResult = dialog.open();

		if (dialogResult == ProgrammePointSelectionDialog.OK) {
			// get selected Programme Points from dialog
			List<ProgrammePointCVO> oldSelection = (List<ProgrammePointCVO>) listViewer.getInput();
			List<ProgrammePointCVO> newSelection = dialog.getSelectedProgrammePointCVOs();

			List<Long> oldSelectionPKs = ProgrammePointCVO.getPKs(oldSelection);
			List<Long> newSelectionPKs = ProgrammePointCVO.getPKs(newSelection);

			if ( ! EqualsHelper.isEqual(newSelectionPKs, oldSelectionPKs) ) {
    			// set new selection of Programme Points to programmePointListViewer
    			listViewer.setInput(newSelection);
    			modifySupport.fire();
			}
		}
	}


	/**
	 * Determine PKs of Programme Points already used in other ProgrammeBookingComponents of the same Page.
	 * @return
	 */
	private List<Long> getUsedProgrammePointPKs() {
		List<Long> usedProgrammePointPKs = new ArrayList<>();
		try {
			Page page = getPage();
			if (page != null) {
				List<Section> sectionList = page.getSectionList();
				if (sectionList != null) {
    				for (Section section : sectionList) {
    					List<Component> componentList = section.getComponentList();
    					if (componentList != null) {
							for (Component component : componentList) {
        						if (component instanceof ProgrammePointIdListProvider) {
        							ProgrammePointIdListProvider ppIdListOwner = (ProgrammePointIdListProvider) component;
        							List<Long> programmePointIds = ppIdListOwner.getProgrammePointIdList();
        							if (programmePointIds != null) {
        								usedProgrammePointPKs.addAll(programmePointIds);
        							}
        						}
        					}
    					}
    				}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return usedProgrammePointPKs;
	}


	private Page getPage() {
		try {
    		List<Page> pages = PageModel.getInstance().getPagesByPortal(portalPK);
    		if (pages != null) {
    			for (Page page : pages) {
    				List<Section> sectionList = page.getSectionList();
    				if (sectionList != null) {
        				for (Section section : sectionList) {
        					List<Component> componentList = section.getComponentList();
        					if (componentList != null) {
    							for (Component component : componentList) {
            						if ( component.equals(component) ) {
            							return page;
            						}
            					}
        					}
        				}
    				}
    			}
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void syncWidgetsToEntity() {
		if (programmePointIdListProvider != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						List<ProgrammePointCVO> ppCVOs = Collections.emptyList();
						List<Long> programmePointIds = programmePointIdListProvider.getProgrammePointIdList();
						if ( notEmpty(programmePointIds) ) {
							ppCVOs = ppModel.getProgrammePointCVOs(programmePointIds);
						}
						listViewer.setInput(ppCVOs);
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


	public void setProgrammePointIdListProvider(ProgrammePointIdListProvider programmePointIdListProvider) {
		this.programmePointIdListProvider = programmePointIdListProvider;
		syncWidgetsToEntity();
	}


	public List<Long> getProgrammePointIds() {
		List<ProgrammePointCVO> ppCVOs = (List<ProgrammePointCVO>) listViewer.getInput();
		List<Long> programmePointIds = ProgrammePointCVO.getPKs(ppCVOs);
		return programmePointIds;
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
