package de.regasus.onlineform.editor;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.messeinfo.regasus.Rule;
import com.lambdalogic.messeinfo.regasus.RuleListStringConverter;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ListComposite;
import com.lambdalogic.util.rcp.ListCompositeController;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IUpDownListener;
import de.regasus.event.ParticipantType;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.ui.Activator;


public class RulesTabComposite
extends Composite
implements ListComposite<RuleComposite> {

	private final class RuleUpDownListener implements IUpDownListener {
		private final RuleComposite composite;


		private RuleUpDownListener(RuleComposite composite) {
			this.composite = composite;
		}


		@Override
		public void upPressed() {
			onUpPressed(composite);

		}


		@Override
		public void topPressed() {
			onTopPressed(composite);

		}


		@Override
		public void downPressed() {
			onDownPressed(composite);
		}


		@Override
		public void bottomPressed() {
			onBottomPressed(composite);
		}
	}


	private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

	// parent entity that contains the list of sub-entities managed by this Composite
	private RegistrationFormConfig registrationFormConfig;

	// support for ModifyEvents
	private ModifySupport modifySupport = new ModifySupport(this);

	// support for handling a List of sub-Composites
	private ListCompositeController<RuleComposite> compositeListSupport =
		new ListCompositeController<>(this);

	// ScrolledComposite to realize vertical scroll bars
	private ScrolledComposite scrollComposite;

	// parent Composite for sub-Composites
	private Composite contentComposite;

	private Button addButton;

	private Button showVarsButton;


	public RulesTabComposite(Composite parent, int style) {
		super(parent, style);

		createPartControl();
	}


	protected void createPartControl() {
		this.setLayout(new GridLayout(1, false));

		// make the folders contentComposite scrollable
		scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL  | SWT.H_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setShowFocusedControl(true);

		contentComposite = new Composite(scrollComposite, SWT.NONE);
		contentComposite.setLayout(new GridLayout(1, false));

		scrollComposite.setContent(contentComposite);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				refreshScrollbar();
			}
		});


		// horizontal line
		Label separatorLabel = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		buttonComposite.setLayout(new GridLayout(2, false));


		// Button to open dialog that shows variables
		showVarsButton = new Button(buttonComposite, SWT.PUSH);
		showVarsButton.setText(OnlineFormI18N.ShowVariables);
		showVarsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					showVariables();
				}
				catch (Exception e1) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
				}
			}
		});


		// Button to add new Composites
		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText(OnlineFormI18N.NewRule);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addItem();
			}
		});


		// calling syncWidgetsToEntity() is not necessary, because this is not a LazyComposite
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#createComposite()
	 */
	@Override
	public RuleComposite createComposite() {
		final RuleComposite composite = new RuleComposite(contentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		composite.addModifyListener(modifySupport);
		composite.addRemoveListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				compositeListSupport.removeComposite(composite);
			}
		});

		return composite;
	}


	private void addItem() {
		try {
			// create Composite
			final RuleComposite composite = compositeListSupport.addComposite();

			// add new Rule to Composite
			composite.setRule(new Rule());
			composite.setUpDownListener(new RuleUpDownListener(composite));

			// scroll to the end
			scrollComposite.setOrigin(0, Integer.MAX_VALUE);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void onTopPressed(RuleComposite clickedComposite) {
		List<RuleComposite> compositeList = compositeListSupport.getCompositeList();
		int idx = compositeList.indexOf(clickedComposite);
		if (idx > 0) {
			RuleComposite otherComposite = compositeList.get(0);
			exchangeRules(clickedComposite, otherComposite);
		}

	}


	protected void onBottomPressed(RuleComposite clickedComposite) {
		List<RuleComposite> compositeList = compositeListSupport.getCompositeList();
		int idx = compositeList.indexOf(clickedComposite);
		if (idx < compositeList.size() - 1) {
			RuleComposite otherComposite = compositeList.get(compositeList.size() - 1);
			exchangeRules(clickedComposite, otherComposite);
		}
	}


	protected void onUpPressed(RuleComposite clickedComposite) {
		List<RuleComposite> compositeList = compositeListSupport.getCompositeList();
		int idx = compositeList.indexOf(clickedComposite);
		if (idx > 0) {
			RuleComposite otherComposite = compositeList.get(idx - 1);
			exchangeRules(clickedComposite, otherComposite);
		}
	}

	protected void onDownPressed(RuleComposite clickedComposite) {
		List<RuleComposite> compositeList = compositeListSupport.getCompositeList();
		int idx = compositeList.indexOf(clickedComposite);
		if (idx < compositeList.size() - 1) {
			RuleComposite otherComposite = compositeList.get(idx + 1);
			exchangeRules(clickedComposite, otherComposite);
		}
	}

	private void exchangeRules(RuleComposite ruleComposite1, RuleComposite ruleComposite2) {
		Rule rule1 = ruleComposite1.getRule();
		ruleComposite1.setRule(ruleComposite2.getRule());
		ruleComposite2.setRule(rule1);
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


	/**
	 * Copy sub-entities from the sub-Composites to main entity.
	 */
	public void syncWidgetsToEntity() {
		if (registrationFormConfig != null && registrationFormConfig.getId() != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						// get sub-entity list
						String rulesString = registrationFormConfig.getBookingRules();
						List<Rule> subEntityList = RuleListStringConverter.fromString(rulesString);


						compositeListSupport.setSize(subEntityList.size());

						for (int i = 0; i < subEntityList.size(); i++) {
							// set sub-entity to sub-Composite
							RuleComposite ruleComposite = compositeListSupport.getComposite(i);
							ruleComposite.setRule( subEntityList.get(i) );
							ruleComposite.setUpDownListener(new RuleUpDownListener(ruleComposite));
						}
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


	/**
	 * Copy values from widgets of sub-Composites to sub-entities.
	 */
	public void syncEntityToWidgets() {
		List<Rule> ruleList = CollectionsHelper.createArrayList();
		for (RuleComposite subComposite : compositeListSupport.getCompositeList()) {
			Rule rule = subComposite.getRule();
			if (StringHelper.isNotEmpty(rule.getCondition())) {
				ruleList.add(rule);
			}
		}

		String rulesString = RuleListStringConverter.toString(ruleList);

		registrationFormConfig.setBookingRules(rulesString);
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#fireModifyEvent()
	 */
	@Override
	public void fireModifyEvent() {
		modifySupport.fire();
	}


	private void refreshScrollbar() {
		Rectangle clientArea = scrollComposite.getClientArea();
		scrollComposite.setMinSize(contentComposite.computeSize(clientArea.width, SWT.DEFAULT));
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#refreshLayout()
	 */
	@Override
	public void refreshLayout() {
		layout(true, true);
		refreshScrollbar();
	}


	public RegistrationFormConfig getRegistrationFormConfig() {
		return registrationFormConfig;
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig) {
		this.registrationFormConfig = registrationFormConfig;
		syncWidgetsToEntity();
	}


	private void showVariables() throws Exception {
		Long eventPK = registrationFormConfig.getEventPK();
		List<ProgrammePointVO> programmePointVOs = ProgrammePointModel.getInstance().getProgrammePointVOsByEventPK(eventPK);
		ProgrammePointTypeModel ppTypeModel = ProgrammePointTypeModel.getInstance();
		Collection<ProgrammePointTypeVO> programmePointTypeVOs = ppTypeModel.getAllUndeletedProgrammePointTypeVOs();
		HashMap<Long, ProgrammePointTypeVO> programmePointTypePK2TypeMap = AbstractVO.abstractVOs2Map(programmePointTypeVOs);
		HashMap<Long, StringBuilder> programmePointTypePK2VarsMap = new HashMap<>();

		StringBuilder sbTotal = new StringBuilder();

		ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();
		List<ParticipantType> typesByEventPK = participantTypeModel.getParticipantTypesByEvent(eventPK);

		for (ParticipantType participantType : typesByEventPK) {
			sbTotal.append("pt" + participantType.getId() + " (" + participantType.getName().getString() + ")\n");
		}
		sbTotal.append("\n");


		for (ProgrammePointVO programmePointVO : programmePointVOs) {

			StringBuilder sb = new StringBuilder();
			sb.append(" - pp" + programmePointVO.getPK() + "      (" +  programmePointVO.getName().getString() + ")\n");
			sb.append(" - pp" + programmePointVO.getPK() + "count \n");

			Long typePK = programmePointVO.getTypePK();
			if (typePK == null) {
				continue;
			}
			List<ProgrammeOfferingVO> offeringVOs = ProgrammeOfferingModel.getInstance().getProgrammeOfferingVOsByProgrammePointPK(programmePointVO.getID());
			for (ProgrammeOfferingVO programmeOfferingVO : offeringVOs) {
				sb.append("    - po" + programmeOfferingVO.getPK() + "      ("+  getDescription(programmeOfferingVO) + ")\n");
				sb.append("    - po" + programmeOfferingVO.getPK() + "count \n");
			}
			StringBuilder sbtype = programmePointTypePK2VarsMap.get(typePK);
			if (sbtype == null) {
				sbtype = new StringBuilder();
				programmePointTypePK2VarsMap.put(typePK, sbtype);
				ProgrammePointTypeVO programmePointTypeVO = programmePointTypePK2TypeMap.get(typePK);
				if (programmePointTypeVO == null) {
					/*
					 * programmePointTypeVO == null means the ProgrammePointType is soft-deleted but still used in
					 * a ProgrammePoint, so we need to load the ProgrammePointType explicit by its ID.
					 */
					programmePointTypeVO = ppTypeModel.getProgrammePointTypeVO(typePK);
				}

				if (programmePointTypeVO != null) {
					sbtype.append("ppt" + typePK + "      (" + programmePointTypeVO.getName().getString() + ")\n");
					sbtype.append("ppt" + typePK + "count \n");
				}
			}
			sbtype.append(sb.toString());

		}

		for (ProgrammePointTypeVO programmePointTypeVO : programmePointTypeVOs) {
			StringBuilder sb = programmePointTypePK2VarsMap.get(programmePointTypeVO.getID());
			if (sb != null) {
				sbTotal.append(sb.toString());
			}
		}

		Shell shell = new Shell(getShell(), SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		shell.setText(OnlineFormI18N.ShowVariables);
		Text text = new Text(shell, SWT.READ_ONLY | SWT.V_SCROLL);
		text.setText(sbTotal.toString());
		shell.pack();
		shell.open();

	}


	private String getDescription(ProgrammeOfferingVO value) throws Exception {
		StringBuilder sb = new StringBuilder();
		// Falls vorhanden, Beschreibung in der aktuellen Sprache anhängen
		LanguageString description = value.getDescription();
		if (description != null && !description.isEmpty()) {
			String s = description.getString();
			s = StringHelper.trim(s);
			try {
				if (s.length() > 10) {
					s = s.substring(0, 10) + "...";
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
			sb.append(s);
		}

		// Falls vorhanden, Teilnehmerart in der aktuellen Sprache anhängen
		Long participantTypePK = value.getParticipantTypePK();
		if (participantTypePK != null) {
			ParticipantType participantType = ParticipantTypeModel.getInstance().getParticipantType(participantTypePK);
			if (participantType != null) {
				String name = participantType.getName().getString();
				StringHelper.appendIfNeeded(sb, ", ");
				sb.append(name);
			}
		}

		// Betrag, wenn vorhanden
		PriceVO priceVO = value.getMainPriceVO();
		if (priceVO != null) {
			CurrencyAmount currencyAmount = priceVO.getCurrencyAmountGross();
			String currency = currencyAmount.format(false, true);
			StringHelper.appendIfNeeded(sb, ", ");
			sb.append(currency);
		}

		// Start- und Enddatum, wenn vorhanden
		String dates = null;
		Date startTime = value.getStartTime();
		if (startTime != null) {
			dates = dateFormat.format(startTime);
			dates += "...";
		}

		Date endTime = value.getEndTime();
		if (endTime != null) {
			if (dates == null) {
				dates = "...";
			}
			dates += dateFormat.format(endTime);
		}
		if (dates != null) {
			StringHelper.appendIfNeeded(sb, ", ");
			sb.append(dates);
		}

		return sb.toString();
	}

}
