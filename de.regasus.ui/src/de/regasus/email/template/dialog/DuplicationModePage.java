package de.regasus.email.template.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.email.EmailI18N;

/**
 * A wizard page to let the user choice the destination of a copied Email Template. 
 */
public class DuplicationModePage extends WizardPage implements SelectionListener {

	private static final String NAME = "DuplicateModePage";

	// *************************************************************************
	// * Widgets
	// *

	private Button sameEventButton;

	private Button noEventButton;

	private Button otherEventButton;

	// *************************************************************************
	// * Other Attributes
	// *

	private DuplicationMode duplicationMode = DuplicationMode.SAME_EVENT;

	private boolean templateHasEvent;

	// *************************************************************************
	// * Constructor
	// *

	protected DuplicationModePage(boolean templateHasEvent) {
		super(NAME);

		this.templateHasEvent = templateHasEvent;
	}


	@Override
	public void createControl(Composite parent) {

		setMessage(EmailI18N.DuplicationSettings);

		Composite group = new Composite(parent, SWT.NONE);
		group.setLayout(new GridLayout(3, false));

		// First Button
		sameEventButton = new Button(group, SWT.RADIO);
		sameEventButton.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
		sameEventButton.addSelectionListener(this);
		Label produceClientDispatchLabel = new Label(group, SWT.WRAP);
		produceClientDispatchLabel.setText(EmailI18N.DuplicateToSameEvent);
		produceClientDispatchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, false, 2, 1));



		// Second Button
		otherEventButton = new Button(group, SWT.RADIO);
		otherEventButton.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
		otherEventButton.addSelectionListener(this);

		Label produceServerDispatchServerLabel = new Label(group, SWT.WRAP);
		produceServerDispatchServerLabel.setText(EmailI18N.DuplicateToOtherEvent);
		produceServerDispatchServerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, false, 2, 1));



		// Third Button
		noEventButton = new Button(group, SWT.RADIO);
		noEventButton.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
		noEventButton.addSelectionListener(this);

		Label produceServerDispatchServerLabelScheduled = new Label(group, SWT.WRAP);
		produceServerDispatchServerLabelScheduled.setText(EmailI18N.DuplicateAsEventIndependentTemplate);
		produceServerDispatchServerLabelScheduled
			.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, false, 2, 1));


		setControl(group);

		if (templateHasEvent) {
			sameEventButton.setSelection(true);
		}
		else {
			sameEventButton.setEnabled(false);
			noEventButton.setSelection(true);
		}

		updateState();
	}


	/**
	 * Not called.
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	

	/**
	 * Gets called when one of the buttons is clicked.
	 */
	@Override
	public void widgetSelected(SelectionEvent event) {
		if (!ModifySupport.isDeselectedRadioButton(event)) {
			updateState();
		}
	}
	

	private void updateState() {
		if (sameEventButton.getSelection()) {
			duplicationMode = DuplicationMode.SAME_EVENT;
			setPageComplete(true);
		}
		else if (otherEventButton.getSelection()) {
			duplicationMode = DuplicationMode.OTHER_EVENT;
			setPageComplete(false);
		}
		else if (noEventButton.getSelection()) {
			duplicationMode = DuplicationMode.NO_EVENT;
			setPageComplete(true);
		}
	}
	

	@Override
	public boolean canFlipToNextPage() {
		return duplicationMode == DuplicationMode.OTHER_EVENT;
	}


	public DuplicationMode getDuplicateMode() {
		return duplicationMode;
	}


	public void setDuplicateMode(DuplicationMode duplicationMode) {
		this.duplicationMode = duplicationMode;
	}

}
