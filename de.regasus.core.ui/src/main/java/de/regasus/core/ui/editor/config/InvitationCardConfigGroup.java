package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigLabel;
import com.lambdalogic.messeinfo.config.parameter.InvitationCardConfigParameter;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class InvitationCardConfigGroup extends Group {

	// the entity
	private InvitationCardConfigParameter invitationCardConfigParameter;


	// Widgets
	private BooleanConfigWidgets containsHerrFrauWidgets;
	private BooleanConfigWidgets ignoreSimpleDregreeWidgets;
	private BooleanConfigWidgets writeOutProfWidgets;
	private BooleanConfigWidgets writeOutDrWidgets;


	public InvitationCardConfigGroup(
		Composite parent,
		int style
	) {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout(BooleanConfigWidgets.NUM_COLS, false);
		setLayout(gridLayout);
		setText( Person.INVITATION_CARD.getString() );


		containsHerrFrauWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.ContainsHerrnFrauInInvitationCard_Label.getString(),
			ConfigLabel.ContainsHerrnFrauInInvitationCard_Description.getString()
		);
		ignoreSimpleDregreeWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.IgnoreSimpleDregreeInSalutation_Label.getString(),
			ConfigLabel.IgnoreSimpleDregreeInSalutation_Description.getString()
		);
		writeOutProfWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.WriteOutProfInSalutation_Label.getString(),
			ConfigLabel.WriteOutProfInSalutation_Description.getString()
		);
		writeOutDrWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.WriteOutDrInSalutation_Label.getString(),
			ConfigLabel.WriteOutDrInSalutation_Description.getString()
		);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		containsHerrFrauWidgets.addModifyListener(modifyListener);
		ignoreSimpleDregreeWidgets.addModifyListener(modifyListener);
		writeOutProfWidgets.addModifyListener(modifyListener);
		writeOutDrWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (invitationCardConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						containsHerrFrauWidgets.setValue(invitationCardConfigParameter.getContainsHerrFrau());
						ignoreSimpleDregreeWidgets.setValue(invitationCardConfigParameter.getIgnoreSimpleDregree());
						writeOutProfWidgets.setValue(invitationCardConfigParameter.getWriteOutProf());
						writeOutDrWidgets.setValue(invitationCardConfigParameter.getWriteOutDr());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (invitationCardConfigParameter != null) {
			invitationCardConfigParameter.setContainsHerrFrau(containsHerrFrauWidgets.getValue());
			invitationCardConfigParameter.setIgnoreSimpleDregree(ignoreSimpleDregreeWidgets.getValue());
			invitationCardConfigParameter.setWriteOutProf(writeOutProfWidgets.getValue());
			invitationCardConfigParameter.setWriteOutDr(writeOutDrWidgets.getValue());
		}
	}


	public void setInvitationCardConfigParameter(InvitationCardConfigParameter invitationCardConfigParameter) {
		this.invitationCardConfigParameter = invitationCardConfigParameter;

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
