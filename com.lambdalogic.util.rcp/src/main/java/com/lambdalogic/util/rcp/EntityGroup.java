package com.lambdalogic.util.rcp;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetBuilder;


/**
 * Base class for {@link Group} implementations that refer to an entity.
 *
 * Local fields must not be initialized at their declaration but in {@link #initialize(Object[])}!
 * {@link #createWidgets(Composite)} is called by super constructor and therefore run before local
 * fields are initialized.
 *
 * @param <EntityType>
 */
public abstract class EntityGroup<EntityType> extends Group implements IModifiable {

	protected EntityType entity;

	protected ModifySupport modifySupport = new ModifySupport(this);

	protected WidgetBuilder<EntityType> widgetBuilder = new WidgetBuilder<>(this, modifySupport);


	public EntityGroup(Composite parent, int style, Object... initValues)
	throws Exception {
		super(parent, style);

		try {
			initialize(initValues);
			createWidgets(this);
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Initialize local fields.
	 * Local fields must not be initialized at their declaration but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields
	 * are initialized.
	 *
	 * @param initValues
	 * @throws Exception
	 */
	protected void initialize(Object[] initValues) throws Exception {
	}


	/**
	 * Set the {@link Layout} and create all {@link Widget}s.
	 * This method is called by the constructor. For implementing classes, this means that their
	 * implementation of {@link #createWidgets(Composite)} is also called before their local fields
	 * are initialized. Therefore, these must be initialized in {@link #initialize(Object[])}.
	 * @throws Exception
	 */
	protected abstract void createWidgets(Composite parent) throws Exception;


	public EntityType getEntity() {
		return entity;
	}


	public void setEntity(EntityType entity) {
		this.entity = entity;

		if (entity != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						syncWidgetsToEntity();
					}
					catch (Exception e) {
						ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	/**
	 * Copy the values from the {@link #entity} to the {@link Widget}s.
	 * This method is only called if {@link #entity} is not null.
	 */
	protected void syncWidgetsToEntity() throws Exception {
		widgetBuilder.syncWidgetsToEntity(entity);
	}


	/**
	 * Copy the values from the {@link Widget}s to the {@link #entity}.
	 * Since this method is public it might be called even if {@link #entity} is null!
	 */
	public void syncEntityToWidgets() {
		widgetBuilder.syncEntityToWidgets(entity);
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
