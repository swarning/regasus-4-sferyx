package de.regasus.portal.page.editor;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Page;
import de.regasus.portal.Section;
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
import de.regasus.portal.component.SendRegistrationConfirmationComponent;
import de.regasus.portal.component.StreamComponent;
import de.regasus.portal.component.SummaryComponent;
import de.regasus.portal.component.TextComponent;
import de.regasus.portal.component.TotalAmountComponent;
import de.regasus.portal.component.UploadComponent;
import de.regasus.portal.component.group.GroupMemberTableComponent;
import de.regasus.portal.component.hotel.EditBookingComponent;
import de.regasus.portal.component.hotel.HotelBookingComponent;
import de.regasus.portal.component.hotel.HotelBookingTableComponent;
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
import de.regasus.portal.component.react.profile.CreateHotelBookingComponent;
import de.regasus.portal.component.react.profile.DocumediasAbstractPaymentComponent;
import de.regasus.portal.component.react.profile.DocumediasAbstractSubmissionComponent;
import de.regasus.portal.component.react.profile.DocumediasAbstractTableComponent;
import de.regasus.portal.component.react.profile.DocumediasAbstractTextComponent;
import de.regasus.portal.component.react.profile.ManageAbstractPaymentComponent;
import de.regasus.portal.component.react.profile.ManageAbstractSubmissionComponent;
import de.regasus.portal.component.react.profile.ManageAbstractTableComponent;
import de.regasus.portal.component.react.profile.ManageAbstractTextComponent;
import de.regasus.portal.component.react.profile.RegistrationBookingTableComponent;
import de.regasus.portal.component.react.profile.RegistrationComponent;
import de.regasus.portal.component.react.profile.RegistrationStatusTextComponent;
import de.regasus.portal.page.editor.group.GroupMemberTableComponentComposite;
import de.regasus.portal.page.editor.hotel.EditBookingComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelBookingComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelBookingTableComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelDetailsComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelSearchCriteriaComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelSearchFilterComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelSearchResultComponentComposite;
import de.regasus.portal.page.editor.hotel.HotelTotalAmountComponentComposite;
import de.regasus.portal.page.editor.hotelspeaker.SpeakerArrivalDepartureComponentComposite;
import de.regasus.portal.page.editor.hotelspeaker.SpeakerRoomTypeComponentComposite;
import de.regasus.portal.page.editor.membership.ecfs.EcfsMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.efic.EficMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.enets.EnetsMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.esicm.EsicmMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.esra.EsraMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.esska.EsskaMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.fens.FensMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.ifla.IflaMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.ilts.IltsMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.ispad.IspadMembershipComponentComposite;
import de.regasus.portal.page.editor.membership.neurosciences.NeurosciencesMembershipComponentComposite;
import de.regasus.portal.page.editor.paymentComponent.PaymentComponentComposite;
import de.regasus.portal.page.editor.paymentWithFeeComponent.PaymentWithFeeComponentComposite;
import de.regasus.portal.page.editor.profile.PortalTableComponentComposite;
import de.regasus.portal.page.editor.react.profile.CreateHotelBookingComponentComposite;
import de.regasus.portal.page.editor.react.profile.DocumediasAbstractPaymentComponentComposite;
import de.regasus.portal.page.editor.react.profile.DocumediasAbstractSubmissionComponentComposite;
import de.regasus.portal.page.editor.react.profile.DocumediasAbstractTableComponentComposite;
import de.regasus.portal.page.editor.react.profile.DocumediasAbstractTextComponentComposite;
import de.regasus.portal.page.editor.react.profile.ManageAbstractPaymentComponentComposite;
import de.regasus.portal.page.editor.react.profile.ManageAbstractSubmissionComponentComposite;
import de.regasus.portal.page.editor.react.profile.ManageAbstractTableComponentComposite;
import de.regasus.portal.page.editor.react.profile.ManageAbstractTextComponentComposite;
import de.regasus.portal.page.editor.react.profile.RegistrationBookingTableComponentComposite;
import de.regasus.portal.page.editor.react.profile.RegistrationComponentComposite;
import de.regasus.portal.page.editor.react.profile.RegistrationStatusTextComponentComposite;
import de.regasus.portal.type.react.certificate.CertificateComponentComposite;
import de.regasus.ui.Activator;


public class PageContentDetailComposite extends ScrolledComposite {

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Page page;
	private Long portalPK;

	// **************************************************************************
	// * Widgets
	// *

	/* this (ScrolledComposite)
	 *  |
	 *  --- contentComposite (Composite with StackLayout)
	 *       |
	 *       --- contentComposite
	 *       |
	 *       --- DigitalEventComponentComposite
	 *       |
	 *       --- FieldComponentComposite
	 *       .
	 *       .
	 *       .
	 */

	/**
	 * Child Composite of this ScrolledComposite.
	 * The area which is scrolled.
	 * Parent of SectionComposite and all Component Composites which contain the visible widgets.
	 * The contentComposite uses a StackLayout. So only one of its child Composites is visible at a time.
	 */
	private Composite contentComposite;
	private StackLayout stackLayout;

	// child Composites of contentComposite
	private SectionComposite sectionComposite;
	private DigitalEventComponentComposite digitalEventComponentComposite;
	private ProfileFieldComponentComposite profileFieldComponentComposite;
	private ParticipantFieldComponentComposite participantFieldComponentComposite;
	private EmailComponentComposite emailComponentComposite;
	private FileComponentComposite fileComponentComposite;
	private PaymentComponentComposite paymentComponentComposite;
	private PaymentWithFeeComponentComposite paymentWithFeeComponentComposite;
	private PrintComponentComposite printComponentComposite;
	private ProgrammeBookingComponentComposite programmeBookingComponentComposite;
	private OpenAmountComponentComposite openAmountComponentComposite;
	private TotalAmountComponentComposite totalAmountComponentComposite;
	private StreamComponentComposite streamComponentComposite;
	private SummaryComponentComposite summaryComponentComposite;
	private ScriptComponentComposite scriptComponentComposite;
	private TextComponentComposite textComponentComposite;
	private UploadComponentComposite uploadComponentComposite;
	private SendRegistrationConfirmationComponentComposite sendRegistrationConfirmationComponentComposite;
	private SendLetterOfInvitationComponentComposite sendLetterOfInvitationComponentComposite;

	// Component Composites of Group Portal
	private GroupMemberTableComponentComposite groupMemberTableComponentComposite;

	// Component Composites of Hotel Portal
	private HotelSearchCriteriaComponentComposite hotelSearchCriteriaComponentComposite;
	private HotelSearchFilterComponentComposite hotelSearchFilterComponentComposite;
	private HotelSearchResultComponentComposite hotelSearchResultComponentComposite;
	private HotelDetailsComponentComposite hotelDetailsComponentComposite;
	private HotelBookingComponentComposite hotelBookingComponentComposite;
	private HotelBookingTableComponentComposite hotelBookingTableComponentComposite;
	private EditBookingComponentComposite editBookingComponentComposite;
	private HotelTotalAmountComponentComposite hotelTotalAmountComponentComposite;
	
