package de.regasus.common.customfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.lambdalogic.messeinfo.contact.CustomFieldListValue;
import com.lambdalogic.messeinfo.contact.CustomFieldListValuePositionComparator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IUpDownListener;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.UpDownComposite;
import de.regasus.core.ui.dialog.CustomFieldListValueDialog;

/**
 * A widget showing the list values of a custom field, with the possibility to add, edit and delete values.
 * To be able to use this class for different types of CustomFieldListValues, an
 * ICustomFieldListValueFactory has to be set, that is responsible for creating new
 * CustomFieldListValues of the correct type.
 */
public class CustomFieldListValueListComposite<ListValueType extends CustomFieldListValue>
extends Group
implements IUpDownListener, IDoubleClickListener, SelectionListener {

	private ICustomFieldListValueFactory<ListValueType> customFieldListValueFactory;
	private Collection<String> defaultLanguages = Collections.singleton( Locale.getDefault().getLanguage() );

	private ListViewer listViewer;
	private org.eclipse.swt.widgets.List list;
	private List<ListValueType> customFieldListValueList = new ArrayList<>();
	private ToolItem addButton;
	private ToolItem editButton;
	private ToolItem deleteButton;
	private CustomFieldListValueLabelProvider customFieldListValueLabelProvider;

	private ModifySupport modifySupport;
	private ToolBar toolBar;
	private UpDownComposite upDownComposite;


	public CustomFieldListValueListComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));

		modifySupport = new ModifySupport(this);


		toolBar = new ToolBar(this, SWT.FLAT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));

		// The button to add an attachment
		addButton = new ToolItem(toolBar, SWT.PUSH);
		addButton.setImage(IconRegistry.getImage(IImageKeys.CREATE));
		addButton.setToolTipText(UtilI18N.Add + UtilI18N.Ellipsis);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAdd();
			}
		});

		// The button to open selected attachments
		editButton = new ToolItem(toolBar, SWT.PUSH);
		editButton.setImage(IconRegistry.getImage(IImageKeys.EDIT));
		editButton.setToolTipText(UtilI18N.View);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEdit();
			}
		});

		// The button to delete selected attachments
		deleteButton = new ToolItem(toolBar, SWT.PUSH);
		deleteButton.setImage(IconRegistry.getImage(IImageKeys.DELETE));
		deleteButton.setToolTipText(UtilI18N.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDelete();
			}
		});

		list = new org.eclipse.swt.widgets.List(this, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listViewer = new ListViewer(list);

		listViewer.setContentProvider(ArrayContentProvider.getInstance());
		customFieldListValueLabelProvider = new CustomFieldListValueLabelProvider();
		listViewer.setLabelProvider(customFieldListValueLabelProvider);
		listViewer.setInput(customFieldListValueList);
		listViewer.addDoubleClickListener(this);
		list.addSelectionListener(this);

		upDownComposite = new UpDownComposite(this, SWT.NONE);
		upDownComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		upDownComposite.setEnabled(false);
		upDownComposite.setUpDownListener(this);
	}


	public void setCustomFieldListValueFactory(ICustomFieldListValueFactory<ListValueType> customFieldListValueFactory) {
		this.customFieldListValueFactory = customFieldListValueFactory;
	}


	public void setDefaultLanguages(Collection<String> defaultLanguages) {
		this.defaultLanguages = defaultLanguages;
	}


	private List<String> getUsedLanguageCodes() {
		Set<String> usedLanguageCodeSet = new HashSet<>();

		for (CustomFieldListValue customFieldListValue: customFieldListValueList) {
			usedLanguageCodeSet.addAll(customFieldListValue.getLabel().getLanguageCodes());
		}

		List<String> usedLanguageCodeList = new ArrayList<>(usedLanguageCodeSet);
		Collections.sort(usedLanguageCodeList);
		return usedLanguageCodeList;
	}


	protected void onDelete() {
		CustomFieldListValue customFieldListValue = SelectionHelper.getUniqueSelected(listViewer.getSelection());
		String value = customFieldListValue.getLabel().getString();
		String message = NLS.bind(UtilI18N.ReallyDeleteOne, UtilI18N.Value, value);
		boolean answer = MessageDialog.openQuestion(getShell(), UtilI18N.Question, message);
		if (answer) {
			customFieldListValueList.remove(customFieldListValue);
			customFieldListValueLabelProvider.setLanguageCodesToShow(getUsedLanguageCodes());
			somethingChanged();
		}
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		list.setEnabled(enabled);
		toolBar.setEnabled(enabled);
	}


	public void setCustomFieldListValueList(Collection<ListValueType> customFieldListValueList) {
		this.customFieldListValueList.clear();
		this.customFieldListValueList.addAll(customFieldListValueList);

		Collections.sort(this.customFieldListValueList, CustomFieldListValuePositionComparator.getInstance());

		customFieldListValueLabelProvider.setLanguageCodesToShow(getUsedLanguageCodes());
		somethingChanged();
	}


	public void onAdd() {
		ListValueType listValue = customFieldListValueFactory.createCustomFieldListValue();
		boolean edited = CustomFieldListValueDialog.openAddInstance(getShell(), listValue, defaultLanguages);
		if (edited) {
			customFieldListValueList.add(listValue);
			customFieldListValueLabelProvider.setLanguageCodesToShow(getUsedLanguageCodes());
			somethingChanged();
		}
	}


	public void onEdit() {
		CustomFieldListValue listValue = SelectionHelper.getUniqueSelected(listViewer.getSelection());
		edit(listValue);
	}


	private void edit(CustomFieldListValue listValue) {
		boolean edited = CustomFieldListValueDialog.openEditInstance(getShell(), listValue, defaultLanguages);
		if (edited) {
			customFieldListValueLabelProvider.setLanguageCodesToShow(listValue.getLabel().getUsedLanguageCodes());
			listViewer.refresh();
			modifySupport.fire();
		}
	}


	public List<ListValueType> getCustomFieldListValues() {
		return customFieldListValueList;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	private void somethingChanged() {
		for (int i = 0; i < customFieldListValueList.size(); i++) {
			customFieldListValueList.get(i).setPosition(i);
		}
		listViewer.refresh();
		updateButtonStates();
		modifySupport.fire();
	}


	@Override
	public void bottomPressed() {
		ListValueType listValue = SelectionHelper.getUniqueSelected(listViewer);
		customFieldListValueList.remove(listValue);
		customFieldListValueList.add(listValue);
		somethingChanged();
	}



	@Override
	public void downPressed() {
		ListValueType listValue = SelectionHelper.getUniqueSelected(listViewer);
		int index = customFieldListValueList.indexOf(listValue);
		if (index < customFieldListValueList.size() - 1) {
			customFieldListValueList.remove(listValue);
			customFieldListValueList.add(index + 1, listValue);
			somethingChanged();
		}

	}


	@Override
	public void upPressed() {
		ListValueType listValue = SelectionHelper.getUniqueSelected(listViewer);
		int index = customFieldListValueList.indexOf(listValue);
		if (index > 0) {
			customFieldListValueList.remove(listValue);
			customFieldListValueList.add(index - 1, listValue);
			somethingChanged();
		}
	}


	@Override
	public void topPressed() {
		ListValueType listValue = SelectionHelper.getUniqueSelected(listViewer);
		customFieldListValueList.remove(listValue);
		customFieldListValueList.add(0, listValue);
		somethingChanged();
	}


	@Override
	public void doubleClick(DoubleClickEvent event) {
		ListValueType listValue = SelectionHelper.getUniqueSelected(event.getSelection());
		edit(listValue);
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		updateButtonStates();
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	private void updateButtonStates() {
		if (list.getSelectionCount() == 0) {
			upDownComposite.setEnabled(false);
			editButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
		else {
			boolean upEnabled = ! list.isSelected(0);
			upDownComposite.setTopEnabled(upEnabled);
			upDownComposite.setUpEnabled(upEnabled);

			boolean downEnabled = ! list.isSelected(list.getItemCount() - 1);
			upDownComposite.setDownEnabled(downEnabled);
			upDownComposite.setBottomEnabled(downEnabled);

			editButton.setEnabled(true);
			deleteButton.setEnabled(true);
		}
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

}
