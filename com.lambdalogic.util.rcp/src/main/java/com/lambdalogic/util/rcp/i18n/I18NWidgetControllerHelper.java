package com.lambdalogic.util.rcp.i18n;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;


public class I18NWidgetControllerHelper {

	public static void setLanguageStringToTextWidget(LanguageString label, Map<String, ? extends Text> widgetMap) {
		if (label == null) {
			label = new LanguageString();
		}
		for (Map.Entry<String, ? extends Text> entry : widgetMap.entrySet()) {
			String lang = entry.getKey();
			Text widget = entry.getValue();
			widget.setText( avoidNull(label.getString(lang, false)) );
		}
	}




	/**
	 * Build a {@link LanguageString} from the values in the given {@link Map} of {@link Text} widgets.
	 * @param widgetMap
	 * @return
	 */
	public static LanguageString buildLanguageString(Map<String, ? extends Text> widgetMap) {
		LanguageString langStr = new LanguageString();

		for (Map.Entry<String, ? extends Text> entry : widgetMap.entrySet()) {
			String lang = entry.getKey();
			Text widget = entry.getValue();
			langStr.put(lang, widget.getText());
		}

		return langStr;
	}


	public static void addFocusListenerToWidgets(FocusListener listener, Map<String, ? extends Text>... widgetMapArray) {
		for (Map<String, ? extends Text> widgetMap : widgetMapArray) {
			for (Text text : widgetMap.values()) {
				text.addFocusListener(listener);
			}
		}
	}


	public static void addFocusListenerToWidgets(FocusListener listener, Collection<Map<String, ? extends Text>> widgetMaps) {
		for (Map<String, ? extends Text> widgetMap : widgetMaps) {
			for (Text text : widgetMap.values()) {
				text.addFocusListener(listener);
			}
		}
	}

}
