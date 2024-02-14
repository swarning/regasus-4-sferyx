package de.regasus.portal.portal.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.EntityProvider;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.participant.AbstractParticipantTypeProvider;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.participant.type.ChooseParticipantTypesComposite;
import de.regasus.portal.Portal;
import de.regasus.ui.Activator;


public class PortalParticipantTypeComposite extends Composite {

	private Portal portal;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// models
	private ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();

	// widgets
	private ChooseParticipantTypesComposite accessibleChooseParticipantTypesComposite;
	private ChooseParticipantTypesComposite mainChooseParticipantTypesComposite;
	private ChooseParticipantTypesComposite companionChooseParticipantTypesComposite;
	private ChooseParticipantTypesComposite allowCompanionChooseParticipantTypesComposite;


	// ******************************************************************************************
	// * CacheModelListeners
	// *

	/**
	 * Observer for the Participant Types of the Event.
	 * Though {@link ChooseParticipantTypesComposite} observes the {@link ParticipantTypeModel}, it does not notice
	 * when a Participant Type is added or removed to an Event, because it does not know the source of available
	 * Participant Types. This is the job of its {@link ParticipantTypeProvider}. Because the
	 * {@link ParticipantTypeProvider} is implemented here as well, it is our job to observe the
	 * relation between Participant Type and Event.
	 */
	private CacheModelListener<Long> participantTypeModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			/* This if-statement is necessary, because ChooseParticipantTypesComposite requests data from
			 * ParticipantTypeModel during its initialization, which causes a refresh. But at this point
			 * of time the Composites are still null.
			 */
			if (accessibleChooseParticipantTypesComposite != null) {
				accessibleChooseParticipantTypesComposite.initAvailableEntities();
			}
			if (mainChooseParticipantTypesComposite != null) {
				mainChooseParticipantTypesComposite.initAvailableEntities();
			}

			if (companionChooseParticipantTypesComposite != null) {
				companionChooseParticipantTypesComposite.initAvailableEntities();
			}

