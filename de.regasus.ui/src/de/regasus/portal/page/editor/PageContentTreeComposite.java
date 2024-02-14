package de.regasus.portal.page.editor;


import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IUpDownListener;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.UpDownComposite;
import de.regasus.core.ui.dnd.CopyPasteButtonComposite;
import de.regasus.portal.IdProvider;
import de.regasus.portal.Page;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.PortalType;
import de.regasus.portal.Section;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.DigitalEventComponent;
import de.regasus.portal.component.EmailComponent;
import de.regasus.portal.component.FileComponent;
import de.regasus.portal.component.OpenAmountComponent;
import de.regasus.portal.component.ParticipantFieldComponent;
import de.regasus.portal.component.PaymentComponent;
import de.regasus.portal.component.PaymentWithFeeComponent;
import de.regasus.portal.component.PrintComponent;
import de.regasus.portal.component.ProfileFieldComponent;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.component.ScriptComponent;
import de.regasus.portal.component.SendLetterOfInvitationComponent;
import de.regasus.portal.component.StreamComponent;
import de.regasus.portal.component.SummaryComponent;
import de.regasus.portal.component.TextComponent;
import de.regasus.portal.component.TotalAmountComponent;
import de.regasus.portal.component.UploadComponent;
import de.regasus.portal.component.group.GroupMemberTableComponent;
import de.regasus.portal.component.hotel.EditBookingComponent;
import de.regasus.portal.component.hotel.HotelBookingComponent;
import de.regasus.portal.component.hotel.HotelDetailsComponent;
import de.regasus.portal.component.hotel.HotelSearchCriteriaComponent;
import de.regasus.portal.component.hotel.HotelSearchFilterComponent;
import de.regasus.portal.component.hotel.HotelSearchResultComponent;
import de.regasus.portal.component.hotel.HotelTotalAmountComponent;
import de.regasus.portal.component.hotelspeaker.SpeakerArrivalDepartureComponent;
import de.regasus.portal.component.hotelspeaker.SpeakerRoomTypeComponent;
import de.regasus.portal.component.membership.ecfs.EcfsMembershipComponent;
import de.regasus.portal.component.membership.efic.EficMembershipComponent;
import de.regasus.portal.component.membership.enets.EnetsMembershipComponent;
import de.regasus.portal.component.membership.esicm.EsicmMembershipComponent;
import de.regasus.portal.component.membership.esra.EsraMembershipComponent;
import de.regasus.portal.component.membership.esska.EsskaMembershipComponent;
import de.regasus.portal.component.membership.fens.FensMembershipComponent;
import de.regasus.portal.component.membership.ifla.IflaMembershipComponent;
import de.regasus.portal.component.membership.ilts.IltsMembershipComponent;
import de.regasus.portal.component.membership.ispad.IspadMembershipComponent;
import de.regasus.portal.component.membership.neurosciences.NeurosciencesMembershipComponent;
import de.regasus.portal.component.profile.PortalTableComponent;
import de.regasus.portal.component.react.certificate.CertificateComponent;
import de.regasus.portal.page.editor.action.CopyAction;
import de.regasus.portal.page.editor.action.CreateCertificateComponentAction;
import de.regasus.portal.page.editor.action.CreateDigitalEventComponentAction;
import de.regasus.portal.page.editor.action.CreateEditBookingComponentAction;
import de.regasus.portal.page.editor.action.CreateEmailComponentAction;
import de.regasus.portal.page.editor.action.CreateFileComponentAction;
import de.regasus.portal.page.editor.action.CreateHotelBookingComponentAction;
import de.regasus.portal.page.editor.action.CreateHotelDetailsComponentAction;
import de.regasus.portal.page.editor.action.CreateHotelSearchCriteriaComponentAction;
import de.regasus.portal.page.editor.action.CreateHotelSearchFilterComponentAction;
import de.regasus.portal.page.editor.action.CreateHotelSearchResultComponentAction;
import de.regasus.portal.page.editor.action.CreateHotelTotalAmountComponentAction;
import de.regasus.portal.page.editor.action.CreateOpenAmountComponentAction;
import de.regasus.portal.page.editor.action.CreateParticipantFieldComponentAction;
import de.regasus.portal.page.editor.action.CreatePaymentComponentAction;
import de.regasus.portal.page.editor.action.CreatePaymentWithFeeComponentAction;
import de.regasus.portal.page.editor.action.CreatePrintComponentAction;
import de.regasus.portal.page.editor.action.CreateProfileFieldComponentAction;
import de.regasus.portal.page.editor.action.CreateProgrammeBookingComponentAction;
import de.regasus.portal.page.editor.action.CreateScriptComponentAction;
import de.regasus.portal.page.editor.action.CreateSectionAction;
import de.regasus.portal.page.editor.action.CreateSendLetterOfInvitationComponentAction;
import de.regasus.portal.page.editor.action.CreateSpeakerArrivalDepartureComponentAction;
import de.regasus.portal.page.editor.action.CreateSpeakerRoomTypeComponentAction;
import de.regasus.portal.page.editor.action.CreateStreamComponentAction;
import de.regasus.portal.page.editor.action.CreateSummaryComponentAction;
import de.regasus.portal.page.editor.action.CreateTextComponentAction;
import de.regasus.portal.page.editor.action.CreateTotalAmountComponentAction;
import de.regasus.portal.page.editor.action.CreateUploadComponentAction;
import de.regasus.portal.page.editor.action.DeleteAction;
import de.regasus.portal.page.editor.action.PasteAction;
import de.regasus.portal.page.editor.dnd.IdProviderTransfer;
import de.regasus.portal.page.editor.group.CreateGroupMemberTableComponentAction;
import de.regasus.portal.page.editor.membership.ecfs.CreateEcfsMembershipComponentAction;
import de.regasus.portal.page.editor.membership.efic.CreateEficMembershipComponentAction;
import de.regasus.portal.page.editor.membership.enets.CreateEnetsMembershipComponentAction;
import de.regasus.portal.page.editor.membership.esicm.CreateEsicmMembershipComponentAction;
import de.regasus.portal.page.editor.membership.esra.CreateEsraMembershipComponentAction;
import de.regasus.portal.page.editor.membership.esska.CreateEsskaMembershipComponentAction;
import de.regasus.portal.page.editor.membership.fens.CreateFensMembershipComponentAction;
import de.regasus.portal.page.editor.membership.ifla.CreateIflaMembershipComponentAction;
import de.regasus.portal.page.editor.membership.ilts.CreateIltsMembershipComponentAction;
import de.regasus.portal.page.editor.membership.ispad.CreateIspadMembershipComponentAction;
import de.regasus.portal.page.editor.membership.neurosciences.CreateNeurosciencesMembershipComponentAction;
import de.regasus.portal.page.editor.profile.CreatePortalTableComponentAction;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalPageConfig;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;
import de.regasus.util.XmlHelper;


