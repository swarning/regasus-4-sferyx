package de.regasus.users.user.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.util.Locale;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryVO;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.report.ReportLabel;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.auth.api.ACLObject;
import de.regasus.auth.api.ACLObjectDefinitions;
import de.regasus.users.UsersAdministrationHelper;
import de.regasus.users.UsersI18N;

public class SetCrudRightsAndPriorityPage extends WizardPage {

	public static final String NAME = "SetCrudRightsAndPriorityPage";

	private Composite summaryComposite;

	private Composite buttonComposite;

	private Composite container;

	private RightButton readButton;
	private RightButton writeButton;
	private RightButton createButton;
	private RightButton deleteButton;

	private NullableSpinner prioritySpinner;

	private ACLObject aclObject;

	
	private String constraintType;

	private Long constraintPK;

	private AddRightWizard addRightWizard;

	protected SetCrudRightsAndPriorityPage() {
		super(NAME);

		setTitle(UsersI18N.SetCrudRightsAndPriority);
	}


	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());

		setControl(container);
		
		summaryComposite = new Composite(container, SWT.NONE);
		summaryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		summaryComposite.setLayout(new GridLayout(2, false));

		buttonComposite = new Composite(container, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.center = true;
		rowLayout.pack = false;
		rowLayout.justify = true;
		buttonComposite.setLayout(rowLayout);
		
		readButton = createRightButton(AccountLabel.Read);
		writeButton = createRightButton(AccountLabel.Write);
		createButton = createRightButton(AccountLabel.Create);
		deleteButton = createRightButton(AccountLabel.Delete);
	}


	private RightButton createRightButton(AccountLabel label) {
		RightButton readButton = new RightButton(buttonComposite, SWT.PUSH);
		readButton.setText(label.getString());
		return readButton;
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			
			for (Control control : summaryComposite.getChildren()) {
				control.dispose();
			}
			
			addRightWizard = (AddRightWizard) getWizard();

			createLabelAndReadOnlyText(summaryComposite, AccountLabel.Owner, addRightWizard.getOwner());
			
			aclObject = addRightWizard.getACLObject();
			String right = AccountLabel.valueOf(aclObject.object).getString();

			createLabelAndReadOnlyText(summaryComposite, AccountLabel.Right, right);

			constraintType = addRightWizard.getConstraintType();

			if (aclObject != null && aclObject.constraintTypes != null && aclObject.constraintTypes.length > 0) {

				if (constraintType == null) {
					createLabelAndReadOnlyText(summaryComposite, AccountLabel.ConstraintType, UsersI18N.NoConstraintType);
				} else {
					createLabelAndReadOnlyText(summaryComposite, AccountLabel.ConstraintType, UsersAdministrationHelper.getLabelForConstraintType(constraintType));
				}
				
				
				if (ACLObjectDefinitions.CONSTRAINT_TYPE_EVENT.equals(constraintType)) {
					EventVO eventVO = addRightWizard.getEventVO();
					constraintPK = eventVO.getID();
					createLabelAndReadOnlyText(
						summaryComposite, 
						ParticipantLabel.Event, 
						eventVO.getName(Locale.getDefault())
					);
				}
				else if (ACLObjectDefinitions.CONSTRAINT_TYPE_REPORT.equals(constraintType)) {
					UserReportVO userReportVO = addRightWizard.getUserReportVO();
					constraintPK = userReportVO.getID();
					String reportName = userReportVO.getName().getString();
					createLabelAndReadOnlyText(summaryComposite, ReportLabel.userReport, reportName);
				}
				else if (
					ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL.equals(constraintType) ||
					ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL_CONTINGENT.equals(constraintType)
				) {
					String eventName = addRightWizard.getEventVO().getName(Locale.getDefault());

					createLabelAndReadOnlyText(summaryComposite, ParticipantLabel.Event, eventName);

					HotelContingentCVO hotelContingentCVO = addRightWizard.getHotelContingentCVO();
					constraintPK = hotelContingentCVO.getPK();
					
					String hotelContingentName = hotelContingentCVO.getHcName();
					createLabelAndReadOnlyText(summaryComposite, HotelLabel.HotelContingent, hotelContingentName);
				}
				else if (ACLObjectDefinitions.CONSTRAINT_TYPE_PROGRAMME_POINT.equals(constraintType)) {
					String eventName = addRightWizard.getEventVO().getName(Locale.getDefault());
					createLabelAndReadOnlyText(summaryComposite, ParticipantLabel.Event, eventName);

					ProgrammePointVO programmePointVO = addRightWizard.getProgrammePointVO();
					constraintPK = programmePointVO.getPK();
					
					String programmePointName = programmePointVO.getName().getString();
					createLabelAndReadOnlyText(summaryComposite, ParticipantLabel.ProgrammePoint, programmePointName);
				}
			}
			
			new Label(summaryComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			SWTHelper.createLabel(summaryComposite, UsersI18N.Priority);
			prioritySpinner = new NullableSpinner(summaryComposite, SWT.BORDER);
			prioritySpinner.setMaximum(9);
			prioritySpinner.setMinimum(0);
			prioritySpinner.setValue(4);
		}
		summaryComposite.layout();
		super.setVisible(visible);
	}
	
	public AccessControlEntryVO createAccessControlEntryVO() {
		AccessControlEntryVO aceVO = new AccessControlEntryVO();
		
		aceVO.setRead(readButton.getValue());
		aceVO.setWrite(writeButton.getValue());
		aceVO.setCreate(createButton.getValue());
		aceVO.setDelete(deleteButton.getValue());
		aceVO.setACLObject(aclObject);
		aceVO.setConstraintType(constraintType);
		aceVO.setPriority(prioritySpinner.getValueAsInteger());
		aceVO.setConstraintKey(constraintPK);
		aceVO.setSubject(addRightWizard.getOwner());
		aceVO.setDisabled(false);
		
		return aceVO;
	}
}
