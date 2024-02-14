package de.regasus.common.composite;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.contact.data.PostalCodeVO;
import com.lambdalogic.util.StringHelper;

import de.regasus.core.ui.CoreI18N;



public class CitySelectionDialog extends Dialog {

	private List<PostalCodeVO> postalCodeVOs;
	private PostalCodeVO postalCodeVO;


	// Widgets
	private ListViewer cityListViewer;


	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public CitySelectionDialog(Shell parentShell, List<PostalCodeVO> postalCodeVOs) {
		super(parentShell);
		this.postalCodeVOs = postalCodeVOs;
	}


	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		cityListViewer = new ListViewer(container, SWT.BORDER);
		cityListViewer.setContentProvider(new ArrayContentProvider());
		cityListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				PostalCodeVO postalCodeVO = (PostalCodeVO) element;

				StringBuilder text = new StringBuilder(100);

				text.append(postalCodeVO.getCity());
				if (StringHelper.isNotEmpty(postalCodeVO.getState())) {
					text.append(" (");
					text.append(postalCodeVO.getState());
					text.append(")");
				}

				return text.toString();
			}
		});

		cityListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					postalCodeVO = (PostalCodeVO) selection.getFirstElement();
				}
			}
		});
		cityListViewer.setSorter(new ViewerSorter());
		cityListViewer.setInput(postalCodeVOs);

		return container;
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


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(CoreI18N.CitySelectionDialog_ShellText);
	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CANCEL) {
			postalCodeVO = null;
		}
		super.buttonPressed(buttonId);
	}


	public PostalCodeVO getPostalCodeVO() {
		return postalCodeVO;
	}

}
