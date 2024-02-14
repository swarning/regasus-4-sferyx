package de.regasus.users;

import org.eclipse.osgi.util.NLS;

public class UsersI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.users.i18n-users-ui";


	static {
		NLS.initializeMessages(BUNDLE_NAME, UsersI18N.class);
	}


	public static String AssignToGroup;

	public static String DeleteUserAccounts_Confirmation;

	public static String DeleteUserGroups_Confirmation;

	public static String DeleteUserGroups_UnsavedUserEditors;

	public static String DeleteUserGroupsAnywayQuestion;

	public static String FoundUserAccountsLabel;

	public static String RemoveFromGroup;

	public static String SelectGroupForAssignment;

	public static String SelectUserGroupForAssignment;

	public static String SelectUserGroupForRemoval;

	public static String UserAccount_Editor_DefaultToolTip;

	public static String UserAccount_Editor_NewName;

	public static String UserGroup_Editor_DefaultToolTip;

	public static String UserGroup_Editor_NewName;

	public static String AddRight;

	public static String RemoveRight;

	public static String SelectRight;

	public static String SelectConstraintType;

	public static String NoConstraintType;

	public static String SelectConstraintDetailPage;

	public static String SelectConstraintSubDetailPage;

	public static String SetCrudRightsAndPriority;

	public static String Priority;

	public static String Password;

	public static String PasswordRepitition;

	public static String UserAccountGroup_GeneratePassword;

	public static String ErrorMessage_PasswordEmpty;

	public static String ErrorMessage_PasswordsNotEqual;


	private UsersI18N() {
	}

}
