package de.regasus.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.UtilI18N;


/**
 * A composite containing four buttons labelled with "Top", "Up", "Down" and "Bottom" (with i18n); their pressing
 * notifies an optionally set {@link IUpDownListener}.
 * <p>
 * Used in views showing tables and lists where the user is allowed to change the order of the contained elements, eg in the
 * {@link ProgrammePointsListView}.
 *
 * @author manfred
 *
 */

public class UpDownComposite extends Composite {

	private Button topButton;

	private Button upButton;

	private Button downButton;

	private Button bottomButton;

	protected IUpDownListener upDownListener;


	public UpDownComposite(Composite parent, int style) {
		super(parent, style);

		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		setLayout(fillLayout);

		topButton = new Button(this, SWT.PUSH);
		topButton.setImage(IconRegistry.getImage("icons/top.png"));
		topButton.setToolTipText(UtilI18N.MoveFirst);
		topButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (upDownListener != null) {
					upDownListener.topPressed();
				}
			}
		});

		upButton = new Button(this, SWT.PUSH);
		upButton.setImage(IconRegistry.getImage("icons/up.png"));
		upButton.setToolTipText(UtilI18N.MoveUp);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (upDownListener != null) {
					upDownListener.upPressed();
				}
			}
		});

		downButton = new Button(this, SWT.PUSH);
		downButton.setImage(IconRegistry.getImage("icons/down.png"));
		downButton.setToolTipText(UtilI18N.MoveDown);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (upDownListener != null) {
					upDownListener.downPressed();
				}
			}
		});

		bottomButton = new Button(this, SWT.PUSH);
		bottomButton.setImage(IconRegistry.getImage("icons/bottom.png"));
		bottomButton.setToolTipText(UtilI18N.MoveLast);
		bottomButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (upDownListener != null) {
					upDownListener.bottomPressed();
				}
			}
		});
	}


	public IUpDownListener getUpDownListener() {
		return upDownListener;
	}


	public void setUpDownListener(IUpDownListener upDownListener) {
		this.upDownListener = upDownListener;
	}


	@Override
	public void setEnabled(boolean b) {
		topButton.setEnabled(b);
		upButton.setEnabled(b);
		downButton.setEnabled(b);
		bottomButton.setEnabled(b);
	}


	public void setTopEnabled(boolean b) {
		topButton.setEnabled(b);
	}


	public void setUpEnabled(boolean b) {
		upButton.setEnabled(b);
	}


	public void setDownEnabled(boolean b) {
		downButton.setEnabled(b);
	}


	public void setBottomEnabled(boolean b) {
		bottomButton.setEnabled(b);
	}

}
