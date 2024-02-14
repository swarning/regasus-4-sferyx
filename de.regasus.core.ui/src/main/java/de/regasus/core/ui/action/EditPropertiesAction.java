package de.regasus.core.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.editor.property.PropertiesEditor;
import de.regasus.core.ui.editor.property.PropertiesEditorInput;

public class EditPropertiesAction extends Action implements ModelListener {

	public static final String ID = "de.regasus.core.ui.action.EditPropertiesAction";

	// Models
	private static final ServerModel serverModel = ServerModel.getInstance();

	private final IWorkbenchWindow window;


	public EditPropertiesAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(CoreI18N.EditPropertiesAction_Text);
		setToolTipText(CoreI18N.EditPropertiesAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.EditPropertiesAction));

		// beim ServerModel registrieren
		serverModel.addListener(this);
		setEnabled(serverModel.isLoggedIn());
	}


	@Override
	public void dataChange(ModelEvent event) {
		if (event.getSource() instanceof ServerModel) {
			setEnabled(serverModel.isLoggedIn());
		}
	}


	public void dispose() {
		serverModel.removeListener(this);
	}

	@Override
	public void run() {
		try {
			window.getActivePage().openEditor(new PropertiesEditorInput(), PropertiesEditor.ID);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
