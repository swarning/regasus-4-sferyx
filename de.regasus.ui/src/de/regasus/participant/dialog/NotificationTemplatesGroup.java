package de.regasus.participant.dialog;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.File;
import de.regasus.common.File_ExternalFileName_Comparator;

/**
 * An SWT group that shows files to be attached and allows their addition, editing and deletion.
 */
public class NotificationTemplatesGroup extends Group {

	/**
	 * The table that shows the notification templates that may be used.
	 */
	private Table table;

	private NotificationTemplateTable notificationTemplateTable;


	public NotificationTemplatesGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setText(ParticipantLabel.NotificationTemplates.getString());

		setLayout(new GridLayout(2, false));

		Composite tableComposite = new Composite(this, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);


		// The SWT table that shows the available notificationTemplates
		table = new Table(tableComposite, SWT.NONE /*.CHECK | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER*/);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn checkTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(checkTableColumn, new ColumnPixelData(10, false, true));

		TableColumn languageTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(languageTableColumn, new ColumnWeightData(100));
		languageTableColumn.setText(UtilI18N.Language);

		TableColumn nameTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(nameTableColumn, new ColumnWeightData(300));
		nameTableColumn.setText(UtilI18N.Name);

//		checkTableColumn.pack();
//		languageTableColumn.setWidth(100);
//		nameTableColumn.setWidth(500);



		/* notificationTemplateTableViewer.setComparator(new ViewerComparator());
		 *
		 * This does not work, because the ViewerComparator only works proper if the LabelProvider
		 * implements ILabelProvider. Instead of extending the NotificationTemplateLabelProvider
		 * we sort the elements directly in setNotificationTemplateList().
		 */


		notificationTemplateTable = new NotificationTemplateTable(table);


		Button selectAll = new Button(this, SWT.PUSH);
		selectAll.setText(UtilI18N.SelectAll);
		selectAll.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
//				tableViewer.setAllChecked(true);
				notificationTemplateTable.selectAll();
			}
		});

		Button deselectAll = new Button(this, SWT.PUSH);
		deselectAll.setText(UtilI18N.SelectNone);
		deselectAll.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
//				tableViewer.setAllChecked(false);
				notificationTemplateTable.selectNothing();
			}
		});

	}



	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public void addSelectionListener(SelectionListener listener) {
		table.addSelectionListener(listener);
	}


	public void setNotificationTemplateList(List<File> notificationTemplateList) {
		Collections.sort(notificationTemplateList, File_ExternalFileName_Comparator.getInstance());

		notificationTemplateTable.setInput(notificationTemplateList);
	}


	public List<Long> getCheckedTemplatePKs() {
		Collection<File> selectedFiles = notificationTemplateTable.getSelection();
		List<Long> selectedFileIds = File.getPKs(selectedFiles);
		return selectedFileIds;
	}


	public Collection<String> getSelectedTemplateExtensions() {
		Collection<String> extensions = CollectionsHelper.createHashSet();

		for (File file : notificationTemplateTable.getSelection()) {
			String extension = file.getExtension();
			extensions.add(extension);
		}

		return extensions;
	}

}
