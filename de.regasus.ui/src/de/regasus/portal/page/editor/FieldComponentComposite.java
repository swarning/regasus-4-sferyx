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
import org.eclipse.swt.widgets.Composite;
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
import de.regasus.portal.component.Field;
import de.regasus.portal.component.FieldComponent;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;


public abstract class FieldComponentComposite extends Composite {

	public static final String DEFAULT_LANG = "en";


	private static final int COL_COUNT = 2;

	private final boolean expertMode;

	// the entity
	private FieldComponent component;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Portal portal;
	private Long eventPK;
	private List<Language> languageList;

	private Field currentField = null;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;
	private FieldCombo fieldCombo;
	private FieldTypeCombo fieldTypeCombo;

	private Composite i18nCompositeContainer;
	private FieldComponentI18NWidgetController fieldComponentI18NWidgetController;
	private I18NComposite<FieldComponent> i18nComposite;

	private ConditionGroup visibleConditionGroup;
	private ConditionGroup requiredConditionGroup;
	private ConditionGroup readOnlyConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public FieldComponentComposite(
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
		this.eventPK = portal.getEventId();
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);

		createWidgets();
	}


	private void createWidgets() {
		try {
			PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(this, COL_COUNT, modifySupport, portal);

			setLayout( new GridLayout(COL_COUNT, false) );

			widgetBuilder.buildTypeLabel( PortalI18N.FieldComponent.getString() );
			if (expertMode) {
    			htmlIdText = widgetBuilder.buildHtmlId();
    			renderText = widgetBuilder.buildRender();
			}
			buildFieldCombo();
			buildFieldType();
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


	protected abstract FieldCombo buildFieldComboWidget(Composite parent) throws Exception;

	private void buildFieldCombo() throws Exception {
		SWTHelper.createLabel(this, FieldComponent.FIELD_ID.getString(), true);

		fieldCombo = buildFieldComboWidget(this);
		fieldCombo.setEventID(eventPK);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fieldCombo);
		SWTHelper.makeBold(fieldCombo);
		fieldCombo.addModifyListener(modifySupport);
		fieldCombo.addModifyListener(fieldComboListener);
	}


	private void buildFieldType() throws Exception {
		SWTHelper.createLabel(this, FieldComponent.FIELD_TYPE.getString(), true);

		fieldTypeCombo = new FieldTypeCombo(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fieldTypeCombo);
		SWTHelper.makeBold(fieldTypeCombo);
		fieldTypeCombo.addModifyListener(modifySupport);
	}


	private void buildI18NWidgets() {
		i18nCompositeContainer = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(0, 20).applyTo(i18nCompositeContainer);
		i18nCompositeContainer.setLayout( new FillLayout() );
	}


	@Override
	public boolean setFocus() {
		return fieldCombo.setFocus();
	}


	private ModifyListener fieldComboListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			fieldComboListenerChanged();
		}
	};


	private void fieldComboListenerChanged() {
		try {
			syncTypeComboToFieldCombo();
			syncI18nCompositeToFieldCombo();
			syncDefaultsToFieldCombo();

			currentField = fieldCombo.getField();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void syncTypeComboToFieldCombo() {
		Field field = fieldCombo.getField();
		fieldTypeCombo.setField(field);
	}


	private void syncI18nCompositeToFieldCombo() {
		Field field = fieldCombo.getField();
		// field is null if there is nothing selected yet in fieldCombo

		if (fieldComponentI18NWidgetController != null) {
			fieldComponentI18NWidgetController.dispose();
		}
		if (i18nComposite != null) {
			i18nComposite.removeModifyListener(modifySupport);
			i18nComposite.dispose();
		}

		if (field != null) {
			fieldComponentI18NWidgetController = new FieldComponentI18NWidgetController(field);
			i18nComposite = new I18NComposite<>(i18nCompositeContainer, SWT.BORDER, languageList, fieldComponentI18NWidgetController);
			i18nComposite.addModifyListener(modifySupport);
		}

		i18nCompositeContainer.getParent().layout(); // without this the size of the 18nCompositeContainer is not adjusted automatically
		i18nCompositeContainer.layout(); // without this the I18N widgets are not visible sometimes until the size of the editor is changed manually
	}

	// setDefaultsOfFieldToWidgets
	private void syncDefaultsToFieldCombo() {
		Field field = fieldCombo.getField();

		if (i18nComposite != null && !i18nComposite.isDisposed()) {
			i18nComposite.syncEntityToWidgets();

			List<String> languageIds = Language.getPKs(languageList);

    		if (field != null) {
    			// set default label
        		LanguageString currentLabel = component.getLabel();
        		if (   LanguageString.isEmpty(currentLabel) // the current label is empty
        			|| currentField == null // previously there was no Field selected
        			|| areEqual(currentLabel, currentField.getDefaultLabel()) // the current label is the default of the previously selected Field
        		) {
        			// build default label
        			LanguageString label = new LanguageString(field.getDefaultLabel(), languageIds, DEFAULT_LANG);
        			component.setLabel(label);
        		}


    			// set default tooltip
        		LanguageString currentTooltip = component.getTooltip();
        		if (   LanguageString.isEmpty(currentTooltip) // the current tooltip is empty
        			|| currentField == null // previously selected Field
        			|| areEqual(currentTooltip, currentField.getDefaultDescription()) // the current tooltip is the default of the previously selected Field
        		) {
        			LanguageString tooltip = new LanguageString(field.getDefaultDescription(), languageIds, DEFAULT_LANG);
        			component.setTooltip(tooltip);
        		}


    			// set default value of fixedValues
        		LanguageString currentFixedValues = component.getFixedValues();
        		if (   LanguageString.isEmpty(currentFixedValues) // the current value of fixedValues is empty
        			|| currentField == null // previously selected Field
        			|| areEqual(currentFixedValues, currentField.getFixedValues()) // the current value of fixedValues is the default of the previously selected Field
        		) {
        			LanguageString fixedValues = new LanguageString(field.getFixedValues(), languageIds, DEFAULT_LANG);
        			component.setFixedValues(fixedValues);
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

						fieldCombo.setEventID(eventPK);

						fieldCombo.setFieldId( component.getFieldId() );
						// instead of calling fieldComboListenerChanged() we do only adapt typeCombo and i18nComposite
						// (to avoid unnecessary double work)
						syncTypeComboToFieldCombo();
						syncI18nCompositeToFieldCombo();

						fieldTypeCombo.setFieldType( component.getFieldType() );

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

						// remember current field for later
						currentField = fieldCombo.getField();
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
			component.setFieldId( fieldCombo.getFieldId() );
			component.setFieldType( fieldTypeCombo.getFieldType() );
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


	public FieldComponent getComponent() {
		return component;
	}


	public void setComponent(FieldComponent fieldComponent) {
		this.component = fieldComponent;
		syncWidgetsToEntity();
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
    		renderText.setEnabled(!fixedStructure);
		}
		fieldCombo.setEnabled(!fixedStructure);
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
