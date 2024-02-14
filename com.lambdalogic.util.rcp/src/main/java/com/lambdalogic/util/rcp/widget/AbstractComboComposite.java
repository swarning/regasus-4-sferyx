package com.lambdalogic.util.rcp.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;

public abstract class AbstractComboComposite<EntityType> extends Composite {

	protected static final String EMPTY_ELEMENT = "";

	/**
	 * Value for the field modelDataDiscriminator to tell the Combo it
	 * must not show any values.
	 */
	public static final Object NO_DATA_MODEL_DISCRIMINATOR = new Object();

	// *************************************************************************
	// * Widgets
	// *

	protected ComboViewer comboViewer;

	protected Combo combo;

	protected ViewerSorter viewerSorter = null;

	// *
	// * Widgets
	// *************************************************************************

	// *************************************************************************
	// * Other attributes
	// *

	/**
	 * The previous entity must be an arbitrary object being different from each entity, because otherwise (if it was
	 * null) there would be no change detected when switching from a certain selection to the the empty selection.
	 */
	protected Object previousEntity = new Object();

	protected EntityType entity = null;

	protected Collection<EntityType> modelData = null;

	protected boolean withEmptyElement = true;

	/**
	 * When synchronized to its model, the entity may have been removed from the list.
	 * This may happen when an entity is a) actually deleted, or b) just "hidden".
	 * <p>
	 * In case a), this combo shouldn't show the entity anymore (eg a deleted
	 * event in the participant search). In case b) the entity must be shown
	 * (eg a hidden participant type in a programme point).
	 */
	protected boolean keepEntityInList = true;

	protected ModifySupport modifySupport = new ModifySupport(this);

	/**
	 * An arbitrary object that might be needed by subclasses to determine the contents of a model.
	 */
	protected Object modelDataDiscriminator;

	// *
	// * Other attributes
	// *************************************************************************

	// **************************************************************************
	// * abstract methods
	// *

	protected abstract Object getEmptyEntity();

	protected abstract LabelProvider getLabelProvider();

	protected abstract Collection<EntityType> getModelData() throws Exception;

	protected abstract void initModel();

	protected abstract void disposeModel();

	// *
	// * abstract methods
	// **************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	/**
	 * Constructor with standard arguments for SWT widgets.
	 */
	public AbstractComboComposite(Composite parent, int style) throws Exception {
		this(parent, style, null);
	}


	/**
	 * Constructor with standard arguments for SWT widgets plus a discriminator.
	 * <p>
	 * During the constructor execution of this class, the data is loaded via initModel in a subclass.
	 * It might be that the data depends on some kind of criteria (like the Event in the case of an
	 * InvoiceNoRangeCombo); but the difficulty is that it isn't sufficient to set that attribute
	 * in the subclasses constructor, because that setting happens afterwards (super must always be
	 * the first statement)!
	 * <p>
	 * As solution, the constructor is now overloaded to store in an attribute, if needed, any object that might
	 * determine the model contents before initModel is called.
	 */
	public AbstractComboComposite(Composite parent, int style, Object modelDataDiscriminator)
	throws Exception {
		super(parent, SWT.NONE);

		this.modelDataDiscriminator = modelDataDiscriminator;

		addDisposeListener(disposeListener);

		createWidgets(this, style);

		comboViewer = new ComboViewer(combo);
		combo.setVisibleItemCount(12);

		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(getLabelProvider());

		if (getViewerSorter() != null) {
			comboViewer.setSorter(getViewerSorter());
		}


		combo.addModifyListener(new ModifyListener() {
			@Override
			public synchronized void modifyText(ModifyEvent modifyEvent) {
				if (modifySupport.isEnabled()) {
					entity = getEntityFromComboViewer();
					// only fire if a different entity was selected
					if (previousEntity != entity) {
						if (modifyEvent == null) {
							Event event = new Event();
							event.widget = combo;
							modifyEvent = new ModifyEvent(event);
						}
						modifyEvent.data = new ClassValuePair(getClass(), entity);

						modifySupport.fire(modifyEvent);
					}
				}

				// previousEntity must be set even if sync is true
				previousEntity = entity;
			}
		});

		initModel();

		syncComboToModel();
	}

	// *
	// * Constructors
	// *************************************************************************


	protected void createWidgets(Composite parent, int style) {
		setLayout(new FillLayout());

		combo = new Combo(parent, style | SWT.READ_ONLY);
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			try {
				disposeModel();
				modelData = null;
				entity = null;
			}
			catch (Throwable t) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(t);
				ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	};


	/**
	 * Return the internal Combo widget.
	 * Needed to make a Combo bold.
	 * @return
	 */
	public Combo getCombo() {
		return combo;
	}


	@Override
	public void setEnabled(boolean enabled) {
		combo.setEnabled(enabled);
	}


	@Override
	public boolean getEnabled() {
		return combo.getEnabled();
	}


	@SuppressWarnings("unchecked")
	public boolean contains(EntityType entity) {
		boolean result = false;

		List<Object> list = (List<Object>) comboViewer.getInput();
		if (list != null) {
			result = list.contains(entity);
		}

		return result;
	}


	/**
	 * Returns the ViewerSorter. Override this method to use another ViewerSorter. If the overwritten method returns
	 * null the viewer elements won't be sorted.
	 *
	 * @return
	 */
	protected ViewerSorter getViewerSorter() {
		if (viewerSorter == null) {
			viewerSorter = new ViewerSorter();
		}
		return viewerSorter;
	}


	public EntityType getEntity() {
		return entity;
	}


