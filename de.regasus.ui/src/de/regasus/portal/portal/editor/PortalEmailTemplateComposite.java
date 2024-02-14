package de.regasus.portal.portal.editor;

import static com.lambdalogic.util.CollectionsHelper.createHashSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.BeanHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalConfig;
import de.regasus.portal.type.react.registration.ReactRegistrationPortalConfig;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalConfig;
import de.regasus.ui.Activator;


public class PortalEmailTemplateComposite extends Composite {


	private static final String REGISTRATION_CONFIRMATION_EMAIL_ON_SUMMARY_PAGE = "registrationConfirmationEmailOnSummaryPage";
	private static final String REGISTRATION_CONFIRMATION_EMAIL_AFTER_PAYMENT = "registrationConfirmationEmailAfterPayment";

	// the entity
	private Portal portal;

	private EmailTemplateModel emailTemplateModel;

	private ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private PortalEmailTemplateTable emailTemplateTable;
	private CheckboxTableViewer tableViewer;
	private Button sendEmailButton;
	private Button registrationConfirmationEmailOnSummaryPageButton;
	private GridData registrationConfirmationEmailOnSummaryPageGridData;
	private boolean hasRegistrationConfirmationEmailOnSummaryPageConfiguration;
	private Button registrationConfirmationEmailAfterPaymentButton;
	private GridData registrationConfirmationEmailAfterPaymentGridData;
	private boolean hasRegistrationConfirmationEmailAfterPaymentConfiguration;

	// *
	// * Widgets
	// **************************************************************************


	public PortalEmailTemplateComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		emailTemplateModel = EmailTemplateModel.getInstance();

		createWidgets();
	}


	private CacheModelListener<Long> emailTemplateModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Object[] checkedEmailTemplates = tableViewer.getCheckedElements();
						initEmailTemplateTable();
						tableViewer.setCheckedElements(checkedEmailTemplates);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	};


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			try {
				if (portal.getEventId() != null) {
					emailTemplateModel.removeForeignKeyListener(emailTemplateModelListener, portal.getEventId());
				}
				else {
					emailTemplateModel.removeListener(emailTemplateModelListener);
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	};


	private void createWidgets() throws Exception {
		this.addDisposeListener(disposeListener);

		setLayout(new GridLayout(1, false));


		/*** sendEmail Button ***/
		sendEmailButton = new Button(this, SWT.CHECK);
		sendEmailButton.setText( Portal.SEND_EMAIL.getString() );
		sendEmailButton.setToolTipText(I18N.PortalEditor_SendEmailButtonDescription);
		sendEmailButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false) );
		sendEmailButton.addSelectionListener(modifySupport);
		
		/*** registrationConfirmationEmailOnSummaryPage Button ***/
		registrationConfirmationEmailOnSummaryPageButton = new Button(this, SWT.CHECK);
		registrationConfirmationEmailOnSummaryPageButton.setText( ReactRegistrationPortalConfig.REGISTRATION_CONFIRMATION_EMAIL_ON_SUMMARY_PAGE.getString() );
		registrationConfirmationEmailOnSummaryPageGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		registrationConfirmationEmailOnSummaryPageButton.setLayoutData( registrationConfirmationEmailOnSummaryPageGridData );
		registrationConfirmationEmailOnSummaryPageButton.setVisible(hasRegistrationConfirmationEmailAfterPaymentConfiguration);
		registrationConfirmationEmailOnSummaryPageButton.addSelectionListener(modifySupport);

		/*** registrationConfirmationEmailAfterPayment Button ***/
		registrationConfirmationEmailAfterPaymentButton = new Button(this, SWT.CHECK);
		registrationConfirmationEmailAfterPaymentButton.setText( StandardRegistrationPortalConfig.REGISTRATION_CONFIRMATION_EMAIL_AFTER_PAYMENT.getString() );
		registrationConfirmationEmailAfterPaymentGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		registrationConfirmationEmailAfterPaymentButton.setLayoutData( registrationConfirmationEmailAfterPaymentGridData );
		registrationConfirmationEmailAfterPaymentButton.setVisible(hasRegistrationConfirmationEmailAfterPaymentConfiguration);
		registrationConfirmationEmailAfterPaymentButton.addSelectionListener(modifySupport);

		/*** explanation text ***/
		Label descriptionLabel = new Label(this, SWT.WRAP);
		GridDataFactory.swtDefaults()
			.align(SWT.LEFT,  SWT.CENTER)
			.grab(true, false)
			.indent(0, 10)
			.applyTo(descriptionLabel);

		descriptionLabel.setText(I18N.PortalEditor_EmailTemplateTableDescription);


		/*** table ***/
		Table table = new Table(this, SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK);
		table.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// SYSTEM_ROLE_NAME
		TableColumn systemRoleNameTableColumn = new TableColumn(table, SWT.NONE);
		systemRoleNameTableColumn.setWidth(200);
		systemRoleNameTableColumn.setText(EmailLabel.EmailTemplateSystemRole.getString());

		// EMAIL_TEMPLATE_NAME
		TableColumn emailTemplateNameTableColumn = new TableColumn(table, SWT.NONE);
		emailTemplateNameTableColumn.setWidth(600);
		emailTemplateNameTableColumn.setText(EmailLabel.EmailTemplate.getString());

		// LANGUAGE
		TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		languageTableColumn.setWidth(200);
		languageTableColumn.setText( Person.LANGUAGE_CODE.getLabel() );


		emailTemplateTable = new PortalEmailTemplateTable(table);
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.addCheckStateListener(checkStateListener);
		tableViewer.addDoubleClickListener(doubleClickListener);
	}


	private ICheckStateListener checkStateListener = new ICheckStateListener() {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			if ( event.getChecked() ) {
				// uncheck other Email Templates with same system role and same language
				EmailTemplate checkedEmailTemplate = (EmailTemplate) event.getElement();

				Object[] elements = tableViewer.getCheckedElements();
				for (Object element : elements) {
					EmailTemplate emailTemplate = (EmailTemplate) element;
					if (   emailTemplate != checkedEmailTemplate
						&& emailTemplate.getSystemRole() == checkedEmailTemplate.getSystemRole()
						&& EqualsHelper.isEqual(emailTemplate.getLanguage(), checkedEmailTemplate.getLanguage())
					) {
						tableViewer.setChecked(emailTemplate, false);
					}
				}
			}


			modifySupport.fire();
		}
	};


	private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			// TODO: open Email Template Editor
