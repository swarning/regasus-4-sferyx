package de.regasus.event.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.ui.Activator;

public class EventCustomFieldsComposite extends LazyComposite {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	/*** Widgets ***/
	private ScrolledComposite scrolledComposite;

	private I18NMultiText i18nMultiText;
	private final String[] LABELS = {
		ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_1_name.getString(),
		ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_2_name.getString(),
		ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_3_name.getString()
	};

	private ParticipantCustomFieldGroupLocationGroup customFieldLocationGroup;

	private SimpleCustomFieldNamesGroup customFieldNamesGroup;

	private Composite mainComposite;


	/**
	 * Create the customFieldNamesGroup.
	 * @param parent
	 * @param style
	 */
	public EventCustomFieldsComposite(Composite parent, int style) {
		super(parent, style);
	}


	/**
	 * Create the composite. It shows scroll bars when the space is not enough
	 * for all the custom fields.
	 *
	 * EventCustomFieldsComposite
	 *  	scrolledComposite
	 *  		mainComposite
	 *  			...
	 *  			customFieldNamesGroup
	 */
	@Override
	protected void createPartControl() throws Exception {
		setLayout(new FillLayout());

		scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		mainComposite = new Composite(scrolledComposite, SWT.NONE);
		mainComposite.setLayout(new GridLayout());

		Label locationLabel = new Label(mainComposite, SWT.NONE);
		locationLabel.setText(I18N.EventEditor_EventCustomFieldsComposite_locationLabel);
		locationLabel.setToolTipText(I18N.EventEditor_EventCustomFieldsComposite_locationLabel_tooltip);
		locationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		i18nMultiText = new I18NMultiText(
			mainComposite,							// parent
			SWT.NONE,								// style
			LABELS,									// labels
			new boolean[] {false, false, false},	// multiLine
			new boolean[] {false, false, false},	// required
			LanguageProvider.getInstance()			// languageProvider
		);
		i18nMultiText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		i18nMultiText.setToolTips(new String[] {
			ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_1_tooltip.getString(),
			ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_2_tooltip.getString(),
			ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_3_tooltip.getString()
		});



		customFieldLocationGroup = new ParticipantCustomFieldGroupLocationGroup(mainComposite, SWT.NONE);
		customFieldLocationGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		customFieldLocationGroup.setText(ParticipantLabel.Event_CustomFieldLocation.getString());


		// Container for textComposite (with Text widgets and the moreButton
		customFieldNamesGroup = new SimpleCustomFieldNamesGroup(mainComposite, SWT.NONE);
		customFieldNamesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		customFieldNamesGroup.addMoreButtonSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ensureAllFieldsVisible();

				// scroll down to show last widget
				scrolledComposite.showControl(customFieldNamesGroup.getLastControl());
			}
		});


		scrolledComposite.setContent(mainComposite);
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));


		// set data
		setEventVO(eventVO);

		// observe widgets
		i18nMultiText.addModifyListener(modifySupport);
		customFieldLocationGroup.addModifyListener(modifySupport);
		customFieldNamesGroup.addModifyListener(modifySupport);
	}


	private boolean widgetsInitialized() {
		return customFieldNamesGroup != null;
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


	private void syncWidgetsToEntity() {
		if (eventVO != null && widgetsInitialized()) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {

						Map<String, LanguageString> labelsToLanguagesMap = new HashMap<>();
						labelsToLanguagesMap.put(LABELS[0], eventVO.getCustomFieldTabName1());
						labelsToLanguagesMap.put(LABELS[1], eventVO.getCustomFieldTabName2());
						labelsToLanguagesMap.put(LABELS[2], eventVO.getCustomFieldTabName3());
						i18nMultiText.setLanguageString(labelsToLanguagesMap, eventVO.getLanguages());

						customFieldLocationGroup.setValue(eventVO.getCustomFieldLocation());

						customFieldNamesGroup.setEventVO(eventVO);

						// MIRCP-2357 - All fields need to be made visible event at the beginning,
						// not only when new fields are added
						ensureAllFieldsVisible();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null && widgetsInitialized()) {
			eventVO.setCustomFieldTabName1(i18nMultiText.getLanguageString(LABELS[0]));
			eventVO.setCustomFieldTabName2(i18nMultiText.getLanguageString(LABELS[1]));
			eventVO.setCustomFieldTabName3(i18nMultiText.getLanguageString(LABELS[2]));

			eventVO.setCustomFieldLocation(customFieldLocationGroup.getValue());

			customFieldNamesGroup.syncEntityToWidgets();
		}
	}


	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	public void selectParticipantCustomFieldGroupLocation(ParticipantCustomFieldGroupLocation location) {
		if (location == ParticipantCustomFieldGroupLocation.TAB_1) {
			i18nMultiText.setFocus();
		}
	}


	private void ensureAllFieldsVisible() {
		Point size = mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinWidth(size.x);
		scrolledComposite.setMinHeight(size.y);

		scrolledComposite.layout();
		mainComposite.layout();
	}

}
