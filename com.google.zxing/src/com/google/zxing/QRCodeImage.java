package com.google.zxing;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * {@link QRCodeImage} is a container for the qr code image and its meta information
 * @author kim
 *
 */
public class QRCodeImage implements Serializable{

	
	private static final long serialVersionUID = 1L;

	private static final NumberFormat numberFormat;
	
	static {
		numberFormat = NumberFormat.getNumberInstance(Locale.US);
	    numberFormat.setMinimumFractionDigits(2);
	    numberFormat.setMaximumFractionDigits(2);
	}

	/**
	 * represents the qr code image as an array of bytes
	 */
	protected byte[] content = null;
	
	/**
	 * the name of the type 
	 */
	protected ImageType type = null;
	protected int width = 0;
	protected int height = 0;
	
	/**
	 * Resolution of the image in pixel per cm.
	 */
	private int pixelPerCM = 0;
	
	
	public QRCodeImage() {
		super();
	}
	
	/**
	 * constructor 
	 * @param content a byte array
	 * @param type name of the type
	 * @param width 
	 * @param height
	 * @param pixelPerCM
	 */
	public QRCodeImage(byte[] content, ImageType type, int width, int height, int pixelPerCM) {
		super();
		this.content = content;
		this.type = type;
		this.width = width;
		this.height = height;
		this.pixelPerCM = pixelPerCM;
	}


	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}


	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}


	/**
	 * @return the type
	 */
	public ImageType getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(ImageType type) {
		this.type = type;
	}


	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}


	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}


	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}


	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}


	/**
	 * Gets the resolution of the image in pixel per cm.
	 * 
	 * @return the pixelPerCM
	 */
	public int getPixelPerCM() {
		return pixelPerCM;
	}


	/**
	 * Sets the resolution of the image in pixel per cm.
	 * 
	 * @param pixelPerCM the pixelPerCM to set
	 */
	public void setPixelPerCM(int pixelPerCM) {
		this.pixelPerCM = pixelPerCM;
	}

	
	public double getWidthInCM() {
		return (double) width / (double) pixelPerCM;
	}
	
	
	public double getHeightInCM() {
		return (double) height / (double) pixelPerCM;
	}

	
	public String getWidthAsString() {
		return numberFormat.format(getWidthInCM()) + "cm";
	}

	
	public String getHeightAsString() {
		return numberFormat.format(getHeightInCM()) + "cm";
	}
	
}
