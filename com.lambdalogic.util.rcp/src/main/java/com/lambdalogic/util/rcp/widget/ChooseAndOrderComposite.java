package com.lambdalogic.util.rcp.widget;

import static com.lambdalogic.util.CollectionsHelper.empty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.tree.ArrayTreeContentProvider;


public abstract class ChooseAndOrderComposite<EntityType> extends Composite {


	// the entity
	private List<EntityType> availableEntities;
	/* Avoid the term select because it is confusing.
	 * Selected means that the user has clicked on an entity to select it.
	 * Chosen means that the user moved an entity to the right side.
	 */
	private List<EntityType> chosenEntities;

	protected ModifySupport modifySupport = new ModifySupport(this);

	/**
	 * Source entities that are generally available (on the left side).
	 * This includes such entities that have been selected (moved to the right side).
	 */
	private EntityProvider<EntityType> entityProvider;


	// **************************************************************************
	// * Widgets
	// *

	private Label availableEntitiesLabel;
	private Label chosenEntitiesLabel;
	private TreeViewer availableTreeViewer;
	private TreeViewer chosenTreeViewer;
	private Button addButton;
	private Button removeButton;

	private Button moveFirstButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button moveLastButton;

	// *
	// * Widgets
	// **************************************************************************


	protected abstract String getAvailableEntitiesLabel();

	protected abstract String getChosenEntitiesLabel();

	protected abstract ILabelProvider getLabelProvider();

	protected abstract String buildCopyInfo(EntityType entity);

	protected abstract Object getId(EntityType entity);



	/**
	 * Create the composite.
	 * @param parent
	 * @param entityProvider
	 *  Source entities that are generally available (on the left side).
	 *  This includes such entities that have been chosen (moved to the right side).
	 * @param style
	 * @throws Exception
	 */
	public ChooseAndOrderComposite(
		Composite parent,
		EntityProvider<EntityType> entityProvider,
		int style
	)
	throws Exception {
		super(parent, style);

		Objects.requireNonNull(entityProvider);
		this.entityProvider = entityProvider;

		createWidgets();
	}


	private void createWidgets() throws Exception {
		/* The proportions of the two Tree widgets shall be constant and independent from their content.
		 * Otherwise the layout looks weird if two ChooseAndOrderComposite appear one below the other.
		 */

		setLayout( new GridLayout(2, true) );


		Composite leftComposite = new Composite(this, SWT.NONE);
		Composite rightComposite = new Composite(this, SWT.NONE);

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, true);
		gridDataFactory.applyTo(leftComposite);
		gridDataFactory.applyTo(rightComposite);

		createWidgetsOnTheLeft(leftComposite);
		createWidgetsOnTheRight(rightComposite);

		initAvailableEntities();

		initDrag(availableTreeViewer);
		initDrag(chosenTreeViewer);

