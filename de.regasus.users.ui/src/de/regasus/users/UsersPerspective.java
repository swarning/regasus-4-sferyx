package de.regasus.users;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.regasus.users.group.view.GroupsView;
import de.regasus.users.user.view.UserAccountSearchView;

public class UsersPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		IFolderLayout usersGroupsFolder = layout.createFolder("usersGroups", IPageLayout.LEFT, 0.25f, editorArea);
		
		// View für die Nutzer
		usersGroupsFolder.addView(UserAccountSearchView.ID);

		// View für die Gruppen
		usersGroupsFolder.addView(GroupsView.ID);


	}

}