	// Component Composite of Hotel Speaker Portal
	private SpeakerArrivalDepartureComponentComposite speakerArrivalDepartureComponentComposite;
	private SpeakerRoomTypeComponentComposite speakerRoomTypeComponentComposite;

	// Component Composites of Profile Portals
	private PortalTableComponentComposite portalTableComponentComposite;

	// Component Composites of REACT Profile Portal
	private RegistrationComponentComposite registrationComponentComposite;
	private RegistrationStatusTextComponentComposite registrationStatusTextComponentComposite;
	private RegistrationBookingTableComponentComposite registrationBookingTableComponentComposite;

	private CreateHotelBookingComponentComposite createHotelBookingComponentComposite;

	private ManageAbstractSubmissionComponentComposite manageAbstractSubmissionComponentComposite;
	private ManageAbstractPaymentComponentComposite manageAbstractPaymentComponentComposite;
	private ManageAbstractTextComponentComposite manageAbstractTextComponentComposite;
	private ManageAbstractTableComponentComposite manageAbstractTableComponentComposite;

	private DocumediasAbstractSubmissionComponentComposite documediasAbstractSubmissionComponentComposite;
	private DocumediasAbstractPaymentComponentComposite documediasAbstractPaymentComponentComposite;
	private DocumediasAbstractTextComponentComposite documediasAbstractTextComponentComposite;
	private DocumediasAbstractTableComponentComposite documediasAbstractTableComponentComposite;

	// Component Composites of REACT Certificate
	private CertificateComponentComposite certificateComponentComposite;

	// Component Composites of Membership Components
	private EcfsMembershipComponentComposite ecfsMembershipComponentComposite;
	private EficMembershipComponentComposite eficMembershipComponentComposite;
	private EnetsMembershipComponentComposite enetsMembershipComponentComposite;
	private EsicmMembershipComponentComposite esicmMembershipComponentComposite;
	private EsraMembershipComponentComposite esraMembershipComponentComposite;
	private EsskaMembershipComponentComposite esskaMembershipComponentComposite;
	private FensMembershipComponentComposite fensMembershipComponentComposite;
	private IflaMembershipComponentComposite iflaMembershipComponentComposite;
	private IltsMembershipComponentComposite iltsMembershipComponentComposite;
	private IspadMembershipComponentComposite ispadMembershipComponentComposite;
	private NeurosciencesMembershipComponentComposite neurosciencesMembershipComponentComposite;

	// *
	// * Widgets
	// **************************************************************************


	public PageContentDetailComposite(Composite parent, int style)
	throws Exception {
		super(parent, style | SWT.V_SCROLL);

		setExpandHorizontal(true);
		setExpandVertical(true);

		contentComposite = new Composite(this, SWT.NONE);
		setContent(contentComposite);

		stackLayout = new StackLayout();
		contentComposite.setLayout(stackLayout);
	}


	/**
	 * Call this method after you created the child components, and also in case the content has changed
	 * (eg child composites have been added, opened or closed).
	 */
	private void refreshScrollbars() {
		contentComposite.layout();

		// calculate new height based on current width
		Rectangle clientArea = getClientArea();
		Point size = contentComposite.computeSize(clientArea.width, SWT.DEFAULT);

		// set new height as minHeight
		setMinHeight(size.y);
	}


