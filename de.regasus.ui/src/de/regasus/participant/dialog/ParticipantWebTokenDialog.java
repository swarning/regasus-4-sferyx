package de.regasus.participant.dialog;

import java.security.interfaces.RSAPublicKey;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.LookupService;
import de.regasus.core.ServerModel;
import de.regasus.core.ui.dnd.CopyPasteButtonComposite;
import de.regasus.event.EventModel;
import de.regasus.util.JsonHelper;

/**
 * Dialog showing the Participant Web Token.
 */
public class ParticipantWebTokenDialog extends TitleAreaDialog {

	private static Point lastLocation = null;
	private static Point lastSize = null;


	private static JWTVerifier jwtVerifier;
	private IParticipant participant;

	// token data
	private String encodedToken;
	private String decodedHeader;
	private String decodedPayload;

	private MultiLineText tokenText;
	private MultiLineText headerText;
	private MultiLineText payloadText;


	public ParticipantWebTokenDialog(Shell parentShell, IParticipant participant) throws Exception {
		super(parentShell);

		this.participant = participant;

		setShellStyle(getShellStyle() | SWT.RESIZE);

		initJWTVerifier();
		loadToken();
	}


	private void initJWTVerifier() {
		if (jwtVerifier == null) {
    		RSAPublicKey publicKey = LookupService.getKernelMgr().getRSAPublicKey();
    		Algorithm regasusAlgorithm = Algorithm.RSA256(publicKey, null /*private key*/);
    		jwtVerifier = JWT.require(regasusAlgorithm).build();
		}
	}


	private void loadToken() throws Exception {
		// get token
		Long participantId = participant.getPK();
		String audience = buildAudience();
		encodedToken = LookupService.getParticipantMgr().buildParticipantWebToken(participantId, audience);

		// decode token
		DecodedJWT decodedJWT = jwtVerifier.verify(encodedToken);

		// decode header
		String header = decodedJWT.getHeader();
		decodedHeader = new String( java.util.Base64.getDecoder().decode(header) );

		// decode payload
		String payload = decodedJWT.getPayload();
		decodedPayload = new String( java.util.Base64.getDecoder().decode(payload) );

		final int indentWidth = 4;
		decodedPayload = JsonHelper.formatJSONStr(decodedPayload, indentWidth);
	}


	private String buildAudience() throws Exception {
		EventVO eventVO = EventModel.getInstance().getEventVO( participant.getEventId() );
		String url = eventVO.getDigitalEventUrl();
		if ( StringHelper.isEmpty(url) ) {
			url = ServerModel.getInstance().getHost();
		}

		String audience = url;
		try {
    		URIBuilder uriBuilder = new URIBuilder(url);
    		audience = uriBuilder.getHost();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return audience;
	}

	/**
	 * Create contents of the dialog, showing the participant barcode and all badge barcodes
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		String title = I18N.ParticipantWebTokenDialog_Title;
		title = title.replace("<name>", participant.getName());
		setTitle(title);

		String message = I18N.ParticipantWebTokenDialog_Message;
		message = message.replace("<number>", participant.getNumber().toString());
		setMessage(message);

		Image jwtImage = IconRegistry.getImage(IImageKeys.JWT);
		setTitleImage(jwtImage);



		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite contentComposite = new Composite(dialogArea, SWT.NONE);
		contentComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
		final int NUM_COLS = 2;
		contentComposite.setLayout(new GridLayout(NUM_COLS, false));



		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.BOTTOM);
		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(NUM_COLS, 1);
		GridDataFactory copyButtonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM);

		// Token
		{
			Label label = new Label(contentComposite, SWT.CENTER);
			label.setText("Encoded Participant Web Token");
			labelGridDataFactory.applyTo(label);

			Button copyUrlButton = CopyPasteButtonComposite.createCopyButton(contentComposite);
			copyButtonGridDataFactory.applyTo(copyUrlButton);
			copyUrlButton.addListener(SWT.Selection, e -> ClipboardHelper.copyToClipboard(encodedToken));

			tokenText = new MultiLineText(contentComposite, SWT.BORDER | SWT.WRAP, true);
			textGridDataFactory.applyTo(tokenText);
			tokenText.setEditable(false);
		}

		SWTHelper.verticalSpace(contentComposite);

		// Header
		{
			Label label = new Label(contentComposite, SWT.CENTER);
			label.setText("Decoded Header");
			labelGridDataFactory.applyTo(label);

			Button copyUrlButton = CopyPasteButtonComposite.createCopyButton(contentComposite);
			copyButtonGridDataFactory.applyTo(copyUrlButton);
			copyUrlButton.addListener(SWT.Selection, e -> ClipboardHelper.copyToClipboard(decodedHeader));

			headerText = new MultiLineText(contentComposite, SWT.BORDER | SWT.WRAP, true);
			textGridDataFactory.applyTo(headerText);
			headerText.setEditable(false);
		}

		SWTHelper.verticalSpace(contentComposite);

		// Payload
		{
			Label label = new Label(contentComposite, SWT.CENTER);
			label.setText("Decoded Payload");
			labelGridDataFactory.applyTo(label);

			Button copyUrlButton = CopyPasteButtonComposite.createCopyButton(contentComposite);
			copyButtonGridDataFactory.applyTo(copyUrlButton);
			copyUrlButton.addListener(SWT.Selection, e -> ClipboardHelper.copyToClipboard(decodedPayload));

			payloadText = new MultiLineText(contentComposite, SWT.BORDER | SWT.WRAP, true);
			textGridDataFactory.applyTo(payloadText);
			payloadText.setEditable(false);
		}


		syncWidgetsToEntity();

		return dialogArea;
	}


	private void syncWidgetsToEntity() {
		// copy values to widgets
		tokenText.setText(encodedToken);
		headerText.setText(decodedHeader);
		payloadText.setText(decodedPayload);
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
			newShell.setSize(600, 800);
		}
	}


	@Override
	public boolean close() {
		lastLocation = getShell().getLocation();
		lastSize = getShell().getSize();

		return super.close();
	}

}
