package de.regasus.common;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.regasus.common.country.view.CountryView;
import de.regasus.common.language.view.LanguageView;


public class MasterDataPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.MasterDataPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		IFolderLayout leftLayout = layout.createFolder(
			"left",
			IPageLayout.LEFT,
			0.35f,
			layout.getEditorArea()
		);
		leftLayout.addView(LanguageView.ID);
		leftLayout.addView(CountryView.ID);
	}

}
