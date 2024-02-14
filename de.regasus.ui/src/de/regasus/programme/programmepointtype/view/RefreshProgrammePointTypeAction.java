package de.regasus.programme.programmepointtype.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.programme.ProgrammePointTypeModel;

public class RefreshProgrammePointTypeAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.programmePointType.RefreshProgrammePointTypeAction"; 

	
	public RefreshProgrammePointTypeAction() {
		super();
		
		setId(ID);
		setText(I18N.RefreshProgrammePointTypeAction_Text);
		setToolTipText(I18N.RefreshProgrammePointTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}
	
	
	/* runWithBusyCursor(), because.
	 * a) Refreshing may last long.
	 * b) Refresh don't need to be run in the Display-Thread.
	 */
	@Override
	public void runWithBusyCursor() {
		try {
			ProgrammePointTypeModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
