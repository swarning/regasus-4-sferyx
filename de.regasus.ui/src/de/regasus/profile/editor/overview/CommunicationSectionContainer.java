package de.regasus.profile.editor.overview;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.CommunicationConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class CommunicationSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long profileID;

	private ProfileModel profileModel;

	private CommunicationConfigParameterSet commConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public CommunicationSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long profileID,
		CommunicationConfigParameterSet commConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.profileID = profileID;
		this.commConfigParameterSet = commConfigParameterSet;

		addDisposeListener(this);

		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this, profileID);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return AbstractPerson.COMMUNICATION.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

			Profile profile = profileModel.getProfile(profileID);
    		Communication communication = profile.getCommunication();

    		boolean visible =
    			(commConfigParameterSet == null || commConfigParameterSet.isVisible()) &&
    			communication != null &&
    			!communication.isEmpty();

    		setVisible(visible);

    		if (visible) {
    			if (commConfigParameterSet != null && commConfigParameterSet.getPhone1().isVisible()) {
    				addIfNotEmpty(Communication.PHONE1.getString(), communication.getPhone1());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getMobile1().isVisible()) {
    				addIfNotEmpty(Communication.MOBILE1.getString(), communication.getMobile1());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getFax1().isVisible()) {
    				addIfNotEmpty(Communication.FAX1.getString(), communication.getFax1());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getEmail1().isVisible()) {
    				addIfNotEmpty(Communication.EMAIL1.getString(), communication.getEmail1());
    			}


    			if (commConfigParameterSet != null && commConfigParameterSet.getPhone2().isVisible()) {
    				addIfNotEmpty(Communication.PHONE2.getString(), communication.getPhone2());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getMobile2().isVisible()) {
    				addIfNotEmpty(Communication.MOBILE2.getString(), communication.getMobile2());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getFax2().isVisible()) {
    				addIfNotEmpty(Communication.FAX2.getString(), communication.getFax2());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getEmail2().isVisible()) {
    				addIfNotEmpty(Communication.EMAIL2.getString(), communication.getEmail2());
    			}

    			if (commConfigParameterSet != null && commConfigParameterSet.getPhone3().isVisible()) {
    				addIfNotEmpty(Communication.PHONE3.getString(), communication.getPhone3());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getMobile3().isVisible()) {
    				addIfNotEmpty(Communication.MOBILE3.getString(), communication.getMobile3());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getFax3().isVisible()) {
    				addIfNotEmpty(Communication.FAX3.getString(), communication.getFax3());
    			}
    			if (commConfigParameterSet != null && commConfigParameterSet.getEmail3().isVisible()) {
    				addIfNotEmpty(Communication.EMAIL3.getString(), communication.getEmail3());
    			}

    			if (commConfigParameterSet != null && commConfigParameterSet.getWww().isVisible()) {
    				addIfNotEmpty(Communication.WWW.getString(), communication.getWww());
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
			} catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
