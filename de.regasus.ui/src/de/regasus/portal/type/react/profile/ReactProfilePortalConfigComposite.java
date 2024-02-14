package de.regasus.portal.type.react.profile;

import static de.regasus.portal.type.react.profile.PageVisibility.*;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.ui.Activator;

public class ReactProfilePortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private ReactProfilePortalConfig portalConfig;
	private List<Language> languageList;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	// Registration Page
	private Button registrationPageNotVisibleButton;
	private Button registrationPageAlwaysVisibleButton;
	private Button registrationPageVisibleIfRegisteredButton;

	// Social Page
	private Button socialPageNotVisibleButton;
	private Button socialPageAlwaysVisibleButton;
	private Button socialPageVisibleIfRegisteredButton;

	// Scholarship Page
	private Button scholarshipPageNotVisibleButton;
	private Button scholarshipPageAlwaysVisibleButton;
	private Button scholarshipPageVisibleIfRegisteredButton;

	// Hotel Page
	private Button hotelPageNotVisibleButton;
	private Button hotelPageAlwaysVisibleButton;

	// Manage Abstract Page
	private Button manageAbstractPageNotVisibleButton;
	private Button manageAbstractPageAlwaysVisibleButton;
	private Button manageAbstractPageVisibleIfRegisteredButton;

	// Documedias Abstract Page
	private Button documediasAbstractPageNotVisibleButton;
	private Button documediasAbstractPageAlwaysVisibleButton;
	private Button documediasAbstractPageVisibleIfRegisteredButton;


	// Join Together Page 1
	private Button joinTogetherPage1NotVisibleButton;
	private Button joinTogetherPage1AlwaysVisibleButton;

	// Join Together Page 2
	private Button joinTogetherPage2NotVisibleButton;
	private Button joinTogetherPage2AlwaysVisibleButton;

	// Join Together Page 3
	private Button joinTogetherPage3NotVisibleButton;
	private Button joinTogetherPage3AlwaysVisibleButton;

	private ValidRedirectUrlsGroup validRedirectUrlsGroup;

	// *
	// * Widgets
	// **************************************************************************


	public ReactProfilePortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (ReactProfilePortalConfig) portal.getPortalConfig();

		// determine Portal languages
		List<String> languageIds = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageIds);
	}


	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		setLayout( new GridLayout(2, true) );

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);


		// Page Visibility
   		Group pageVisibilityGroup = buildPageVisibilityGroup(this);
    	groupGridDataFactory.applyTo(pageVisibilityGroup);

    	validRedirectUrlsGroup = new ValidRedirectUrlsGroup(this, SWT.NONE);
    	groupGridDataFactory.applyTo(validRedirectUrlsGroup);
    	validRedirectUrlsGroup.addModifyListener(modifySupport);
	}


	private Group buildPageVisibilityGroup(Composite parent) {
		Group group = new Group(this, SWT.NONE);

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);


		group.setText("Visibility of Pages");
		group.setLayout( new GridLayout(2, false) );

		// registrationPageVisibility
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Registration Page");

			Button[] radioButtons = buildRadioComposite(group);
			registrationPageNotVisibleButton = radioButtons[0];
			registrationPageAlwaysVisibleButton = radioButtons[1];
			registrationPageVisibleIfRegisteredButton = radioButtons[2];
		}

		// socialPageVisibility
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Social Page");

			Button[] radioButtons = buildRadioComposite(group);
			socialPageNotVisibleButton = radioButtons[0];
			socialPageAlwaysVisibleButton = radioButtons[1];
			socialPageVisibleIfRegisteredButton = radioButtons[2];
		}

		// scholarshipPageVisibility
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Scholarship Page");

			Button[] radioButtons = buildRadioComposite(group);
			scholarshipPageNotVisibleButton = radioButtons[0];
			scholarshipPageAlwaysVisibleButton = radioButtons[1];
			scholarshipPageVisibleIfRegisteredButton = radioButtons[2];
		}

		SWTHelper.verticalSpace(group);

		// hotelPageVisible
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Hotel Page");

			Button[] radioButtons = buildRadioComposite(group);
			hotelPageNotVisibleButton = radioButtons[0];
			hotelPageAlwaysVisibleButton = radioButtons[1];
			radioButtons[2].setVisible(false);
		}


		SWTHelper.verticalSpace(group);


		// manageAbstractPageVisible
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Manage Abstract Pages");

			Button[] radioButtons = buildRadioComposite(group);
			manageAbstractPageNotVisibleButton = radioButtons[0];
			manageAbstractPageAlwaysVisibleButton = radioButtons[1];
			manageAbstractPageVisibleIfRegisteredButton = radioButtons[2];
		}

		// documediasAbstractPageVisible
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Documedias Abstract Pages");

			Button[] radioButtons = buildRadioComposite(group);
			documediasAbstractPageNotVisibleButton = radioButtons[0];
			documediasAbstractPageAlwaysVisibleButton = radioButtons[1];
			documediasAbstractPageVisibleIfRegisteredButton = radioButtons[2];
		}


		SWTHelper.verticalSpace(group);


		// joinTogetherPage1Visible
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Join Together Page 1");

			Button[] radioButtons = buildRadioComposite(group);
			joinTogetherPage1NotVisibleButton = radioButtons[0];
			joinTogetherPage1AlwaysVisibleButton = radioButtons[1];
			radioButtons[2].setVisible(false);
		}

		// joinTogetherPage2Visible
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Join Together Page 2");

			Button[] radioButtons = buildRadioComposite(group);
			joinTogetherPage2NotVisibleButton = radioButtons[0];
			joinTogetherPage2AlwaysVisibleButton = radioButtons[1];
			radioButtons[2].setVisible(false);
		}

		// joinTogetherPage3Visible
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Join Together Page 3");

			Button[] radioButtons = buildRadioComposite(group);
			joinTogetherPage3NotVisibleButton = radioButtons[0];
			joinTogetherPage3AlwaysVisibleButton = radioButtons[1];
			radioButtons[2].setVisible(false);
		}

		return group;
	}


	private Button[] buildRadioComposite(Composite parent) {
		Composite radioComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true,  false).applyTo(radioComposite);
		radioComposite.setLayout( new GridLayout(3, true) );

		Button[] buttons = {
			new Button(radioComposite, SWT.RADIO),
			new Button(radioComposite, SWT.RADIO),
			new Button(radioComposite, SWT.RADIO)
		};

		buttons[0].setText("Not visible");
		buttons[1].setText("Always visible");
		buttons[2].setText("Visible if registered");

		buttons[0].addSelectionListener(modifySupport);
		buttons[1].addSelectionListener(modifySupport);
		buttons[2].addSelectionListener(modifySupport);

		return buttons;
	}


	@Override
	public void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// Registration Page
						registrationPageNotVisibleButton.setSelection(portalConfig.getRegistrationPageVisibility() == NOT_VISIBLE);
						registrationPageAlwaysVisibleButton.setSelection(portalConfig.getRegistrationPageVisibility() == ALWAYS_VISIBLE);
						registrationPageVisibleIfRegisteredButton.setSelection(portalConfig.getRegistrationPageVisibility() == VISIBLE_IF_REGISTERED);

						// Social Page
						socialPageNotVisibleButton.setSelection(portalConfig.getSocialPageVisibility() == NOT_VISIBLE);
						socialPageAlwaysVisibleButton.setSelection(portalConfig.getSocialPageVisibility() == ALWAYS_VISIBLE);
						socialPageVisibleIfRegisteredButton.setSelection(portalConfig.getSocialPageVisibility() == VISIBLE_IF_REGISTERED);

						// Scholarship Page
						scholarshipPageNotVisibleButton.setSelection(portalConfig.getScholarshipPageVisibility() == NOT_VISIBLE);
						scholarshipPageAlwaysVisibleButton.setSelection(portalConfig.getScholarshipPageVisibility() == ALWAYS_VISIBLE);
						scholarshipPageVisibleIfRegisteredButton.setSelection(portalConfig.getScholarshipPageVisibility() == VISIBLE_IF_REGISTERED);

						// Hotel Page
						hotelPageNotVisibleButton.setSelection( ! portalConfig.isHotelPageVisible() );
						hotelPageAlwaysVisibleButton.setSelection( portalConfig.isHotelPageVisible() );

						// Manage Abstract Page
						manageAbstractPageNotVisibleButton.setSelection(portalConfig.getManageAbstractPageVisibility() == NOT_VISIBLE);
						manageAbstractPageAlwaysVisibleButton.setSelection(portalConfig.getManageAbstractPageVisibility() == ALWAYS_VISIBLE);
						manageAbstractPageVisibleIfRegisteredButton.setSelection(portalConfig.getManageAbstractPageVisibility() == VISIBLE_IF_REGISTERED);

						// Documedias Abstract Page
						documediasAbstractPageNotVisibleButton.setSelection(portalConfig.getDocumediasAbstractPageVisibility() == NOT_VISIBLE);
						documediasAbstractPageAlwaysVisibleButton.setSelection(portalConfig.getDocumediasAbstractPageVisibility() == ALWAYS_VISIBLE);
						documediasAbstractPageVisibleIfRegisteredButton.setSelection(portalConfig.getDocumediasAbstractPageVisibility() == VISIBLE_IF_REGISTERED);

						// Join Together Page 1
						joinTogetherPage1NotVisibleButton.setSelection( ! portalConfig.isJoinTogetherPage1Visible() );
						joinTogetherPage1AlwaysVisibleButton.setSelection( portalConfig.isJoinTogetherPage1Visible() );

						// Join Together Page 2
						joinTogetherPage2NotVisibleButton.setSelection( ! portalConfig.isJoinTogetherPage2Visible() );
						joinTogetherPage2AlwaysVisibleButton.setSelection( portalConfig.isJoinTogetherPage2Visible() );

						// Join Together Page 3
						joinTogetherPage3NotVisibleButton.setSelection( ! portalConfig.isJoinTogetherPage3Visible() );
						joinTogetherPage3AlwaysVisibleButton.setSelection( portalConfig.isJoinTogetherPage3Visible() );

						// valid redirect URLs
						validRedirectUrlsGroup.setEntity(portalConfig);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() throws ErrorMessageException {
		if (portalConfig != null) {

			// Registration Page
			if ( registrationPageNotVisibleButton.getSelection() ) {
				portalConfig.setRegistrationPageVisibility(NOT_VISIBLE);
			}
			else if ( registrationPageAlwaysVisibleButton.getSelection() ) {
				portalConfig.setRegistrationPageVisibility(ALWAYS_VISIBLE);
			}
			else {
				portalConfig.setRegistrationPageVisibility(VISIBLE_IF_REGISTERED);
			}


			// Social Page
			if ( socialPageNotVisibleButton.getSelection() ) {
				portalConfig.setSocialPageVisibility(NOT_VISIBLE);
			}
			else if ( socialPageAlwaysVisibleButton.getSelection() ) {
				portalConfig.setSocialPageVisibility(ALWAYS_VISIBLE);
			}
			else {
				portalConfig.setSocialPageVisibility(VISIBLE_IF_REGISTERED);
			}


			// Scholarship Page
			if ( scholarshipPageNotVisibleButton.getSelection() ) {
				portalConfig.setScholarshipPageVisibility(NOT_VISIBLE);
			}
			else if ( scholarshipPageAlwaysVisibleButton.getSelection() ) {
				portalConfig.setScholarshipPageVisibility(ALWAYS_VISIBLE);
			}
			else {
				portalConfig.setScholarshipPageVisibility(VISIBLE_IF_REGISTERED);
			}


			// Hotel Page
			portalConfig.setHotelPageVisible( hotelPageAlwaysVisibleButton.getSelection() );


			// Manage Abstract Page
			if (manageAbstractPageNotVisibleButton.getSelection()) {
				portalConfig.setManageAbstractPageVisibility(NOT_VISIBLE);
			}
			else if (manageAbstractPageAlwaysVisibleButton.getSelection()) {
				portalConfig.setManageAbstractPageVisibility(ALWAYS_VISIBLE);
			}
			else {
				portalConfig.setManageAbstractPageVisibility(VISIBLE_IF_REGISTERED);
			}

			// Documedias Abstract Page
			if (documediasAbstractPageNotVisibleButton.getSelection()) {
				portalConfig.setDocumediasAbstractPageVisibility(NOT_VISIBLE);
			}
			else if (documediasAbstractPageAlwaysVisibleButton.getSelection()) {
				portalConfig.setDocumediasAbstractPageVisibility(ALWAYS_VISIBLE);
			}
			else {
				portalConfig.setDocumediasAbstractPageVisibility(VISIBLE_IF_REGISTERED);
			}


			// Join Together Page 1
			portalConfig.setJoinTogetherPage1Visible( joinTogetherPage1AlwaysVisibleButton.getSelection() );

			// Join Together Page 2
			portalConfig.setJoinTogetherPage2Visible( joinTogetherPage2AlwaysVisibleButton.getSelection() );

			// Join Together Page 3
			portalConfig.setJoinTogetherPage3Visible( joinTogetherPage3AlwaysVisibleButton.getSelection() );


			// valid redirect URLs
			validRedirectUrlsGroup.syncEntityToWidgets();
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
