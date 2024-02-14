package com.lambdalogic.util.rcp.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.lambdalogic.util.geom.Rectangle;
import com.lambdalogic.util.image.ImageUtil;

/**
 * This class contains some algorithms to scale images.
 * They were programmed in the context of PortalPhotoComposite but did not solve the problem increase performance
 * while keeping good quality.
 * However, I wanna keep them for further experiments.
 */
public class ImageScaler {

	private static final boolean DEBUG = true;


	public static ImageData convertToSWT2(BufferedImage bufferedImage) throws IOException {
//		/2) awt.BufferedImage -> raw Data
		java.awt.image.WritableRaster awtRaster = bufferedImage.getRaster();
		java.awt.image.DataBufferByte awtData = (DataBufferByte) awtRaster.getDataBuffer();
		byte[] rawData = awtData.getData();

		//3) raw Data -> swt.ImageData
		org.eclipse.swt.graphics.PaletteData swtPalette = new PaletteData(0xff, 0xff00, 0xff0000);

		int depth = 0x18;
		org.eclipse.swt.graphics.ImageData swtImageData = new ImageData(
			bufferedImage.getWidth(),
			bufferedImage.getHeight(),
			depth,
			swtPalette,
			bufferedImage.getWidth(),
			rawData
		);

		return swtImageData;
//		return new Image(Display.getDefault(), swtImageData);
	}


	private static ImageData convertToSWT(BufferedImage bufferedImage) {
		ColorModel bufferedImageColorModel = bufferedImage.getColorModel();

		if (bufferedImageColorModel instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImageColorModel;
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
					data.setPixel(x, y, pixel);
					if (colorModel.hasAlpha()) {
						data.setAlpha(x, y, (rgb >> 24) & 0xFF);
					}
				}
			}
			return data;
		}
		else if (bufferedImageColorModel instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel)bufferedImageColorModel;
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		else if (bufferedImageColorModel instanceof ComponentColorModel) {
		    ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();

		    //ASSUMES: 3 BYTE BGR IMAGE TYPE

		    PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
		    ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);

		    //This is valid because we are using a 3-byte Data model with no transparent pixels
		    data.transparentPixel = -1;

		    WritableRaster raster = bufferedImage.getRaster();
		    int[] pixelArray = colorModel.getComponentSize();
		    for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		            raster.getPixel(x, y, pixelArray);
		            int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
		            data.setPixel(x, y, pixel);
		        }
		    }
		    return data;
		}
		return null;
	}


	/**
	 * Scale an image by using {@link ImageUtil} and converting the resulting {@link BufferedImage} into
	 * {@link ImageData} directly without creating an image file (e.g. JPG).
	 * @param photoContent
	 * @param maxImageWidth
	 * @param maxImageHeight
	 * @return
	 * @throws Exception
	 */
	private Image scaleImageAWTSWT(byte[] photoContent, int maxImageWidth, int maxImageHeight) throws Exception {
		Image image = null;

		if (photoContent != null && photoContent.length > 0) {
			try {
				long scaleTime = System.currentTimeMillis();
				ImageUtil imageUtil = new ImageUtil(photoContent);
				imageUtil.scaleIntoBorder(maxImageWidth, maxImageHeight);
				log("scaleTime: " + (System.currentTimeMillis() - scaleTime));


				long createImageTime = System.currentTimeMillis();
				BufferedImage bufferedImage = imageUtil.getBufferedImage();
				ImageData imageData = convertToSWT(bufferedImage);
				image = new Image(Display.getDefault(), imageData);
				log("createImageTime: " + (System.currentTimeMillis() - createImageTime));
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		return image;
	}


	/**
	 * Scale an image by using {@link ImageData}.
	 * @param photoId
	 * @return
	 * @throws Exception
	 */
	private Image scaleImageSWT(byte[] photoContent, int maxImageWidth, int maxImageHeight) throws Exception {
		Image image = null;
		if (photoContent != null && photoContent.length > 0) {
			try {
				long readImageTime = System.currentTimeMillis();
				InputStream inputStream = new ByteArrayInputStream(photoContent);
				ImageData imageData = new ImageData(inputStream);
				log("readImageTime: " + (System.currentTimeMillis() - readImageTime));
				log("original imageData size: " + imageData.width + "x" + imageData.height);

				long scaleTime = System.currentTimeMillis();

				Rectangle imageRect = new Rectangle(imageData.width, imageData.height);
				Rectangle borderRect = new Rectangle(maxImageWidth, maxImageHeight);
				imageRect.scaleIntoBorder(borderRect);
				int scaledWidth = (int) imageRect.getWidth().getLength();
				int scaledHeight = (int) imageRect.getHeight().getLength();

				imageData = imageData.scaledTo(scaledWidth, scaledHeight);
				log("scaleTime: " + (System.currentTimeMillis() - scaleTime));
				log("scaled imageData size: " + imageData.width + "x" + imageData.height);

				long createImageTime = System.currentTimeMillis();
				image = new Image(Display.getDefault(), imageData);
				log("createImageTime: " + (System.currentTimeMillis() - createImageTime));

				log("scaled image size: " + image.getBounds().width + "x" + image.getBounds().height);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		return image;
	}


	private void log(String message) {
		if (DEBUG) {
			System.out.println(message);
		}
	}

}
