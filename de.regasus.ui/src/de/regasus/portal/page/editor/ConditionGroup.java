package de.regasus.portal.page.editor;


import static com.lambdalogic.util.StringHelper.*;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalConstants;
import de.regasus.portal.Section;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;

public class ConditionGroup extends Group {

	private boolean advancedAccess = false;

	private ModifySupport modifySupport = new ModifySupport(this);

	private boolean defaultCondition = true;

	private boolean showYesIfNotNewButton;

	private String condition;

	// widgets
	private Composite buttonComposite;
	private Composite textComposite;

	private Button yesButton;
	private Button yesIfNotNewButton;
	private Button noButton;
	private Button expertExpressionButton;
	private MultiLineText descriptionText;
	private Label conditionLabel;
	private MultiLineText conditionText;


	public ConditionGroup(Composite parent, int style, boolean showYesIfNotNewButton, Portal portal) {
		super(parent, style);

		this.showYesIfNotNewButton = showYesIfNotNewButton;

		/* Determine if the user has advanced access to the ScriptComponent
		 * The condition is: isAdmin || (advancedAccess && isSpecialConditionsAvailable)
		 * To improve performance, we evaluate the ConfigParameterSet at the end and only if necessary.
		 */
		try {
			advancedAccess = CurrentUserModel.getInstance().isAdmin();
			if (!advancedAccess) {
				advancedAccess =
					   CurrentUserModel.getInstance().isPortalExpert()
					&& isSpecialConditionsAvailable(portal);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		createWidgets();
	}


	private boolean isSpecialConditionsAvailable(Portal portal) throws Exception {
		Long eventId = portal.getEventId();

		boolean specialConditionsAvailable = false;
		ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
		if (eventId == null) {
			specialConditionsAvailable = configParameterSet.getPortal().isSpecialConditions();
		}
		else {
			specialConditionsAvailable = configParameterSet.getEvent().getPortal().isSpecialConditions();
		}

		return specialConditionsAvailable;
	}


	private void createWidgets() {
		setLayout( new GridLayout(2, false) );

		Composite buttonComposite = buildButtonComposite();
		GridDataFactory.fillDefaults().applyTo(buttonComposite);

		Composite textComposite = buildTextComposite();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textComposite);

		updateStatus();
	}


	private Composite buildButtonComposite() {
		buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayout( new RowLayout(SWT.VERTICAL) );

		yesButton = new Button(buttonComposite, SWT.RADIO);
		yesButton.setText(UtilI18N.Yes);
		yesButton.addSelectionListener(yesButtonListener);

		if (showYesIfNotNewButton) {
    		yesIfNotNewButton = new Button(buttonComposite, SWT.RADIO);
    		yesIfNotNewButton.setText(I18N.YesIfNotNew);
    		yesIfNotNewButton.setToolTipText(I18N.YesIfNotNew_Desc);
    		yesIfNotNewButton.addSelectionListener(yesIfNotNewButtonListener);
		}

		noButton = new Button(buttonComposite, SWT.RADIO);
		noButton.setText(UtilI18N.No);
		noButton.addSelectionListener(noButtonListener);

		expertExpressionButton = new Button(buttonComposite, SWT.RADIO);
		expertExpressionButton.setText(I18N.SpecialCondition);
		expertExpressionButton.setEnabled(advancedAccess);
		expertExpressionButton.addSelectionListener(expertExpressionButtonListener);

		return buttonComposite;
	}


	private SelectionListener yesButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// it is important to ignore events if the button gets un-selected
			if ( yesButton.getSelection() ) {
				setCondition(PortalConstants.TRUE_CONDITION);
			}
		}
	};


	private SelectionListener yesIfNotNewButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// it is important to ignore events if the button gets un-selected
			if ( yesIfNotNewButton != null && yesIfNotNewButton.getSelection() ) {
				setCondition(PortalConstants.TRUE_IF_NOT_NEW_CONDITION);
			}
		}
	};


	private SelectionListener noButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// it is important to ignore events if the button gets un-selected
			if ( noButton.getSelection() ) {
    			setCondition(PortalConstants.FALSE_CONDITION);
			}
		}
	};


	private SelectionListener expertExpressionButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// it is important to ignore events if the button gets un-selected
			if ( expertExpressionButton.getSelection() ) {
    			updateStatus();
			}
		}
	};

	private Composite buildTextComposite() {
		textComposite = new Composite(this, SWT.NONE);
		textComposite.setLayout( new GridLayout(1, false) );

		final GridDataFactory LABEL_GRID_DATA_FACTORY = GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER);
		final GridDataFactory TEXT_GRID_DATA_FACTORY = GridDataFactory.fillDefaults().grab(true, true);

		Label descriptionLabel = new Label(textComposite, SWT.NONE);
		descriptionLabel.setText(UtilI18N.Description);
		LABEL_GRID_DATA_FACTORY.applyTo(descriptionLabel);

		descriptionText = new MultiLineText(textComposite, SWT.BORDER);
		TEXT_GRID_DATA_FACTORY.copy().grab(true, false).applyTo(descriptionText);
		descriptionText.setMinLineCount(1);
		descriptionText.setTextLimit( Section.VISIBLE_CONDITION_DESCRIPTION.getMaxLength() );
		descriptionText.addModifyListener(modifySupport);

		if (advancedAccess) {
			conditionLabel = new Label(textComposite, SWT.NONE);
			conditionLabel.setText(I18N.PageEditor_Condition);
			LABEL_GRID_DATA_FACTORY.applyTo(conditionLabel);

			conditionText = new MultiLineText(textComposite, SWT.BORDER);
			TEXT_GRID_DATA_FACTORY.applyTo(conditionText);
			conditionText.setTextLimit( Section.VISIBLE_CONDITION.getMaxLength() );
			conditionText.addModifyListener(modifySupport);
		}

		return textComposite;
	}


	private void hideConditionWidgets() {
		if (advancedAccess) {
    		conditionLabel.setVisible(false);
    		conditionText.setVisible(false);

    		((GridData) conditionLabel.getLayoutData()).exclude = true;
    		((GridData) conditionText.getLayoutData()).exclude = true;

    		SWTHelper.recursiveLayout(this);
		}
	}


	private void showConditionWidgets() {
		if (advancedAccess) {
    		conditionLabel.setVisible(true);
    		conditionText.setVisible(true);

    		((GridData) conditionLabel.getLayoutData()).exclude = false;
    		((GridData) conditionText.getLayoutData()).exclude = false;

    		SWTHelper.recursiveLayout(this);
		}
	}


	private void updateStatus() {
		boolean expertExpressionSelected = expertExpressionButton.getSelection();


		// show/hide text widgets

		// show text widgets (descriptionText and conditionText) only if the expert condition is selected
		textComposite.setVisible(expertExpressionSelected);

		// in addition build/destroy condition widgets depending on expertExpressionButton
		if (expertExpressionSelected) {
			showConditionWidgets();
		}
		else {
			hideConditionWidgets();
		}



		// enable/disable text widgets
		if (expertExpressionSelected) {
			// conditionText does only exist in expert mode, otherwise it is null
			if (advancedAccess) {
				SWTHelper.enableTextWidget(descriptionText, true);
				SWTHelper.enableTextWidget(conditionText);
			}
			else {
				SWTHelper.enableTextWidget(descriptionText, false);
			}
		}
		// if expertExpressionSelected == false we don't have to care about the text widgets, cause they are not visible


		// enable/disable buttons

		// Disable condition widgets for non-portal-experts if the condition is a special one.
		boolean enableButtons = advancedAccess || !expertExpressionSelected;
		yesButton.setEnabled(enableButtons);
		if (yesIfNotNewButton != null) {
			yesIfNotNewButton.setEnabled(enableButtons);
		}
		noButton.setEnabled(enableButtons);

		expertExpressionButton.setEnabled(advancedAccess);
	}


	public String getDescription() {
		return trim( descriptionText.getText() );
	}


	public void setDescription(String description) {
		if (description == null) {
			// set default description if condition is yes or no
			if ( isTrueCondition() ) {
				description = UtilI18N.Yes;
			}
			else if ( isFalseCondition() ) {
				description = UtilI18N.No;
			}

		}

		descriptionText.setText( avoidNull(description) );
	}


	public String getCondition() {
		if (advancedAccess) {
			condition = conditionText.getText();
			condition = trim(condition);
		}

		return condition;
	}


	public void setCondition(String condition) {
		this.condition = condition;

		if (advancedAccess) {
			conditionText.setText( avoidNull(condition) );
		}

		boolean trueCondition = isTrueCondition();
		boolean trueIfNotNewCondition = isTrueIfNotNewCondition();
		boolean falseCondition = isFalseCondition();

		if (trueCondition) {
			descriptionText.setText(UtilI18N.Yes);
		}
		else if (trueIfNotNewCondition) {
			descriptionText.setText(I18N.YesIfNotNew_Desc);
		}
		else {
			if (falseCondition) {
				descriptionText.setText(UtilI18N.No);
			}
		}

		yesButton.setSelection(trueCondition);
		if (yesIfNotNewButton != null) {
			yesIfNotNewButton.setSelection(trueIfNotNewCondition);
		}
		noButton.setSelection(falseCondition);
		expertExpressionButton.setSelection(!trueCondition && !trueIfNotNewCondition && !falseCondition);

		updateStatus();
	}


	public boolean getDefaultCondition() {
		return defaultCondition;
	}


	public void setDefaultCondition(boolean defaultCondition) {
		this.defaultCondition = defaultCondition;

		if (getCondition() == null) {
			if (defaultCondition == true) {
				setCondition(PortalConstants.TRUE_CONDITION);
			}
			else if (defaultCondition == false) {
				setCondition(PortalConstants.FALSE_CONDITION);
			}
		}
	}


	private boolean isTrueCondition() {
		if ( isEmpty(condition) ) {
			return defaultCondition;
		}
		else {
			return PortalConstants.TRUE_CONDITION.equals(condition);
		}
	}


	private boolean isTrueIfNotNewCondition() {
		// the condition is false if the corresponding Button does not exist
		if ( yesIfNotNewButton == null || isEmpty(condition) ) {
			return false;
		}
		else {
			return PortalConstants.TRUE_IF_NOT_NEW_CONDITION.equals(condition);
		}
	}


	private boolean isFalseCondition() {
		if ( isEmpty(condition) ) {
			return ! defaultCondition;
    	}
    	else {
    		return PortalConstants.FALSE_CONDITION.equals(condition);
    	}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
