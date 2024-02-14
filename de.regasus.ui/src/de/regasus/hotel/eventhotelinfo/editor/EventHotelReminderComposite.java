package de.regasus.hotel.eventhotelinfo.editor;

import static com.lambdalogic.util.StringHelper.*;
import static de.regasus.LookupService.getEventHotelInfoMgr;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderStatus;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class EventHotelReminderComposite extends Group {

	// *************************************************************************
	// * Widgets and other Attributes
	// *

	/**
	 * The entity
	 */
	private EventHotelReminderVO eventHotelReminderVO;

	/**
	 * The listeners who are to be notified when the set of attached files changes
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	private DateTimeComposite reminderDateTimeComposite;
	private DateTimeComposite eventDateTimeComposite;

	private Text subjectText;
	private MultiLineText textText;
	private Text emailText;

	/**
	 * A flag that tells whether the textText is expanded
	 */
	protected boolean textTextExpanded = false;

	/**
	 * The button to switch the size of the remark text
	 */
	private ToolItem largerSmallerButton;


	/**
	 * An image showing a right arrow (next) as in Eclipse's compiler preferences to indicate that
	 * here is unexpanded data.
	 */
	private Image nextImage;

	/**
	 * An image showing a down arrow as in Eclipse's compiler preferences to indicate that here is
	 * expanded data.
	 */
	private Image downImage;

	private ImageRegistry imageRegistry = de.regasus.ui.Activator.getDefault().getImageRegistry();

	private GridData textTextLayoutData;

	private Button removeButton;

	private Button sendTestMailButton;

	private Text statusText;

	private Label messageLabel;
	private Text messageText;

	private Label iconLabel;


	// *************************************************************************
	// * Constructor
	// *

	public EventHotelReminderComposite(Composite parent, int style) {
		super(parent, style);

		GridLayout layout = new GridLayout(7, false);
		setLayout(layout);


		/* In syncWidgetsToEntity() the row 2 is made unvisible by modifying the layoutData of
		 * iconLabel and messageText.
		 * So if you are changing the layoutData here, you have to check syncWidgetsToEntity(), too.
		 */

		// Row 1: Status

		Label statusLabel = new Label(this, SWT.NONE);
		statusLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		statusLabel.setText(KernelLabel.Status.getString());

		statusText = new Text(this, SWT.BORDER);
		statusText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		SWTHelper.disableTextWidget(statusText);

		iconLabel = new Label(this, SWT.RIGHT);
		iconLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1));


		// Row 2: message
		messageLabel = new Label(this, SWT.NONE);
		messageLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		messageLabel.setText(KernelLabel.Message.getString());

		messageText = new Text(this, SWT.BORDER);
		messageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		SWTHelper.disableTextWidget(messageText);


		// Row 3: Two times (pun intended) and an icon
		Label reminderTimeLabel = new Label(this, SWT.NONE);
		reminderTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		reminderTimeLabel.setText(HotelLabel.EventHotelReminderReminderTime.getString());

		reminderDateTimeComposite = new DateTimeComposite(this, SWT.BORDER);
		reminderDateTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
