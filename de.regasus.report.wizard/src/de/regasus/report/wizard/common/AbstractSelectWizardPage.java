package de.regasus.report.wizard.common;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLFieldPath;
import com.lambdalogic.messeinfo.kernel.sql.AbstractSearch;
import com.lambdalogic.messeinfo.participant.report.parameter.GroupField;
import com.lambdalogic.messeinfo.participant.report.parameter.IGroupFieldReportParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.IOrderFieldsReportParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.ISelectFieldsReportParameter;
import com.lambdalogic.messeinfo.participant.report.participantList.OrderField;
import com.lambdalogic.messeinfo.participant.report.participantList.SelectField;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.tree.DefaultTreeNode;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.tree.TreeNodeContentProvider;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.participant.list.OrderColumnsTable;
import de.regasus.report.wizard.participant.list.SQLDirectory;
import de.regasus.report.wizard.participant.list.SQLFieldComparator;
import de.regasus.report.wizard.participant.list.SQLFieldContainer;
import de.regasus.report.wizard.participant.list.SelectColumnsTable;
import de.regasus.report.wizard.participant.list.SelectFieldTreeLabelProvider;
import de.regasus.report.wizard.participant.list.dnd.SQLFieldKeysTransfer;
import de.regasus.report.wizard.ui.Activator;

public abstract class AbstractSelectWizardPage extends WizardPage implements IReportWizardPage {
	private static Logger log = Logger.getLogger("ui.AbstractSelectWizardPage");

	public static final String DESCRIPTION_SELECT_ID = "selectFields";
	public static final String DESCRIPTION_ORDER_ID = "orderFields";
	public static final String DESCRIPTION_GROUP_ID = "groupField";


	/**
	 * Indicates if the Grouping-Area is present.
	 */
	private boolean withGroups;

	/**
	 * Indicates whether the composite for selection of columns is used
	 */
	private boolean withSelection = true;

	private Label groupColumnLabel;

	private SQLField groupSQLField;

	private Button removeGroupColumnButton;

	// Widgets for the Tree with available solumns
	private Tree availableColumnsTree;

	private TreeViewer availableColumnsViewer;

	private TreeNode<SQLDirectory> root = new DefaultTreeNode<>();

	private Map<SQLFieldPath, TreeNode<SQLDirectory>> pathMap = MapHelper.createHashMap(100);

	// Widgets for the Table with the Select-Columns
	private TableViewer selectColumnsViewer;

	private ArrayList<SQLFieldContainer> selectColumnsList = new ArrayList<>();

	// Widgets for the Table with the Order-Columns
	private TableViewer orderColumnsViewer;

	private ArrayList<SQLFieldContainer> orderColumnsList = new ArrayList<>();

	/**
	 * ReportParameter is expected to implement additionally the interfaces {@link IGroupFieldReportParameter} if
	 * withGroup is set to true, and {@link ISelectFieldsReportParameter} if withSelection is set to true.
	 */
	private IOrderFieldsReportParameter reportParameter;

	private AbstractSearch abstractSearch;

	// Variables for Drag and Drop
	private List<SQLFieldContainer> draggedSQLFieldContainers = new ArrayList<>();

	private boolean selectViewerIsDragSource = false;

	private boolean selectViewerIsDropTarget = false;

	private boolean orderViewerIsDragSource = false;

	private boolean orderViewerIsDropTarget = false;

	private Clipboard clipboard;

	private Button addGroupColumnButton;

	private Button removeSelectColumnButton;

	private Button addSelectColumnButton;


	/**
	 * Create the wizard
	 */
	public AbstractSelectWizardPage(
		String pageName,
		boolean withGroups,
		boolean withSelection
	) {
		super(pageName);

		this.withGroups = withGroups;
		this.withSelection = withSelection;

		setTitle(ReportWizardI18N.AbstractSelectWizardPage_Title);
		setDescription(ReportWizardI18N.AbstractSelectWizardPage_Description);
	}


	/**
	 * Create the wizard
	 */
	public AbstractSelectWizardPage(
		String pageName,
		boolean withGroups
	) {
		this(pageName, withGroups, true);
	}


