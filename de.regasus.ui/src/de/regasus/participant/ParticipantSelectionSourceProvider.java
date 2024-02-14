package de.regasus.participant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.lambdalogic.messeinfo.invoice.data.InvoiceCVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.participant.ParticipantModel;
import de.regasus.participant.command.MakeGroupManagerCommandHandler;
import de.regasus.participant.editor.ISaveListener;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.view.ParticipantSearchView;

public class ParticipantSelectionSourceProvider extends AbstractSourceProvider {

	// name of the variable participantSelectionState ...
	private final static String PARTICIPANT_SELECTION_STATE = "de.regasus.event.participantSelectionState";
	// ... and its values
	private final static String PARTICIPANT_SELECTION_STATE_NONE = "none";
	private final static String PARTICIPANT_SELECTION_STATE_ONE = "one";
	private final static String PARTICIPANT_SELECTION_STATE_MANY = "many";


	// name of the variable groupMemberSelectionState ...
	private final static String GROUP_MEMBER_SELECTION_STATE = "de.regasus.event.groupMemberSelectionState";
	// ... and its values
	private final static String GROUP_MEMBER_SELECTION_STATE_UNDEFINED = "undefined";
	private final static String GROUP_MEMBER_SELECTION_STATE_NONE = "none";
	private final static String GROUP_MEMBER_SELECTION_STATE_ALL = "all";
	private final static String GROUP_MEMBER_SELECTION_STATE_SOME = "some";

	// name of the variable groupManagerSelectionState ...
	private final static String GROUP_MANAGER_SELECTION_STATE = "de.regasus.event.groupManagerSelectionState";
	// ... and its values
	private final static String GROUP_MANAGER_SELECTION_STATE_UNDEFINED = "undefined";
	private final static String GROUP_MANAGER_SELECTION_STATE_NONE = "none";
	private final static String GROUP_MANAGER_SELECTION_STATE_ALL = "all";
	private final static String GROUP_MANAGER_SELECTION_STATE_SOME = "some";

	// name of the variable companionSelectionState ...
	private final static String COMPANION_SELECTION_STATE = "de.regasus.event.companionSelectionState";
	// ... and its values
	private final static String COMPANION_SELECTION_STATE_UNDEFINED = "undefined";
	private final static String COMPANION_SELECTION_STATE_NONE = "none";
	private final static String COMPANION_SELECTION_STATE_ALL = "all";
	private final static String COMPANION_SELECTION_STATE_SOME = "some";

	// name of the variable personLinkSelectionState ...
	private final static String PERSON_LINK_SELECTION_STATE = "de.regasus.event.personLinkSelectionState";
	// ... and its values
	private final static String PERSON_LINK_SELECTION_STATE_UNDEFINED = "undefined";
	private final static String PERSON_LINK_SELECTION_STATE_NONE = "none";
	private final static String PERSON_LINK_SELECTION_STATE_ALL = "all";
	private final static String PERSON_LINK_SELECTION_STATE_SOME = "some";


	// initial values of variables
	private String participantSelectionState = PARTICIPANT_SELECTION_STATE_NONE;
	private String groupMemberSelectionState = GROUP_MEMBER_SELECTION_STATE_UNDEFINED;
	private String groupManagerSelectionState = GROUP_MANAGER_SELECTION_STATE_UNDEFINED;
	private String companionSelectionState = COMPANION_SELECTION_STATE_UNDEFINED;
	private String personLinkSelectionState = PERSON_LINK_SELECTION_STATE_UNDEFINED;

	// selected participants
	private List<IParticipant> participantList = new ArrayList<>();

	/**
	 * Safe the window because we cannot get it in dispose() with
	 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().
	 */
	private IWorkbenchWindow window;

	/**
	 * The currently visible ParticipantProvider if it is a Part.
	 * Necessary because when closing one of two ParticipantEditors,
	 * the events for closing may be arrive after the event for
	 * activating the second editor.
	 * This field allows us to ignore close- and deactivate-events
	 * if they doesn't belong to the currently visible part.
	 */
	private IWorkbenchPart currentParticipantProviderPart = null;

	private boolean verbose = false;



