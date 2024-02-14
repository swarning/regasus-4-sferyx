package de.regasus.portal.page.editor;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
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
import de.regasus.portal.pagelayout.combo.PageLayoutCombo;
import de.regasus.ui.Activator;

public class PageContentComposite extends Composite {

	// the entity
	private Page page;
	private Long portalId;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private PageLayoutCombo pageLayoutCombo;
	private I18NComposite<Page> i18nComposite;

	private PageContentTreeComposite treeComposite;
	private PageContentDetailComposite detailsComposite;

	// *
	// * Widgets
	// **************************************************************************


	public PageContentComposite(Composite parent, int style, Long portalId, List<Language> languageList)
	throws Exception {
		super(parent, style);

		this.portalId = Objects.requireNonNull(portalId);
		this.languageList = languageList;

		createWidgets();
	}


	private void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		final int COL_COUNT = 2;
		setLayout(new GridLayout(COL_COUNT, false));


		/*** page layout ***/
		{
    		SWTHelper.createLabel(this, Page.PAGE_LAYOUT.getString(), true);

    		pageLayoutCombo = new PageLayoutCombo(this, SWT.BORDER);
    		GridDataFactory.swtDefaults().align(SWT.FILL,  SWT.CENTER).grab(true, false).applyTo(pageLayoutCombo);
    		SWTHelper.makeBold(pageLayoutCombo);
    		pageLayoutCombo.setWithEmptyElement(false);
    		pageLayoutCombo.addModifyListener(modifySupport);
		}

		/*** I18N widgets ***/
		{
    		i18nComposite = new I18NComposite<>(this, SWT.BORDER, languageList, new PageTitleI18NWidgetController());
    		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(0, 10).applyTo(i18nComposite);
    		i18nComposite.addModifyListener(modifySupport);
		}


		/*** PageContentTreeComposite ***/
		{
    		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
    		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).indent(0, 10).applyTo(sashForm);

    		treeComposite = new PageContentTreeComposite(sashForm, SWT.BORDER, portalId);
    		detailsComposite = new PageContentDetailComposite(sashForm, SWT.BORDER);
    		treeComposite.addSelectionChangedListener(selectionChangedListener);

    		treeComposite.addModifyListener(modifySupport);
    		detailsComposite.addModifyListener(modifySupport);

