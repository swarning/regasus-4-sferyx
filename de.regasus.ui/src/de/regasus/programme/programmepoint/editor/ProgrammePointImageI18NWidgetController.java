package de.regasus.programme.programmepoint.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointFile;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.image.ImageFileGroup;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class ProgrammePointImageI18NWidgetController implements I18NWidgetController<ProgrammePointVO>{

	// the entity
	private ProgrammePointVO programmePoint;

	/**
	 * Map from Language to Text widget.
	 */
	private List<ProgrammePointImageFileGroupController> controllerList = new ArrayList<>();


	public ProgrammePointImageI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite widgetComposite, ModifySupport modifySupport, String language) {
		widgetComposite.setLayout( new FillLayout() );

		// ImageFileComposite
		ImageFileGroup imageFileGroup = new ImageFileGroup(widgetComposite, SWT.NONE);

		// create ImageFileGroupController
		ProgrammePointImageFileGroupController controller = new ProgrammePointImageFileGroupController(
			language,
			ProgrammePointFile.IMAGE
		);
		imageFileGroup.setController(controller);
		controllerList.add(controller);
	}


	@Override
	public void dispose() {
	}


	@Override
	public ProgrammePointVO getEntity() {
		return programmePoint;
	}


	@Override
	public void setEntity(ProgrammePointVO programmePoint) {
		this.programmePoint = programmePoint;

		// init ImageFileComposites and controllers, but not before the PageLayout exists in the DB
		if (programmePoint.getID() != null) {
    		for (ProgrammePointImageFileGroupController controller : controllerList) {
    			controller.setProgrammePointPK( programmePoint.getID() );
    		}
		}

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (programmePoint != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// nothing to do
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
		// nothing to do
	}


	@Override
	public void addFocusListener(FocusListener listener) {
	}

}
