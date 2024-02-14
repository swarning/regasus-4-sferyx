package de.regasus.common.composite;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.ILabelTextCombinations;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class LabelTextCombinationsI18NWidgetController implements I18NWidgetController<ILabelTextCombinations> {

	// the entity
	private ILabelTextCombinations labelTextCombinations;

	private int size;

	// widget Maps
	private List<Map<String, Text>> labelWidgetMapList;
	private List<Map<String, Text>> textWidgetMapList;


	public LabelTextCombinationsI18NWidgetController(int size) {
		this.size = size;

		labelWidgetMapList = new ArrayList<>(size);
		textWidgetMapList = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			labelWidgetMapList.add( new HashMap<>());
			textWidgetMapList.add( new HashMap<>() );
		}
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		for (int i = 0; i < size; i++) {
			String labelPostfix = " " + (i + 1);

			// create single-line widget for label
			Map<String, Text> labelWidgetMap = labelWidgetMapList.get(i);
			builder.multiLine(false);
			String labelLabel = KernelLabel.Label.getString() + labelPostfix;
			labelWidgetMap.put(lang, builder.label(labelLabel).build());

			// create multi-line widget for text
			Map<String, Text> textWidgetMap = textWidgetMapList.get(i);
			builder.multiLine(true);
			String textLabel = KernelLabel.Text.getString() + labelPostfix;
			textWidgetMap.put(lang, builder.label(textLabel).build());
		}
	}


	@Override
	public void dispose() {
	}


	@Override
	public ILabelTextCombinations getEntity() {
		return labelTextCombinations;
	}


	@Override
	public void setEntity(ILabelTextCombinations labelTextCombinations) {
		this.labelTextCombinations = labelTextCombinations;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (labelTextCombinations != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						for (int i = 0; i < size; i++) {
							LanguageString label = labelTextCombinations.getLabel(i);
							Map<String, Text> labelWidgetMap = labelWidgetMapList.get(i);
							setLanguageStringToTextWidget(label, labelWidgetMap);

							LanguageString text = labelTextCombinations.getText(i);
							Map<String, Text> textWidgetMap = textWidgetMapList.get(i);
							setLanguageStringToTextWidget(text, textWidgetMap);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (labelTextCombinations != null) {
			for (int i = 0; i < size; i++) {
				Map<String, Text> labelWidgetMap = labelWidgetMapList.get(i);
				labelTextCombinations.setLabel(i, buildLanguageString(labelWidgetMap) );

    			Map<String, Text> textWidgetMap = textWidgetMapList.get(i);
    			labelTextCombinations.setText(i, buildLanguageString(textWidgetMap) );
			}
		}
	}


	@Override
	@SuppressWarnings("rawtypes")
	public void addFocusListener(FocusListener listener) {
		addFocusListenerToWidgets(listener, (List) labelWidgetMapList);
		addFocusListenerToWidgets(listener, (List) textWidgetMapList);
	}

}