			if (allowCompanionChooseParticipantTypesComposite != null) {
				allowCompanionChooseParticipantTypesComposite.initAvailableEntities();
			}
		}
	};

	// *
	// * CacheModelListeners
	// ******************************************************************************************


	private EntityProvider<ParticipantType> participantTypeProvider = new AbstractParticipantTypeProvider() {
		@Override
		public List<ParticipantType> getEntityList() {
			List<ParticipantType> participantTypes = Collections.emptyList();
			try {
				if (portal != null) {
					participantTypes = participantTypeModel.getParticipantTypesByEvent( portal.getEventId() );
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			return participantTypes;
		}
	};


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent event) {
			if (participantTypeModel != null && portal != null && portal.getEventId() != null) {
				try {
					participantTypeModel.removeForeignKeyListener(participantTypeModelListener, portal.getEventId());
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
	};


	public PortalParticipantTypeComposite(Composite parent, int style, Portal portal) {
		super(parent, style);

		this.portal = Objects.requireNonNull(portal);

		addDisposeListener(disposeListener);

		createWidgets();

		syncWidgetsToEntity();

		participantTypeModel.addForeignKeyListener(participantTypeModelListener, portal.getEventId());
	}


	private void createWidgets() {
		try {
			setLayout(new GridLayout(1, false));

			SashForm sashForm = new SashForm(this, SWT.VERTICAL);
			sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Composite firstComposite = new Composite(sashForm, SWT.NONE);
			Composite secondComposite = new Composite(sashForm, SWT.NONE);
			Composite thirdComposite = new Composite(sashForm, SWT.NONE);
			Composite fourthComposite = new Composite(sashForm, SWT.NONE);

			sashForm.setWeights(new int[] { 1, 1, 1, 1 });

			createAccessibleParticipantTypesArea(firstComposite);

			createMainParticipantTypesArea(secondComposite);

			if ( portal.getPortalConfig().isWithCompanions() ) {
				createCompanionParticipantTypesArea(thirdComposite);
				createAllowCompanionForParticipantTypesArea(fourthComposite);
			}

			registerListener();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void createAccessibleParticipantTypesArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		accessibleChooseParticipantTypesComposite = new ChooseParticipantTypesComposite(
			parent,
			participantTypeProvider,
			SWT.NONE
		);

		accessibleChooseParticipantTypesComposite.setChosenEntitiesLabel(I18N.PortalEditor_ParticipantTypesAccessible);
	}


	private void createMainParticipantTypesArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		mainChooseParticipantTypesComposite = new ChooseParticipantTypesComposite(
			parent,
			participantTypeProvider,
			SWT.NONE
		);

		mainChooseParticipantTypesComposite.setChosenEntitiesLabel(I18N.PortalEditor_ParticipantTypesForMainParticipant);
	}


	private void createCompanionParticipantTypesArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		companionChooseParticipantTypesComposite = new ChooseParticipantTypesComposite(
			parent,
			participantTypeProvider,
			SWT.NONE
		);

		companionChooseParticipantTypesComposite.setChosenEntitiesLabel(I18N.PortalEditor_ParticipantTypesForCompanions);
	}


	private void createAllowCompanionForParticipantTypesArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		allowCompanionChooseParticipantTypesComposite = new ChooseParticipantTypesComposite(
			parent,
			participantTypeProvider,
			SWT.NONE
		);

		allowCompanionChooseParticipantTypesComposite.setChosenEntitiesLabel(I18N.PortalEditor_ParticipantTypesWithCompanions);
	}


	private void registerListener() {
		accessibleChooseParticipantTypesComposite.addModifyListener(modifySupport);

		mainChooseParticipantTypesComposite.addModifyListener(modifySupport);

		if (companionChooseParticipantTypesComposite != null) {
			companionChooseParticipantTypesComposite.addModifyListener(modifySupport);
		}

		if (allowCompanionChooseParticipantTypesComposite != null) {
			allowCompanionChooseParticipantTypesComposite.addModifyListener(modifySupport);
		}
	}


	public List<ParticipantType> getAccessibleParticipantTypeList() {
		return accessibleChooseParticipantTypesComposite.getChosenEntities();
	}


	public List<ParticipantType> getMainParticipantTypeList() {
		return mainChooseParticipantTypesComposite.getChosenEntities();
	}


	public List<ParticipantType> getCompanionParticipantTypeList() {
		if (companionChooseParticipantTypesComposite != null) {
			return companionChooseParticipantTypesComposite.getChosenEntities();
		}
		return Collections.emptyList();
	}


	public List<ParticipantType> getAllowCompanionForParticipantTypeList() {
		if (allowCompanionChooseParticipantTypesComposite != null) {
			return allowCompanionChooseParticipantTypesComposite.getChosenEntities();
		}
		return Collections.emptyList();
	}


	public void setPortal(Portal portal) {
		this.portal = portal;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (portal != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						accessibleChooseParticipantTypesComposite.setChosenIds( portal.getAccessibleParticipantTypeIds() );

						mainChooseParticipantTypesComposite.setChosenIds( portal.getParticipantTypeIds() );

						if (companionChooseParticipantTypesComposite != null) {
							companionChooseParticipantTypesComposite.setChosenIds( portal.getCompanionParticipantTypeIds() );
						}

						if (allowCompanionChooseParticipantTypesComposite != null) {
							allowCompanionChooseParticipantTypesComposite.setChosenIds( portal.getAllowCompanionForParticipantTypeIds() );
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
		syncAccessibleParticipantTypes();
		syncMainParticipantTypes();
		syncCompanionParticipantTypes();
		syncAllowCompanionForParticipantTypes();
	}


	private void syncAccessibleParticipantTypes() {
		// sync accessible Participant Types
		accessibleChooseParticipantTypesComposite.syncEntityToWidgets();
		List<Long> participantTypePKs = new ArrayList<>();
		List<ParticipantType> participantTypes = getAccessibleParticipantTypeList();
		for (ParticipantType participantType : participantTypes) {
			participantTypePKs.add( participantType.getId() );
		}
		portal.setAccessibleParticipantTypeIds(participantTypePKs);
	}


	private void syncMainParticipantTypes() {
		// sync Participant Types for the main Participant
		mainChooseParticipantTypesComposite.syncEntityToWidgets();
		List<Long> participantTypePKs = new ArrayList<>();
		List<ParticipantType> participantTypes = getMainParticipantTypeList();
		for (ParticipantType participantType : participantTypes) {
			participantTypePKs.add( participantType.getId() );
		}
		portal.setParticipantTypeIds(participantTypePKs);
	}


	private void syncCompanionParticipantTypes() {
		// sync Participant Types for companions
		if (companionChooseParticipantTypesComposite != null) {
    		companionChooseParticipantTypesComposite.syncEntityToWidgets();
    		List<Long> participantTypePKs = new ArrayList<>();
    		List<ParticipantType> participantTypes = getCompanionParticipantTypeList();
    		for (ParticipantType participantType : participantTypes) {
    			participantTypePKs.add( participantType.getId() );
    		}
    		portal.setCompanionParticipantTypeIds(participantTypePKs);
		}
	}


	private void syncAllowCompanionForParticipantTypes() {
		// sync Participant Types for companions
		if (allowCompanionChooseParticipantTypesComposite != null) {
    		allowCompanionChooseParticipantTypesComposite.syncEntityToWidgets();
    		List<Long> participantTypePKs = new ArrayList<>();
    		List<ParticipantType> participantTypes = getAllowCompanionForParticipantTypeList();
    		for (ParticipantType participantType : participantTypes) {
    			participantTypePKs.add( participantType.getId() );
    		}
    		portal.setAllowCompanionForParticipantTypeIds(participantTypePKs);
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

}
