package de.regasus.event.editor;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

// REFERENCE
public class EventNextNumbersGroup extends Group {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Label nextParticipantNo;
	private Button changeNextParticipantNoButton;
	private Label nextBookingNo;
	private Label nextBadgeNo;

	// *
	// * Widgets
	// **************************************************************************


	public EventNextNumbersGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setLayout(new GridLayout(3, false));
		setText(I18N.EventEditor_Counters);


		// line 1: nextParticipantNo
		{
			Label nextParticipantNoLabel = new Label(this, SWT.NONE);
			nextParticipantNoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nextParticipantNoLabel.setText(ParticipantLabel.Event_NextParticipantNo.getString());

			nextParticipantNo = new Label(this, SWT.BORDER | SWT.RIGHT);
			nextParticipantNo.setText("1");

			// reserve enough width for maximum numbers
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
			int maxDigits = Participant.NUMBER.getIntegerDigits();
			gridData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(nextParticipantNo, maxDigits);
			nextParticipantNo.setLayoutData(gridData);
		}
		{
			changeNextParticipantNoButton = new Button(this, SWT.PUSH);
			changeNextParticipantNoButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			changeNextParticipantNoButton.setText(UtilI18N.Change);
			changeNextParticipantNoButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeNextParticipantNumber();
				}
			});
		}


		// line 2: nextBookingNo
		{
			Label nextBookingNoLabel = new Label(this, SWT.NONE);
			nextBookingNoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nextBookingNoLabel.setText(ParticipantLabel.Event_NextBookingNo.getString());

			nextBookingNo = new Label(this, SWT.BORDER | SWT.RIGHT);
			nextBookingNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			nextBookingNo.setText("1");

			new Label(this, SWT.NONE);
		}


		// line 3: nextBadgeNo
		{
			Label nextBadgeNoLabel = new Label(this, SWT.NONE);
			nextBadgeNoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nextBadgeNoLabel.setText(ParticipantLabel.Event_NextBadgeNo.getString());
			nextBadgeNo = new Label(this, SWT.BORDER | SWT.RIGHT);
			nextBadgeNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			nextBadgeNo.setText("1");

			new Label(this, SWT.NONE);
		}
	}


	public void setEvent(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						nextParticipantNo.setText( String.valueOf(eventVO.getNextParticipantNo()) );
						nextBookingNo.setText( String.valueOf(eventVO.getNextBookingNo()) );
						nextBadgeNo.setText( String.valueOf(eventVO.getNextBadgeNo()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			// An alternative initial nextParticipantNo can only be set via VO at creation time.
			if (eventVO.getPK() == null) {
				// No invalid value can occur here, see method changeNextParticipantNumber
				eventVO.setNextParticipantNo( Integer.valueOf(nextParticipantNo.getText()) );
			}
		}
	}


	protected void changeNextParticipantNumber() {
		try {
			boolean eventAlreadyExists = eventVO.getPK() != null;

			// When the user presses the button to enter an alternative
			// initial participant number, and the event has not yet been created,
			// no save is attempted anymore so that no error occurs (missing PK)
			if (eventAlreadyExists) {
				AbstractEditor.saveActiveEditor();
			}

			// Validate an entered nextParticipantNumber via arithmetical
			// values rather than pattern matching
			InputDialog inputDialog = new InputDialog(
				getShell(),
				UtilI18N.Input,
				ParticipantLabel.Event_NextParticipantNo.getString(),
				String.valueOf(eventVO.getNextParticipantNo()),

				new IInputValidator() {
					@Override
					public String isValid(String newText) {
						try {
							int no = Integer.parseInt(newText);
							if (1 <= no && no <= EventVO.MAX_NEXT_PARTICIPANT_NO) {
								return null;
							}
						}
						catch(Exception ignore) {}
						// Indicate invalid value without error message
						return "";
					}
				}
			);

			int open = inputDialog.open();
			if (open == Window.OK) {
				String value = inputDialog.getValue();
				Integer nextNumber = Integer.valueOf(value);

				if (eventAlreadyExists) {
					// If this action succeeds, it fires update event, so we don't need to do anything else
					EventModel.getInstance().updateNextParticipantNumber(eventVO, nextNumber);

					// If no exception happened, the value _could_ be set and may be shown
					nextParticipantNo.setText(value);
				}
				else {
					// The value may be shown because it will be used for creation
					nextParticipantNo.setText(value);
				}

			}
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
