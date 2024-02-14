package de.regasus.profile.role.combo;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileRoleModel;


@SuppressWarnings("rawtypes")
public class ProfileRoleCombo extends AbstractComboComposite<ProfileRole> implements CacheModelListener {

	// Model
	private ProfileRoleModel model;


	public ProfileRoleCombo(Composite parent, int style) throws Exception {
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

				ProfileRole profileRole = (ProfileRole) element;
				return avoidNull(profileRole.getName());
			}
		};

		return result;
	}


	@Override
	protected Collection<ProfileRole> getModelData() throws Exception {
		Collection<ProfileRole> modelData = model.getAllProfileRoles();
		return modelData;
	}


	@Override
	protected void initModel() {
		model = ProfileRoleModel.getInstance();
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


	public Long getProfileRoleId() {
		Long profileRoleId = null;
		if (entity != null) {
			profileRoleId = entity.getID();
		}

		return profileRoleId;
	}


	public void setProfileRoleId(Long profileRoleId) {
		try {
			ProfileRole profileRole = null;
			if (profileRoleId != null) {
				profileRole = model.getProfileRole(profileRoleId);
				if (profileRole == null) {
					throw new EntityNotFoundException("ProfileRole", profileRoleId);
				}
			}
			setEntity(profileRole);
		}
		catch (EntityNotFoundException e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
