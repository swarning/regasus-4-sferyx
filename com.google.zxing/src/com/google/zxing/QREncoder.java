package com.google.zxing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

/**
 * {@link QREncoder} encodes a QR code image for a given {@link String} content. Encoding a QR code image will be done
 * in two steps as described followed:
 * <ol>
 * <li>Just create a {@link QRCode} instance by invoking the static method <code>QREncoder.encode(String content)</code>
 * with a String parameter. This returned QR code representing the information of the content, but doesn't representing
 * the image as byte code</li>
 * <li>In this step the created {@link QRCode} instance will be encoded as an image. By invoking the method e.g.
 * <code>QREncoder.encodeGIF(QRCode qrCode, int factor, int frame ,int pointPerCM)</code> an instance of {@link QRCodeImage}
 * with byte array and meta information of the image.</li>
 * </ol>
 */
public class QREncoder {
	public static final int BLACK = 0x000000;
	public static final int WHITE = 0xFFFFFF;

	/**
	 * the factor the byte matrix in the QR code translated into an image
	 */
	protected static int FACTOR = 10;

	/**
	 * default value for the security frame of the QR code image
	 */
	protected static int SECURITY_FRAME_WIDTH = 3;

	/**
	 * default value for the resolution of the created image
	 */
	protected static int POINT_PER_CM = 100;