//		WidgetSizer.setWidth(reminderDateTimeComposite);

		reminderDateTimeComposite.addModifyListener(modifySupport);

		Label eventTimeLabel = new Label(this, SWT.NONE);
		GridData eventTimeLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		eventTimeLayoutData.horizontalIndent = 10;
		eventTimeLabel.setLayoutData(eventTimeLayoutData);
		eventTimeLabel.setText(HotelLabel.EventHotelReminderEventTime.getString());

		eventDateTimeComposite = new DateTimeComposite(this, SWT.BORDER);
		eventDateTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		WidgetSizer.setWidth(eventDateTimeComposite);

		eventDateTimeComposite.addModifyListener(modifySupport);


		// Row 4: Email addresses
		Label emailLabel = new Label(this, SWT.NONE);
		emailLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		emailLabel.setText(EmailLabel.Email.getString());

		emailText = new Text(this, SWT.BORDER);
		emailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		emailText.setTextLimit(200);

		emailText.addModifyListener(modifySupport);


		// Row 5: Subject
		Label subjectLabel = new Label(this, SWT.NONE);
		subjectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		subjectLabel.setText(EmailLabel.Subject.getString());

		subjectText = new Text(this, SWT.BORDER);
		subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		subjectText.setTextLimit(200);

		subjectText.addModifyListener(modifySupport);


		// Row 6: Text
		Label textLabel = new Label(this, SWT.NONE);
		textLabel.setText(EmailLabel.Text.getString());
		GridData remarkLabelLayoutData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		remarkLabelLayoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		textLabel.setLayoutData(remarkLabelLayoutData);

		ToolBar textToolBar = new ToolBar(this, SWT.FLAT);
		textToolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));


		nextImage = imageRegistry.get("next");
		downImage = imageRegistry.get("down");

		largerSmallerButton = new ToolItem(textToolBar, SWT.PUSH);
		largerSmallerButton.setImage(nextImage);

		textText = new MultiLineText(this, SWT.BORDER, false);
		textTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		textText.setLayoutData(textTextLayoutData);

		textText.addModifyListener(modifySupport);


		/* Button Row
		 * Layout is independent from the GridLayout of this EventHotelReminderComposite.
		 * Buttons appear on the right side and have equal width.
		 */

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,  false, false, 7, 1));
		buttonComposite.setLayout(new GridLayout(2, true));

		sendTestMailButton = new Button(buttonComposite, SWT.PUSH);
		sendTestMailButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,  true, false));
		sendTestMailButton.setText(I18N.SendTestMail);
		sendTestMailButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendTestMail();
			}
		});

		removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeButton.setText(UtilI18N.Remove);


		// What happens when the user switches the text size
		largerSmallerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textTextExpanded = !textTextExpanded;

				adaptTextText();
				SWTHelper.refreshSuperiorScrollbar(textText);
			}
		});
	}


	public void addRemoveListener(SelectionListener selectionListener) {
		removeButton.addSelectionListener(selectionListener);
	}


	public EventHotelReminderVO getEventHotelReminderVO() {
		return eventHotelReminderVO;
	}


	public void setEventHotelReminderVO(EventHotelReminderVO eventHotelReminderVO) {
		this.eventHotelReminderVO = eventHotelReminderVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventHotelReminderVO != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
			    		reminderDateTimeComposite.setI18NDateMinute( eventHotelReminderVO.getReminderTime() );
			    		eventDateTimeComposite.setI18NDateMinute( eventHotelReminderVO.getEventTime() );

			    		emailText.setText(avoidNull(eventHotelReminderVO.getEmail()));
			    		subjectText.setText(avoidNull(eventHotelReminderVO.getSubject()));
			    		textText.setText(avoidNull(eventHotelReminderVO.getText()));


			    		switch(eventHotelReminderVO.getStatus()) {
			    			case DONE:
			    				iconLabel.setImage(imageRegistry.get("flagGreen"));
			    				break;
			    			case ERROR:
			    			case ERROR_ACK:
			    				iconLabel.setImage(imageRegistry.get("flagRed"));
			    				break;
			    			default:
			    				iconLabel.setImage(null);
			    		}


			    		boolean editable = eventHotelReminderVO.getStatus() == EventHotelReminderStatus.OPEN;

			    		reminderDateTimeComposite.setEditable(editable);
			    		eventDateTimeComposite.setEditable(editable);
			    		SWTHelper.enableTextWidget(emailText, editable);
			    		SWTHelper.enableTextWidget(subjectText, editable);
			    		SWTHelper.enableTextWidget(textText, editable);

			    		removeButton.setEnabled(editable);
			    		sendTestMailButton.setEnabled(editable);

			    		// Always read only anyway
			    		statusText.setText(eventHotelReminderVO.getStatus().getString());
			    		String message = eventHotelReminderVO.getStatusMessage();
			    		if (StringHelper.isNotEmpty(message)) {
			    			messageText.setText(message);

			    			messageLabel.setVisible(true);
			    			messageText.setVisible(true);

			    			/* Restore original layoutData of iconLabel and messageText to show messageLabel
			    			 * and messageText in a seperate row.
			    			 */

			    			iconLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1));
			    			messageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));

			    			/* Code that has not the wanted effect to make the row of messageLabel and
			    			 * messageText totally invisible. There is always a small gab.
			    			 */
