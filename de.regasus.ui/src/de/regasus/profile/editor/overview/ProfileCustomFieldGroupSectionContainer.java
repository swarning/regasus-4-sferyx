/**
 * ProfileCustomFieldGroupSectionContainer.java
 * created on 18.07.2013 17:06:46
 */
package de.regasus.profile.editor.overview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.contact.CustomFieldFormatter;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldValue;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class ProfileCustomFieldGroupSectionContainer
extends AbstractSectionContainer 
implements CacheModelListener<Long>, DisposeListener {
	
	private Long profileID;
	
	private ProfileModel profileModel;
	private ProfileCustomFieldModel customFieldModel;
	
	private ProfileCustomFieldGroup group;
	
	private ProfileConfigParameterSet profileConfigParameterSet;
	
	private boolean ignoreCacheModelEvents = false;
	
		
	public ProfileCustomFieldGroupSectionContainer(
		FormToolkit formToolkit, 
		Composite body, 
		Long profileID,
		ProfileCustomFieldGroup group,
		ProfileConfigParameterSet profileConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);
		
		this.profileID = profileID;
		this.group = group;
		this.profileConfigParameterSet = profileConfigParameterSet;

		addDisposeListener(this);
		
		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this, profileID);
		
		customFieldModel = ProfileCustomFieldModel.getInstance();
		
		refreshSection();
	}

	
	@Override
	protected String getTitle() {
		String title = null;
		if (group == null) {
			title = ProfileLabel.ProfileCustomFields.getString();
		}
		else {
			title = group.getName().getString();
		}
		return title;
	}
	
	
	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

			ArrayList<Tuple<String, String>> labelValueTuples = null;
			
			boolean visible = 
				profileConfigParameterSet == null || 
				profileConfigParameterSet.isVisible();
			
			if (visible) {
				Profile profile = profileModel.getExtendedProfile(profileID);
				
				List<ProfileCustomField> customFields;
				if (group == null) {
					customFields = customFieldModel.getProfileCustomFieldsByGroup(null);
				}
				else {
					customFields = customFieldModel.getProfileCustomFieldsByGroup(group.getID());
				}
				
				// First collect the fields and values to show, if any
				labelValueTuples = new ArrayList<Tuple<String, String>>(customFields.size());
				
				for (ProfileCustomField customField : customFields) {
					ProfileCustomFieldValue customFieldValue = profile.getCustomFieldValue(customField.getID());
					if (customFieldValue != null) {
						String value = CustomFieldFormatter.format(
							customField, 
							customFieldValue
						);
						
						if (StringHelper.isNotEmpty(value)) {
							Tuple<String, String> valueLabelTuple = new Tuple<String, String>(
								customField.getLabelOrName(), 
								value
							);
							labelValueTuples.add(valueLabelTuple);
						}
					}
				}
				
				visible = ! labelValueTuples.isEmpty();
			}
			
			setVisible(visible);
			if (visible) {
				for (Tuple<String, String> tuple : labelValueTuples) {
					addIfNotEmpty(tuple.getA(), tuple.getB());
				}
			}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}
	

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (profileModel != null && profileID != null) {
			try {
				profileModel.removeListener(this, profileID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