    		sashForm.setWeights(new int[] { 1, 3 });
		}
	}


	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

		Object currentSelection = null;

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			Object selectedObject = SelectionHelper.getUniqueSelected( event.getSelection() );

			if (selectedObject != currentSelection) {
				currentSelection = selectedObject;

				if (selectedObject instanceof Section) {
					detailsComposite.setSection( (Section) selectedObject );
				}
				else if (selectedObject instanceof DigitalEventComponent) {
					detailsComposite.setDigitalEventComponent( (DigitalEventComponent) selectedObject );
				}
				else if (selectedObject instanceof ProfileFieldComponent) {
					detailsComposite.setProfileFieldComponent( (ProfileFieldComponent) selectedObject );
				}
				else if (selectedObject instanceof ParticipantFieldComponent) {
					detailsComposite.setParticipantFieldComponent( (ParticipantFieldComponent) selectedObject );
				}
				else if (selectedObject instanceof EmailComponent) {
					detailsComposite.setEmailComponent( (EmailComponent) selectedObject );
				}
				else if (selectedObject instanceof FileComponent) {
					detailsComposite.setFileComponent( (FileComponent) selectedObject );
				}
				else if (selectedObject instanceof PaymentComponent) {
					detailsComposite.setPaymentComponent( (PaymentComponent) selectedObject );
				}
				else if (selectedObject instanceof PaymentWithFeeComponent) {
					detailsComposite.setPaymentWithFeeComponent( (PaymentWithFeeComponent) selectedObject );
				}
				else if (selectedObject instanceof PrintComponent) {
					detailsComposite.setPrintComponent( (PrintComponent) selectedObject );
				}
				else if (selectedObject instanceof ProgrammeBookingComponent) {
					detailsComposite.setProgrammeBookingComponent( (ProgrammeBookingComponent) selectedObject );
				}
				else if (selectedObject instanceof OpenAmountComponent) {
					detailsComposite.setOpenAmountComponent( (OpenAmountComponent) selectedObject );
				}
				else if (selectedObject instanceof TotalAmountComponent) {
					detailsComposite.setTotalAmountComponent( (TotalAmountComponent) selectedObject );
				}
				else if (selectedObject instanceof StreamComponent) {
					detailsComposite.setStreamComponent( (StreamComponent) selectedObject );
				}
				else if (selectedObject instanceof SummaryComponent) {
					detailsComposite.setSummaryComponent( (SummaryComponent) selectedObject );
				}
				else if (selectedObject instanceof ScriptComponent) {
					detailsComposite.setScriptComponent( (ScriptComponent) selectedObject );
				}
				else if (selectedObject instanceof TextComponent) {
					detailsComposite.setTextComponent( (TextComponent) selectedObject );
				}
				else if (selectedObject instanceof UploadComponent) {
					detailsComposite.setUploadComponent( (UploadComponent) selectedObject );
				}
				else if (selectedObject instanceof SendRegistrationConfirmationComponent) {
					detailsComposite.setSendRegistrationConfirmationComponent( (SendRegistrationConfirmationComponent) selectedObject );
				}
				else if (selectedObject instanceof SendLetterOfInvitationComponent) {
					detailsComposite.setSendLetterOfInvitationComponent( (SendLetterOfInvitationComponent) selectedObject );
				}

				// Component Composites of Group Portal
				else if (selectedObject instanceof GroupMemberTableComponent) {
					detailsComposite.setGroupMemberTableComponent( (GroupMemberTableComponent) selectedObject );
				}

				// Component Composites of Hotel Portal
				else if (selectedObject instanceof HotelSearchCriteriaComponent) {
					detailsComposite.setHotelSearchCriteriaComponent( (HotelSearchCriteriaComponent) selectedObject );
				}
				else if (selectedObject instanceof HotelSearchFilterComponent) {
					detailsComposite.setHotelSearchFilterComponent( (HotelSearchFilterComponent) selectedObject );
				}
				else if (selectedObject instanceof HotelSearchResultComponent) {
					detailsComposite.setHotelSearchResultComponent( (HotelSearchResultComponent) selectedObject );
				}
				else if (selectedObject instanceof HotelDetailsComponent) {
					detailsComposite.setHotelDetailsComponent( (HotelDetailsComponent) selectedObject );
				}
				else if (selectedObject instanceof HotelBookingComponent) {
					detailsComposite.setHotelBookingComponent( (HotelBookingComponent) selectedObject );
				}
				else if (selectedObject instanceof HotelBookingTableComponent) {
					detailsComposite.setHotelBookingTableComponent( (HotelBookingTableComponent) selectedObject );
				}
				else if (selectedObject instanceof EditBookingComponent) {
					detailsComposite.setEditBookingComponent( (EditBookingComponent) selectedObject );
				}
				else if (selectedObject instanceof HotelTotalAmountComponent) {
					detailsComposite.setHotelTotalAmountComponent( (HotelTotalAmountComponent) selectedObject );
				}

				// Component Composites of Hotel Speaker Portal
				else if (selectedObject instanceof SpeakerArrivalDepartureComponent) {
					detailsComposite.setSpeakerArrivalDepartureComponent( (SpeakerArrivalDepartureComponent) selectedObject );
				}
				else if (selectedObject instanceof SpeakerRoomTypeComponent) {
					detailsComposite.setSpeakerRoomTypeComponent( (SpeakerRoomTypeComponent) selectedObject );
				}

				// Component Composites of Profile Portals
				else if (selectedObject instanceof PortalTableComponent) {
					detailsComposite.setPortalTableComponent( (PortalTableComponent) selectedObject );
				}

				// Component Composites of REACT Profile Portal
				else if (selectedObject instanceof RegistrationComponent) {
					detailsComposite.setRegistrationComponent( (RegistrationComponent) selectedObject );
				}
				else if (selectedObject instanceof RegistrationStatusTextComponent) {
					detailsComposite.setRegistrationStatusTextComponent( (RegistrationStatusTextComponent) selectedObject );
				}
				else if (selectedObject instanceof RegistrationBookingTableComponent) {
					detailsComposite.setRegistrationBookingTableComponent( (RegistrationBookingTableComponent) selectedObject );
				}

				else if (selectedObject instanceof CreateHotelBookingComponent) {
					detailsComposite.setCreateHotelBookingComponent( (CreateHotelBookingComponent) selectedObject );
				}

				else if (selectedObject instanceof ManageAbstractSubmissionComponent) {
					detailsComposite.setManageAbstractSubmissionComponent( (ManageAbstractSubmissionComponent) selectedObject );
				}
				else if (selectedObject instanceof ManageAbstractPaymentComponent) {
					detailsComposite.setManageAbstractPaymentComponent( (ManageAbstractPaymentComponent) selectedObject);
				}
				else if (selectedObject instanceof ManageAbstractTextComponent) {
					detailsComposite.setManageAbstractTextComponent( (ManageAbstractTextComponent) selectedObject );
				}
				else if (selectedObject instanceof ManageAbstractTableComponent) {
					detailsComposite.setManageAbstractTableComponent( (ManageAbstractTableComponent) selectedObject );
				}

				else if (selectedObject instanceof DocumediasAbstractSubmissionComponent) {
					detailsComposite.setDocumediasAbstractSubmissionComponent( (DocumediasAbstractSubmissionComponent) selectedObject );
				}
				else if (selectedObject instanceof DocumediasAbstractPaymentComponent) {
					detailsComposite.setDocumediasAbstractPaymentComponent( (DocumediasAbstractPaymentComponent) selectedObject);
				}
				else if (selectedObject instanceof DocumediasAbstractTextComponent) {
					detailsComposite.setDocumediasAbstractTextComponent( (DocumediasAbstractTextComponent) selectedObject );
				}
				else if (selectedObject instanceof DocumediasAbstractTableComponent) {
					detailsComposite.setDocumediasAbstractTableComponent( (DocumediasAbstractTableComponent) selectedObject );
				}

				// Component Composites of REACT Certificate Portal
				else if (selectedObject instanceof CertificateComponent) {
					detailsComposite.setCertificateComponent( (CertificateComponent) selectedObject );
				}

	    		// Component Composites of Membership Components
				else if (selectedObject instanceof EcfsMembershipComponent) {
					detailsComposite.setEcfsMembershipComponent( (EcfsMembershipComponent) selectedObject);
				}
				else if (selectedObject instanceof EficMembershipComponent) {
					detailsComposite.setEficMembershipComponent( (EficMembershipComponent) selectedObject);
				}
				else if (selectedObject instanceof EnetsMembershipComponent) {
					detailsComposite.setEnetsMembershipComponent( (EnetsMembershipComponent) selectedObject);
				}
				else if (selectedObject instanceof EsicmMembershipComponent) {
					detailsComposite.setEsicmMembershipComponent( (EsicmMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof EsraMembershipComponent) {
					detailsComposite.setEsraMembershipComponent( (EsraMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof EsskaMembershipComponent) {
					detailsComposite.setEsskaMembershipComponent( (EsskaMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof FensMembershipComponent) {
					detailsComposite.setFensMembershipComponent( (FensMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof IflaMembershipComponent) {
					detailsComposite.setIflaMembershipComponent( (IflaMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof IltsMembershipComponent) {
					detailsComposite.setIltsMembershipComponent( (IltsMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof IspadMembershipComponent) {
					detailsComposite.setIspadMembershipComponent( (IspadMembershipComponent) selectedObject );
				}
				else if (selectedObject instanceof NeurosciencesMembershipComponent) {
					detailsComposite.setNeurosciencesMembershipComponent( (NeurosciencesMembershipComponent) selectedObject );
				}
			}

			// show eventually modified Section or Component in tree
			treeComposite.refresh();
		}
	};


	@Override
	public boolean setFocus() {
		return pageLayoutCombo.setFocus();
	}


	public void setPage(Page page) {
		this.page = page;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (page != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						pageLayoutCombo.setPortalId( page.getPortalId() );
						pageLayoutCombo.setPageLayoutId( page.getPageLayoutId() );

						i18nComposite.setEntity(page);

						treeComposite.setPage(page);
						detailsComposite.setPage(page);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		page.setPageLayoutId( pageLayoutCombo.getPageLayoutId() );
		i18nComposite.syncEntityToWidgets();
		treeComposite.syncEntityToWidgets();
		detailsComposite.syncEntityToWidgets();
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
