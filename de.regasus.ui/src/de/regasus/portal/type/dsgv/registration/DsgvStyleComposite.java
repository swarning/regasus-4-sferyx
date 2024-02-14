package de.regasus.portal.type.dsgv.registration;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.portal.portal.editor.StyleParameterComposite;
import de.regasus.ui.Activator;



public class DsgvStyleComposite extends Composite implements StyleParameterComposite {

	private static final String FONT_FAMILY = "font-family";


	private PageLayout pageLayout;

	private ModifySupport modifySupport = new ModifySupport(this);

	private boolean widgetsCreated = false;


	// **************************************************************************
	// * Widgets
	// *

	private Text fontFamilyText;

	// *
	// * Widgets
	// **************************************************************************


	public DsgvStyleComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void createWidgets() throws Exception {
		if (widgetsCreated) {
			return;
		}

		// layout without margin, because it works only as a container
		GridLayout mainLayout = new GridLayout();
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		setLayout(mainLayout);

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false);
		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		Group fontGroup = new Group(this, SWT.NONE);
		groupGridDataFactory.applyTo(fontGroup);
		fontGroup.setText("Schriftart");
		fontGroup.setLayout( new GridLayout(2, false) );

		// font family, e.g.: "Sparkasse Web", Arial, sans-serif
		{
			Label label = new Label(fontGroup, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Schriftfamilie");

			fontFamilyText = new Text(fontGroup, SWT.BORDER);
			textGridDataFactory.applyTo(fontFamilyText);
			fontFamilyText.addModifyListener(modifySupport);
		}

		widgetsCreated = true;
	}


	@Override
	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;

		setStyleParameters( pageLayout.getStyleParameters() );
	}


	@Override
	public void syncEntityToWidgets() {
		Properties parameters = getStyleParameters();
		pageLayout.setStyleParameters(parameters);

		String css = getCSS();
		pageLayout.setStyle(css);
	}


	private Properties getStyleParameters() {
		Properties styleParameters = new Properties();

		styleParameters.setProperty(FONT_FAMILY, fontFamilyText.getText());

		return styleParameters;
	}


	private void setStyleParameters(Properties styleParameters) {
		if (styleParameters != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						fontFamilyText.setText( avoidNull(styleParameters.getProperty(FONT_FAMILY)) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private String getCSS() {
		String css = null;
		try {
    		String template = pageLayout.getStyleTemplateContent();
    		Properties parameters = getStyleParameters();

    		if (template != null && parameters != null) {
       			css = generateStyle(template, parameters);
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return css;
	}


	private String generateStyle(String template, Properties parameters) {
		Objects.requireNonNull(template);
		Objects.requireNonNull(parameters);

		List<String> variableList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();

		StringBuilder variable = new StringBuilder(128);

		for (Map.Entry<?, ?> entry : parameters.entrySet()) {
			variable.setLength(0);
			variable.append("${").append( entry.getKey() ).append("}");
			String value = entry.getValue() != null ? entry.getValue().toString() : "";

			variableList.add( variable.toString() );
			valueList.add(value);
		}

		String style = StringHelper.replace(template, variableList, valueList);
		return style;
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
