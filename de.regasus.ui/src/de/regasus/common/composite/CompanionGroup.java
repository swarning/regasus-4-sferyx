package de.regasus.common.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.person.PersonTreeModel;
import de.regasus.ui.Activator;

public class CompanionGroup 
extends Group 
implements CacheModelListener<Long>, DisposeListener  {

	// The Entity
	private Participant participant;
	
	// Widgets
	private List<CompanionRow> companionRows = new ArrayList<CompanionRow>();
	private ModifySupport modifySupport = new ModifySupport(this);
	private ModifySupport layoutModifyListenerConnector = new ModifySupport(this);

	// Models
	private PersonTreeModel personTreeModel = PersonTreeModel.getInstance();
	private ParticipantModel participantModel = ParticipantModel.getInstance();
	
	// Work Data
	private List<Participant> companionList;
	private Participant secondPerson;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public CompanionGroup(Composite parent, int style) {
		super(parent, style);
	
		addDisposeListener(this);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (participant != null) {
			Long rootPK = participant.getRootPK();
			participantModel.removeForeignKeyListener(this, rootPK);
		}
	}

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

	// **************************************************************************
	// * Modifying Layout
	// *

	public void addLayoutModifyListener(ModifyListener modifyListener) {
		layoutModifyListenerConnector.addListener(modifyListener);
	}

	
	public void removeLayoutModifyListener(ModifyListener modifyListener) {
		layoutModifyListenerConnector.removeListener(modifyListener);
	}

	
	private void fireLayoutModifyEvent() {
		layoutModifyListenerConnector.fire();
	}
	
	// *
	// * Modifying Layout
	// **************************************************************************

	
	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			if (button.getSelection()) {
				secondPerson = (Participant) button.getData();
			}
			else {
				secondPerson = null;
			}

			modifySupport.fire();

			updateRows();
		}
	};

	
	private void syncWidgetsToEntity() {
		if (participant != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						Long mainParticipantPK = participant.getPK();
						if (mainParticipantPK != null) {
							companionList = personTreeModel.getCompanions(mainParticipantPK);

							// Make sure there are as much rows as second person candidates
							int companionCount = companionList.size();
							while (companionRows.size() < companionCount) {
								CompanionRow companionRow = new CompanionRow(CompanionGroup.this);
								companionRow.addSelectionListener(selectionListener);
								companionRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
								companionRows.add(companionRow);
							}
							while (companionRows.size() > companionCount) {
								CompanionRow row = companionRows.remove(companionRows.size() - 1);
								row.dispose();
							}
							
							// set companion data to rows
							for (int i = 0; i < companionList.size(); i++) {
								companionRows.get(i).setCompanion(companionList.get(i));
							}
							
							updateRows();
							
							fireLayoutModifyEvent();
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}
	
	
	public void syncEntityToWidgets() {
		if (participant != null ) {
			if (secondPerson != null) {
				participant.setSecondPersonID(secondPerson.getID());
			}
			else {
				participant.setSecondPersonID(null);
			}
		}
	}

	
	public void setParticipant(Participant participant) {
		// Check if the participant's root changed and remove/add listeners
		
		// get old rootPK
		Long oldRootPK = null;
		if (this.participant != null) {
			oldRootPK = this.participant.getRootPK();
		}

		// get new rootPK
		Long newRootPK = null;
		if (participant != null) {
			newRootPK = participant.getRootPK();
		}

		// if there was an old rootPK but it changed, stop observing it
		if (oldRootPK != null && !oldRootPK.equals(newRootPK)) {
			participantModel.removeForeignKeyListener(this, oldRootPK);
		}

		// if there is a new rootPK and it is not the old one, start observing it
		if (newRootPK != null && !newRootPK.equals(oldRootPK)) {
			participantModel.addForeignKeyListener(this, newRootPK);
		}


		this.participant = participant;
		this.secondPerson = null;
		
		if (participant.getSecondPersonID() != null) {
			try {
				secondPerson = participantModel.getParticipant(participant.getSecondPersonID());	
			}
			catch(Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
		
		
		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	
	private void updateRows() {
		for (CompanionRow secondPersonRow : companionRows) {
			secondPersonRow.updateForSecondPerson(secondPerson);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! isDisposed()) {
    			/* We have to react on all keys, because we are a foreignKeyListener for the rootPK.
    			 * Therefore we don't know the keys in the event, because they are not the rootPKs.
    			 */
    			syncWidgetsToEntity();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

	
	public boolean isEmpty() {
		return companionRows.isEmpty();
	}
	
}
