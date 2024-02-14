package de.regasus.profile.relationtype.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileRelationTypeModel;


@SuppressWarnings("rawtypes")
public class ProfileRelationTypeCombo
extends AbstractComboComposite<ProfileRelationType>
implements CacheModelListener {

	// Model
	private ProfileRelationTypeModel model;


	public ProfileRelationTypeCombo(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		LabelProvider result = new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				ProfileRelationType profileRelationType = (ProfileRelationType) element;
				return LanguageString.toStringAvoidNull(profileRelationType.getName());
			}
		};

		return result;
	}


	@Override
	protected Collection<ProfileRelationType> getModelData() throws Exception {
		Collection<ProfileRelationType> modelData = model.getAllProfileRelationTypes();
		return modelData;
	}


	@Override
	protected void initModel() {
		model = ProfileRelationTypeModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getProfileReleationTypeID() {
		Long profileRelationTypeID = null;
		if (entity != null) {
			profileRelationTypeID = entity.getID();
		}

		return profileRelationTypeID;
	}


	public void setProfileRelationTypeID(Long profileRelationTypeID) {
		try {
			ProfileRelationType profileRelationType = null;
			if (profileRelationTypeID != null) {
				profileRelationType = model.getProfileRelationType(profileRelationTypeID);
				if (profileRelationType == null) {
					throw new EntityNotFoundException("ProfileRelationType", profileRelationTypeID);
				}
			}
			setEntity(profileRelationType);
		}
		catch (EntityNotFoundException e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
