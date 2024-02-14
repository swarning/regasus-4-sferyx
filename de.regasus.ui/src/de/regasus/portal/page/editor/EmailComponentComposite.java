package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.EmailComponent;
import de.regasus.portal.component.EmailField;
import de.regasus.portal.component.FieldComponent;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;


public class EmailComponentComposite extends Composite {

	public static final String DEFAULT_LANG = "en";


	private static final int COL_COUNT = 2;

	private final boolean expertMode;

	// the entity
	private EmailComponent component;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Portal portal;
	private List<Language> languageList;

	private EmailField currentField = null;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;
	private EmailFieldCombo emailFieldCombo;

	private Button emailRepetitionRequiredButton;

	private Button duplicateCheckRequiredButton;

	private Composite i18nCompositeContainer;
	private EmailComponentI18NWidgetController emailComponentI18NWidgetController;
	private I18NComposite<EmailComponent> i18nComposite;

	private ConditionGroup visibleConditionGroup;
	private ConditionGroup requiredConditionGroup;
	private ConditionGroup readOnlyConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public EmailComponentComposite(
		Composite parent,
		int style,
		Long portalPK
	)
	throws Exception {
		super(parent, style);

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// load Portal to get Event and Languages
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);

		createWidgets();
	}


	private void createWidgets() {
		try {
			PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(this, COL_COUNT, modifySupport, portal);

			setLayout( new GridLayout(COL_COUNT, false) );

			widgetBuilder.buildTypeLabel( PortalI18N.EmailComponent.getString() );
			if (expertMode) {
    			htmlIdText = widgetBuilder.buildHtmlId();
    			renderText = widgetBuilder.buildRender();
			}
			buildEmailFieldCombo();

			buildButtons();

			buildI18NWidgets();
			visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
			visibleConditionGroup.setDefaultCondition(true);

			requiredConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Required);
			requiredConditionGroup.setDefaultCondition(false);

			readOnlyConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_ReadOnly, true /*showYesIfNotNewButton*/);
			readOnlyConditionGroup.setDefaultCondition(false);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void buildEmailFieldCombo() throws Exception {
		SWTHelper.createLabel(this, FieldComponent.FIELD_ID.getString(), true);

		emailFieldCombo = new EmailFieldCombo(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(emailFieldCombo);
		SWTHelper.makeBold(emailFieldCombo);
		emailFieldCombo.addModifyListener(modifySupport);
		emailFieldCombo.addModifyListener(emailFieldComboListener);
	}


	private void buildButtons() {
		new Label(this, SWT.NONE);

		emailRepetitionRequiredButton = new Button(this, SWT.CHECK);
		emailRepetitionRequiredButton.setText( EmailComponent.EMAIL_REPETITION_REQUIRED.getLabel() );
		emailRepetitionRequiredButton.setToolTipText( EmailComponent.EMAIL_REPETITION_REQUIRED.getDescription() );
		emailRepetitionRequiredButton.addSelectionListener(modifySupport);
		GridDataFactory.fillDefaults().span(COL_COUNT - 1, 1).applyTo(emailRepetitionRequiredButton);

		new Label(this, SWT.NONE);
		duplicateCheckRequiredButton = new Button(this, SWT.CHECK);
		duplicateCheckRequiredButton.setText( EmailComponent.DUPLICATE_CHECK_REQUIRED.getLabel() );
		duplicateCheckRequiredButton.setToolTipText( EmailComponent.DUPLICATE_CHECK_REQUIRED.getDescription() );
		duplicateCheckRequiredButton.addSelectionListener(modifySupport);
		GridDataFactory.fillDefaults().span(COL_COUNT - 1, 1).applyTo(duplicateCheckRequiredButton);
	}


	private void buildI18NWidgets() {
		i18nCompositeContainer = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(0, 20).applyTo(i18nCompositeContainer);
		i18nCompositeContainer.setLayout( new FillLayout() );
	}


	private ModifyListener emailFieldComboListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			emailFieldComboListenerChanged();
		}
	};


	private void emailFieldComboListenerChanged() {
		try {
			syncI18nCompositeToFieldCombo();
			syncDefaultsToEmailFieldCombo();

			currentField = emailFieldCombo.getEmailField();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void syncI18nCompositeToFieldCombo() {
		EmailField field = emailFieldCombo.getEmailField();
		// field is null if there is nothing selected yet in fieldCombo

		if (emailComponentI18NWidgetController != null) {
			emailComponentI18NWidgetController.dispose();
		}
		if (i18nComposite != null) {
			i18nComposite.removeModifyListener(modifySupport);
			i18nComposite.dispose();
		}

		if (field != null) {
			emailComponentI18NWidgetController = new EmailComponentI18NWidgetController();
			i18nComposite = new I18NComposite<>(i18nCompositeContainer, SWT.BORDER, languageList, emailComponentI18NWidgetController);
			i18nComposite.addModifyListener(modifySupport);
		}

		i18nCompositeContainer.getParent().layout(); // without this the size of the 18nCompositeContainer is not adjusted automatically
		i18nCompositeContainer.layout(); // without this the I18N widgets are not visible sometimes until the size of the editor is changed manually
	}

	// setDefaultsOfFieldToWidgets
	private void syncDefaultsToEmailFieldCombo() {
		EmailField emailField = emailFieldCombo.getEmailField();

		if (i18nComposite != null && !i18nComposite.isDisposed()) {
			i18nComposite.syncEntityToWidgets();

			List<String> languageIds = Language.getPKs(languageList);

    		if (emailField != null) {
    			// set default label
        		LanguageString currentLabel = component.getLabel();
        		if (   LanguageString.isEmpty(currentLabel) // the current label is empty
        			|| currentField == null // previously there was no Field selected
        			|| areEqual(currentLabel, currentField.getDefaultLabel()) // the current label is the default of the previously selected Field
        		) {
        			// build default label
        			LanguageString label = new LanguageString(emailField.getDefaultLabel(), languageIds, DEFAULT_LANG);
        			component.setLabel(label);
        		}


    			// set default tooltip
        		LanguageString currentTooltip = component.getTooltip();
        		if (   LanguageString.isEmpty(currentTooltip) // the current tooltip is empty
        			|| currentField == null // previously selected Field
        			|| areEqual(currentTooltip, currentField.getDefaultDescription()) // the current tooltip is the default of the previously selected Field
        		) {
        			LanguageString tooltip = new LanguageString(emailField.getDefaultDescription(), languageIds, DEFAULT_LANG);
        			component.setTooltip(tooltip);
        		}

    			// set default required message
        		LanguageString currentRequiredMessage = component.getRequiredMessage();
        		if (   LanguageString.isEmpty(currentRequiredMessage) ) {
        			LanguageString requiredMessage = new LanguageString(EmailComponent.REQUIRED_MESSAGE.getDefault(), languageIds, DEFAULT_LANG);
        			component.setRequiredMessage(requiredMessage);
        		}

    			// set default invalid message
        		LanguageString currentInvalidMessage = component.getInvalidMessage();
        		if (   LanguageString.isEmpty(currentInvalidMessage) ) {
        			LanguageString invalidMessage = new LanguageString(EmailComponent.INVALID_MESSAGE.getDefault(), languageIds, DEFAULT_LANG);
        			component.setInvalidMessage(invalidMessage);
        		}

    			// set default email repetition label
        		LanguageString currentEmailRepetitionLabel = component.getEmailRepetitionLabel();
        		if (   LanguageString.isEmpty(currentEmailRepetitionLabel) ) {
        			LanguageString emailRepetitionLabel = new LanguageString(EmailComponent.EMAIL_REPETITION_LABEL.getDefault(), languageIds, DEFAULT_LANG);
        			component.setEmailRepetitionLabel(emailRepetitionLabel);
        		}

    			// set default email repetition not match message
        		LanguageString currentEmailRepetitionNotMatchMessage = component.getEmailRepetitionNotMatchMessage();
        		if (   LanguageString.isEmpty(currentEmailRepetitionNotMatchMessage) ) {
        			LanguageString emailRepetitionNotMatchMessage =
        				new LanguageString(EmailComponent.EMAIL_REPETITION_NOT_MATCH_MESSAGE.getDefault(), languageIds, DEFAULT_LANG);
        			component.setEmailRepetitionNotMatchMessage(emailRepetitionNotMatchMessage);
        		}

    			// set default duplicate message
        		LanguageString currentDuplicateMessage = component.getDuplicateMessage();
        		if (   LanguageString.isEmpty(currentDuplicateMessage) ) {
        			LanguageString duplicateMessage = new LanguageString(EmailComponent.DUPLICATE_MESSAGE.getDefault(), languageIds, DEFAULT_LANG);
        			component.setDuplicateMessage(duplicateMessage);
        		}
    		}

			i18nComposite.setEntity(component);
		}
	}


	private boolean areEqual(LanguageString widgetValue, I18NString fieldValue) {
		boolean result = true;

		if (widgetValue != null) {
			Collection<String> languageCodes = widgetValue.getLanguageCodes();
			for (String lang : languageCodes) {
				String widgetStr = widgetValue.getString(lang);
				String fieldStr = fieldValue.getString(lang);
				result = widgetStr.equals(fieldStr);
				if ( ! result) {
					break;
				}
			}
		}

		return result;
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						if (expertMode) {
    						htmlIdText.setText( avoidNull(component.getHtmlId()) );
    						renderText.setText( avoidNull(component.getRender()) );
						}


						emailFieldCombo.setEmailField( component.getEmailField() );
						emailFieldComboListenerChanged();

						emailRepetitionRequiredButton.setSelection(component.isEmailRepetitionRequired());
						duplicateCheckRequiredButton.setSelection(component.isDuplicateCheckRequired());

						/* the i18nComposite can be disposed when syncI18nCompositeToFieldCombo() is called above.
						 * In the case we cannot call i18nComposite.setEntity(component);
						 * The entity should be set later to i18nComposite when fieldComboListenerChanged() is called.
						 */
						if (i18nComposite != null && !i18nComposite.isDisposed()) {
							i18nComposite.setEntity(component);
						}

						visibleConditionGroup.setCondition( component.getVisibleCondition() );
						visibleConditionGroup.setDescription( component.getVisibleConditionDescription() );

						requiredConditionGroup.setCondition( component.getRequiredCondition() );
						requiredConditionGroup.setDescription( component.getRequiredConditionDescription() );

						readOnlyConditionGroup.setCondition( component.getReadOnlyCondition() );
						readOnlyConditionGroup.setDescription( component.getReadOnlyConditionDescription() );
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


	public void syncEntityToWidgets() {
		if (component != null) {
			if (expertMode) {
    			component.setHtmlId( htmlIdText.getText() );
    			component.setRender( renderText.getText() );
			}

			component.setEmailField( emailFieldCombo.getEmailField() );

			component.setEmailRepetitionRequired( emailRepetitionRequiredButton.getSelection() );
			component.setDuplicateCheckRequired( duplicateCheckRequiredButton.getSelection() );

			if (i18nComposite != null && !i18nComposite.isDisposed()) {
				i18nComposite.syncEntityToWidgets();
			}

			component.setVisibleCondition( visibleConditionGroup.getCondition() );
			component.setVisibleConditionDescription( visibleConditionGroup.getDescription() );

			component.setRequiredCondition( requiredConditionGroup.getCondition() );
			component.setRequiredConditionDescription( requiredConditionGroup.getDescription() );

			component.setReadOnlyCondition( readOnlyConditionGroup.getCondition() );
			component.setReadOnlyConditionDescription( readOnlyConditionGroup.getDescription() );
		}
	}


	public EmailComponent getComponent() {
		return component;
	}


	public void setComponent(EmailComponent emailComponent) {
		this.component = emailComponent;
		syncWidgetsToEntity();
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
    		renderText.setEnabled(!fixedStructure);
		}
		emailFieldCombo.setEnabled(!fixedStructure);
		emailRepetitionRequiredButton.setEnabled(!fixedStructure);
		duplicateCheckRequiredButton.setEnabled(!fixedStructure);
		requiredConditionGroup.setEnabled(!fixedStructure);
		readOnlyConditionGroup.setEnabled(!fixedStructure);
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

}