public class PageContentTreeComposite extends Composite {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private boolean isAdmin;

	private boolean isScriptComponentVisible;

	// the entity
	private Page page;
	private PortalType portalType;
	private List<Class<? extends Component>> availableComponentClassList;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private TreeViewer treeViewer;
	private PageContentTreeContentProvider contentProvider;

	private GridDataFactory buttonGridDataFactory;
	private Button addButton;
	private Button removeButton;
	private UpDownComposite upDownComposite;
	private CopyPasteButtonComposite copyPasteButtonComposite;

	// *
	// * Widgets
	// **************************************************************************

	private CreateSectionAction createSectionAction;

	private CreateScriptComponentAction createScriptComponentAction;
	private CreateTextComponentAction createTextComponentAction;
	private CreateProfileFieldComponentAction createProfileFieldComponentAction;
	private CreateParticipantFieldComponentAction createParticipantFieldComponentAction;
	private CreateEmailComponentAction createEmailComponentAction;
	private CreateFileComponentAction createFileComponentAction;
	private CreateUploadComponentAction createUploadComponentAction;
	private CreateProgrammeBookingComponentAction createProgrammeBookingComponentAction;
	private CreateOpenAmountComponentAction createOpenAmountComponentAction;
	private CreateTotalAmountComponentAction createTotalAmountComponentAction;
	private CreateStreamComponentAction createStreamComponentAction;
	private CreatePaymentComponentAction createPaymentComponentAction;
	private CreatePaymentWithFeeComponentAction createPaymentWithFeeComponentAction;
	private CreateDigitalEventComponentAction createDigitalEventComponentAction;
	private CreatePrintComponentAction createPrintComponentAction;
	private CreateSendLetterOfInvitationComponentAction createSendLetterOfInvitationComponentAction;
	private CreateSummaryComponentAction createSummaryComponentAction;

	// Actions of Components of the Group Portal
	private CreateGroupMemberTableComponentAction createGroupMemberTableComponentAction;

	// Actions of Components of the Hotel Portal
	private CreateHotelSearchCriteriaComponentAction createHotelSearchCriteriaComponentAction;
	private CreateHotelSearchFilterComponentAction createHotelSearchFilterComponentAction;
	private CreateHotelSearchResultComponentAction createHotelSearchResultComponentAction;
	private CreateHotelDetailsComponentAction createHotelDetailsComponentAction;
	private CreateHotelBookingComponentAction createHotelBookingComponentAction;
	private CreateEditBookingComponentAction createEditBookingComponentAction;
	private CreateHotelTotalAmountComponentAction createHotelTotalAmountComponentAction;
	
	// Actions of Components of the Hotel Speaker Portal
	private CreateSpeakerArrivalDepartureComponentAction createSpeakerArrivalDepartureComponentAction;
	private CreateSpeakerRoomTypeComponentAction createSpeakerRoomTypeComponentAction;

	// Actions of Components of the Profile Portals
	private CreatePortalTableComponentAction createPortalTableComponentAction;

	// Actions of Components of the Certificate Portal
	private CreateCertificateComponentAction createCertificateComponentAction;

	// Actions of Membership Components
	private CreateEcfsMembershipComponentAction createEcfsMembershipComponentAction;
	private CreateEficMembershipComponentAction createEficMembershipComponentAction;
	private CreateEnetsMembershipComponentAction createEnetsMembershipComponentAction;
	private CreateEsicmMembershipComponentAction createEsicmMembershipComponentAction;
	private CreateEsraMembershipComponentAction createEsraMembershipComponentAction;
	private CreateEsskaMembershipComponentAction createEsskaMembershipComponentAction;
	private CreateFensMembershipComponentAction createFensMembershipComponentAction;
	private CreateIflaMembershipComponentAction createIflaMembershipComponentAction;
	private CreateIltsMembershipComponentAction createIltsMembershipComponentAction;
	private CreateIspadMembershipComponentAction createIspadMembershipComponentAction;
	private CreateNeurosciencesMembershipComponentAction createNeurosciencesMembershipComponentAction;

	private DeleteAction deleteAction;
	private CopyAction copyAction;
	private PasteAction pasteAction;


	public PageContentTreeComposite(Composite parent, int style, Long portalId) throws Exception {
		super(parent, style);

		Portal portal = PortalModel.getInstance().getPortal(portalId);
		portalType = portal.getPortalType();
		availableComponentClassList = portalType.getComponentClassList();

		isAdmin = CurrentUserModel.getInstance().isAdmin();

		Long eventId = portal.getEventId();
		if (eventId == null) {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet();
			isScriptComponentVisible = configParameterSet.getPortal().isScriptComponent();
		}
		else {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
			isScriptComponentVisible = configParameterSet.getEvent().getPortal().isScriptComponent();
		}


		createWidgets();
	}


	private void createWidgets() throws Exception {
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		// column 1
		Composite treeComposite = createTreeComposite();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(treeComposite);

		// column 2
		Composite buttonComposite = createButtonComposite();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(false, true).applyTo(buttonComposite);

		createActions();
		hookContextMenu();

		// copy values of sub-entities to the widgets of sub-Composites
		// (in the case of lazy instantiation the entity is already there)
		syncWidgetsToEntity();

		updateButtonState();
	}


	private Composite createTreeComposite() {
		Composite composite = new Composite(this, SWT.NONE);

		// using a TreeColumnLayout and a TreeViewerColumn is the only way to set the column width to 100%
		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		composite.setLayout(treeColumnLayout);


		treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

		contentProvider = new PageContentTreeContentProvider();
		treeViewer.setContentProvider(contentProvider);

		treeViewer.addSelectionChangedListener(selectionChangedListener);
		treeViewer.getTree().addKeyListener(treeKeyListener);


//		treeViewer.getTree().setLinesVisible(true);

		// define tree column
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(100));

