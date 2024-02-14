package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.UploadComponent;
import de.regasus.users.CurrentUserModel;


public class UploadComponentComposite extends EntityComposite<UploadComponent> {

	private final int COL_COUNT = 3;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean expertMode;

	private Portal portal;
	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;

	private I18NComposite<UploadComponent> i18nComposite;

	private Text documentNameText;
	private NullableSpinner maxFileSizeSpinner;
	private Text validFileExtensionsText;
	private List<Button> validFileExtensionButtonList;

	private ConditionGroup visibleConditionGroup;
	private ConditionGroup requiredConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	private Group validFileExtensionButtonGroup;
	private SelectionListener validFileExtensionsButtonSelectionListener;




	public UploadComponentComposite(Composite parent, int style, Long portalPK)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// determine Portal languages
		Long portalPK = (Long) initValues[0];
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder pageWidgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		pageWidgetBuilder.buildTypeLabel( PortalI18N.UploadComponent.getString() );

		if (expertMode) {
			htmlIdText = pageWidgetBuilder.buildHtmlId();
			renderText = pageWidgetBuilder.buildRender();
		}

		buildI18NComposite(parent);

		buildDocumentName();
		buildMaxFileSize();
		buildValidFileExtensions();

		visibleConditionGroup = pageWidgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);

		requiredConditionGroup = pageWidgetBuilder.buildConditionGroup(I18N.PageEditor_Required);
		requiredConditionGroup.setDefaultCondition(false);
	}


	private void buildI18NComposite(Composite parent) {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new UploadCompositeI18NWidgetController());
		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT, 1)
			.applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	private void buildDocumentName() throws Exception {
		SWTHelper.createLabel(this, UploadComponent.DOCUMENT_NAME.getString(), false);

		documentNameText = new Text(this, SWT.BORDER);
		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT - 1, 1)
			.applyTo(documentNameText);
		documentNameText.addModifyListener(modifySupport);
	}


	private void buildMaxFileSize() throws Exception {
		SWTHelper.createLabel(this, UploadComponent.MAX_FILE_SIZE.getString(), false);

		maxFileSizeSpinner = new NullableSpinner(this, SWT.NONE);
		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT - 2, 1)
			.applyTo(maxFileSizeSpinner);
		maxFileSizeSpinner.setMinimum( UploadComponent.MAX_FILE_SIZE.getMin().intValue() );
		maxFileSizeSpinner.setMaximum( UploadComponent.MAX_FILE_SIZE.getMax().intValue() );
		maxFileSizeSpinner.addModifyListener(modifySupport);

		SWTHelper.createLabel(this, "MB");
	}


	private void buildValidFileExtensions() throws Exception {
		validFileExtensionButtonGroup = new Group(this, SWT.NONE);
		validFileExtensionButtonGroup.setText( UploadComponent.VALID_FILE_EXTENSIONS.getString() );
		GridDataFactory
    		.fillDefaults()
    		.grab(true, false)
    		.span(COL_COUNT, 1)
    		.indent(SWT.NONE, 10)
    		.applyTo(validFileExtensionButtonGroup);
		GridLayout groupLayout = new GridLayout(6, true);
		validFileExtensionButtonGroup.setLayout(groupLayout);

		validFileExtensionsText = new Text(validFileExtensionButtonGroup, SWT.BORDER);
		GridDataFactory
    		.fillDefaults()
    		.grab(true, false)
    		.span(groupLayout.numColumns, 1)
    		.applyTo(validFileExtensionsText);
		validFileExtensionsText.addModifyListener(e -> syncExtensionButtons(validFileExtensionsText.getText()));
		validFileExtensionsText.addModifyListener(modifySupport);


		validFileExtensionsButtonSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Button button = (Button) event.widget;
				handleButtonSelection(button);
			}
		};

		validFileExtensionButtonList = new ArrayList<>();


		addCheckbox("pdf");
		addPlaceholder();
		addCheckbox("png");
		addCheckbox("jpg/jpeg");
		addCheckbox("tif");
		addCheckbox("bmp");

		addCheckbox("odt");
		addCheckbox("doc");
		addCheckbox("docx");
		addCheckbox("ods");
		addCheckbox("xls");
		addCheckbox("xlsx");
	}


	private void addCheckbox(String extension) {
		Button button = new Button(validFileExtensionButtonGroup, SWT.CHECK);
		button.setText(extension);
		button.addSelectionListener(validFileExtensionsButtonSelectionListener);
		validFileExtensionButtonList.add(button);
	}


	private void addPlaceholder() {
		new Label(validFileExtensionButtonGroup, SWT.NONE);
	}


	private void handleButtonSelection(Button button) {
		boolean selected = button.getSelection();

		String extensionsString = validFileExtensionsText.getText();
		List<String> extensionsList = getSegments(extensionsString);
		for (ListIterator<String> it = extensionsList.listIterator(); it.hasNext();) {
			String extension = it.next();
			extension = extension.toLowerCase();
			it.set(extension);
		}

		String extension = button.getText();
		if (selected && !extensionsList.contains(extension)) {
			extensionsList.add(extension);
		}
		else if (!selected && extensionsList.contains(extension)) {
			extensionsList.remove(extension);
		}

		extensionsString = concatIfNotEmpty(", ", extensionsList);
		validFileExtensionsText.setText(extensionsString);
	}


	private void syncExtensionButtons(String extensionsString) {
		List<String> extensionsList = UploadComponent.extensionsStringToList(extensionsString);
		syncExtensionButtons(extensionsList);
	}


	private void syncExtensionButtons(List<String> extensionsList) {
		for (Button button : validFileExtensionButtonList) {
			String extension = button.getText();
			boolean selected = extensionsList != null && extensionsList.contains(extension);
			button.setSelection(selected);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		i18nComposite.setEntity(entity);

		documentNameText.setText( avoidNull(entity.getDocumentName()) );
		maxFileSizeSpinner.setValue( entity.getMaxFileSize() );
		validFileExtensionsText.setText( avoidNull(entity.getValidFileExtensionsAsString()) );
		syncExtensionButtons( entity.getValidFileExtensions() );

		visibleConditionGroup.setCondition( entity.getVisibleCondition() );
		visibleConditionGroup.setDescription( entity.getVisibleConditionDescription() );

		requiredConditionGroup.setCondition( entity.getRequiredCondition() );
		requiredConditionGroup.setDescription( entity.getRequiredConditionDescription() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
				entity.setRender( renderText.getText() );
			}

			i18nComposite.syncEntityToWidgets();

			entity.setDocumentName( documentNameText.getText() );
			entity.setMaxFileSize( maxFileSizeSpinner.getValueAsInteger() );
			entity.setValidFileExtensionsAsString( validFileExtensionsText.getText() );

			entity.setVisibleCondition( visibleConditionGroup.getCondition() );
			entity.setVisibleConditionDescription( visibleConditionGroup.getDescription() );

			entity.setRequiredCondition( requiredConditionGroup.getCondition() );
			entity.setRequiredConditionDescription( requiredConditionGroup.getDescription() );
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
			renderText.setEnabled(!fixedStructure);
		}
		requiredConditionGroup.setEnabled(!fixedStructure);
	}

}
