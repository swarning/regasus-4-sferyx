package de.regasus.common.photo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.image.ImageFormat;
import com.lambdalogic.util.image.ImageUtil;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.Photo;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


enum PhotoTableColumns {COUNTER, FILE_NAME, THUMBNAIL}

public class PhotoTable extends SimpleTable<Photo, PhotoTableColumns> {

	private Map<Photo, Image> photo2ImageMap = new HashMap<>();

	private ResourceManager resourceManager;

	private static final int ROW_HEIGHT = Photo.MAX_THUMBNAIL_HEIGHT;
	private static final int IMAGE_MARGIN = 2;


	public PhotoTable(Table table) {
		super(table, PhotoTableColumns.class, false /*sortable*/);

		resourceManager = new LocalResourceManager(JFaceResources.getResources(), table);

		table.addListener(SWT.MeasureItem, measureItemListener);
		table.addListener(SWT.PaintItem, paintItemListener);
	}


	private Listener measureItemListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			try {
				if (event.index == PhotoTableColumns.THUMBNAIL.ordinal()) {
					event.height = ROW_HEIGHT;
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				throw new RuntimeException(e);
			}
		}
	};


	private Listener paintItemListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			try {
				if (event.index == PhotoTableColumns.THUMBNAIL.ordinal()) {
					TableItem tableItem = (TableItem) event.item;
					Photo photo = (Photo) tableItem.getData();
					if (photo != null) {
						Image image = buildImage(photo);
						int x = event.x + IMAGE_MARGIN;

						int imageHeight = image.getBounds().height;

						int y = event.y + (ROW_HEIGHT - imageHeight) / 2;
						event.gc.drawImage(image, x, y);
					}
				}
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				throw new RuntimeException(e);
			}
		}
	};


	@Override
	public String getColumnText(
		Photo photo,
		PhotoTableColumns column
	) {
		String label = null;

		switch (column) {
			case COUNTER:
				Integer counter = (Integer) photo.get(PhotoTableColumns.COUNTER);
				if (counter != null) {
					label = String.valueOf(counter);
				}
				break;
			case FILE_NAME:
				label = photo.getExternalPath();
				label = FileHelper.getName(label);
				break;
			default:
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(Photo photo, PhotoTableColumns column) {
		return photo.getPosition();
	}


	/**
	 * Build a device dependent Image.
	 *
	 * See also: https://www.eclipse.org/articles/Article-SWT-images/graphics-resources.html
	 *
	 * @param photo
	 * @return
	 */
	private Image buildImage(Photo photo) {
		Image image = photo2ImageMap.get(photo);

		if (image == null) {
			try {
				byte[] imageBytes = photo.getThumbnail();


				// scale down the image if its height is greater than the table row
				ImageUtil imageUtil = new ImageUtil(imageBytes);

				if (imageUtil.getHeight() > ROW_HEIGHT) {
					// scale down
					int maxWidth = imageUtil.getWidth();
					int maxHeight = ROW_HEIGHT;
					ImageFormat targetFormat = ImageFormat.JPG;
					imageBytes = imageUtil.scaleIntoBorder(maxWidth, maxHeight).toImageBytes(targetFormat);
				}


				InputStream inputStream = new ByteArrayInputStream(imageBytes);
				ImageData imageData = new ImageData(inputStream);
				ImageDescriptor imageDescriptor = ImageDescriptor.createFromImageData(imageData);
				image = resourceManager.createImage(imageDescriptor);

				photo2ImageMap.put(photo, image);
			}
			catch (ErrorMessageException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return image;
	}

}
