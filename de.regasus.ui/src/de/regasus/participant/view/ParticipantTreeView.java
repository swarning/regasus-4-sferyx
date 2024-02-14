package de.regasus.participant.view;

import static com.lambdalogic.util.rcp.KeyEventHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.tree.TreeNodeContentProvider;
import com.lambdalogic.util.rcp.tree.TreeNodeLabelProvider;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.event.EventIdProvider;
import de.regasus.participant.ParticipantProvider;
import de.regasus.participant.command.EditParticipantCommandHandler;
import de.regasus.participant.editor.ISaveListener;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.view.pref.ParticipantTreeViewPreference;
import de.regasus.person.PersonTreeModel;
import de.regasus.ui.Activator;

public class ParticipantTreeView extends AbstractView implements EventIdProvider {
	/* ISaveListener
	 * The ParticipantTreeView has to be an ISaveListener of the ParticipantEditor,
	 * to sync to these editors when they are saved. Listening to CREATE events of the corresponding models
	 * does'nt work, because the order in which listeners are informed is not deterministic. If the
	 * ParticipantTreeView is informed before the editor, the ProfileRelationView cannot sync to the
	 * editor, because the latter doesn't know the ID of the saved entity (Participant) yet.
	 */


	public static final String ID = "ParticipantTreeView";

	private ParticipantTreeViewPreference preference;

	private boolean ignoreDataChange = false;

	// Actions
	private IWorkbenchAction refreshAction;
	private IWorkbenchAction linkWithEditorAction;

	/**
	 * false until Actions are initialized in initializeActions().
	 */
	private boolean actionsInitialized = false;

	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;

	/**
	 * Variable used to hold the initial value for the linkWithEditorAction that is stored in the
	 * system environment (memento).
	 */
	private Boolean linkWithEditorAction_initialValue;


	/**
	 * EntityType used in find() and show()
	 */
	private Class<?> _entityType;

	/**
	 * Key used in find() and show()
	 */
	private Object _key;

	/**
	 * Current path used in find() and show()
	 */
	private List<TreeNode<?>> _path;


	/**
	 * PK of the participant to which this view is synchronized with.
	 */
	private Long abstractPersonPK = null;

	/**
	 * PK of the participant which is the root of the current tree.
	 */
	private Long rootPK = null;

	/**
	 * List of all Participants shown in the tree.
	 */
	private final List<AbstractPerson> treeDataList = new ArrayList<>();

	private ParticipantTreeNode root;

	private PersonTreeModel personTreeModel;

	// Widgets
	private Label summaryLabel;
	private Button sortByNameButton;
	private Button sortByNumberButton;
	private TreeViewer treeViewer;


	public ParticipantTreeView() {
		personTreeModel = PersonTreeModel.getInstance();
		personTreeModel.addListener(personTreeModelListener);
		ParticipantEditor.addSaveListener(saveListener);

		// observer ServerModel to init from preferences on login and save t preferences on logout
		ServerModel.getInstance().addListener(serverModelListener);

		preference = ParticipantTreeViewPreference.getInstance();
	}


