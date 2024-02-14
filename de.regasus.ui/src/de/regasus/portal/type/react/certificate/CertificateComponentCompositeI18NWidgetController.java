package de.regasus.portal.type.react.certificate;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.react.certificate.CertificateComponent;

public class CertificateComponentCompositeI18NWidgetController
	implements I18NWidgetController<CertificateComponent> {

	// the entity
	private CertificateComponent component;

	// widget Maps
	private Map<String, Text> nameWidgetMap = new HashMap<>();
	private Map<String, Text> downloadButtonLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		nameWidgetMap.put(lang, builder.fieldMetadata(CertificateComponent.FIELD_NAME).build());
		downloadButtonLabelWidgetMap.put(lang, builder.fieldMetadata(CertificateComponent.FIELD_DOWNLOAD_BUTTON_LABEL).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public CertificateComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(CertificateComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getName(), nameWidgetMap);
						setLanguageStringToTextWidget(component.getDownloadButtonLabel(), downloadButtonLabelWidgetMap);
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
		if (component != null) {
			component.setName( buildLanguageString(nameWidgetMap) );
			component.setDownloadButtonLabel( buildLanguageString(downloadButtonLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
