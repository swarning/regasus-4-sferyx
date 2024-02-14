package de.regasus.impex.ods.dialog;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.program.Program;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.impex.ImpexI18N;

public abstract class ODSImportWizard extends Wizard {

	public ODSImportWizard() {
		super();
	}

	public void showStatisticsMessageDialog(
		String entities,
		boolean success,
		int created,
		int updated,
		int errors,
		int duplicates,
		File errorFile
	) {
		if (success) {
			String stats = ImpexI18N.ODSImport_Statistics;
			stats = stats.replace("<entities>", entities);
			stats = stats.replace("<created>", String.valueOf(created));
			stats = stats.replace("<updated>", String.valueOf(updated));
			
			String message = ImpexI18N.ODSImportAction_Completed + "\n\n" + stats;
			
			MessageDialog.openInformation(
				getShell(),
				UtilI18N.Info,
				message
			);
		}
		else {
			String stats = ImpexI18N.ODSImport_StatisticsWithErrors;
			stats = stats.replace("<entities>", entities);
			stats = stats.replace("<created>", String.valueOf(created));
			stats = stats.replace("<updated>", String.valueOf(updated));
		
			stats = stats.replace("<errors>", String.valueOf(errors));
			stats = stats.replace("<duplicates>", String.valueOf(duplicates));
			
			String errorFileName = errorFile.getName();
			stats = stats.replace("<ErrorFileName>", errorFileName);
			
			String message = ImpexI18N.ODSImportAction_Completed + "\n\n" + stats;
			
			// Show a dialog with a button to optionally open the error file 
			MessageDialog dialog = new MessageDialog(getShell(), UtilI18N.Warning, null,
				message, MessageDialog.WARNING, new String[] { UtilI18N.OpenVerb, UtilI18N.OK }, 0);
		
			// Open the file if desired
			int result = dialog.open();
			if (result == 0) {
				Program.launch(errorFile.getAbsolutePath());
			}
		}
	}

}
