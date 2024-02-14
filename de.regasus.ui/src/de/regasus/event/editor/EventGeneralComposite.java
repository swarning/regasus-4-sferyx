package de.regasus.event.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.TypeHelper.toLocalDate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.EventConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.geo.GeoDataGroup;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.common.CommonI18N;
import de.regasus.common.country.combo.CountryCombo;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.finance.customeraccount.combo.CustomerAccountCombo;
import de.regasus.ui.Activator;

public class EventGeneralComposite extends Composite {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private I18NMultiText i18nMultiText;

	private Text mnemonicText;
	private Text externalIdText;

	private DateComposite startTime;
	private DateComposite endTime;

	private Text cityText;
	private Text locationText;

	private CountryCombo countryCombo;
	private CountryCombo organisationOfficeCountryCombo;

	private GeoDataGroup geoDataGroup;

	private LanguageCombo languageCombo;

	private CustomerAccountCombo customerAccountCombo;
	private Text eventNoText;

	private NullableSpinner expectedNoParticipantsSpinner;

	private MultiLineText noteText;

	private DigitalEventGroup digitalEventGroup;
	private EventNextNumbersGroup nextNumbersGroup;
	private EventBadgeGroup badgeGroup;
	private EventStreamGroup streamGroup;

	// *
	// * Widgets
	// **************************************************************************


	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public EventGeneralComposite(Composite parent, int style, ConfigParameterSet configParameterSet) throws Exception {
		super(parent, style);

		createWidgets( configParameterSet.getEvent() );
	}


	private void createWidgets(EventConfigParameterSet eventConfigParameterSet) throws Exception {
		/* layout with 4 columns
		 */
		final int COL_COUNT = 4;
		setLayout(new GridLayout(COL_COUNT, false));

		/* Row 1: I18N-Texts: label, name
		 */
		{
			String[] labels = {
				ParticipantLabel.Event_Name.getString(),
				ParticipantLabel.Event_Label.getString()
			};
			i18nMultiText = new I18NMultiText(
				this,							// parent
				SWT.NONE,						// style
				labels,							// labels
				new boolean[] {true, false},	// multiLine
				new boolean[] {true, true},		// required
				LanguageProvider.getInstance()	// languageProvider
			);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, COL_COUNT, 1);
			// do NOT set gridData.heightHint cause this disables dynamic height
			i18nMultiText.setLayoutData(gridData);

			i18nMultiText.addModifyListener(modifySupport);
		}

		/* Row 2: mnemonic, externalId
		 */
		{
			// mnemonic
			Label mnemonicLabel = new Label(this, SWT.NONE);
			mnemonicLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			mnemonicLabel.setText(ParticipantLabel.Event_Mnemonic.getString());
			SWTHelper.makeBold(mnemonicLabel);

			mnemonicText = new Text(this, SWT.BORDER);
			mnemonicText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			SWTHelper.makeBold(mnemonicText);

			mnemonicText.addModifyListener(modifySupport);


			// externalId
			if ( eventConfigParameterSet.getExternalId().isVisible() ) {
    			Label externalIdLabel = new Label(this, SWT.NONE);
    			externalIdLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    			externalIdLabel.setText(CommonI18N.ExternalID.getString());

    			externalIdText = new Text(this, SWT.BORDER);
    			externalIdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    			externalIdText.addModifyListener(modifySupport);
			}
			else {
				SWTHelper.fillGridLayout(COL_COUNT - 2, this);
			}
		}

		/* Row 3: startTime / endTime
		 */
		{
			Label startTimeLabel = new Label(this, SWT.NONE);
			startTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			startTimeLabel.setText(ParticipantLabel.Event_StartTime.getString());
			SWTHelper.makeBold(startTimeLabel);

			// startTime widget
			startTime = new DateComposite(this, SWT.BORDER, true /*required*/);
			startTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
//			WidgetSizer.setWidth(startTime);
			startTime.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Date startTimeValue = startTime.getDate();
					if (startTimeValue != null && endTime.getLocalDate() == null) {
						endTime.setLocalDate( TypeHelper.toLocalDate(startTimeValue) );
					}
				}
			});

			startTime.addModifyListener(modifySupport);

			// endTime Label
			Label endTimeLabel = new Label(this, SWT.NONE);
			endTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			endTimeLabel.setText(ParticipantLabel.Event_EndTime.getString());
			SWTHelper.makeBold(endTimeLabel);

			// endTime widget
			endTime = new DateComposite(this, SWT.BORDER, true /*required*/);
			endTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
