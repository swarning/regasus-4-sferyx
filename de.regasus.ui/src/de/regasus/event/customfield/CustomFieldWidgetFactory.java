package de.regasus.event.customfield;

import java.util.Collection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.SWTConstants;

public class CustomFieldWidgetFactory {

	public static final int LONG_LABEL_LENGTH = 50;

	public static Group createGroupComposite(
		Composite parent,
		boolean makeColumnsEqualWidth,
		int style,
		String label
	) {
		Group group = new Group(parent, style);
		group.setLayout(new GridLayout(2, makeColumnsEqualWidth));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(label);
		return group;
	}


	/**
	 * Returns a composite containing a (possibly wrapping) label on the left
	 * and a widget suitable for a custom field (like list, choice, text, etc)
	 * on the right.
	 * <p>
	 * This method assumes that the parent has a GridLayout with column count 2.
	 * It was introduced during review of MICRP-1896 to have a common layouting
	 * for both participant and profile custom fields
	 */
	public static AbstractCustomFieldWidget createWithLabel(Composite parent, CustomField field) {

		String labelText;
		if (field.getCustomFieldType() == CustomFieldType.BST) {
			labelText = "";
		}
		else {
			labelText = field.getLabelOrName();
			labelText = StringHelper.stripHtml(labelText);
		}

		// Let the label perform an automatic line wrap within a dynamic width
		// so that the text never extends the visible area
		Label label = new Label(parent, SWT.WRAP);
		{
    		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults();
    		labelGridDataFactory.align(SWT.RIGHT, SWT.TOP);
    		labelGridDataFactory.indent(SWT.NONE, SWTConstants.VERTICAL_INDENT);
    		labelGridDataFactory.exclude( field.isInvisible() );


    		/* If text that is longer than 50 chars, activate the wrapping of the label.
    		 * This is done by setting grabExcessHorizontalSpace of the Label's GridData to true.
    		 * Looks strange, but it works.
    		 */
    		if (labelText.length() > LONG_LABEL_LENGTH) {
    			labelGridDataFactory.grab(true, false);
    		}

    		labelGridDataFactory.applyTo(label);
    		label.setText(labelText);
		}


		// Create the widget for the custom field value according to its type
		AbstractCustomFieldWidget widget = CustomFieldWidgetFactory.create(parent, field);

		// Give the widget to the right a minimum width so that it doesn't disappear when the label text is very wide.
		GridDataFactory widgetGridDataFactory = GridDataFactory.fillDefaults();
		widgetGridDataFactory.grab(true, false);
		widgetGridDataFactory.exclude( field.isInvisible() );
		widgetGridDataFactory.minSize(100, SWT.DEFAULT);
		widgetGridDataFactory.applyTo(widget);

		widget.setEnabled( !field.isReadOnly() );

		label.setToolTipText( StringHelper.stripHtml(widget.getToolTipText()) );

		return widget;
	}


	public static AbstractCustomFieldWidget create(Composite parent, CustomField field) {
		CustomFieldType customFieldType = field.getCustomFieldType();
		switch (customFieldType) {
			case SLT:
				return new SingleLineTextCustomFieldWidget(parent, field);
			case MLT:
				return new MultiLineTextCustomFieldWidget(parent, field);
			case NUM:
				Integer precision = field.getPrecision();
				if (precision != null && precision.intValue() > 0) {
					return new DecimalNumberCustomFieldWidget(parent, field);
				}
				else {
					return new IntegralNumberCustomFieldWidget(parent, field);
				}
			case BST:
			case BSW:
				return new BooleanCustomFieldWidget(parent, field);
			case DAT:
				return new DateCustomFieldWidget(parent, field);
			case DTM:
				return new DateTimeCustomFieldWidget(parent, field);
			case COM:
				return new ComboCustomFieldWidget(parent, field);
			case LIS:
				return new ListCustomFieldWidget(parent, field);
			case CHK:
				return new CheckBoxesCustomFieldWidget(parent, field);
			case RAD:
				return new RadioButtonsCustomFieldWidget(parent, field);
		}
		return null;
	}


	public static boolean containsLongLabel(Collection<? extends CustomField> customFields) {
		for (CustomField customField : customFields) {
			String label = customField.getLabelOrName();
			if (label.length() > CustomFieldWidgetFactory.LONG_LABEL_LENGTH) {
				return true;
			}
		}
		return false;
	}

}
