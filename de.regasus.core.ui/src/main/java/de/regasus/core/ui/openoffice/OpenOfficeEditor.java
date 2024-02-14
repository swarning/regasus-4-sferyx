package de.regasus.core.ui.openoffice;

import static de.regasus.LookupService.getDataStoreMgr;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.Bundle;

import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceTemplateType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.sun.star.view.DocumentZoomType;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.event.DocumentAdapter;
import ag.ion.bion.officelayer.event.IDocumentEvent;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.frame.IDispatchDelegate;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;

/**
 * An editor which shows an embedded OpenOffice.org window and loads in it a document that is taken from the DataStoreVO
 * whose ID is taken from the {@link OpenOfficeEditorInput}.
 *
 * @author manfred
 *
 */
public class OpenOfficeEditor extends AbstractEditor<OpenOfficeEditorInput> implements IRefreshableEditorPart {

	public static final String ID = "OpenOfficeEditor";

	private static boolean nativeLibsFound = false;

	private IOfficeApplication officeApplication;

	private IDocument officeDocument;

	private IFrame officeFrame;

	private Frame frame;

	private String openOfficePath;

	private Composite embedded;

	private DataStoreVO dataStoreVO;

	private Panel awtPanel;


	// ******************************************************************************************
	// * Overriden EditorPart methods

	/**
	 * Sets name, tooltip and icon for editor and makes sure that the installation path to OpenOffice.org exists and the
	 * native libraries can be found
	 */
	@Override
	public void init() throws Exception {
		// makes sure that installation path exists and the native libraries can be found
		try {
			initNativeLibPath();
			if (!nativeLibsFound) {
				throw new PartInitException(CoreI18N.OpenOfficePreference_NativeLibsNotFound);
			}
			openOfficePath = FileHelper.getOpenOfficePath();

			if (openOfficePath != null && !new File(openOfficePath).exists()) {
				// try to use the openOfficePath as relative path and the absolute path is the path
				// to user.dir folder + the openOfficePath
				openOfficePath = System.getProperty("user.dir") + File.separator + openOfficePath;
			}

			if (!new File(openOfficePath).exists()) {
				System.err.println("OpenOfficeEditor couldn't find openOfficePath: " + openOfficePath + " does not exist");
				throw new PartInitException(CoreI18N.OpenOfficePreference_NoProperPathConfiguration);
			}
			else {
				System.out.println("OpenOfficeEditor using existing openOfficePath " + openOfficePath);
			}

			// disable the editorTopComposite from AbstractEditor
			enableEditorTopComposite = false;
		}
		catch (IOException e) {
			throw new PartInitException(CoreI18N.OpenOfficePreference_NativeLibsNotFound, e);
		}
	}


