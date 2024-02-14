package de.regasus.core.ui.statusline;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;

public class CountHandlesStatusLineContribution extends ControlContribution {


	private static final int LIMIT_HIGH = 7500;
	private static final int LIMIT_CRITICAL = 8800;

	private static CountHandlesStatusLineContribution instance;

	public static CountHandlesStatusLineContribution getInstance() {
		if (instance == null) {
			instance = new CountHandlesStatusLineContribution();
		}
		return instance;
	}

	private CountHandlesStatusLineContribution() {
		super("HandleCountStatusLineContribution");
	}

	private int UPDATE_INTERVAL = 3 * 1000; // Every 3 seconds
	private int count = 0;

	private int previousCount;

	private Label label;

    private final Runnable timer = new Runnable() {
        @Override
		public void run() {
            if (!label.isDisposed()) {
            	updateCountAndLabel();
            	label.getDisplay().timerExec(UPDATE_INTERVAL, this);
            }
        }
    };



	@Override
	protected Control createControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout());

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		updateCountAndLabel();

		StatusLineLayoutData layoutData = new StatusLineLayoutData();
		layoutData.widthHint = 150;
		composite.setLayoutData(layoutData);

		return composite;
	}



	private void updateLabel() {
		String text = String.valueOf(count) + " " + CoreI18N.Widgets;
		label.setText(text);

		Display display = Display.getCurrent();

		if (count < LIMIT_HIGH) {
			label.setForeground( display.getSystemColor(SWT.COLOR_DARK_GREEN) );
			label.setToolTipText(CoreI18N.WidgetsCountOK);
		}
		else if (count < LIMIT_CRITICAL) {
			label.setForeground( display.getSystemColor(SWT.COLOR_DARK_YELLOW) );
			label.setToolTipText(CoreI18N.WidgetsCountHigh);
			if (previousCount < LIMIT_HIGH) {
				MessageDialog.openInformation(label.getShell(), UtilI18N.Info, CoreI18N.WidgetsCountHigh);
			}
		}
		else {
			label.setForeground( display.getSystemColor(SWT.COLOR_DARK_RED) );
			label.setToolTipText(CoreI18N.WidgetsCountCritical);
			if (previousCount < LIMIT_CRITICAL) {
				MessageDialog.openWarning(label.getShell(), UtilI18N.Warning, CoreI18N.WidgetsCountCritical);
			}
		}

		previousCount = count;
	}


	public void updateCountAndLabel() {
		updateCount();
		updateLabel();
	}


	private void updateCount() {
		count = 0;

		computeWidgetCountRecursively(label.getShell());


		label.getDisplay().timerExec(UPDATE_INTERVAL, timer);
	}


	private void computeWidgetCountRecursively(Composite composite) {
		Control[] controls = composite.getChildren();
		if (controls != null) {
			count += controls.length;
		}
		for (Control control : controls) {
			if (control instanceof Composite) {
				Composite childComposite = (Composite) control;
				computeWidgetCountRecursively(childComposite);
			}
		}
	}

}
