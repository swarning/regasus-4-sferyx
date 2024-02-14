package de.regasus.profile.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.profile.editor.overview.AddressSectionContainer;
import de.regasus.profile.editor.overview.CommunicationSectionContainer;
import de.regasus.profile.editor.overview.ConnectionSectionContainer;
import de.regasus.profile.editor.overview.PersonSectionContainer;
import de.regasus.profile.editor.overview.ProfileCustomFieldGroupSectionContainer;
import de.regasus.profile.editor.overview.ProfileSectionContainer;
import de.regasus.profile.editor.overview.RelatedProfileSectionContainer;
import de.regasus.ui.Activator;

/**
 * A composite that uses the Eclipse Forms API to present the profile's data on one single page.
 * <p>
 * See the article <a href="http://www.eclipse.org/articles/Article-Forms/article.html">Eclipse Forms: Rich UI for the
 * Rich Client</a>.
 *
 * @author manfred
 *
 */

public class ProfileOverviewComposite extends LazyComposite implements DisposeListener {

	// models
	private ProfileCustomFieldGroupModel pcfgm;

	/**
	 * The responsible for creating adapted SWT controls
	 */
	private FormToolkit formToolkit;

	/**
	 * All sections are created in here
	 */
	private Composite body;

	private Long profileID;

	private ScrolledForm form;

	private ConfigParameterSet configParameterSet;


	/**
	 * Constructor
	 */
	public ProfileOverviewComposite(
		Composite parent,
		Long profileID,
		ConfigParameterSet configParameterSet
	) {
		super(parent, SWT.NONE);

		pcfgm = ProfileCustomFieldGroupModel.getInstance();

		this.profileID = profileID;
		this.configParameterSet = configParameterSet;

		formToolkit = new FormToolkit(parent.getDisplay());

		setLayout(new FillLayout());

		addDisposeListener(this);
	}


	@Override
	protected void createPartControl() throws Exception {
		form = formToolkit.createScrolledForm(this);
		form.setText(UtilI18N.Overview);
		body = form.getBody();

		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 3;
		layout.minNumColumns = 1;
		body.setLayout(layout);

		try {
			new PersonSectionContainer(
				formToolkit,
				body,
				profileID,
				configParameterSet.getProfile()
			);

			new ProfileSectionContainer(
				formToolkit,
				body,
				profileID,
				configParameterSet.getProfile()
			);

			new AddressSectionContainer(
				formToolkit,
				body,
				profileID,
				configParameterSet.getProfile().getAddress()
			);

			new CommunicationSectionContainer(
				formToolkit,
				body,
				profileID,
				configParameterSet.getProfile().getCommunication()
			);

			new ConnectionSectionContainer(
				formToolkit,
				body,
				profileID,
				configParameterSet
			);

			// load all ProfileCustomFields to avoid multiple server calls
			ProfileCustomFieldModel.getInstance().getAllProfileCustomFields();

			Collection<ProfileCustomFieldGroup> groups = pcfgm.getAllProfileCustomFieldGroups();

			List<ProfileCustomFieldGroup> groupsList = new ArrayList<>(groups);
			Collections.sort(groupsList, ProfileCustomFieldGroup_Location_Position_Comparator.getInstance());

			for (ProfileCustomFieldGroup group : groupsList) {
				new ProfileCustomFieldGroupSectionContainer(
					formToolkit,
					body,
					profileID,
					group,
					configParameterSet.getProfile()
				);
			}


			new RelatedProfileSectionContainer(
				formToolkit,
				body,
				profileID,
				configParameterSet.getProfile()
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		formToolkit.dispose();
	}

}
