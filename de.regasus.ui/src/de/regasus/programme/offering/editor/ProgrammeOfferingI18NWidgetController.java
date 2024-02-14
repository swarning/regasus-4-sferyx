package de.regasus.programme.offering.editor;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;


public class ProgrammeOfferingI18NWidgetController implements I18NWidgetController<ProgrammeOfferingVO> {

	// the entity
	private ProgrammeOfferingVO programmeOfferingVO;

	// widget Maps
	private Map<String, Text> descriptionWidgetMap = new HashMap<>();
	private Map<String, Text> onlineInfoWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		builder.multiLine(true);
		descriptionWidgetMap.put(lang, builder.label(ParticipantLabel.ProgrammeOffering_Description).build());
		onlineInfoWidgetMap.put(lang,  builder.label(ParticipantLabel.ProgrammeOffering_OnlineInfo).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public ProgrammeOfferingVO getEntity() {
		return programmeOfferingVO;
	}


	@Override
	public void setEntity(ProgrammeOfferingVO programmeOfferingVO) {
		this.programmeOfferingVO = programmeOfferingVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (programmeOfferingVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(programmeOfferingVO.getDescription(), descriptionWidgetMap);
						setLanguageStringToTextWidget(programmeOfferingVO.getOnlineInfo(), onlineInfoWidgetMap);
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
		if (programmeOfferingVO != null) {
			programmeOfferingVO.setDescription( buildLanguageString(descriptionWidgetMap) );
			programmeOfferingVO.setOnlineInfo(  buildLanguageString(onlineInfoWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		addFocusListenerToWidgets(listener, descriptionWidgetMap, onlineInfoWidgetMap);
	}

}
