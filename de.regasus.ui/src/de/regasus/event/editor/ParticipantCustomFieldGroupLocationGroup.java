package de.regasus.event.editor;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.PropertyModel;


public class ParticipantCustomFieldGroupLocationGroup extends Group {

	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	/*** Widgets ***/
	private List<Button> buttonList = createArrayList(ParticipantCustomFieldGroupLocation.values().length);


	public ParticipantCustomFieldGroupLocationGroup(Composite parent, int style) {
		super(parent, style);

		RowLayout layout = new RowLayout();
		setLayout(layout);

		setText(ParticipantLabel.ParticipantCustomFieldGroupLocation.getString());


		// create 1 Button for every enum item
		for (ParticipantCustomFieldGroupLocation location : ParticipantCustomFieldGroupLocation.values()) {
			Button button = new Button(this, SWT.RADIO);
			button.setText(location.getString());
			
			buttonList.add(button);
			
			button.addSelectionListener(modifySupport);
		}
	}



	// **************************************************************************
	// * Modify support
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modify support
	// **************************************************************************


	public ParticipantCustomFieldGroupLocation getValue() {
		ParticipantCustomFieldGroupLocation value = null;

		int i = 0;
		for (Button button : buttonList) {
			if (button.getSelection()) {
				value = ParticipantCustomFieldGroupLocation.values()[i];
				break;
			}
			i++;
		}

		return value;
	}


	public void setValue(ParticipantCustomFieldGroupLocation value) {
		int ordinal = -1;
		if (value != null) {
			ordinal = value.ordinal();
		}

		int i = 0;
		for (Button button : buttonList) {
			button.setSelection(i++ == ordinal);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
