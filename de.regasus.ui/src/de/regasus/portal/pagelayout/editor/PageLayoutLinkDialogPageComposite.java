package de.regasus.portal.pagelayout.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Page;
import de.regasus.portal.PageModel;
import de.regasus.portal.PortalI18N;
import de.regasus.ui.Activator;


public class PageLayoutLinkDialogPageComposite extends Composite {

	private static final PageModel MODEL = PageModel.getInstance();

	private Long portalId;

	private ModifySupport modifySupport = new ModifySupport(this);

	// widgets
	private Table table;
	private TableViewer tableViewer;


	public PageLayoutLinkDialogPageComposite(Composite parent, int style) {
		super(parent, style);

		createWidgets();
		table.addSelectionListener(modifySupport);
	}


	public void setPortalId(Long portalId) {
		this.portalId = portalId;
		syncWidgetsToModel();
	}


	private void createWidgets() {
		setLayout( new GridLayout() );
		Composite tableComposite = new Composite(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);

		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn titleTableColumn = new TableColumn(table, SWT.NONE);
		titleTableColumn.setText( Page.WINDOW_TITLE.getString() );
		layout.setColumnData(titleTableColumn, new ColumnWeightData(1));

		PageTable pageTable = new PageTable(table);
		tableViewer = pageTable.getViewer();
	}


	private void syncWidgetsToModel() {
		List<Page> pages;
		try {
			synchronized (MODEL) {
				List<Page> allPages = MODEL.getPagesByPortal(portalId);
				pages = new ArrayList<>(allPages.size());
				for (Page page : allPages) {
					if (page.isStaticAccess()) {
						pages.add(page);
					}
				}

			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return;
		}

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					tableViewer.setInput(pages);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public String getLink() {
		String link = null;

		Page page = SelectionHelper.getUniqueSelected(tableViewer);
		if (page != null) {
			link = "${portal.url}" + page.getUrlPath();
		}

		return link;
	}


	public String getDescription() {
		String description = null;

		Page page = SelectionHelper.getUniqueSelected(tableViewer);
		if (page != null) {
			StringBuilder sb = new StringBuilder();

			sb.append(PortalI18N.Page);
			sb.append(" - ");


			// append title
			String name = page.getName().getString();
			sb.append( Page.NAME.getString() );
			sb.append(": ");
			sb.append(name);


			description = sb.toString();
		}

		return description;
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


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
