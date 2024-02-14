package de.regasus.core.ui.editor.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameter.FieldConfigParameter;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

/**
 * Set of widgets to display the values of {@link FieldConfigParameter}.
 * Currently only the parameter visible is implemented.
 * In the future this class will be enhanced to handle the parameters required and default, too.
 * However, both won't be visible for all FieldConfigParameters, so their visibility has to be configurable.
 */
public class FieldConfigWidgets {

	public static final int NUM_COLUMNS = 2;

	// the entity
	private FieldConfigParameter fieldConfig;


	// Widgets
	private Label label;

	private Button visibleCheckbox;


	public FieldConfigWidgets(Composite parent, String labelText) {
		this(parent, labelText, null);
	}


	public FieldConfigWidgets(Composite parent, String labelText, String toolTip) {
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		label.setText(labelText);
		if (toolTip != null) {
			label.setToolTipText(toolTip);
		}

		visibleCheckbox = new Button(parent, SWT.CHECK);
		visibleCheckbox.setText(CoreI18N.FieldConfigWidgets_visible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		ModifyListenerAdapter adapter = new ModifyListenerAdapter(modifyListener);

		// add this as ModifyListener to all widgets
		visibleCheckbox.addSelectionListener(adapter);
	}


	public void syncWidgetsToEntity() {
		if (fieldConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Boolean visible = fieldConfig.isVisible();
						visibleCheckbox.setSelection(visible != null && visible.booleanValue());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (fieldConfig != null) {
			Boolean visible = getVisible();
			fieldConfig.setVisible(visible);
		}
	}


	public void setFieldConfigParameter(FieldConfigParameter fieldConfig) {
		this.fieldConfig = fieldConfig;

		// syncEntityToWidgets() is called from outside
	}


	public boolean getVisible() {
		return visibleCheckbox.getSelection();
	}


	public boolean isEnabled() {
		// visibleTrueCheckbox is used as representative
		return visibleCheckbox.getEnabled();
	}


	public void setEnabled(boolean enabled) {
		visibleCheckbox.setEnabled(enabled);
	}

}
