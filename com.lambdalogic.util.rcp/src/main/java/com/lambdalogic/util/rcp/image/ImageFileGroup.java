package com.lambdalogic.util.rcp.image;

import static com.lambdalogic.util.StringHelper.*;

import java.io.ByteArrayInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.geom.DistanceUnit;
import com.lambdalogic.util.geom.Rectangle;
import com.lambdalogic.util.observer.DefaultEvent;
import com.lambdalogic.util.observer.Observer;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.File;


public class ImageFileGroup extends Group {
	// used in logging output for debug only
//	private static int INSTANCE_COUNTER = 0;
//	private int instanceCounter = INSTANCE_COUNTER++;

	private ImageFileGroupController controller;

	private ImageData imageData = null;
	private Rectangle originalImageSize = null;
	private Point imageBorderSize = new Point(0, 0);

	/** true if the user set the checkbox in the confirmation dialog for refresh to not ask again */
	private boolean refreshDontAsk;


	// **************************************************************************
	// * Widgets
	// *

	private Composite imageBorderComposite;
	private Label imageLabel;
	private Text extPathText;
	private Button copyPathButton;
	private Text urlText;
	private Button copyUrlButton;

	private Button uploadButton;
	private Button downloadButton;
	private Button refreshButton;
	private Button deleteButton;

	// *
	// * Widgets
	// **************************************************************************


	public ImageFileGroup(Composite parent, int style) {
		super(parent, style);

		createWidgets();

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (imageLabel != null) {
					Image image = imageLabel.getImage();
					if (image != null) {
						image.dispose();
					}
				}

				if (controller != null) {
					controller.dispose();
				}
			}
		});
	}


	public void setController(ImageFileGroupController controller) {
		this.controller = controller;

		controller.addObserver(new Observer<DefaultEvent>() {
			@Override
			public void update(Object source, DefaultEvent event) {
				syncWidgetsToEntity();
			}
		});
	}


	private void createWidgets() {
		int NUL_COLUMNS = 3;
		setLayout(new GridLayout(NUL_COLUMNS, false));


		/***** Buttons *****/

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, NUL_COLUMNS, 1));
		buttonComposite.setLayout(new GridLayout(4, true));

		GridDataFactory buttonGridDataFactory = GridDataFactory.fillDefaults();

		uploadButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(uploadButton);
		uploadButton.setText(UtilI18N.Upload);
		uploadButton.setToolTipText(UtilI18N.UploadFileButton_ToolTip);
		uploadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				upload();
			}
		});

		downloadButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(downloadButton);
		downloadButton.setText(UtilI18N.Download);
		downloadButton.setToolTipText(UtilI18N.DownloadFileButton_ToolTip);
		downloadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				download();
			}
		});

		refreshButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(refreshButton);
		refreshButton.setText(UtilI18N.Refresh);
		refreshButton.setToolTipText(UtilI18N.RefreshFileButton_ToolTip);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		});

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(deleteButton);
		deleteButton.setText(UtilI18N.Delete);
		deleteButton.setToolTipText(UtilI18N.DeleteFileButton_ToolTip);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				delete();
			}
		});


		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

		/***** file path *****/

		Label pathLabel = new Label(this, SWT.RIGHT);
		GridDataFactory.defaultsFor(pathLabel).indent(15, 0).applyTo(pathLabel);
		pathLabel.setText(UtilI18N.Path);


		extPathText = new Text(this, SWT.BORDER);
		extPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		SWTHelper.disableTextWidget(extPathText);

		copyPathButton = new Button(this, SWT.PUSH);
		buttonGridDataFactory.applyTo(copyPathButton);
		copyPathButton.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_COPY));
		copyPathButton.setToolTipText(UtilI18N.CopyToClipboard);
		copyPathButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ClipboardHelper.copyToClipboard( extPathText.getText() );
			}
		});



		/***** url *****/

		Label urlLabel = new Label(this, SWT.RIGHT);
		GridDataFactory.defaultsFor(urlLabel).indent(15, 0).applyTo(urlLabel);
		urlLabel.setText("URL");


		urlText = new Text(this, SWT.BORDER);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		SWTHelper.disableTextWidget(urlText);

		copyUrlButton = new Button(this, SWT.PUSH);
		buttonGridDataFactory.applyTo(copyUrlButton);
		copyUrlButton.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_COPY));
		copyUrlButton.setToolTipText(UtilI18N.CopyToClipboard);
		copyUrlButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ClipboardHelper.copyToClipboard( urlText.getText() );
			}
		});


		/***** image widget *****/

		imageBorderComposite = new Composite(this, SWT.BORDER);
		imageBorderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, NUL_COLUMNS, 1));
		imageBorderComposite.setLayout(new GridLayout(1, false));

		imageLabel = new Label(imageBorderComposite, SWT.LEFT);
		GridDataFactory.swtDefaults()
			.align(SWT.LEFT, SWT.FILL)
			.grab(true, true)
