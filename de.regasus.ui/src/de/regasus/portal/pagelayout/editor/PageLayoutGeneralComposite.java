package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.StringHelper.trim;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.image.ImageFileGroup;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.portal.PageLayoutFile;
import de.regasus.ui.Activator;


public class PageLayoutGeneralComposite extends Composite {

	// the entity
	private PageLayout pageLayout;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Text nameText;
	private MultiLineText descriptionText;
	private PageLayoutImageFileGroupController controller;

	// *
	// * Widgets
	// **************************************************************************


	public PageLayoutGeneralComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		final int COL_COUNT = 2;
		setLayout(new GridLayout(COL_COUNT, false));


		/*** name ***/
		SWTHelper.createLabel(this, PageLayout.NAME.getString(), true);

		nameText = new Text(this, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		SWTHelper.makeBold(nameText);
		nameText.setTextLimit( PageLayout.NAME.getMaxLength() );


		/*** description ***/
		Label descriptionLabel = new Label(this, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.TOP).indent(0, SWTConstants.VERTICAL_INDENT).applyTo(descriptionLabel);
		descriptionLabel.setText(PageLayout.DESCRIPTION.getString());

		descriptionText = new MultiLineText(this, SWT.BORDER);
		descriptionText.setMinLineCount(2);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		descriptionText.setTextLimit( PageLayout.DESCRIPTION.getMaxLength() );


		// observe all widgets and groups
		nameText.addModifyListener(modifySupport);
		descriptionText.addModifyListener(modifySupport);


		// ImageFileComposite
		ImageFileGroup imageFileGroup = new ImageFileGroup(this, SWT.NONE);
		imageFileGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		imageFileGroup.setPreferredImageSize(SWT.DEFAULT, 32);
		imageFileGroup.setText(I18N.PageLayoutFaviconComposite_FaviconImage);

		// create ImageFileGroupController
		controller = new PageLayoutImageFileGroupController(null, PageLayoutFile.FAVICON_IMAGE);
		imageFileGroup.setController(controller);
	}


	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;

		if (pageLayout.getId() != null) {
			controller.setPageLayoutPK( pageLayout.getId() );
		}

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						nameText.setText( StringHelper.avoidNull(pageLayout.getName()) );
						descriptionText.setText( StringHelper.avoidNull(pageLayout.getDescription()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (pageLayout != null) {
			pageLayout.setName( trim(nameText.getText()) );
			pageLayout.setDescription( trim(descriptionText.getText()) );
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
