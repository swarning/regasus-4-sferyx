package com.idautomation.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.idautomation.pdf417.PDF417;
import com.idautomation.pdf417.encoder.GifEncoder;

public class BarcodeEncoder {

	public static ImageContainer encodeGIF(PDF417 bc) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encodeGIF(bc, outputStream);
		byte[] imageBytes = outputStream.toByteArray();
		
		ImageContainer imageContainer = new ImageContainer(
			imageBytes,
			ImageType.GIF,
			bc.getSize().width,
			bc.getSize().height,
			bc.resolution
		);
		
		return imageContainer;
	}

	
	public static void saveToGIF(PDF417 bc, String fileName) throws FileNotFoundException {
		File file = new File(fileName);
        file.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        encodeGIF(bc, fileOutputStream);
	}


	public static ImageContainer encodeJPEG(PDF417 bc) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encodeJPEG(bc, outputStream);
		byte[] imageBytes = outputStream.toByteArray();

		ImageContainer imageContainer = new ImageContainer(
			imageBytes,
			ImageType.GIF,
			bc.getSize().width,
			bc.getSize().height,
			bc.resolution
		);
		
		return imageContainer;
	}

	
	public static void saveToJPEG(PDF417 bc, String fileName) throws FileNotFoundException {
		File file = new File(fileName);
        file.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        encodeJPEG(bc, fileOutputStream);
	}


	private static void encodeGIF(PDF417 bc, OutputStream outputStream) {
		String v = java.lang.System.getProperty("java.version");
		if (v.indexOf("1.1") == 0) {
			throw new RuntimeException("Java Version 1.1 is not supported.");
		}

		try {
			// *** This method was added to auto size the images the first time ***
			if (bc.autoSize) {
				BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_INDEXED);
				Graphics graphics = image.createGraphics();
				bc.paint(graphics);
				bc.invalidate();
				graphics.dispose();
			}

			// create bufferred image
			BufferedImage image = new BufferedImage(
				bc.getSize().width,
				bc.getSize().height,
				BufferedImage.TYPE_BYTE_INDEXED
			);
			// java.awt.Image image=c.createImage(c.getSize().width,c.getSize().height);
			Graphics imgGraphics = image.createGraphics();

			bc.paint(imgGraphics);

			// encode buffered image to a gif
			GifEncoder encoder = new GifEncoder(image, outputStream);
			encoder.encode();
			outputStream.close();
			
//			System.out.println("bc.getSize().height: " + bc.getSize().height);
//			System.out.println("bc.getSize().width: " + bc.getSize().width);
//			System.out.println("bc.resolution: " + bc.resolution);
//			System.out.println("Höhe: " + (double) bc.getSize().height / bc.resolution + " cm");
//			System.out.println("Breite: " + (double) bc.getSize().width / bc.resolution + " cm");
			
//			System.out.println("image.getHeight(): " + image.getHeight());
//			System.out.println("image.getWidth(): " + image.getWidth());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private static void encodeJPEG(PDF417 bc, OutputStream outputStream) {
		String v = java.lang.System.getProperty("java.version");
		if (v.indexOf("1.1") == 0) {
			throw new RuntimeException("Java Version 1.1 is not supported.");
		}

		try {
			// *** This method was added to auto size the images the first time ***
			if (bc.autoSize) {
				bc.setSize(170, 90);
				BufferedImage imageTemp = new BufferedImage(
					bc.getSize().width,
					bc.getSize().height,
					BufferedImage.TYPE_BYTE_INDEXED
				);
				Graphics imgTempGraphics = imageTemp.createGraphics();
				bc.paint(imgTempGraphics);
				bc.invalidate();
				imgTempGraphics.dispose();
			}

			// create bufferred image
			BufferedImage image = new BufferedImage(
				bc.getSize().width,
				bc.getSize().height,
				BufferedImage.TYPE_INT_RGB
			);
			Graphics imgGraphics = image.createGraphics();

			bc.paint(imgGraphics);

			
			javax.imageio.ImageIO.write(image, "jpg", outputStream);
			
//			// encode buffered image to a jpeg
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outputStream);
//
//			// increase the JPEG quality to 100%
//			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
//			param.setQuality(1.0F, true);
//			encoder.setJPEGEncodeParam(param);
//			encoder.encode(image, param);

			outputStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