		initChosenDrop();
		initAvailableDrop();
	}


	/**
	 * Build the tree and the Buttons in the middle
	 */
	private void createWidgetsOnTheLeft(Composite parent) {
		final int COL_COUNT = 2;
		parent.setLayout( new GridLayout(COL_COUNT, false) );


		/*
		 * Row 1
		 */
		availableEntitiesLabel = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.span(COL_COUNT, 1)
			.applyTo(availableEntitiesLabel);
		availableEntitiesLabel.setText( getAvailableEntitiesLabel() );


		/*
		 * Row 2
		 */
		{
    		Composite treeComposite = new Composite(parent, SWT.NONE);
    		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    		// using a TreeColumnLayout and a TreeViewerColumn is the only way to set the width to 100%
    		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
    		treeComposite.setLayout(treeColumnLayout);

    		Tree tree = new Tree(treeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    		tree.setLinesVisible(false);
    		tree.setHeaderVisible(false);

    		availableTreeViewer = new TreeViewer(tree);
    		availableTreeViewer.setContentProvider( new ArrayTreeContentProvider() );

    		availableTreeViewer.setLabelProvider( getLabelProvider() ); // necessary for the Comparator!
    		availableTreeViewer.setComparator( new ViewerComparator() );

    		availableTreeViewer.getTree().addKeyListener(treeKeyListener);

    		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(availableTreeViewer, SWT.NONE);
    		TreeColumn treeColumn = treeViewerColumn.getColumn();
    		treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(100, 340, true));

    		// set ColumnLabelProvider that delegated to the ILabelProvider returned by getLabelProvider()
    		treeViewerColumn.setLabelProvider( ColumnLabelProvider.createTextProvider(e -> getLabelProvider().getText(e)) );
		}

		{
    		Composite buttonComposite = new Composite(parent, SWT.NONE);
    		buttonComposite.setLayout( new GridLayout(1, false) );
    		{
    			addButton = new Button(buttonComposite, SWT.NONE);
    			addButton.setBounds(0, 0, 75, 25);
    			addButton.setText(">>");
    			addButton.setToolTipText(UtilI18N.Add + UtilI18N.Ellipsis);
    			addButton.addListener(SWT.Selection, e -> add());
    		}
    		{
    			removeButton = new Button(buttonComposite, SWT.NONE);
    			removeButton.setBounds(0, 0, 75, 25);
    			removeButton.setText("<<");
    			removeButton.setToolTipText(UtilI18N.Remove);
    			removeButton.addListener(SWT.Selection, e -> remove());
    		}
		}
	}


	private void createWidgetsOnTheRight(Composite parent) {
		final int COL_COUNT = 2;
		parent.setLayout(new GridLayout(COL_COUNT, false));


		/*
		 * Row 1
		 */
		chosenEntitiesLabel = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.span(COL_COUNT, 1)
			.applyTo(chosenEntitiesLabel);
		chosenEntitiesLabel.setText( getChosenEntitiesLabel() );


		/*
		 * Row 2
		 */
		{
    		Composite treeComposite = new Composite(parent, SWT.NONE);
    		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    		// using a TreeColumnLayout and a TreeViewerColumn is the only way to set the width to 100%
    		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
    		treeComposite.setLayout(treeColumnLayout);

    		Tree tree = new Tree(treeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    		tree.setLinesVisible(false);
    		tree.setHeaderVisible(false);

    		chosenTreeViewer = new TreeViewer(tree);
    		chosenTreeViewer.setContentProvider( new ArrayTreeContentProvider() );

    		// Don't set a ViewerSorter or ViewerComparator! The user may sort the entities as he want!
//    		chosenTreeViewer.setLabelProvider( getLabelProvider() ); // necessary for the Comparator!
//    		chosenTreeViewer.setComparator( new ViewerComparator() );

    		chosenTreeViewer.getTree().addKeyListener(treeKeyListener);

    		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(chosenTreeViewer, SWT.NONE);
    		TreeColumn treeColumn = treeViewerColumn.getColumn();
    		treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(100, 340, true));

    		// set ColumnLabelProvider that delegated to the ILabelProvider returned by getLabelProvider()
    		treeViewerColumn.setLabelProvider( ColumnLabelProvider.createTextProvider(e -> getLabelProvider().getText(e)) );

		}


		{
    		Composite buttonComposite = new Composite(parent, SWT.NONE);
    		buttonComposite.setLayout(new GridLayout(1, false));
    		{
    			moveFirstButton = new Button(buttonComposite, SWT.NONE);
    			moveFirstButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    			moveFirstButton.setBounds(0, 0, 75, 25);
    			moveFirstButton.setText(UtilI18N.MoveFirst);
    			moveFirstButton.addListener(SWT.Selection, e -> moveFirst());
    		}
    		{
    			moveUpButton = new Button(buttonComposite, SWT.NONE);
    			moveUpButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    			moveUpButton.setBounds(0, 0, 75, 25);
    			moveUpButton.setText(UtilI18N.MoveUp);
    			moveUpButton.addListener(SWT.Selection, e -> moveUp());
    		}
    		{
    			moveDownButton = new Button(buttonComposite, SWT.NONE);
    			moveDownButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    			moveDownButton.setBounds(0, 0, 75, 25);
    			moveDownButton.setText(UtilI18N.MoveDown);
    			moveDownButton.addListener(SWT.Selection, e -> moveDown());
    		}
    		{
    			moveLastButton = new Button(buttonComposite, SWT.NONE);
    			moveLastButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    			moveLastButton.setBounds(0, 0, 75, 25);
    			moveLastButton.setText(UtilI18N.MoveLast);
    			moveLastButton.addListener(SWT.Selection, e -> moveLast());
    		}


    		// initial enable-state
    		moveFirstButton.setEnabled(false);
    		moveUpButton.setEnabled(false);
    		moveDownButton.setEnabled(false);
    		moveLastButton.setEnabled(false);

    		// dynamical enable-state
    		chosenTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
    			@Override
    			public void selectionChanged(SelectionChangedEvent event) {
    				setEnableButtons();
    			}
    		});

    		setEnableButtons();
		}
	}


	private KeyListener treeKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
			// run CopyAction when user presses ctrl+c or ⌘+c

			if (   e.keyCode == 'c'
				&& ( e.stateMask == SWT.MOD1 || e.stateMask == (SWT.MOD1 | SWT.SHIFT) )
			) {
				// determine current selection
				List<EntityType> entityList = Collections.emptyList();
				if (e.getSource() == availableTreeViewer.getTree()) {
					entityList = getAvailableSelection();
				}
				else if (e.getSource() == chosenTreeViewer.getTree()) {
					entityList = getChosenSelection();
				}

				// build copy text
				String copyText = null;
				if (e.stateMask == SWT.MOD1) {
					copyText = buildCopyInfo(entityList);
				}
				else if (e.stateMask == (SWT.MOD1 | SWT.SHIFT)) {
					copyText = buildCopyIds(entityList);
				}

				ClipboardHelper.copyToClipboard(copyText);
			}
		};
	};


	private String buildCopyInfo(List<EntityType> entityList) {
		StringBuilder text = new StringBuilder();
		for (EntityType entity : entityList) {
			if (text.length() > 0) {
				text.append("\n");
			}
			text.append( buildCopyInfo(entity) );
		}

		return text.toString();
	}


	private String buildCopyIds(List<EntityType> entityList) {
		StringBuilder text = new StringBuilder();
		for (EntityType entity : entityList) {
			if (text.length() > 0) {
				text.append(", ");
			}
			text.append( getId(entity) );
		}

		return text.toString();
	}


	public void initAvailableEntities() throws Exception {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					Collection<EntityType> entityList = entityProvider.getEntityList();

					availableEntities = new ArrayList<>(entityList);

					if (chosenEntities != null) {
						availableEntities.removeAll(chosenEntities);
					}

					refreshAvailableListViewer();
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	private void refreshAvailableListViewer() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					availableTreeViewer.setInput(availableEntities);
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void refreshChosenListViewer() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					chosenTreeViewer.setInput(chosenEntities);
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@SuppressWarnings("unchecked")
	private void add() {
		IStructuredSelection selection = (IStructuredSelection) availableTreeViewer.getSelection();
		if (selection != null) {
			List<EntityType> entityList = selection.toList();

			availableEntities.removeAll(entityList);
			chosenEntities.addAll(entityList);

			refreshAvailableListViewer();
			refreshChosenListViewer();

			// inform registered ModifyListeners
			fireModify();
		}
	}


	@SuppressWarnings("unchecked")
	private void remove() {
		IStructuredSelection selection = (IStructuredSelection) chosenTreeViewer.getSelection();
		if (selection != null) {
			List<EntityType> entities = selection.toList();

			chosenEntities.removeAll(entities);
			availableEntities.addAll(entities);

			refreshAvailableListViewer();
			refreshChosenListViewer();

			// inform registered ModifyListeners
			fireModify();
		}
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


	private void fireModify() {
		modifySupport.fire();
	}

	// *
	// * Modifying
	// **************************************************************************

	private void syncWidgetsToEntity() {
		if (availableTreeViewer == null || availableTreeViewer.getTree().isDisposed()) {
			return;
		}

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					modifySupport.setEnabled(false);
					refreshChosenListViewer();

					// remove chosen from available entities
					if (availableEntities != null && chosenEntities != null) {
						boolean hasChanged = availableEntities.removeAll(chosenEntities);
						if (hasChanged) {
							refreshAvailableListViewer();
						}
					}
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
				finally {
					modifySupport.setEnabled(true);
				}
			}
		});
	}


	public void syncEntityToWidgets() {
		// Do nothing, because changes are done in chosenEntities directly.
	}


	public List<EntityType> getChosenEntities() {
		return chosenEntities;
	}


	public List<Object> getChosenIds() {
		List<Object> chosenIds = new ArrayList<>( chosenEntities.size() );
		for (EntityType entity : chosenEntities) {
			Object pk = getPrimaryKey(entity);
			chosenIds.add(pk);
		}
		return chosenIds;
	}


	public void setChosenEntities(List<EntityType> entities) {
		this.chosenEntities = entities;
		syncWidgetsToEntity();
	}


	public void setChosenIds(List<Long> chosenIds) {
		try {
			if (chosenEntities == null) {
				chosenEntities = new ArrayList<>();
			}
			else {
				chosenEntities.clear();
			}

			if (chosenIds != null) {
    			for (Long id : chosenIds) {
    				EntityType entity = entityProvider.findEntity(id);
    				chosenEntities.add(entity);
    			}
			}

			syncWidgetsToEntity();
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@SuppressWarnings("unchecked")
	protected List<EntityType> getAvailableSelection() {
		List<EntityType> selectedEntities = null;
		IStructuredSelection selection = (IStructuredSelection) availableTreeViewer.getSelection();
		if (selection != null) {
			selectedEntities = selection.toList();
		}
		return selectedEntities;
	}


	@SuppressWarnings("unchecked")
	protected List<EntityType> getChosenSelection() {
		List<EntityType> selectedEntities = null;
		IStructuredSelection selection = (IStructuredSelection) chosenTreeViewer.getSelection();
		if (selection != null) {
			selectedEntities = selection.toList();
		}
		return selectedEntities;
	}


	protected void moveFirst() {
		List<EntityType> selectedEntities = getChosenSelection();
		if (!selectedEntities.isEmpty()) {
			CollectionsHelper.moveFirst(chosenEntities, selectedEntities);

			chosenTreeViewer.refresh(true);

			// signal that the Editor contains unsaved data
			fireModify();
			setEnableButtons();
		}
	}


	protected void moveUp() {
		List<EntityType> selectedEntities = getChosenSelection();
		if (!selectedEntities.isEmpty()) {
			CollectionsHelper.moveUp(chosenEntities, selectedEntities);

			chosenTreeViewer.refresh(true);

			// signal that the Editor contains unsaved data
			fireModify();
			setEnableButtons();
		}
	}


	protected void moveDown() {
		List<EntityType> selectedEntities = getChosenSelection();
		if (!selectedEntities.isEmpty()) {
			CollectionsHelper.moveDown(chosenEntities, selectedEntities);

			chosenTreeViewer.refresh(true);

			// signal that the Editor contains unsaved data
			fireModify();
			setEnableButtons();
		}
	}


	/**
	 * All selected bookings are taken out and added again at the end
	 */
	protected void moveLast() {
		List<EntityType> selectedEntities = getChosenSelection();

		if (!selectedEntities.isEmpty()) {
			CollectionsHelper.moveLast(chosenEntities, selectedEntities);

			chosenTreeViewer.refresh(true);

			// signal that the Editor contains unsaved data
			fireModify();
			setEnableButtons();
		}
	}


	private void setEnableButtons() {
		boolean moveFirstEnabled = false;
		boolean moveUpEnabled = false;
		boolean moveDownEnabled = false;
		boolean moveLastEnabled = false;

		List<EntityType> selectedEntities = getChosenSelection();
		boolean selectionEmpty = selectedEntities.isEmpty();

		if (!selectionEmpty) {
			EntityType firstSelectedEntity = selectedEntities.get(0);
			EntityType lastSelectedEntity = selectedEntities.get(selectedEntities.size() - 1);
			EntityType lastChosenEntity = chosenEntities.get(chosenEntities.size() - 1);

			// When the last of the n selected is after the nth position of the chosen,
			// there must be a gap above, and we can move first
			if (chosenEntities.indexOf(lastSelectedEntity) >= selectedEntities.size()) {
				moveFirstEnabled = true;
			}

			// When first selected is not the one in the first row we can move up
			moveUpEnabled = !chosenEntities.get(0).equals(firstSelectedEntity);

			// When last selected is not the one in last row we can move down
			moveDownEnabled = ! lastChosenEntity.equals(lastSelectedEntity);

			// When the first of the n selected is not at the size-nth position,
			// there must be a gap below, and we can move down
			int indexOfFirstSelectedInChosen = chosenEntities.indexOf(firstSelectedEntity);
			if (indexOfFirstSelectedInChosen < chosenEntities.size() - selectedEntities.size()) {
				moveLastEnabled = true;
			}
		}

		moveFirstButton.setEnabled(moveFirstEnabled);
		moveLastButton.setEnabled(moveLastEnabled);
		moveUpButton.setEnabled(moveUpEnabled);
		moveDownButton.setEnabled(moveDownEnabled);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Drag and drop
	// *

	private static final Transfer[] DND_TRANSFERS = new Transfer[] { TextTransfer.getInstance() };
	private final static int DND_OPERATIONS = DND.DROP_MOVE;

	/**
	 * Dragging from the tree transports the entity's PK as String
	 */
	private void initDrag(final TreeViewer treeViewer) {

		final DragSource source = new DragSource(treeViewer.getTree(), DND_OPERATIONS);
		source.setTransfer(DND_TRANSFERS);

		source.addDragListener(new DragSourceListener() {
			/* variable to store data temporarily
			 * event.data must not be set in dragStart() but in dragSetData().
			 * Otherwise a very internal error occurs that is hard to handle.
			 */
			private String data;

			@Override
			public void dragStart(DragSourceEvent event) {
				try {
					ISelection selection = treeViewer.getSelection();
					if (!selection.isEmpty()) {
						List<EntityType> selectedEntities = SelectionHelper.toList(selection);
						List<Object> selectedPKs = getPrimaryKeyList(selectedEntities);

						String separatedString = TypeHelper.getSeparatedString(selectedPKs, " ");
						System.out.println("dragStart: " + separatedString);
						data = separatedString;

						event.doit = true;
					}
					else {
						event.doit = false;
					}
				}
				catch (Throwable e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = data;
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
			}
		});
	}


	/**
	 * When dragging and dropping on the chosen tree, make sure there is visual feedback,
	 * drop it at the right position and remove from the other tree.
	 */
	private void initChosenDrop() {
		DropTarget target = new DropTarget(chosenTreeViewer.getTree(), DND_OPERATIONS);
		target.setTransfer(DND_TRANSFERS);

		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragOver(DropTargetEvent event) {
				try {
					event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
					if (event.item != null) {
						TreeItem treeItem = (TreeItem) event.item;

						// get the current mouse position related to the Display
						Point pt = Display.getCurrent().map(null, chosenTreeViewer.getTree(), event.x, event.y);

						// get the Rectangle of the TreeItem under the current mouse position
						Rectangle bounds = treeItem.getBounds();

						if (pt.y <= bounds.y + bounds.height/2) {
							// mouse is in the upper half of the TreeItem: insert before
							event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
						}
						else  {
							// mouse is in the lower half of the TreeItem: insert after
							event.feedback |= DND.FEEDBACK_INSERT_AFTER;
						}
					}
				}
				catch (Throwable e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}


			@Override
			public void drop(DropTargetEvent event) {
				try {
					if (event.data == null) {
						event.detail = DND.DROP_NONE;
						return;
					}

					// one or more entity PKs separated with space characters
					String separatedPKs = ((String) event.data);

					List<String> pkList = TypeHelper.toStringList(separatedPKs, " ");

					Collection<EntityType> entityList = entityProvider.findEntities(pkList);

					if ( empty(entityList) ) {
						event.detail = DND.DROP_NONE;
						return;
					}

					if (event.item != null) {
						EntityType targetEntity = (EntityType) event.item.getData();

						if (pkList.contains( getPrimaryKey(targetEntity) )) {
							event.detail = DND.DROP_NONE;
							return;
						}

						int targetIndex = chosenEntities.indexOf(targetEntity);
						TreeItem item = (TreeItem) event.item;
						Point pt = Display.getCurrent().map(null, chosenTreeViewer.getTree(), event.x, event.y);
						Rectangle bounds = item.getBounds();
						if (pt.y <= bounds.y + bounds.height/2) {
							insert(entityList, targetIndex);
						}
						else  {
							insert(entityList, targetIndex + 1);
						}
					}
					else {
						insert(entityList, chosenEntities.size());
					}

				}
				catch (Throwable e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					event.detail = DND.DROP_NONE;
				}
			}


			/**
			 * Insert the given entities at the given index, removing them first from the previous position.
			 *
			 * @param entities
			 * @param index
			 */
			private void insert(Collection<EntityType> entities, int index) {
				for (EntityType entity : entities) {
					if (availableEntities.contains(entity)) {
						availableEntities.remove(entity);
						chosenEntities.add(index, entity);
					}
					// If in this tree, remove, but take care that the index is shifted if needed
					else if (chosenEntities.contains(entity)) {
						int previousIndex = chosenEntities.indexOf(entity);
						chosenEntities.remove(entity);
						if (index > previousIndex) {
							index--;
						}
						chosenEntities.add(index, entity);
					}
					index++;
				}


				availableTreeViewer.refresh();
				chosenTreeViewer.refresh();
				fireModify();
				setEnableButtons();
			}
		});
	}


	/**
	 * Simpler then initChosenDrop: When dragging and dropping on the available
	 * drop anywhere and remove from the other tree
	 */
	private void initAvailableDrop() {
		DropTarget target = new DropTarget(availableTreeViewer.getTree(), DND_OPERATIONS);
		target.setTransfer(DND_TRANSFERS);

		target.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				try {
					if (event.data == null) {
						event.detail = DND.DROP_NONE;
						return;
					}

					// one or more entity PKs separated with space characters
					String separatedPKs = ((String) event.data);

					List<String> pkList = TypeHelper.toStringList(separatedPKs, " ");

					Collection<EntityType> entities = entityProvider.findEntities(pkList);

					if ( empty(entities) ) {
						event.detail = DND.DROP_NONE;
						return;
					}

					for (EntityType entity : entities) {
    					if (chosenEntities.contains(entity)) {
    						chosenEntities.remove(entity);
    						availableEntities.add(entity);
    					}
					}

					availableTreeViewer.refresh();
					chosenTreeViewer.refresh();
					fireModify();
				}
				catch (Throwable e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					event.detail = DND.DROP_NONE;
				}
			}

		});
	}

	// *
	// * Drag and drop
	// **************************************************************************


	@SuppressWarnings("rawtypes")
	protected Object getPrimaryKey(EntityType entity) {
		Objects.requireNonNull(entity);

		Object pk = null;

		if (entity instanceof AbstractCVO) {
			pk = ((AbstractCVO) entity).getPK();
		}
		else if (entity instanceof AbstractVO) {
			pk = ((AbstractVO) entity).getPK();
		}
		else if (entity instanceof com.lambdalogic.messeinfo.kernel.AbstractEntity) {
			pk = ((com.lambdalogic.messeinfo.kernel.AbstractEntity) entity).getPrimaryKey();
		}
		else if (entity instanceof com.lambdalogic.messeinfo.kernel.AbstractEntity2) {
			pk = ((com.lambdalogic.messeinfo.kernel.AbstractEntity2) entity).getPrimaryKey();
		}
		else if (entity instanceof de.regasus.core.AbstractEntity) {
			pk = ((de.regasus.core.AbstractEntity) entity).getId();
		}
		else {
			throw new RuntimeException("Unsupported EntityType " + entity.getClass().getName());
		}

		return pk;
	}


	protected List<Object> getPrimaryKeyList(Collection<EntityType> entities) {
		List<Object> pkList = new ArrayList<>();

		for (EntityType entity : entities) {
			Object pk = getPrimaryKey(entity);
			pkList.add(pk);
		}

		return pkList;
	}


	public void setAvailableEntitiesLabel(String text) {
		availableEntitiesLabel.setText(text);
	}


	public void setChosenEntitiesLabel(String text) {
		chosenEntitiesLabel.setText(text);
	}

}