		// set ColumnLabelProvider that delegated to PageContentTreeLabelProvider
		treeViewerColumn.setLabelProvider( ColumnLabelProvider.createTextProvider(e -> new PageContentTreeLabelProvider().getText(e)) );


		return composite;
	}


	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateButtonState();
		}
	};


	private KeyListener treeKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent event) {
			log.debug("event: " + event);

			if (page != null && !page.isFixedStructure()) {
    			// run DeleteAction when user presses DEL
    			if (event.keyCode == SWT.DEL && event.stateMask == 0) {
    				deleteAction.run();

    				// suppress further processing of this KeyEvent
    				event.doit = false;
    			}
    			// run CopyAction when user presses ctrl+c or ⌘+c
    			else if (event.keyCode == 'c' && event.stateMask == SWT.MOD1) {
    				copyAction.run();

    				// suppress further processing of this KeyEvent
    				event.doit = false;
    			}
    			// run PasteAction when user presses ctrl+v or ⌘+v
    			else if (event.keyCode == 'v' && event.stateMask == SWT.MOD1) {
    				pasteAction.run();

    				// suppress further processing of this KeyEvent
    				event.doit = false;
    			}
			}
		};
	};


	private Composite createButtonComposite() {
		Composite composite = new Composite(this, SWT.NONE);

		composite.setLayout( new GridLayout() );
		buttonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER);

		createAddButton(composite);
		createRemoveButton(composite);

		upDownComposite = new UpDownComposite(composite, SWT.NONE);
		buttonGridDataFactory.applyTo(upDownComposite);
		upDownComposite.setUpDownListener(upDownListener);

		copyPasteButtonComposite = new CopyPasteButtonComposite(composite, SWT.NONE, false /*horizontal*/);
		buttonGridDataFactory.applyTo(copyPasteButtonComposite);
		copyPasteButtonComposite.getCopyButton().addListener(SWT.Selection, e -> copyToClipboad());
		copyPasteButtonComposite.getPasteButton().addListener(SWT.Selection, e -> pasteFromClipboad());

		return composite;
	}


	private void createAddButton(Composite parent) {
		addButton = new Button(parent, SWT.PUSH);
		buttonGridDataFactory.applyTo(addButton);
		addButton.setImage( IconRegistry.getImage("icons/add.png") );
		addButton.addListener(SWT.Selection, e -> addItem());
	}


	private void createRemoveButton(Composite parent) {
		removeButton = new Button(parent, SWT.PUSH);
		buttonGridDataFactory.applyTo(removeButton);
		removeButton.setImage( IconRegistry.getImage("icons/delete.png") );
		removeButton.addListener(SWT.Selection, e -> removeItem());
	}


	private void updateButtonState() {
		// always enable even if nothing is selected, otherwise the user wouldn't be able to add the first Section
		boolean addEnabled = false;
		boolean removeEnabled = false;
		boolean upEnabled = false;
		boolean downEnabled = false;
		boolean copyPasterEnabled = false;


		if (page != null && !page.isFixedStructure()) {
			addEnabled = true;
			copyPasterEnabled = true;

    		// determine current selection
    		Object selectedObject = SelectionHelper.getUniqueSelected( getSelection() );

    		removeEnabled = selectedObject != null && isSelectedItemDeleteable();


    		// determine index of selected Composite
    		Object selectedItem = SelectionHelper.getUniqueSelected( getSelection() );
    		if (selectedItem instanceof Section) {
    			List<Section> sectionList = page.getSectionList();
    			int sectionIndex = sectionList.indexOf(selectedItem);

    			upEnabled = sectionIndex > 0;
    			downEnabled = sectionIndex < sectionList.size() - 1;
    		}
    		else if (selectedItem instanceof Component) {
    			// determine parent Section
    			Section parentSection = (Section) contentProvider.getParent(selectedItem);

    			List<Section> sectionList = page.getSectionList();
    			int parentSectionIndex = sectionList.indexOf(parentSection);

    			List<Component> componentList = parentSection.getComponentList();
    			int componentIndex = componentList.indexOf(selectedItem);

    			upEnabled = componentIndex > 0 || parentSectionIndex > 0;
    			downEnabled = componentIndex < componentList.size() - 1 || parentSectionIndex < sectionList.size() - 1;
    		}
		}

		addButton.setEnabled(addEnabled);
		removeButton.setEnabled(removeEnabled);
		upDownComposite.setTopEnabled(upEnabled);
		upDownComposite.setUpEnabled(upEnabled);
		upDownComposite.setDownEnabled(downEnabled);
		upDownComposite.setBottomEnabled(downEnabled);

		copyPasteButtonComposite.setEnabled(copyPasterEnabled);
	}


	private IUpDownListener upDownListener = new IUpDownListener() {
		@Override
		public void topPressed() {
			moveToFirst();
		}

		@Override
		public void upPressed() {
			moveUp();
		}

		@Override
		public void downPressed() {
			moveDown();
		}

		@Override
		public void bottomPressed() {
			moveToLast();
		}
	};


	private void createActions() {
		createSectionAction = new CreateSectionAction(this);

		if ( (isAdmin || isScriptComponentVisible) && availableComponentClassList.contains(ScriptComponent.class)) {
			createScriptComponentAction = new CreateScriptComponentAction(this);
		}

		if (availableComponentClassList.contains(TextComponent.class)) {
			createTextComponentAction = new CreateTextComponentAction(this);
		}

		if (availableComponentClassList.contains(ProfileFieldComponent.class)) {
			createProfileFieldComponentAction = new CreateProfileFieldComponentAction(this);
		}

		if (availableComponentClassList.contains(ParticipantFieldComponent.class)) {
			createParticipantFieldComponentAction = new CreateParticipantFieldComponentAction(this);
		}

		if (availableComponentClassList.contains(EmailComponent.class)) {
			createEmailComponentAction = new CreateEmailComponentAction(this);
		}

		if (availableComponentClassList.contains(FileComponent.class)) {
			createFileComponentAction = new CreateFileComponentAction(this);
		}

		if (availableComponentClassList.contains(UploadComponent.class)) {
			createUploadComponentAction = new CreateUploadComponentAction(this);
		}

		if (availableComponentClassList.contains(ProgrammeBookingComponent.class)) {
			createProgrammeBookingComponentAction = new CreateProgrammeBookingComponentAction(this);
		}

		if (availableComponentClassList.contains(OpenAmountComponent.class)) {
			createOpenAmountComponentAction = new CreateOpenAmountComponentAction(this);
		}

		if (availableComponentClassList.contains(TotalAmountComponent.class)) {
			createTotalAmountComponentAction = new CreateTotalAmountComponentAction(this);
		}

		if (availableComponentClassList.contains(StreamComponent.class)) {
			createStreamComponentAction = new CreateStreamComponentAction(this);
		}

		if (availableComponentClassList.contains(PaymentComponent.class)) {
			createPaymentComponentAction = new CreatePaymentComponentAction(this);
		}

		if (availableComponentClassList.contains(PaymentWithFeeComponent.class)) {
			createPaymentWithFeeComponentAction = new CreatePaymentWithFeeComponentAction(this);
		}

		if (availableComponentClassList.contains(DigitalEventComponent.class)) {
			createDigitalEventComponentAction = new CreateDigitalEventComponentAction(this);
		}

		if (availableComponentClassList.contains(PrintComponent.class)) {
			createPrintComponentAction = new CreatePrintComponentAction(this);
		}

		if (availableComponentClassList.contains(SendLetterOfInvitationComponent.class)) {
			createSendLetterOfInvitationComponentAction = new CreateSendLetterOfInvitationComponentAction(this);
		}

		if (availableComponentClassList.contains(SummaryComponent.class)) {
			createSummaryComponentAction = new CreateSummaryComponentAction(this);
		}

		// Actions of Components of Group Portals
		if (availableComponentClassList.contains(GroupMemberTableComponent.class)) {
			createGroupMemberTableComponentAction = new CreateGroupMemberTableComponentAction(this);
		}

		// Actions of Components of Hotel Portals
		if (availableComponentClassList.contains(HotelSearchCriteriaComponent.class)) {
			createHotelSearchCriteriaComponentAction = new CreateHotelSearchCriteriaComponentAction(this);
		}

		if (availableComponentClassList.contains(HotelSearchFilterComponent.class)) {
			createHotelSearchFilterComponentAction = new CreateHotelSearchFilterComponentAction(this);
		}

		if (availableComponentClassList.contains(HotelSearchResultComponent.class)) {
			createHotelSearchResultComponentAction = new CreateHotelSearchResultComponentAction(this);
		}

		if (availableComponentClassList.contains(HotelDetailsComponent.class)) {
			createHotelDetailsComponentAction = new CreateHotelDetailsComponentAction(this);
		}

		if (availableComponentClassList.contains(HotelBookingComponent.class)) {
			createHotelBookingComponentAction = new CreateHotelBookingComponentAction(this);
		}
		
		if (availableComponentClassList.contains(EditBookingComponent.class)) {
			createEditBookingComponentAction = new CreateEditBookingComponentAction(this);
		}

		if (availableComponentClassList.contains(HotelTotalAmountComponent.class)) {
			createHotelTotalAmountComponentAction = new CreateHotelTotalAmountComponentAction(this);
		}
		
		// Actions of Components of the Hotel Speaker Portal
		if (availableComponentClassList.contains(SpeakerArrivalDepartureComponent.class)) {
			createSpeakerArrivalDepartureComponentAction = new CreateSpeakerArrivalDepartureComponentAction(this);
		}
		if (availableComponentClassList.contains(SpeakerRoomTypeComponent.class)) {
			createSpeakerRoomTypeComponentAction = new CreateSpeakerRoomTypeComponentAction(this);
		}

		// Actions of Components of the Profile Portals
		if (availableComponentClassList.contains(PortalTableComponent.class)) {
			createPortalTableComponentAction = new CreatePortalTableComponentAction(this);
		}

		// Actions of Components of the Certificate Portal
		if (availableComponentClassList.contains(CertificateComponent.class)) {
			createCertificateComponentAction = new CreateCertificateComponentAction(this);
		}


		// Actions of Membership Components

		if (availableComponentClassList.contains(EcfsMembershipComponent.class)) {
			createEcfsMembershipComponentAction = new CreateEcfsMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(EficMembershipComponent.class)) {
			createEficMembershipComponentAction = new CreateEficMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(EnetsMembershipComponent.class)) {
			createEnetsMembershipComponentAction = new CreateEnetsMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(EsicmMembershipComponent.class)) {
			createEsicmMembershipComponentAction = new CreateEsicmMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(EsraMembershipComponent.class)) {
			createEsraMembershipComponentAction = new CreateEsraMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(EsskaMembershipComponent.class)) {
			createEsskaMembershipComponentAction = new CreateEsskaMembershipComponentAction(this);
		}
		
		if (availableComponentClassList.contains(FensMembershipComponent.class)) {
			createFensMembershipComponentAction = new CreateFensMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(IflaMembershipComponent.class)) {
			createIflaMembershipComponentAction = new CreateIflaMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(IltsMembershipComponent.class)) {
			createIltsMembershipComponentAction = new CreateIltsMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(IspadMembershipComponent.class)) {
			createIspadMembershipComponentAction = new CreateIspadMembershipComponentAction(this);
		}

		if (availableComponentClassList.contains(NeurosciencesMembershipComponent.class)) {
			createNeurosciencesMembershipComponentAction = new CreateNeurosciencesMembershipComponentAction(this);
		}


		deleteAction = new DeleteAction(this);
		copyAction = new CopyAction(this);
		pasteAction = new PasteAction(this);
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu( treeViewer.getControl() );
		treeViewer.getControl().setMenu(menu);
	}


	private void fillContextMenu(IMenuManager manager) {
		if (page != null && !page.isFixedStructure()) {
    		manager.add(createSectionAction);


    		// add Component actions
    		if (createScriptComponentAction != null) {
    			manager.add(createScriptComponentAction);
    		}

    		if (createTextComponentAction != null) {
    			manager.add(createTextComponentAction);
    		}

    		if (createProfileFieldComponentAction != null) {
    			manager.add(createProfileFieldComponentAction);
    		}

    		if (createParticipantFieldComponentAction != null) {
    			manager.add(createParticipantFieldComponentAction);
    		}

    		if (createEmailComponentAction != null) {
    			manager.add(createEmailComponentAction);
    		}

    		if (createFileComponentAction != null) {
    			manager.add(createFileComponentAction);
    		}

    		if (createUploadComponentAction != null) {
    			manager.add(createUploadComponentAction);
    		}

    		if (createProgrammeBookingComponentAction != null) {
    			manager.add(createProgrammeBookingComponentAction);
    		}

    		if (createOpenAmountComponentAction != null) {
    			manager.add(createOpenAmountComponentAction);
    		}

    		if (createTotalAmountComponentAction != null) {
    			manager.add(createTotalAmountComponentAction);
    		}

    		if (createStreamComponentAction != null) {
    			manager.add(createStreamComponentAction);
    		}

    		if (createPaymentComponentAction != null) {
    			manager.add(createPaymentComponentAction);
    		}

    		if (createPaymentWithFeeComponentAction != null) {
    			manager.add(createPaymentWithFeeComponentAction);
    		}

    		if (createDigitalEventComponentAction != null) {
    			manager.add(createDigitalEventComponentAction);
    		}

    		if (createPrintComponentAction != null) {
    			manager.add(createPrintComponentAction);
    		}

    		if (createSendLetterOfInvitationComponentAction != null) {
    			manager.add(createSendLetterOfInvitationComponentAction);
    		}

    		if (createSummaryComponentAction != null) {
    			manager.add(createSummaryComponentAction);
    		}


    		// Actions of Components of Group Portals
    		manager.add(new Separator());

    		if (createGroupMemberTableComponentAction != null) {
    			manager.add(createGroupMemberTableComponentAction);
    		}


    		// Actions of Components of Hotel Portals
    		manager.add(new Separator());

    		if (createHotelSearchCriteriaComponentAction != null) {
    			manager.add(createHotelSearchCriteriaComponentAction);
    		}

    		if (createHotelSearchFilterComponentAction != null) {
    			manager.add(createHotelSearchFilterComponentAction);
    		}

    		if (createHotelSearchResultComponentAction != null) {
    			manager.add(createHotelSearchResultComponentAction);
    		}

    		if (createHotelDetailsComponentAction != null) {
    			manager.add(createHotelDetailsComponentAction);
    		}

    		if (createHotelBookingComponentAction != null) {
    			manager.add(createHotelBookingComponentAction);
    		}
    		
    		if (createEditBookingComponentAction != null) {
    			manager.add(createEditBookingComponentAction);
    		}

    		if (createHotelTotalAmountComponentAction != null) {
    			manager.add(createHotelTotalAmountComponentAction);
    		}
    		
    		// Actions of Components of the Hotel Speaker Portal
    		if (createSpeakerArrivalDepartureComponentAction != null) {
    			manager.add(createSpeakerArrivalDepartureComponentAction);
    		}
    		if (createSpeakerRoomTypeComponentAction != null) {
    			manager.add(createSpeakerRoomTypeComponentAction);
    		}


    		// Actions of Components of the Profile Portals
    		manager.add(new Separator());

    		if (createPortalTableComponentAction != null) {
    			manager.add(createPortalTableComponentAction);
    		}

    		// Action of Components of the Certificate Portal
    		manager.add(new Separator());

    		if (createCertificateComponentAction != null) {
    			manager.add(createCertificateComponentAction);
    		}


    		// Actions of Membership Components
    		manager.add(new Separator());

    		if (createEcfsMembershipComponentAction != null) {
    			manager.add(createEcfsMembershipComponentAction);
    		}

    		if (createEficMembershipComponentAction != null) {
    			manager.add(createEficMembershipComponentAction);
    		}

    		if (createEnetsMembershipComponentAction != null) {
    			manager.add(createEnetsMembershipComponentAction);
    		}

    		if (createEsicmMembershipComponentAction != null) {
    			manager.add(createEsicmMembershipComponentAction);
    		}

    		if (createEsraMembershipComponentAction != null) {
    			manager.add(createEsraMembershipComponentAction);
    		}

    		if (createEsskaMembershipComponentAction != null) {
    			manager.add(createEsskaMembershipComponentAction);
    		}
    		
    		if (createFensMembershipComponentAction != null) {
    			manager.add(createFensMembershipComponentAction);
    		}

    		if (createIflaMembershipComponentAction != null) {
    			manager.add(createIflaMembershipComponentAction);
    		}

    		if (createIltsMembershipComponentAction != null) {
    			manager.add(createIltsMembershipComponentAction);
    		}

    		if (createIspadMembershipComponentAction != null) {
    			manager.add(createIspadMembershipComponentAction);
    		}

    		if (createNeurosciencesMembershipComponentAction != null) {
    			manager.add(createNeurosciencesMembershipComponentAction);
    		}



    		manager.add(new Separator());
    		manager.add(deleteAction);

    		manager.add(new Separator());
    		manager.add(copyAction);
    		manager.add(pasteAction);

    		// Other plug-ins can contribute there actions here
    		manager.add(new Separator());
    		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}


	private void addItem() {
		try {
			// create and initialize dialog
			CreatePageContentDialog dialog = new CreatePageContentDialog(getShell(), portalType);
			dialog.create();
			dialog.setSectionEnabled( createSectionAction.isEnabled() );


			// enable Components
			dialog.setTextComponentEnabled( 					createTextComponentAction != null 					&& 	createTextComponentAction.isEnabled() );
			dialog.setProfileFieldComponentEnabled( 			createProfileFieldComponentAction != null 			&& 	createProfileFieldComponentAction.isEnabled() );
			dialog.setParticipantFieldComponentEnabled( 		createParticipantFieldComponentAction != null 		&& 	createParticipantFieldComponentAction.isEnabled() );
			dialog.setEmailComponentEnabled( 					createEmailComponentAction != null 					&& 	createEmailComponentAction.isEnabled() );
			dialog.setFileComponentEnabled( 					createFileComponentAction != null 					&& 	createFileComponentAction.isEnabled() );
			dialog.setUploadComponentEnabled( 					createUploadComponentAction != null 				&& 	createUploadComponentAction.isEnabled() );
			dialog.setProgrammeBookingComponentEnabled( 		createProgrammeBookingComponentAction != null		&& 	createProgrammeBookingComponentAction.isEnabled() );
			dialog.setOpenAmountComponentEnabled( 				createOpenAmountComponentAction != null 			&& 	createOpenAmountComponentAction.isEnabled() );
			dialog.setTotalAmountComponentEnabled( 				createTotalAmountComponentAction != null 			&& 	createTotalAmountComponentAction.isEnabled() );
			dialog.setStreamComponentEnabled( 					createStreamComponentAction != null 				&& 	createStreamComponentAction.isEnabled() );
			dialog.setPaymentComponentEnabled( 					createPaymentComponentAction != null 				&& 	createPaymentComponentAction.isEnabled() );
			dialog.setPaymentWithFeeComponentEnabled(			createPaymentWithFeeComponentAction != null 		&&	createPaymentWithFeeComponentAction.isEnabled() );
			dialog.setDigitalEventComponentEnabled( 			createDigitalEventComponentAction != null 			&&	createDigitalEventComponentAction.isEnabled() );
			dialog.setPrintComponentEnabled( 					createPrintComponentAction != null 					&&	createPrintComponentAction.isEnabled() );
			dialog.setSendLetterOfInvitationComponentEnabled( 	createSendLetterOfInvitationComponentAction != null && 	createSendLetterOfInvitationComponentAction.isEnabled() );
			dialog.setSummaryComponentEnabled( 					createSummaryComponentAction != null 				&& 	createSummaryComponentAction.isEnabled() );

			// open dialog
			int open = dialog.open();

			// evaluate dialog settings
			if (open == Window.OK) {
				if (dialog.isSection()) {
					createSectionAction.run();
				}
				else if (dialog.isTextComponent()) {
					createTextComponentAction.run();
				}
				else if (dialog.isProfileFieldComponent()) {
					createProfileFieldComponentAction.run();
				}
				else if (dialog.isParticipantFieldComponent()) {
					createParticipantFieldComponentAction.run();
				}
				else if (dialog.isEmailComponent()) {
					createEmailComponentAction.run();
				}
				else if (dialog.isFileComponent()) {
					createFileComponentAction.run();
				}
				else if (dialog.isUploadComponent()) {
					createUploadComponentAction.run();
				}
				else if (dialog.isProgrammeBookingComponent()) {
					createProgrammeBookingComponentAction.run();
				}
				else if (dialog.isOpenAmountComponent()) {
					createOpenAmountComponentAction.run();
				}
				else if (dialog.isTotalAmountComponent()) {
					createTotalAmountComponentAction.run();
				}
				else if (dialog.isStreamComponent()) {
					createStreamComponentAction.run();
				}
				else if (dialog.isPaymentComponent()) {
					createPaymentComponentAction.run();
				}
				else if (dialog.isPaymentWithFeeComponent()) {
					createPaymentWithFeeComponentAction.run();
				}
				else if (dialog.isDigitalEventComponent()) {
					createDigitalEventComponentAction.run();
				}
				else if (dialog.isPrintComponent()) {
					createPrintComponentAction.run();
				}
				else if (dialog.isSendLetterOfInvitationComponent()) {
					createSendLetterOfInvitationComponentAction.run();
				}
				else if (dialog.isSummaryComponent()) {
					createSummaryComponentAction.run();
				}
			}

			/* Finally adjust Buttons, because for unknown reasons they have not been adjusted yet, although
			 * adjustButtonState() has already been called.
			 */
			updateButtonState();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void removeItem() {
		try {
			// determine current selection
			IdProvider selectedItem = getSelectedItem();
			IdProvider nextSelectedItem = removeItem(selectedItem);

			// refresh tree data
			syncWidgetsToEntity();

			if (nextSelectedItem != null) {
				setSelectedItem(nextSelectedItem);
			}

			// fire ModifyEvent
			modifySupport.fire();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private IdProvider removeItem(IdProvider item) {
		IdProvider nextSelectedItem = null;

		List<IdProvider> origItemList = getItemList();
		int indexOfFirstDeletedItem = -1;
		int indexOfLastDeletedItem = -1;

		List<Section> sectionList = page.getSectionList();
		if (sectionList != null) {
    		for (int sectionIdx = 0; sectionIdx < sectionList.size(); sectionIdx++) {
    			if (indexOfFirstDeletedItem > -1) {
    				break;
    			}

    			Section section = sectionList.get(sectionIdx);
    			if (section == item) {
    				sectionList.remove(sectionIdx);
    				removeHtmlIdFromAllRenderFields( section.getHtmlId() );

    				// determine next selected item
    				indexOfFirstDeletedItem = origItemList.indexOf(section);
    				indexOfLastDeletedItem = indexOfFirstDeletedItem + section.getComponentList().size();

    				break;
    			}
    			else {
    				List<Component> componentList = section.getComponentList();
    				for (int componentIdx = 0; componentIdx < componentList.size(); componentIdx++) {
    					Component component = componentList.get(componentIdx);
    					if (component == item) {
    						componentList.remove(componentIdx);
    						removeHtmlIdFromAllRenderFields( component.getHtmlId() );

    						// determine next selected item
    	    				indexOfFirstDeletedItem = origItemList.indexOf(component);
    	    				indexOfLastDeletedItem = indexOfFirstDeletedItem;

    	    				break;
    					}
    				}
    			}
    		}

			if (indexOfLastDeletedItem < origItemList.size() - 1) {
				nextSelectedItem = origItemList.get(indexOfLastDeletedItem + 1);
			}
			else if (indexOfFirstDeletedItem > 0) {
				nextSelectedItem = origItemList.get(indexOfFirstDeletedItem - 1);
			}
		}


		return nextSelectedItem;
	}


	private void removeHtmlIdFromAllRenderFields(String htmlId) {
		List<Section> sectionList = page.getSectionList();
		if (sectionList != null) {
			for (Section section : sectionList) {
				for (Component component : section.getComponentList()) {
					String render = component.getRender();
					if (render != null && render.contains(htmlId)) {
						// remove htmlId
						render = render.replace(htmlId, "");

						// trim whitespace
						render = render.replaceAll("\\s+", " ");

						component.setRender(render);
					}
				}
			}
		}
	}


	private List<IdProvider> getItemList() {
		// put all items into a List
		List<IdProvider> itemList = new ArrayList<>();
		for (Section section : page.getSectionList()) {
			itemList.add(section);
			for (Component component : section.getComponentList()) {
				itemList.add(component);
			}
		}
		return itemList;
	}


	public void copyToClipboad() {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		try {
			syncDetails();

			// determine current selection
			IdProvider selectedItem = getSelectedItem();

			if (selectedItem instanceof Section) {
				Section section = (Section) selectedItem;

				String xml = XmlHelper.toXML(section);

				clipboard.setContents(
					new Object[] { section, xml},
					new Transfer[] { IdProviderTransfer.getInstance(), TextTransfer.getInstance() }
				);
			}
			else if (selectedItem instanceof Component) {
				Component textComponent = (Component) selectedItem;

				String xml = XmlHelper.toXML(textComponent);

				clipboard.setContents(
					new Object[] { textComponent, xml},
					new Transfer[] { IdProviderTransfer.getInstance(), TextTransfer.getInstance() }
				);
			}
		}
		catch (Exception t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			clipboard.dispose();
		}
	}


	/**
	 * Sync current content in PageContentComposite.
	 */
	private void syncDetails() {
		Composite parent = getParent();
		while (parent != null) {
			if (parent instanceof PageContentComposite) {
				PageContentComposite pageContentComposite = (PageContentComposite) parent;
				pageContentComposite.syncEntityToWidgets();
				break;
			}
			else {
				parent = parent.getParent();
			}
		}
	}


	public void pasteFromClipboad() {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		try {
			Object contents = clipboard.getContents( IdProviderTransfer.getInstance() );

			if (contents instanceof Section) {
				Section newSection = (Section) contents;
				newSection.setHtmlId( PageHelper.generateUniqueHtmlId(PageHelper.SECTION_HTML_ID_PREFIX) );

				// determine current selection
				String selectedId = null;
				IdProvider selectedItem = getSelectedItem();
				if (selectedItem != null) {
					selectedId = selectedItem.getId().toString();
				}

				PageHelper.addSection(getPage(), selectedId, newSection);

				handleNewItem(newSection);
			}
			else if (contents instanceof Component) {
				Component newComponent = (Component) contents;
				newComponent.setHtmlId( PageHelper.generateUniqueHtmlId(PageHelper.COMPONENT_HTML_ID_PREFIX) );
				if ( availableComponentClassList.contains(newComponent.getClass()) ) {
    				// determine current selection
    				IdProvider selectedItem = getSelectedItem();
    				if (selectedItem != null) {
    					String selectedId = selectedItem.getId().toString();
    					PageHelper.addComponent(getPage(), selectedId, newComponent);
    					handleNewItem(newComponent);
    				}
				}
				else {
					System.out.println("The Portal Type " + portalType + " does not support " + newComponent.getClass() + ".");
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			clipboard.dispose();
		}
	}


	/**
	 * Do everything that is necessary when the structure of the tree has changed.
	 */
	private void handleTreeDataChange() {
		treeViewer.refresh();
		updateButtonState();
		modifySupport.fire();
	}


	/**
	 * Move the selected tree node to the top position.
	 * If the selected tree node is a {@link Component} which is at the top of its {@link Section}, then it is moved
	 * to the top of the previous {@link Section} (if there is one).
	 */
	private void moveToFirst() {
		Object selectedItem = SelectionHelper.getUniqueSelected( treeViewer.getSelection() );
		if (selectedItem instanceof Section) {
			Section selectedSection = (Section) selectedItem;

			// determine index of selected Section
			List<Section> sectionList = page.getSectionList();
			int sectionIndex = sectionList.indexOf(selectedSection);
			if (sectionIndex > 0) {
				// move data
				CollectionsHelper.moveFirst(sectionList, sectionIndex);
				handleTreeDataChange();
			}
		}
		else if (selectedItem instanceof Component) {
			Component selectedComponent = (Component) selectedItem;

			Section parentSection = (Section) contentProvider.getParent(selectedComponent);
			List<Component> componentList = parentSection.getComponentList();

			int componentIndex = componentList.indexOf(selectedComponent);

			if (componentIndex > 0) {
				// move to top in same Section
				CollectionsHelper.moveFirst(componentList, componentIndex);
				handleTreeDataChange();
			}
			else {
				// move Component from first position of current Section to first position of previous Section
				List<Section> sectionList = page.getSectionList();
				int parentSectionIndex = sectionList.indexOf(parentSection);
				if (parentSectionIndex > 0) {
					Section targetSection = sectionList.get(parentSectionIndex - 1);
					componentList.remove(0);
					targetSection.getComponentList().add(0, selectedComponent);
					contentProvider.initElementToParentMap();
					handleTreeDataChange();
				}
			}
		}
	}


	/**
	 * Move the selected tree node up.
	 * If the selected tree node is a {@link Component} which is at the top of its {@link Section}, then it is moved
	 * to the end of the previous {@link Section} (if there is one).
	 */
	private void moveUp() {
		Object selectedItem = SelectionHelper.getUniqueSelected( treeViewer.getSelection() );
		if (selectedItem instanceof Section) {
			Section selectedSection = (Section) selectedItem;

			// determine index of selected Section
			List<Section> sectionList = page.getSectionList();
			int sectionIndex = sectionList.indexOf(selectedSection);
			if (sectionIndex > 0) {
				// move data
				CollectionsHelper.moveUp(sectionList, sectionIndex);
				handleTreeDataChange();
			}
		}
		else if (selectedItem instanceof Component) {
			Component selectedComponent = (Component) selectedItem;

			Section parentSection = (Section) contentProvider.getParent(selectedComponent);
			List<Component> componentList = parentSection.getComponentList();

			int componentIndex = componentList.indexOf(selectedComponent);

			if (componentIndex > 0) {
				// move up in same Section
				CollectionsHelper.moveUp(componentList, componentIndex);
				handleTreeDataChange();
			}
			else {
				// move Component from first position of current Section to last position of previous Section
				List<Section> sectionList = page.getSectionList();
				int parentSectionIndex = sectionList.indexOf(parentSection);
				if (parentSectionIndex > 0) {
					Section targetSection = sectionList.get(parentSectionIndex - 1);
					componentList.remove(0);
					targetSection.getComponentList().add(selectedComponent);
					contentProvider.initElementToParentMap();
					handleTreeDataChange();
				}
			}
		}
	}


	/**
	 * Move the selected tree node down.
	 * If the selected tree node is a {@link Component} which is at the end of its {@link Section}, then it is moved
	 * to the top of the next {@link Section} (if there is one).
	 */
	private void moveDown() {
		Object selectedItem = SelectionHelper.getUniqueSelected( treeViewer.getSelection() );
		if (selectedItem instanceof Section) {
			Section selectedSection = (Section) selectedItem;

			// determine index of selected Section
			List<Section> sectionList = page.getSectionList();
			int sectionIndex = sectionList.indexOf(selectedSection);
			if (sectionIndex < sectionList.size() - 1) {
				// move data
				CollectionsHelper.moveDown(sectionList, sectionIndex);
				handleTreeDataChange();
			}
		}
		else if (selectedItem instanceof Component) {
			Component selectedComponent = (Component) selectedItem;

			Section parentSection = (Section) contentProvider.getParent(selectedComponent);
			List<Component> componentList = parentSection.getComponentList();

			int componentIndex = componentList.indexOf(selectedComponent);

			if (componentIndex < componentList.size() - 1) {
				// move down in same Section
				CollectionsHelper.moveDown(componentList, componentIndex);
				handleTreeDataChange();
			}
			else {
				// move Component from last position of current Section to fist position of next Section
				List<Section> sectionList = page.getSectionList();
				int parentSectionIndex = sectionList.indexOf(parentSection);
				if (parentSectionIndex < sectionList.size() - 1) {
					Section targetSection = sectionList.get(parentSectionIndex + 1);
					componentList.remove(componentList.size() - 1);
					targetSection.getComponentList().add(0, selectedComponent);
					contentProvider.initElementToParentMap();
					handleTreeDataChange();
				}
			}
		}
	}


	/**
	 * Move the selected tree node to the last position.
	 * If the selected tree node is a {@link Component} which is at the end of its {@link Section}, then it is moved
	 * to the end of the next {@link Section} (if there is one).
	 */
	private void moveToLast() {
		Object selectedItem = SelectionHelper.getUniqueSelected( treeViewer.getSelection() );
		if (selectedItem instanceof Section) {
			Section selectedSection = (Section) selectedItem;

			// determine index of selected Section
			List<Section> sectionList = page.getSectionList();
			int sectionIndex = sectionList.indexOf(selectedSection);
			if (sectionIndex < sectionList.size() - 1) {
				// move data
				CollectionsHelper.moveLast(sectionList, sectionIndex);
				handleTreeDataChange();
			}
		}
		else if (selectedItem instanceof Component) {
			Component selectedComponent = (Component) selectedItem;

			Section parentSection = (Section) contentProvider.getParent(selectedComponent);
			List<Component> componentList = parentSection.getComponentList();

			int componentIndex = componentList.indexOf(selectedComponent);

			if (componentIndex < componentList.size() - 1) {
				// move to the end in same Section
				CollectionsHelper.moveLast(componentList, componentIndex);
				handleTreeDataChange();
			}
			else {
				// move Component from last position of current Section to last position of next Section
				List<Section> sectionList = page.getSectionList();
				int parentSectionIndex = sectionList.indexOf(parentSection);
				if (parentSectionIndex < sectionList.size() - 1) {
					Section targetSection = sectionList.get(parentSectionIndex + 1);
					componentList.remove(componentList.size() - 1);
					targetSection.getComponentList().add(selectedComponent);
					contentProvider.initElementToParentMap();
					handleTreeDataChange();
				}
			}
		}
	}


	public boolean isSelectedItemDeleteable() {
		boolean result = true;

		if (page != null && page.getKey().equals(StandardRegistrationPortalPageConfig.LOGIN_PAGE.getKey())) {
			Object selectedObject = SelectionHelper.getUniqueSelected( getSelection() );
			if (selectedObject instanceof Section) {
				result = false;
			}
		}

		return result;
	}


	public ISelection getSelection() {
		return treeViewer.getSelection();
	}


	public void setSelection(ISelection selection) {
		treeViewer.setSelection(selection);
	}


	public IdProvider getSelectedItem() {
		IdProvider selectedObject = (IdProvider) SelectionHelper.getUniqueSelected( getSelection() );
		return selectedObject;
	}


	public void setSelectedItem(IdProvider selectedItem) {
		setSelection( new StructuredSelection(selectedItem) );
	}


	public Page getPage() {
		return page;
	}


	public void setPage(Page page) {
		this.page = page;
		syncWidgetsToEntity();

		if (page.isStaticAccess()) {
			if (copyPasteButtonComposite != null) {
				copyPasteButtonComposite.getPasteButton().setEnabled(false);
			}
			if (pasteAction != null) {
				pasteAction.setEnabled(false);
			}
		}
	}


	public void refresh() {
		treeViewer.refresh();
	}


	private void syncWidgetsToEntity() {
		if (page != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						ISelection selection = treeViewer.getSelection();
						treeViewer.setInput(page);
						treeViewer.expandAll();
						treeViewer.setSelection(selection, true);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	/**
	 * Handle a new tree node.
	 * First the tree refreshes its data.
	 * Then the new item gets selected.
	 * Finally a {@link ModifyEvent} is fired.
	 * @param newItem
	 */
	public void handleNewItem(Object newItem) {
		// refresh tree data
		syncWidgetsToEntity();

		// select new item
		if (newItem != null) {
    		StructuredSelection selection = new StructuredSelection(newItem);
    		treeViewer.setSelection(selection, true /*reveal*/);
		}

		// fire ModifyEvent
		modifySupport.fire();
	}


	public void syncEntityToWidgets() {
		// nothing to do, cause all operations operate on original data
	}


	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		treeViewer.addSelectionChangedListener(listener);
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		treeViewer.removeSelectionChangedListener(listener);
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
