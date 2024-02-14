package com.lambdalogic.util.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetBuilder;


public abstract class EntityScrolledComposite<EntityType> extends ScrolledComposite implements IModifiable {

	protected EntityType entity;

	protected ModifySupport modifySupport = new ModifySupport(this);

	protected WidgetBuilder<EntityType> widgetBuilder = new WidgetBuilder<>(this, modifySupport);


	public EntityScrolledComposite(Composite parent, int style, Object... initValues)
	throws Exception {
		super(parent, style | SWT.V_SCROLL);

		try {
			setExpandHorizontal(true);
			setExpandVertical(true);

			Composite contentComposite = new Composite(this, SWT.NONE);

			initialize(initValues);
			createWidgets(contentComposite);

			setContent(contentComposite);
			Point point = contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			contentComposite.setSize(point);
			setMinSize(point);
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void initialize(Object[] initValues) throws Exception {
	}


	/**
	 * The the {@link Layout} and create all {@link Widget}s.
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
