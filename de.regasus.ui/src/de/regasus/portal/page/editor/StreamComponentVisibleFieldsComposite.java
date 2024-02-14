package de.regasus.portal.page.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.util.rcp.EntityComposite;

import de.regasus.I18N;
import de.regasus.core.ui.CoreI18N;
import de.regasus.portal.component.StreamComponent;

public class StreamComponentVisibleFieldsComposite extends EntityComposite<StreamComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button showOmitLockedButtonButton;
	private Button showOmitUnavailableNowButtonButton;
	private Button showDescriptionButton;
	private Button showLiveStreamStartButton;
	private Button showLiveStreamEndButton;
	private Button showVideoStreamAvailableFromButton;
	private Button showVideoStreamAvailableUntilButton;
	private Button showVideoStreamDurationButton;

	// *
	// * Widgets
	// **************************************************************************


	public StreamComponentVisibleFieldsComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		final int COL_COUNT = 1;
		GridLayout layout = new GridLayout(COL_COUNT, false);
//		layout.horizontalSpacing = 0;
//		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		setLayout(layout);


		createButtonGroup(parent);
		createTextGroup(parent);
		createLiveStreamGroup(parent);
		createVideoStreamGroup(parent);
	}


	private void createButtonGroup(Composite parent) {
		Group group = createGroup(parent, CoreI18N.Buttons);

		showOmitLockedButtonButton = createButton(group, StreamComponent.FIELD_SHOW_OMIT_LOCKED_BUTTON.getString() );
		showOmitUnavailableNowButtonButton = createButton(group, StreamComponent.FIELD_SHOW_OMIT_UNAVAILABLE_NOW_BUTTON.getString() );
	}


	private void createTextGroup(Composite parent) {
		Group group = createGroup(parent, CoreI18N.TextFields);

		showDescriptionButton = createButton(group, StreamComponent.FIELD_SHOW_DESCRIPTION.getString() );
	}


	private void createLiveStreamGroup(Composite parent) {
		Group group = createGroup(parent, I18N.LiveStream);

		showLiveStreamStartButton = createButton(group,  StreamComponent.FIELD_SHOW_LIVE_STREAM_START.getString() );
		showLiveStreamEndButton = createButton(group,  StreamComponent.FIELD_SHOW_LIVE_STREAM_END.getString() );
	}


	private void createVideoStreamGroup(Composite parent) {
		Group group = createGroup(parent, I18N.VideoStream);

		showVideoStreamAvailableFromButton = createButton(group,  StreamComponent.FIELD_SHOW_VIDEO_STREAM_AVAILABLE_FROM.getString() );
		showVideoStreamAvailableUntilButton = createButton(group,  StreamComponent.FIELD_SHOW_VIDEO_STREAM_AVAILABLE_UNTIL.getString() );
		showVideoStreamDurationButton = createButton(group,  StreamComponent.FIELD_SHOW_VIDEO_STREAM_DURATION.getString() );
	}


	private Group createGroup(Composite parent, String label) {
		Group group = new Group(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
		group.setLayout( new GridLayout());
		group.setText(label);
		return group;
	}


	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(button);
		button.addSelectionListener(modifySupport);
		return button;
	}


	@Override
	protected void syncWidgetsToEntity() {
		showOmitLockedButtonButton.setSelection( entity.isShowOmitLockedButton() );
		showOmitUnavailableNowButtonButton.setSelection( entity.isShowOmitUnavailableNowButton() );
		showDescriptionButton.setSelection( entity.isShowDescription() );
		showLiveStreamStartButton.setSelection( entity.isShowLiveStreamStart() );
		showLiveStreamEndButton.setSelection( entity.isShowLiveStreamEnd() );
		showVideoStreamAvailableFromButton.setSelection( entity.isShowVideoStreamAvailableFrom() );
		showVideoStreamAvailableUntilButton.setSelection( entity.isShowVideoStreamAvailableUntil() );
		showVideoStreamDurationButton.setSelection( entity.isShowVideoStreamDuration() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowOmitLockedButton( showOmitLockedButtonButton.getSelection() );
			entity.setShowOmitUnavailableNowButton( showOmitUnavailableNowButtonButton.getSelection() );
			entity.setShowDescription( showDescriptionButton.getSelection() );
			entity.setShowLiveStreamStart( showLiveStreamStartButton.getSelection() );
			entity.setShowLiveStreamEnd( showLiveStreamEndButton.getSelection() );
			entity.setShowVideoStreamAvailableFrom( showVideoStreamAvailableFromButton.getSelection() );
			entity.setShowVideoStreamAvailableUntil( showVideoStreamAvailableUntilButton.getSelection() );
			entity.setShowVideoStreamDuration( showVideoStreamDurationButton.getSelection() );
		}
	}

}
