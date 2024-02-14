package com.lambdalogic.util.rcp.widget;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.zxing.QRCodeImage;
import com.google.zxing.QREncoder;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.encoder.QRCode;


public class QRCodeComposite extends Composite implements DisposeListener {

	private Label imageLabel;
	private Image image;


	public QRCodeComposite(Composite parent, int style) {
		super(parent, style);

		setLayout( new FillLayout() );

		imageLabel = new Label(this, SWT.NONE);

		addDisposeListener(this);
	}


	public void setContent(String content) throws Exception {
		// Dispose existing image if someone sets a content twice
		if (image != null) {
			image.dispose();
		}

		image = createQRCodeImage(content);

		imageLabel.setImage(image);
	}


	@Override
	public Point getSize() {
		if (image != null) {
			Rectangle bounds = image.getBounds();
			return new Point(bounds.width, bounds.height);
		}
		else {
			return new Point(0, 0);
		}
	}


	private Image createQRCodeImage(String content) throws WriterException, IOException {
		QRCode qrCode = QREncoder.encode(content);

		QRCodeImage qrCodeImage = QREncoder.encodePNG(
			qrCode,
			5,		// factor: value for transforming the byte matrix to an image. Factor is the transformation factor.
			0,		// frame: value for the security frame around the qr code (in pixel)
			72		// pointPerCM: the resolution
		);
		byte[] qrCodeContent = qrCodeImage.getContent();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(qrCodeContent);

		return new Image(getDisplay(), inputStream);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (image != null) {
			image.dispose();
		}
	}

}
