package com.lambdalogic.util.rcp.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.lambdalogic.util.rcp.IconRegistry;
import com.lambdalogic.util.rcp.LazyableComposite;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.common.Language;

/**
 * A {@link Composite} to show the same set of widgets for a list of languages.
 * The {@link Composite} contains a {@link CTabFolder} with one {@link CTabItem} for each language.
 * The widgets on each {@link CTabFolder} are defined and managed by a separate {@link I18NWidgetController}.
 *
 * The list of languages is fixed and has to be provided in the constructor.
 */
public class I18NComposite<Entity> extends Composite {

	private List<Language> languageList;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	/**
	 * CTabFolder that contains all CTabItem in cTabItemList.
	 */
	private CTabFolder folder;

	/**
	 * Map from Language to CTabItem widget.
	 */
	private Map<String, CTabItem> cTabItemMap = new HashMap<>();

	private I18NWidgetController<Entity> widgetController;

	// *
	// * Widgets
	// **************************************************************************


	public I18NComposite(
		Composite parent,
		int style,
		List<Language> languageList,
		I18NWidgetController<Entity> widgetController
	) {
		super(parent, style);

		this.languageList = Objects.requireNonNull(languageList);
		this.widgetController = widgetController;

		createWidgets();

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				widgetController.dispose();
			}
		});
	}


	private void createWidgets() {
		setLayout( new FillLayout() );


		folder = new CTabFolder(this, SWT.NONE);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(false);
		folder.setMinimizeVisible(false);
		folder.setMaximizeVisible(false);
		folder.marginWidth = 0;
		folder.marginHeight = 0;

		// create 1 Tab for each language
		for (Language language : languageList) {
			createTab(language);
		}

		folder.setSelection(0);
	}


	/**
	 * Create tab for the given language.
	 * @param language
	 */
	private void createTab(Language language) {
		CTabItem cTabItem = new CTabItem(folder, SWT.NONE);
		// add cTabItem to its Map
		cTabItemMap.put(language.getId(), cTabItem);
		cTabItem.setText( language.getId() );

		cTabItem.setToolTipText(language.getName().getString());

		// set image of cTabItem
		Image image = IconRegistry.getLanguageIcon( language.getId() );
		cTabItem.setImage(image);


		// add Composite for contained widgets
		LazyableComposite lazyableComposite = new LazyableComposite(folder, SWT.NONE);
		cTabItem.setControl(lazyableComposite);
		cTabItem.setFont( JFaceResources.getDefaultFont() );
		setLayout( new FillLayout() );



		/***** Widgets *****/

		widgetController.createWidgets(lazyableComposite, modifySupport, language.getId());
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

	// *
	// * Modifying
	// **************************************************************************


	public Entity getEntity() {
		return widgetController.getEntity();
	}


	public void setEntity(Entity entity) {
		widgetController.setEntity(entity);
	}


	@Override
	public void setEnabled(boolean enabled) {
		folder.setEnabled(enabled);

		// call setEnabled(enabled) for all subordinated Controls
		for (CTabItem cTabItem : cTabItemMap.values()) {
			Composite widgetComposite = (Composite) cTabItem.getControl();
			widgetComposite.setEnabled(enabled);
			for (Control childControl : widgetComposite.getChildren()) {
				childControl.setEnabled(enabled);
			}
		}
	}


	public void syncEntityToWidgets() {
		widgetController.syncEntityToWidgets();
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		widgetController.addFocusListener(listener);
	}

}