	public void setPage(Page page) {
		this.page = Objects.requireNonNull(page);
		this.portalPK = page.getPortalId();

		if (sectionComposite != null) {
			sectionComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (digitalEventComponentComposite != null) {
			digitalEventComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (profileFieldComponentComposite != null) {
			profileFieldComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (participantFieldComponentComposite != null) {
			participantFieldComponentComposite.setFixedStructure( page.isFixedStructure() );
		}
		
		if (emailComponentComposite != null) {
			emailComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (fileComponentComposite != null) {
			fileComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (paymentComponentComposite != null) {
			paymentComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (paymentWithFeeComponentComposite != null) {
			paymentWithFeeComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (printComponentComposite != null) {
			printComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (programmeBookingComponentComposite != null) {
			programmeBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (openAmountComponentComposite != null) {
			openAmountComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (totalAmountComponentComposite != null) {
			totalAmountComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (streamComponentComposite != null) {
			streamComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (summaryComponentComposite != null) {
			summaryComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (scriptComponentComposite != null) {
			scriptComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (textComponentComposite != null) {
			textComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (uploadComponentComposite != null) {
			uploadComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (sendRegistrationConfirmationComponentComposite != null) {
			sendRegistrationConfirmationComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (sendLetterOfInvitationComponentComposite != null) {
			sendLetterOfInvitationComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		// Component Composites of Group Portal
		if (groupMemberTableComponentComposite != null) {
			groupMemberTableComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		// Component Composites of Hotel Portal
		if (hotelSearchCriteriaComponentComposite != null) {
			hotelSearchCriteriaComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (hotelSearchFilterComponentComposite != null) {
			hotelSearchFilterComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (hotelSearchResultComponentComposite != null) {
			hotelSearchResultComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (hotelDetailsComponentComposite != null) {
			hotelDetailsComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (hotelBookingComponentComposite != null) {
			hotelBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
		}
		
		if (hotelBookingTableComponentComposite != null) {
			hotelBookingTableComponentComposite.setFixedStructure( page.isFixedStructure() );
		}
		
		if (editBookingComponentComposite != null) {
			editBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (hotelTotalAmountComponentComposite != null) {
			hotelTotalAmountComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		// Component Composites of Profile Portals
		if (portalTableComponentComposite != null) {
			portalTableComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		// Component Composites of REACT Profile Portal
		if (registrationComponentComposite != null) {
			registrationComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (registrationStatusTextComponentComposite != null) {
			registrationStatusTextComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (registrationBookingTableComponentComposite != null) {
			registrationBookingTableComponentComposite.setFixedStructure( page.isFixedStructure() );
		}


		if (manageAbstractSubmissionComponentComposite != null) {
			manageAbstractSubmissionComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (manageAbstractPaymentComponentComposite != null) {
			manageAbstractPaymentComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (manageAbstractTextComponentComposite != null) {
			manageAbstractTextComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (manageAbstractTableComponentComposite != null) {
			manageAbstractTableComponentComposite.setFixedStructure( page.isFixedStructure() );
		}


		if (documediasAbstractSubmissionComponentComposite != null) {
			documediasAbstractSubmissionComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (documediasAbstractPaymentComponentComposite != null) {
			documediasAbstractPaymentComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (documediasAbstractTextComponentComposite != null) {
			documediasAbstractTextComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (documediasAbstractTableComponentComposite != null) {
			documediasAbstractTableComponentComposite.setFixedStructure( page.isFixedStructure() );
		}


		// Component Composites of REACT Certificate Portal
		if (certificateComponentComposite != null) {
			certificateComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		// Component Composites of Membership Components
		if (ecfsMembershipComponentComposite != null) {
			ecfsMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (eficMembershipComponentComposite != null) {
			eficMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (enetsMembershipComponentComposite != null) {
			enetsMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (esicmMembershipComponentComposite != null) {
			esicmMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (esraMembershipComponentComposite != null) {
			esraMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (esskaMembershipComponentComposite != null) {
			esskaMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}
		
		if (fensMembershipComponentComposite != null) {
			fensMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (iflaMembershipComponentComposite != null) {
			iflaMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (iltsMembershipComponentComposite != null) {
			iltsMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (ispadMembershipComponentComposite != null) {
			ispadMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}

		if (neurosciencesMembershipComponentComposite != null) {
			neurosciencesMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
		}
	}


	public void setSection(Section section) {
		try {
			// avoid to do anything if Section did not change
			if (   sectionComposite == null
				|| sectionComposite != stackLayout.topControl
				|| sectionComposite.getSection() != section
			) {
				/* Copy values from widgets to old Section. But only if the new Section has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Section.
				 */
				if (   sectionComposite == null
					|| ! sectionComposite.getSection().getId().equals(section.getId())
				) {
					syncEntityToWidgets();
				}

    			if (sectionComposite == null) {
    				sectionComposite = new SectionComposite(contentComposite, SWT.NONE, page);
    				sectionComposite.addModifyListener(modifySupport);
    				sectionComposite.setFixedStructure( page.isFixedStructure() );
    			}
    			sectionComposite.setSection(section);
    			stackLayout.topControl = sectionComposite;

    			refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setDigitalEventComponent(DigitalEventComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   digitalEventComponentComposite == null
				|| digitalEventComponentComposite != stackLayout.topControl
				|| digitalEventComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   digitalEventComponentComposite == null
					|| ! digitalEventComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (digitalEventComponentComposite == null) {
        			digitalEventComponentComposite = new DigitalEventComponentComposite(contentComposite, SWT.NONE, portalPK);
        			digitalEventComponentComposite.addModifyListener(modifySupport);
        			digitalEventComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		digitalEventComponentComposite.setEntity(component);
        		stackLayout.topControl = digitalEventComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setProfileFieldComponent(ProfileFieldComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   profileFieldComponentComposite == null
				|| profileFieldComponentComposite != stackLayout.topControl
				|| profileFieldComponentComposite.getComponent() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   profileFieldComponentComposite == null
					|| ! profileFieldComponentComposite.getComponent().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (profileFieldComponentComposite == null) {
        			profileFieldComponentComposite = new ProfileFieldComponentComposite(contentComposite, SWT.NONE, portalPK);
        			profileFieldComponentComposite.addModifyListener(modifySupport);
       				profileFieldComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		profileFieldComponentComposite.setComponent(component);
        		stackLayout.topControl = profileFieldComponentComposite;

    			refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setParticipantFieldComponent(ParticipantFieldComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   participantFieldComponentComposite == null
				|| participantFieldComponentComposite != stackLayout.topControl
				|| participantFieldComponentComposite.getComponent() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   participantFieldComponentComposite == null
					|| ! participantFieldComponentComposite.getComponent().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (participantFieldComponentComposite == null) {
        			participantFieldComponentComposite = new ParticipantFieldComponentComposite(contentComposite, SWT.NONE, portalPK);
        			participantFieldComponentComposite.addModifyListener(modifySupport);
       				participantFieldComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		participantFieldComponentComposite.setComponent(component);
        		stackLayout.topControl = participantFieldComponentComposite;

    			refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public void setEmailComponent(EmailComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   emailComponentComposite == null
				|| emailComponentComposite != stackLayout.topControl
				|| emailComponentComposite.getComponent() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   emailComponentComposite == null
					|| ! emailComponentComposite.getComponent().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (emailComponentComposite == null) {
        			emailComponentComposite = new EmailComponentComposite(contentComposite, SWT.NONE, portalPK);
        			emailComponentComposite.addModifyListener(modifySupport);
        			emailComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		emailComponentComposite.setComponent(component);
        		stackLayout.topControl = emailComponentComposite;

    			refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setFileComponent(FileComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   fileComponentComposite == null
				|| fileComponentComposite != stackLayout.topControl
				|| fileComponentComposite.getComponent() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   fileComponentComposite == null
					|| ! fileComponentComposite.getComponent().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (fileComponentComposite == null) {
        			fileComponentComposite = new FileComponentComposite(contentComposite, SWT.NONE, portalPK);
        			fileComponentComposite.addModifyListener(modifySupport);
        			fileComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		fileComponentComposite.setComponent(component);
        		stackLayout.topControl = fileComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setPaymentComponent(PaymentComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   paymentComponentComposite == null
				|| paymentComponentComposite != stackLayout.topControl
				|| paymentComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   paymentComponentComposite == null
					|| ! paymentComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (paymentComponentComposite == null) {
        			paymentComponentComposite = new PaymentComponentComposite(contentComposite, SWT.NONE, portalPK);
        			paymentComponentComposite.addModifyListener(modifySupport);
        			paymentComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		paymentComponentComposite.setEntity(component);
        		stackLayout.topControl = paymentComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setPaymentWithFeeComponent(PaymentWithFeeComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   paymentWithFeeComponentComposite == null
				|| paymentWithFeeComponentComposite != stackLayout.topControl
				|| paymentWithFeeComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   paymentWithFeeComponentComposite == null
					|| ! paymentWithFeeComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (paymentWithFeeComponentComposite == null) {
        			paymentWithFeeComponentComposite = new PaymentWithFeeComponentComposite(contentComposite, SWT.NONE, portalPK);
        			paymentWithFeeComponentComposite.addModifyListener(modifySupport);
        			paymentWithFeeComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		paymentWithFeeComponentComposite.setEntity(component);
        		stackLayout.topControl = paymentWithFeeComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setPrintComponent(PrintComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   printComponentComposite == null
				|| printComponentComposite != stackLayout.topControl
				|| printComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   printComponentComposite == null
					|| ! printComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (printComponentComposite == null) {
        			printComponentComposite = new PrintComponentComposite(contentComposite, SWT.NONE, portalPK);
        			printComponentComposite.addModifyListener(modifySupport);
        			printComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		printComponentComposite.setEntity(component);
        		stackLayout.topControl = printComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setProgrammeBookingComponent(ProgrammeBookingComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   programmeBookingComponentComposite == null
				|| programmeBookingComponentComposite != stackLayout.topControl
				|| programmeBookingComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   programmeBookingComponentComposite == null
					|| ! programmeBookingComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

    			if (programmeBookingComponentComposite == null) {
    				programmeBookingComponentComposite = new ProgrammeBookingComponentComposite(
    					contentComposite,
    					SWT.NONE,
    					portalPK
    				);
    				programmeBookingComponentComposite.addModifyListener(modifySupport);
    				programmeBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
    			}
    			programmeBookingComponentComposite.setEntity(component);
    			stackLayout.topControl = programmeBookingComponentComposite;

    			refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setOpenAmountComponent(OpenAmountComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   openAmountComponentComposite == null
				|| openAmountComponentComposite != stackLayout.topControl
				|| openAmountComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   openAmountComponentComposite == null
					|| ! openAmountComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (openAmountComponentComposite == null) {
        			openAmountComponentComposite = new OpenAmountComponentComposite(contentComposite, SWT.NONE, portalPK);
        			openAmountComponentComposite.addModifyListener(modifySupport);
        			openAmountComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		openAmountComponentComposite.setEntity(component);
        		stackLayout.topControl = openAmountComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setTotalAmountComponent(TotalAmountComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   totalAmountComponentComposite == null
				|| totalAmountComponentComposite != stackLayout.topControl
				|| totalAmountComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   totalAmountComponentComposite == null
					|| ! totalAmountComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (totalAmountComponentComposite == null) {
        			totalAmountComponentComposite = new TotalAmountComponentComposite(contentComposite, SWT.NONE, portalPK);
        			totalAmountComponentComposite.addModifyListener(modifySupport);
        			totalAmountComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		totalAmountComponentComposite.setEntity(component);
        		stackLayout.topControl = totalAmountComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setStreamComponent(StreamComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   streamComponentComposite == null
				|| streamComponentComposite != stackLayout.topControl
				|| streamComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   streamComponentComposite == null
					|| ! streamComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

    			if (streamComponentComposite == null) {
    				streamComponentComposite = new StreamComponentComposite(contentComposite, SWT.NONE, portalPK);
    				streamComponentComposite.addModifyListener(modifySupport);
    				streamComponentComposite.setFixedStructure( page.isFixedStructure() );
    			}
    			streamComponentComposite.setEntity(component);
    			stackLayout.topControl = streamComponentComposite;

    			refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setSummaryComponent(SummaryComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   summaryComponentComposite == null
				|| summaryComponentComposite != stackLayout.topControl
				|| summaryComponentComposite.getComponent() != component
				) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   summaryComponentComposite == null
					|| ! summaryComponentComposite.getComponent().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}

				if (summaryComponentComposite == null) {
					summaryComponentComposite = new SummaryComponentComposite(contentComposite, SWT.NONE, portalPK);
					summaryComponentComposite.addModifyListener(modifySupport);
					summaryComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				summaryComponentComposite.setComponent(component);
				stackLayout.topControl = summaryComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setScriptComponent(ScriptComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   scriptComponentComposite == null
				|| scriptComponentComposite != stackLayout.topControl
				|| scriptComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   scriptComponentComposite == null
					|| ! scriptComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (scriptComponentComposite == null) {
        			scriptComponentComposite = new ScriptComponentComposite(contentComposite, SWT.NONE, portalPK);
        			scriptComponentComposite.addModifyListener(modifySupport);
        			scriptComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		scriptComponentComposite.setEntity(component);
        		stackLayout.topControl = scriptComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setTextComponent(TextComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   textComponentComposite == null
				|| textComponentComposite != stackLayout.topControl
				|| textComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   textComponentComposite == null
					|| ! textComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (textComponentComposite == null) {
        			textComponentComposite = new TextComponentComposite(contentComposite, SWT.NONE, portalPK);
        			textComponentComposite.addModifyListener(modifySupport);
        			textComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		textComponentComposite.setEntity(component);
        		stackLayout.topControl = textComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setUploadComponent(UploadComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   uploadComponentComposite == null
				|| uploadComponentComposite != stackLayout.topControl
				|| uploadComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   uploadComponentComposite == null
					|| ! uploadComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (uploadComponentComposite == null) {
        			uploadComponentComposite = new UploadComponentComposite(contentComposite, SWT.NONE, portalPK);
        			uploadComponentComposite.addModifyListener(modifySupport);
        			uploadComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		uploadComponentComposite.setEntity(component);
        		stackLayout.topControl = uploadComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setGroupMemberTableComponent(GroupMemberTableComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   groupMemberTableComponentComposite == null
				|| groupMemberTableComponentComposite != stackLayout.topControl
				|| groupMemberTableComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   groupMemberTableComponentComposite == null
					|| ! groupMemberTableComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


				if (groupMemberTableComponentComposite == null) {
					groupMemberTableComponentComposite = new GroupMemberTableComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					groupMemberTableComponentComposite.addModifyListener(modifySupport);
					groupMemberTableComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				groupMemberTableComponentComposite.setEntity(component);
				stackLayout.topControl = groupMemberTableComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setHotelSearchCriteriaComponent(HotelSearchCriteriaComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelSearchCriteriaComponentComposite == null
				|| hotelSearchCriteriaComponentComposite != stackLayout.topControl
				|| hotelSearchCriteriaComponentComposite.getEntity() != component
				) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelSearchCriteriaComponentComposite == null
					|| ! hotelSearchCriteriaComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (hotelSearchCriteriaComponentComposite == null) {
					hotelSearchCriteriaComponentComposite = new HotelSearchCriteriaComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					hotelSearchCriteriaComponentComposite.addModifyListener(modifySupport);
					hotelSearchCriteriaComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				hotelSearchCriteriaComponentComposite.setEntity(component);
				stackLayout.topControl = hotelSearchCriteriaComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setHotelSearchFilterComponent(HotelSearchFilterComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelSearchFilterComponentComposite == null
				|| hotelSearchFilterComponentComposite != stackLayout.topControl
				|| hotelSearchFilterComponentComposite.getEntity() != component
				) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelSearchFilterComponentComposite == null
					|| ! hotelSearchFilterComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (hotelSearchFilterComponentComposite == null) {
					hotelSearchFilterComponentComposite = new HotelSearchFilterComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					hotelSearchFilterComponentComposite.addModifyListener(modifySupport);
					hotelSearchFilterComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				hotelSearchFilterComponentComposite.setEntity(component);
				stackLayout.topControl = hotelSearchFilterComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setHotelSearchResultComponent(HotelSearchResultComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelSearchResultComponentComposite == null
				|| hotelSearchResultComponentComposite != stackLayout.topControl
				|| hotelSearchResultComponentComposite.getEntity() != component
				) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelSearchResultComponentComposite == null
					|| ! hotelSearchResultComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (hotelSearchResultComponentComposite == null) {
					hotelSearchResultComponentComposite = new HotelSearchResultComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					hotelSearchResultComponentComposite.addModifyListener(modifySupport);
					hotelSearchResultComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				hotelSearchResultComponentComposite.setEntity(component);
				stackLayout.topControl = hotelSearchResultComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setHotelDetailsComponent(HotelDetailsComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelDetailsComponentComposite == null
				|| hotelDetailsComponentComposite != stackLayout.topControl
				|| hotelDetailsComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelDetailsComponentComposite == null
					|| ! hotelDetailsComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (hotelDetailsComponentComposite == null) {
					hotelDetailsComponentComposite = new HotelDetailsComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					hotelDetailsComponentComposite.addModifyListener(modifySupport);
					hotelDetailsComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				hotelDetailsComponentComposite.setEntity(component);
				stackLayout.topControl = hotelDetailsComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setHotelBookingComponent(HotelBookingComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelBookingComponentComposite == null
				|| hotelBookingComponentComposite != stackLayout.topControl
				|| hotelBookingComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelBookingComponentComposite == null
					|| ! hotelBookingComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (hotelBookingComponentComposite == null) {
					hotelBookingComponentComposite = new HotelBookingComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					hotelBookingComponentComposite.addModifyListener(modifySupport);
					hotelBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				hotelBookingComponentComposite.setEntity(component);
				stackLayout.topControl = hotelBookingComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	public void setHotelBookingTableComponent(HotelBookingTableComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelBookingTableComponentComposite == null
				|| hotelBookingTableComponentComposite != stackLayout.topControl
				|| hotelBookingTableComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelBookingTableComponentComposite == null
					|| ! hotelBookingTableComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


				if (hotelBookingTableComponentComposite == null) {
					hotelBookingTableComponentComposite = new HotelBookingTableComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					hotelBookingTableComponentComposite.addModifyListener(modifySupport);
					hotelBookingTableComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				hotelBookingTableComponentComposite.setEntity(component);
				stackLayout.topControl = hotelBookingTableComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public void setEditBookingComponent(EditBookingComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   editBookingComponentComposite == null
				|| editBookingComponentComposite != stackLayout.topControl
				|| editBookingComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   editBookingComponentComposite == null
					|| ! editBookingComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (editBookingComponentComposite == null) {
					editBookingComponentComposite = new EditBookingComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					editBookingComponentComposite.addModifyListener(modifySupport);
					editBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				editBookingComponentComposite.setEntity(component);
				stackLayout.topControl = editBookingComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	

	public void setHotelTotalAmountComponent(HotelTotalAmountComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   hotelTotalAmountComponentComposite == null
				|| hotelTotalAmountComponentComposite != stackLayout.topControl
				|| hotelTotalAmountComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   hotelTotalAmountComponentComposite == null
					|| ! hotelTotalAmountComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (hotelTotalAmountComponentComposite == null) {
        			hotelTotalAmountComponentComposite = new HotelTotalAmountComponentComposite(contentComposite, SWT.NONE, portalPK);
        			hotelTotalAmountComponentComposite.addModifyListener(modifySupport);
        			hotelTotalAmountComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		hotelTotalAmountComponentComposite.setEntity(component);
        		stackLayout.topControl = hotelTotalAmountComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public void setSpeakerArrivalDepartureComponent(SpeakerArrivalDepartureComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   speakerArrivalDepartureComponentComposite == null
				|| speakerArrivalDepartureComponentComposite != stackLayout.topControl
				|| speakerArrivalDepartureComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   speakerArrivalDepartureComponentComposite == null
					|| ! speakerArrivalDepartureComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (speakerArrivalDepartureComponentComposite == null) {
					speakerArrivalDepartureComponentComposite = new SpeakerArrivalDepartureComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					speakerArrivalDepartureComponentComposite.addModifyListener(modifySupport);
					speakerArrivalDepartureComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				speakerArrivalDepartureComponentComposite.setEntity(component);
				stackLayout.topControl = speakerArrivalDepartureComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	public void setSpeakerRoomTypeComponent(SpeakerRoomTypeComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   speakerRoomTypeComponentComposite == null
				|| speakerRoomTypeComponentComposite != stackLayout.topControl
				|| speakerRoomTypeComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   speakerRoomTypeComponentComposite == null
					|| ! speakerRoomTypeComponentComposite.getEntity().getId().equals(component.getId())
					) {
					syncEntityToWidgets();
				}


				if (speakerRoomTypeComponentComposite == null) {
					speakerRoomTypeComponentComposite = new SpeakerRoomTypeComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					speakerRoomTypeComponentComposite.addModifyListener(modifySupport);
					speakerRoomTypeComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				speakerRoomTypeComponentComposite.setEntity(component);
				stackLayout.topControl = speakerRoomTypeComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	

	public void setPortalTableComponent(PortalTableComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   portalTableComponentComposite == null
				|| portalTableComponentComposite != stackLayout.topControl
				|| portalTableComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   portalTableComponentComposite == null
					|| ! portalTableComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


				if (portalTableComponentComposite == null) {
					portalTableComponentComposite = new PortalTableComponentComposite(
						contentComposite,
						SWT.NONE,
						portalPK
					);
					portalTableComponentComposite.addModifyListener(modifySupport);
					portalTableComponentComposite.setFixedStructure( page.isFixedStructure() );
				}
				portalTableComponentComposite.setEntity(component);
				stackLayout.topControl = portalTableComponentComposite;

				refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setSendRegistrationConfirmationComponent(SendRegistrationConfirmationComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   sendRegistrationConfirmationComponentComposite == null
				|| sendRegistrationConfirmationComponentComposite != stackLayout.topControl
				|| sendRegistrationConfirmationComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   sendRegistrationConfirmationComponentComposite == null
					|| ! sendRegistrationConfirmationComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (sendRegistrationConfirmationComponentComposite == null) {
        			sendRegistrationConfirmationComponentComposite = new SendRegistrationConfirmationComponentComposite(
        				contentComposite,
        				SWT.NONE,
        				portalPK
        			);
        			sendRegistrationConfirmationComponentComposite.addModifyListener(modifySupport);
        			sendRegistrationConfirmationComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		sendRegistrationConfirmationComponentComposite.setEntity(component);
        		stackLayout.topControl = sendRegistrationConfirmationComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setSendLetterOfInvitationComponent(SendLetterOfInvitationComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   sendLetterOfInvitationComponentComposite == null
				|| sendLetterOfInvitationComponentComposite != stackLayout.topControl
				|| sendLetterOfInvitationComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   sendLetterOfInvitationComponentComposite == null
					|| ! sendLetterOfInvitationComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (sendLetterOfInvitationComponentComposite == null) {
        			sendLetterOfInvitationComponentComposite = new SendLetterOfInvitationComponentComposite(
        				contentComposite,
        				SWT.NONE,
        				portalPK
        			);
        			sendLetterOfInvitationComponentComposite.addModifyListener(modifySupport);
        			sendLetterOfInvitationComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		sendLetterOfInvitationComponentComposite.setEntity(component);
        		stackLayout.topControl = sendLetterOfInvitationComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setRegistrationComponent(RegistrationComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   registrationComponentComposite == null
				|| registrationComponentComposite != stackLayout.topControl
				|| registrationComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   registrationComponentComposite == null
					|| ! registrationComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (registrationComponentComposite == null) {
        			registrationComponentComposite = new RegistrationComponentComposite(contentComposite, SWT.NONE, portalPK);
        			registrationComponentComposite.addModifyListener(modifySupport);
        			registrationComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		registrationComponentComposite.setEntity(component);
        		stackLayout.topControl = registrationComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setRegistrationStatusTextComponent(RegistrationStatusTextComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   registrationStatusTextComponentComposite == null
				|| registrationStatusTextComponentComposite != stackLayout.topControl
				|| registrationStatusTextComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   registrationStatusTextComponentComposite == null
					|| ! registrationStatusTextComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (registrationStatusTextComponentComposite == null) {
        			registrationStatusTextComponentComposite = new RegistrationStatusTextComponentComposite(contentComposite, SWT.NONE, portalPK);
        			registrationStatusTextComponentComposite.addModifyListener(modifySupport);
        			registrationStatusTextComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		registrationStatusTextComponentComposite.setEntity(component);
        		stackLayout.topControl = registrationStatusTextComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setRegistrationBookingTableComponent(RegistrationBookingTableComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   registrationBookingTableComponentComposite == null
				|| registrationBookingTableComponentComposite != stackLayout.topControl
				|| registrationBookingTableComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   registrationBookingTableComponentComposite == null
					|| ! registrationBookingTableComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (registrationBookingTableComponentComposite == null) {
        			registrationBookingTableComponentComposite = new RegistrationBookingTableComponentComposite(contentComposite, SWT.NONE, portalPK);
        			registrationBookingTableComponentComposite.addModifyListener(modifySupport);
        			registrationBookingTableComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		registrationBookingTableComponentComposite.setEntity(component);
        		stackLayout.topControl = registrationBookingTableComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setCreateHotelBookingComponent(CreateHotelBookingComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   createHotelBookingComponentComposite == null
				|| createHotelBookingComponentComposite != stackLayout.topControl
				|| createHotelBookingComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   createHotelBookingComponentComposite == null
					|| ! createHotelBookingComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (createHotelBookingComponentComposite == null) {
        			createHotelBookingComponentComposite = new CreateHotelBookingComponentComposite(contentComposite, SWT.NONE, portalPK);
        			createHotelBookingComponentComposite.addModifyListener(modifySupport);
        			createHotelBookingComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		createHotelBookingComponentComposite.setEntity(component);
        		stackLayout.topControl = createHotelBookingComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setManageAbstractSubmissionComponent(ManageAbstractSubmissionComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   manageAbstractSubmissionComponentComposite == null
				|| manageAbstractSubmissionComponentComposite != stackLayout.topControl
				|| manageAbstractSubmissionComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   manageAbstractSubmissionComponentComposite == null
					|| ! manageAbstractSubmissionComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (manageAbstractSubmissionComponentComposite == null) {
        			manageAbstractSubmissionComponentComposite = new ManageAbstractSubmissionComponentComposite(contentComposite, SWT.NONE, portalPK);
        			manageAbstractSubmissionComponentComposite.addModifyListener(modifySupport);
        			manageAbstractSubmissionComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		manageAbstractSubmissionComponentComposite.setEntity(component);
        		stackLayout.topControl = manageAbstractSubmissionComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setManageAbstractPaymentComponent(ManageAbstractPaymentComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   manageAbstractPaymentComponentComposite == null
				|| manageAbstractPaymentComponentComposite != stackLayout.topControl
				|| manageAbstractPaymentComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   manageAbstractPaymentComponentComposite == null
					|| ! manageAbstractPaymentComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (manageAbstractPaymentComponentComposite == null) {
        			manageAbstractPaymentComponentComposite = new ManageAbstractPaymentComponentComposite(contentComposite, SWT.NONE, portalPK);
        			manageAbstractPaymentComponentComposite.addModifyListener(modifySupport);
        			manageAbstractPaymentComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		manageAbstractPaymentComponentComposite.setEntity(component);
        		stackLayout.topControl = manageAbstractPaymentComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setManageAbstractTextComponent(ManageAbstractTextComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   manageAbstractTextComponentComposite == null
				|| manageAbstractTextComponentComposite != stackLayout.topControl
				|| manageAbstractTextComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   manageAbstractTextComponentComposite == null
					|| ! manageAbstractTextComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (manageAbstractTextComponentComposite == null) {
        			manageAbstractTextComponentComposite = new ManageAbstractTextComponentComposite(contentComposite, SWT.NONE, portalPK);
        			manageAbstractTextComponentComposite.addModifyListener(modifySupport);
        			manageAbstractTextComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		manageAbstractTextComponentComposite.setEntity(component);
        		stackLayout.topControl = manageAbstractTextComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setManageAbstractTableComponent(ManageAbstractTableComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   manageAbstractTableComponentComposite == null
				|| manageAbstractTableComponentComposite != stackLayout.topControl
				|| manageAbstractTableComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   manageAbstractTableComponentComposite == null
					|| ! manageAbstractTableComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (manageAbstractTableComponentComposite == null) {
        			manageAbstractTableComponentComposite = new ManageAbstractTableComponentComposite(contentComposite, SWT.NONE, portalPK);
        			manageAbstractTableComponentComposite.addModifyListener(modifySupport);
        			manageAbstractTableComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		manageAbstractTableComponentComposite.setEntity(component);
        		stackLayout.topControl = manageAbstractTableComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setDocumediasAbstractSubmissionComponent(DocumediasAbstractSubmissionComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   documediasAbstractSubmissionComponentComposite == null
				|| documediasAbstractSubmissionComponentComposite != stackLayout.topControl
				|| documediasAbstractSubmissionComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   documediasAbstractSubmissionComponentComposite == null
					|| ! documediasAbstractSubmissionComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (documediasAbstractSubmissionComponentComposite == null) {
        			documediasAbstractSubmissionComponentComposite = new DocumediasAbstractSubmissionComponentComposite(contentComposite, SWT.NONE, portalPK);
        			documediasAbstractSubmissionComponentComposite.addModifyListener(modifySupport);
        			documediasAbstractSubmissionComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		documediasAbstractSubmissionComponentComposite.setEntity(component);
        		stackLayout.topControl = documediasAbstractSubmissionComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setDocumediasAbstractPaymentComponent(DocumediasAbstractPaymentComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   documediasAbstractPaymentComponentComposite == null
				|| documediasAbstractPaymentComponentComposite != stackLayout.topControl
				|| documediasAbstractPaymentComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   documediasAbstractPaymentComponentComposite == null
					|| ! documediasAbstractPaymentComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (documediasAbstractPaymentComponentComposite == null) {
        			documediasAbstractPaymentComponentComposite = new DocumediasAbstractPaymentComponentComposite(contentComposite, SWT.NONE, portalPK);
        			documediasAbstractPaymentComponentComposite.addModifyListener(modifySupport);
        			documediasAbstractPaymentComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		documediasAbstractPaymentComponentComposite.setEntity(component);
        		stackLayout.topControl = documediasAbstractPaymentComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setDocumediasAbstractTextComponent(DocumediasAbstractTextComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   documediasAbstractTextComponentComposite == null
				|| documediasAbstractTextComponentComposite != stackLayout.topControl
				|| documediasAbstractTextComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   documediasAbstractTextComponentComposite == null
					|| ! documediasAbstractTextComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (documediasAbstractTextComponentComposite == null) {
        			documediasAbstractTextComponentComposite = new DocumediasAbstractTextComponentComposite(contentComposite, SWT.NONE, portalPK);
        			documediasAbstractTextComponentComposite.addModifyListener(modifySupport);
        			documediasAbstractTextComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		documediasAbstractTextComponentComposite.setEntity(component);
        		stackLayout.topControl = documediasAbstractTextComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setDocumediasAbstractTableComponent(DocumediasAbstractTableComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   documediasAbstractTableComponentComposite == null
				|| documediasAbstractTableComponentComposite != stackLayout.topControl
				|| documediasAbstractTableComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   documediasAbstractTableComponentComposite == null
					|| ! documediasAbstractTableComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (documediasAbstractTableComponentComposite == null) {
        			documediasAbstractTableComponentComposite = new DocumediasAbstractTableComponentComposite(contentComposite, SWT.NONE, portalPK);
        			documediasAbstractTableComponentComposite.addModifyListener(modifySupport);
        			documediasAbstractTableComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		documediasAbstractTableComponentComposite.setEntity(component);
        		stackLayout.topControl = documediasAbstractTableComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setCertificateComponent(CertificateComponent component) {
		try {
			if (   certificateComponentComposite == null
				|| certificateComponentComposite != stackLayout.topControl
				|| certificateComponentComposite.getEntity() != component
			) {
				if (   certificateComponentComposite == null
					|| ! certificateComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}

        		if (certificateComponentComposite == null) {
        			certificateComponentComposite = new CertificateComponentComposite(contentComposite, SWT.NONE, portalPK);
        			certificateComponentComposite.addModifyListener(modifySupport);
        			certificateComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		certificateComponentComposite.setEntity(component);
        		stackLayout.topControl = certificateComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEcfsMembershipComponent(EcfsMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   ecfsMembershipComponentComposite == null
				|| ecfsMembershipComponentComposite != stackLayout.topControl
				|| ecfsMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   ecfsMembershipComponentComposite == null
					|| ! ecfsMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (ecfsMembershipComponentComposite == null) {
        			ecfsMembershipComponentComposite = new EcfsMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			ecfsMembershipComponentComposite.addModifyListener(modifySupport);
        			ecfsMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		ecfsMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = ecfsMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEficMembershipComponent(EficMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   eficMembershipComponentComposite == null
				|| eficMembershipComponentComposite != stackLayout.topControl
				|| eficMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   eficMembershipComponentComposite == null
					|| ! eficMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (eficMembershipComponentComposite == null) {
        			eficMembershipComponentComposite = new EficMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			eficMembershipComponentComposite.addModifyListener(modifySupport);
        			eficMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		eficMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = eficMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEnetsMembershipComponent(EnetsMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   enetsMembershipComponentComposite == null
				|| enetsMembershipComponentComposite != stackLayout.topControl
				|| enetsMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   enetsMembershipComponentComposite == null
					|| ! enetsMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (enetsMembershipComponentComposite == null) {
        			enetsMembershipComponentComposite = new EnetsMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			enetsMembershipComponentComposite.addModifyListener(modifySupport);
        			enetsMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		enetsMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = enetsMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEsicmMembershipComponent(EsicmMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   esicmMembershipComponentComposite == null
				|| esicmMembershipComponentComposite != stackLayout.topControl
				|| esicmMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   esicmMembershipComponentComposite == null
					|| ! esicmMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (esicmMembershipComponentComposite == null) {
        			esicmMembershipComponentComposite = new EsicmMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			esicmMembershipComponentComposite.addModifyListener(modifySupport);
        			esicmMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		esicmMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = esicmMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEsraMembershipComponent(EsraMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   esraMembershipComponentComposite == null
				|| esraMembershipComponentComposite != stackLayout.topControl
				|| esraMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   esraMembershipComponentComposite == null
					|| ! esraMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (esraMembershipComponentComposite == null) {
        			esraMembershipComponentComposite = new EsraMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			esraMembershipComponentComposite.addModifyListener(modifySupport);
        			esraMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		esraMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = esraMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEsskaMembershipComponent(EsskaMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   esskaMembershipComponentComposite == null
				|| esskaMembershipComponentComposite != stackLayout.topControl
				|| esskaMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   esskaMembershipComponentComposite == null
					|| ! esskaMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (esskaMembershipComponentComposite == null) {
        			esskaMembershipComponentComposite = new EsskaMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			esskaMembershipComponentComposite.addModifyListener(modifySupport);
        			esskaMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		esskaMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = esskaMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public void setFensMembershipComponent(FensMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   fensMembershipComponentComposite == null
				|| fensMembershipComponentComposite != stackLayout.topControl
				|| fensMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   fensMembershipComponentComposite == null
					|| ! fensMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (fensMembershipComponentComposite == null) {
        			fensMembershipComponentComposite = new FensMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			fensMembershipComponentComposite.addModifyListener(modifySupport);
        			fensMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		fensMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = fensMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setIflaMembershipComponent(IflaMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   iflaMembershipComponentComposite == null
				|| iflaMembershipComponentComposite != stackLayout.topControl
				|| iflaMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   iflaMembershipComponentComposite == null
					|| ! iflaMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (iflaMembershipComponentComposite == null) {
        			iflaMembershipComponentComposite = new IflaMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			iflaMembershipComponentComposite.addModifyListener(modifySupport);
        			iflaMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		iflaMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = iflaMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setIltsMembershipComponent(IltsMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   iltsMembershipComponentComposite == null
				|| iltsMembershipComponentComposite != stackLayout.topControl
				|| iltsMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   iltsMembershipComponentComposite == null
					|| ! iltsMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (iltsMembershipComponentComposite == null) {
        			iltsMembershipComponentComposite = new IltsMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			iltsMembershipComponentComposite.addModifyListener(modifySupport);
        			iltsMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		iltsMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = iltsMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setIspadMembershipComponent(IspadMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   ispadMembershipComponentComposite == null
				|| ispadMembershipComponentComposite != stackLayout.topControl
				|| ispadMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   ispadMembershipComponentComposite == null
					|| ! ispadMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (ispadMembershipComponentComposite == null) {
        			ispadMembershipComponentComposite = new IspadMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			ispadMembershipComponentComposite.addModifyListener(modifySupport);
        			ispadMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		ispadMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = ispadMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setNeurosciencesMembershipComponent(NeurosciencesMembershipComponent component) {
		try {
			// avoid to do anything if Component did not change
			if (   neurosciencesMembershipComponentComposite == null
				|| neurosciencesMembershipComponentComposite != stackLayout.topControl
				|| neurosciencesMembershipComponentComposite.getEntity() != component
			) {
				/* Copy values from widgets to old Component. But only if the new Component has a different identity.
				 * If the identity is the same, we can skip this step, because we don't need to sync the old version
				 * of the same Component.
				 */
				if (   neurosciencesMembershipComponentComposite == null
					|| ! neurosciencesMembershipComponentComposite.getEntity().getId().equals(component.getId())
				) {
					syncEntityToWidgets();
				}


        		if (neurosciencesMembershipComponentComposite == null) {
        			neurosciencesMembershipComponentComposite = new NeurosciencesMembershipComponentComposite(contentComposite, SWT.NONE, portalPK);
        			neurosciencesMembershipComponentComposite.addModifyListener(modifySupport);
        			neurosciencesMembershipComponentComposite.setFixedStructure( page.isFixedStructure() );
        		}
        		neurosciencesMembershipComponentComposite.setEntity(component);
        		stackLayout.topControl = neurosciencesMembershipComponentComposite;

        		refreshScrollbars();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void syncEntityToWidgets() {
		if (stackLayout.topControl != null) {
    		if (stackLayout.topControl == sectionComposite) {
    			sectionComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == scriptComponentComposite) {
    			scriptComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == textComponentComposite) {
    			textComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == profileFieldComponentComposite) {
    			profileFieldComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == participantFieldComponentComposite) {
    			participantFieldComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == emailComponentComposite) {
				emailComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == fileComponentComposite) {
    			fileComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == programmeBookingComponentComposite) {
    			programmeBookingComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == openAmountComponentComposite) {
    			openAmountComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == totalAmountComponentComposite) {
    			totalAmountComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == streamComponentComposite) {
    			streamComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == paymentComponentComposite) {
    			paymentComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == paymentWithFeeComponentComposite) {
    			paymentWithFeeComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == printComponentComposite) {
    			printComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == digitalEventComponentComposite) {
    			digitalEventComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == summaryComponentComposite) {
    			summaryComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == uploadComponentComposite) {
    			uploadComponentComposite.syncEntityToWidgets();
    		}

    		// Component Composites of Group Portal
    		else if (stackLayout.topControl == groupMemberTableComponentComposite) {
    			groupMemberTableComponentComposite.syncEntityToWidgets();
    		}

    		// Component Composites of Hotel Portal
    		else if (stackLayout.topControl == hotelSearchCriteriaComponentComposite) {
    			hotelSearchCriteriaComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == hotelSearchFilterComponentComposite) {
    			hotelSearchFilterComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == hotelSearchResultComponentComposite) {
    			hotelSearchResultComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == hotelDetailsComponentComposite) {
    			hotelDetailsComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == hotelBookingComponentComposite) {
    			hotelBookingComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == hotelBookingTableComponentComposite) {
    			hotelBookingTableComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == editBookingComponentComposite) {
    			editBookingComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == hotelTotalAmountComponentComposite) {
    			hotelTotalAmountComponentComposite.syncEntityToWidgets();
    		}
    		
    		// Component Composites of Hotel Speaker Portals
    		else if (stackLayout.topControl == speakerArrivalDepartureComponentComposite) {
    			speakerArrivalDepartureComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == speakerRoomTypeComponentComposite) {
    			speakerRoomTypeComponentComposite.syncEntityToWidgets();
    		}

    		// Component Composites of Profile Portals
    		else if (stackLayout.topControl == portalTableComponentComposite) {
    			portalTableComponentComposite.syncEntityToWidgets();
    		}

    		// Component Composites of REACT Profile Portal
    		else if (stackLayout.topControl == registrationComponentComposite) {
    			registrationComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == registrationStatusTextComponentComposite) {
    			registrationStatusTextComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == registrationBookingTableComponentComposite) {
    			registrationBookingTableComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == createHotelBookingComponentComposite) {
    			createHotelBookingComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == sendRegistrationConfirmationComponentComposite) {
				sendRegistrationConfirmationComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == sendLetterOfInvitationComponentComposite) {
				sendLetterOfInvitationComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == manageAbstractSubmissionComponentComposite) {
    			manageAbstractSubmissionComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == manageAbstractPaymentComponentComposite) {
    			manageAbstractPaymentComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == manageAbstractTextComponentComposite) {
    			manageAbstractTextComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == manageAbstractTableComponentComposite) {
    			manageAbstractTableComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == documediasAbstractSubmissionComponentComposite) {
				documediasAbstractSubmissionComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == documediasAbstractPaymentComponentComposite) {
				documediasAbstractPaymentComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == documediasAbstractTextComponentComposite) {
				documediasAbstractTextComponentComposite.syncEntityToWidgets();
			}
    		else if (stackLayout.topControl == documediasAbstractTableComponentComposite) {
    			documediasAbstractTableComponentComposite.syncEntityToWidgets();
    		}

    		// Component Composites of React Certificate Portal
    		else if (stackLayout.topControl == certificateComponentComposite) {
				certificateComponentComposite.syncEntityToWidgets();
			}

    		// Component Composites of Membership Components
    		else if (stackLayout.topControl == ecfsMembershipComponentComposite) {
    			ecfsMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == eficMembershipComponentComposite) {
    			eficMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == enetsMembershipComponentComposite) {
    			enetsMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == esicmMembershipComponentComposite) {
    			esicmMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == esraMembershipComponentComposite) {
    			esraMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == esskaMembershipComponentComposite) {
    			esskaMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == fensMembershipComponentComposite) {
    			fensMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == iflaMembershipComponentComposite) {
    			iflaMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == iltsMembershipComponentComposite) {
    			iltsMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == ispadMembershipComponentComposite) {
    			ispadMembershipComponentComposite.syncEntityToWidgets();
    		}
    		else if (stackLayout.topControl == neurosciencesMembershipComponentComposite) {
    			neurosciencesMembershipComponentComposite.syncEntityToWidgets();
    		}
		}
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
