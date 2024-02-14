package de.regasus.portal.combo;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;


@SuppressWarnings("rawtypes")
public class PortalParticipantTypeCombo extends AbstractComboComposite<ParticipantType> {

	// Model
	protected PortalModel portalModel;
	protected ParticipantTypeModel participantTypeModel;
	/**
	 * Do not initialize Listener here, because it will be used in initModel() which is called from super constructor.
	 * Therefore fields have not been initialized yet!
	 */
	private CacheModelListener<Long> participantTypeModelListener;

	/**
	 * PK of the Portal whose ParticipantTypes shall be presented.
	 */
	protected Long portalId;


	public PortalParticipantTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	private CacheModelListener<Long> portalModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent event) {
			try {
				handleModelChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				ParticipantType participantType = (ParticipantType) element;
				return LanguageString.toStringAvoidNull(participantType.getName());
			}
		};
	}


	@Override
	protected Collection<ParticipantType> getModelData() throws Exception {
		List<ParticipantType> modelData = null;
		if (portalId != null) {
			Portal portal = portalModel.getPortal(portalId);
			List<Long> participantTypeIds = portal.getParticipantTypeIds();
			modelData = participantTypeModel.getParticipantTypes(participantTypeIds);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		portalModel = PortalModel.getInstance();
		// do not observe the models now, because we don't know the Portal  yet

		participantTypeModel = ParticipantTypeModel.getInstance();

		/* Initialize Listener right here, because initModel() is called from super constructor.
		 * Therefore fields have not been initialized yet!
		 */
		participantTypeModelListener = e -> handleModelChange();
		participantTypeModel.addListener(participantTypeModelListener);
	}


	@Override
	protected void disposeModel() {
		portalModel.removeListener(portalModelListener);
		participantTypeModel.removeListener(participantTypeModelListener);
	}


	public Long getParticipantTypePK() {
		Long id = null;
		if (entity != null) {
			id = entity.getId();
		}
		return id;
	}


	public void setParticipantTypePK(Long participantTypePK) {
		try {
			ParticipantType participantType = null;
			if (participantTypePK != null) {
				participantType = participantTypeModel.getParticipantType(participantTypePK);
			}
			setEntity(participantType);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public Long getPortalId() {
		return portalId;
	}


	public void setPortalId(Long portalId) throws Exception {
		if ( ! EqualsHelper.isEqual(this.portalId, portalId)) {
			Long oldPortalId = this.portalId;
			this.portalId = portalId;

			// save old selection
			final ISelection selection = comboViewer.getSelection();
			entity = null;

			portalModel.removeListener(portalModelListener, oldPortalId);

			// register at the model before getting its data, so the data will be put to the models cache
			portalModel.addListener(portalModelListener, portalId);

			// update combo
			handleModelChange();


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);
						comboViewer.setSelection(selection);
						entity = getEntityFromComboViewer();
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}

}
