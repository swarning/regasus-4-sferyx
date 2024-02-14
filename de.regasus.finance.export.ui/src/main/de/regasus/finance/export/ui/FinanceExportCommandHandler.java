package de.regasus.finance.export.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.StringHelper;

import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;


/**
 * Load configured {@link IFinanceExportStarter} and open the dialog.
 */
public class FinanceExportCommandHandler extends AbstractHandler {

	public static final String FINANCE_EXPORT_CLASS_KEY = "finance.export.starter.class";
	
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("Starting Finance Export");

		try {
			Shell shell = HandlerUtil.getActiveShell(event);			
			
			// check if a class to be used as FinanceExportDialog is configured
			String className = PropertyModel.getInstance().getPropertyValue(FINANCE_EXPORT_CLASS_KEY);
			if (StringHelper.isNotEmpty(className)) {
				// try to instantiate financeExportClass
				try {
					IFinanceExportStarter dialog = (IFinanceExportStarter) Class.forName(className).newInstance();
					dialog.openFinanceExportDialog(shell);
				}
				catch (ClassNotFoundException e) {
					String msg = FinanceExportI18N.FinanceExportClassNotFound;
					msg = msg.replaceFirst("<class>", className);
					MessageDialog.openError(shell, UtilI18N.Error, msg);
				}
				catch (ClassCastException e) {
					String msg = FinanceExportI18N.FinanceExportClassCastError;
					msg = msg.replaceFirst("<class>", className);
					MessageDialog.openError(shell, UtilI18N.Error, msg);
				}
			}
			else {
				MessageDialog.openInformation(shell, UtilI18N.Info, FinanceExportI18N.KeyNotDefined);
			}
			
			return null;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