	public void setEntity(EntityType entity) {
		try {
			if (entity != null && modelData != null) {
				this.entity = entity;

				/* If entity is not in modelData sync combo to model again.
				 * It will add the entity to the Combo automatically.
				 */
				if (!modelData.contains(entity)) {
					syncComboToModel();
				}
			}
			else {
				this.entity = null;
			}

			syncComboToEntity();
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public boolean isWithEmptyElement() {
		return withEmptyElement;
	}


	public boolean isEntitySelected() {
		return entity != null && entity != getEmptyEntity();
	}


	public void setWithEmptyElement(boolean withEmptyElement) throws Exception {
		if (this.withEmptyElement != withEmptyElement) {
			this.withEmptyElement = withEmptyElement;
			syncComboToModel();
		}
	}


	protected void setModelDataDiscriminator(Object modelDataDiscriminator) throws Exception {
		this.modelDataDiscriminator = modelDataDiscriminator;
		syncComboToModel();
	}


	/**
	 * Synchronisiert die Werteliste der Combo mit dem Model.
	 *
	 * @throws Exception
	 */
	protected synchronized void syncComboToModel() throws Exception {
		// save the current entity so we can notice if the entity has changed later
		EntityType currentEntity = entity;

		if (modelDataDiscriminator == NO_DATA_MODEL_DISCRIMINATOR) {
			modelData = Collections.emptyList();
		}
		else {
			modelData = getModelData();
		}



		List<Object> list = null;
		if (modelData != null) {
    		if (withEmptyElement && getEmptyEntity() != null) {
    			if (modelData != null) {
    				// + 2: to have space for the empty and a deleted element
    				list = new ArrayList<>(modelData.size() + 2);
    				// add empty element
    				Object emptyEntity = getEmptyEntity();
    				list.add(emptyEntity);
    				list.addAll(modelData);
    			}
    		}
    		else {
    			// + 1: to have space for a deleted element
    			list = new ArrayList<>(modelData.size() + 1);
    			list.addAll(modelData);
    		}
		}
		else {
			list = new ArrayList<>(1);
		}

		/* Add entity to list if it is not an element of the list.
		 * This happens if the entity is deleted.
		 *
		 * For MIRCP-826 this is possible to be disabled, eg an EventCombo
		 * in ParticipantSearch shouldn't show a deleted Event.
		 */
		if (entity != null && !list.contains(entity)) {
			if (keepEntityInList) {
				list.add(entity);
			}
			else {
				entity = null;
			}
		}

		if (currentEntity != null && entity == null) {
			/* Don't call modifyText(null), because it will call getEntityFromComboViewer()
			 * which will determine the entity from the comboViewer. But, the comboViewer
			 * may not be updated yet, because of the asynchronous call above.
			 */

			if (modifySupport.isEnabled()) {
				Event event = new Event();
				event.widget = combo;

				ModifyEvent modifyEvent = new ModifyEvent(event);
				modifyEvent.data = new ClassValuePair(getClass(), entity);

				modifySupport.fire(modifyEvent);
			}
		}


		/* If we use syncExec(Runnable), comboViewer.setSelection(selection)
		 * never returns when refreshing all models!
		 * Why? I don't know, yet.
		 *
		 * Furthermore, this code must be done after informing the ModifyListeners.
		 * Otherwise calling modifyListener.modifyText(e) does not return!
		 */
		final List<Object> finalList = list;
		SWTHelper.asyncExecDisplayThread(new Runnable() {

			@Override
			public void run() {
				try {
					// When the combo is disposed (eg after an editor was closed), no synchronizing needs to take place
					if (combo.isDisposed()) {
						return;
					}

					modifySupport.setEnabled(false);
					comboViewer.setInput(finalList);

					if (entity != null) {
						StructuredSelection selection = new StructuredSelection(entity);
						comboViewer.setSelection(selection, true);
					}
					else {
						combo.deselectAll();
					}
				}
				catch (Throwable t) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
				finally {
					modifySupport.setEnabled(true);
				}
			}

		});

	}


	/**
	 * Synchronisiert die Combo mit dem Entity
	 */
	private synchronized void syncComboToEntity() {
		/* If we use syncExec(Runnable), comboViewer.setSelection(selection)
		 * never returns in some cases!
		 * Why? I don't know, yet.
		 */
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					modifySupport.setEnabled(false);
					if (entity != null) {
						StructuredSelection selection = new StructuredSelection(entity);
						comboViewer.setSelection(selection, true);
					}
					else {
						combo.deselectAll();
					}
				}
				catch (Throwable t) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
				finally {
					modifySupport.setEnabled(true);
				}
			}
		});

	}


	protected void handleModelChange() throws Exception {
		if ( ! isDisposed() ) {
			syncComboToModel();
		}
	}


	@SuppressWarnings("unchecked")
	protected EntityType getEntityFromComboViewer() {
		EntityType entity = null;
		IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();

		if (selection != null && selection.size() == 1) {
			Object element = selection.getFirstElement();
			if (element == getEmptyEntity()) {
				entity = null;
			}
			else {
				entity = (EntityType) element;
			}
		}
		return entity;
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

	// *
	// * Modifying
	// **************************************************************************

	public String getText() {
		return combo.getText();
	}


	@Override
	public void setToolTipText(String string) {
		combo.setToolTipText(string);
	}

	public void setKeepEntityInList(boolean keepEntityInList) {
		this.keepEntityInList = keepEntityInList;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	/**
	 * Return all entities that have been returned from {@link #getModelData()}.
	 * The result does not contain {@link #EMPTY_ELEMENT} and not the entity if it has been added automatically.
	 *
	 * @return
	 */
	public Collection<EntityType> getEntities() {
		if (modelData != null) {
			return Collections.unmodifiableCollection(modelData);
		}
		return Collections.emptyList();
	}

}
