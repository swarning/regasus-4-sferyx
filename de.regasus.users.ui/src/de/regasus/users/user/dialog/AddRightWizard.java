package de.regasus.users.user.dialog;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.account.data.AccessControlEntryVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.auth.api.ACLObject;
import de.regasus.users.UsersI18N;

public class AddRightWizard extends Wizard {

	private String owner;
	private SelectACLObjectPage selectACLObjectPage;
	private SelectConstraintTypePage selectConstraintTypePage;
	private SetCrudRightsAndPriorityPage setCrudRightsAndPriorityPage;
	private SelectEventConstraintPage selectEventConstraintPage;
	private SelectReportConstraintPage selectReportConstraintPage;
	private SelectProgrammePointConstraintPage selectProgrammePointConstraintPage;
	private SelectHotelContingentConstraintPage selectHotelContingentConstraintPage;

	private AccessControlEntryVO accessControlEntryVO;

	// **************************************************************************
	// * Constructors
	// *

	public AddRightWizard(String owner) {
		this.owner = owner;
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		selectACLObjectPage = new SelectACLObjectPage();
		addPage(selectACLObjectPage);
		
		selectConstraintTypePage = new SelectConstraintTypePage();
		addPage(selectConstraintTypePage);
		
		selectEventConstraintPage = new SelectEventConstraintPage();
		addPage(selectEventConstraintPage);
	
		selectProgrammePointConstraintPage = new SelectProgrammePointConstraintPage();
		addPage(selectProgrammePointConstraintPage);
		
		selectHotelContingentConstraintPage = new SelectHotelContingentConstraintPage();
		addPage(selectHotelContingentConstraintPage);
		
		selectReportConstraintPage = new SelectReportConstraintPage();
		addPage(selectReportConstraintPage);
		
		setCrudRightsAndPriorityPage = new SetCrudRightsAndPriorityPage();
		addPage(setCrudRightsAndPriorityPage);
	}


	public ACLObject getACLObject() {
		return selectACLObjectPage.getSelectedACLObject();
	}
	
	public EventVO getEventVO() {
		return selectEventConstraintPage.getSelectedEvent();
	}
	
	public ProgrammePointVO getProgrammePointVO() {
		return selectProgrammePointConstraintPage.getProgrammePointVO();
	}
	
	public HotelContingentCVO getHotelContingentCVO() {
		return selectHotelContingentConstraintPage.getHotelContingentCVO();
	}
	
	public UserReportVO getUserReportVO() {
		return selectReportConstraintPage.getUserReportVO();
	}

	public String getConstraintType() {
		return selectConstraintTypePage.getConstraintType();
	}
	
	
	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == setCrudRightsAndPriorityPage; 
	}
	
	@Override
	public String getWindowTitle() {
		return UsersI18N.AddRight;
	}

	@Override
	public boolean performFinish() {
		accessControlEntryVO = setCrudRightsAndPriorityPage.createAccessControlEntryVO();
		return true;
	}

	public String getOwner() {
		return owner;
	}

	public AccessControlEntryVO getAccessControlEntryVO() {
		return accessControlEntryVO;
	}
	
}
