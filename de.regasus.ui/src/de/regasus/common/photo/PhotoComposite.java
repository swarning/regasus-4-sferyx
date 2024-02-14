package de.regasus.common.photo;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.ArrayHelper;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.cache.HardCache;
import com.lambdalogic.util.cache.ICacheRemoveListener;
import com.lambdalogic.util.image.ImageFormat;
import com.lambdalogic.util.image.ImageUtil;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Photo;
import de.regasus.common.PhotoComparator;
import de.regasus.common.PhotoModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IUpDownListener;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.UpDownComposite;
import de.regasus.ui.Activator;

/**
 * Base class of a Composite that shows a table with the image file names and thumbnails of {@link Photo}s which allow
 * users to add or delete images. It does not only display the image, but also cache it. All caches will be released
 * after the PhotoComposite is closed.
 *
 * Extending classes have to initialize the PhotoComposite be setting the specific {@link PhotoModel} and the ID (refId)
 * of the owning entity which works as a foreign key.
 *
 * In addition the abstract method {@link #uploadPhotos(List, int)} has to be implemented which delegates the call to
 * the corresponding method of the specific {@link PhotoModel}.
 */
public abstract class PhotoComposite extends Composite {

	private static final boolean DEBUG = false;

	private static final String[] FILE_EXTENSIONS = { "*.png;*.jpg" };

	private final PhotoModel photoModel;

	private final Long refId;

	private boolean ignoreRefresh = false;

	protected ModifySupport modifySupport = new ModifySupport(this);
	protected boolean modified = false;

	private HardCache<Image> imageCache;

	/**
	 * Current maximum width that images may have.
	 * This value depends on the dimensions of {@link #imageArea} and is updated automatically when the
	 * size of {@link #imageArea} changes.
	 */
	private int maxImageWidth;

	/**
	 * Current maximum height that images may have.
	 * This value depends on the dimensions of {@link #imageArea} and is updated automatically when the
	 * size of {@link #imageArea} changes.
	 */
	private int maxImageHeight;

	private int[] currentTableSelectionIndices = null;

	// widgets
	private Button addButton;
	private Button removeButton;
	private UpDownComposite upDownComposite;
	private Button showButton;
	private Button downloadButton;
	private Button urlButton;

	private Button previousImageButton;
	private Button nextImageButton;


	/**
	 * Area that includes the image.
	 * The size of {@link #imageArea} depends only on the surrounding {@link Composite}, but not of the
	 * {@link #imageLabel}. Therefore it is used to determine the maximum dimensions for a new image to be displayed.
	 */
	private Composite imageArea;

	/**
	 * Widget that is used to show the photo. It's size is adapted to the dimensions of every new photo.
	 */
	private Label imageLabel;

	private Table table;
	private TableViewer tableViewer;


	/**
	 * @param parent
	 * @param style
	 * @param refId PK of the referencing entity, e.g. Portal or Hotel
	 * @param eventId
	 */
	public PhotoComposite(
		Composite parent,
		PhotoModel photoModel,
		Long refId
	) {
		super(parent, SWT.NONE);

		Objects.requireNonNull(photoModel);
		Objects.requireNonNull(refId);

		this.photoModel = photoModel;
		this.refId = refId;

		photoModel.addForeignKeyListener(modelListener, refId);

		addDisposeListener(disposeListener);

		imageCache = new HardCache<>(10);
		imageCache.addCacheRemoveListener(cacheRemoveListener);

		createWidgets();
	}


