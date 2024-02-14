package de.regasus.core.ui.editor;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.auth.AuthorizationException;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.statusline.CountHandlesStatusLineContribution;

public abstract class AbstractEditor<EditorInputType extends AbstractEditorInput<?>>
extends EditorPart
implements ModifyListener {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private boolean isDirty = false;

	protected Composite parent;

	protected Composite widgetsComposite;

	protected EditorInputType editorInput;

	protected boolean enableEditorTopComposite = true;

	/**
	 * The init method is called by the {@link AbstractEditor} whithin its {@link #init(IEditorSite, IEditorInput)},
	 * after site and input have been set, and before name and tooltip are going to be set via calls to
	 * {@link #getName()} and {@link #getToolTipText()}. Subclasses need to implement this method and should gather all data
	 * from the {@link #editorInput} so the subsequent calls to {@link #getName()} can be answered properly.
	 * <p>
	 * Another responsibility is to register with any models which might signal that the shown entities have been changed.
	 * DO NOT FORGET to deregister with those models in the {@link #dispose()} method!
	 *
	 * Typical steps are:
	 * - handle EditorInput
	 * - get model
	 * - register at model
	 * - get entity or create empty entity
	 *
	 * @throws Exception
	 */
	protected abstract void init() throws Exception;


	/**
	 * Subclasses need to implement this method and give the name to be shown in the editor tab,
	 * preferrably by using data that was gathered in the {@link #init()} method.
	 * @return
	 */
	protected abstract String getName();


	/**
	 * Subclasses need to implement this method and give the name to be shown in the
	 * editor top panel.
	 *
	 * @return
	 */
	protected String getTypeName() {
		return "";
	}

	protected String getInfoButtonToolTipText() {
		return CoreI18N.InfoButtonToolTip;
	}


	protected abstract String getToolTipText();

	protected boolean abort = false;


	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			// RCP Stuff
			setSite(site);
			setInput(input);

			// handle EditorInput
			editorInput = (EditorInputType) input;

			// call of abstract method, to be implemented by subclasses.
			init();

			// set the name in the EditorInput
			String name = getName();
			editorInput.setName(name);
			editorInput.setToolTipText(getToolTipText());

			// set PartName
			setPartName(name);
		}
		catch (EntityNotFoundException e) {
			MessageDialog.openInformation(getSite().getShell(), "", e.getMessage());
			abort = true;
			closeLater();
//			throw new PartInitException(e.getMessage(), e);
		}
		catch (AuthorizationException e) {
			MessageDialog.openInformation(getSite().getShell(), "", e.getMessage());
			abort = true;
			closeLater();
//			throw new PartInitException(e.getMessage());
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			abort = true;
			closeLater();
//			throw new PartInitException(t.getMessage(), t);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		CountHandlesStatusLineContribution.getInstance().updateCountAndLabel();
	}


	@Override
	final public void createPartControl(Composite parent) {
		this.parent = parent;

		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));

		if (abort) {
			setPartName("");
			editorInput.setName("");
			editorInput.setToolTipText("");

			return;
		}

		if (enableEditorTopComposite) {
			// EditorTopComposite
			EditorTopComposite editorTopComposite = new EditorTopComposite(mainComposite, SWT.NONE);
			editorTopComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			editorTopComposite.setEditorTypeLabelText(getTypeName());
			editorTopComposite.setInfoButtonToolTipText(getInfoButtonToolTipText());
			editorTopComposite.addInfoButtonSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					openInfoDialog();
				}
			});
		}

		widgetsComposite = new Composite(mainComposite, SWT.NONE);
		widgetsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		widgetsComposite.setLayout(new FillLayout());

		createWidgets(widgetsComposite);
		if (isNew()) {
			setDirty(true);
		}

		CountHandlesStatusLineContribution.getInstance().updateCountAndLabel();

		afterCreatePartControl();
	}


	protected abstract void createWidgets(Composite parent);


	/**
	 * This method is mentioned to be overwritten.
	 * It is called at the end of createPartControl(Composite parent).
	 */
	protected void afterCreatePartControl() {
	}


	public Object getKey() {
		return editorInput.getKey();
	}


	public abstract boolean isNew();

	@Override
	public boolean isDirty() {
		return isDirty;
	}


	/**
	 * Encapsulation of the firePropertyChange in the Display-Thread, otherwise it has no effect.

	 * This method must be public so that a copied profile which is not yet stored on the server
	 * can be made to show up in a dirty editor.
	 */
	public void setDirty(final boolean dirty) {
		if (dirty != isDirty) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					isDirty = dirty;
					firePropertyChange(PROP_DIRTY);
				}
			});
		}
	}


	@Override
	public void setFocus() {
		try {
			widgetsComposite.setFocus();
		}
		catch (Exception e) {
			ErrorHandler.logError(e);
		}
	}


	@Override
	public void doSaveAs() {
	}


	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}


	@Override
	public void modifyText(ModifyEvent event) {
		setDirty(true);
	}


	/**
	 * An editor refresh which reads its data anew from the server
	 */
	public abstract void refresh() throws Exception;

	/**
	 * Closes this editor asynchronous.
	 */
	protected void close() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					getSite().getPage().closeEditor(AbstractEditor.this, false /* save */);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		});
	}


	protected void closeLater() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					sleep(100);
				}
				catch (InterruptedException e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				close();
			}
		};

		t.start();
	}


	public static boolean closeAllEditors() {
		final boolean[] cancel = {false};

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {

				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (int i = 0; i < windows.length; i++) {
					IWorkbenchPage[] pages = windows[i].getPages();
					for (int j = 0; j < pages.length; j++) {
						IEditorReference[] editorRefs = pages[j].getEditorReferences();
						for (int k = 0; k < editorRefs.length; k++) {
							IEditorPart editor = editorRefs[k].getEditor(false /* restore */);
							boolean closed = pages[j].closeEditor(editor, true /* save */);
							if (!closed) {
								cancel[0] = true;
								break;
							}
						}
					}
				}

			}
		});

		return cancel[0];
	}


	public static void closeEditor(Object key) {
		if (key == null) {
			return;
		}

		final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				final IWorkbenchPage page = pages[j];
				IEditorReference[] editorRefs = page.getEditorReferences();
				for (int k = 0; k < editorRefs.length; k++) {
					final IEditorPart editor = editorRefs[k].getEditor(false /* restore */);
					if (editor != null) {
						IEditorInput input = editor.getEditorInput();
						if (input instanceof AbstractEditorInput) {
							AbstractEditorInput<?> editorInput = (AbstractEditorInput<?>) input;
							if (key.equals(editorInput.getKey())) {

								SWTHelper.syncExecDisplayThread(new Runnable() {

									@Override
									public void run() {
										page.closeEditor(editor, false /* save */);
									}

								});

							}
						}
					}
				}
			}
		}
	}


	public static void closeEditors(Collection<?> keys) {
		if (keys == null || keys.isEmpty()) {
			return;
		}

		final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			final IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				final IWorkbenchPage page = pages[j];
				final IEditorReference[] editorRefs = page.getEditorReferences();
				for (int k = 0; k < editorRefs.length; k++) {
					final IEditorPart editor = editorRefs[k].getEditor(false /* restore */);
					final IEditorInput input = editor.getEditorInput();
					if (input instanceof AbstractEditorInput) {
						final AbstractEditorInput<?> editorInput = (AbstractEditorInput<?>) input;
						if (keys.contains(editorInput.getKey())) {

							SWTHelper.syncExecDisplayThread(new Runnable() {

								@Override
								public void run() {
									page.closeEditor(editor, false /* save */);
								}

							});

						}
					}
				}
			}
		}
	}


	/**
	 * Checks whether the parent composite's display belongs to the current thread. If yes, the given runnable is
	 * executed immediately by straight method call, otherwise the method via the Display is used.
	 */
	public void asyncExecInParentDisplay(Runnable runnable) {
		if (parent.isDisposed()) {
			return;
		}

		if (parent.getDisplay().getThread() == Thread.currentThread()) {
			runnable.run();
		}
		else {
			SWTHelper.asyncExecDisplayThread(runnable);
		}
	}

	/**
	 * Checks whether the parent composite's display belongs to the current thread. If yes, the given runnable is
	 * executed immediately by straight method call, otherwise the method via the Display is used.
	 */
	public void syncExecInParentDisplay(Runnable runnable) {
		if (parent.getDisplay().getThread() == Thread.currentThread()) {
			runnable.run();
		}
		else {
			SWTHelper.syncExecDisplayThread(runnable);
		}
	}

	/**
	 * Tells the user that the current editor will be closed because the edited object has been deleted, and
	 * subsequently closes this editor. This method will typically be called when an editor is told to refresh himself,
	 * he tries to reload the entity with the PK from the editor input, and receives null.
	 * <p>
	 * The closing is done now asynchroneously to avoid blocking, since this message dialog might otherwise
	 * appear behind the progress monitor dialog!
	 */
	protected void closeBecauseDeletion() {
		asyncExecInParentDisplay(new Runnable() {
			@Override
			public void run() {
				String message = de.regasus.core.ui.CoreI18N.Message_EditorWasClosedBecauseObjectWasDeleted;
				String info = NLS.bind(message, getPartName());
				MessageDialog.openInformation(getSite().getShell(), UtilI18N.Info, info);
			}
		});

		close();
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.Name,
			UtilI18N.ID
		};

		// the values of the info dialog
		final String[] values = {
			getName(),
			editorInput.getKey() != null ? editorInput.getKey().toString() : ""
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			getEditorInput().getName() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	/**
	 * Save the editors data if its data is dirty.
	 * If the parameter confirm is true, the user is asked to save the editors data.
	 * The result is true if the editor's data is not dirty anymore.
	 *
	 * @param confirm
	 * @return
	 */
	public boolean save(boolean confirm) {
		boolean safe = true;

		if ( isDirty() ) {
			/* Save the editor.
			 * The result will be true if the user chooses Yes or No!
			 * Only if the user chooses Cancel the result is false!
			 */
			IWorkbenchPage page = getSite().getPage();
			page.saveEditor(this, confirm);
			safe = !isDirty();
		}

		return safe;
	}


	/**
	 * Checks whether the editor belonging to the given {@link IEditorInput} contains unsaved data and,
	 * if this is the case, prompts the user to save it.
	 *
	 * The result is <code>true</code> if there is no corresponding dirty editor open.
	 *
	 * @param editorInput
	 * @return
	 */
	public static boolean saveEditor(IEditorInput editorInput) {
		Objects.requireNonNull(editorInput);

		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		boolean safe = true;
		IEditorPart editor = page.findEditor(editorInput);
		if (editor != null && editor.isDirty()) {
			/* Save the editor.
			 * The result will be true if the user chooses Yes or No!
			 * Only if the user chooses Cancel the result is false!
			 */
			page.saveEditor(editor, true);
			safe = !editor.isDirty();
		}

		return safe;
	}


	/**
	 * Check if all editors of a given class are saved.
	 * @param editorClass {@link Class} of the editor that is checked.
	 *        If the parameter is <code>null</code>, all editors are checked.
	 * @return true if all editors of the given class are saved, false if at least one editor is not
	 */
	public static boolean saveEditors(Class<?> editorClass) {
		Objects.requireNonNull(editorClass);

		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart[] dirtyEditors = page.getDirtyEditors();

		if (dirtyEditors != null) {
			for (IEditorPart editorPart : dirtyEditors) {
				if (editorPart.getClass() == editorClass) {
    				/* Save the editor.
    				 * The result will be true if the user chooses Yes or No!
    				 * Only if the user chooses Cancel the result is false!
    				 */
    				page.saveEditor(editorPart, true);

    				// check if the editor is still dirty which means that the user either selected "cancel" or "no"
    				if ( editorPart.isDirty() ) {
    					return false;
    				}
				}
			}
		}

		return true;
	}


	/**
	 * Check if all editors of a given class are saved.
	 * @return true if all editors of the given class are saved, false if at least one editor is not
	 */
	public static boolean saveAllEditors() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart[] dirtyEditors = page.getDirtyEditors();

		if (dirtyEditors != null) {
			for (IEditorPart editorPart : dirtyEditors) {
				/* Save the editor.
				 * The result will be true if the user chooses Yes or No!
				 * Only if the user chooses Cancel the result is false!
				 */
				page.saveEditor(editorPart, true);

				// check if the editor is still dirty which means that the user either selected "cancel" or "no"
				if ( editorPart.isDirty() ) {
					return false;
				}
			}
		}

		return true;
	}


