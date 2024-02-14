package de.regasus.common.customfield;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.CustomFieldConfigParameterSet;
import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldListValue;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.ui.Activator;

public class CustomFieldComposite<ListValueType extends CustomFieldListValue> extends Composite {

	// the entity
	private CustomField customField;

	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private Text name;
	private MultiLineText description;
	private Button required;
	private Button requiredWeb;
	private Button invisible;
	private Button readOnly;
	private I18NMultiText i18nMultiText;
	private CustomFieldTypeCombo typeCombo;
	private NullableSpinner minSpinner;
	private NullableSpinner maxSpinner;
	private NullableSpinner precisionSpinner;
	private CustomFieldListValueListComposite<ListValueType> customFieldListValueListComposite;

	private Collection<String> defaultLanguagePKs;


	public CustomFieldComposite(
		Composite parent,
		int style,
		CustomFieldConfigParameterSet customFieldConfigParameterSet,
		Collection<String> defaultLanguagePKs
	) {
		super(parent, style);

		this.defaultLanguagePKs = defaultLanguagePKs;

		try {
			setLayout(new GridLayout(6, false));

			// name
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(ParticipantLabel.CustomField_Name.getString());
				label.setToolTipText(ParticipantLabel.CustomField_Name_Desc.getString());
				SWTHelper.makeBold(label);
			}
			{
				name = new Text(this, SWT.BORDER);
				name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
				SWTHelper.makeBold(name);

				name.addModifyListener(modifySupport);
			}


			// description
			{
				Label label = new Label(this, SWT.NONE);
				label.setText(ParticipantLabel.CustomField_Description.getString());
				label.setToolTipText(ParticipantLabel.CustomField_Description_Desc.getString());

				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				label.setLayoutData(gridData);
			}
			{
				description = new MultiLineText(this, SWT.BORDER);
				description.setMinLineCount(5);
				description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

				description.addModifyListener(modifySupport);
			}

			// required
			{
				// dummy
				new Label(this, SWT.NONE);
			}
			{
				required = new Button(this, SWT.CHECK);
				required.setText(ParticipantLabel.CustomField_Required.getString());
				required.setToolTipText(ParticipantLabel.CustomField_Required_Desc.getString());

				required.addSelectionListener(modifySupport);
			}
			{
				requiredWeb = new Button(this, SWT.CHECK);
				requiredWeb.setText(ParticipantLabel.CustomField_RequiredWeb.getString());
				requiredWeb.setToolTipText(ParticipantLabel.CustomField_RequiredWeb_Desc.getString());

				requiredWeb.addSelectionListener(modifySupport);
			}
			{
				// dummy
				Label dummy = new Label(this, SWT.NONE);
				dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			}

			// invisible
			{
				// dummy
				new Label(this, SWT.NONE);
			}
			{
				invisible = new Button(this, SWT.CHECK);
				invisible.setText(ParticipantLabel.CustomField_Invisible.getString());
				invisible.setToolTipText(ParticipantLabel.CustomField_Invisible_Desc.getString());

				invisible.addSelectionListener(modifySupport);
			}

			{
				readOnly = new Button(this, SWT.CHECK);
				readOnly.setText(ParticipantLabel.CustomField_ReadOnly.getString());
				readOnly.setToolTipText(ParticipantLabel.CustomField_ReadOnly_Desc.getString());

				readOnly.addSelectionListener(modifySupport);
			}

			{
				// dummy
				Label dummy = new Label(this, SWT.NONE);
				dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			}

			// Label & ToolTip
			{
				String[] labels = {
					ParticipantLabel.CustomField_Label.getString(),
					ParticipantLabel.CustomField_ToolTip.getString()
				};

				String[] toolTips = {
					ParticipantLabel.CustomField_Label_Desc.getString(),
					ParticipantLabel.CustomField_ToolTip_Desc.getString()
				};


				i18nMultiText = new I18NMultiText(
					this,
					SWT.NONE,
					labels,
					new boolean[] {false, true},	// multiline
					new boolean[] {false, false},	// required
					LanguageProvider.getInstance()
				);
				i18nMultiText.setToolTips(toolTips);
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1);
				gridData.heightHint = 150;
				i18nMultiText.setLayoutData(gridData);

				i18nMultiText.addModifyListener(modifySupport);
			}