	/**
	 * Create contents of the wizard
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		// container-Composite for this WizardPage
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		setControl(container);

		// Sash to separate the Tree on the left side from the Buttons and the List on the right side.
		final SashForm mainSashForm = new SashForm(container, SWT.BORDER);

		// Composite for the Tree on the left side.
		final Composite leftComposite = new Composite(mainSashForm, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		leftComposite.setLayout(gridLayout_1);

		final Label availableColumnsLabel = new Label(leftComposite, SWT.NONE);
		availableColumnsLabel.setText(ReportWizardI18N.AbstractSelectWizardPage_AvailableColumnsLabel);

		// **************************************************************************
		// * AvailableColumns
		// *

		availableColumnsViewer = new TreeViewer(leftComposite, SWT.MULTI | SWT.BORDER);
		availableColumnsTree = availableColumnsViewer.getTree();
		availableColumnsTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		availableColumnsViewer.setContentProvider(new TreeNodeContentProvider());
		availableColumnsViewer.setLabelProvider(new SelectFieldTreeLabelProvider());
		// sorting
		availableColumnsViewer.setComparator(new SQLFieldComparator());

		// *
		// * AvailableColumns
		// **************************************************************************

		// Composite for the Buttons and the List on the right side.
		final Composite rightComposite = new Composite(mainSashForm, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		rightComposite.setLayout(gridLayout);

		final SashForm rightSashForm = new SashForm(rightComposite, SWT.BORDER | SWT.VERTICAL);
		final GridData gd_rightSashForm = new GridData(SWT.FILL, SWT.FILL, true, true);
		rightSashForm.setLayoutData(gd_rightSashForm);

		if (withSelection) {

			final Composite selectComposite = new Composite(rightSashForm, SWT.NONE);
			final GridLayout selectGridLayout = new GridLayout();
			selectGridLayout.numColumns = 2;
			selectComposite.setLayout(selectGridLayout);

			// **************************************************************************
			// * Group-Widgets
			// *

			if (withGroups) {
				new Label(selectComposite, SWT.NONE);

				final Label groupColumnLabelLabel = new Label(selectComposite, SWT.NONE);
				groupColumnLabelLabel.setText(ReportWizardI18N.AbstractSelectWizardPage_GroupColumnLabel);

				final Composite groupBtnComposite = new Composite(selectComposite, SWT.NONE);
				final GridData gd_groupBtnComposite = new GridData();
				groupBtnComposite.setLayoutData(gd_groupBtnComposite);
				final GridLayout gridLayout_4 = new GridLayout();
				gridLayout_4.marginHeight = 0;
				gridLayout_4.numColumns = 2;
				groupBtnComposite.setLayout(gridLayout_4);

				removeGroupColumnButton = new Button(groupBtnComposite, SWT.NONE);
				removeGroupColumnButton.setLayoutData(new GridData());
				removeGroupColumnButton.setText("<<");

				removeGroupColumnButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						moveGroupFieldToAvailableColumn();
					}
				});
				removeGroupColumnButton.setEnabled(false);

				addGroupColumnButton = new Button(groupBtnComposite, SWT.NONE);
				addGroupColumnButton.setText(">>");
				addGroupColumnButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						moveAvailableColumnToGroupField();
					}
				});
				addGroupColumnButton.setEnabled(false);
				availableColumnsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						addGroupColumnButton.setEnabled(selection.size() == 1);
					}
				});

				groupColumnLabel = new Label(selectComposite, SWT.BORDER);
				groupColumnLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				final GridData gd_groupColumnText = new GridData(SWT.FILL, SWT.TOP, true, false);
				groupColumnLabel.setLayoutData(gd_groupColumnText);
			}

			// *
			// * Group-Widgets
			// **************************************************************************

			new Label(selectComposite, SWT.NONE);

			final Label reportColumnsLabel = new Label(selectComposite, SWT.NONE);
			reportColumnsLabel.setText(ReportWizardI18N.AbstractSelectWizardPage_ReportColumnsLabel);

			final Composite reportBtnComposite = new Composite(selectComposite, SWT.NONE);
			final GridData gd_reportBtnComposite = new GridData(SWT.LEFT, SWT.TOP, false, false);
			reportBtnComposite.setLayoutData(gd_reportBtnComposite);
			final GridLayout gridLayout_5 = new GridLayout();
			gridLayout_5.numColumns = 2;
			reportBtnComposite.setLayout(gridLayout_5);

			removeSelectColumnButton = new Button(reportBtnComposite, SWT.NONE);
			removeSelectColumnButton.setLayoutData(new GridData());
			removeSelectColumnButton.setText("<<");
			removeSelectColumnButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					moveSelectColumnToAvailableColumn();
				}
			});

			addSelectColumnButton = new Button(reportBtnComposite, SWT.NONE);
			addSelectColumnButton.setText(">>");
			addSelectColumnButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					copyAvailableColumnsToSelectColumns();
				}
			});

			// **************************************************************************
			// * Select Columns Table
			// *

			{
				final Table table = new Table(selectComposite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
				table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				table.setHeaderVisible(true);
				table.setLinesVisible(true);

				final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
				nameTableColumn.setWidth(200);
				nameTableColumn.setText(UtilI18N.Name);
				TableLayout tableLayout = new TableLayout();
				tableLayout.addColumnData(new ColumnWeightData(100, true));
				table.setLayout(tableLayout);

				final SelectColumnsTable selectColumnsTable = new SelectColumnsTable(table);
				selectColumnsViewer = selectColumnsTable.getViewer();
				selectColumnsTable.setInput(selectColumnsList);
			}

			// *
			// * Select Columns Table
			// **************************************************************************
		}

		final Composite orderComposite = new Composite(rightSashForm, SWT.NONE);
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		orderComposite.setLayout(gridLayout_3);
		new Label(orderComposite, SWT.NONE);

		final Label orderColumnsLabel = new Label(orderComposite, SWT.NONE);
		orderColumnsLabel.setText(ReportWizardI18N.AbstractSelectWizardPage_OrderColumnsLabel);

		final Composite orderBtnComposite = new Composite(orderComposite, SWT.NONE);
		orderBtnComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		final GridLayout gridLayout_6 = new GridLayout();
		gridLayout_6.numColumns = 2;
		orderBtnComposite.setLayout(gridLayout_6);

		final Button removeOrderColumnButton = new Button(orderBtnComposite, SWT.NONE);
		removeOrderColumnButton.setLayoutData(new GridData());
		removeOrderColumnButton.setText("<<");
		removeOrderColumnButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveOrderColumnToAvailableColumn();
			}
		});

		final Button addOrderColumnButton = new Button(orderBtnComposite, SWT.NONE);
		addOrderColumnButton.setText(">>");
		addOrderColumnButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyAvailableColumnsToOrderColumns();
			}
		});

		// **************************************************************************
		// * Order Columns Table
		// *

		{
			final Table table = new Table(orderComposite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			nameTableColumn.setWidth(200);
			nameTableColumn.setText(UtilI18N.Name);
			TableLayout tableLayout = new TableLayout();
			tableLayout.addColumnData(new ColumnWeightData(100, true));
			table.setLayout(tableLayout);

			final OrderColumnsTable orderColumnsTable = new OrderColumnsTable(table);
			orderColumnsViewer = orderColumnsTable.getViewer();
			orderColumnsTable.setInput(orderColumnsList);
		}

		// *
		// * * Order Columns Table
		// **************************************************************************

		mainSashForm.setWeights(new int[] { 1, 1 });

		if (withSelection) {
			rightSashForm.setWeights(new int[] { 2, 1 });
		}

		// **************************************************************************
		// * Drag and Drop
		// *

		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		Transfer[] transfers = new Transfer[] { SQLFieldKeysTransfer.getInstance() };

		if (withGroups) {
			DragSource groupColumnDragSource = new DragSource(groupColumnLabel, operations);
			groupColumnDragSource.setTransfer(transfers);
			groupColumnDragSource.addDragListener(new DragSourceListener() {
				@Override
				public void dragStart(DragSourceEvent event) {
					// Only start the drag if there is actually a GroupColumn.
					if (groupSQLField == null) {
						event.doit = false;
					}
				}


				@Override
				public void dragSetData(DragSourceEvent event) {
					if (groupSQLField != null && SQLFieldKeysTransfer.getInstance().isSupportedType(event.dataType)) {
						String[] keys = new String[1];
						keys[0] = groupSQLField.getKey();
						event.data = keys;
					}
				}


				@Override
				public void dragFinished(DragSourceEvent event) {
					log.info("groupColumnDragSource.dragFinished");
					if (!event.doit) {
						return;
					}

					// if the SQLField was moved, remove it from the source viewer
					if (event.detail == DND.DROP_MOVE) {
						setGroupColumn(null);
					}
				}
			}); // DragSourceListener

			DropTarget groupColumnDropTarget = new DropTarget(groupColumnLabel, operations);
			groupColumnDropTarget.setTransfer(transfers);
			groupColumnDropTarget.addDropListener(new DropTargetListener() {
				@Override
				public void dragEnter(DropTargetEvent event) {
					if (SQLFieldKeysTransfer.getInstance().isSupportedType(event.currentDataType)) {
						if (event.data instanceof String[]) {
							String[] keys = (String[]) event.data;
							if (keys == null || keys.length != 1) {
								event.detail = DND.DROP_NONE;
							}
						}
					}
				}


				@Override
				public void dragOver(DropTargetEvent event) {
				}


				@Override
				public void dragOperationChanged(DropTargetEvent event) {
				}


				@Override
				public void dragLeave(DropTargetEvent event) {
				}


				@Override
				public void dropAccept(DropTargetEvent event) {
				}


				@Override
				public void drop(DropTargetEvent event) {
					if (SQLFieldKeysTransfer.getInstance().isSupportedType(event.currentDataType)) {
						log.info("groupColumnDropTarget.drop");
						try {
							if (event.data instanceof String[]) {

								// dropSource bestimmen und Operation durchführen
								String[] keys = (String[]) event.data;
								if (keys != null && keys.length == 1) {
									SQLField sqlField = getSQLField(keys[0]);
									setGroupColumn(sqlField);
								}
							}
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
						}
					}
				}

			}); // DropTargetListener
		} // if (withGroups)

		availableColumnsViewer.addDragSupport(operations, transfers, new DragSourceAdapter() {
			ArrayList<SQLField> draggedSQLFields = new ArrayList<>();

			@Override
			public void dragStart(DragSourceEvent event) {
				selectViewerIsDragSource = false;
				selectViewerIsDropTarget = false;
				orderViewerIsDragSource = false;
				orderViewerIsDropTarget = false;

				event.doit = !availableColumnsViewer.getSelection().isEmpty();
			}


			@Override
			public void dragSetData(DragSourceEvent event) {
				if (SQLFieldKeysTransfer.getInstance().isSupportedType(event.dataType)) {
					draggedSQLFields.clear();

					Collection<SQLField> sqlFields = getAvailableColumnsSelection();

					String[] keys = new String[sqlFields.size()];
					int i = 0;
					for (SQLField sqlField : sqlFields) {
						draggedSQLFields.add(sqlField);
						keys[i++] = sqlField.getKey();
					}

					event.data = keys;
				}
			}


			@Override
			public void dragFinished(DragSourceEvent event) {
				log.info("availableColumnsViewer.dragFinished");

				if (!event.doit) {
					return;
				}

				// if the SQLField was moved, remove it from the source viewer
				if (event.detail == DND.DROP_MOVE) {
					removeAvailableColumns(draggedSQLFields);
					draggedSQLFields.clear();
				}
			}
		}); // DragSourceAdapter


		availableColumnsViewer.addDropSupport(operations, transfers, new ViewerDropAdapter(availableColumnsViewer) {
			// initialize inner anonym class
			{
				setScrollExpandEnabled(true);
			}


			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return
					(selectViewerIsDragSource || orderViewerIsDragSource)
					&&
					SQLFieldKeysTransfer.getInstance().isSupportedType(transferType);
			}


			@Override
			public boolean performDrop(Object data) {
				selectViewerIsDropTarget = false;

				try {
					if (data instanceof String[]) {
						String[] keys = (String[]) data;

						// determine dropTarget: not necessary

						// determine dropSource and execute operation

						for (int i = 0; i < keys.length; i++) {
							SQLField sqlField = getSQLField(keys[i]);
							addAvailableColumns(sqlField, false);
						}
						availableColumnsViewer.refresh();
					}
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
				return true;
			}


			@Override
			public void dragOver(DropTargetEvent event) {
				super.dragOver(event);
				event.feedback = DND.FEEDBACK_NONE;
			}

		} // ViewerDropAdapter
			);

		if (withSelection) {
			selectColumnsViewer.addDragSupport(operations, transfers, new DragSourceAdapter() {

				@Override
				public void dragStart(DragSourceEvent event) {
					selectViewerIsDragSource = true;
					selectViewerIsDropTarget = false;
					orderViewerIsDragSource = false;
					orderViewerIsDropTarget = false;

					event.doit = !selectColumnsViewer.getSelection().isEmpty();
				}


				@Override
				public void dragSetData(DragSourceEvent event) {
					if (SQLFieldKeysTransfer.getInstance().isSupportedType(event.dataType)) {
						draggedSQLFieldContainers = getSelectColumnsSelection();
						event.data = getKeys(draggedSQLFieldContainers);
					}
				}


				@Override
				public void dragFinished(DragSourceEvent event) {
					log.info("selectColumnsViewer.dragFinished");
					if (!event.doit) {
						return;
					}

					// if the SQLField was moved, remove it from the source viewer
					if (event.detail == DND.DROP_MOVE && !selectViewerIsDropTarget) {
						removeSelectSQLFieldContainers(draggedSQLFieldContainers);
					}
					draggedSQLFieldContainers.clear();

					selectViewerIsDragSource = false;
					selectViewerIsDropTarget = false;
					orderViewerIsDragSource = false;
					orderViewerIsDropTarget = false;
				}
			}); // DragSourceAdapter


			selectColumnsViewer.addDropSupport(operations, transfers, new ViewerDropAdapter(selectColumnsViewer) {
				// initialize inner anonym class
				{
					setScrollExpandEnabled(true);
				}


				@Override
				public boolean validateDrop(Object target, int operation, TransferData transferType) {
					return SQLFieldKeysTransfer.getInstance().isSupportedType(transferType);
				}


				@Override
				public boolean performDrop(Object data) {
					selectViewerIsDropTarget = true;

					try {
						if (data instanceof String[]) {

							// determine dropTarget
							Object objTarget = getCurrentTarget();
							SQLFieldContainer neighbourSQLFieldContainer = null;
							if (objTarget != null && objTarget instanceof SQLFieldContainer) {
								neighbourSQLFieldContainer = (SQLFieldContainer) objTarget;
							}

							// determine dropSource and execute operation
							String[] keys = (String[]) data;
							ArrayList<SQLField> transferedSQLFields = new ArrayList<>(keys.length);
							for (int i = 0; i < keys.length; i++) {
								SQLField sqlField = getSQLField(keys[i]);
								transferedSQLFields.add(sqlField);
							}

							if (getCurrentOperation() == DND.DROP_MOVE && selectViewerIsDragSource) {
								removeSelectSQLFieldContainers(draggedSQLFieldContainers);
							}

							int index = selectColumnsList.indexOf(neighbourSQLFieldContainer);
							if (index == -1) {
								index = selectColumnsList.size();
							}
							else if (getCurrentLocation() != LOCATION_BEFORE) {
								index++;
							}
							List<SQLFieldContainer> newSQLFieldContainers =
								addSelectColumns(transferedSQLFields, index);

							// select the pasted elements
							ISelection selection = new StructuredSelection(newSQLFieldContainers);
							selectColumnsViewer.setSelection(selection, true/* reveal */);

							draggedSQLFieldContainers.clear();
						}
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
					return true;
				}


				@Override
				public void dragOver(DropTargetEvent event) {
					super.dragOver(event);
					// event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
				}

			}); // ViewerDropAdapter
		}


		orderColumnsViewer.addDragSupport(operations, transfers, new DragSourceAdapter() {
			@Override
			public void dragStart(DragSourceEvent event) {
				selectViewerIsDragSource = false;
				selectViewerIsDropTarget = false;
				orderViewerIsDragSource = true;
				orderViewerIsDropTarget = false;

				event.doit = !orderColumnsViewer.getSelection().isEmpty();
			}


			@Override
			public void dragSetData(DragSourceEvent event) {
				if (SQLFieldKeysTransfer.getInstance().isSupportedType(event.dataType)) {
					draggedSQLFieldContainers = getOrderColumnsSelection();
					event.data = getKeys(draggedSQLFieldContainers);
				}
			}


			@Override
			public void dragFinished(DragSourceEvent event) {
				log.info("orderColumnsViewer.dragFinished");
				if (!event.doit) {
					return;
				}

				// if the SQLField was moved, remove it from the source viewer
				if (event.detail == DND.DROP_MOVE && !orderViewerIsDropTarget) {
					removeOrderSQLFieldContainers(draggedSQLFieldContainers);
				}
				draggedSQLFieldContainers.clear();

				selectViewerIsDragSource = false;
				selectViewerIsDropTarget = false;
				orderViewerIsDragSource = false;
				orderViewerIsDropTarget = false;
			}
		}); // DragSourceAdapter


		orderColumnsViewer.addDropSupport(operations, transfers, new ViewerDropAdapter(selectColumnsViewer) {
			// initialize inner anonym class
			{
				setScrollExpandEnabled(true);
			}


			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return SQLFieldKeysTransfer.getInstance().isSupportedType(transferType);
			}


			@Override
			public boolean performDrop(Object data) {
				orderViewerIsDropTarget = true;

				log.info("orderColumnsViewer.performDrop");
				try {
					if (data instanceof String[]) {

						// determine dropTarget
						Object objTarget = getCurrentTarget();
						SQLFieldContainer neighbourSQLFieldContainer = null;
						if (objTarget != null && objTarget instanceof SQLFieldContainer) {
							neighbourSQLFieldContainer = (SQLFieldContainer) objTarget;
						}

						// determine dropSource and execute operation
						String[] keys = (String[]) data;
						ArrayList<SQLField> transferedSQLFields = new ArrayList<>(keys.length);
						for (int i = 0; i < keys.length; i++) {
							SQLField sqlField = getSQLField(keys[i]);
							transferedSQLFields.add(sqlField);
						}

						if (getCurrentOperation() == DND.DROP_MOVE && orderViewerIsDragSource) {
							removeOrderSQLFieldContainers(draggedSQLFieldContainers);
						}

						int index = orderColumnsList.indexOf(neighbourSQLFieldContainer);
						if (index == -1) {
							index = orderColumnsList.size();
						}
						else if (getCurrentLocation() != LOCATION_BEFORE) {
							index++;
						}
						List<SQLFieldContainer> newSQLFieldContainers = addOrderColumns(transferedSQLFields, index);

						// select the pasted elements
						ISelection selection = new StructuredSelection(newSQLFieldContainers);
						orderColumnsViewer.setSelection(selection, true/* reveal */);

						draggedSQLFieldContainers.clear();
					}
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
				return true;
			}


			@Override
			public void dragOver(DropTargetEvent event) {
				super.dragOver(event);
				// event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

		}); // ViewerDropAdapter


		// *
		// * Drag and Drop
		// **************************************************************************

		// **************************************************************************
		// * Cut, Copy and Paste
		// *

		clipboard = new Clipboard(getShell().getDisplay());

		availableColumnsTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && (e.keyCode == 'x' || e.keyCode == 'c')) {
					Collection<SQLField> sqlFields = getAvailableColumnsSelection();

					String[] keys = new String[sqlFields.size()];
					int i = 0;
					for (SQLField sqlField : sqlFields) {
						keys[i++] = sqlField.getKey();
					}

					clipboard.setContents(new Object[] { keys }, new Transfer[] { SQLFieldKeysTransfer.getInstance() });

					if (e.keyCode == 'x') {
						removeAvailableColumns(sqlFields);
					}
				}
				else if (e.stateMask == SWT.CTRL && e.keyCode == 'v') {
					try {
						String[] keys = (String[]) clipboard.getContents(SQLFieldKeysTransfer.getInstance());
						if (keys != null) {
							// determine dropTarget: not necessary

							// determine dropSource and execute operation
							for (int i = 0; i < keys.length; i++) {
								SQLField sqlField = getSQLField(keys[i]);
								addAvailableColumns(sqlField, false);
							}
							availableColumnsViewer.refresh();
						}
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
				else if (e.stateMask == 0 && e.keyCode == SWT.DEL) {
					try {
						Collection<SQLField> sqlFields = getAvailableColumnsSelection();
						removeAvailableColumns(sqlFields);
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			}
		});

		if (withGroups) {
			groupColumnLabel.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.stateMask == SWT.CTRL && (e.keyCode == 'x' || e.keyCode == 'c')) {
						if (groupSQLField != null) {
							String[] keys = new String[] { groupSQLField.getKey() };
							clipboard.setContents(new Object[] { keys }, new Transfer[] { SQLFieldKeysTransfer
								.getInstance() });
							if (e.keyCode == 'x') {
								setGroupColumn(null);
							}
						}
					}
					else if (e.stateMask == SWT.CTRL && e.keyCode == 'v') {
						try {
							String[] keys = (String[]) clipboard.getContents(SQLFieldKeysTransfer.getInstance());
							if (keys != null) {
								// determine dropTarget: not necessary

								// determine dropSource and execute operation
								if (keys.length == 1) {
									SQLField sqlField = getSQLField(keys[0]);
									setGroupColumn(sqlField);
								}
							}
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
						}
					}
				}
			});
		}

		if (withSelection) {
			selectColumnsViewer.getTable().addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.stateMask == SWT.CTRL && (e.keyCode == 'x' || e.keyCode == 'c')) {
						List<SQLFieldContainer> sqlFieldContainers = getSelectColumnsSelection();

						String[] keys = new String[sqlFieldContainers.size()];
						int i = 0;
						for (SQLFieldContainer sqlFieldContainer : sqlFieldContainers) {
							keys[i++] = sqlFieldContainer.getSqlField().getKey();
						}

						clipboard.setContents(new Object[] { keys }, new Transfer[] { SQLFieldKeysTransfer
							.getInstance() });

						if (e.keyCode == 'x') {
							removeSelectSQLFieldContainers(sqlFieldContainers);
						}
					}
					else if (e.stateMask == SWT.CTRL && e.keyCode == 'v') {
						try {
							String[] keys = (String[]) clipboard.getContents(SQLFieldKeysTransfer.getInstance());

							if (keys != null) {
								// determine dropTarget: last selected element
								int[] selectionIndices = selectColumnsViewer.getTable().getSelectionIndices();
								int index = selectColumnsViewer.getTable().getItemCount();
								if (selectionIndices != null && selectionIndices.length > 0) {
									index = selectionIndices[selectionIndices.length - 1] + 1;
								}

								// determine dropSource and execute operation
								ArrayList<SQLField> transferedSQLFields = new ArrayList<>(keys.length);
								for (int i = 0; i < keys.length; i++) {
									SQLField sqlField = getSQLField(keys[i]);
									transferedSQLFields.add(sqlField);
								}

								List<SQLFieldContainer> newSQLFieldContainers = addSelectColumns(transferedSQLFields, index);

								// select the pasted elements
								ISelection selection = new StructuredSelection(newSQLFieldContainers);
								selectColumnsViewer.setSelection(selection, true/* reveal */);
							}
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
						}
					}
					else if (e.stateMask == 0 && e.keyCode == SWT.DEL) {
						try {
							List<SQLFieldContainer> sqlFieldContainers = getSelectColumnsSelection();
							removeSelectSQLFieldContainers(sqlFieldContainers);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
						}
					}
				}
			});
		}

		orderColumnsViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && (e.keyCode == 'x' || e.keyCode == 'c')) {
					List<SQLFieldContainer> sqlFieldContainers = getOrderColumnsSelection();

					String[] keys = new String[sqlFieldContainers.size()];
					int i = 0;
					for (SQLFieldContainer sqlFieldContainer : sqlFieldContainers) {
						keys[i++] = sqlFieldContainer.getSqlField().getKey();
					}

					clipboard.setContents(new Object[] { keys }, new Transfer[] { SQLFieldKeysTransfer.getInstance() });

					if (e.keyCode == 'x') {
						removeOrderSQLFieldContainers(sqlFieldContainers);
					}
				}
				else if (e.stateMask == SWT.CTRL && e.keyCode == 'v') {
					try {
						String[] keys = (String[]) clipboard.getContents(SQLFieldKeysTransfer.getInstance());

						if (keys != null) {
							// determine dropTarget: last selected element
							int[] selectionIndices = orderColumnsViewer.getTable().getSelectionIndices();
							int index = orderColumnsViewer.getTable().getItemCount();
							if (selectionIndices != null && selectionIndices.length > 0) {
								index = selectionIndices[selectionIndices.length - 1] + 1;
							}

							// determine dropSource and execute operation
							ArrayList<SQLField> transferedSQLFields = new ArrayList<>(keys.length);
							for (int i = 0; i < keys.length; i++) {
								SQLField sqlField = getSQLField(keys[i]);
								transferedSQLFields.add(sqlField);
							}

							List<SQLFieldContainer> newSQLFieldContainers = addOrderColumns(transferedSQLFields, index);

							// select the pasted elements
							ISelection selection = new StructuredSelection(newSQLFieldContainers);
							orderColumnsViewer.setSelection(selection, true/* reveal */);
						}
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
				else if (e.stateMask == 0 && e.keyCode == SWT.DEL) {
					try {
						List<SQLFieldContainer> sqlFieldContainers = getOrderColumnsSelection();
						removeOrderSQLFieldContainers(sqlFieldContainers);
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			}
		});

		// *
		// * Cut, Copy and Paste
		// **************************************************************************

		// **************************************************************************
		// * enabling of arrow buttons
		// *

		// Disable all arrow buttons initially.
		if (withSelection) {
			addSelectColumnButton.setEnabled(false);
			removeSelectColumnButton.setEnabled(false);

			selectColumnsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					removeSelectColumnButton.setEnabled(selection.size() > 0);
				}
			});

		}
		addOrderColumnButton.setEnabled(false);
		removeOrderColumnButton.setEnabled(false);

		// Enabling of removeGroupColumnButton is implemented in setGroupColumn().
		// The enabling of all other Arrow-Buttons depends on the selection of a certain Viewer.

		availableColumnsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();

				if (withSelection) {
					addSelectColumnButton.setEnabled(selection.size() > 0);
				}
				addOrderColumnButton.setEnabled(selection.size() > 0);
			}
		});

		orderColumnsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				removeOrderColumnButton.setEnabled(selection.size() > 0);
			}
		});

		// *
		// * enabling of arrow buttons
		// **************************************************************************

	}


	private Collection<SQLField> getAvailableColumnsSelection() {
		@SuppressWarnings("rawtypes")
		List<TreeNode> selectedNodes = SelectionHelper.getSelection(availableColumnsViewer, TreeNode.class);

		/*
		 * The use of a LinkedHashSet avoids multiple occurrence of SQLFields (Set) and assures the order of insertion
		 * (linking).
		 */
		LinkedHashSet<SQLField> sqlFields = new LinkedHashSet<>(selectedNodes.size());
		for (TreeNode<?> treeNode : selectedNodes) {
			extract(treeNode, sqlFields);
		}
		return sqlFields;
	}


	private List<SQLFieldContainer> getSelectColumnsSelection() {
		return SelectionHelper.toList(selectColumnsViewer.getSelection(), SQLFieldContainer.class);
	}


	private List<SQLFieldContainer> getOrderColumnsSelection() {
		return SelectionHelper.toList(orderColumnsViewer.getSelection(), SQLFieldContainer.class);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		this.reportParameter = (IOrderFieldsReportParameter) reportParameter;

		// Some assertions to guard ourselves against sloppy programming
		if (withGroups) {
			assert reportParameter instanceof IGroupFieldReportParameter;
		}

		if (withSelection) {
			assert reportParameter instanceof ISelectFieldsReportParameter;
		}
	}


	public void setAbstractSearch(AbstractSearch abstractSearch) {
		this.abstractSearch = abstractSearch;

		// remove existing nodes if necessary
		pathMap.clear();
		root.removeAll();
		selectColumnsList.clear();
		orderColumnsList.clear();

		// determine select fields
		ArrayList<SQLField> selectSqlFields = abstractSearch.getSearchFieldsCVO().getSelectFields();

		// add the selectSQLFIelds to the availableViewer
		for (SQLField sqlField : selectSqlFields) {
			addAvailableColumns(sqlField, false);
		}

		availableColumnsViewer.setInput(root);

		if (withGroups && reportParameter instanceof IGroupFieldReportParameter) {
			// set GroupSQLField
			GroupField groupField = ((IGroupFieldReportParameter) reportParameter).getGroupField();

			if (groupField != null) {
				SQLField groupSQLField = abstractSearch.getSQLField(groupField.key);
				if (groupSQLField != null) {
					setGroupColumn(groupSQLField);
				}
			}
		}

		// initialize dialog with ReportParameter
		try {
			if (withSelection && reportParameter instanceof ISelectFieldsReportParameter) {
				List<SQLField> selectSQLFields = getSelectSQLFields(
					(ISelectFieldsReportParameter) reportParameter,
					abstractSearch
				);

				for (SQLField sqlField : selectSQLFields) {
					/*
					 * Move SQLField from availableViewer to selectViewer, but only if the SQLField exists. In cases of
					 * changes of the Events master data it may happen, that they doesn't exist.
					 */
					if (selectSqlFields.contains(sqlField)) {
						moveAvailableColumnsToSelectColumns(sqlField);
					}
				}
				selectColumnsViewer.refresh();
			}
			if (reportParameter instanceof IOrderFieldsReportParameter) {
				List<SQLField> orderSQLFields = getOrderSQLFields(reportParameter, abstractSearch);
				// remove non existing orderSQLFields
				for (Iterator<SQLField> it = orderSQLFields.iterator(); it.hasNext();) {
					SQLField sqlField = it.next();
					if (!selectSqlFields.contains(sqlField)) {
						it.remove();
					}
				}
				addOrderColumns(orderSQLFields, 0);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		availableColumnsViewer.refresh();
	}


	private List<SQLField> getSelectSQLFields(ISelectFieldsReportParameter reportParameters, AbstractSearch abstractSearch) {
    	List<SQLField> sqlFieldList = new ArrayList<>();

		try {
			List<SelectField> selectFields = reportParameters.getSelectFields();
			if (selectFields != null) {
				for (SelectField selectField : selectFields) {
					if (selectField.key != null) {
						SQLField sqlField = abstractSearch.getSQLField(selectField.key);

						if (sqlField != null) {
							sqlFieldList.add(sqlField);
						}
					}
				}
			}
		}
		catch (RuntimeException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return sqlFieldList;
	}


    private List<SQLField> getOrderSQLFields(IOrderFieldsReportParameter reportParameters, AbstractSearch abstractSearch) {
    	List<SQLField> sqlFieldList = new ArrayList<>();

		try {
			List<OrderField> orderFields = reportParameters.getOrderFields();
			if (orderFields != null) {
				for (OrderField orderField : orderFields) {
					if (orderField.key != null) {
						SQLField sqlField = abstractSearch.getSQLField(orderField.key);

						if (sqlField != null) {
							sqlFieldList.add(sqlField);
						}
					}
				}
			}
		}
		catch (RuntimeException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return sqlFieldList;
	}



	/**
	 * Adds a SQLField to the Tree. If the SQLFields Directories doesn't exist they will be created.
	 *
	 * @param sqlField
	 */
	private void addAvailableColumns(SQLField sqlField, boolean refresh) {
		/*
		 * For SQLField - determine or create parent node (directory) - create leave node
		 */

		// determine all parent nodes (directories) in which the select field shall appear
		List<SQLFieldPath> pathList = sqlField.getPathList();
		for (SQLFieldPath path : pathList) {
			TreeNode<SQLDirectory> parentNode = pathMap.get(path);

			/*
			 * If the parent directory node does not exist: Create it and (if required) all of its parent nodes
			 */
			if (parentNode == null) {
				/* Split the path into its elements (directories).
				 * Check if the directory already exist (start from the root directory).
				 * If it does not exist: create it.
				 */
				SQLFieldPath currentPath = new SQLFieldPath();
				TreeNode<SQLDirectory> currentDir = root;
				for (I18NString pathElement : path) {
					currentPath.add(pathElement);
					/*
					 * Use parentNode temporarily as variable for the parent nodes.
					 * Finally parentNode references the parent node of the SQLField.
					 */
					parentNode = pathMap.get(currentPath);
					if (parentNode == null) {
						String label = pathElement.getString();
						SQLFieldPath singlePath = currentPath.clone();
						SQLDirectory sqlDirectory = new SQLDirectory(singlePath, label);
						parentNode = new DefaultTreeNode<>(availableColumnsViewer, currentDir, sqlDirectory, true);
						pathMap.put(singlePath, parentNode);
					}
					currentDir = parentNode;
				}
			}

			// if the SQLField doesn't already exist in this parentNode, create it
			boolean exist = false;

			List<TreeNode<?>> children = parentNode.getChildren();
			if (children != null) {
				for (TreeNode<?> childTreeNode : children) {
					Object value = childTreeNode.getValue();
					if (value != null && value instanceof SQLField && ((SQLField) value).equals(sqlField)) {
						exist = true;
						break;
					}
				}
			}

			if (!exist) {
				new DefaultTreeNode<>(availableColumnsViewer, parentNode, sqlField, true);
			}

		}
		if (refresh) {
			availableColumnsViewer.refresh();
		}
	}


	protected void moveAvailableColumnToGroupField() {
		IStructuredSelection selection = (IStructuredSelection) availableColumnsViewer.getSelection();
		if (selection.size() == 1) {
			TreeNode<?> treeNode = (TreeNode<?>) selection.getFirstElement();
			Object treeNodeValue = treeNode.getValue();
			if (treeNodeValue instanceof SQLField) {
				SQLField sqlField = (SQLField) treeNodeValue;
				removeAvailableColumns( createArrayList(sqlField) );
				setGroupColumn(sqlField);
			}
		}
	}


	protected void moveGroupFieldToAvailableColumn() {
		if (groupSQLField != null) {
			try {
				// add Group-Field to AvailableColumnsViewer
				addAvailableColumns(groupSQLField, false);

				// set Group-Field to null
				setGroupColumn(null);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			availableColumnsViewer.refresh();
		}
	}


	protected void copyAvailableColumnsToSelectColumns() {
		try {
			Collection<SQLField> sqlFields = getAvailableColumnsSelection();

			// handle SQLFields
			List<SQLFieldContainer> newSQLFieldContainers = SQLFieldContainer.wrap(sqlFields);
			List<SQLField> toRemoveSQLFields = createArrayList(sqlFields.size());
			for (SQLFieldContainer fieldContainer : newSQLFieldContainers) {
				if (!selectColumnsList.contains(fieldContainer)) {
					selectColumnsList.add(fieldContainer);
					toRemoveSQLFields.add(fieldContainer.getSqlField());
				}
			}
			removeAvailableColumns(toRemoveSQLFields);

			selectColumnsViewer.refresh();

			// select the pasted elements
			ISelection selection = new StructuredSelection(newSQLFieldContainers);
			selectColumnsViewer.setSelection(selection, true/* reveal */);

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void copyAvailableColumnsToOrderColumns() {
		try {
			Collection<SQLField> sqlFields = getAvailableColumnsSelection();

			// handle SQLFields
			List<SQLFieldContainer> newSQLFieldContainers = SQLFieldContainer.wrap(sqlFields);
			List<SQLField> toRemoveSQLFields = createArrayList(sqlFields.size());
			for (SQLFieldContainer fieldContainer : newSQLFieldContainers) {
				if (!orderColumnsList.contains(fieldContainer)) {
					orderColumnsList.add(fieldContainer);
					toRemoveSQLFields.add(fieldContainer.getSqlField());
				}
			}
			removeAvailableColumns(toRemoveSQLFields);

			orderColumnsViewer.refresh();

			// select the pasted elements
			ISelection selection = new StructuredSelection(newSQLFieldContainers);
			orderColumnsViewer.setSelection(selection, true/* reveal */);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Moves a SQLField from the AvailableViewer to the SelectViewer.
	 *
	 * @param sqlField
	 */
	private void moveAvailableColumnsToSelectColumns(SQLField sqlField) {
		if (sqlField != null) {
			// remove from Tree
			List<SQLFieldPath> pathList = sqlField.getPathList();
			for (SQLFieldPath path : pathList) {
				TreeNode<SQLDirectory> parentNode = pathMap.get(path);
				if (parentNode != null) {
					parentNode.removeChildByValue(sqlField);
					if (!parentNode.hasChildrenMaybe()) {
						removeAvailableDirectory(parentNode);
					}
				}
			}

			// add to List
			SQLFieldContainer sqlFieldContainer = new SQLFieldContainer(sqlField);
			if (!selectColumnsList.contains(sqlFieldContainer)) {
				selectColumnsList.add(sqlFieldContainer);
			}
		}
	}


	// **************************************************************************
	// * Drag and Drop
	// *

	private SQLField getSQLField(String sqlFieldKey) {
		return abstractSearch.getSQLField(sqlFieldKey);
	}


	private void setGroupColumn(SQLField sqlField) {
		groupSQLField = sqlField;

		if (groupSQLField != null) {
			groupColumnLabel.setText(sqlField.getLabel());
			// Image image =
			// de.regasus.report.Activator.ui.getImageDescriptor(IImageKeys.SQL_SELECT_FIELD).createImage();
			// groupColumnViewer.setImage(image);

			ArrayList<SQLField> sqlFields = new ArrayList<>(1);
			sqlFields.add(sqlField);

			removeAvailableColumns(sqlFields);
			removeSelectSQLFields(sqlFields);
			removeOrderSQLFields(sqlFields);
		}
		else {
			groupColumnLabel.setText("");
			// groupColumnViewer.setImage(null);
		}

		if (withGroups) {
			removeGroupColumnButton.setEnabled(groupSQLField != null);
		}
	}


	public void removeAvailableColumns(Collection<SQLField> availableSQLFields) {
		if (availableSQLFields != null) {
			// remove from availableColumnViewer

			for (SQLField sqlField : availableSQLFields) {
				if (sqlField != null) {
					List<SQLFieldPath> pathList = sqlField.getPathList();
					for (SQLFieldPath path : pathList) {
						TreeNode<SQLDirectory> parentNode = pathMap.get(path);
						if (parentNode != null) {
							parentNode.removeChildByValue(sqlField);
						}
						if (!parentNode.hasChildrenMaybe()) {
							removeAvailableDirectory(parentNode);
						}
					}
				}
			}
			availableColumnsViewer.refresh();
		}
	}


	public List<SQLFieldContainer> addSelectColumns(List<SQLField> newSQLFields, final int index) {
		List<SQLFieldContainer> newSQLFieldContainers = null;
		if (newSQLFields != null) {
			// add to selectColumnViewer
			newSQLFieldContainers = SQLFieldContainer.wrap(newSQLFields);
			for (SQLFieldContainer sqlFieldContainer : newSQLFieldContainers) {
				if (!selectColumnsList.contains(sqlFieldContainer)) {
					selectColumnsList.add(index, sqlFieldContainer);
				}
			}
			selectColumnsViewer.refresh();
		}
		return newSQLFieldContainers;
	}


	public void removeSelectSQLFieldContainers(List<SQLFieldContainer> sqlFieldContainers) {
		if (sqlFieldContainers != null) {
			for (SQLFieldContainer sqlFieldContainer : sqlFieldContainers) {
				if (sqlFieldContainer != null) {
					selectColumnsList.remove(sqlFieldContainer);
				}
			}
			selectColumnsViewer.refresh();
		}
	}


	public void removeSelectSQLFields(List<SQLField> sqlFields) {
		if (sqlFields != null) {
			for (SQLField sqlField : sqlFields) {
				if (sqlField != null) {
					for (Iterator<SQLFieldContainer> it = selectColumnsList.iterator(); it.hasNext();) {
						SQLFieldContainer sqlFieldContainer = it.next();
						if (sqlField.equals(sqlFieldContainer.getSqlField())) {
							it.remove();
						}
					}
				}
			}
			selectColumnsViewer.refresh();
		}
	}


	public List<SQLFieldContainer> addOrderColumns(List<SQLField> newSQLFields, int index) {
		List<SQLFieldContainer> newSQLFieldContainers = null;
		if (newSQLFields != null) {
			// add to orderColumnsViewer
			newSQLFieldContainers = SQLFieldContainer.wrap(newSQLFields);
			for (SQLFieldContainer sqlFieldContainer : newSQLFieldContainers) {
				if (!orderColumnsList.contains(sqlFieldContainer)) {
					orderColumnsList.add(index++, sqlFieldContainer);
				}
			}
			orderColumnsViewer.refresh();
		}
		return newSQLFieldContainers;
	}


	public void removeOrderSQLFields(List<SQLField> sqlFields) {
		if (sqlFields != null) {
			for (SQLField sqlField : sqlFields) {
				if (sqlField != null) {
					orderColumnsList.remove(sqlField);
				}
			}

			orderColumnsViewer.refresh();
		}
	}


	public void removeOrderSQLFieldContainers(List<SQLFieldContainer> sqlFieldContainers) {
		if (sqlFieldContainers != null) {
			for (SQLFieldContainer sqlFieldContainer : sqlFieldContainers) {
				if (sqlFieldContainer != null) {
					orderColumnsList.remove(sqlFieldContainer);
				}
			}
			orderColumnsViewer.refresh();
		}
	}

	// *
	// * Drag and Drop
	// **************************************************************************

	private void removeAvailableDirectory(TreeNode<SQLDirectory> dirTreeNode) {
		pathMap.remove(dirTreeNode.getValue().getPath());
		dirTreeNode.getParent().removeChild(dirTreeNode);
	}


	@SuppressWarnings({ "rawtypes"})
	private void extract(TreeNode treeNode, Collection<SQLField> sqlFields) {
		Object treeNodeValue = treeNode.getValue();
		if (treeNodeValue instanceof SQLField) {
			sqlFields.add((SQLField) treeNodeValue);
		}
		else if (treeNodeValue instanceof SQLDirectory) {
			List<TreeNode> children = treeNode.getChildren();
			if (children != null) {
				for (TreeNode childTreeNode : children) {
					extract(childTreeNode, sqlFields);
				}
			}
		}
	}


	protected void moveSelectColumnToAvailableColumn() {
		try {
			for (SQLFieldContainer sqlFieldContainer : getSelectColumnsSelection()) {
				selectColumnsList.remove(sqlFieldContainer);
				addAvailableColumns(sqlFieldContainer.getSqlField(), false);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		availableColumnsViewer.refresh();
		selectColumnsViewer.refresh();
	}


	protected void moveOrderColumnToAvailableColumn() {
		try {
			for (SQLFieldContainer sqlFieldContainer : getOrderColumnsSelection()) {
				orderColumnsList.remove(sqlFieldContainer);
				addAvailableColumns(sqlFieldContainer.getSqlField(), false);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		availableColumnsViewer.refresh();
		orderColumnsViewer.refresh();
	}


	@Override
	public void saveReportParameters() {
		if (reportParameter == null) {
			return;
		}

		StringBuilder desc;

		// =============== Ordering ===============
		List<SQLField> orderSQLFields = SQLFieldContainer.unwrap(orderColumnsList);

		reportParameter.setOrderSQLFields(orderSQLFields);

		// set description for Order-Fields
		desc = new StringBuilder();
		if (!orderColumnsList.isEmpty()) {
			desc.append(ReportWizardI18N.AbstractSelectWizardPage_OrderColumnsLabel);
			desc.append(": ");
			for (Iterator<SQLField> it = orderSQLFields.iterator(); it.hasNext();) {
				SQLField sqlField = it.next();
				desc.append(sqlField.getLabel());
				if (it.hasNext()) {
					desc.append(", ");
				}
			}
		}
		reportParameter.setDescription(DESCRIPTION_ORDER_ID, desc.toString());

		// remove descriptions set by legacy code
		reportParameter.setDescription("participantOrderFields", null);


		// =============== Selection ===============
		if (withSelection && reportParameter instanceof ISelectFieldsReportParameter) {
			List<SQLField> selectSQLFields = SQLFieldContainer.unwrap(selectColumnsList);
			((ISelectFieldsReportParameter) reportParameter).setSelectSQLFields(selectSQLFields);

			// set description for Select-Fields
			desc = new StringBuilder();
			desc.append(ReportWizardI18N.AbstractSelectWizardPage_ReportColumnsLabel);
			desc.append(": ");

			for (Iterator<SQLField> it = selectSQLFields.iterator(); it.hasNext();) {
				SQLField sqlField = it.next();
				desc.append(sqlField.getLabel());
				if (it.hasNext()) {
					desc.append(", ");
				}
			}
			reportParameter.setDescription(DESCRIPTION_SELECT_ID, desc.toString());

			// remove descriptions set by legacy code
			reportParameter.setDescription("participantSelectFields", null);
		}


		// =============== Grouping ===============
		if (withGroups && reportParameter instanceof IGroupFieldReportParameter) {
			((IGroupFieldReportParameter) reportParameter).setGroupSQLField(groupSQLField);

			// set description for Group-Field
			desc = new StringBuilder();
			if (groupSQLField != null) {
				desc.append(ReportWizardI18N.AbstractSelectWizardPage_GroupColumnLabel);
				desc.append(": ");
				desc.append(groupSQLField.getLabel());
			}
			reportParameter.setDescription(
				DESCRIPTION_GROUP_ID,
				desc.toString()
			);

			// remove descriptions set by legacy code
			reportParameter.setDescription("participantGroupField", null);
		}

	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void dispose() {
		super.dispose();
		if (clipboard != null && !clipboard.isDisposed()) {
			clipboard.dispose();
		}
	}


	private String[] getKeys(List<SQLFieldContainer> selection) {
		String[] keys = new String[selection.size()];
		int i = 0;

		for (SQLFieldContainer sqlFieldContainer : selection) {
			keys[i++] = sqlFieldContainer.getSqlField().getKey();
		}
		return keys;
	}

}
