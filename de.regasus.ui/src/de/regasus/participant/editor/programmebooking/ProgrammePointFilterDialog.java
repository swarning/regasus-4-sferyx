package de.regasus.participant.editor.programmebooking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO_Position_Comparator;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.programme.ProgrammePointModel;

class ProgrammePointFilterDialog extends TitleAreaDialog {

	private CheckboxTableViewer programmePointTableViewer;

	private Table table;

	private TableColumn programmePointNameColumn;

	private ProgrammePointModel programmePointModel = ProgrammePointModel.getInstance();

	private ProgrammePointCVO[] checkedProgrammePoints;

	private List<ProgrammePointCVO> allProgrammePoints;


	public ProgrammePointFilterDialog(Shell parentShell, Long eventPK) throws Exception {
		super(parentShell);

		List<ProgrammePointVO> list = programmePointModel.getProgrammePointVOsByEventPK(eventPK);
		list = new ArrayList<>(list);
		Collections.sort(list, ProgrammePointVO_Position_Comparator.getInstance());

		allProgrammePoints = ProgrammePointCVO.convertProgrammePointVO2CVO(list);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Filter);

		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(1, true));

		table = new Table(area, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableColumn checkTableColumn = new TableColumn(table, SWT.RIGHT, 0);

		programmePointNameColumn = new TableColumn(table, SWT.LEFT, 1);

		programmePointTableViewer = new CheckboxTableViewer(table);
		programmePointTableViewer.setContentProvider(new ArrayContentProvider());
		programmePointTableViewer.setLabelProvider(new ProgrammePointFilterLabelProvider());
		programmePointTableViewer.setInput(allProgrammePoints);

		checkTableColumn.pack();
		programmePointNameColumn.setWidth(350);

		Composite checkBoxComposite = new Composite(area, SWT.NONE);
		checkBoxComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.pack = false;
		checkBoxComposite.setLayout(rowLayout);

		// Checkboxen
		Button allButton = new Button(checkBoxComposite, SWT.PUSH);
		allButton.setText(UtilI18N.All);
		allButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				programmePointTableViewer.setCheckedElements(allProgrammePoints.toArray());
			}
		});

		Button nothingButton = new Button(checkBoxComposite, SWT.PUSH);
		nothingButton.setText(UtilI18N.None);
		nothingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				programmePointTableViewer.setCheckedElements(new Object[0]);
			}
		});

		if (checkedProgrammePoints != null) {
			programmePointTableViewer.setCheckedElements(checkedProgrammePoints);
		}
		else {
			programmePointTableViewer.setCheckedElements(allProgrammePoints.toArray());
		}

		return area;
	}


	/**
	 * Create the three buttons of the button bar, one to switch the filter, and the common OK and CANCEL buttons.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		checkedProgrammePoints = getCheckedProgrammePointsFromViewer();

		createButton(parent, IDialogConstants.CLIENT_ID + 1, UtilI18N.On, true);
		createButton(parent, IDialogConstants.CLIENT_ID + 2, UtilI18N.Off, false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		shell.setText(UtilI18N.Filter);
	}


	private ProgrammePointCVO[] getCheckedProgrammePointsFromViewer() {
		Object[] checkedElements = programmePointTableViewer.getCheckedElements();
		ProgrammePointCVO[] checkedProgrammePoints = new ProgrammePointCVO[checkedElements.length];
		for (int i = 0; i < checkedProgrammePoints.length; i++) {
			checkedProgrammePoints[i] = (ProgrammePointCVO) checkedElements[i];
		}
		return checkedProgrammePoints;
	}


	@Override
	protected void buttonPressed(int buttonId) {
		checkedProgrammePoints = getCheckedProgrammePointsFromViewer();

		setReturnCode(buttonId);
		close();
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 605);
	}


	public void setCheckedProgrammePoints(ProgrammePointCVO[] checkedProgrammePoints) {
		this.checkedProgrammePoints = checkedProgrammePoints;
	}


	public ProgrammePointCVO[] getCheckedProgrammePoints() {
		return checkedProgrammePoints;
	}

}

class ProgrammePointFilterLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	/**
	 * Show no images
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	/**
	 * The second column (index=1) shall show programme point name
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ProgrammePointCVO && columnIndex == 1) {
			ProgrammePointCVO programmePointCVO = (ProgrammePointCVO) element;
			return programmePointCVO.getVO().getName().getString();
		}
		return null;
	}

}
