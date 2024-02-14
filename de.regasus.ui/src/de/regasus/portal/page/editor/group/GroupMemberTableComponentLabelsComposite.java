package de.regasus.portal.page.editor.group;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.group.GroupMemberTableComponent;
import de.regasus.ui.Activator;


public class GroupMemberTableComponentLabelsComposite extends EntityComposite<GroupMemberTableComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private I18NComposite<GroupMemberTableComponent> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public GroupMemberTableComponentLabelsComposite(Composite parent, int style, Long portalPK)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		// determine Portal languages
		Long portalPK = (Long) initValues[0];
		Portal portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout() );

		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new GroupMemberTableComponentLabelsI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (entity != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						i18nComposite.setEntity(entity);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			i18nComposite.syncEntityToWidgets();
		}
	}

}
