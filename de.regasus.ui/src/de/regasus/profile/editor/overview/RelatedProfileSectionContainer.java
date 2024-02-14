/**
 * RelatedProfileSectionContainer.java
 * created on 07.08.2013 09:33:11
 */
package de.regasus.profile.editor.overview;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileRelationModel;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.ui.Activator;

public class RelatedProfileSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long profileID;

	private ProfileModel profileModel;
	private ProfileRelationModel profileRelationModel;
	private ProfileRelationTypeModel profileRelationTypeModel;

	private ProfileConfigParameterSet profileConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public RelatedProfileSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long profileID,
		ProfileConfigParameterSet profileConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.profileID = profileID;
		this.profileConfigParameterSet = profileConfigParameterSet;

		addDisposeListener(this);

		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this, profileID);
		profileRelationModel = ProfileRelationModel.getInstance();
		profileRelationModel.addForeignKeyListener(this, profileID);
		profileRelationTypeModel = ProfileRelationTypeModel.getInstance();

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ProfileLabel.RelatedProfiles.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

			boolean visible =
				profileConfigParameterSet == null ||
				profileConfigParameterSet.getProfileRelation().isVisible();

			if (visible) {
				try {
					List<ProfileRelation> profileRelations = profileRelationModel.getProfileRelationsByProfile(profileID);
					Profile profile = profileModel.getProfile(profileID);
					visible = CollectionsHelper.notEmpty(profileRelations);
					setVisible(visible);
					if (visible) {
						for (int i = 0; i < profileRelations.size(); i++) {

							// Find related profile
							ProfileRelation profileRelation = profileRelations.get(i);
							Long otherProfileID = profileRelation.getOtherProfileID(profileID);
							Profile otherProfile = profileModel.getProfile(otherProfileID);

							// Hyperlink with profile's name
							Hyperlink hyperlink = formToolkit.createHyperlink(
								sectionComposite,
								otherProfile.getName(),
								SWT.NONE
								);
							hyperlink.setHref(otherProfile.getID());
							hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
								@Override
								public void linkActivated(HyperlinkEvent e) {
									Long profileID = (Long) e.data;
									openProfileEditor(profileID);
								}
							});

							// Role of profile, bold if it is second person
							String otherProfileRole = profileRelationTypeModel.getRole(otherProfileID, profileRelation);
							Label rightLabel = formToolkit.createLabel(
								sectionComposite,
								SWTHelper.prepareLabelText("("+ otherProfileRole + ")"),
								SWT.LEFT
							);

							if (profile.getSecondPersonID() != null &&
								profile.getSecondPersonID().equals(otherProfileID)) {
								SWTHelper.makeBold(rightLabel);
							}
							rightLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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

		if (profileRelationModel != null && profileID != null) {
			try {
				profileRelationModel.removeForeignKeyListener(this, profileID);
			} catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
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

}
