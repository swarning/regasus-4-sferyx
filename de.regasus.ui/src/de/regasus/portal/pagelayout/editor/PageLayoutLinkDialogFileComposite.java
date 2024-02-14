package de.regasus.portal.pagelayout.editor;

import java.util.ArrayList;
import java.util.Collection;
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

import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.File;
import de.regasus.common.FileContentUrlHelper;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PortalFileHelper;
import de.regasus.portal.PortalFileModel;
import de.regasus.portal.portal.editor.PortalFileTable;
import de.regasus.ui.Activator;


public class PageLayoutLinkDialogFileComposite extends Composite {

	private static final PortalFileModel MODEL = PortalFileModel.getInstance();

	private Long portalId;
	private String language;

	private  ModifySupport modifySupport = new ModifySupport(this);

	// widgets
	private Table table;
	private TableViewer tableViewer;


	public PageLayoutLinkDialogFileComposite(Composite parent, int style, String language) {
		super(parent, style);

		this.language = language;

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

		TableColumn mnemonicTableColumn = new TableColumn(table, SWT.NONE);
		mnemonicTableColumn.setText(UtilI18N.Mnemonic);
		layout.setColumnData(mnemonicTableColumn, new ColumnWeightData(1));

		TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		languageTableColumn.setText(UtilI18N.Language);
		layout.setColumnData(languageTableColumn, new ColumnWeightData(1));

		TableColumn extPathTableColumn = new TableColumn(table, SWT.NONE);
		extPathTableColumn.setText(UtilI18N.File);
		layout.setColumnData(extPathTableColumn, new ColumnWeightData(3));

		TableColumn urlTableColumn = new TableColumn(table, SWT.NONE);
		urlTableColumn.setText(UtilI18N.URL);
		layout.setColumnData(urlTableColumn, new ColumnWeightData(3));

		PortalFileTable portalFileTable = new PortalFileTable(table);
		tableViewer = portalFileTable.getViewer();
	}


	private void syncWidgetsToModel() {
		List<File> files;
		try {
			synchronized (MODEL) {
				Collection<File> allFiles = MODEL.getPortalFiles(portalId);
				files = new ArrayList<>(allFiles.size());
				for (File file : allFiles) {
					if (file.getLanguage() == null || file.getLanguage().equals(language)) {
						files.add(file);
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
					tableViewer.setInput(files);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public String getLink() {
		String link = null;

		File file = SelectionHelper.getUniqueSelected(tableViewer);
		if (file != null) {
			String webServiceBaseUrl = "${rws.url}";
			String internalPath = file.getInternalPath();
			link = FileContentUrlHelper.buildUrl(webServiceBaseUrl, internalPath);
		}

		return link;
	}


	public String getDescription() {
		String description = null;

		File file = SelectionHelper.getUniqueSelected(tableViewer);
		if (file != null) {
			StringBuilder sb = new StringBuilder();

			sb.append(UtilI18N.File);
			sb.append(" - ");


			// append mnemonic
			try {
				String mnemonic = PortalFileHelper.extractFileMnemonic( file.getInternalPath() );
				sb.append(UtilI18N.Mnemonic);
				sb.append(": ");
				sb.append(mnemonic);
			}
			catch (ErrorMessageException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}


			// append language
			try {
				String language = PortalFileHelper.extractLanguage( file.getInternalPath() );
				if (language != null) {
					sb.append(", ");
					sb.append(UtilI18N.Language);
					sb.append(": ");
					sb.append(language);
				}
			}
			catch (ErrorMessageException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}


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
