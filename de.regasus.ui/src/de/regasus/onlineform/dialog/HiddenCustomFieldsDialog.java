package de.regasus.onlineform.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.CollectionsHelper.sublistByComparison;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.regasus.FormLabel;
import com.lambdalogic.util.rcp.widget.ListSelectionComposite;

import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.provider.CustomFieldLabelProvider;

public class HiddenCustomFieldsDialog extends TitleAreaDialog {

	private List<Integer> customFields;
	private List<Integer> hiddenCustomFields;
	private ListSelectionComposite composite1;
	private ListSelectionComposite composite2;
	private ListSelectionComposite composite3;
	private ListSelectionComposite composite4;
	private LabelProvider labelProvider;
	private boolean travelEnabled;

	public HiddenCustomFieldsDialog(Shell parentShell, EventVO eventVO, List<Integer> customFields, List<Integer> hiddenCustomFields, boolean travelEnabled) {
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.labelProvider = new CustomFieldLabelProvider(eventVO);
		this.customFields = customFields;
		// a clone, so that we don't involuntarily change the config attribute
		this.hiddenCustomFields = new ArrayList(hiddenCustomFields);
		this.travelEnabled = travelEnabled;
	}

	
	
	@Override
	protected Control createDialogArea(Composite parent) {
   		String elements = ContactLabel.CustomFields.getString();
		String message = OnlineFormI18N.SelectWhichElementsToHideInOnlineForm.replace("<elements>", elements);

		setTitle(elements);
		setMessage(message);
		
		Composite area = (Composite) super.createDialogArea(parent);
		
		ArrayList<Integer> customFields_1_personal = sublistByComparison(customFields, 1, 10);
		ArrayList<Integer> customFields_2_arrival = sublistByComparison(customFields, 12, 20); // 11 = Arrival
		ArrayList<Integer> customFields_3_departure = sublistByComparison(customFields, 22, 30); // 12 = Departure
		ArrayList<Integer> customFields_4_summary = sublistByComparison(customFields, 31, 40);

		
		
		TabFolder tabFolder = new TabFolder (area, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// At least one of the following composites will be created, because otherwise
		// the dialog wouldn't have be opened at all.
		composite1 = createListSelectionComposite(tabFolder, customFields_1_personal, FormLabel.PersoenlicheAngaben.getString() + " (1-10)");
		if (travelEnabled) {
			composite2 = createListSelectionComposite(tabFolder, customFields_2_arrival, FormLabel.Anreise.getString() + " (12-20)");
			composite3 = createListSelectionComposite(tabFolder, customFields_3_departure, FormLabel.Abreise.getString() + " (22-30)");
		}
		composite4 = createListSelectionComposite(tabFolder, customFields_4_summary, FormLabel.Zusammenfassung.getString() + " (31-40)");
		
		return area;
	}
	
	
	private ListSelectionComposite createListSelectionComposite(TabFolder tabFolder, List<Integer> list, String titel) {
		ListSelectionComposite composite = null;
		if (notEmpty(list)) {
			TabItem item = new TabItem (tabFolder, SWT.NONE);
			item.setText(titel);
			composite = new ListSelectionComposite(tabFolder, list, labelProvider);
			composite.setCheckedElements(hiddenCustomFields);
			item.setControl(composite);
		}
		return composite;
	}
	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	
	@Override
	protected void okPressed() {
		hiddenCustomFields.clear();
		
		if (composite1 != null) {
			hiddenCustomFields.addAll(composite1.getCheckedElements());
		}
		
		if (composite2 != null) {
			hiddenCustomFields.addAll(composite2.getCheckedElements());
		}
		
		if (composite3 != null) {
			hiddenCustomFields.addAll(composite3.getCheckedElements());
		}

		if (composite4 != null) {
			hiddenCustomFields.addAll(composite4.getCheckedElements());
		}

		super.okPressed();
	}
	
	
	public List<Integer> getHiddenCustomFields() {
		return hiddenCustomFields;
	}
}
