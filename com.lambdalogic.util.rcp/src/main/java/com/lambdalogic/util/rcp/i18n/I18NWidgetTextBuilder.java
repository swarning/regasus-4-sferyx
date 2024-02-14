package com.lambdalogic.util.rcp.i18n;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.validation.FieldMetadata;


public class I18NWidgetTextBuilder {

	private static final GridDataFactory topLabelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER);
	private static final GridDataFactory singleLineLabelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
	private static final GridDataFactory multiLineLabelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.TOP).indent(0, SWTConstants.VERTICAL_INDENT);
	private static final GridDataFactory singleLineTextGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
	private static final GridDataFactory multiLineTextGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);


	private Composite parent;
	private FieldMetadata fieldMetadata;
	private String label;
	private String tooltip;
	private ModifyListener modifyListener;
	private boolean multiLine = false;
	private boolean bold = false;


	public I18NWidgetTextBuilder(Composite parent) {
		this.parent = Objects.requireNonNull(parent);
	}


	public I18NWidgetTextBuilder parent(Composite parent) {
		this.parent = parent;
		return this;
	}


	public I18NWidgetTextBuilder modifyListener(ModifyListener modifyListener) {
		this.modifyListener = modifyListener;
		return this;
	}


	public I18NWidgetTextBuilder fieldMetadata(FieldMetadata fieldMetadata) {
		this.fieldMetadata = fieldMetadata;
		this.label = fieldMetadata.getString();
		this.tooltip = fieldMetadata.getDescription();
		return this;
	}


	public I18NWidgetTextBuilder label(String label) {
		this.label = label;
		fieldMetadata = null;
		return this;
	}


	public I18NWidgetTextBuilder label(I18NString label) {
		this.label = label.getString();
		fieldMetadata = null;
		return this;
	}


	public I18NWidgetTextBuilder tooltip(String tooltip) {
		this.tooltip = tooltip;
		fieldMetadata = null;
		return this;
	}


	public I18NWidgetTextBuilder tooltip(I18NString tooltip) {
		this.tooltip = tooltip.getString();
		fieldMetadata = null;
		return this;
	}


	public I18NWidgetTextBuilder multiLine(boolean multiLine) {
		this.multiLine = multiLine;
		return this;
	}


	public I18NWidgetTextBuilder bold(boolean bold) {
		this.bold = bold;
		return this;
	}


	public Text build() {
		Objects.requireNonNull(parent);

		GridLayout layout = (GridLayout) parent.getLayout();
		int numColumn = layout.numColumns;

		// label
		Label labelWidget = new Label(parent, SWT.RIGHT);
		if (numColumn == 1) {
			topLabelGridDataFactory.applyTo(labelWidget);
		}
		else if (multiLine) {
			multiLineLabelGridDataFactory.applyTo(labelWidget);
		}
		else {
			singleLineLabelGridDataFactory.applyTo(labelWidget);
		}

		if (bold) {
			SWTHelper.makeBold(labelWidget);
		}

		if (label != null) {
			labelWidget.setText(label);
		}

		if (tooltip != null) {
			labelWidget.setToolTipText(tooltip);
		}


		// Text
		Text text;
		if (multiLine) {
			MultiLineText multiLineText = new MultiLineText(parent, SWT.BORDER);
			multiLineText.setMinLineCount(2);
			text = multiLineText;
			multiLineTextGridDataFactory.applyTo(text);
		}
		else {
			text = new Text(parent, SWT.BORDER);
			singleLineTextGridDataFactory.applyTo(text);
		}

		if (bold) {
			SWTHelper.makeBold(text);
		}

		if (fieldMetadata != null) {
			text.setTextLimit( fieldMetadata.getMaxLength() );
		}

		if (modifyListener != null) {
			text.addModifyListener(modifyListener);
		}

		text.setData( labelWidget.getText() );

		return text;
	}

}
