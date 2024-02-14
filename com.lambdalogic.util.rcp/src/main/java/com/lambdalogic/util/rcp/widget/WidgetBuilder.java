package com.lambdalogic.util.rcp.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;

import de.regasus.core.validation.FieldMetadata;

public class WidgetBuilder<Entity> {

	private GridDataFactory labelGridDataFactory;
	private GridDataFactory textGridDataFactory;
	private GridDataFactory checkboxGridDataFactory;

	private Composite parent;

	private FieldMetadata fieldMetadata;
	private ModifySupport modifySupport;

	private List<WidgetBinder<Entity, ?>> widgetBinderList = new ArrayList<>();


	public WidgetBuilder() {
		labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		textGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		checkboxGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER);
	}


	public WidgetBuilder(Composite parent) {
		this();
		parent(parent);
	}


	public WidgetBuilder(Composite parent, ModifySupport modifySupport) {
		this(parent);
		modifySupport(modifySupport);
	}


	public WidgetBuilder<Entity> copy() {
		WidgetBuilder<Entity> copy = new WidgetBuilder<>();

		copy.labelGridDataFactory = this.labelGridDataFactory.copy();
		copy.textGridDataFactory = this.textGridDataFactory.copy();
		copy.checkboxGridDataFactory = this.checkboxGridDataFactory.copy();

		copy.parent = parent;

		copy.fieldMetadata = fieldMetadata;
		copy.modifySupport = modifySupport;

		copy.widgetBinderList = widgetBinderList;

		return copy;
	}


	public WidgetBuilder<Entity> parent(Composite parent) {
		this.parent = parent;
		return this;
	}


	public GridDataFactory getLabelGridDataFactory() {
		return labelGridDataFactory;
	}


	public GridDataFactory getTextGridDataFactory() {
		return textGridDataFactory;
	}


	public GridDataFactory getCheckboxGridDataFactory() {
		return checkboxGridDataFactory;
	}


	public WidgetBuilder<Entity> fieldMetadata(FieldMetadata fieldMetadata) {
		this.fieldMetadata = fieldMetadata;
		return this;
	}


	public WidgetBuilder<Entity> modifySupport(ModifySupport modifySupport) {
		this.modifySupport = modifySupport;
		return this;
	}


	public Label createLabel() {
		Label label = new Label(parent, SWT.RIGHT);

		// default LayoutData
		if (parent.getLayout() instanceof GridLayout) {
			labelGridDataFactory.applyTo(label);
		}

		// text
		label.setText( determineLabel() );

		// tooltip
		label.setToolTipText( determineToolTipText() );

		// make bold if mandatory and not initialized
		if (fieldMetadata.isNotNull() && !fieldMetadata.isInitialized()) {
			SWTHelper.makeBold(label);
		}

		return label;
	}


	public Label createLabel(String text, String tooltip) {
		Label label = new Label(parent, SWT.RIGHT);

		// default LayoutData
		if (parent.getLayout() instanceof GridLayout) {
			labelGridDataFactory.applyTo(label);
		}

		// text
		if (text != null) {
			label.setText(text);
		}

		// tooltip
		if (tooltip != null) {
			label.setToolTipText(tooltip);
		}

		return label;
	}


	public Label createLabel(String text) {
		return createLabel(text, null);
	}


	public Label createLabel(I18NString text, I18NString tooltip) {
		return createLabel(
			text != null ? text.getString() : null,
			tooltip != null ? tooltip.getString() : null
		);
	}


	public Label createLabel(I18NString text) {
		return createLabel(text, (I18NString) null);
	}


	public void createPlaceholder() {
		new Label(parent, SWT.NONE);
	}


	public Text createText() {
		Text text = createTextWithoutBinding();

		// create WidgetBinder based on reflection
		WidgetBinder<Entity, String> widgetBinder = WidgetBinder.build(text, fieldMetadata);

		widgetBinderList.add(widgetBinder);

		return text;
	}


	/**
	 * Create {@link Text} and bind it to the entity by functional interfaces.
	 *
	 * Example:
	 * widgetBuilder.createText(
	 * 		EasyCheckoutSetup::getPassword,
	 * 		(entity, value) -> entity.setPassword(value)
	 * );
	 *
	 * @param entityGetter
	 * @param entitySetter
	 * @return
	 */
	public Text createText(Function<Entity, String> entityGetter, BiConsumer<Entity, String> entitySetter) {
		Text text = createTextWithoutBinding();

		// create WidgetBinder based on functional interfaces
		WidgetBinder<Entity, String> widgetBinder = WidgetBinder.build(text, entityGetter, entitySetter);

		widgetBinderList.add(widgetBinder);

		return text;
	}


	private Text createTextWithoutBinding() {
		Text text = new Text(parent, SWT.BORDER);

		// default LayoutData
		if (parent.getLayout() instanceof GridLayout) {
			textGridDataFactory.applyTo(text);
		}

		// make bold if mandatory and not initialized
		if (fieldMetadata.isNotNull() && !fieldMetadata.isInitialized()) {
			SWTHelper.makeBold(text);
		}

		// text limit
		text.setTextLimit( fieldMetadata.getMaxLength() );

		// observe widget
		if (modifySupport != null) {
			text.addModifyListener(modifySupport);
		}

		return text;
	}


	public Button createCheckbox(String text, String tooltip) {
		return createButton(SWT.CHECK, text, tooltip);
	}


	public Button createCheckbox(String text) {
		return createButton(SWT.CHECK, text, null);
	}


	public Button createCheckbox() {
		return createButton(SWT.CHECK, null, null);
	}