	private ICacheRemoveListener<Image> cacheRemoveListener = new ICacheRemoveListener<Image>() {
		@Override
		public void elementRemoved(Object key, Image element) {
			element.dispose();
		}
	};


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			photoModel.removeForeignKeyListener(modelListener, refId);
			imageCache.clear();
		}
	};


	private void createWidgets() {
		setLayout(new GridLayout(1, false));

		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite leftComposite = new Composite(sashForm, SWT.NONE);
		Composite rightComposite = new Composite(sashForm, SWT.NONE);

		sashForm.setWeights(new int[] { 1, 1 });

		createTableArea(leftComposite);
		createImageArea(rightComposite);

		registerListener();
	}


	private void createTableArea(Composite parent) {
		int COL_COUNT = 2;
		parent.setLayout( new GridLayout(COL_COUNT, false) );

		Composite tableComposite = new Composite(parent, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);

		table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// COUNTER column
		TableColumn counterTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(counterTableColumn, new ColumnWeightData(15));

		// FILE_NAME column
		TableColumn fileNameTableColumn = new TableColumn(table, SWT.NONE);
		fileNameTableColumn.setText(UtilI18N.File);
		layout.setColumnData(fileNameTableColumn, new ColumnWeightData(100));

		// THUMBNAIL column
		TableColumn thumbnailTableColumn = new TableColumn(table, SWT.NONE);
		thumbnailTableColumn.setText(UtilI18N.Image);
		layout.setColumnData(thumbnailTableColumn, new ColumnWeightData(100));

		PhotoTable photoTable = new PhotoTable(table);
		tableViewer = photoTable.getViewer();


		GridDataFactory imageButtonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER);
		GridDataFactory textButtonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);

		/* Move buttons on the right
		 */

		Composite imageButtonComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(imageButtonComposite);
		imageButtonComposite.setLayout( new GridLayout() );


		{
    		addButton = new Button(imageButtonComposite, SWT.PUSH);
    		imageButtonGridDataFactory.applyTo(addButton);
    		addButton.setImage( IconRegistry.getImage("icons/add.png") );
		}

		{
    		removeButton = new Button(imageButtonComposite, SWT.PUSH);
    		imageButtonGridDataFactory.applyTo(removeButton);
    		removeButton.setImage( IconRegistry.getImage("icons/delete.png") );
		}

		{
			upDownComposite = new UpDownComposite(imageButtonComposite, SWT.NONE);
		}