	@Override
	public void dispose() {
		try {
			getSite().getPage().removePartListener(partListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			personTreeModel.removeListener(personTreeModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			ParticipantEditor.removeSaveListener(saveListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.view.AbstractView#isVisible()
	 */
	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getEvent().isVisible();
		}
		return visible;
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.view.AbstractView#createWidgets(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createWidgets(Composite parent) {
		if (isVisible()) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout viewLayout = new GridLayout();
			container.setLayout(viewLayout);

			{
				Composite summaryComposite = new Composite(container, SWT.NONE);
				summaryComposite.setLayoutData(new GridData(SWT.FILL, SWT.VERTICAL, true, false));
				summaryComposite.setLayout(new GridLayout());


				summaryLabel = new Label(summaryComposite, SWT.NONE);
				summaryLabel.setLayoutData(new GridData(SWT.FILL, SWT.VERTICAL, true, false));
				summaryLabel.setText(UtilI18N.Count + ": 0");
				summaryLabel.setToolTipText(I18N.ParticipantTreeView_Count_ToolTip);

				sortByNameButton = new Button(summaryComposite, SWT.RADIO);
				sortByNameButton.setLayoutData(new GridData(SWT.LEFT, SWT.VERTICAL, true, false));
				sortByNameButton.setText(I18N.ParticipantTreeView_sortByNameButton_Name);
				sortByNameButton.setToolTipText(I18N.ParticipantTreeView_sortByNameButton_ToolTip);
				sortByNameButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (sortByNameButton.getSelection()) {
							sortByName();
						}
					}
				});


				sortByNumberButton = new Button(summaryComposite, SWT.RADIO);
				sortByNumberButton.setLayoutData(new GridData(SWT.LEFT, SWT.VERTICAL, true, false));
				sortByNumberButton.setText(I18N.ParticipantTreeView_sortByNumberButton_Name);
				sortByNumberButton.setToolTipText(I18N.ParticipantTreeView_sortByNumberButton_ToolTip);
				sortByNumberButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (sortByNumberButton.getSelection()) {
							sortByNumber();
						}
					}
				});
			}
			{
				Composite treeComposite = new Composite(container, SWT.NONE);
				treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				treeComposite.setLayout(new FillLayout());
				{
					treeViewer = new TreeViewer(treeComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

					treeViewer.setContentProvider(new TreeNodeContentProvider());
					treeViewer.setLabelProvider(new TreeNodeLabelProvider());

					// This call is needed to make tooltips work
					ColumnViewerToolTipSupport.enableFor(treeViewer);

					// copy ID to clipboard when user types ctrl+shift+c or ⌘+shift+c
					treeViewer.getTree().addKeyListener(treeKeyListener);

					// initialize root (after init of treeViewer, before sortByName())
					root = new ParticipantTreeNode(treeViewer, null, null);

					sortByName();
				}
			}


			// make the Tree the SelectionProvider
			getSite().setSelectionProvider(treeViewer);


			// create Actions and add them to different menus
			initializeActions();

			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

			syncToCurrentParticipantProvider();

			getSite().getPage().addPartListener(partListener);

			initFromPreferences();
		}
		else {
			getSite().getPage().removePartListener(partListener);
			// delete current root to enable synchronisation with it again when the view is made visible
			setRoot(null);

			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	private KeyListener treeKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent event) {
			try {
    			if (   isCopy(event)
    				|| isCopyPK(event)
    				|| isCopyVigenere1(event)
    				|| isCopyVigenere1Hex(event)
    				|| isCopyVigenere2(event)
    				|| isCopyVigenere2Hex(event)
    			) {
    				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
    				if (selection != null) {
    					Object selectedElement = selection.getFirstElement();
    					if (selectedElement instanceof ParticipantTreeNode) {
    	    				TreeNode<?> treeNode = (TreeNode<?>) selectedElement;
    	    				if (treeNode != null) {

    	    					// get Participant
    	    					AbstractPerson person = (AbstractPerson) treeNode.getValue();

    	    					// determine clipboard content depending on keys
    	    					String text = null;
    	    					if ( isCopy(event) ) {
    	    						text = person.getCopyInfo();
    	    					}
    	    					else if ( isCopyPK(event) ) {
        	    					Object key = treeNode.getKey();
        	    					if (key != null) {
        	    						text = key.toString() ;
        	    					}
    	    					}
    	    					else if ( isCopyVigenere1(event) ) {
   	    							text = person.getVigenereCode();
    	    					}
    	    					else if ( isCopyVigenere1Hex(event) ) {
   	    							text = person.getVigenereCodeHex();
    	    					}
    	    					else if ( isCopyVigenere2(event) ) {
   	    							text = person.getVigenere2Code();
    	    					}
    	    					else if ( isCopyVigenere2Hex(event) ) {
   	    							text = person.getVigenere2CodeHex();
    	    					}

    	    					ClipboardHelper.copyToClipboard(text);
    	    				}
    					}
    					else {
    						System.err.println("The selected element is not an instance of " + TreeNode.class.getName());
    					}
    				}

    			}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
    	};
	};


	/**
	 * Create the actions, but only once.
	 */
	private void initializeActions() {
		if ( ! actionsInitialized) {
    		refreshAction = new ParticipantTreeRefreshAction(this);
    		linkWithEditorAction = new LinkWithEditorAction(this);
    		linkWithEditorAction.setChecked(Boolean.TRUE.equals(linkWithEditorAction_initialValue)); // maybe null

			initializeToolBar();
			initializeMenu();

			actionsInitialized = true;
		}
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");

		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menuManager) {
				ParticipantTreeView.this.fillContextMenu(menuManager);
			}
		});

		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}


	private void initializeDoubleClickAction() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object treeNode = selection.getFirstElement();
				IWorkbenchPage activePage = getSite().getWorkbenchWindow().getActivePage();

				if (treeNode instanceof ParticipantTreeNode) {
					ParticipantTreeNode participantTreeNode = (ParticipantTreeNode) treeNode;
					Long participantID = participantTreeNode.getParticipantPK();
					EditParticipantCommandHandler.openParticipantEditor(activePage, participantID);
				}
			}
		});
	}


	private void fillContextMenu(IMenuManager menuManager) {
		// The reason for such a segment (that lets dynamically fill the popup-menu every time anew
		// when it get's opened) is that you might add plugins at runtime, and new menu contributions
		// can herewith be shown immediately, without restart.

		// Other plug-ins can contribute there actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator("emailAdditions"));

		menuManager.update(true);
	}


	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		toolBarManager.add(refreshAction);
		toolBarManager.add(linkWithEditorAction);

		// Other plug-ins can contribute there actions here
		toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);
	}


	@Override
	public void setFocus() {
		try {
			if (treeViewer != null) {
    			Tree tree = treeViewer.getTree();
				if (tree != null && !tree.isDisposed() && tree.isEnabled()) {
    				tree.setFocus();
    			}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public Long getEventId() {
		Long eventPK = null;
		if (root != null) {
			List<TreeNode<?>> children = root.getChildren();
			if (children != null && !children.isEmpty()) {
				TreeNode<?> treeNode = children.get(0);

				if (treeNode instanceof ParticipantTreeNode) {
					ParticipantTreeNode participantTreeNode = (ParticipantTreeNode) treeNode;
					eventPK = participantTreeNode.getValue().getEventId();

				}
			}
		}
		return eventPK;
	}


	/**
	 * Checks the current active editor (if any) whether it is a ParticipantProvider
	 * (e.g. ParticipantEditor); if yes updates itself with the participants tree data.
	 *
	 * @see de.regasus.participant.ParticipantProvider#registerForForeignKey()
	 */
	private void syncToCurrentParticipantProvider() {
		try {
			IWorkbenchPart activePart = getSite().getPage().getActivePart();

			if (activePart != null) {

				Class<?> entityType = null;
				Object key = null;

				if (activePart instanceof ParticipantProvider) {
					// determine the new rootPK and set it
					ParticipantProvider participantProvider = (ParticipantProvider) activePart;

					IParticipant newParticipant = participantProvider.getIParticipant();
					abstractPersonPK = newParticipant.getPK();
					Long newRootPK = newParticipant.getRootPK();

					if (abstractPersonPK != null) {
						participantProvider.registerForForeignKey();
					}
					setRoot(newRootPK);

					entityType = Participant.class;
					key = newParticipant.getPK();
				}


				if (linkWithEditorAction.isChecked()) {
					show(entityType, key);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getRootPK() {
		return rootPK;
	}


	private boolean setRoot(Long newRootPK) {
		boolean rootChanged = false;
		try {
			if (rootPK != null && newRootPK == null) {
				List<AbstractPerson> emptyList = Collections.emptyList();
				setTreeDataList(emptyList);

				this.rootPK = null;
				rootChanged = true;
			}
			else if (
				rootPK == null && newRootPK != null ||
				rootPK != null && !rootPK.equals(newRootPK)
			) {
				List<AbstractPerson> participantList = personTreeModel.getTreeData(newRootPK);
				setTreeDataList(participantList);

				this.rootPK = newRootPK;
				rootChanged = true;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return rootChanged;
	}


	private void setTreeDataList(final List<AbstractPerson> newTreeDataList) {
		treeDataList.clear();
		if (newTreeDataList != null) {
			treeDataList.addAll(newTreeDataList);
		}

		if (treeDataList != null) {
			List<AbstractPerson> level1 = new ArrayList<>();
			List<Participant> level2 = new ArrayList<>();
			List<Participant> level3 = new ArrayList<>();

			for (AbstractPerson abstractPerson : treeDataList) {
				if (abstractPerson instanceof Participant) {
					Participant participant = (Participant) abstractPerson;

					switch (participant.getRole()) {
						case Participant.PARTICIPANT_ROLE_NOT_EXISTENT:
							break;
						case Participant.PARTICIPANT_ROLE_INDIVIDUAL:
						case Participant.PARTICIPANT_ROLE_GROUP_MANAGER:
							level1.add(participant);
							break;
						case Participant.PARTICIPANT_ROLE_GROUP_MEMBER:
						case Participant.PARTICIPANT_ROLE_COMPANION_OF_INDIVIDUAL:
							level2.add(participant);
							break;
						case Participant.PARTICIPANT_ROLE_COMPANION_OF_GROUP_MEMBER:
							level3.add(participant);
							break;
					}
				}
			}

			root.removeAll();

			Map<Long, TreeNode<?>> nodeMap = new HashMap<>();
			for (AbstractPerson abstractPerson : level1) {
				if (abstractPerson instanceof Participant) {
					Participant participant = (Participant) abstractPerson;
					ParticipantTreeNode node = new ParticipantTreeNode(treeViewer, root, participant);
					nodeMap.put(abstractPerson.getID(), node);
				}
			}

			for (Participant participant : level2) {
				Long parentID = participant.getCompanionOfPK();
				if (parentID == null) {
					parentID = participant.getGroupManagerPK();
				}

				TreeNode<?> parentNode = nodeMap.get(parentID);

				ParticipantTreeNode node = new ParticipantTreeNode(treeViewer, parentNode, participant);
				nodeMap.put(participant.getID(), node);
			}

			for (Participant participant : level3) {
				Long parentID = participant.getCompanionOfPK();

				TreeNode<?> parentNode = nodeMap.get(parentID);

				new ParticipantTreeNode(treeViewer, parentNode, participant);
			}

			updateGUI();
		}
	}


	protected void updateGUI() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				/* Only if the view is not made invisible by configuration.
				 * Don't check this before this thread is started, because summaryLabel may be
				 * removed in the meantime.
				 */
				if (summaryLabel != null && ! summaryLabel.isDisposed()) {
    				int totalCount = treeDataList.size();
    				int groupMemberCount = 0;
    				for (AbstractPerson abstractPerson : treeDataList) {
    					if (abstractPerson instanceof Participant) {
    						Participant participant = (Participant) abstractPerson;
    						if (!participant.isGroupManager() && !participant.isCancelled()) {
    							groupMemberCount++;
    						}
    					}

    				}
    				String text = UtilI18N.Count + ": " + groupMemberCount + " (" + totalCount + ")";
    				summaryLabel.setText(text);
    				treeViewer.setInput(root);
    				treeViewer.expandAll();
				}
			}
		});
	}


	private void sortByName() {
		sortByNameButton.setSelection(true);

		root.sortByName();

		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object o1, Object o2) {
				if (o1 instanceof ParticipantTreeNode &&
					o2 instanceof ParticipantTreeNode) {

					ParticipantTreeNode node1 = (ParticipantTreeNode) o1;
					ParticipantTreeNode node2 = (ParticipantTreeNode) o2;

					Participant p1 = node1.getValue();
					Participant p2 = node2.getValue();

					String c1 = p1.getName(true);
					String c2 = p2.getName(true);

					return c1.compareTo(c2);
				}
				return super.compare(viewer, o1, o2);
			}
		});
	}


	private void sortByNumber() {
		sortByNumberButton.setSelection(true);

		root.sortByNumber();

		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object o1, Object o2) {
				if (o1 instanceof ParticipantTreeNode &&
					o2 instanceof ParticipantTreeNode) {

					ParticipantTreeNode node1 = (ParticipantTreeNode) o1;
					ParticipantTreeNode node2 = (ParticipantTreeNode) o2;

					Participant p1 = node1.getValue();
					Participant p2 = node2.getValue();

					Integer c1 = p1.getNumber();
					Integer c2 = p2.getNumber();

					return c1.compareTo(c2);
				}
				return super.compare(viewer, o1, o2);
			}
		});
	}


	private CacheModelListener<Long> personTreeModelListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if (ignoreDataChange || abstractPersonPK == null) {
    			return;
    		}

    		try {
    			List<Long> keyList = event.getKeyList();


    			boolean rootChanged = false;
    			// check if the participant itself has been changed
    			if (keyList.isEmpty() || keyList.contains(abstractPersonPK)) {

    				/* Der TN, dessen Tree angezeigt wird, hat sich (möglicherweise) verändert.
    				 * Da diese Änderung auch den RootPK des TN betreffen kann, wird dieser
    				 * neu ermittelt.
    				 */
    				Long newRootPK = null;
    				AbstractPerson abstractPerson = personTreeModel.getAbstractPerson(abstractPersonPK);
    				if (abstractPerson != null) {
    					if (abstractPerson instanceof Participant) {
    						Participant participant = (Participant) abstractPerson;
    						newRootPK = participant.getRootPK();
    						/* newRootPK is ever != null, because participant has an id
    						 * and getRootPK() delivers at least the participants id.
    						 */
    					}

    					rootChanged = setRoot(newRootPK);
    				}
    				else {
    					// The participant wasn't found in model.
    					// This happens if we're not logged in.
    					rootChanged = setRoot(null);
    				}
    			}

    			/* Update if any other participant changed.
    			 * It may be a new group member.
    			 */
    			if (!rootChanged) {
    				// update the tree data if setRoot() was not called

    				for (Long changedAbstractPersonPK : keyList) {
    					// check if the AbstractPerson is shown in the tree
    					boolean isShown = false;
    					for (AbstractPerson abstractPerson : treeDataList) {
    						if (abstractPerson.getID().equals(changedAbstractPersonPK)) {
    							isShown = true;
    							break;
    						}
    					}


    					AbstractPerson changedAbstractPerson = personTreeModel.getAbstractPerson(changedAbstractPersonPK);

    					if (changedAbstractPerson instanceof Participant) {
    						Participant changedParticipant = (Participant) changedAbstractPerson;

    						// check if the participant (now) belongs to the group

    						boolean belongsToGroup = changedParticipant.getRootPK().equals(rootPK);

    						// handle event
    						if (event.getOperation() == CacheModelOperation.CREATE) {
    							if (belongsToGroup) {
    								addToTree(changedParticipant);
    							}
    						}
    						else if (event.getOperation() == CacheModelOperation.DELETE) {
    							if (isShown) {
    								removeFromTree(changedParticipant);
    							}
    						}
    						else {
    							if (belongsToGroup) {
    								if (isShown) {
    									/* There may have been structural changes.
    									 * E.g. a companion could be a companion now or a non companion could be a companion.
    									 */
    									reloadTreeData();
    								}
    								else {

    									if ( changedParticipant.isCompanion() ) {
    										addToTree(changedParticipant);
    									}
    									else {
    										/* The new group member may have companions,
    										 * so the whole tree must be updated.
    										 */
    										refreshTreeData();
    									}
    								}
    							}
    							else if (isShown) {
    								removeFromTree(changedParticipant);
    							}
    						}
    					}
    				}

    				updateGUI();
    			}
    		}
    		catch (Exception e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}
    	}
	};


	protected void reloadTreeData() {
		try {
			List<AbstractPerson> newAbstractPersonList = personTreeModel.getTreeData(rootPK);
			setTreeDataList(newAbstractPersonList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void refreshTreeData() {
		try {
			ignoreDataChange = true;
			personTreeModel.refreshForeignKey(rootPK);

			List<AbstractPerson> newAbstractPersonList = personTreeModel.getTreeData(rootPK);
			setTreeDataList(newAbstractPersonList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}
	}


	protected Participant getParticipant(Long participantPK) {
		for (AbstractPerson abstractPerson : treeDataList) {
			if (abstractPerson.getID().equals(participantPK)) {
				return (Participant)abstractPerson;
			}
		}
		return null;
	}


	protected void removeFromTree(AbstractPerson abstractPerson) {
		// remove from participantList
		treeDataList.remove(abstractPerson);

		// remove from tree
		removeFromTree(abstractPerson, root);
	}


	protected boolean removeFromTree(AbstractPerson abstractPerson, TreeNode<?> treeNode) {
		if (treeNode != null) {
			List<TreeNode<?>> childTreeNodeList = treeNode.getChildren();
			if (childTreeNodeList != null) {
				for (TreeNode<?> childTreeNode : childTreeNodeList) {
					AbstractPerson childPerson = (AbstractPerson) childTreeNode.getValue();
					if (childPerson != null && childPerson.equals(abstractPerson)) {
						treeNode.removeChild(childTreeNode);
						return true;
					}
					else {
						boolean removed = removeFromTree(abstractPerson, childTreeNode);
						if (removed) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	protected void addToTree(Participant participant) {
		Long parentPK = null;
		if (participant.isCompanion()) {
			parentPK = participant.getCompanionOfPK();
		}
		else if (participant.isInGroup()) {
			parentPK = participant.getGroupManagerPK();
		}


		if (parentPK != null) {
			boolean added = addToTree(participant, parentPK, root);
			if (added) {
				// add to participantList
				treeDataList.add(participant);
			}
		}
	}

	protected boolean addToTree(Participant participant, Long parentPK, TreeNode<?> treeNode) {
		if (treeNode != null) {
			List<TreeNode<?>> childTreeNodeList = treeNode.getChildren();
			if (childTreeNodeList != null) {
				for (TreeNode<?> childTreeNode : childTreeNodeList) {

					AbstractPerson person = (AbstractPerson) childTreeNode.getValue();
					if (person != null && person.getID().equals(parentPK)) {
						new ParticipantTreeNode(
							treeViewer,
							childTreeNode,
							participant
						);
						return true;
					}
					else {
						boolean added = addToTree(participant, parentPK, childTreeNode);
						if (added) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	protected void updateTreeNode(Participant participant) {
		boolean updated = updateTreeNode(participant, root);
		if (updated) {
			// update participantList
			treeDataList.remove(participant);
			treeDataList.add(participant);
		}
	}


	protected boolean updateTreeNode(Participant participant, TreeNode treeNode) {
		if (treeNode != null) {
			Participant treeNodeParticipant = (Participant) treeNode.getValue();
			if (treeNodeParticipant != null && treeNodeParticipant.equals(participant)) {
				treeNode.setValue(participant);
				return true;
			}
			else {
				List<TreeNode<?>> childTreeNodeList = treeNode.getChildren();
				if (childTreeNodeList != null) {
					for (TreeNode<?> childTreeNode : childTreeNodeList) {
						boolean updated = updateTreeNode(participant, childTreeNode);
						if (updated) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	private IPartListener2 partListener = new IPartListener2() {
    	@Override
    	public void partActivated(IWorkbenchPartReference partRef) {
    		syncToCurrentParticipantProvider();
    	}

    	@Override
    	public void partBroughtToTop(IWorkbenchPartReference partRef) {
    	}

    	@Override
    	public void partClosed(IWorkbenchPartReference partRef) {
    	}

    	@Override
    	public void partDeactivated(IWorkbenchPartReference partRef) {
    	}

    	@Override
    	public void partHidden(IWorkbenchPartReference partRef) {
    	}

    	@Override
    	public void partInputChanged(IWorkbenchPartReference partRef) {
    	}

    	@Override
    	public void partOpened(IWorkbenchPartReference partRef) {
    	}

    	@Override
    	public void partVisible(IWorkbenchPartReference partRef) {
    	}
	};


	private ISaveListener saveListener = new ISaveListener() {
    	@Override
    	public void saved(Object source, boolean create) {
    		if (create) {
    			// set rot to null to enfoce refreshing the tree even if root has not changed
    			setRoot(null);
    			syncToCurrentParticipantProvider();
    		}
    	}
	};


	public synchronized void show(Class<?> entityType, Object key) {
		try {
			if (entityType != null && key != null) {
				// initialize fields for find()
				_entityType = entityType;
				_key = key;

				if (_path == null) {
					_path = new ArrayList<>();
				}
				_path.add(root);

				// start the search
				if (find()) {
					TreePath treePath = new TreePath(_path.toArray());
					TreeSelection selection = new TreeSelection(treePath);
					treeViewer.setSelection(selection, true/* reveal (make visible) */);
				}

				// reset fields for find()
				_entityType = null;
				_key = null;

				// delete all elements
				_path.clear();
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	/**
	 * Searches recursive for a TreeNode with a given EntityType and Key.
	 *
	 * @return
	 */
	private boolean find() {
		TreeNode<?> treeNode = _path.get(_path.size() - 1);
		if (treeNode.hasChildren()) {
			List<TreeNode<?>> children = treeNode.getLoadedChildren();
			for (TreeNode<?> child : children) {
				_path.add(child);
				if (child.getEntityType() == _entityType && _key.equals(child.getKey())) {
					return true;
				}
				else if (find()) {
					return true;
				}
				_path.remove(_path.size() - 1);
			}
		}

		return false;
	}


	// *****************************************************************************************************************
	// * Preferences
	// *

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if (serverModelEvent.getType() == ServerModelEventType.BEFORE_LOGOUT) {
				// save values to preferences before the logout will remove them
				savePreferences();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						initFromPreferences();
					}
				});
			}
		}
	};


	private void savePreferences() {
		preference.setLinkWithEditor( linkWithEditorAction.isChecked() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
			linkWithEditorAction.setChecked( preference.isLinkWithEditor() );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}
