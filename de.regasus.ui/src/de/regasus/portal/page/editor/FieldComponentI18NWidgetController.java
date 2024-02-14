package de.regasus.portal.page.editor;

import static com.lambdalogic.util.CollectionsHelper.*;
import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.Ostermiller.util.CSVParser;
import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.csv.CsvLineBuilder;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.component.Field;
import de.regasus.portal.component.FieldComponent;
import de.regasus.portal.component.FieldType;

public class FieldComponentI18NWidgetController implements I18NWidgetController<FieldComponent>{

	// the entity
	private FieldComponent fieldComponent;

	private Field field;

	// widget Maps
	private Map<String, Text> labelWidgetMap = new HashMap<>();
	private Map<String, Text> placeholderWidgetMap = new HashMap<>();
	private Map<String, Text> tooltipWidgetMap = new HashMap<>();
	private List<Map<String, Text>> fixedValuesWidgetMapList = null;
	private Map<String, Text> requiredMessageWidgetMap = new HashMap<>();


	public FieldComponentI18NWidgetController(Field field) {
		this.field = Objects.requireNonNull(field);
	}


	private boolean showPlaceholder() {
		return field.getFieldTypes().contains(FieldType.SINGLE_LINE_TEXT)
			|| field.getFieldTypes().contains(FieldType.MULTI_LINE_TEXT)
			|| field.getFieldTypes().contains(FieldType.CUSTOM_FIELD)
			|| field.getFieldTypes().contains(FieldType.COMBO)
			|| field.getFieldTypes().contains(FieldType.COUNTRY)
			|| field.getFieldTypes().contains(FieldType.LANGUAGE);
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		labelWidgetMap.put(lang, builder.fieldMetadata(FieldComponent.LABEL).build());

		if ( showPlaceholder() ) {
			placeholderWidgetMap.put(lang, builder.fieldMetadata(FieldComponent.PLACEHOLDER).build());
		}

		tooltipWidgetMap.put(lang, builder.fieldMetadata(FieldComponent.TOOLTIP).build());

		// fixedValues
		{
    		// number of widgets depends on fixedValueLabels
    		List<I18NString> fixedValueLabels = field.getFixedValuesLabelList();


    		// init fixedValuesWidgetMapList
    		if (fixedValuesWidgetMapList == null) {
    			fixedValuesWidgetMapList = new ArrayList<>();

    			if ( empty(fixedValueLabels) ) {
    				// add one Map
    				Map<String, Text> fixedValuesWidgetMap = new HashMap<>();
    				fixedValuesWidgetMapList.add(fixedValuesWidgetMap);
    			}
    			else {
    				// add one Map for every fixedValueLabels
    				for (@SuppressWarnings("unused") I18NString fixedValueLabel : fixedValueLabels) {
    					Map<String, Text> fixedValuesWidgetMap = new HashMap<>();
    					fixedValuesWidgetMapList.add(fixedValuesWidgetMap);
    				}
    			}
    		}


    		// create Widgets
    		if ( empty(fixedValueLabels) ) {
    			// always one Map
    			Map<String, Text>fixedValuesWidgetMap = fixedValuesWidgetMapList.get(0);
    			fixedValuesWidgetMap.put(lang, builder.fieldMetadata(FieldComponent.FIXED_VALUES).build());
    		}
    		else {
    			// one Map for every fixedValueLabels
    			int i = 0;
    			for (I18NString fixedValueLabel : fixedValueLabels) {
    				Map<String, Text>fixedValuesWidgetMap = fixedValuesWidgetMapList.get(i++);
    				fixedValuesWidgetMap.put(lang, builder.label(fixedValueLabel).build());
    			}
    		}
		}

		requiredMessageWidgetMap.put(lang, 	builder.fieldMetadata(FieldComponent.REQUIRED_MESSAGE).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public FieldComponent getEntity() {
		return fieldComponent;
	}


	public List<I18NString> getFixedValueLabels() {
		if (field != null) {
			return field.getFixedValuesLabelList();
		}
		return null;
	}


	@Override
	public void setEntity(FieldComponent fieldComponent) {
		this.fieldComponent = fieldComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (fieldComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(fieldComponent.getLabel(), labelWidgetMap);
						if ( showPlaceholder() ) {
							setLanguageStringToTextWidget(fieldComponent.getPlaceholder(), placeholderWidgetMap);
						}
						setLanguageStringToTextWidget(fieldComponent.getTooltip(), tooltipWidgetMap);
						syncFixedValuesWidgetsToEntity();
						setLanguageStringToTextWidget(fieldComponent.getRequiredMessage(), requiredMessageWidgetMap);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncFixedValuesWidgetsToEntity() throws IOException {
		// the number of widgets depends on fixedValueLabels

		List<I18NString> fixedValueLabels = field.getFixedValuesLabelList();
		if ( empty(fixedValueLabels) ) {
			Map<String, Text> fixedValuesWidgetMap = fixedValuesWidgetMapList.get(0);
			setLanguageStringToTextWidget(fieldComponent.getFixedValues(), fixedValuesWidgetMap);
		}
		else {
			LanguageString fixedValues = fieldComponent.getFixedValues();
			/* The Strings in fixedValues are comma-separated text values.
			 * So there is one comma-separated text value for each language.
			 * The number of text values in each comma-separated text is fixed and corresponds to
			 * fixedValueLabels.size() which is equal to the number of elements in fixedValuesWidgetMapList.
			 *
			 * What we need is one LanguageString for every fixedValuesWidgetMap in
			 * fixedValuesWidgetMapList. Each of them contains the text of a certain index position of the
			 * comma-separated text.
			 */

			// number of values in each comma-separated String
			int fixedValuesCount = fixedValueLabels.size();

			// one LanguageString for every widget
			List<LanguageString> fixedValueList = new ArrayList<>(fixedValuesCount);
			for (int i = 0; i < fixedValuesCount; i++) {
				fixedValueList.add( new LanguageString() );
			}

			for (Map.Entry<String, String> entry : fixedValues.entrySet()) {
				String lang = entry.getKey();
				String csvText = entry.getValue();

				String[] values = parseFirstLineCsv(csvText);

				for (int i = 0; i < fixedValuesCount; i++) {
					// value in language lang for widgets i
					String value = "";
					if (values.length > i) {
						value = values[i];
					}

					// LanguageString for widget i
					LanguageString languageString = fixedValueList.get(i);

					languageString.put(lang, value);
				}
			}

			for (int i = 0; i < fixedValuesCount; i++) {
				LanguageString fixedValue = fixedValueList.get(i);
				Map<String, Text> fixedValuesWidgetMap = fixedValuesWidgetMapList.get(i);
				setLanguageStringToTextWidget(fixedValue, fixedValuesWidgetMap);
			}
		}
	}


	private String[] parseFirstLineCsv(String csv) throws IOException {
		String[][] allValues = CSVParser.parse( new StringReader(csv) );
		String[] values = allValues[0];
		return values;
	}


	@Override
	public void syncEntityToWidgets() {
		if (fieldComponent != null) {
			fieldComponent.setLabel( buildLanguageString(labelWidgetMap) );
			if ( showPlaceholder() ) {
				fieldComponent.setPlaceholder( buildLanguageString(placeholderWidgetMap) );
			}
			fieldComponent.setTooltip( buildLanguageString(tooltipWidgetMap) );
			syncEntityToFixedValuesWidgets();
			fieldComponent.setRequiredMessage( buildLanguageString(requiredMessageWidgetMap) );
		}
	}


	private void syncEntityToFixedValuesWidgets() {
		List<I18NString> fixedValueLabels = field.getFixedValuesLabelList();
		if ( empty(fixedValueLabels) ) {
			Map<String, Text> fixedValuesWidgetMap = fixedValuesWidgetMapList.get(0);
			fieldComponent.setFixedValues( buildLanguageString(fixedValuesWidgetMap) );
		}
		else {
			// build Map from language code to csv and put in the values from each widget in the right order
			Map<String, CsvLineBuilder> lang2CsvLineBuilderMap = new HashMap<>();
			for (Map<String, Text> fixedValuesWidgetMap : fixedValuesWidgetMapList) {
				// build LanguageString from values of Nth widget
				LanguageString currentValueLanguageString = buildLanguageString(fixedValuesWidgetMap);

				// put values from previously LanguageString to the Map from language code to csv
				for (Map.Entry<String, String> entry : currentValueLanguageString.entrySet()) {
					String lang = entry.getKey();
					String value = entry.getValue();

					// get CsvLineBuilder for this language
					CsvLineBuilder csvLineBuilder = lang2CsvLineBuilderMap.get(lang);
					if (csvLineBuilder == null) {
						csvLineBuilder = FieldComponent.buildCsvLineBuilder();
						lang2CsvLineBuilderMap.put(lang,  csvLineBuilder);
					}

					// add current value to CSV line (with text delimiter)
					csvLineBuilder.addField(value);
				}
			}

			// build new LanguageString with csv-lines as values
			LanguageString fixedValues = new LanguageString();
			for (Map.Entry<String, CsvLineBuilder> entry : lang2CsvLineBuilderMap.entrySet()) {
				String lang = entry.getKey();
				CsvLineBuilder csvLineBuilder = entry.getValue();
				String csvLine = csvLineBuilder.build();

				fixedValues.put(lang, csvLine);
			}

			fieldComponent.setFixedValues(fixedValues);
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
