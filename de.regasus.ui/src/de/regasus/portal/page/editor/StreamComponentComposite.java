package de.regasus.portal.page.editor;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.StreamComponent;

public class StreamComponentComposite extends EntityComposite<StreamComponent> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private Portal portal;

	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private StreamComponentGeneralComposite generalComposite;
	private StreamComponentVisibleFieldsComposite visibleFieldsComposite;
	private StreamComponentContentComposite contentComposite;
	private StreamComponentLabelsComposite labelsComposite;

	// *
	// * Widgets
	// **************************************************************************


	public StreamComponentComposite(Composite parent, int style, Long portalPK)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Long portalPK = (Long) initValues[0];

		// load Portal
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.StreamComponent.getString() );

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

		// General Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(UtilI18N.General);
    		generalComposite = new StreamComponentGeneralComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(generalComposite);
    		generalComposite.addModifyListener(modifySupport);
		}

		// Visible Fields Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_AvailableItems);
			visibleFieldsComposite = new StreamComponentVisibleFieldsComposite(tabFolder, SWT.NONE);
			tabItem.setControl(visibleFieldsComposite);
			visibleFieldsComposite.addModifyListener(modifySupport);
		}

		// Content Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_FieldContent);
			contentComposite = new StreamComponentContentComposite(tabFolder, SWT.NONE);
			tabItem.setControl(contentComposite);
			contentComposite.addModifyListener(modifySupport);
		}

		// Labels Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_Labels);
			labelsComposite = new StreamComponentLabelsComposite(tabFolder, SWT.NONE, portal.getId());
			tabItem.setControl(labelsComposite);
			labelsComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		generalComposite.setEntity(entity);
		visibleFieldsComposite.setEntity(entity);
		contentComposite.setEntity(entity);
		labelsComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		generalComposite.syncEntityToWidgets();
		visibleFieldsComposite.syncEntityToWidgets();
		contentComposite.syncEntityToWidgets();
		labelsComposite.syncEntityToWidgets();
	}


	public void setFixedStructure(boolean fixedStructure) {
		generalComposite.setFixedStructure(fixedStructure);
	}

}
