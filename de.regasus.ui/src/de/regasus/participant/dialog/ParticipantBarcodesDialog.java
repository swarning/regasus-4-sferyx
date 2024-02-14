package de.regasus.participant.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static de.regasus.LookupService.getBadgeMgr;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.BadgeVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.widget.QRCodeComposite;

import de.regasus.participant.ParticipantModel;


/**
 * A dialog showing the participant barcode and all badge barcodes.
 */
public class ParticipantBarcodesDialog extends Dialog {

	private static Point lastLocation = null;
	private static Point lastSize = null;

	private List<BadgeVO> badgeVOs;

	private Participant participant;

	private Font barcodeFont;

	private Font bigFont;

	public ParticipantBarcodesDialog(Shell parentShell, IParticipant iParticipant) throws Exception {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		// Get Fonts
		Activator activator = com.lambdalogic.util.rcp.Activator.getDefault();
		bigFont = activator.getFontFromRegistry(com.lambdalogic.util.rcp.Activator.BIG_FONT);
		barcodeFont = activator.getFontFromRegistry(com.lambdalogic.util.rcp.Activator.BARCODE_FONT);

		// Get participant and badges with barcode
		Long participantID = iParticipant.getPK();
		badgeVOs = getBadgeMgr().getBadgeVOListByParticipantPK(participantID);
		participant = ParticipantModel.getInstance().getParticipant(participantID);
	}


	/**
	 * Create contents of the dialog, showing the participant barcode and all badge barcodes
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new FillLayout());

		Color white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);

		ScrolledComposite scrolledComposite = new ScrolledComposite(dialogArea, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setBackground(white);

		Composite contentComposite = new Composite(scrolledComposite,SWT.NONE);
		scrolledComposite.setContent(contentComposite);

		contentComposite.setLayout(new GridLayout(3, false));
		contentComposite.setBackground(white);

		{
			// Header Participant Barcode
			Label label = new Label(contentComposite, SWT.CENTER);
			label.setText(ParticipantLabel.Participant.getString());
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			label.setBackground(white);
			label.setFont(bigFont);

			// Participant QRCode
			QRCodeComposite qrCodeComposite = new QRCodeComposite(contentComposite, SWT.NONE);
			try {
				qrCodeComposite.setContent(participant.getBarcode());
				GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1);
				layoutData.widthHint = qrCodeComposite.getSize().x;
				layoutData.heightHint = qrCodeComposite.getSize().y;
				qrCodeComposite.setLayoutData(layoutData);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}

			// Spacer
			Label spacer = new Label(contentComposite, SWT.NONE);
			spacer.setBackground(white);
			spacer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

			// Participant Barcode
			new Label(contentComposite, SWT.NONE);
			Text text1 = new Text(contentComposite, SWT.NONE);
			text1.setText(participant.getBarcode());
			text1.setBackground(white);

			// Use text instead of label, so you can copy and paste
			Text text2 = new Text(contentComposite, SWT.NONE);
			text2.setText(participant.getBarcodeCode128BWill());
			text2.setFont(barcodeFont);
			text2.setEditable(false);
			text2.setBackground(white);
		}


		if ( notEmpty(badgeVOs) ) {
			Label separatorLabel = new Label(contentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
			separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			separatorLabel.setBackground(white);

			// Header Badge Barcode
			Label label = new Label(contentComposite, SWT.CENTER);
			label.setText(ParticipantLabel.Badges.getString());
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			label.setBackground(white);
			label.setFont(bigFont);

			// Bade Barcodes
			for (BadgeVO badgeVO : badgeVOs) {
				Label noLabel = new Label(contentComposite, SWT.NONE);
				noLabel.setText(String.valueOf(badgeVO.getBadgeNo()));
				noLabel.setBackground(white);

				Text text1 = new Text(contentComposite, SWT.NONE);
				text1.setText(badgeVO.getBarcode());
				text1.setBackground(white);

				// Use text instead of label, so you can copy and paste
				Text text2 = new Text(contentComposite, SWT.NONE);
				text2.setText(badgeVO.getBarcodeCode128BWill());
				text2.setEditable(false);
				text2.setFont(barcodeFont);
				text2.setBackground(white);
			}
		}

		Point prefSize = scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinWidth(prefSize.x);
		scrolledComposite.setMinHeight(prefSize.y);

		return dialogArea;
	}


	/**
	 * Create one OK button in the button bar
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}


	/**
	 * Set the name of the participant in the shell's title
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText( participant.getName(true) );

		if (lastLocation != null) {
			newShell.setLocation(lastLocation);
		}

		if (lastSize != null) {
			newShell.setSize(lastSize);
		}
		else {
			newShell.setSize(300, 400);
		}
	}


	@Override
	public boolean close() {
		lastLocation = getShell().getLocation();
		lastSize = getShell().getSize();

		return super.close();
	}

}
