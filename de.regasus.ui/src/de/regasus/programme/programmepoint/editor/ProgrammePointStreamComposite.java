package de.regasus.programme.programmepoint.editor;

import static com.lambdalogic.util.StringHelper.*;
import static de.regasus.LookupService.getProgrammePointMgr;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.BrowserHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.programmepoint.combo.StreamProviderCombo;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;

public class ProgrammePointStreamComposite extends Composite {

	// the entity
	private ProgrammePointVO programmePointVO;

	// modifyListeners
	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private Button liveStreamAvailableButton;
	private Button liveStreamFreeButton;
	private Button openLiveStreamButton;
	private StreamProviderCombo liveStreamProviderCombo;
	private Label liveStreamUrlLabel;
	private Text liveStreamUrlText;

	private Button videoStreamAvailableButton;
	private Button videoStreamFreeButton;
	private Button openVideoStreamButton;
	private StreamProviderCombo videoStreamProviderCombo;
	private Label videoStreamUrlLabel;
	private Text videoStreamUrlText;
	private DateTimeComposite videoStreamAvailableFromDateTime;
	private DateTimeComposite videoStreamAvailableUntilDateTime;
	private NullableSpinner videoStreamDurationSpinner;

	/**
	 * Status variable that shows if the data of any widget has been modified.
	 */
	private boolean dirty = false;


	private static GridDataFactory checkBoxGridDataFactory = GridDataFactory.fillDefaults();

	private static GridDataFactory openStreamButtonGridDataFactory = GridDataFactory
		.swtDefaults()
		.align(SWT.RIGHT, SWT.TOP);

	private static GridDataFactory labelGridDataFactory = GridDataFactory
		.swtDefaults()
		.align(SWT.RIGHT, SWT.CENTER);

	private static GridDataFactory controlGridDataFactory = GridDataFactory
		.swtDefaults()
		.align(SWT.FILL, SWT.CENTER);

	public ProgrammePointStreamComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setLayout(new GridLayout());

		GridDataFactory gridDataFactory = GridDataFactory
			.fillDefaults()
			.grab(true, false);

		gridDataFactory.applyTo( createLiveStreamWidgets(this) );
		gridDataFactory.applyTo( createVideoStreamWidgets(this) );