//	/**
//	 * Checks whether the editor belonging to the given {@link IEditorInput} contains unsaved data and,
//	 * if this is the case, prompts the user to save it.
//	 * If the user cancels this dialog, the result is <code>false</code>. In all other cases the result is <code>true</code>.
//	 * If the result is <code>true</code>, it means that the editor either contains no unsaved data (anymore) or that
//	 * the user does not want to save it.
//	 *
//	 * @param editorInput
//	 * @return
//	 */
//	public static boolean checkEditorIsSaved(IEditorInput editorInput) {
//		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		IWorkbenchPage page = workbenchWindow.getActivePage();
//		IEditorPart editor = page.findEditor(editorInput);
//		if (editor != null && editor.isDirty()) {
//			/* Save the editor.
//			 * The result will be true if the user chooses Yes or No!
//			 * Only if the user chooses Cancel the result is false!
//			 */
//			return page.saveEditor(editor, true);
//		}
//
//		return true;
//	}


	/**
	 * Checks whether the active editor contains unsaved data and, if this is the case, prompts the user to save it.
	 * If the user cancels this dialog, the result is <code>false</code>. In all other cases the result is <code>true</code>.
	 * If the result is <code>true</code>, it means that the editor either contains no unsaved data (anymore) or that
	 * the user does not want to save it.
	 * @param editorInput
	 * @return
	 */
	public static boolean saveActiveEditor() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart activeEditor = page.getActiveEditor();

		if (activeEditor != null && activeEditor.isDirty()) {
			/* Save the editor.
			 * The result will be true if the user chooses Yes or No!
			 * Only if the user chooses Cancel the result is false!
			 */
			return page.saveEditor(activeEditor, true);
		}

		return true;
	}


	/**
	 * Check if an editor is saved.
	 * @param editorInput of the editor that is checked.
	 * @return true if the editors is saved
	 */
	public static boolean isEditorSaved(AbstractEditorInput<?> editorInput) {
		Objects.requireNonNull(editorInput);

		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart editor = page.findEditor(editorInput);

		boolean safe =
			   editor == null
			|| !editor.isDirty();

		return safe;
	}


	/**
	 * Check if all editors of a given class are saved.
	 * @param editorClass {@link Class} of the editor that is checked.
	 *        If the parameter is <code>null</code>, all editors are checked.
	 * @return true if all editors of the given class are saved, false if at least one editor is not
	 */
	public static boolean isEditorsSaved(Class<?> editorClass) {
		Objects.requireNonNull(editorClass);

		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart[] dirtyEditors = page.getDirtyEditors();

		if (dirtyEditors != null) {
			for (IEditorPart editorPart : dirtyEditors) {
				if (editorPart.getClass() == editorClass) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Check if all editors are saved.
	 * @param editorClass
	 * @return true if all editors are saved, false if at least one editor is not
	 */
	public static boolean isAllEditorsSaved() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart[] dirtyEditors = page.getDirtyEditors();
		return dirtyEditors == null || dirtyEditors.length == 0;
	}


	/**
	 * Get editors with unsaved data.
	 * @param editorClass {@link Class} of the editor that is checked.
	 *        If the parameter is <code>null</code>, all editors are checked.
	 */
	public static List<IEditorPart> getDirtyEditors(Class<?> editorClass) {
		List<IEditorPart> dirtyEditorList = new ArrayList<>();

		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		IEditorPart[] dirtyEditors = page.getDirtyEditors();

		if (dirtyEditors != null) {
			for (IEditorPart editorPart : dirtyEditors) {
				if (editorClass != null && editorPart.getClass() == editorClass) {
					dirtyEditorList.add(editorPart);
				}
			}
		}

		return dirtyEditorList;
	}

}
