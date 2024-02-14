package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigLabel;
import com.lambdalogic.messeinfo.config.parameter.SalutationConfigParameter;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class SalutationConfigGroup extends Group {

	// the entity
	private SalutationConfigParameter salutationConfigParameter;


	// Widgets
	private BooleanConfigWidgets ignoreSimpleDregreeWidgets;
	private BooleanConfigWidgets writeOutProfWidgets;
	private BooleanConfigWidgets writeOutDrWidgets;


	public SalutationConfigGroup(
		Composite parent,
		int style
	) {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout(BooleanConfigWidgets.NUM_COLS, false);
		setLayout(gridLayout);
		setText( Person.SALUTATION.getString() );


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
		ignoreSimpleDregreeWidgets.addModifyListener(modifyListener);
		writeOutProfWidgets.addModifyListener(modifyListener);
		writeOutDrWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (salutationConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						ignoreSimpleDregreeWidgets.setValue( salutationConfigParameter.getIgnoreSimpleDregree() );
						writeOutProfWidgets       .setValue( salutationConfigParameter.getWriteOutProf() );
						writeOutDrWidgets         .setValue( salutationConfigParameter.getWriteOutDr() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (salutationConfigParameter != null) {
			salutationConfigParameter.setIgnoreSimpleDregree(ignoreSimpleDregreeWidgets.getValue());
			salutationConfigParameter.setWriteOutProf(writeOutProfWidgets.getValue());
			salutationConfigParameter.setWriteOutDr(writeOutDrWidgets.getValue());
		}
	}


	public void setSalutationConfigParameter(SalutationConfigParameter salutationConfigParameter) {
		this.salutationConfigParameter = salutationConfigParameter;

		// syncWidgetsToEntity() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