	public ParticipantSelectionSourceProvider() {
		super();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			init(window);
		}
		else {
			PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

				@Override
				public void windowOpened(IWorkbenchWindow window) {
					init(window);
					PlatformUI.getWorkbench().removeWindowListener(this);
				}

				@Override
				public void windowDeactivated(IWorkbenchWindow arg0) {
				}

				@Override
				public void windowClosed(IWorkbenchWindow arg0) {
				}

				@Override
				public void windowActivated(IWorkbenchWindow arg0) {
				}
			});
		}

		ParticipantEditor.addSaveListener(saveListener);
	}


	private void init(IWorkbenchWindow window) {
		this.window = window;

		window.getSelectionService().addSelectionListener(selectionListener);
		window.getPartService().addPartListener(partListener);

		ParticipantModel.getInstance().addListener(modelListener);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(selectionListener);
		window.getPartService().removePartListener(partListener);

		ParticipantModel.getInstance().removeListener(modelListener);
	}


	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<>(4);
		currentState.put(PARTICIPANT_SELECTION_STATE, participantSelectionState);
		currentState.put(GROUP_MEMBER_SELECTION_STATE, groupMemberSelectionState);
		currentState.put(GROUP_MANAGER_SELECTION_STATE, groupManagerSelectionState);
		currentState.put(COMPANION_SELECTION_STATE, companionSelectionState);
		currentState.put(PERSON_LINK_SELECTION_STATE, personLinkSelectionState);
		return currentState;
	}


	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {
			PARTICIPANT_SELECTION_STATE,
			GROUP_MEMBER_SELECTION_STATE,
			GROUP_MANAGER_SELECTION_STATE,
			COMPANION_SELECTION_STATE,
			PERSON_LINK_SELECTION_STATE,
		};
	}


	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
			printIfVerbose("ParticipantSelectionSourceProvider.selectionChanged(IWorkbenchPart part, ISelection incoming)");
			printIfVerbose("	IWorkbenchPart: " +  part);
			printIfVerbose("	ISelection: " +  incoming);

			participantList.clear();

			if (incoming instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) incoming;
				printIfVerbose("	ISelection is IStructuredSelection, size: " + selection.size());
				int i = 0;
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object selectedElement = it.next();
					if (selectedElement instanceof IParticipant) {
						IParticipant iParticipant = (IParticipant) selectedElement;
						printIfVerbose("	" + (i++) + " is IParticipant, no: " + iParticipant.getNumber());
						participantList.add(iParticipant);
					}
					else if (selectedElement instanceof ParticipantProvider) {
						ParticipantProvider participantProvider = (ParticipantProvider) selectedElement;
						printIfVerbose("	" + (i++) + " is ParticipantProvider, no: " + participantProvider.getIParticipant().getNumber());
						IParticipant iParticipant = participantProvider.getIParticipant();
						participantList.add(iParticipant);
					}
					else if (selectedElement instanceof InvoiceCVO && ((InvoiceCVO)selectedElement).getRecipientCVO() instanceof ParticipantCVO) {
						InvoiceCVO invoiceCVO = (InvoiceCVO) selectedElement;
						ParticipantCVO participantCVO = (ParticipantCVO) invoiceCVO.getRecipientCVO();
						printIfVerbose("	" + (i++) + " is InvoiceCVO, no: " + participantCVO.getNumber());
						participantList.add(participantCVO);
					}
					else {
						printIfVerbose("	" + (i++) + " is no IParticipant and ParticipantProvider, participantList.clear(), break");
						participantList.clear();
						break;
					}
				}
			}

			refreshVariables();
		}
	};


	private IPartListener partListener = new IPartListener() {

    	@Override
    	public void partActivated(IWorkbenchPart part) {
    		printIfVerbose("ParticipantSelectionSourceProvider.partActivated(IWorkbenchPart part)");
    //		if (part instanceof ParticipantProvider) {
    //			ParticipantProvider participantProvider = (ParticipantProvider) part;
    //			IParticipant participant = participantProvider.getIParticipant();
    //			printIfVerbose("partActivated: " + participant.getNumber());
    //		}
    		handlePart(part, true);
    	}


    	@Override
    	public void partBroughtToTop(IWorkbenchPart part) {
    		printIfVerbose("ParticipantSelectionSourceProvider.partBroughtToTop(IWorkbenchPart part)");
    //		if (part instanceof ParticipantProvider) {
    //			ParticipantProvider participantProvider = (ParticipantProvider) part;
    //			IParticipant participant = participantProvider.getIParticipant();
    //			printIfVerbose("partBroughtToTop: " + participant.getNumber());
    //		}
    		handlePart(part, true);
    	}


    	@Override
    	public void partClosed(IWorkbenchPart part) {
    		printIfVerbose("ParticipantSelectionSourceProvider.partClosed(IWorkbenchPart part)");
    //		if (part instanceof ParticipantProvider) {
    //			ParticipantProvider participantProvider = (ParticipantProvider) part;
    //			IParticipant participant = participantProvider.getIParticipant();
    //			printIfVerbose("partClosed: " + participant.getNumber());
    //		}
    		handlePart(part, false);
    	}


    	@Override
    	public void partDeactivated(IWorkbenchPart part) {
    		printIfVerbose("ParticipantSelectionSourceProvider.partDeactivated(IWorkbenchPart part)");
    //		if (part instanceof ParticipantProvider) {
    //			ParticipantProvider participantProvider = (ParticipantProvider) part;
    //			IParticipant participant = participantProvider.getIParticipant();
    //			printIfVerbose("partDeactivated: " + participant.getNumber());
    //		}
    		handlePart(part, false);
    	}


    	@Override
    	public void partOpened(IWorkbenchPart part) {
    		printIfVerbose("ParticipantSelectionSourceProvider.partOpened(IWorkbenchPart part)");
    //		if (part instanceof ParticipantProvider) {
    //			ParticipantProvider participantProvider = (ParticipantProvider) part;
    //			IParticipant participant = participantProvider.getIParticipant();
    //			printIfVerbose("partOpened: " + participant.getNumber());
    //		}
    		handlePart(part, true);
    	}

	};


	private void handlePart(IWorkbenchPart part, boolean visible) {
		printIfVerbose("ParticipantSelectionSourceProvider.handlePart(IWorkbenchPart part, boolean visible)");
		printIfVerbose("	part: " + part);
		printIfVerbose("	visible: " + visible);
		printIfVerbose("	part == currentParticipantProviderPart: " + (part == currentParticipantProviderPart));

		if (part instanceof ParticipantProvider) {
			printIfVerbose("	part is ParticipantProvider");
			if (visible || part == currentParticipantProviderPart) {
				participantList.clear();
				ParticipantProvider participantProvider = null;
				if (visible) {
					participantProvider = (ParticipantProvider) part;
					IParticipant participant = participantProvider.getIParticipant();
					if (participant != null) {
						participantList.add(participant);
					}
				}
				currentParticipantProviderPart = part;
				refreshVariables();
			}
		}
	}


	private void refreshVariables() {
		printIfVerbose("ParticipantSelectionSourceProvider.refreshVariables()");
		int participantCount = 0;
		int groupMemberCount = 0;
		int groupManagerCount = 0;
		int companionCount = 0;
		int personLinkCount = 0;

		if (participantList != null) {
			for (IParticipant iParticipant : participantList) {
				if (iParticipant != null && iParticipant.getPK() != null) {
					participantCount++;

					if (iParticipant.isInGroup()) {
						groupMemberCount++;
						if (iParticipant.isGroupManager()) {
							groupManagerCount++;
						}
					}

					if (iParticipant.isCompanion()) {
						companionCount++;
					}

					if (iParticipant.getPersonLink() != null) {
						personLinkCount++;
					}
				}
			}
		}

		printIfVerbose("	--- counts ---");
		printIfVerbose("	participantCount:  " + participantCount);
		printIfVerbose("	groupMemberCount:  " + groupMemberCount);
		printIfVerbose("	groupManagerCount: " + groupManagerCount);
		printIfVerbose("	companionCount:    " + companionCount);
		printIfVerbose("	personLinkCount:   " + personLinkCount);

		String oldParticipantSelectionState = participantSelectionState;
		String oldGroupMemberSelectionState = groupMemberSelectionState;
		String oldGroupManagerSelectionState = groupManagerSelectionState;
		String oldCompanionSelectionState = companionSelectionState;
		String oldPersonLinkSelectionState = personLinkSelectionState;

		// set participantSelectionState
		if (participantCount == 0) {
			participantSelectionState = PARTICIPANT_SELECTION_STATE_NONE;
		}
		else if (participantCount == 1) {
			participantSelectionState = PARTICIPANT_SELECTION_STATE_ONE;
		}
		else {
			participantSelectionState = PARTICIPANT_SELECTION_STATE_MANY;
		}

		// set groupMemberSelectionState
		if (participantCount == 0) {
			groupMemberSelectionState = GROUP_MEMBER_SELECTION_STATE_UNDEFINED;
		}
		else if (groupMemberCount == 0) {
			groupMemberSelectionState = GROUP_MEMBER_SELECTION_STATE_NONE;
		}
		else if (groupMemberCount < participantCount) {
			groupMemberSelectionState = GROUP_MEMBER_SELECTION_STATE_SOME;
		}
		else if (groupMemberCount == participantCount) {
			groupMemberSelectionState = GROUP_MEMBER_SELECTION_STATE_ALL;
		}
		else {
			// darf eigentlich nicht eintreten
			groupMemberSelectionState = GROUP_MEMBER_SELECTION_STATE_UNDEFINED;
		}

		// set groupManagerSelectionState
		if (participantCount == 0) {
			groupManagerSelectionState = GROUP_MANAGER_SELECTION_STATE_UNDEFINED;
		}
		else if (groupManagerCount == 0) {
			groupManagerSelectionState = GROUP_MANAGER_SELECTION_STATE_NONE;
		}
		else if (groupManagerCount < participantCount) {
			groupManagerSelectionState = GROUP_MANAGER_SELECTION_STATE_SOME;
		}
		else if (groupManagerCount == participantCount) {
			groupManagerSelectionState = GROUP_MANAGER_SELECTION_STATE_ALL;
		}
		else {
			// darf eigentlich nicht eintreten
			groupManagerSelectionState = GROUP_MANAGER_SELECTION_STATE_UNDEFINED;
		}

		// set companionSelectionState
		if (participantCount == 0) {
			companionSelectionState = COMPANION_SELECTION_STATE_UNDEFINED;
		}
		else if (companionCount == 0) {
			companionSelectionState = COMPANION_SELECTION_STATE_NONE;
		}
		else if (companionCount < participantCount) {
			companionSelectionState = COMPANION_SELECTION_STATE_SOME;
		}
		else if (companionCount == participantCount) {
			companionSelectionState = COMPANION_SELECTION_STATE_ALL;
		}
		else {
			// darf eigentlich nicht eintreten
			companionSelectionState = COMPANION_SELECTION_STATE_UNDEFINED;
		}

		// set personLinkSelectionState
		if (participantCount == 0) {
			personLinkSelectionState = PERSON_LINK_SELECTION_STATE_UNDEFINED;
		}
		else if (personLinkCount == 0) {
			personLinkSelectionState = PERSON_LINK_SELECTION_STATE_NONE;
		}
		else if (personLinkCount < participantCount) {
			personLinkSelectionState = PERSON_LINK_SELECTION_STATE_SOME;
		}
		else if (personLinkCount == participantCount) {
			personLinkSelectionState = PERSON_LINK_SELECTION_STATE_ALL;
		}
		else {
			// darf eigentlich nicht eintreten
			personLinkSelectionState = PERSON_LINK_SELECTION_STATE_UNDEFINED;
		}


		if (participantSelectionState != oldParticipantSelectionState ||
			groupMemberSelectionState != oldGroupMemberSelectionState ||
			groupManagerSelectionState != oldGroupManagerSelectionState ||
			companionSelectionState != oldCompanionSelectionState ||
			personLinkSelectionState != oldPersonLinkSelectionState) {

			Map<String, String> currentState = new HashMap<>(3);

			if (participantSelectionState != oldParticipantSelectionState) {
				currentState.put(PARTICIPANT_SELECTION_STATE, participantSelectionState);
			}

			if (groupMemberSelectionState != oldGroupMemberSelectionState) {
				currentState.put(GROUP_MEMBER_SELECTION_STATE, groupMemberSelectionState);
			}

			if (groupManagerSelectionState != oldGroupManagerSelectionState) {
				currentState.put(GROUP_MANAGER_SELECTION_STATE, groupManagerSelectionState);
			}

			if (companionSelectionState != oldCompanionSelectionState) {
				currentState.put(COMPANION_SELECTION_STATE, companionSelectionState);
			}

			if (personLinkSelectionState != oldPersonLinkSelectionState) {
				currentState.put(PERSON_LINK_SELECTION_STATE, personLinkSelectionState);
			}

			printIfVerbose("	--- states changed ---");
			printIfVerbose("	participantSelectionState:  " + participantSelectionState);
			printIfVerbose("	groupMemberSelectionState:  " + groupMemberSelectionState);
			printIfVerbose("	groupManagerSelectionState: " + groupManagerSelectionState);
			printIfVerbose("	companionSelectionState:    " + companionSelectionState);
			printIfVerbose("	personLinkSelectionState:   " + personLinkSelectionState);

			printIfVerbose("fireSourceChanged(ISources.WORKBENCH, currentState): " + currentState);
			fireSourceChanged(ISources.WORKBENCH, currentState);
		}
		else {
			printIfVerbose("	--- states not changed ---");
		}

		// Please don't do this, since output that at each kind of event makes that
		// i cannot copy and paste anything in the applications console view
		// printIfVerbose("participantSelectionState=" + participantSelectionState);
	}


	private void printIfVerbose(String string) {
		if (verbose) {
			System.out.println(string);
		}

	}


	public static ParticipantSelectionSourceProvider getInstance() {
		ParticipantSelectionSourceProvider psp = null;

		IWorkbench workbench = PlatformUI.getWorkbench();
		ISourceProviderService service = (ISourceProviderService) workbench.getService(ISourceProviderService.class);
		ISourceProvider[] sourceProviders = service.getSourceProviders();

		for (ISourceProvider iSourceProvider : sourceProviders) {
			if (iSourceProvider instanceof ParticipantSelectionSourceProvider) {
				psp = (ParticipantSelectionSourceProvider) iSourceProvider;
				break;
			}
		}

		return psp;
	}


	/**
	 * Fix for MIRCP-970 - Menu didn't change because it's contents are
	 * determined by the current selection, and just changing entity attributes
	 * isn't seen by the selection service.
	 * As solution we are observing the ParticipantModel for changes.
	 *
	 * Important to know here: Selected participants might not be available in the ParticipantModel, because the
	 * PartcipantSearchView shows data from ParticipantSearchModel whose data is "only" ParticipantSearchData.
	 *
	 * Sadly we don't receive a key for changed Participants in case they aren't loaded yet from the server.
	 * Also we don't want to load blindly all participants in the current list of selected participants.
	 * As a compromise we reload at most one participant and fire the event to recompute the commands available in menus.
	 * <p>
	 * The case when participants aren't loaded may occur when eg the {@link MakeGroupManagerCommandHandler}
	 * is called for participants selected in the {@link ParticipantSearchView}.
	 *
	 */
	private CacheModelListener<Long> modelListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if (   event.getOperation() == CacheModelOperation.UPDATE
    			|| event.getOperation() == CacheModelOperation.REFRESH
    		) {

    			try {
    				if (participantList.size() == 1) {
    					IParticipant iParticipant = participantList.get(0);
    					Participant participant = ParticipantModel.getInstance().getParticipant(iParticipant.getPK());
    					participantList.set(0, participant);
    					refreshVariables();
    				}
    			}
    			catch (Exception e) {
    				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
    			}
    		}
    	}
	};


	private ISaveListener saveListener = new ISaveListener() {
    	@Override
    	public void saved(Object source, boolean create) {
    		if (create) {
    			handlePart(currentParticipantProviderPart, true);
    		}
    	}
	};

}