//	public Button createCheckbox(Function<Entity, Boolean> entityGetter, BiConsumer<Entity, Boolean> entitySetter) {
//		return createButton(SWT.CHECK, entityGetter, entitySetter);
//	}


	public Button createRadio(String text, String tooltip) {
		return createButton(SWT.RADIO, text, tooltip);
	}


	public Button createRadio(String text) {
		return createButton(SWT.RADIO, text, null);
	}


	public Button createRadio() {
		return createButton(SWT.RADIO, null, null);
	}


//	public Button createRadio(Function<Entity, Boolean> entityGetter, BiConsumer<Entity, Boolean> entitySetter) {
//		return createButton(SWT.RADIO, entityGetter, entitySetter);
//	}


	private Button createButton(int style, String text, String tooltip) {
		Button button = createButtonWithoutBinding(style, text, tooltip);

		if (fieldMetadata != null) {
    		// create WidgetBinder based on reflection
    		WidgetBinder<Entity, Boolean> widgetBinder = WidgetBinder.build(button, fieldMetadata);
    		widgetBinderList.add(widgetBinder);
		}

		return button;
	}


//	private Button createButton(int style, Function<Entity, Boolean> entityGetter, BiConsumer<Entity, Boolean> entitySetter) {
//		Button button = createButtonWithoutBinding(style, null, null);
//
//		// create WidgetBinder based on functional interfaces
//		WidgetBinder<Entity, Boolean> widgetBinder = WidgetBinder.build(button, entityGetter, entitySetter);
//
//		widgetBinderList.add(widgetBinder);
//
//		return button;
//	}


	private Button createButtonWithoutBinding(int style, String text, String tooltip) {
		Button button = new Button(parent, style);

		// default LayoutData
		if (parent.getLayout() instanceof GridLayout) {
			checkboxGridDataFactory.applyTo(button);
		}

		if (fieldMetadata != null) {
			// make bold if mandatory and not initialized
			if (fieldMetadata.isNotNull() && !fieldMetadata.isInitialized()) {
				SWTHelper.makeBold(button);
			}
		}


		if (text == null) {
			text = determineLabel();
		}
		button.setText(text);


		if (tooltip == null) {
			tooltip = determineToolTipText();
		}
		button.setToolTipText(tooltip);


		// observe widget
		if (modifySupport != null) {
			button.addSelectionListener(modifySupport);
		}

		return button;
	}


	private String determineLabel() {
		String text = null;

		if (fieldMetadata != null) {
			// determine label from FieldMetadata
			text = fieldMetadata.getLabel();
		}

		if (text != null) {
			text = SWTHelper.prepareLabelText(text);
		}

		return text;
	}


	private String determineToolTipText() {
		String text = null;

		if (fieldMetadata != null) {
			// determine tooltip from FieldMetadata
			text = fieldMetadata.getDescription();
		}

		if (text != null) {
			text = SWTHelper.prepareLabelText(text);
		}

		return text;
	}


	public Text createTextWithLabel() {
		createLabel();
		return createText();
	}


	public Widget createDefaultWithLabel(FieldMetadata fieldMetadata) {
		fieldMetadata(fieldMetadata);

		Class<?> propertyClass = fieldMetadata.getPropertyClass();
		if (propertyClass == boolean.class || propertyClass == Boolean.class) {
			createPlaceholder();
			return createCheckbox();
		}
		else if (propertyClass == String.class) {
			createLabel();
			return createText();
		}
		else {
			throw new RuntimeException("No default for property class " + propertyClass.getName());
		}
	}


	public Label verticalSpace(int verticalSize) {
		return SWTHelper.verticalSpace(parent, verticalSize);
	}


	public Label verticalSpace() {
		return SWTHelper.verticalSpace(parent);
	}


	public Label horizontalLine() {
		return SWTHelper.horizontalLine(parent);
	}


	public void syncWidgetsToEntity(Entity entity) {
		if (entity != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						widgetBinderList.forEach( c -> c.syncWidgetToEntity(entity) );
					}
					catch (Exception e) {
						ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets(Entity entity) {
		if (entity != null) {
			widgetBinderList.forEach( c -> c.syncEntityToWidget(entity) );
		}
	}

}
