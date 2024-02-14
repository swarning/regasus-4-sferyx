package de.regasus.users.user.dialog;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.users.ui.Activator;


public class SelectHotelContingentConstraintPage extends WizardPage implements ISelectionChangedListener {

	public static final String NAME = "SelectHotelContingentConstraintPage";

	private ListViewer listViewer;

	private boolean dirty;

	protected SelectHotelContingentConstraintPage() {
		super(NAME);

		String title = UtilI18N.SelectX.replace("<x>", HotelLabel.HotelContingent.getString());
		setTitle(title);
	}

	@Override
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);


		List list = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		listViewer = new ListViewer(list);

		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new HotelContingentLabelProvider());

		listViewer.addSelectionChangedListener(this);
		setPageComplete(false);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = listViewer.getSelection();
		setPageComplete(! selection.isEmpty());
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && dirty) {
			try {

				dirty = false;

				EventVO eventVO = ((AddRightWizard) getWizard()).getEventVO();
				HotelContingentModel instance = HotelContingentModel.getInstance();
				Long eventPK = eventVO.getID();
				java.util.List<HotelContingentCVO> hotelContingentCVOs = instance.getHotelContingentCVOsByEventPK(eventPK);
				listViewer.setInput(hotelContingentCVOs);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
		super.setVisible(visible);
	}


	@Override
	public IWizardPage getNextPage() {
		return getWizard().getPage(SetCrudRightsAndPriorityPage.NAME);
	}

	@Override
	public IWizardPage getPreviousPage() {
		return getWizard().getPage(SelectEventConstraintPage.NAME);
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public HotelContingentCVO getHotelContingentCVO() {
		return SelectionHelper.getUniqueSelected(listViewer.getSelection());
	}
}