//			WidgetSizer.setWidth(endTime);
			SWTHelper.makeBold(endTime);

			endTime.addModifyListener(modifySupport);
		}

		/* Row 4: city & location
		 */
		{
			// city
			Label cityLabel = new Label(this, SWT.NONE);
			cityLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			cityLabel.setText(ParticipantLabel.Event_City.getString());

			cityText = new Text(this, SWT.BORDER);
			cityText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			cityText.addModifyListener(modifySupport);

			Label locationLabel = new Label(this, SWT.NONE);
			locationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			locationLabel.setText(ParticipantLabel.Event_Location.getString());

			locationText = new Text(this, SWT.BORDER);
			locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			locationText.addModifyListener(modifySupport);
		}

		/* Row 5: Countries
		 */
		{
    		Label countryLabel = new Label(this, SWT.NONE);
    		countryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    		countryLabel.setText(ParticipantLabel.Event_Country.getString());

    		// country
    		countryCombo = new CountryCombo(this, SWT.NONE);
    		countryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    		countryCombo.addModifyListener(modifySupport);


    		// organisationOfficeCountry
    		Label organisationOfficeCountryLabel = new Label(this, SWT.NONE);
    		organisationOfficeCountryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    		organisationOfficeCountryLabel.setText(ParticipantLabel.Event_OrganisationOfficeCountry.getString());

    		organisationOfficeCountryCombo = new CountryCombo(this, SWT.NONE);
    		organisationOfficeCountryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    		organisationOfficeCountryCombo.addModifyListener(modifySupport);
		}

		/* Row 6: Geo Data
		 */
		if ( eventConfigParameterSet.getGeoData().isVisible() ) {
			geoDataGroup = new GeoDataGroup(this, SWT.NONE);
			geoDataGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COL_COUNT, 1));
			geoDataGroup.setText( CommonI18N.GeoData.getString() );

			geoDataGroup.addModifyListener(modifySupport);

		}

		/* Row 7: language
		 */
		{
    		Label languagesLabel = new Label(this, SWT.NONE);
    		languagesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    		languagesLabel.setText(ParticipantLabel.Event_Language.getString());

    		languageCombo = new LanguageCombo(this, SWT.NONE);
    		languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    		languageCombo.addModifyListener(modifySupport);

    		SWTHelper.fillGridLayout(COL_COUNT - 2, this);
		}

		/* Row 8: customerAccount & eventNo
		 */
		{
			// customerAccount
			Label customerAccountLabel = new Label(this, SWT.NONE);
			customerAccountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			customerAccountLabel.setText(InvoiceLabel.CustomerAccountNo.getString());

			customerAccountCombo = new CustomerAccountCombo(this, SWT.NONE);
			customerAccountCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			customerAccountCombo.addModifyListener(modifySupport);


			// eventNo
			Label eventNoLabel = new Label(this, SWT.NONE);
			eventNoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			eventNoLabel.setText(ParticipantLabel.Event_EventNo.getString());

			eventNoText = new Text(this, SWT.BORDER);
			eventNoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			eventNoText.addModifyListener(modifySupport);

			// fill-up last columns
//			SWTHelper.fillGridLayout(COL_COUNT - 2, this);
		}

		/* Row 9: expectedNoParticipants
		 */
		{
			Label expectedNoParticipantsLabel = new Label(this, SWT.NONE);
			expectedNoParticipantsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			expectedNoParticipantsLabel.setText(ParticipantLabel.Event_ExpectedNoParticipants.getString());

			expectedNoParticipantsSpinner = new NullableSpinner(this, SWT.NONE);
			expectedNoParticipantsSpinner.setMinimum(0);
			expectedNoParticipantsSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			WidgetSizer.setWidth(expectedNoParticipantsSpinner);

			expectedNoParticipantsSpinner.addModifyListener(modifySupport);

			// fill-up last columns
			SWTHelper.fillGridLayout(COL_COUNT - 2, this);
		}

		/* Row 10: note
		 */
		{
			Label noteLabel = new Label(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				noteLabel.setLayoutData(gridData);
			}
			noteLabel.setText(ParticipantLabel.Event_Note.getString());

			noteText = new MultiLineText(this, SWT.BORDER);
			noteText.setMinLineCount(2);
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, COL_COUNT - 1, 1);
			noteText.setLayoutData(gridData);

			noteText.addModifyListener(modifySupport);
		}


		// horizontal gap
		new Label(this, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, COL_COUNT, 1));


		/* Row 11: Digital Event
		 */
		new Label(this, SWT.NONE);
		digitalEventGroup = new DigitalEventGroup(this, SWT.NONE, eventConfigParameterSet);
		digitalEventGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, COL_COUNT - 1, 1));
		digitalEventGroup.addModifyListener(modifySupport);


		/* Row 12: next numbers & badge
		 */
		{
    		new Label(this, SWT.NONE);

    		nextNumbersGroup = new EventNextNumbersGroup(this, SWT.NONE);
    		nextNumbersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    		badgeGroup = new EventBadgeGroup(this, SWT.NONE);
    		badgeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    		badgeGroup.addModifyListener(modifySupport);
		}


		/* Row 13: Stream
		 */
		{
    		// left side empty
    		new Label(this, SWT.NONE);
    		new Label(this, SWT.NONE);

    		streamGroup = new EventStreamGroup(this, SWT.NONE);
    		streamGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    		streamGroup.addModifyListener(modifySupport);
		}
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Map<String, LanguageString> labelToLanguageMap = new HashMap<>();
						labelToLanguageMap.put(ParticipantLabel.Event_Label.getString(), eventVO.getLabel());
						labelToLanguageMap.put(ParticipantLabel.Event_Name.getString(), eventVO.getName());
						i18nMultiText.setLanguageString(labelToLanguageMap, null /*defaultLanguagePKs*/);

						mnemonicText.setText( avoidNull(eventVO.getMnemonic()) );
						if (externalIdText != null) {
							externalIdText.setText( avoidNull(eventVO.getExternalId()) );
						}

						startTime.setLocalDate( toLocalDate(eventVO.getStartTime()) );
						endTime.setLocalDate( toLocalDate(eventVO.getEndTime()) );

						cityText.setText( avoidNull(eventVO.getCity()) );
						locationText.setText( avoidNull(eventVO.getLocation()) );

						countryCombo.setCountryCode( eventVO.getCountryPK() );
						organisationOfficeCountryCombo.setCountryCode( eventVO.getOrganisationOfficeCountryPK() );

						if (geoDataGroup != null) {
							geoDataGroup.setGeoData( eventVO.getGeoData() );
						}

						languageCombo.setLanguageCode( eventVO.getLanguage() );

						customerAccountCombo.setCustomerAccountPK( eventVO.getCustomerAccountNo() );
						eventNoText.setText(StringHelper.avoidNull( eventVO.getEventNo()) );

						expectedNoParticipantsSpinner.setValue( eventVO.getExpectedNumberParticipants() );

						noteText.setText( avoidNull(eventVO.getNote()) );

						digitalEventGroup.setEvent(eventVO);
						nextNumbersGroup.setEvent(eventVO);
						badgeGroup.setEvent(eventVO);
						streamGroup.setEvent(eventVO);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			eventVO.setName( i18nMultiText.getLanguageString(ParticipantLabel.Event_Name.getString()) );
			eventVO.setLabel( i18nMultiText.getLanguageString(ParticipantLabel.Event_Label.getString()) );

			eventVO.setMnemonic( mnemonicText.getText() );
			if (externalIdText != null) {
				eventVO.setExternalId( externalIdText.getText() );
			}

			eventVO.setBeginDate( startTime.getI18NDate() );
			eventVO.setEndDate( endTime.getI18NDate() );

			eventVO.setCity( cityText.getText() );
			eventVO.setLocation( locationText.getText() );

			eventVO.setCountryPK( countryCombo.getCountryCode() );
			eventVO.setOrganisationOfficeCountryPK( organisationOfficeCountryCombo.getCountryCode() );

			if (geoDataGroup != null) {
				eventVO.setGeoData( geoDataGroup.getGeoData() );
			}

			eventVO.setLanguage( languageCombo.getLanguageCode() );

			eventVO.setCustomerAccountNo( customerAccountCombo.getCustomerAccountNo() );
			eventVO.setEventNo( eventNoText.getText() );

			eventVO.setExpectedNumberParticipants( expectedNoParticipantsSpinner.getValueAsInteger() );

			eventVO.setNote( noteText.getText() );

			digitalEventGroup.syncEntityToWidgets();
			nextNumbersGroup.syncEntityToWidgets();
			badgeGroup.syncEntityToWidgets();
			streamGroup.syncEntityToWidgets();
		}
	}


	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
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

}