//			.hint(SWT.DEFAULT, 32)
			.applyTo(imageLabel);

		imageLabel.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point newImageSize = imageBorderComposite.getSize();
				if (imageBorderSize == null || ! imageBorderSize.equals(newImageSize)) {
					//System.out.println(instanceCounter + ": Image size changed from " + imageSize + " to " + newImageSize);

					imageBorderSize = newImageSize;
					drawImage();
				}
    			else {
    				//System.out.println(instanceCounter + ": Image size did not change");
    			}
			}
		});

		initButtonStatus();

		// after sync add this as ModifyListener to all widgets and groups
		extPathText.addModifyListener(buttonModifyListener);

		addPaintListener(paintListener);
	}


	public void setPreferredImageSize(int width, int height) {
		Object layoutData = imageLabel.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				GridData gridData = (GridData) layoutData;
				gridData.widthHint = width;
				gridData.heightHint = height;
			}
		}
	}


	private ModifyListener buttonModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			refreshButtonStatus();
		}
	};


	/**
	 * The initial state for Buttons corresponds to a new PageLayout which is not persisted yet.
	 * Therefore all Buttons are disabled.
	 */
	private void initButtonStatus() {
		uploadButton.setEnabled(false);
		downloadButton.setEnabled(false);
		refreshButton.setEnabled(false);
		deleteButton.setEnabled(false);

		copyPathButton.setEnabled(false);
		copyUrlButton.setEnabled(false);
	}


	private void refreshButtonStatus() {
		//System.out.println(instanceCounter + ": refreshButtonStatus()");
		boolean enabled = getEnabled();
		boolean extPathExists = isNotEmpty( extPathText.getText() );
		boolean urlExists = isNotEmpty( urlText.getText() );
		boolean imageExists = imageData != null;

		uploadButton.setEnabled(enabled);
		downloadButton.setEnabled(enabled && imageExists);
		refreshButton.setEnabled(enabled && extPathExists);
		deleteButton.setEnabled(enabled && imageExists);

		copyPathButton.setEnabled(extPathExists);
		copyUrlButton.setEnabled(urlExists);
	}


	private byte[] getImageContent() throws Exception {
		byte[] imageContent = null;
		File file = controller.read();
		if (file != null) {
			imageContent = file.getContent();
		}
		return imageContent;
	}


	private void setImageContent(byte[] imageContent) throws ErrorMessageException {
		//System.out.println(instanceCounter + ": setImageContent()");

		imageData = null;
		originalImageSize = null;

		if (imageContent != null) {
			try {
				ByteArrayInputStream is = new ByteArrayInputStream(imageContent);
				imageData = new ImageData(is);

				// determine original width and height of the image
				Image image = new Image(Display.getCurrent(), imageData);
				org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
				image.dispose();
				originalImageSize = new Rectangle(bounds.width, DistanceUnit.mm, bounds.height, DistanceUnit.mm);
				
			}
			catch (Exception e) {
				throw new ErrorMessageException(UtilI18N.ColorSpaceNotSupported);
			}
		}

		drawImage();
	}


	private void drawImage() {
		//System.out.println(instanceCounter + ": drawImage()");

		// dispose old Image
		Image oldImage = imageLabel.getImage();
		if (oldImage != null) {
			oldImage.dispose();
		}

		Image newImage = null;
		if (imageData != null && imageBorderSize.x > 0 && imageBorderSize.y > 0) {
			newImage = getScaledImage();
		}
		imageLabel.setImage(newImage);

		imageBorderComposite.layout();
	}


	private Image getScaledImage() {
		//System.out.println(instanceCounter + ": getScaledImage() !!!!!");
		Rectangle newBorderRectangle = new Rectangle(imageBorderSize.x, DistanceUnit.mm, imageBorderSize.y, DistanceUnit.mm);

		// make image smaller to fit into the current size of imageLabel, but not bigger
		Rectangle scaledRectangle;
		if (   newBorderRectangle.getWidth().getLength()  < originalImageSize.getWidth().getLength()
			|| newBorderRectangle.getHeight().getLength() < originalImageSize.getHeight().getLength()
		) {
			scaledRectangle = Rectangle.scaleImageIntoBorder(originalImageSize, newBorderRectangle);
		}
		else {
			scaledRectangle = originalImageSize;
		}

		int newWidth = (int) Math.round( scaledRectangle.getWidth().getLength() );
		int newHeight = (int) Math.round( scaledRectangle.getHeight().getLength() );
		ImageData scaledImageData = imageData.scaledTo(newWidth, newHeight);

		return new Image(Display.getCurrent(), scaledImageData);
	}


	private boolean dirtyWidgets = false;
	private boolean active = false;

	public void syncWidgetsToEntity() {
		//System.out.println(instanceCounter + ": syncWidgetsToEntity()");

		dirtyWidgets = true;
		syncWidgetsToEntityWhenActive();
	}


	private PaintListener paintListener = new PaintListener() {
		@Override
		public void paintControl(PaintEvent e) {
			//System.out.println(instanceCounter + ": painted()");
			active = true;
			syncWidgetsToEntityWhenActive();
		}
	};


	private void syncWidgetsToEntityWhenActive() {
		//System.out.println(instanceCounter + ": syncWidgetsToEntityWhenActive(" + active + ")");
		if (dirtyWidgets && active) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						byte[] content = null;
						String extPath = "";
						String url = "";

						//System.out.println(instanceCounter + ": syncWidgetsToEntityWhenActive(" + active + ") --> read image from controller");
						File file = controller.read();
						if (file != null) {
							content = file.getContent();
							extPath = avoidNull( file.getExternalPath() );
							url = avoidNull( controller.getWebServiceUrl() );
						}

						setImageContent(content);
						extPathText.setText(extPath);
						urlText.setText(url);

						refreshButtonStatus();

						dirtyWidgets = false;
					}
					catch (Exception e) {
						ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
		else {
			//System.out.println(instanceCounter + ": syncWidgetsToEntityWhenActive(" + active + ") --> do nothing");
		}
	}


	private void upload() {
		try {
			FileDialog dialog = new FileDialog( Display.getCurrent().getActiveShell(), SWT.OPEN);

			// avoid space characters! (MIRCP-2897 - Use portable file extension filter for multiple extensions)
			String[] extensions = {"*.png;*.jpg;*.jpeg;*.gif;*.ico", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.ico"};
			dialog.setFilterExtensions(extensions);

			// init Dialog with filename
			if (isNotEmpty(extPathText.getText())) {
				java.io.File file = new java.io.File(extPathText.getText());
				dialog.setFilterPath(file.getParent());
				dialog.setFileName(file.getName());
			}

			// open Dialog
			String fileName = dialog.open();

			if (fileName != null) {
				try {
					// read File
					java.io.File file = new java.io.File(fileName);
					byte[] content = FileHelper.readFile(file);
					
					// set image content before persist to ensure, that the image is valid.
					setImageContent(content);

					// persist File in DB
					controller.persist(content, fileName);

					extPathText.setText(fileName);
				}
				catch (Exception e) {
					ErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void download() {
		// Open Save-as-Dialog
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);

		String fileName = extPathText.getText();
		if ( imageLabel != null && isNotEmpty(fileName) ) {
			java.io.File file = new java.io.File(fileName);
			java.io.File dir = file.getParentFile();
			if (dir != null && dir.exists()) {
				fileDialog.setFilterPath(dir.getPath());
			}
			fileDialog.setFileName(file.getName());

			String saveFileName = fileDialog.open();
			if (saveFileName != null) {
				try {
					byte[] imageContent = getImageContent();
					FileHelper.writeFile(new java.io.File(saveFileName), imageContent);
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
	}


	private void refresh() {
		String fileName = extPathText.getText();
		java.io.File file = new java.io.File(fileName);
		if (file.exists()) {
			boolean refreshOK;

			if (refreshDontAsk) {
				refreshOK = true;
			}
			else {
				MessageDialogWithToggle dialogWithToggle = MessageDialogWithToggle.openOkCancelConfirm(
					Display.getDefault().getActiveShell(),
					UtilI18N.Hint,
					UtilI18N.RefreshFileConfirmationDialogMessage,
					UtilI18N.DontShowThisConfirmDialogAgain,
					false,
					null,
					null
				);

				refreshOK = (Window.OK == dialogWithToggle.getReturnCode());
				refreshDontAsk = dialogWithToggle.getToggleState();
			}

			if (refreshOK) {
				try {
					// read File
					byte[] content = FileHelper.readFile(file);

					setImageContent(content);
					
					// persist File in DB
					controller.persist(content, fileName);
				}
				catch (Exception e) {
					ErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
	}


	private void delete() {
		try {
			boolean deleteOK = MessageDialog.openQuestion(
				Display.getDefault().getActiveShell(),
				UtilI18N.Hint,
				UtilI18N.DeleteFileConfirmationDialogMessage
			);
			// If the user selected "Yes"
			if (deleteOK) {
				controller.delete();

				setImageContent(null);
				extPathText.setText("");
			}
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
