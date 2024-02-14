package de.regasus.programme.programmepoint.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class ProgrammePointSelectionDialog extends TitleAreaDialog {

	private EntityProvider<ProgrammePointCVO> programmePointProvider;

	// init values
	private ListSet<Long> initiallySelectedProgrammePointPKs;

	// result values
	private List<ProgrammePointCVO> selectedProgrammePointList;

	// widgets
	private ChooseProgrammePointsComposite chooseProgrammePointsComposite;


	public ProgrammePointSelectionDialog(
		Shell shell,
		EntityProvider<ProgrammePointCVO> programmePointProvider,
		ListSet<Long> selectedProgrammePointPKs
	) {
		super(shell);
		this.programmePointProvider = programmePointProvider;
		this.initiallySelectedProgrammePointPKs = selectedProgrammePointPKs;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.ProgrammePointSelectionDialog_Title);
		setMessage(I18N.ProgrammePointSelectionDialog_Message);

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		try {
			chooseProgrammePointsComposite = new ChooseProgrammePointsComposite(dialogArea, programmePointProvider, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(chooseProgrammePointsComposite);
			chooseProgrammePointsComposite.setChosenIds(initiallySelectedProgrammePointPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return dialogArea;
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


//	@Override
//	protected void configureShell(Shell newShell) {
//		super.configureShell(newShell);
//		newShell.setText(EmailI18N.CitySelectionDialog_ShellText);
//	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
			selectedProgrammePointList = chooseProgrammePointsComposite.getChosenEntities();
		}
		super.buttonPressed(buttonId);
	}


	public List<ProgrammePointCVO> getSelectedProgrammePointCVOs() {
		return selectedProgrammePointList;
	}

}
