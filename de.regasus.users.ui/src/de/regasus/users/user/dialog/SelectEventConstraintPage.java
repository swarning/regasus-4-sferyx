package de.regasus.users.user.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.auth.api.ACLObjectDefinitions;
import de.regasus.event.EventTableComposite;

public class SelectEventConstraintPage extends WizardPage {

	public static final String NAME = "SelectEventConstraintPage";

	private EventTableComposite eventTableComposite;


	protected SelectEventConstraintPage() {
		super(NAME);

		String title = UtilI18N.SelectX.replace("<x>", ParticipantLabel.Event.getString());
		setTitle(title);

	}


	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);

		eventTableComposite = new EventTableComposite(
			container,
			null,	// hiddenEventPKs
			null,	// initiallySelectedEventPKs
			false,	// multiSelection
			SWT.NONE
		);

		eventTableComposite.addModifyListener(tableListener);

		setPageComplete(false);
	}


	private ModifyListener tableListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			SelectHotelContingentConstraintPage hotelContingentConstraintPage =
				(SelectHotelContingentConstraintPage) getWizard().getPage(SelectHotelContingentConstraintPage.NAME);
			hotelContingentConstraintPage.setDirty(true);

			SelectProgrammePointConstraintPage programmePointConstraintPage =
				(SelectProgrammePointConstraintPage) getWizard().getPage(SelectProgrammePointConstraintPage.NAME);
			programmePointConstraintPage.setDirty(true);

			setPageComplete( isPageComplete() );
		}
	};


	@Override
	public boolean isPageComplete() {
		return !eventTableComposite.getSelectedEvents().isEmpty();
	}


//	/**
//	 * When this page is made visible, show buttons for the constraint types of the right selected on the previous page.
//	 */
//	@Override
//	public void setVisible(boolean visible) {
//		if (visible && eventVOs == null) {
//			try {
//				eventVOs = EventModel.getInstance().getAllEventVOs(true /*onlyUnclosed*/);
//				tableViewer.setInput(eventVOs);
//			}
//			catch (Exception e) {
//				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//			}
//		}
//		super.setVisible(visible);
//	}


	public EventVO getSelectedEvent() {
		return eventTableComposite.getSelectedEvent();
	}


	@Override
	public IWizardPage getNextPage() {

		String constraintType = ((AddRightWizard) getWizard()).getConstraintType();

		if (ACLObjectDefinitions.CONSTRAINT_TYPE_PROGRAMME_POINT.equals(constraintType)) {
			return getWizard().getPage(SelectProgrammePointConstraintPage.NAME);
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL.equals(constraintType)) {
			return getWizard().getPage(SelectHotelContingentConstraintPage.NAME);
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL_CONTINGENT.equals(constraintType)) {
			return getWizard().getPage(SelectHotelContingentConstraintPage.NAME);
		}
		else {
			return getWizard().getPage(SetCrudRightsAndPriorityPage.NAME);
		}
	}


	@Override
	public IWizardPage getPreviousPage() {
		return getWizard().getPage(SelectConstraintTypePage.NAME);
	}

}
