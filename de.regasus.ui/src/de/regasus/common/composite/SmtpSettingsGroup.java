package de.regasus.common.composite;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.NumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

/**
 * An SWT group that shows and allows editing of an {@link SmtpSettingsVO} object for the settings of an SMTP server to
 * be used for sending emails.
 * <p>
 * This group used to be in the masterdata.ui plugin, I moved it to the event.ui plugin
 * in order to remove the dependency from email.ui to masterdata.ui, which lead to cycles
 * when i needed the dependency from formedit.admin to email.ui.
 *
 * <ul>
 * <li>host</li>
 * <li>port</li>
 * <li>user</li>
 * <li>password</li>
 * <li>Local copy</li>
 * <li>ssl</li>
 * <li>sslPort</li>
 * <li>tsl</li>
 * </ul>
 */
public class SmtpSettingsGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	private Text host;

	private NumberText port;

	private Text user;

	private Text password;

	private Button sslButton;

	private Button tlsButton;

	private NumberText sslPort;

	private NullableSpinner chunkSize;

	private NullableSpinner timeInterval;


	// *************************************************************************
	// * Other Attributes
	// *

	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	/**
	 * The entity for which this group shows some property
	 */
	private SmtpSettingsVO smtpSettingsVO;

	private Button noEncryptionButton;


	// *************************************************************************
	// * Constructor
	// *

	public SmtpSettingsGroup(Composite parent, int style) {
		super(parent, style);

		setText(EmailLabel.SmtpSettings.getString());

		setLayout(new GridLayout(2, false));

		Group serverGroup = buildServerGroup(this);
		GridDataFactory.swtDefaults()
    		.align(SWT.FILL, SWT.CENTER)
    		.grab(true, false)
    		.span(2, 1)
//    		.indent(SWT.DEFAULT, 20)
    		.applyTo(serverGroup);


		Group authenticationGroup = buildAuthenticationGroup(this);
		GridDataFactory.swtDefaults()
    		.align(SWT.FILL, SWT.CENTER)
    		.grab(true, false)
    		.span(2, 1)
    		.indent(SWT.DEFAULT, 20)
    		.applyTo(authenticationGroup);


		Group encryptionGroup = buildEncryptionGroup(this);
		GridDataFactory.swtDefaults()
    		.align(SWT.FILL, SWT.CENTER)
    		.grab(true, false)
    		.span(2, 1)
    		.indent(SWT.DEFAULT, 20)
    		.applyTo(encryptionGroup);


		Group numberOfEmailsPerTimeIntervalGroup = buildNumberOfEmailsPerTimeIntervalGroup(this);
		GridDataFactory.swtDefaults()
    		.align(SWT.FILL, SWT.CENTER)
    		.grab(true, false)
    		.span(2, 1)
    		.indent(SWT.DEFAULT, 20)
    		.applyTo(numberOfEmailsPerTimeIntervalGroup);

	}


	private static final GridDataFactory LABEL_GRID_DATA_FACTORY = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
	private static final GridDataFactory TEXT_GRID_DATA_FACTORY = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	private Group buildServerGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText( EmailLabel.SMTPHost.getString() );
		group.setLayout( new GridLayout(2, false) );

		// Host
		Label hostLabel = new Label(group, SWT.NONE);
		hostLabel.setText(UtilI18N.Host);
		LABEL_GRID_DATA_FACTORY.applyTo(hostLabel);

		host = new Text(group, SWT.BORDER);
		host.setTextLimit(SmtpSettingsVO.MAX_LENGTH_SMTP_HOST);
		TEXT_GRID_DATA_FACTORY.applyTo(host);
		host.addModifyListener(modifySupport);


		// Port
		Label portLabel = new Label(group, SWT.NONE);
		portLabel.setText( EmailLabel.Port.getString() );
		LABEL_GRID_DATA_FACTORY.applyTo(portLabel);

		port = new NumberText(group, SWT.BORDER);
		port.setMinValue(SmtpSettingsVO.MIN_VALUE_SMTP_PORT);
		port.setMaxValue(SmtpSettingsVO.MAX_VALUE_SMTP_PORT);
		port.setNullAllowed(true);
		port.setMessage("25");
		WidgetSizer.setWidth(port);
		port.addModifyListener(modifySupport);

		return group;
	}


	private Group buildAuthenticationGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText( KernelLabel.Authentication.getString() );
		group.setLayout( new GridLayout(2, false) );

		// User
		Label userLabel = new Label(group, SWT.NONE);
		userLabel.setText( EmailLabel.UserName.getString() );
		LABEL_GRID_DATA_FACTORY.applyTo(userLabel);

		user = new Text(group, SWT.BORDER);
		user.setTextLimit(SmtpSettingsVO.MAX_LENGTH_SMTP_USER);
		TEXT_GRID_DATA_FACTORY.applyTo(user);
		user.addModifyListener(modifySupport);


		// Password
		Label passwordLabel = new Label(group, SWT.NONE);
		passwordLabel.setText(UtilI18N.Password);
		LABEL_GRID_DATA_FACTORY.applyTo(passwordLabel);

		password = new Text(group, SWT.BORDER | SWT.PASSWORD);
		password.setTextLimit(SmtpSettingsVO.MAX_LENGTH_SMTP_PASSWORD);
		TEXT_GRID_DATA_FACTORY.applyTo(password);
		password.addModifyListener(modifySupport);

		return group;
	}


	private Group buildEncryptionGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText( EmailLabel.Encryption.getString() );
		group.setLayout( new GridLayout(3, false) );


		/* Row 1 */

		// no encryption
		noEncryptionButton = new Button(group, SWT.RADIO | SWT.RIGHT);
		noEncryptionButton.setText( EmailLabel.None.getString() );
		noEncryptionButton.addSelectionListener(modifySupport);

		// SSL
		sslButton = new Button(group, SWT.RADIO | SWT.RIGHT);
		sslButton.setText(EmailLabel.SSL.getString());
		sslButton.addSelectionListener(modifySupport);

		// TLS
		tlsButton = new Button(group, SWT.RADIO | SWT.RIGHT);
		tlsButton.setText(EmailLabel.TLS.getString());
		tlsButton.addSelectionListener(modifySupport);


		/* Row 2 */

		// SSL Port
		Label sslPortLabel = new Label(group, SWT.NONE);
		sslPortLabel.setText( EmailLabel.SSLPort.getString() );
		LABEL_GRID_DATA_FACTORY.applyTo(sslPortLabel);

		sslPort = new NumberText(group, SWT.BORDER);
		sslPort.setMinValue( SmtpSettingsVO.MIN_VALUE_SMTP_SSL_PORT );
		sslPort.setMaxValue( SmtpSettingsVO.MAX_VALUE_SMTP_SSL_PORT );
		sslPort.setNullAllowed(true);
		sslPort.setEnabled(false);
		WidgetSizer.setWidth(sslPort);
		sslPort.addModifyListener(modifySupport);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(2, 1).applyTo(sslPort);


		/* Make SSL Port only editable when SSL Checkbox is selected
		 * Attention:
		 * Observe all radio buttons, not only the sslButton, because we cannot rely on that events for deselected
		 * radio buttons are actually fired!
		 */
		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					sslPort.setEnabled( sslButton.getSelection() );
				}
			}
		};
		noEncryptionButton.addSelectionListener(selectionListener);
		sslButton.addSelectionListener(selectionListener);
		tlsButton.addSelectionListener(selectionListener);


		return group;
	}


	private Group buildNumberOfEmailsPerTimeIntervalGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText( EmailLabel.NumberOfEmailsPerTimeInterval.getString() );
		group.setLayout( new GridLayout(2, false) );

		// Chunk Size
		Label chunkSizeLabel = new Label(group, SWT.NONE);
		chunkSizeLabel.setText( EmailLabel.ChunkSize.getString() );
		LABEL_GRID_DATA_FACTORY.applyTo(chunkSizeLabel);

		chunkSize = new NullableSpinner(group, SWT.BORDER);
		chunkSize.setMinimum(SmtpSettingsVO.MIN_VALUE_SMTP_CHUNK_SIZE);
		chunkSize.setNullable(SmtpSettingsVO.NULL_ALLOWED_SMTP_CHUNK_SIZE);
		chunkSize.addModifyListener(modifySupport);


		// Time Interval
		Label timeIntervalLabel = new Label(group, SWT.NONE);
		timeIntervalLabel.setText( EmailLabel.TimeInterval.getString() );
		LABEL_GRID_DATA_FACTORY.applyTo(timeIntervalLabel);

		timeInterval = new NullableSpinner(group, SWT.BORDER);
		timeInterval.setMinimum(SmtpSettingsVO.MIN_VALUE_SMTP_TIME_INTERVAL);
		timeInterval.setNullable(SmtpSettingsVO.NULL_ALLOWED_SMTP_TIME_INTERVAL);
		timeInterval.addModifyListener(modifySupport);

		// set temporarily the highest max value to both widgets to make their width equal
		int highestMaxValue = Math.max(SmtpSettingsVO.MAX_VALUE_SMTP_CHUNK_SIZE, SmtpSettingsVO.MAX_VALUE_SMTP_TIME_INTERVAL);
		chunkSize.setMaximum(highestMaxValue);
		timeInterval.setMaximum(highestMaxValue);
		WidgetSizer.setWidth(chunkSize);
		WidgetSizer.setWidth(timeInterval);

		// finally set individual max values
		chunkSize.setMaximum(SmtpSettingsVO.MAX_VALUE_SMTP_CHUNK_SIZE);
		timeInterval.setMaximum(SmtpSettingsVO.MAX_VALUE_SMTP_TIME_INTERVAL);


		return group;
	}


	@Override
	public boolean setFocus() {
		return host.setFocus();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Synching and Modifying
	// *

	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets() {
		if (smtpSettingsVO != null) {
			smtpSettingsVO.setHost( host.getText() );
			smtpSettingsVO.setUser( user.getText() );
			smtpSettingsVO.setPassword( password.getText() );
			smtpSettingsVO.setPort( port.getValue() );
			smtpSettingsVO.setSsl( sslButton.getSelection() );
			smtpSettingsVO.setTsl( tlsButton.getSelection() );
			smtpSettingsVO.setSslPort( sslPort.getValue() );

			Integer chunkSizeValue = chunkSize.getValueAsInteger();
			if (chunkSizeValue == null) {
				chunkSizeValue = SmtpSettingsVO.DEFAULT_CHUNK_SIZE;
			}
			smtpSettingsVO.setChunkSize(chunkSizeValue);

			Integer timeIntervalValue = timeInterval.getValueAsInteger();
			if (timeIntervalValue == null) {
				timeIntervalValue = SmtpSettingsVO.DEFAULT_TIME_INTERVAL;
			}
			smtpSettingsVO.setTimeInterval(timeIntervalValue);
		}
	}


	/**
	 * Show the entity's properties to the widgets
	 */
	private void syncWidgetsToEntity() {
		if (smtpSettingsVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						host.setText( avoidNull( smtpSettingsVO.getHost() ) );
						user.setText( avoidNull( smtpSettingsVO.getUser() ) );
						password.setText( avoidNull( smtpSettingsVO.getPassword() ) );
						port.setValue( smtpSettingsVO.getPort() );

						noEncryptionButton.setSelection( ! (smtpSettingsVO.isSsl()|| smtpSettingsVO.isTsl()));
						sslButton.setSelection( smtpSettingsVO.isSsl() );
						tlsButton.setSelection( smtpSettingsVO.isTsl() );
						sslPort.setValue( smtpSettingsVO.getSslPort() );
						sslPort.setEnabled( sslButton.getSelection() );
						chunkSize.setValue( smtpSettingsVO.getChunkSize() );
						timeInterval.setValue( smtpSettingsVO.getTimeInterval() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	// *
	// * Synching and Modifying
	// **************************************************************************


	public void setSmtpSettingsVO(SmtpSettingsVO smtpSettingsVO) {
		this.smtpSettingsVO = smtpSettingsVO;
		syncWidgetsToEntity();
	}

}
