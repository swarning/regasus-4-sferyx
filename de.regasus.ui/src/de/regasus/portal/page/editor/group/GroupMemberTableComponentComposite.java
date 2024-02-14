package de.regasus.portal.page.editor.group;

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
import de.regasus.portal.component.group.GroupMemberTableComponent;
import de.regasus.portal.page.editor.PageWidgetBuilder;

public class GroupMemberTableComponentComposite extends EntityComposite<GroupMemberTableComponent> {

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

	private GroupMemberTableComponentGeneralComposite generalComposite;
	private GroupMemberTableComponentAvailableItemsComposite availableItemsComposite;
	private GroupMemberTableComponentLabelsComposite columnNameComposite;

	// *
	// * Widgets
	// **************************************************************************


	public GroupMemberTableComponentComposite(Composite parent, int style, Long portalPK)
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

		widgetBuilder.buildTypeLabel( PortalI18N.GroupMemberTableComponent.getString() );

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

		// General Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(UtilI18N.General);
    		generalComposite = new GroupMemberTableComponentGeneralComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(generalComposite);
    		generalComposite.addModifyListener(modifySupport);
		}

		// Available Items Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_AvailableItems);
			availableItemsComposite = new GroupMemberTableComponentAvailableItemsComposite(tabFolder, SWT.NONE);
			tabItem.setControl(availableItemsComposite);
			availableItemsComposite.addModifyListener(modifySupport);
		}

		// Labels Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_Labels);
			columnNameComposite = new GroupMemberTableComponentLabelsComposite(tabFolder, SWT.NONE, portal.getId());
			tabItem.setControl(columnNameComposite);
			columnNameComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		generalComposite.setEntity(entity);
		availableItemsComposite.setEntity(entity);
		columnNameComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		generalComposite.syncEntityToWidgets();
		availableItemsComposite.syncEntityToWidgets();
		columnNameComposite.syncEntityToWidgets();
	}


	public void setFixedStructure(boolean fixedStructure) {
		generalComposite.setFixedStructure(fixedStructure);
	}

}
