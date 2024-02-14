package de.regasus.finance.invoice.combo;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.ReminderState;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;


public class ReminderStateCombo extends AbstractComboComposite<ReminderState> {

	protected List<ReminderState> reminderStates = null;
	
	
	/**
	 * Create ReminderStateCombo with all {@link ReminderStateType}s.
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ReminderStateCombo(Composite parent, int style) throws Exception {
		super(parent, style);
	}

	
	/**
	 * Create ReminderStateCombo with defined {@link ReminderStateType}s.
	 * @param parent
	 * @param style
	 * @param reminderStates
	 * @throws Exception
	 */
	public ReminderStateCombo(Composite parent, int style, List<ReminderState> reminderStates)
	throws Exception {
		/* This is a misuse of the modelDataDiscriminator,
		 * but it works.
		 * See getModelData() also.
		 */
		super(parent, style, reminderStates);
	}
	

	@Override
	protected void disposeModel() {
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {

				ReminderState type = (ReminderState) element;
				return type.getString();
			}
		};
	}


	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ReminderState> getModelData() throws Exception {
		if (reminderStates == null) {
			if (modelDataDiscriminator != null) {
				reminderStates = (List<ReminderState>) modelDataDiscriminator;
			}
			else {
				reminderStates = createArrayList(ReminderState.values());
				
				// do not sort, keep original order
			}
		}
		
		return reminderStates;
	}


	@Override
	protected void initModel() {
	}

}