	/**
	 * Logger of this class
	 */
	private static Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());


	private static QREncoder instance;


	private QREncoder() {
	}


	public static QREncoder getInstance() {
		if (instance == null) {
			instance = new QREncoder();
		}
		return instance;
	}


	/**
	 * Encode a String as QR Code.
	 * @param content represented as a String
	 * @return an instance of {@link QRCode} representing the String
	 * @throws WriterException will be thrown if encoding failed
	 */
	public static QRCode encode(String content) throws WriterException {
		QRCode qrCode = Encoder.encode(content, ErrorCorrectionLevel.H);
		return qrCode;
	}


	/**
	 * encodes for the given instance of {@link QRCode} an instance of {@link QRCodeImage} representing the image and its meta information in GIF data format
	 * @param qr QRCode
	 * @return an instance of QRCodeImage
	 */
	public static QRCodeImage encodeGIF(QRCode qr) {
		return encodeGIF(qr, FACTOR, SECURITY_FRAME_WIDTH, POINT_PER_CM);
	}


	/**
	 * encodes for the given instance of {@link QRCode} an instance of {@link QRCodeImage} representing the image and its meta information in GIF data format
	 * @param qr QRCode
	 * @param factor int value for transforming the byte matrix to an image. Factor is the transformation factor.
	 * @param frame int value for the security frame around the qr code (in pixel)
	 * @param pointPerCM is the resolution
	 * @return an instance of QRCodeImage
	 */
	public static QRCodeImage encodeGIF(QRCode qr, int factor, int frame, int pointPerCM) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encodeGIF(qr, outputStream, factor, frame);
		byte[] imageBytes = outputStream.toByteArray();


		QRCodeImage imageContainer = new QRCodeImage(
			imageBytes,
			ImageType.GIF,
			(qr.getMatrix().getWidth()*factor)+(2*frame),
			(qr.getMatrix().getHeight()*factor)+(2*frame),
			pointPerCM
		);

		return imageContainer;
	}


	/**
	 * encodes for the given instance of {@link QRCode} an instance of {@link QRCodeImage} representing the image and its meta information in JPG data format
	 * @param qr QRCode
	 * @return an instance of QRCodeImage
	 */
	public static QRCodeImage encodeJPEG(QRCode qr) {
		return encodeJPEG(qr, FACTOR, SECURITY_FRAME_WIDTH, POINT_PER_CM);
	}


	/**
	 * encodes for the given instance of {@link QRCode} an instance of {@link QRCodeImage} representing the image and its meta information in JPG data format
	 * @param qr QRCode
	 * @param factor int value for transforming the byte matrix to an image. Factor is the transformation factor.
	 * @param frame int value for the security frame around the qr code (in pixel)
	 * @param pointPerCM is the resolution
	 * @return an instance of QRCodeImage
	 */
	public static QRCodeImage encodeJPEG(QRCode qr, int factor, int frame, int pointPerCM) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encodeJPEG(qr, outputStream, factor, frame);
		byte[] imageBytes = outputStream.toByteArray();

		int twoFrames = frame + frame;

		QRCodeImage imageContainer = new QRCodeImage(
			imageBytes,
			ImageType.JPG,
			(qr.getMatrix().getWidth()  * factor) + twoFrames,
			(qr.getMatrix().getHeight() * factor) + twoFrames,
			pointPerCM
		);

		return imageContainer;
	}


	/**
	 * encodes for the given instance of {@link QRCode} an instance of {@link QRCodeImage} representing the image and its meta information in PNG data format
	 * @param qr QRCode
	 * @return an instance of QRCodeImage
	 */
	public static QRCodeImage encodePNG(QRCode qr) {
		return encodePNG(qr, FACTOR, SECURITY_FRAME_WIDTH, POINT_PER_CM);
	}


	/**
	 * encodes for the given instance of {@link QRCode} an instance of {@link QRCodeImage} representing the image and its meta information in PNG data format
	 * @param qr QRCode
	 * @param factor int value for transforming the byte matrix to an image. Factor is the transformation factor.
	 * @param frame int value for the security frame around the qr code (in pixel)
	 * @param pointPerCM is the resolution
	 * @return an instance of QRCodeImage
	 */
	public static QRCodeImage encodePNG(QRCode qr, int factor, int frame ,int pointPerCM) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encodePNG(qr, outputStream, factor, frame);
		byte[] imageBytes = outputStream.toByteArray();

		int twoFrames = frame + frame;

		QRCodeImage imageContainer = new QRCodeImage(
			imageBytes,
			ImageType.PNG,
			(qr.getMatrix().getWidth()  * factor) + twoFrames,
			(qr.getMatrix().getHeight() * factor) + twoFrames,
			pointPerCM
		);

		return imageContainer;
	}


	public static void saveToGIF(QRCode qr, String fileName) throws FileNotFoundException {
		File file = new File(fileName);
        file.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        encodeGIF(qr, fileOutputStream, FACTOR, SECURITY_FRAME_WIDTH);
	}


	public static void saveToJPEG(QRCode qr, String fileName) throws FileNotFoundException {
		File file = new File(fileName);
        file.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        encodeJPEG(qr, fileOutputStream, FACTOR, SECURITY_FRAME_WIDTH);
	}


	public static void saveToPNG(QRCode qr, String fileName) throws FileNotFoundException {
		File file = new File(fileName);
        file.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        encodePNG(qr, fileOutputStream, FACTOR, SECURITY_FRAME_WIDTH);
	}


	private static void encodeGIF(QRCode qr, OutputStream outputStream, int factor, int frame) {
		String v = java.lang.System.getProperty("java.version");
		if (v.indexOf("1.1") == 0) {
			throw new RuntimeException("Java Version 1.1 is not supported.");
		}
		BufferedImage image = null;
		try {
			image = createBufferedImage(qr, factor, frame);

			// encode buffered image to a gif
			ImageIO.write(image, "gif", outputStream);
			image.flush();
			outputStream.close();
		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Couldn't encode qr code as GIF image ", e);
		}
		finally {
			try {
				if (image != null) {
					image.flush();
				}
				outputStream.close();
			}
			catch (Exception e){
				log.log(Level.SEVERE, "Couldn't flush the created image and close the outputStream. ", e);
			}
		}
	}


	private static void encodeJPEG(QRCode qr, OutputStream outputStream, int factor, int frame) {
		String v = java.lang.System.getProperty("java.version");
		if (v.indexOf("1.1") == 0) {
			throw new RuntimeException("Java Version 1.1 is not supported.");
		}

		BufferedImage image = null;
		try {
			image = createBufferedImage(qr, factor, frame);

			ImageIO.write(image, "jpg", outputStream);
			image.flush();
			outputStream.close();
		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Couldn't encode qr code as JPEG image ", e);
		}
		finally {
			try {
				if (image != null) {
					image.flush();
				}
				outputStream.close();
			}
			catch (Exception e){
				log.log(Level.SEVERE, "Couldn't flush the created image and close the outputStream. ", e);
			}
		}
	}


	private static void encodePNG(QRCode qr, OutputStream outputStream, int factor, int frame) {
		String v = java.lang.System.getProperty("java.version");
		if (v.indexOf("1.1") == 0) {
			throw new RuntimeException("Java Version 1.1 is not supported.");
		}

		BufferedImage image = null;
		try {
			image = createBufferedImage(qr, factor, frame);
			ImageIO.write(image, "png", outputStream);
			image.flush();
			outputStream.close();
		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Couldn't encode qr code as PNG image ", e);
		}
		finally {
			try {
				if (image != null) {
					image.flush();
				}
				outputStream.close();
			}
			catch (Exception e){
				log.log(Level.SEVERE, "Couldn't flush the created image and close the outputStream. ", e);
			}
		}
	}


	protected static BufferedImage createBufferedImage(QRCode qrCode, int factor, int frame) {
		boolean[][] booleanMatrix = createBooleanMatrix(qrCode.getMatrix(), factor, frame);

		BufferedImage image = new BufferedImage(
			booleanMatrix[0].length, // width
			booleanMatrix.length,    // height
			BufferedImage.TYPE_INT_RGB
		);

		for (int x = 0; x < booleanMatrix.length; x++) {
			for (int y = 0; y < booleanMatrix[x].length; y++) {
				int color = booleanMatrix[x][y] ? BLACK : WHITE;
				image.setRGB(x, y, color);
			}
		}

		return image;
	}


	/**
	 * Convert the byteMatrix to a boolean array where true stands for black.
	 * The actual algorithm is implemented in this method because it is more convenient for unit
	 * tests to check a boolean array than a BufferedImage.
	 *
	 * This algorithm is a bit less efficient than setting the colors directly in the BufferedImage,
	 * but is is much simpler and independent from graphical classes like BufferedImage.
	 *
	 * @param byteMatrix
	 * @param factor to enlarge the image
	 * @param frame size of the security frame around the QR code
	 * @return
	 */
	protected static boolean[][] createBooleanMatrix(ByteMatrix byteMatrix, int factor, int frame) {
		if (factor < 1) {
			throw new IllegalArgumentException("Parameter factor must be greater or equal than 1");
		}
		if (frame < 0) {
			throw new IllegalArgumentException("Paraemter frame must be greater or equal than 0");
		}

		int matrixHeight = byteMatrix.getHeight();
		int matrixWidth = byteMatrix.getWidth();
		int twoFrames = frame + frame;
		int height = (matrixHeight * factor) + twoFrames;
		int width =  (matrixWidth  * factor) + twoFrames;

		/* Remember that the default value of a boolean is false, so white fields (including the
		 * frame) have to be set.
		 */
		boolean[][] result = new boolean[height][width];

		// set black fields to true
		int row = frame;
		int col = frame;
		for (int x = 0; x < matrixHeight; x++) {
			for (int fRow = 0; fRow < factor; fRow++) {
				col = frame;
				for (int y = 0; y < matrixWidth; y++) {
					for (int fCol = 0; fCol < factor; fCol++) {
						if (byteMatrix.get(x, y) != 0) {
							result[row][col] = true;
						}
						col++;
					}
				}
				row++;
			}
		}

		return result;
	}

}
