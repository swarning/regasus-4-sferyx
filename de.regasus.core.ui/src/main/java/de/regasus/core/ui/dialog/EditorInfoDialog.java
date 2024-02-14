package de.regasus.core.ui.dialog;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;

public class EditorInfoDialog extends Dialog {

	private String title;
	private String[] labels;
	private String[] values;
	private Point size = null;


	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public EditorInfoDialog(
		Shell parentShell,
		String title,
		String[] labels,
		String[] values
	) {
		super(parentShell);
		this.title = title;
		this.labels = labels;
		this.values = values;
	}


	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * @return the size
	 */
	public Point getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Point size) {
		this.size = size;
	}


	@Override
	protected Point getInitialSize() {
		if (size != null) {
			return size;
		}
		else {
			return super.getInitialSize();
		}
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);


		if (labels != null) {
			GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
				.align(SWT.RIGHT, SWT.CENTER);

			GridDataFactory valueGridDataFactory = GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.CENTER)
				.grab(true, false);

			for (int i = 0; i < labels.length; i++) {
				Label label = new Label(container, SWT.RIGHT);
				label.setText(avoidNull(labels[i]) + ":");
				labelGridDataFactory.applyTo(label);

				Text valueText = new Text(container, SWT.NONE);
				valueGridDataFactory.applyTo(valueText);
				SWTHelper.disableTextWidget(valueText);
				if (values != null && values.length > i) {
					valueText.setText( avoidNull(values[i]) );
				}
			}
		}

		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false)
			.span(gridLayout.numColumns, 1)
			.applyTo(separator);

		if (size == null) {
			parent.pack();
		}

		return container;
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(IconRegistry.getImage(IImageKeys.INFORMATION));
		newShell.setText(title);
	}

}
