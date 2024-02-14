package de.regasus.portal.portal.editor;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.i18n.LanguageWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dnd.CopyPasteButtonComposite;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalConfig;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;


class PortalGeneralComposite extends Composite {

	// the entity
	private Portal portal;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Text portalTypeText;
	private Text mnemonicText;
	private Text urlText;
	private Text nameText;
	private LanguageWidget languageWidget;
	private DateTimeComposite onlineBeginDateTimeComposite;
	private DateTimeComposite onlineEndDateTimeComposite;
	private Text passwordText;
	private Button copyPasswordButton;

	// *
	// * Widgets
	// **************************************************************************


	/**
	 * Create a {@link PortalGeneralComposite}.
	 * @param parent
	 * @param style
	 * @param portal at this point the Portal is only necessary to get its {@link PortalConfig}
	 * @throws Exception
	 */
	public PortalGeneralComposite(Composite parent, int style, Portal portal) throws Exception {
		super(parent, style);

		this.portal = Objects.requireNonNull(portal);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		/* layout with 4 columns
		 */
		final int COL_COUNT = 4;
		setLayout(new GridLayout(COL_COUNT, false));

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		/*
		 * Row 1
		 */

		/*** portal type ***/
		SWTHelper.createLabel(this, Portal.PORTAL_TYPE.getString(), false);

		portalTypeText = new Text(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(portalTypeText);
		SWTHelper.disableTextWidget(portalTypeText);


		/*** mnemonic ***/
		SWTHelper.createLabel(this, Portal.MNEMONIC.getString(), true);

		mnemonicText = new Text(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(mnemonicText);
		SWTHelper.makeBold(mnemonicText);
		mnemonicText.setTextLimit( Portal.MNEMONIC.getMaxLength() );
		mnemonicText.addModifyListener(modifySupport);

		/*
		 * Row 2
		 */

		SWTHelper.createLabel(this, UtilI18N.URL, false);

		urlText = new Text(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(urlText);
		SWTHelper.disableTextWidget(urlText);

		Button copyUrlButton = CopyPasteButtonComposite.createCopyButton(this);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).applyTo(copyUrlButton);
		copyUrlButton.addSelectionListener(copyUrlListener);

		new Label(this, SWT.NONE); // placeholder


		/*
		 * Row 3
		 */

		/*** name ***/
		SWTHelper.createLabel(this, Portal.NAME.getString(), true);

		nameText = new Text(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(nameText);
		SWTHelper.makeBold(nameText);
		nameText.setTextLimit( Portal.NAME.getMaxLength() );
		nameText.addModifyListener(modifySupport);


		/*** languages ***/
		SWTHelper.createLabel(this, Portal.LANGUAGES.getString(), true);

		languageWidget = new LanguageWidget(this, SWT.NONE, LanguageProvider.getInstance());
		widgetGridDataFactory.applyTo(languageWidget);
		languageWidget.addModifyListener(modifySupport);


		/*
		 * Row 4
		 */

		/*** online begin ***/
		SWTHelper.createLabel(this, Portal.ONLINE_BEGIN.getString());

		onlineBeginDateTimeComposite = new DateTimeComposite(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(onlineBeginDateTimeComposite);
		onlineBeginDateTimeComposite.addModifyListener(modifySupport);


		/*** online end ***/
		SWTHelper.createLabel(this, Portal.ONLINE_END.getString());

		onlineEndDateTimeComposite = new DateTimeComposite(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(onlineEndDateTimeComposite);
		onlineEndDateTimeComposite.addModifyListener(modifySupport);


		/*
		 * Row 5
		 */

		if ( portal.getPortalConfig().isWithPassword() ) {
    		/*** password ***/
    		SWTHelper.createLabel(this, Portal.PASSWORD.getString());

    		passwordText = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(passwordText);
    		passwordText.setTextLimit( Portal.PASSWORD.getMaxLength() );
    		passwordText.addModifyListener(modifySupport);

    		copyPasswordButton = CopyPasteButtonComposite.createCopyButton(this);
    		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).applyTo(copyPasswordButton);
    		copyPasswordButton.addSelectionListener(copyPasswordListener);

    		new Label(this, SWT.NONE); // placeholder
		}
	}


	private SelectionListener copyUrlListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
			try {
				String url = PortalModel.getInstance().getPortalUrl( portal.getId() );
				ClipboardHelper.copyToClipboard(url);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		};
	};


	private SelectionListener copyPasswordListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
			try {
				ClipboardHelper.copyToClipboard(portal.getPassword());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		};
	};


	public void setPortal(Portal portal) {
		this.portal = portal;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (portal != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						mnemonicText.setText( StringHelper.avoidNull(portal.getMnemonic()) );
						portalTypeText.setText( portal.getPortalType().getName().getString() );

						String url = PortalModel.getInstance().getPortalUrl( portal.getId() );
						urlText.setText(url);

						nameText.setText( StringHelper.avoidNull(portal.getName()) );
						languageWidget.setLanguageCodeList( portal.getLanguageList() );

						onlineBeginDateTimeComposite.setI18NDateMinute( portal.getOnlineBegin() );
						onlineEndDateTimeComposite.setI18NDateMinute( portal.getOnlineEnd() );

						if (passwordText != null) {
							passwordText.setText( StringHelper.avoidNull(portal.getPassword()) );
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (portal != null) {
			portal.setMnemonic( StringHelper.trim(mnemonicText.getText()) );
			portal.setName( StringHelper.trim(nameText.getText()) );
			portal.setLanguageList(languageWidget.getLanguageCodeList());
			portal.setOnlineBegin( onlineBeginDateTimeComposite.getI18NDateMinute() );
			portal.setOnlineEnd( onlineEndDateTimeComposite.getI18NDateMinute() );

			if (passwordText != null) {
				portal.setPassword( StringHelper.trim(passwordText.getText()) );
			}
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
