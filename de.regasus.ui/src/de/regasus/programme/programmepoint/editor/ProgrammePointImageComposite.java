package de.regasus.programme.programmepoint.editor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

/**
 * Composite used in {@link ProgrammePointEditor} to show the image of a {@link ProgrammePoint}.
 */
public class ProgrammePointImageComposite extends Composite {

	// the entity
	private ProgrammePointVO programmePoint;

	// languages as defined in the associated Event
	private List<Language> languageList;


	protected ModifySupport modifySupport = new ModifySupport(this);


	// **************************************************************************
	// * Widgets
	// *

	private I18NComposite<ProgrammePointVO> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************

	public ProgrammePointImageComposite(
		Composite parent,
		int style,
		List<String> languageIds
	)
	throws Exception {
		super(parent, style);

		try {
			languageList = LanguageModel.getInstance().getLanguages(languageIds);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setLayout(new FillLayout());

		ProgrammePointImageI18NWidgetController controller = new ProgrammePointImageI18NWidgetController();
		i18nComposite = new I18NComposite<>(this, SWT.BORDER, languageList, controller);

		i18nComposite.addModifyListener(modifySupport);
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


	public void setProgrammePoint(ProgrammePointVO programmePoint) {
		this.programmePoint = programmePoint;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					i18nComposite.setEntity(programmePoint);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public void syncEntityToWidgets() {
		if (programmePoint != null) {
			i18nComposite.syncEntityToWidgets();
		}
	}

}
