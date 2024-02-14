package de.regasus.email.dispatch;

import de.regasus.email.dispatch.dialog.EmailDispatchWizard;

/**
 * Modes that are given to the {@link EmailDispatchWizard} to control what pages are needed to be shown, depending on
 * the current selection and view.
 * 
 * @author manfred
 * 
 */
public enum DispatchCommandMode {

	/**
	 * Participants are selected, the user may select one of the email templates for the participants' event or the
	 * general templates.
	 */
	PARTICIPANTS_SELECTED_SEARCH_TEMPLATE(false, false, true),

	/**
	 * Profiles are selected, the user may select one of the general templates.
	 */
	PROFILES_SELECTED_SEARCH_TEMPLATE(false, false, true),

	/**
	 * An event specific template is selected, the user may select among the event's participants.
	 */
	TEMPLATE_SELECTED_SEARCH_PARTICIPANTS(true, false, false),

	/**
	 * A general template is selected, the user may select among profiles.
	 */
	GENERAL_TEMPLATE_SELECTED_SEARCH_PROFILES(false, true, false),

	/**
	 * An event is selected, the user one of its templates and among the event's participants. Not yet used.
	 */
	EVENT_SELECTED_SEARCH_TEMPLATE_AND_PARTICIPANTS(true, false, true);
	
	

	private boolean showParticipantSearchPage;

	private boolean showProfileSearchPage;

	private boolean showTemplateSearchPage;


	private DispatchCommandMode(
		boolean showParticipantSearchPage,
		boolean showProfileSearchPage,
		boolean showTemplateSearchPage) {
		this.showParticipantSearchPage = showParticipantSearchPage;
		this.showProfileSearchPage = showProfileSearchPage;
		this.showTemplateSearchPage = showTemplateSearchPage;
	}


	public boolean isShowParticipantSearchPage() {
		return showParticipantSearchPage;
	}


	public boolean isShowProfileSearchPage() {
		return showProfileSearchPage;
	}


	public boolean isShowTemplateSearchPage() {
		return showTemplateSearchPage;
	}

}
