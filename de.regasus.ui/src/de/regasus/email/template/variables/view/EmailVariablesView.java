package de.regasus.email.template.variables.view;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.report.script.ScriptContext;

import de.regasus.email.template.editor.ContextChangedListener;
import de.regasus.email.template.editor.EmailTemplateEditor;
import de.regasus.email.template.search.view.EmailTemplateSearchView;
import de.regasus.email.template.variables.VariableValuePair;
import de.regasus.email.template.variables.VariablesHelper;
import de.regasus.email.template.variables.VariablesTable;

/**
 * A view with a table, showing a list of sample variables taken from the {@link VariablesHelper}, being
 * either those of a participant or a profile. Care is taken that thee sample values are always updated according to the
 * {@link EmailTemplateEditor}, since each of them can have a different language and/or a different sampleRecipient.
 * Therefore this view implements both interfaces {@link IPartListener} and {@link ContextChangedListener}, so that it
 * can be notified when a) a different {@link EmailTemplateEditor} is opened, or b) gets another samplePerson or
 * language.
 *
 * @author manfred
 *
 */
public class EmailVariablesView extends ViewPart implements IPartListener, ContextChangedListener, ModifyListener {

	public static final String ID = "EmailVariablesView";

	// **************************************************************************
	// * Widgets
	// *

	private VariablesTable variablesTable;

	private VariablesHelper variablesHelper;

	private Table table;

	private boolean isShowing;

	private boolean isForEvent;


	private Long lastUsedEventPK;
	private String lastUsedLanguage;
	private long lastUsedContextHash;

	// Actions
	private CopyVariableAction copyAction;


	@Override
	public void createPartControl(Composite variablesTableComposite) {
		variablesHelper = new VariablesHelper();

		TableColumnLayout layout = new TableColumnLayout();
		variablesTableComposite.setLayout(layout);

		table = new Table(variablesTableComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(nameTableColumn, new ColumnWeightData(40));
		nameTableColumn.setText(EmailLabel.Variables.getString());

		final TableColumn valueTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(valueTableColumn, new ColumnWeightData(50));
		valueTableColumn.setText(EmailLabel.SampleValues.getString());

		getSite().getPage().addPartListener(this);

		variablesTable = new VariablesTable(table);
		variablesTable.addModifyListener(this);

		getSite().setSelectionProvider(variablesTable.getViewer());

		createActions();

		initializeContextMenu();

		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
	}



	private void createActions() {
		copyAction = new CopyVariableAction(table);
	}


	private void initializeContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				EmailVariablesView.this.fillContextMenu(manager);
			}
		});

		final TableViewer tableViewer = variablesTable.getViewer();
		final Table table = tableViewer.getTable();
		final Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(copyAction);
	}


	public VariablesTable getVariablesTable() {
		return variablesTable;
	}


	public VariablesHelper getVariablesHelper() {
		return variablesHelper;
	}


	public Table getTable() {
		return table;
	}



	@Override
	public void dispose() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			activePage.removePartListener(this);
		}
		super.dispose();
	}


	@Override
	public void setFocus() {
		try {
			if (table != null && !table.isDisposed() && table.isEnabled()) {
				table.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

	}


	public void fillVariables(EmailTemplateEditor emailTemplateEditor, boolean force) {
		EmailTemplate emailTemplate = emailTemplateEditor.getEmailTemplate();
		Long eventPK = emailTemplate.getEventPK();

		isForEvent = (eventPK != null);

		List<VariableValuePair> variableValuePairs = variablesHelper.getVariableValuePairList(isForEvent);

		// Don't evaluate if no data in context (avoiding IllegalArgumentException)
		if (emailTemplateEditor.isCanEvaluateContext()) {
			ScriptContext context = emailTemplateEditor.getContext();

			if (force || lastUsedContextHash != context.hashCode() || lastUsedEventPK != eventPK || lastUsedLanguage != emailTemplate.getLanguage())  {
				VariableValuePair.evaluateValuesWithContext(variableValuePairs, context);
				lastUsedContextHash = context.hashCode();
				lastUsedEventPK = eventPK;
				lastUsedLanguage = emailTemplate.getLanguage();
			}
		}

		variablesTable.getViewer().setInput(variableValuePairs);

		isShowing = true;
	}


	/**
	 * This method is called when the context changes in an {@link EmailTemplateEditor}, either by setting it to another
	 * language, or by getting another sampleRecipient from the {@link EmailTemplateSearchView}.
	 */
	@Override
	public void contextChanged(EmailTemplateEditor emailTemplateEditor) {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() == emailTemplateEditor) {
			fillVariables(emailTemplateEditor, true);
		}
	}


	public void clearVariables() {
		variablesTable.getViewer().setInput(Collections.emptyList());
		isShowing = false;
	}


	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof EditorPart) {
			if (part instanceof EmailTemplateEditor) {
				EmailTemplateEditor emailTemplateEditor = (EmailTemplateEditor) part;
				fillVariables(emailTemplateEditor, false);
				emailTemplateEditor.addContextChangedListener(this);
			}
			else {
				clearVariables();
			}
		}
	}


	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}


	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof EmailTemplateEditor) {
			EmailTemplateEditor emailTemplateEditor = (EmailTemplateEditor) part;
			emailTemplateEditor.removeContextChangedListener(this);
		}
	}


	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}


	@Override
	public void partOpened(IWorkbenchPart part) {
	}


	@Override
	public void modifyText(ModifyEvent e) {
		IEditorPart ep = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (ep instanceof EmailTemplateEditor) {
			fillVariables((EmailTemplateEditor)ep, true);
		}

		if (isShowing) {
			saveVariableValuePairList();
		}
	}


	public void saveVariableValuePairList() {
		variablesHelper.saveVariableValuePairList(isForEvent);
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editorPart instanceof EmailTemplateEditor) {
			EmailTemplateEditor emailTemplateEditor = (EmailTemplateEditor) editorPart;
			fillVariables(emailTemplateEditor, true);
		}
	}

}