//			EmailTemplate emailTemplate = SelectionHelper.getUniqueSelected(event.getSelection());
//			EmailTemplateEditorInput editorInput = EmailTemplateEditorInput.getEditInstance( emailTemplate.getID() );
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, EventEditor.ID);
		}
	};


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
		if (portal != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						updateButtons();

						sendEmailButton.setSelection( portal.isSendEmail() );
						
						if (hasRegistrationConfirmationEmailOnSummaryPageConfiguration) {
							registrationConfirmationEmailOnSummaryPageButton.setSelection(
								getRegistrationConfirmationEmailOnSummaryPage()
							);
						}

						if (hasRegistrationConfirmationEmailAfterPaymentConfiguration) {
							registrationConfirmationEmailAfterPaymentButton.setSelection(
								getRegistrationConfirmationEmailAfterPayment()
							);
						}
						
						Set<Long> emailTemplateIds = portal.getEmailTemplateIds();
						tableViewer.setAllChecked(false);
						for (Long emailTemplateId : emailTemplateIds) {
							EmailTemplate emailTemplate = EmailTemplateModel.getInstance().getEmailTemplate(emailTemplateId);
							tableViewer.setChecked(emailTemplate, true);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void updateButtons() {
		Class<? extends PortalConfig> portalConfigClazz = portal.getPortalConfig().getClass();
		
		Field registrationConfirmationEmailOnSummaryPageField = BeanHelper.getDeclaredField(portalConfigClazz, REGISTRATION_CONFIRMATION_EMAIL_ON_SUMMARY_PAGE);
		hasRegistrationConfirmationEmailOnSummaryPageConfiguration = registrationConfirmationEmailOnSummaryPageField != null;
		registrationConfirmationEmailOnSummaryPageButton.setVisible(hasRegistrationConfirmationEmailOnSummaryPageConfiguration);

		if (!hasRegistrationConfirmationEmailOnSummaryPageConfiguration) {
			registrationConfirmationEmailOnSummaryPageGridData.heightHint = 0;
		}
		else {
			registrationConfirmationEmailOnSummaryPageGridData.heightHint = -1;
		}		

		Field registrationConfirmationEmailAfterPaymentField = BeanHelper.getDeclaredField(portalConfigClazz, REGISTRATION_CONFIRMATION_EMAIL_AFTER_PAYMENT);
		hasRegistrationConfirmationEmailAfterPaymentConfiguration = registrationConfirmationEmailAfterPaymentField != null;
		registrationConfirmationEmailAfterPaymentButton.setVisible(hasRegistrationConfirmationEmailAfterPaymentConfiguration);

		if (!hasRegistrationConfirmationEmailAfterPaymentConfiguration) {
			registrationConfirmationEmailAfterPaymentGridData.heightHint = 0;
		}
		else {
			registrationConfirmationEmailAfterPaymentGridData.heightHint = -1;
		}

		layout();
	}
	
	
	private boolean getRegistrationConfirmationEmailOnSummaryPage() {
		boolean value = false;
		Field field = BeanHelper.getDeclaredField(portal.getPortalConfig().getClass(), REGISTRATION_CONFIRMATION_EMAIL_ON_SUMMARY_PAGE);
		if (field != null) {
			try {
				field.setAccessible(true);
				value = field.getBoolean(portal.getPortalConfig());
			}
			catch (IllegalAccessException | IllegalArgumentException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		return value;
	}


	private boolean getRegistrationConfirmationEmailAfterPayment() {
		boolean value = false;
		Field field = BeanHelper.getDeclaredField(portal.getPortalConfig().getClass(), REGISTRATION_CONFIRMATION_EMAIL_AFTER_PAYMENT);
		if (field != null) {
			try {
				field.setAccessible(true);
				value = field.getBoolean(portal.getPortalConfig());
			}
			catch (IllegalAccessException | IllegalArgumentException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		return value;
	}


	public void syncEntityToWidgets() {
		try {
			if (portal != null) {
				portal.setSendEmail( sendEmailButton.getSelection() );

				Object[] checkedEmailTemplates = tableViewer.getCheckedElements();
				Set<Long> checkedEmailTemplateIds = createHashSet( checkedEmailTemplates.length );
				for (Object o : checkedEmailTemplates) {
					EmailTemplate emailTemplate = (EmailTemplate) o;
					checkedEmailTemplateIds.add( emailTemplate.getID() );
				}

				portal.setEmailTemplateIds(checkedEmailTemplateIds);
				
				if (hasRegistrationConfirmationEmailOnSummaryPageConfiguration) {
					setRegistrationConfirmationEmailOnSummaryPage(
						registrationConfirmationEmailOnSummaryPageButton.getSelection()
					);
				}

				if (hasRegistrationConfirmationEmailAfterPaymentConfiguration) {
					setRegistrationConfirmationEmailAfterPayment(
						registrationConfirmationEmailAfterPaymentButton.getSelection()
					);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	private void setRegistrationConfirmationEmailOnSummaryPage(boolean selected) {
		Field field = BeanHelper.getDeclaredField(portal.getPortalConfig().getClass(), REGISTRATION_CONFIRMATION_EMAIL_ON_SUMMARY_PAGE);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.setBoolean(portal.getPortalConfig(), selected);
			}
			catch (IllegalAccessException | IllegalArgumentException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}
	

	private void setRegistrationConfirmationEmailAfterPayment(boolean selected) {
		Field field = BeanHelper.getDeclaredField(portal.getPortalConfig().getClass(), REGISTRATION_CONFIRMATION_EMAIL_AFTER_PAYMENT);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.setBoolean(portal.getPortalConfig(), selected);
			}
			catch (IllegalAccessException | IllegalArgumentException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}


	public void setPortal(Portal portal) {
		this.portal = portal;

		Long eventId = portal.getEventId();

		if (eventId != null) {
			emailTemplateModel.addForeignKeyListener(emailTemplateModelListener, eventId);
		}
		else {
			emailTemplateModel.addListener(emailTemplateModelListener);
		}

		initEmailTemplateTable();

		syncWidgetsToEntity();
	}


	private void initEmailTemplateTable() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					Long eventId = portal.getEventId();

					// eventId == null is accepted and returns all EmailTempaltes without Event context
					List<EmailTemplate> emailTemplates = emailTemplateModel.getEmailTemplateSearchDataByEvent(eventId);

					// create List of valid Email Templates
					List<EmailTemplate> validEmailTemplates = new ArrayList<>( emailTemplates.size() );
					for (EmailTemplate emailTemplate : emailTemplates) {
						if ( Portal.isSystemRoleValidForPortal(emailTemplate.getSystemRole()) ) {
							validEmailTemplates.add(emailTemplate);
						}
					}

					emailTemplateTable.setInput(validEmailTemplates);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}

}
