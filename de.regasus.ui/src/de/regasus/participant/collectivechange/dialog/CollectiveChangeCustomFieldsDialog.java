package de.regasus.participant.collectivechange.dialog;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.messeinfo.participant.interfaces.OldCustomFieldUpdateParameter;
import com.lambdalogic.util.StringHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;


public class CollectiveChangeCustomFieldsDialog extends TitleAreaDialog  {

	/**
	 * Number of custom fields that can be changed at a time.
	 */
	private static final int OFFERED_FIELD_COUNT = 5;

	/**
	 * Map with the names and indexes of available custom fields.
	 * The keys of the Map are the names of the custom field names of the Event.
	 * The values are their index numbers.
	 */
	private Map<String, Integer> customField2IndexMap = new TreeMap<>();

	private List<Combo> customFieldComboList = createArrayList();
	private List<Text> customFieldTextList = createArrayList();
	private List<Button> customFieldOverwriteCheckBoxList = createArrayList();

	/**
	 * Number of selected participants.
	 */
	private int participantCount;

	/**
	 * List of OldCustomFieldUpdateParameter that represents the final settings the user entered into this dialog.
	 */
	private List<OldCustomFieldUpdateParameter> parameters = createArrayList(OFFERED_FIELD_COUNT);


	/**
	 * Constructor.
	 *
	 * @param parentShell
	 * @param customField2IndexMap - Map with the names and indexes of available custom fields.
	 *  The keys of the Map are the names of the custom field names of the Event.
	 *  The values are their index numbers.
	 * @param participantCount - Number of selected participants.
	 */
	public CollectiveChangeCustomFieldsDialog(
		Shell parentShell,
		Map<String, Integer> customField2IndexMap,
		int participantCount
	) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.customField2IndexMap = customField2IndexMap;
		this.participantCount = participantCount;
	}


	@Override
    public void create() {
		super.create();

		// set title and message after the dialog has been opened
		String title = I18N.CollectiveChangeCustomFieldsDialog_Title;
		title = title.replace("<count>", String.valueOf(participantCount));

		setTitle(title);
		setMessage(I18N.CollectiveChangeCustomFieldsDialog_Message);
    }


	/**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

    	resetButtonState();
    }


    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

		try {
			area.setLayout(new GridLayout());

			Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			composite.setLayout(new GridLayout(4, false));

			if (! customField2IndexMap.isEmpty()) {
				for (int i = 0; i < OFFERED_FIELD_COUNT; i++) {
					createCustomFieldEditRow(composite);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

    	return area;
    }


    private void createCustomFieldEditRow(Composite composite) {
    	// Label
		Label label = new Label(composite, SWT.NONE);
		label.setText(ContactLabel.CustomField.getString());


		// Combo that shows available custom fields
		Combo combo = new Combo(composite, SWT.READ_ONLY);
		combo.add(""); // An empty entry so that the user can deselect a custom field

		for (String fieldName : customField2IndexMap.keySet()) {
			combo.add(fieldName);
		}

		combo.addModifyListener(e -> resetButtonState());


		// Text for new value
		Text text = new Text(composite, SWT.BORDER);
		text.setTextLimit(ParticipantVO.MAX_LENGTH_CUSTOM_FIELD);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.addModifyListener(e -> resetButtonState());


		// Button (overwrite)
		Button overwriteCheckBox = new Button(composite, SWT.CHECK);
		overwriteCheckBox.setText(UtilI18N.Overwrite);
		overwriteCheckBox.addListener(SWT.Selection, e -> resetButtonState());


		// Put the three widgets in a list for later collection of all parameters
		customFieldTextList.add(text);
		customFieldComboList.add(combo);
		customFieldOverwriteCheckBoxList.add(overwriteCheckBox);
	}


    private void resetButtonState() {
    	/* At least one Combo must have set a value and either its corresponding Text has a value of its Button
    	 * is checked.
    	 */

    	boolean pageComplete = false;
    	for (int i = 0; i < OFFERED_FIELD_COUNT; i++) {
    		// determine widgets
    		Combo combo = customFieldComboList.get(i);
    		Text text = customFieldTextList.get(i);
    		Button checkBox = customFieldOverwriteCheckBoxList.get(i);

    		// determine values
    		String customField = combo.getText();
    		String value = text.getText();
    		boolean overwrite = checkBox.getSelection();

    		if (customField != null && (overwrite || StringHelper.isNotEmpty(value))) {
    			pageComplete = true;
    			break;
    		}
		}

    	getButton(IDialogConstants.OK_ID).setEnabled(pageComplete);
    }


	@Override
    protected void okPressed() {
		updateValues();

		super.setReturnCode(OK);
		super.close();
	}


	private void updateValues() {
		for (int i = 0; i < OFFERED_FIELD_COUNT; i++) {
			String fieldName = customFieldComboList.get(i).getText();

			if (customField2IndexMap.containsKey(fieldName)) {
				int index = customField2IndexMap.get(fieldName).intValue();
				String value = customFieldTextList.get(i).getText();
				boolean overwrite = customFieldOverwriteCheckBoxList.get(i).getSelection();

				OldCustomFieldUpdateParameter parameter = new OldCustomFieldUpdateParameter(index, value, overwrite);
				parameters.add(parameter);
			}
		}
	}

	/**
	 * List of OldCustomFieldUpdateParameter that represents the final settings the user entered into this dialog.
	 */
	public List<OldCustomFieldUpdateParameter> getCustomFieldParameters() {
		return parameters;
	}

}