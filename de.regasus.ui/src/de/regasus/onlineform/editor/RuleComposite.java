package de.regasus.onlineform.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.regasus.Rule;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IUpDownListener;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.UpDownComposite;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.ui.Activator;


public class RuleComposite extends Group {

	/**
	 * The entity
	 */
	private Rule rule;


	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	/*** widgets ***/

	private Text conditionText;
	private I18NText messageI18NText;


	/**
	 * Button to remove this Composite
	 */

	private Button removeButton;


	private UpDownComposite upDownComposite;


	public RuleComposite(Composite parent, int style) {
		super(parent, style);

		/*** create widgets ***/
		try {
			createPartControl();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Create widgets.
	 * @throws Exception
	 */
	protected void createPartControl() throws Exception {
		setLayout(new GridLayout(4, false));

		// First Row
		new Label(this, SWT.NONE); // Dummy for empty grid cell

		Label conditionLabel = new Label(this, SWT.NONE);
		conditionLabel.setText(OnlineFormI18N.Rule);

		Label messageLabel = new Label(this, SWT.NONE);
		messageLabel.setText(OnlineFormI18N.ViolationMessage);

		upDownComposite = new UpDownComposite(this, SWT.NONE);
		upDownComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 2));

		// remove Button
		removeButton = new Button(this, SWT.PUSH);
		removeButton.setImage(IconRegistry.getImage("icons/delete.png"));
		/* There is no SelectionListener for removeButton here.
		 * RulesTabComposite is observing to removeButton directly,
		 * see addRemoveListener(SelectionListener).
		 * When removebutton is selected, RulesTabComposite will destroy this
		 * RuleComposite.
		 */


		// condition
		conditionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 100;
		conditionText.setLayoutData(gridData);

		conditionText.addModifyListener(modifySupport);


		// message
		messageI18NText = new I18NText(this, SWT.WRAP | SWT.MULTI, LanguageProvider.getInstance());
		messageI18NText.setLayoutData(gridData);

		messageI18NText.addModifyListener(modifySupport);
	}


	// *********************************************************************************************
	// * ModifyListener support
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	// *
	// * ModifyListener support
	// *********************************************************************************************


	/**
	 * Add {@link SelectionListener} that will be notified when an item is removed.
	 * @param selectionListener
	 */
	public void addRemoveListener(SelectionListener selectionListener) {
		removeButton.addSelectionListener(selectionListener);
	}


	public void setUpDownListener(IUpDownListener upDownListener) {
		upDownComposite.setUpDownListener(upDownListener);
	}


	/**
	 * Copy values from entity to widgets.
	 */
	private void syncWidgetsToEntity() {
		if (rule != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						/*** copy values from entity to widgets ***/

						conditionText.setText(StringHelper.avoidNull(rule.getCondition()));
						messageI18NText.setLanguageString(rule.getMessage());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	/**
	 * Copy values from widgets to entity.
	 */
	public void syncEntityToWidgets() {
		if (rule != null) {
			rule.setCondition(conditionText.getText());
			rule.setMessage(messageI18NText.getLanguageString());
		}
	}


	/**
	 * Copy values from widgets to entity and return it.
	 * @return
	 */
	public Rule getRule() {
		syncEntityToWidgets();
		return rule;
	}


	public void setRule(Rule rule) {
		this.rule = rule;
		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