			// Type
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(ParticipantLabel.CustomField_Type.getString());
				label.setToolTipText(ParticipantLabel.CustomField_Type_Desc.getString());
			}
			{
				typeCombo = new CustomFieldTypeCombo(this, SWT.NONE);
				typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));

				// Set type to SLT and disable type selection if not all widgets are allowed.
				if ( ! customFieldConfigParameterSet.getAllTypes().isVisible()) {
					typeCombo.setEntity(CustomFieldType.SLT);
					typeCombo.setEnabled(false);
				}

				typeCombo.addModifyListener(modifySupport);

				typeCombo.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						updateListValueEnabledState();
					}
				});
			}

			// Min, Max, Precision
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(ParticipantLabel.CustomField_Min.getString());
				label.setToolTipText(ParticipantLabel.CustomField_Min_Desc.getString());
			}
			{
				minSpinner = new NullableSpinner(this, SWT.NONE);
				minSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				// calculate and set the maximum width
				WidgetSizer.setWidth(minSpinner);

				minSpinner.addModifyListener(modifySupport);
			}
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(ParticipantLabel.CustomField_Max.getString());
				label.setToolTipText(ParticipantLabel.CustomField_Max_Desc.getString());
			}
			{
				maxSpinner = new NullableSpinner(this, SWT.NONE);
				maxSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				// calculate and set the maximum width
				WidgetSizer.setWidth(maxSpinner);

				maxSpinner.addModifyListener(modifySupport);
			}
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(ParticipantLabel.CustomField_Precision.getString());
				label.setToolTipText(ParticipantLabel.CustomField_Precision_Desc.getString());
			}
			{
				precisionSpinner = new NullableSpinner(this, SWT.NONE);
				precisionSpinner.setMinimumAndMaximum(CustomField.MIN_PRECISION, CustomField.MAX_PRECISION);
				precisionSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				// calculate and set the maximum width
				WidgetSizer.setWidth(precisionSpinner);

				precisionSpinner.addModifyListener(modifySupport);
			}

			{
				customFieldListValueListComposite = new CustomFieldListValueListComposite<>(this, SWT.NONE);
				customFieldListValueListComposite.setLayoutData(
					GridDataFactory.fillDefaults().grab(true, true).span(6, 1).hint(SWT.DEFAULT, 150).create()
				);
				customFieldListValueListComposite.setText(I18N.CustomFieldComposite_ValueList);

				customFieldListValueListComposite.setDefaultLanguages(defaultLanguagePKs);
				customFieldListValueListComposite.addModifyListener(modifySupport);
			}

			// handle modification of the typeCombo, must be before setEntity()
			typeCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					handleTypeModified();
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setCustomFieldListValueFactory(ICustomFieldListValueFactory<ListValueType> customFieldListValueFactory) {
		customFieldListValueListComposite.setCustomFieldListValueFactory(customFieldListValueFactory);
	}


	/**
	 * The group of list values should only be usable when the type supports list values
	 * (like RAD, COM, LST and CHK).
	 */
	protected void updateListValueEnabledState() {
		CustomFieldType customFieldType = typeCombo.getEntity();
		customFieldListValueListComposite.setEnabled(customFieldType.isWithListValues());
	}


	private void syncWidgetsToEntity() {
		if (customField != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				@SuppressWarnings("unchecked")
				public void run() {
					try {
						modifySupport.setEnabled(false);

						name.setText(StringHelper.avoidNull(customField.getName()));
						description.setText(StringHelper.avoidNull(customField.getDescription()));

//						EventVO eventVO = EventModel.getInstance().getEventVO(programmePointVO.getEventPK());

						Map<String, LanguageString> labelToLanguageMap = new HashMap<>();
						labelToLanguageMap.put(ParticipantLabel.CustomField_Label.getString(),		customField.getLabel());
						labelToLanguageMap.put(ParticipantLabel.CustomField_ToolTip.getString(),	customField.getToolTip());

						i18nMultiText.setLanguageString(labelToLanguageMap, defaultLanguagePKs);


						required.setSelection(customField.isRequired());
						requiredWeb.setSelection(customField.isRequiredWeb());
						invisible.setSelection(customField.isInvisible());
						readOnly.setSelection(customField.isReadOnly());

						typeCombo.setEntity(customField.getCustomFieldType());
						// handle modification of typeCombo, because it is not called automatically when the value is set directly
						handleTypeModified();

						minSpinner.setValue(customField.getMin());
						maxSpinner.setValue(customField.getMax());
						precisionSpinner.setValue(customField.getPrecision());
						customFieldListValueListComposite.setCustomFieldListValueList(
							(List<ListValueType>) customField.getCustomFieldListValues()
						);

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
		if (customField != null) {
			customField.setID(customField.getID());
			customField.setName(name.getText());
			customField.setDescription(description.getText());
			customField.setRequired(required.getSelection());
			customField.setRequiredWeb(requiredWeb.getSelection());
			customField.setInvisible(invisible.getSelection());
			customField.setReadOnly(readOnly.getSelection());
			customField.setLabel(i18nMultiText.getLanguageString(ParticipantLabel.CustomField_Label.getString()));
			customField.setToolTip(i18nMultiText.getLanguageString(ParticipantLabel.CustomField_ToolTip.getString()));
			customField.setCustomFieldType(typeCombo.getEntity());
			customField.setMin(minSpinner.getValueAsInteger());
			customField.setMax(maxSpinner.getValueAsInteger());
			customField.setPrecision(precisionSpinner.getValueAsInteger());
			customField.setCustomFieldListValues(customFieldListValueListComposite.getCustomFieldListValues());
		}
	}


	public void setCustomField(CustomField customField) {
		this.customField = customField;
		syncWidgetsToEntity();
	}


	private void handleTypeModified() {
		CustomFieldType type = typeCombo.getEntity();

		// get minimum and maximum values for min and max depending on the CustomFieldType
		int minAllowedMin = ParticipantCustomField.getMinMinNumber(type);
		int maxAllowedMin = ParticipantCustomField.getMaxMinNumber(type);
		int minAllowedMax = ParticipantCustomField.getMinMaxNumber(type);
		int maxAllowedMax = ParticipantCustomField.getMaxMaxNumber(type);

		System.out.println(
			"type=" + type +
			", minAllowedMin=" + minAllowedMin +
			", maxAllowedMin=" + maxAllowedMin +
			", minAllowedMax=" + minAllowedMax +
			", maxAllowedMax=" + maxAllowedMax
		);


		/* Correct min and max if they are not in the range of allowed values depending on the
		 * CustomFieldType.
		 * When type changes, the previous values of min and max might not be allowed anymore.
		 * So change them to be within their allowed interval.
		 */
		Integer currentMin = customField.getMin();
		if (currentMin != null && currentMin < minAllowedMin) {
			System.out.println(
				"currentMin " + currentMin +
				" < minAllowedMin " + minAllowedMin +
				", increasing min to " + minAllowedMin
			);
			currentMin = minAllowedMin;
			customField.setMin(minAllowedMin);
		}
		else if (currentMin != null && currentMin > maxAllowedMin) {
			System.out.println(
				"currentMin " + currentMin +
				" > maxAllowedMin " + maxAllowedMin +
				", decreasing min to " + maxAllowedMin
			);
			currentMin = maxAllowedMin;
			customField.setMin(currentMin);
		}

		Integer currentMax = customField.getMax();
		if (currentMax != null && currentMax < minAllowedMax) {
			System.out.println(
				"currentMax " + currentMax +
				" < minAllowedMax " + minAllowedMax +
				", increasing max to " + minAllowedMax
			);
			currentMax = minAllowedMax;
			customField.setMax(currentMax);
		}
		else if (currentMax != null && currentMax > maxAllowedMax) {
			System.out.println(
				"currentMax " + currentMax +
				" > maxAllowedMax " + maxAllowedMax +
				", decreasing max to " + maxAllowedMax
			);
			currentMax = maxAllowedMax;
			customField.setMax(maxAllowedMax);
		}


		// configure minSpinner
		minSpinner.setMinimumAndMaximum(minAllowedMin, maxAllowedMin);
		minSpinner.setValue(currentMin);
		minSpinner.setEnabled(ParticipantCustomField.isMinEnabled(type));

		// configure maxSpinner
		maxSpinner.setMinimumAndMaximum(minAllowedMax, maxAllowedMax);
		maxSpinner.setValue(currentMax);
		maxSpinner.setEnabled(ParticipantCustomField.isMaxEnabled(type));
		precisionSpinner.setEnabled(ParticipantCustomField.isPrecisionEnabled(type));


		// configure required
		boolean requiredEnabled = ParticipantCustomField.isRequiredEnabled(type);
		if (! requiredEnabled) {
			// Set required to false if the CustomFieldType does not allow required fields
			required.setSelection(false);
			requiredWeb.setSelection(false);
		}
		required.setEnabled(requiredEnabled);
		requiredWeb.setEnabled(requiredEnabled);
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
