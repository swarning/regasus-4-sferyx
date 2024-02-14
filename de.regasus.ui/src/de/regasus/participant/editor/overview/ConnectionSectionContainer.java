/**
 * ConnectionSectionContainer.java
 * created on 18.07.2013 11:26:55
 */
package de.regasus.participant.editor.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.ui.Activator;

public class ConnectionSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;
	private Long groupManagerPK;
	private Long companionOfPK;
	private Long personLink;

	private ParticipantModel participantModel;
	private PersonLinkModel personLinkModel;

	private ConfigParameterSet configParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public ConnectionSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.participantID = participantID;
		this.configParameterSet = configParameterSet;

		addDisposeListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);

		personLinkModel = PersonLinkModel.getInstance();


		refreshSection();
	}


	@Override
	protected String getTitle() {
		return I18N.ParticipantEditor_Connections;
	}


	@Override
	protected void createSectionElements() throws Exception {
		// remove listener
		if (groupManagerPK != null) {
			participantModel.removeListener(this, groupManagerPK);
		}
		if (companionOfPK != null) {
			participantModel.removeListener(this, companionOfPK);
		}
		if (personLink != null) {
			personLinkModel.removeListener(this, personLink);
		}

		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

    		// load data
    		Participant participant = participantModel.getParticipant(participantID);
    		personLink = participant.getPersonLink();

    		Long profileID = null;
    		if ((configParameterSet == null || configParameterSet.getProfile().isVisible()) &&
    			personLink != null
    		) {
    			PersonLinkData personLinkData = personLinkModel.getPersonLinkData(personLink);
    			if (personLinkData != null) {
    				profileID = personLinkData.getProfileID();
    			}

    			// Link to profile of participant if a personLink exists, even if there is no profile
       			personLinkModel.addListener(this, personLink);
    		}


    		// get reference PKs
    		groupManagerPK = participant.getGroupManagerPK();
    		companionOfPK = participant.getCompanionOfPK();


    		boolean showGroupManager = ! participant.isGroupManager() && groupManagerPK != null;
    		boolean showCompanionOf = companionOfPK != null;
    		boolean showProfile = profileID != null;



    		boolean visible = showGroupManager || showCompanionOf || showProfile;
    		setVisible(visible);

    		// Don't show this section if no connection data is present
    		if (visible) {

        		// Group Manager, if existing...
        		if (showGroupManager) {
        			try {
        				Participant groupManager = participantModel.getParticipant(groupManagerPK);
        				participantModel.addListener(this, groupManagerPK);
        				if (groupManager != null &&
        					groupManager.getName() != null &&
        					groupManager.getName().length() > 0
        				) {
            				Label leftLabel = formToolkit.createLabel(
            					sectionComposite,
            					SWTHelper.prepareLabelText(Participant.GROUP_MANAGER.getString() + ":"),
            					SWT.RIGHT
            				);
            				leftLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

            				Hyperlink hyperlink = formToolkit.createHyperlink(sectionComposite, groupManager.getName(), SWT.NONE);
            				hyperlink.setHref(groupManager.getID());
            				hyperlink.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

            				hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            					@Override
								public void linkActivated(HyperlinkEvent e) {
            						openParticipantEditor((Long) e.data);
            					}
            				});
        				}
        			}
        			catch (Exception e) {
        				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
        			}
        		}


        		// Companion, if existing...
        		if (showCompanionOf) {
        			try {
        				Participant accompaniedParticipant = participantModel.getParticipant(companionOfPK);
        				participantModel.addListener(this, companionOfPK);
        				if (accompaniedParticipant != null && StringHelper.isNotEmpty(accompaniedParticipant.getName())) {
        					Label leftLabel = formToolkit.createLabel(
        						sectionComposite,
        						SWTHelper.prepareLabelText( Participant.COMPANION_OF.getString() + ":"),
        						SWT.RIGHT
        					);
        					leftLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

        					Hyperlink hyperlink = formToolkit.createHyperlink(sectionComposite, accompaniedParticipant.getName(), SWT.NONE);
        					hyperlink.setHref(accompaniedParticipant.getID());
        					hyperlink.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        					hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
        						@Override
								public void linkActivated(HyperlinkEvent e) {
        							openParticipantEditor((Long) e.data);
        						}
        					});
        				}
        			}
        			catch (Exception e) {
        				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
        			}
        		}


        		if (showProfile) {
        			Label leftLabel = formToolkit.createLabel(
        				sectionComposite,
        				SWTHelper.prepareLabelText(I18N.ParticipantEditor_Profile),
        				SWT.RIGHT
        			);
        			leftLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));

        			Hyperlink hyperlink = formToolkit.createHyperlink(sectionComposite, UtilI18N.Yes, SWT.NONE);
        			hyperlink.setHref(profileID);
        			hyperlink.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        			hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
        				@Override
						public void linkActivated(HyperlinkEvent e) {
        					openProfileEditor((Long) e.data);
        				}
        			});
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
		if (participantModel != null) {
			try {
				if (participantID != null) {
					participantModel.removeListener(this, participantID);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}

			try {
				if (groupManagerPK != null) {
					participantModel.removeListener(this, groupManagerPK);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}

			try {
				if (companionOfPK != null) {
					participantModel.removeListener(this, companionOfPK);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}


		try {
			if (personLinkModel != null && personLink != null) {
				personLinkModel.removeListener(this, personLink);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	protected void openProfileEditor(Long profileID) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.openEditor(new ProfileEditorInput(profileID), ProfileEditor.ID);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void openParticipantEditor(Long participantID) {
		ParticipantEditorInput participantEditorInput = ParticipantEditorInput.getEditInstance(participantID);
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.openEditor(participantEditorInput, ParticipantEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