	@Override
	protected void createWidgets(Composite parent) {
		this.parent = parent;

		// parent.setLayout(new FillLayout());

		embedded = new Composite(parent, SWT.EMBEDDED);
		embedded.setLayout(new FillLayout());

		// Prepare the embedded AWT Frame (Panel is needed for propagation of events)
		frame = SWT_AWT.new_Frame(embedded);
		frame.setSize(400, 400);
		frame.validate();
		awtPanel = new Panel(new BorderLayout());
		frame.add(awtPanel);
		frame.setVisible(true);

		buildOfficeFrameAndLoadDocument();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			monitor.beginTask(CoreI18N.OpenOffice_Saving, 2);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			officeDocument.getPersistenceService().store(baos);
			baos.close();
			monitor.worked(1);

			dataStoreVO.setContent(baos.toByteArray());
			dataStoreVO.setCompressed(false);

			getDataStoreMgr().update(dataStoreVO);
			monitor.worked(1);

			// Signalisieren, dass Editor keinen ungespeicherten Daten mehr enthält
			setDirty(false);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			monitor.done();
		}
	}


	/**
	 * Saving the document in a different file is supported from within the OpenOffice frame.
	 */
	@Override
	public void doSaveAs() {

	}


	/**
	 * When the editor is closed, close the document so that it needn't recovered in case OOo gets forcefully terminated
	 * afterwards.
	 */
	@Override
	public void dispose() {
		closeOfficeDocumentAndFrame();
		super.dispose();
	}


	@Override
	protected String getName() {
		String docType = editorInput.getDocType();

		if (docType != null) {
			if (InvoiceTemplateType.isValue(docType)) {
				InvoiceTemplateType type = InvoiceTemplateType.valueOf(docType);
				return type.getString();
			}
			else {
				if (DataStoreVO.DOC_TYPE_BADGE.equals(docType)) {
					String fileName = editorInput.getExtFileName();
					if (fileName != null) {
						return new File(fileName).getName();
					}
					else {
						return CoreI18N.BadgeTemplate;
					}
				}
				else if (DataStoreVO.DOC_TYPE_NOTE.equals(docType)) {
					String fileName = editorInput.getExtFileName();
					if (fileName != null) {
						return new File(fileName).getName();
					}
					else {
						return CoreI18N.NotificationTemplate;
					}

				}
				return docType;
			}
		}
		else {
			String fileName = editorInput.getExtFileName();
			if (fileName != null) {
				return new File(fileName).getName();
			}
			else {
				return UtilI18N.Template;
			}
		}
	}


	@Override
	protected String getToolTipText() {
		return editorInput.getToolTipText();
	}


	/**
	 * This editor can (up to now) never be opened for a new, unsaved document, in which case a refresh isn't allowed
	 * because it made no sense
	 */
	@Override
	public boolean isNew() {
		return false;
	}


	/**
	 * Refreshing this editor didn't work with just opening a new document in the frame, so we are forced to close and
	 * recreate the complete OpenOffice frame.
	 */
	@Override
	public void refresh() throws Exception {
		syncExecInParentDisplay(new Runnable() {
			@Override
			public void run() {
				closeOfficeDocumentAndFrame();
				buildOfficeFrameAndLoadDocument();
			}
		});

	}


	// ******************************************************************************************
	// * Private helper methods

	private void closeOfficeDocumentAndFrame() {
		// may happen when initialization of frame fails
		if (officeDocument != null) {
			officeDocument.close();
		}
		awtPanel.removeAll();
	}


	/**
	 * The display of OpenOffice.org in our GUI requires that it can find some of its native dlls. They are contained in
	 * the lib-directory of the plugin "ag.ion.noa", the absolute location of which is found and set as System property.
	 *
	 * @throws IOException
	 */
	private void initNativeLibPath() throws IOException {
		if (nativeLibsFound)
			return;

		Bundle bundle = Platform.getBundle("ag.ion.noa");
		URL bundleUrl = FileLocator.find(bundle, new Path("/lib"), null);

		if (bundleUrl != null) {
			URL bundleFileUrl = FileLocator.toFileURL(bundleUrl);
			System.out.println("OpenOfficeEditor found bundleFileUrl " + bundleFileUrl);
			String fileUrl = bundleFileUrl.toExternalForm();
			if (fileUrl.startsWith("file:/")) {
				String filePath = fileUrl.substring(5);
				System.setProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH, filePath);
				System.out.println("OpenOfficeEditor using native lib path " + filePath);
				nativeLibsFound = true;
			}
		}
	}


	/**
	 * Wraps the creation and activation of the OpenOffice stuff in an hourglass if needed
	 */
	private void buildOfficeFrameAndLoadDocument() {
		try {
			BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					buildOfficeFrameAndLoadDocument(monitor);
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Starts OpenOffice if necessary, opens an embedded OOo-Window and loads the Document that is contained as byte[]
	 * in the DataStoreVO.
	 */
	private void buildOfficeFrameAndLoadDocument(IProgressMonitor monitor) {
		try {

			// Starts OpenOffice if necessary
			officeApplication = Activator.getDefault().getManagedLocalOfficeApplication(openOfficePath);

			if (!officeApplication.isActive()) {
				monitor.beginTask(CoreI18N.OpenOffice_Starting, -1);
				officeApplication.activate();
			}

			// Opens an embedded OOo-Window which delegates Ctrl+S to the doSave method
			officeFrame = officeApplication.getDesktopService().constructNewOfficeFrame(awtPanel);
			officeFrame.addDispatchDelegate(GlobalCommands.SAVE, new IDispatchDelegate() {

				@Override
				public void dispatch(Object[] arg0) {
					// Tell the display thread to execute an action with a progress monitor, but asynchroneously
					// othewise OOo deadlocks
					SWTHelper.asyncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
							try {
								BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {
									@Override
									public void run(IProgressMonitor monitor)
										throws InvocationTargetException,
										InterruptedException {
										doSave(monitor);
									}
								});
							}
							catch (Exception e) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							}
						}
					});
				}
			});

			officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
			officeFrame.disableDispatch(GlobalCommands.CLOSE_WINDOW);
			officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
			officeFrame.updateDispatches();

			// Loads the Document
			monitor.beginTask(CoreI18N.OpenOffice_Loading, -1);

			/*
			 * The "white editor" seemed to be a consequence of an NPE during loadDocument(), which seemed to happen
			 * when a process "soffice.bin" was running with a client that was prevously terminated abruptly.
			 */
			try {
				loadDocument();
			}
			catch (NullPointerException e) {
				RegasusErrorHandler.handleError(
					Activator.PLUGIN_ID,
					getClass().getName(),
					e,
					CoreI18N.OpenOfficeError_TryKillRunningSofficeProcess);
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void loadDocument() throws Exception {
		IDocumentService documentService = officeApplication.getDocumentService();

		// Fetch the object with the byte[] of the document
		dataStoreVO = getDataStoreMgr().getDataStoreVO(editorInput.getKey(), true);

		// Create document from ByteArray
		if (dataStoreVO != null) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(dataStoreVO.getContentUncompressed());
			officeDocument = documentService.loadDocument(officeFrame, inputStream, DocumentDescriptor.DEFAULT);

			if (officeApplication instanceof ITextDocument) {
				((ITextDocument) officeDocument).zoom(DocumentZoomType.OPTIMAL, (short) 0);
			}
		}
		else {
			officeDocument =
				documentService.constructNewDocument(
					officeFrame,
					IDocument.WRITER,
					DocumentDescriptor.DEFAULT);
		}

		// When the document changes, the RCP editor gets dirty
		officeDocument.addDocumentListener(new DocumentAdapter() {
			@Override
			public void onModifyChanged(IDocumentEvent arg0) {
				setDirty(true);
			}

		});

		frame.validate();
	}

}