//		{
//			moveFirstButton = new Button(moveButtonComposite, SWT.NONE);
//			imageButtonGridDataFactory.applyTo(moveFirstButton);
//			moveFirstButton.setBounds(0, 0, 75, 25);
//			moveFirstButton.setText(com.lambdalogic.util.rcp.UtilI18N.MoveFirst);
//			moveFirstButton.addSelectionListener(moveFirstSelectionListener);
//		}
//		{
//			moveUpButton = new Button(moveButtonComposite, SWT.NONE);
//			imageButtonGridDataFactory.applyTo(moveUpButton);
//			moveUpButton.setBounds(0, 0, 75, 25);
//			moveUpButton.setText(com.lambdalogic.util.rcp.UtilI18N.MoveUp);
//			moveUpButton.addSelectionListener(moveUpSelectionListener);
//		}
//		{
//			moveDownButton = new Button(moveButtonComposite, SWT.NONE);
//			imageButtonGridDataFactory.applyTo(moveDownButton);
//			moveDownButton.setBounds(0, 0, 75, 25);
//			moveDownButton.setText(com.lambdalogic.util.rcp.UtilI18N.MoveDown);
//			moveDownButton.addSelectionListener(moveDownSelectionListener);
//		}
//		{
//			moveLastButton = new Button(moveButtonComposite, SWT.NONE);
//			imageButtonGridDataFactory.applyTo(moveLastButton);
//			moveLastButton.setBounds(0, 0, 75, 25);
//			moveLastButton.setText(com.lambdalogic.util.rcp.UtilI18N.MoveLast);
//			moveLastButton.addSelectionListener(moveLastSelectionListener);
//		}


		/* CRUD buttons at the bottom
		 */

		Composite crudButtonComposite = new Composite(parent, SWT.NONE);
		crudButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COL_COUNT, 1));
		crudButtonComposite.setLayout(new GridLayout(5, true));

		showButton = new Button(crudButtonComposite, SWT.PUSH);
		textButtonGridDataFactory.applyTo(showButton);
		showButton.setText(UtilI18N.Show.toString());

		downloadButton = new Button(crudButtonComposite, SWT.PUSH);
		textButtonGridDataFactory.applyTo(downloadButton);
		downloadButton.setText(UtilI18N.Download.toString());

		urlButton = new Button(crudButtonComposite, SWT.PUSH);
		textButtonGridDataFactory.applyTo(urlButton);
		urlButton.setText(UtilI18N.CopyURL.toString());
	}


	private void createImageArea(Composite parent) {
		parent.setLayout(new GridLayout());

		imageArea = new Composite(parent, SWT.BORDER);
		imageArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		imageArea.setLayout(new GridLayout(1, false));

		imageLabel = new Label(imageArea, SWT.NONE);
		imageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

		/* Buttons
		 */

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buttonComposite.setLayout(new GridLayout(2, true));

		previousImageButton = new Button(buttonComposite, SWT.PUSH);
		previousImageButton.setText("<<");
		previousImageButton.setAlignment(SWT.CENTER);

		nextImageButton = new Button(buttonComposite, SWT.PUSH);
		nextImageButton.setText(">>");
		nextImageButton.setAlignment(SWT.CENTER);
	}


	private ControlListener imageAreaControlListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e) {
			int imageAreaWidth = imageArea.getSize().x - imageLabel.getBorderWidth();
			int imageAreaHeight = imageArea.getSize().y - imageLabel.getBorderWidth();

			if (imageAreaHeight != maxImageWidth || imageAreaHeight != maxImageHeight) {
				log("imageAreaControlListener: " + maxImageWidth + "x" + maxImageHeight + " --> " + imageAreaWidth + "x" + imageAreaHeight);

				maxImageWidth = imageAreaWidth;
				maxImageHeight = imageAreaHeight;

				Image image = imageLabel.getImage();
//				if (image != null) {
//    				log("Resize imageLabel");
//    				int currentImageWidth = image.getBounds().width;
//    				int currentImageHeight = image.getBounds().height;
//
//    				Rectangle borderRectangle = new Rectangle(maxImageWidth, maxImageHeight);
//    				Rectangle imageRectangle = new Rectangle(currentImageWidth, currentImageHeight);
//    				imageRectangle.scaleIntoBorder(borderRectangle);
//
//    				int newImageWidth = (int) imageRectangle.getWidth().getLength();
//    				int newImageHeight = (int) imageRectangle.getHeight().getLength();
//
//    				imageLabel.setSize(newImageWidth, newImageHeight);
//				}
				if (image != null) {
					removePhoto();
					imageCache.clear();
				}
//				if (image != null) {
//    				int currentImageWidth = image.getBounds().width;
//    				int currentImageHeight = image.getBounds().height;
//    				log(currentImageWidth + "x" + currentImageHeight);
//					imageLabel.setImage(image);
//					imageLabel.setSize(image.getBounds().width, image.getBounds().height);
//				}

			}
			else {
				log("imageAreaControlListener: size did not change");
			}
		}

		@Override
		public void controlMoved(ControlEvent e) {
		}
	};


	/**
	 * Registers all listeners to all require components of this composite.
	 * One important is the order of listener registration of the same event
	 * matters since the widget will execute them in order.
	 * Therefore,
	 * <pre>
	 * table.addListener(SWT.MouseDoubleClick, new ListenerA());
	 * table.addListener(SWT.MouseDoubleClick, new ListenerB());
	 * </pre>
	 * are not the same as
	 * <pre>
	 * table.addListener(SWT.MouseDoubleClick, new ListenerB());
	 * table.addListener(SWT.MouseDoubleClick, new ListenerA());
	 * </pre>
	 */
	private void registerListener() {
		imageArea.addControlListener(imageAreaControlListener);

		table.addKeyListener(tableLeftKeyListener);
		table.addKeyListener(tableRightKeyListener);
		table.addSelectionListener(tableSelectionListener);
		tableViewer.addDoubleClickListener(doubleClickListener);

		addButton.addSelectionListener(addSelectionListener);
		removeButton.addSelectionListener(removeSelectionListener);
		upDownComposite.setUpDownListener(upDownListener);
		showButton.addSelectionListener(showSelectionListener);
		downloadButton.addSelectionListener(downloadSelectionListener);
		urlButton.addSelectionListener(urlSelectionListener);
		previousImageButton.addSelectionListener(previousImageSelectionListener);
		nextImageButton.addSelectionListener(nextImageSelectionListener);

		syncWidgetsToModel();
		handleButtonState();
	}


	private KeyListener tableLeftKeyListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.ARROW_LEFT) {
				moveTableSelection(-1);
				handleButtonState();
				showPhoto();
			}
		}
	};


	private KeyListener tableRightKeyListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.ARROW_RIGHT) {
				moveTableSelection(+1);
				handleButtonState();
				showPhoto();
			}
		}
	};


	private SelectionListener tableSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {

			int[] selectionIndices = table.getSelectionIndices();
			if ( ! EqualsHelper.isEqual(selectionIndices, currentTableSelectionIndices) ) {
				log("tableSelectionListener: " + ArrayHelper.toString(currentTableSelectionIndices)
					+ " --> " + ArrayHelper.toString(selectionIndices)
				);
				currentTableSelectionIndices = selectionIndices;
    			handleButtonState();
    			removePhoto();
			}
			else {
				log("tableSelectionListener: selection did not change");
			}
		}
	};


	private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			try {
				showPhoto();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	public List<Photo> getPhotoList() {
		List<Photo> photos = (List<Photo>) tableViewer.getInput();
		return photos;
	}


	private void initPositions() {
		List<Photo> photoList = getPhotoList();
		int pos = 1;
		for (Photo photo : photoList) {
			photo.setPosition(pos++);
		}
	}


	/**
	 * Add one or multiple image file to model regardless of the UI.
	 * All files will be selected by user through dialog and only specific extensions will be accepted.
	 */
	private SelectionListener addSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent event) {
			List<File> selectedFiles = openDialogToSelectFiles();
			if ( selectedFiles.isEmpty() ) {
				return;
			}

			try {
				int maxPosition = 0;

				// determine the highest position of currently selected Photos, or all Photos if none is selected
				List<Photo> selectedPhotos = SelectionHelper.toList( tableViewer.getSelection() );
				if ( empty(selectedPhotos) ) {
					// treat all Photos as selected
					selectedPhotos = getPhotoList();
				}

				if ( notEmpty(selectedPhotos) ) {
    				for (Photo photo : selectedPhotos) {
    					if (photo.getPosition() > maxPosition) {
    						maxPosition = photo.getPosition();
    					}
    				}
				}

				int targetPosition = maxPosition + 1;

				uploadPhotos(selectedFiles, targetPosition);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}


		private List<File> openDialogToSelectFiles() {
			List<File> files = new ArrayList<>();

			FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
			dialog.setFilterExtensions(FILE_EXTENSIONS);
			String result = dialog.open();
			if (result != null) {
    			String filterPath = dialog.getFilterPath();
    			for (String fileName : dialog.getFileNames()) {
    				File file = new File(fileName);
    				if (!file.exists()) {
    					int totalLength = filterPath.length() + fileName.length() + 1;
    					StringBuilder absolutePath = new StringBuilder(totalLength)
    						.append(filterPath)
    						.append("/")
    						.append(fileName);
    					file = new File(absolutePath.toString());
    				}
    				files.add(file);
    			}
			}

			return files;
		}

	};


	protected abstract void uploadPhotos(List<File> selectedFiles, int targetPosition) throws Exception;


	/**
	 * To delete the data store from model using a selected items on the table.
	 * This listener is not update the table since there is no guarantee that all selected items will be deleted properly.
	 * Finally, User should provide that the {@link OnRefreshListener} will be executed after this listener to update UI.
	 */
	private SelectionListener removeSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent event) {
			List<Photo> selectedPhotos = SelectionHelper.toList( tableViewer.getSelection() );
			if (selectedPhotos.isEmpty()) {
				return;
			}

			MessageDialog confirmDialog = new MessageDialog(
				getShell(),
				null,
				null,
				UtilI18N.DeleteFileConfirmationDialogMessage.toString(),
				MessageDialog.CONFIRM,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, },
				IDialogConstants.CANCEL_ID
			);

			if (confirmDialog.open() != IDialogConstants.OK_ID) {
				return;
			}

			try {
				delete(selectedPhotos);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			handleButtonState();
		}


		private void delete(List<Photo> photos) throws Exception {
			synchronized (photoModel) {
				photoModel.deletePhotos(photos);
			}
		}
	};


	private IUpDownListener upDownListener = new IUpDownListener() {
		@Override
		public void topPressed() {
			moveToFirst();
		}

		@Override
		public void upPressed() {
			moveUp();
		}

		@Override
		public void downPressed() {
			moveDown();
		}

		@Override
		public void bottomPressed() {
			moveToLast();
		}
	};


	private void moveToFirst() {
		try {
			List<Photo> selectedPhotos = SelectionHelper.toList( tableViewer.getSelection() );
			if (!selectedPhotos.isEmpty()) {
				List<Photo> photos = getPhotoList();
				if (photos != null) {
					CollectionsHelper.moveFirst(photos, selectedPhotos);
					initPositions();

					tableViewer.refresh(true);

					// signal that the Editor contains unsaved data
					fireModify();
					handleButtonState();
				}
			}
		}
		catch (RuntimeException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void moveUp() {
		List<Photo> selectedPhotos = SelectionHelper.toList( tableViewer.getSelection() );
		if (!selectedPhotos.isEmpty()) {
			List<Photo> photos = getPhotoList();
			if (photos != null) {
				CollectionsHelper.moveUp(photos, selectedPhotos);
				initPositions();

				tableViewer.refresh(true);

				// signal that the Editor contains unsaved data
				fireModify();
				handleButtonState();
			}
		}
	}


	private void moveDown() {
		List<Photo> selectedPhotos = SelectionHelper.toList( tableViewer.getSelection() );
		if (!selectedPhotos.isEmpty()) {
			List<Photo> photos = getPhotoList();
			if (photos != null) {
				CollectionsHelper.moveDown(photos, selectedPhotos);
				initPositions();

				tableViewer.refresh(true);

				// signal that the Editor contains unsaved data
				fireModify();
				handleButtonState();
			}
		}
	}


	private void moveToLast() {
		List<Photo> selectedPhotos = SelectionHelper.toList( tableViewer.getSelection() );
		if (!selectedPhotos.isEmpty()) {
			List<Photo> photos = getPhotoList();
			if (photos != null) {
				CollectionsHelper.moveLast(photos, selectedPhotos);
				initPositions();

				tableViewer.refresh(true);

				// signal that the Editor contains unsaved data
				fireModify();
				handleButtonState();
			}
		}
	}


	private SelectionListener showSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			showPhoto();
		}
	};


	private SelectionListener downloadSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {

			try {
				List<Photo> photos = SelectionHelper.toList( tableViewer.getSelection() );

				PhotoDownloader photoDownloader = new PhotoDownloader(getShell(), photoModel);
				photoDownloader.download(photos);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}
	};


	private SelectionListener urlSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				List<Photo> photos = SelectionHelper.toList( tableViewer.getSelection() );

				if ( notEmpty(photos) && photos.size() == 1) {
					Photo photo = photos.get(0);
					URL url = PhotoModel.buildWebServiceUrl(photo);
					ClipboardHelper.copyToClipboard( url.toString() );
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private SelectionListener previousImageSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			moveTableSelection(-1);
			handleButtonState();
			showPhoto();
		}
	};


	private SelectionListener nextImageSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			moveTableSelection(+1);
			handleButtonState();
			showPhoto();
		}
	};


	private void syncWidgetsToModel() {
		final List<Photo> photos;
		try {
			synchronized (photoModel) {
				List<Photo> _photoList = photoModel.getPhotosByRefId(refId);

				// photos must not be unmodifiable and sorted
				photos = new ArrayList<>(_photoList);
				Collections.sort(photos, PhotoComparator.getInstance());

				// add counter value to every photo
				int index = 1;
				for (Photo photo : photos) {
					photo.put(PhotoTableColumns.COUNTER, index);
		    		index++;
			    }
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return;
		}

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					tableViewer.setInput(photos);
					modified = false;
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	/**
	 * Update the button status correspond to the current table stage
	 */
	private void handleButtonState() {
		log("handleButtonState()");

		int selectionCount = table.getSelectionCount();
		int selectingIndex = table.getSelectionIndex();
		int itemCount = table.getItemCount();

		removeButton.setEnabled(selectionCount > 0);
		showButton.setEnabled(selectionCount == 1);
		downloadButton.setEnabled(selectionCount > 0);
		urlButton.setEnabled(selectionCount == 1);

		previousImageButton.setEnabled(selectionCount == 1 && selectingIndex > 0);
		nextImageButton.setEnabled(
			   selectionCount == 1
			&& selectingIndex >= 0
			&& (selectingIndex + 1) < itemCount
		);


		boolean moveTopEnabled = false;
		boolean moveUpEnabled = false;
		boolean moveDownEnabled = false;
		boolean moveBottomEnabled = false;
		if (selectionCount > 0) {
			int[] selectionIndices = table.getSelectionIndices();
			int firstSelectionIndex = selectionIndices[0];
			int lastSelectionIndex = selectionIndices[selectionIndices.length - 1];

			// When the last of the n selected is after the nth position of the chosen,
			// there must be a gap above, and we can move first
			moveTopEnabled = lastSelectionIndex >= selectionCount;

			// When first selected is not the one in the first row we can move up
			moveUpEnabled = firstSelectionIndex > 0;

			// When last selected is not the one in last row we can move down
			moveDownEnabled = lastSelectionIndex < itemCount - 1;

			// When the first of the n selected is not at the size-nth position,
			// there must be a gap below, and we can move down
			moveBottomEnabled = firstSelectionIndex < itemCount - selectionCount;
		}
		upDownComposite.setTopEnabled(moveTopEnabled);
		upDownComposite.setUpEnabled(moveUpEnabled);
		upDownComposite.setDownEnabled(moveDownEnabled);
		upDownComposite.setBottomEnabled(moveBottomEnabled);
	}


	private CacheModelListener<Long> modelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (ignoreRefresh) {
				return;
			}

			syncWidgetsToModel();
		}
	};


	/**
	 * Update the content on image panel UI correspond to the selecting item on the table.
	 * Update only if 1 item is selected.
	 * Multiple item selection will clear the image panel instead.
	 */
	private void showPhoto() {
		log("showPhoto()");

		try {
			List<Photo> photos = SelectionHelper.toList( tableViewer.getSelection() );

			Image image = null;
			if (photos.size() == 1) {
				Long photoId = photos.get(0).getId();
				image = loadImage(photoId);
			}
			imageLabel.setImage(image);
			imageLabel.setSize(image.getBounds().width, image.getBounds().height);

			preloadImages();
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	/**
	 * Clear the image panel UI to nothing.
	 */
	private void removePhoto() {
		log("removePhoto()");

		imageLabel.setImage(null);
	}


	private Object buildCacheKey(Long photoId, int width, int height) {
		StringBuilder sb = new StringBuilder(64);

		sb.append(photoId);
		sb.append('-');
		sb.append(width);
		sb.append('-');
		sb.append(height);

		return sb.toString();
	}


	private Image loadImage(Long photoId) throws Exception {
		if (DEBUG) log("Load Image of photoId " + photoId);

		Object cacheKey = buildCacheKey(photoId, maxImageWidth, maxImageHeight);

		Image image = imageCache.get(cacheKey);

		if (image == null) {
			if (DEBUG) log("Image of photoId " + photoId + " not found in cache");
			byte[] photoContent;
			synchronized (photoModel) {
				if (DEBUG) log("Get content of photoId " + photoId + " from model");
				photoContent = photoModel.getPhotoContent(photoId);
			}

			if (photoContent != null && photoContent.length > 0) {
				try {
					if (DEBUG) log("Scale image of photoId " + photoId);
					ImageFormat imageFormat = ImageFormat.JPG;
					log("imageFormat: " + imageFormat);

					long scaleTime = System.currentTimeMillis();
					ImageUtil imageUtil = new ImageUtil(photoContent);
					byte[] scaledImageBytes = imageUtil.scaleIntoBorder(maxImageWidth, maxImageHeight).toImageBytes(imageFormat);
					log("scaleTime: " + (System.currentTimeMillis() - scaleTime));

					if (DEBUG) log("Prepare image of photoId " + photoId);
					long createImageTime = System.currentTimeMillis();
					InputStream inputStream = new ByteArrayInputStream(scaledImageBytes);
					image = new Image(getDisplay(), inputStream);
					log("createImageTime: " + (System.currentTimeMillis() - createImageTime));

					if (DEBUG) log("Put image of photoId " + photoId + " into cache\n");
					imageCache.put(cacheKey, image);
				}
				catch (Throwable e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		}
		else {
			if (DEBUG) log("Image of photoId " + photoId + " found in cache\n");
		}

		return image;
	}


	/**
	 * Indexes of the Photos whose images are loaded when one image is shown.
	 */
	private static final int[] PRELOAD_INDEX_OFFSETS = {1};

	synchronized
	private void preloadImages() {
		log("preloadImages()");
		for (int indexOffset : PRELOAD_INDEX_OFFSETS) {
			preloadImage(indexOffset);
		}
	}


	synchronized
	private void preloadImage(int indexOffset) {
		if (DEBUG) log("preloadImage(" + indexOffset + ")");
		int itemCount = table.getItemCount();
		int selectedIndex =  table.getSelectionIndex();
		int preLoadIndex = selectedIndex + indexOffset;
		if (preLoadIndex >= 0 && preLoadIndex < itemCount - 1) {
			Photo photo = (Photo) tableViewer.getElementAt(preLoadIndex);

			if (DEBUG) log("Preload image of photoId " + photo.getId());
			Callable<byte[]> callable = new LoadPhotoContentCallable( photo.getId() );
			Executors.newSingleThreadExecutor().submit(callable);
		}
	}


	private final class LoadPhotoContentCallable implements Callable<byte[]> {
		private final Long photoId;

		private LoadPhotoContentCallable(Long photoId) {
			this.photoId = Objects.requireNonNull(photoId);
		}

		@Override
		public byte[] call() throws Exception {
			synchronized (photoModel) {
				// ignore the CacheModelEvent triggered by this operation to avoid a dead-lock
				ignoreRefresh = true;
				try {
					loadImage(photoId);
				}
				finally {
					ignoreRefresh = false;
				}
			}
			return null;
		}
	}


	/**
	 * Move the selecting item of a specific table at a specific step.
	 * A positive number will move forward, but negative will move backward.
	 * During the move, the new selection item will not exceed the range.
	 * For example, the selection item will end at 0 index when try to move 2 step backward at the 1st index.
	 * On the other hand, the last index will be selected instead of moving 2 step forward at the pre-last index.
	 */
	private void moveTableSelection(int step) {
		int selectionIndex = table.getSelectionIndex();
		int totalItem = table.getItemCount();
		int newIndex = selectionIndex + step;

		int applyIndex = Math.max(0, Math.min(newIndex, totalItem - 1));
		table.setSelection(applyIndex);
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


	private void fireModify() {
		modified = true;
		modifySupport.fire();
	}


	public boolean isModified() {
		return modified;
	}

	// *
	// * Modifying
	// **************************************************************************


	private void log(String message) {
		if (DEBUG) {
			System.out.println(message);
		}
	}

}