//			    			((GridData) messageLabel.getLayoutData()).heightHint = SWT.DEFAULT;
//			    			((GridData) messageText.getLayoutData()).heightHint = SWT.DEFAULT;
			    		}
			    		else {
			    			messageLabel.setVisible(false);
			    			messageText.setVisible(false);

			    			/* Make the row of messageLabel and messageText invisible by using a trick:
			    			 * Move messageLabel and messageText to the row above by setting the verticalSpan
			    			 * of its last widget iconLabel from 4 to 2 and of messageText from 5 to 1.
			    			 * So all widgets of row 1 and 2 fit into row 1 what "removes" row 2.
			    			 */

			    			iconLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));
			    			messageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			    			/* Code that has not the wanted effect to make the row of messageLabel and
			    			 * messageText totally invisible. There is always a small gab.
			    			 */
//			    			((GridData) messageLabel.getLayoutData()).heightHint = 0;
//			    			((GridData) messageText.getLayoutData()).heightHint = 0;
//			    			messageLabel.setSize(0, 0);
//			    			messageText.setSize(0, 0);
//			    			((GridData) messageLabel.getLayoutData()).verticalIndent = 0;
//			    			((GridData) messageLabel.getLayoutData()).minimumHeight = 0;
			    		}


			    		adaptTextText();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		if (eventHotelReminderVO != null) {
			// statusText and messageText are read only
			eventHotelReminderVO.setSubject( trim(subjectText.getText()) );
			eventHotelReminderVO.setReminderTime( reminderDateTimeComposite.getI18NDateMinute() );
			eventHotelReminderVO.setText( trim(textText.getText()) );
			eventHotelReminderVO.setEventTime( eventDateTimeComposite.getI18NDateMinute() );
			eventHotelReminderVO.setEmail( trim(emailText.getText()) );
		}
	}


	@Override
	protected void checkSubclass() {
	}

	// *************************************************************************
	// * Internal methods
	// *

	private void adaptTextText() {
		int lineCount;
		if (textTextExpanded) {
			String string = textText.getText();
			// Use the actual line count if not empty
			if (!StringHelper.isEmpty(string)) {
				StringHelper.getLines(string).size();
				lineCount = StringHelper.getLines(string).size();
			}
			else {
				lineCount = 10;
			}
			largerSmallerButton.setImage(downImage);
		}
		else {
			lineCount = 1;
			largerSmallerButton.setImage(nextImage);
		}
		int height = SWTHelper.computeTextWidgetHeightForLineCount(textText, lineCount);
		textTextLayoutData.heightHint = height;
		getParent().pack();
	}


	/**
	 * Send test mail for this composite's reminder VO and use status and message of returned to
	 * show info or error dialog.
	 */
	protected void sendTestMail() {
		try {
			// Put current changes in widgets to the reminderVO, explicitly without saving it because
			// it is only a test and possibly gets changed by the user
			syncEntityToWidgets();

			// The returned reminder is not stored in the DB and should NOT be made put in any model
			EventHotelReminderVO reminder = getEventHotelInfoMgr().send(eventHotelReminderVO, true);

			// Extract status and message
			String message = reminder.getStatusMessage();
			EventHotelReminderStatus status = reminder.getStatus();

			// Show dialog depending on status
			if (status == EventHotelReminderStatus.DONE) {
				MessageDialog.openInformation(getShell(), UtilI18N.Success, message);
			}
			else {
				MessageDialog.openError(getShell(), UtilI18N.Error, message);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

	// *
	// * Internal methods
	// *************************************************************************

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
