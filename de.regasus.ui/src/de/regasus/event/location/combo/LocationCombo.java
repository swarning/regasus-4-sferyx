package de.regasus.event.location.combo;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.LocationModel;
import de.regasus.ui.Activator;


@SuppressWarnings("rawtypes")
public class LocationCombo
extends AbstractComboComposite<LocationVO>
implements CacheModelListener {

	// Model
	private LocationModel model;
	
	
	/**
	 * Long of the Event which ParticipantTypes are hold by this model.
	 */
	private Long eventPK;

	
	public LocationCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				LocationVO locationVO = (LocationVO) element;
				return StringHelper.avoidNull(locationVO.getName());
			}
		};
	}
	
	
	protected Collection<LocationVO> getModelData() throws Exception {
		List<LocationVO> modelData = null;
		if (model != null) {
			modelData = model.getLocationVOsByEventPK(eventPK);
		}
		return modelData;
	}
	
	
	protected void initModel() {
		// do nothing because we don't know the event yet
	}
	
	
	protected void disposeModel() {
		if (model != null) {
			model.removeForeignKeyListener(this, eventPK);
		}
	}

	
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public Long getLocationPK() {
		Long locationPK = null;
		if (entity != null) {
			locationPK = entity.getPK();
		}
		return locationPK;
	}

	
	public void setLocationPK(Long locationPK) {
		try {
			LocationVO locationVO = null;
			if (model != null && locationPK != null) {
				locationVO = model.getLocationVO(locationPK);
			}
			setEntity(locationVO);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

	
	public Long getEventPK() {
		return eventPK;
	}

	
	public void setEventPK(Long eventPK) throws Exception {
		if ( ! EqualsHelper.isEqual(this.eventPK, eventPK)) {
			this.eventPK = eventPK;
			
			// alte Selection merken
			final ISelection selection = comboViewer.getSelection();
			entity = null;
			
			// neues Model holen und registrieren
			try {
				if (model != null) {
					model.removeForeignKeyListener(this, eventPK);
				}
				else {
					model = LocationModel.getInstance();
				}
				model.addForeignKeyListener(this, eventPK);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
			
			
			// combo aktualisieren
			handleModelChange();
						
			
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						modifySupport.setEnabled(false);
						comboViewer.setSelection(selection);
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
