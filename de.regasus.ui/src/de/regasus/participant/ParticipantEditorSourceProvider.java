package de.regasus.participant;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;

import de.regasus.core.ServerModel;
import de.regasus.event.EventModel;
import de.regasus.participant.editor.BadgesComposite;
import de.regasus.participant.editor.ParticipantEditor;

/**
 * Provides variables for the state within an active {@link ParticipantEditor}.
 */
public class ParticipantEditorSourceProvider extends AbstractSourceProvider {

	// provided variables
	private static final String IS_ONE_BADGE_SELECTED = "de.regasus.participant.editor.isOneBadgeSelected";
	private static final String IS_ONSITE_WORKFLOW_AVAILABLE = "de.regasus.participant.editor.isOnsiteWorkflowAvailable";

	// values used for variables
	private static final String YES = "yes";
	private static final String NO = "no";

	/**
	 * In the eclipse workbench, there is always at most one selected {@link IWorkbenchPart}; in case this part is the
	 * ParticipantEditor, it is referred to in the variable, otherwise it is null.
	 */
	private ParticipantEditor currentParticipantEditor = null;

	private Map<String, String> stateMap = new HashMap<>(2);

	private EventModel eventModel = EventModel.getInstance();


	/**
	 * This SourceProvider gets constructed before a workbench window is opened, so we need to be informed when that
	 * moment has come to be able to hook ourself into that window as part listener.
	 */
	public ParticipantEditorSourceProvider() {
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

			@Override
			public void windowOpened(IWorkbenchWindow window) {
			}

			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
				window.getPartService().removePartListener(partListener);
			}

			@Override
			public void windowClosed(IWorkbenchWindow window) {
			}

			@Override
			public void windowActivated(IWorkbenchWindow window) {
				window.getPartService().addPartListener(partListener);
			}

		});
	}


	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {
			IS_ONE_BADGE_SELECTED,
			IS_ONSITE_WORKFLOW_AVAILABLE,
		};
	}


	@Override
	public Map<String, String> getCurrentState() {
		return stateMap;
	}


	@Override
	public void dispose() {
	}


	private IPartListener partListener = new  IPartListener() {

    	/**
    	 * When a ParticipantEditor is deactivated, we need deregister as listener so that it can be garbage collected, and
    	 * until further activation of any other ParticipantEditor there is no finance selection.
    	 */
    	@Override
    	public void partDeactivated(IWorkbenchPart part) {
    		if (currentParticipantEditor != null && currentParticipantEditor == part) {
    			currentParticipantEditor.getTabFolder().removeSelectionListener(selectionListener);
    			currentParticipantEditor.removeBadgeSelectionChangedListener(selectionChangedListener);
    			currentParticipantEditor = null;
    			update();
    		}
    	}


    	/**
    	 * When a ParticipantEditor is activated, we need to keep an eye on the selected tabs and the possible selections in
    	 * the FinanceComposite and keep the {@link #stateMap} always in sync.
    	 */
    	@Override
    	public void partActivated(IWorkbenchPart part) {
    		if (part instanceof ParticipantEditor) {
    			currentParticipantEditor = (ParticipantEditor) part;
    			currentParticipantEditor.getTabFolder().addSelectionListener(selectionListener);
    			currentParticipantEditor.addBadgeSelectionChangedListener(selectionChangedListener);
    			update();
    		}
    	}


    	/**
    	 * After a part is brought to top, it gets still activated, so no action needed
    	 */
    	@Override
    	public void partBroughtToTop(IWorkbenchPart part) {
    	}


    	/**
    	 * Before a part gets closed, it gets already deactivated, so no action needed
    	 */
    	@Override
    	public void partClosed(IWorkbenchPart part) {
    	}


    	/**
    	 * After a part is opened, it gets still activated, so no action needed
    	 */
    	@Override
    	public void partOpened(IWorkbenchPart part) {
    	}

	};


	/**
	 * SelectionListener which get called when the selected tab of the tab folder of the current
	 * participant editor changes.
	 */
	private SelectionListener selectionListener = new SelectionAdapter() {
    	@Override
    	public void widgetSelected(SelectionEvent e) {
    		update();
    	}
	};


	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
    	@Override
    	public void selectionChanged(SelectionChangedEvent event) {
    		update();
    	}
	};


	private void update() {
		for(String key : getProvidedSourceNames()) {
			stateMap.put(key, NO);
		}

		if (! ServerModel.getInstance().isLoggedIn() ) {
			return;
		}

		// Check if participant editor or invoice seach view is active at all
		if (currentParticipantEditor == null ) {
			fireSourceChanged(ISources.WORKBENCH, stateMap);
			return;
		}

		// ============ The initialization for ParticipantEditor ================

		if (currentParticipantEditor != null) {
			// Check workflow availability
			Participant participant = currentParticipantEditor.getParticipant();
			if (participant.getPK() != null) {
				try {
					EventVO eventVO = eventModel.getEventVO(participant.getEventId());
					String onsiteWorkflow = eventVO.getOnsiteWorkflow();
					if (! StringHelper.isEmpty(onsiteWorkflow)) {
						stateMap.put(IS_ONSITE_WORKFLOW_AVAILABLE, YES);
					}
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}



			// Check if finance tab is visible at all
			TabItem[] tabItems = currentParticipantEditor.getTabFolder().getSelection();
			if (tabItems.length == 0 || !(tabItems[0].getControl() instanceof BadgesComposite)) {
				fireSourceChanged(ISources.WORKBENCH, stateMap);
				return;
			}

			// We fetch  the selected badges
			if (currentParticipantEditor.getSelectedBadge() != null) {
				stateMap.put(IS_ONE_BADGE_SELECTED, YES);
			}
		}


		// Now to the individual command handlers

		fireSourceChanged(ISources.WORKBENCH, stateMap);
	}

}
