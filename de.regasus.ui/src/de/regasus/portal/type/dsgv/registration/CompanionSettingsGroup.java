package de.regasus.portal.type.dsgv.registration;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.combo.PortalCompanionParticipantTypeCombo;

public class CompanionSettingsGroup extends EntityGroup<DsgvRegistrationPortalConfig> {

	private final int COL_COUNT = 2;

	private Long portalId;
	private List<Language> languageList;


	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner companionCountSpinner;
	private Button companionParticipantTypeEqualToMainParticipantButton;
	private PortalCompanionParticipantTypeCombo companionDefaultParticipantTypeCombo;
	private Button companionWithProgrammeBookingsButton;
	private I18NComposite<DsgvRegistrationPortalConfig> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public CompanionSettingsGroup(Composite parent, int style, Portal portal)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(
			parent,
			style,
			Objects.requireNonNull(portal)
		);

		setText( ParticipantLabel.Companions.getString() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Portal portal = (Portal) initValues[0];

		this.portalId = portal.getId();

		// determine Portal languages
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		GridDataFactory checkGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER);


		// Row 1
		Label companionCountLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(companionCountLabel);
		companionCountLabel.setText(DsgvRegistrationPortalI18N.CompanionCount);

		companionCountSpinner = new NullableSpinner(parent, SWT.NONE);
		WidgetSizer.setWidth(companionCountSpinner);
		companionCountSpinner.setMinimum(0);
		companionCountSpinner.setMaximum(100);
		companionCountSpinner.addModifyListener(e -> refreshState());
		companionCountSpinner.addModifyListener(modifySupport);

		// Row 2
		GridDataFactory.fillDefaults().span(2,  1).applyTo( new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL) );

		new Label(parent, SWT.NONE); // placeholder

		companionParticipantTypeEqualToMainParticipantButton = new Button(parent, SWT.CHECK);
		checkGridDataFactory.applyTo(companionParticipantTypeEqualToMainParticipantButton);
		companionParticipantTypeEqualToMainParticipantButton.setText(DsgvRegistrationPortalI18N.CompanionParticipantTypeEqualToMainParticipant);
		companionParticipantTypeEqualToMainParticipantButton.addListener(SWT.Selection, e -> refreshState());
		companionParticipantTypeEqualToMainParticipantButton.addSelectionListener(modifySupport);

		// Row 3
		Label companionDefaultParticipantTypeLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(companionDefaultParticipantTypeLabel);
		companionDefaultParticipantTypeLabel.setText(DsgvRegistrationPortalI18N.CompanionDefaultParticipantType);

		companionDefaultParticipantTypeCombo = new PortalCompanionParticipantTypeCombo(parent, SWT.NONE);
		widgetGridDataFactory.applyTo(companionDefaultParticipantTypeCombo);
		companionDefaultParticipantTypeCombo.addModifyListener(modifySupport);

		// Row 4
		GridDataFactory.fillDefaults().span(2, 1).applyTo( new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL) );

		new Label(parent, SWT.NONE); // placeholder

		companionWithProgrammeBookingsButton = new Button(parent, SWT.CHECK);
		checkGridDataFactory.applyTo(companionWithProgrammeBookingsButton);
		companionWithProgrammeBookingsButton.setText(DsgvRegistrationPortalI18N.CompanionWithProgrammeBookings);
		companionWithProgrammeBookingsButton.addSelectionListener(modifySupport);
		companionWithProgrammeBookingsButton.addListener(SWT.Selection, e -> refreshState());

		// Row 5
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new CompanionSettingsGroupI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).indent(0, 20).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	private void refreshState() {
		// companionCountSpinner
		if (companionCountSpinner.getValue() > 0) {
			companionParticipantTypeEqualToMainParticipantButton.setEnabled(true);

			if ( companionParticipantTypeEqualToMainParticipantButton.getSelection() ) {
				companionDefaultParticipantTypeCombo.setEnabled(false);
				companionDefaultParticipantTypeCombo.setParticipantTypePK(null);
			}
			else {
				companionDefaultParticipantTypeCombo.setEnabled(true);
			}

			companionWithProgrammeBookingsButton.setEnabled(true);

			if (companionWithProgrammeBookingsButton.getSelection()) {
				i18nComposite.setEnabled(true);
			}
			else {
				i18nComposite.setEnabled(false);
			}
		}
		else {
			companionParticipantTypeEqualToMainParticipantButton.setEnabled(false);
			companionDefaultParticipantTypeCombo.setEnabled(false);
			companionWithProgrammeBookingsButton.setEnabled(false);
			i18nComposite.setEnabled(false);
		}
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		companionCountSpinner.setValue( entity.getCompanionCount() );
		companionParticipantTypeEqualToMainParticipantButton.setSelection(
			entity.isCompanionParticipantTypeEqualToMainParticipant()
		);

		companionDefaultParticipantTypeCombo.setPortalId(portalId);
		companionDefaultParticipantTypeCombo.setParticipantTypePK(
			entity.getCompanionDefaultParticipantTypeId()
		);

		companionWithProgrammeBookingsButton.setSelection(
			entity.isCompanionWithProgrammeBookings()
		);

		i18nComposite.setEntity(entity);

		refreshState();
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setCompanionCount( companionCountSpinner.getValueAsInteger() );
			entity.setCompanionParticipantTypeEqualToMainParticipant(
				companionParticipantTypeEqualToMainParticipantButton.getSelection()
			);
			entity.setCompanionDefaultParticipantTypeId(
				companionDefaultParticipantTypeCombo.getParticipantTypePK()
			);
			entity.setCompanionWithProgrammeBookings(
				companionWithProgrammeBookingsButton.getSelection()
			);
			i18nComposite.syncEntityToWidgets();
		}
	}

}