		modifySupport.addListener(modifyListener);
	}


	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			dirty = true;
			updateButtonStates();
		}
	};


	private Composite createLiveStreamWidgets(Composite parent) throws Exception {
		// Group
		Group group = new Group(parent, SWT.NONE);

		group.setText(I18N.ProgrammePointStreamComposite_LiveStreamGroup_Text);

		group.setLayout(new GridLayout(3, false));

		// liveStreamAvailable
		{
    		new Label(group, SWT.NONE);

    		liveStreamAvailableButton = new Button(group, SWT.CHECK);
    		liveStreamAvailableButton.setText( ProgrammePointVO.LIVE_STREAM_AVAILABLE.getString() );
    		liveStreamAvailableButton.setToolTipText( ProgrammePointVO.LIVE_STREAM_AVAILABLE.getDescription() );
    		checkBoxGridDataFactory.applyTo(liveStreamAvailableButton);
    		liveStreamAvailableButton.addSelectionListener(modifySupport);
		}

		// openLiveStream
		{
    		openLiveStreamButton = new Button(group, SWT.PUSH);
    		openLiveStreamButton.setText(I18N.OpenLiveStream);
    		openLiveStreamButton.setToolTipText(I18N.OpenLiveStream_Description);
    		openStreamButtonGridDataFactory.applyTo(openLiveStreamButton);

    		openLiveStreamButton.addListener(SWT.Selection, e -> openLiveStream());
		}

		// liveStreamFree
		{
    		new Label(group, SWT.NONE);

    		liveStreamFreeButton = new Button(group, SWT.CHECK);
    		liveStreamFreeButton.setText( ProgrammePointVO.LIVE_STREAM_FREE.getString() );
    		liveStreamFreeButton.setToolTipText( ProgrammePointVO.LIVE_STREAM_FREE.getDescription() );
    		checkBoxGridDataFactory.applyTo(liveStreamFreeButton);
    		liveStreamFreeButton.addSelectionListener(modifySupport);

    		new Label(group, SWT.NONE);
		}

		// liveStreamProvider
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ProgrammePointVO.LIVE_STREAM_PROVIDER.getString() );
			label.setToolTipText( ProgrammePointVO.LIVE_STREAM_PROVIDER.getDescription() );

			liveStreamProviderCombo = new StreamProviderCombo(group, SWT.NONE);
			controlGridDataFactory.applyTo(liveStreamProviderCombo);
			liveStreamProviderCombo.addModifyListener(modifySupport);

			new Label(group, SWT.NONE);
		}

		// liveStreamUrl;
		{
			liveStreamUrlLabel = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(liveStreamUrlLabel);
			liveStreamUrlLabel.setText( ProgrammePointVO.LIVE_STREAM_URL.getString() );
			liveStreamUrlLabel.setToolTipText( ProgrammePointVO.LIVE_STREAM_URL.getDescription() );

			liveStreamUrlText = new Text(group, SWT.BORDER);
    		controlGridDataFactory.copy().span(2, 1).grab(true, false).applyTo(liveStreamUrlText);
    		liveStreamUrlText.setTextLimit( ProgrammePointVO.LIVE_STREAM_URL.getMaxLength() );

    		liveStreamUrlText.addModifyListener(modifySupport);
		}

		return group;
	}


	private Composite createVideoStreamWidgets(Composite parent) throws Exception {
		// Group
		Group group = new Group(parent, SWT.NONE);

		group.setText(I18N.ProgrammePointStreamComposite_VideoStreamGroup_Text);

		group.setLayout(new GridLayout(3, false));

		// videoStreamAvailable
		{
    		new Label(group, SWT.NONE);

    		videoStreamAvailableButton = new Button(group, SWT.CHECK);
    		videoStreamAvailableButton.setText( ProgrammePointVO.VIDEO_STREAM_AVAILABLE.getString() );
    		videoStreamAvailableButton.setToolTipText( ProgrammePointVO.VIDEO_STREAM_AVAILABLE.getDescription() );
    		checkBoxGridDataFactory.applyTo(videoStreamAvailableButton);
    		videoStreamAvailableButton.addSelectionListener(modifySupport);
		}

		// openVideoStream
		{
    		openVideoStreamButton = new Button(group, SWT.PUSH);
    		openVideoStreamButton.setText(I18N.OpenVideoStream);
    		openVideoStreamButton.setToolTipText(I18N.OpenVideoStream_Description);
    		openStreamButtonGridDataFactory.applyTo(openVideoStreamButton);

    		openVideoStreamButton.addListener(SWT.Selection, e -> openVideoStream());
		}

		// videoStreamFree
		{
    		new Label(group, SWT.NONE);

    		videoStreamFreeButton = new Button(group, SWT.CHECK);
    		videoStreamFreeButton.setText( ProgrammePointVO.VIDEO_STREAM_FREE.getString() );
    		videoStreamFreeButton.setToolTipText( ProgrammePointVO.VIDEO_STREAM_FREE.getDescription() );
    		checkBoxGridDataFactory.applyTo(videoStreamFreeButton);
    		videoStreamFreeButton.addSelectionListener(modifySupport);

    		new Label(group, SWT.NONE);
		}

		// videoStreamProvider
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ProgrammePointVO.VIDEO_STREAM_PROVIDER.getString() );
			label.setToolTipText( ProgrammePointVO.VIDEO_STREAM_PROVIDER.getDescription() );

			videoStreamProviderCombo = new StreamProviderCombo(group, SWT.NONE);
			controlGridDataFactory.applyTo(videoStreamProviderCombo);
			videoStreamProviderCombo.addModifyListener(modifySupport);

			new Label(group, SWT.NONE);
		}

		// videoStreamUrl;
		{
			videoStreamUrlLabel = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(videoStreamUrlLabel);
			videoStreamUrlLabel.setText( ProgrammePointVO.VIDEO_STREAM_URL.getString() );
			videoStreamUrlLabel.setToolTipText( ProgrammePointVO.VIDEO_STREAM_URL.getDescription() );

			videoStreamUrlText = new Text(group, SWT.BORDER);
			controlGridDataFactory.copy().span(2, 1).grab(true, false).applyTo(videoStreamUrlText);
    		videoStreamUrlText.setTextLimit( ProgrammePointVO.VIDEO_STREAM_URL.getMaxLength() );

    		videoStreamUrlText.addModifyListener(modifySupport);
		}

		// videoStreamAvailableFrom
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ProgrammePointVO.VIDEO_STREAM_AVAILABLE_FROM.getString() );
			label.setToolTipText( ProgrammePointVO.VIDEO_STREAM_AVAILABLE_FROM.getDescription() );

			videoStreamAvailableFromDateTime = new DateTimeComposite(group, SWT.NONE);
			controlGridDataFactory.applyTo(videoStreamAvailableFromDateTime);
			WidgetSizer.setWidth(videoStreamAvailableFromDateTime);

			new Label(group, SWT.NONE);

			videoStreamAvailableFromDateTime.addModifyListener(modifySupport);
		}

		// videoStreamAvailableUntil
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ProgrammePointVO.VIDEO_STREAM_AVAILABLE_UNTIL.getString() );
			label.setToolTipText( ProgrammePointVO.VIDEO_STREAM_AVAILABLE_UNTIL.getDescription() );

			videoStreamAvailableUntilDateTime = new DateTimeComposite(group, SWT.NONE);
			controlGridDataFactory.applyTo(videoStreamAvailableUntilDateTime);
			WidgetSizer.setWidth(videoStreamAvailableUntilDateTime);

			new Label(group, SWT.NONE);

			videoStreamAvailableUntilDateTime.addModifyListener(modifySupport);
		}

		// videoStreamDuration
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ProgrammePointVO.VIDEO_STREAM_DURATION.getString() );
			label.setToolTipText( ProgrammePointVO.VIDEO_STREAM_DURATION.getDescription() );

			videoStreamDurationSpinner = new NullableSpinner(group, SWT.NONE);
			controlGridDataFactory.applyTo(videoStreamDurationSpinner);
			videoStreamDurationSpinner.setMinimum( ProgrammePointVO.VIDEO_STREAM_DURATION.getMin().intValue() );
			videoStreamDurationSpinner.setMaximum( ProgrammePointVO.VIDEO_STREAM_DURATION.getMax().intValue() );
			WidgetSizer.setWidth(videoStreamDurationSpinner);

			Label minuteLabel = new Label(group, SWT.NONE);
			minuteLabel.setText( KernelLabel.Minutes.getString() );
			GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(minuteLabel);

			videoStreamDurationSpinner.addModifyListener(modifySupport);
		}

	    return group;
	}


	/**
	 * Set programm point VO entity in all GUI components that need it.
	 * @param programmePointVO Programm point VO to set.
	 */
	public void setProgrammePointVO(ProgrammePointVO programmePointVO) {
		this.programmePointVO = programmePointVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (programmePointVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						liveStreamAvailableButton.setSelection( programmePointVO.isLiveStreamAvailable() );
						liveStreamFreeButton.setSelection( programmePointVO.isLiveStreamFree() );
						liveStreamProviderCombo.setStreamProvider( programmePointVO.getLiveStreamProvider() );
						liveStreamUrlText.setText( avoidNull(programmePointVO.getLiveStreamUrl()) );

						videoStreamAvailableButton.setSelection( programmePointVO.isVideoStreamAvailable() );
						videoStreamFreeButton.setSelection( programmePointVO.isVideoStreamFree() );
						videoStreamProviderCombo.setStreamProvider( programmePointVO.getVideoStreamProvider() );
						videoStreamUrlText.setText( avoidNull(programmePointVO.getVideoStreamUrl()) );
						videoStreamAvailableFromDateTime.setI18NDateMinute( programmePointVO.getVideoStreamAvailableFrom() );
						videoStreamAvailableUntilDateTime.setI18NDateMinute( programmePointVO.getVideoStreamAvailableUntil() );
						videoStreamDurationSpinner.setValue( programmePointVO.getVideoStreamDuration() );

						dirty = false;
						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (programmePointVO != null) {
			programmePointVO.setLiveStreamAvailable( liveStreamAvailableButton.getSelection() );
			programmePointVO.setLiveStreamFree( liveStreamFreeButton.getSelection() );
			programmePointVO.setLiveStreamProvider( liveStreamProviderCombo.getStreamProvider() );
			programmePointVO.setLiveStreamUrl( liveStreamUrlText.getText() );

			programmePointVO.setVideoStreamAvailable( videoStreamAvailableButton.getSelection() );
			programmePointVO.setVideoStreamFree( videoStreamFreeButton.getSelection() );
			programmePointVO.setVideoStreamProvider( videoStreamProviderCombo.getStreamProvider() );
			programmePointVO.setVideoStreamUrl( videoStreamUrlText.getText() );
			programmePointVO.setVideoStreamAvailableFrom( videoStreamAvailableFromDateTime.getI18NDateMinute() );
			programmePointVO.setVideoStreamAvailableUntil( videoStreamAvailableUntilDateTime.getI18NDateMinute() );
			programmePointVO.setVideoStreamDuration( videoStreamDurationSpinner.getValueAsInteger() );
		}
	}


	private void updateButtonStates() {
		// enable/disable openLiveStreamButton
		{
    		boolean enabled =
    			   ! dirty
    			&& liveStreamAvailableButton.getSelection()
    			&& liveStreamProviderCombo.getStreamProvider() != null
    			&& (
    				! liveStreamProviderCombo.getStreamProvider().isUrlRequired()
    				||
    				isNotEmpty(liveStreamUrlText.getText())
    			);

    		openLiveStreamButton.setEnabled(enabled);
		}

		// enable/disable openVideoStreamButton
		{
			boolean enabled =
				   ! dirty
    			&& videoStreamAvailableButton.getSelection()
    			&& videoStreamProviderCombo.getStreamProvider() != null
    			&& (
    				! videoStreamProviderCombo.getStreamProvider().isUrlRequired()
    				||
    				isNotEmpty(videoStreamUrlText.getText())
    			);

    		openVideoStreamButton.setEnabled(enabled);
		}


		// visibility of liveStreamUrlText
		{
    		boolean visible =
    			   liveStreamProviderCombo.getStreamProvider() != null
    			&& liveStreamProviderCombo.getStreamProvider().isUrlRequired();

    		liveStreamUrlLabel.setVisible(visible);
    		liveStreamUrlText.setVisible(visible);
		}

		// visibility of videoStreamUrlText
		{
    		boolean visible =
    			   videoStreamProviderCombo.getStreamProvider() != null
    			&& videoStreamProviderCombo.getStreamProvider().isUrlRequired();

    		videoStreamUrlLabel.setVisible(visible);
    		videoStreamUrlText.setVisible(visible);
		}
	}


	private void openLiveStream() {
		try {
			Long programmePointPK = programmePointVO.getID();
			String userID = CurrentUserModel.getInstance().getModelData().getUserName();

			String url = getProgrammePointMgr().getPersonalLiveStreamUrlForUser(programmePointPK, userID);

			BrowserHelper.openBrowser(url);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void openVideoStream() {
		try {
			Long programmePointPK = programmePointVO.getID();
			String userID = CurrentUserModel.getInstance().getModelData().getUserName();

			String url = getProgrammePointMgr().getPersonalVideoStreamUrlForUser(programmePointPK, userID);

			BrowserHelper.openBrowser(url);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
