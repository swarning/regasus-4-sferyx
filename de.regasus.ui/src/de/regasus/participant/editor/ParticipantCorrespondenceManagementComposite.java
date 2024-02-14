package de.regasus.participant.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CorrespondenceComparator;
import com.lambdalogic.messeinfo.contact.CorrespondenceType;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.participant.ParticipantCorrespondence;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.composite.AbstractCorrespondenceManagementComposite;
import de.regasus.common.composite.CorrespondenceComposite;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantCorrespondenceModel;
import de.regasus.ui.Activator;

public class ParticipantCorrespondenceManagementComposite
extends AbstractCorrespondenceManagementComposite<ParticipantCorrespondence> {

	private ParticipantCorrespondenceModel correspondenceModel;

	private Long participantID;

	// the entity
	private ArrayList<ParticipantCorrespondence> correspondenceList;


	private CacheModelListener<Long> modelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (event.getSource() == correspondenceModel && isInitialized() ) {
				refreshData();
			}
		}
	};


	/**
	 * Create the composite. It shows scroll bars when the space is not enough
	 * for all the participant correspondences.
	 *
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ParticipantCorrespondenceManagementComposite(final Composite tabFolder, int style) {
		super(tabFolder, style);

		correspondenceModel = ParticipantCorrespondenceModel.getInstance();

		correspondenceList = CollectionsHelper.createArrayList();


		// stop observing CorrespondenceModel when getting disposed
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (participantID != null) {
					correspondenceModel.removeForeignKeyListener(
						modelListener,
						participantID
					);
				}
			}
		});

	}


	@Override
	public void refreshData() {
		loadData();
		syncWidgetsToEntity();
	}


	private void loadData() {
		try {
			correspondenceList.clear();

			if (participantID != null) {
    			List<ParticipantCorrespondence> modelCorrespondenceList =
    				correspondenceModel.getCorrespondenceListByParticipantId(participantID);

    			correspondenceList.ensureCapacity(modelCorrespondenceList.size());

    			for (ParticipantCorrespondence correspondence : modelCorrespondenceList) {
    				correspondenceList.add(correspondence.clone());
    			}

    			Collections.sort(correspondenceList, CorrespondenceComparator.getInstance());
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void syncWidgetsToEntity() {
		if (participantID != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						// get sub-entity list
						if (participantID != null) {
							// set number of necessary Composites
							compositeListSupport.setSize(correspondenceList.size());

							// set n sub-entities to n sub-Composites
							for (int i = 0; i < correspondenceList.size(); i++) {
								// set sub-entity to sub-Composite
								compositeListSupport.getComposite(i).setCorrespondence( correspondenceList.get(i) );
							}
						}
						else {
							// set number of necessary Composites
							compositeListSupport.setSize(0);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		// do nothing
	}


	public void doSave() throws Exception {
		if (isInitialized()) {
			try {
				for (CorrespondenceComposite<ParticipantCorrespondence> correspondenceComposite : compositeListSupport.getCompositeList()) {
	    			if (correspondenceComposite.isModified()) {
	    				// get ParticipantCorrespondence from current Composite
	    				ParticipantCorrespondence correspondence = correspondenceComposite.getCorrespondence();

	    				/* After creating or updating the correspondence, its new version is set directly
	    				 * to the correspondenceComposite, because the correspondenceComposite does not
	    				 * listen to the CorrespondenceModel.
	    				 */
	    				if (correspondence.getId() == null) {
	    					// create correspondence via Model
	    					ParticipantCorrespondence createdCorrespondence = correspondenceModel.create(correspondence);

	    					// set new version of ParticipantCorrespondence to Composite
	    					correspondenceComposite.setCorrespondence(createdCorrespondence.clone());
	    				}
	    				else {
	    					// update correspondence via Model
	    					ParticipantCorrespondence updatedCorrespondence = correspondenceModel.update(correspondence);

	    					// set new version of ParticipantCorrespondence to Composite
	    					correspondenceComposite.setCorrespondence(updatedCorrespondence.clone());
	    				}
	    			}
	    		}

				// reload the whole List of Correspondences to get those that could have been created anywhere else
				correspondenceModel.refreshForeignKey(participantID);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void validate() throws Exception {
		InvalidValuesException ive = new InvalidValuesException();

		for (CorrespondenceComposite<ParticipantCorrespondence> correspondenceComposite : compositeListSupport.getCompositeList()) {
			ParticipantCorrespondence correspondence = correspondenceComposite.getCorrespondence();
			try {
				correspondence.validate();
			}
			catch(InvalidValuesException e) {
				ive.add(e);
			}
		}

		if (ive.count() > 0) {
			throw ive;
		}
	}


	@Override
	protected ParticipantCorrespondence createEntity() throws Exception {
		ParticipantCorrespondence correspondence = new ParticipantCorrespondence();
		correspondence.setCorrespondenceTime(new Date());
		correspondence.setType(CorrespondenceType.Other);
		correspondence.setNewUser(ServerModel.getInstance().getModelData().getUser());
		correspondence.setParticipantId(participantID);
		return correspondence;
	}


	public Long getParticipantID() {
		return participantID;
	}


	public void setParticipantID(Long participantID) {
		if (this.participantID != null) {
			correspondenceModel.removeForeignKeyListener(modelListener, this.participantID);
		}

		this.participantID = participantID;

		if (participantID != null) {
			correspondenceModel.addForeignKeyListener(modelListener, participantID);
		}

		/* Do not load the data here, because this Composite could not be initialized and loading the List of
		 * ParticipantCorrespondence could lead to a server call.
		 * The data is loaded in syncWidgetsToEntity() instead.
		 */
	}

}
