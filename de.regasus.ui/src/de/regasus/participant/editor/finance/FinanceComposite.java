package de.regasus.participant.editor.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.CommandButtonFactory;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.payment.command.ChargePaymentCommandHandler;
import de.regasus.finance.payment.command.CreatePaymentCommandHandler;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

public class FinanceComposite extends LazyComposite implements DisposeListener, CacheModelListener<Long> {

	private static final String COMMAND_ID = "CancelClearingCommand";

	/**
	 * Flag that signals that the method syncWidgetsToEntity() is running.
	 * If syncWidgetsToEntity() is called while sync is true it returns before doing anything.
	 * This is necessary because some calls in syncWidgetsToEntity() may trigger CacheModelEvents
	 * that lead to concurrent calls of syncWidgetsToEntity(), e.g. loading ProgrammeBookingCVOs.
	 */
	private boolean sync = false;

	private Participant participant;
	private AccountancyHelper accountancyHelper;

	private boolean ignoreGridSelection = false;
	private FormatHelper formatHelper = FormatHelper.getDefaultLocaleInstance();


	// models
	private ServerModel serverModel;
	private EventModel eventModel;
	private AccountancyModel accountancyModel;
	private ProgrammeBookingModel programmeBookingModel;
	private HotelBookingModel hotelBookingModel;


	// widgets
	private GridTreeViewer gridTreeViewer;
	private GridViewerColumn invoicesAndPositionsColumn;
	private AmountLabelProvider amountLabelProvider;
	private OpenAmountLabelProvider openAmountLabelProvider;
	private List<GridViewerColumn> paymentColumns = new ArrayList<>();
	private InvoiceDetailsComposite invoiceDetailsComposite;
	private InvoicePositionDetailsComposite invoicePositionDetailsComposite;
	private PaymentDetailsComposite paymentDetailsComposite;
	private Composite emptyComposite;
	private Composite detailsComposite;
	private StackLayout stackLayout;
	private Grid grid;
	private AccountancyContentProvider accountancyContentProvider;

	private Button createPaymentButton;
	private Button chargePaymentButton;

	private Group financeStatusGroup;
	private Label financeStatusInvoice;
	private Label financeStatusIncomingPayment;
	private Label financeStatusRefund;
	private Label financeStatusPayment;
	private Label financeStatusBalance;



	public FinanceComposite(Composite parent, int style, final IWorkbenchPartSite site) {
		super(parent, style, site);

		accountancyHelper = new AccountancyHelper();

		serverModel = ServerModel.getInstance();
		eventModel = EventModel.getInstance();
		accountancyModel = AccountancyModel.getInstance();
		programmeBookingModel = ProgrammeBookingModel.getInstance();
		hotelBookingModel = HotelBookingModel.getInstance();

		addDisposeListener(this);
	}


	@Override
	protected void createPartControl() throws Exception {
		try {
			setLayout(new FillLayout());

			// This sash form parts the composite in the left and right pane
			SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);

			// On the left pane, there is a composite, containing a GridViewer and a bottom composite for buttons

			// Left-Composite as container foir leftTopComposite and leftBottomComposite
			Composite leftComposite = new Composite(sashForm, SWT.NONE);
			leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			leftComposite.setLayout(new GridLayout());

			// Left-Top
			Composite leftTopComposite = new Composite(leftComposite, SWT.NONE);
			leftTopComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			leftTopComposite.setLayout(new FillLayout());

			grid = new Grid(leftTopComposite, SWT.BORDER | SWT.MULTI| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			grid.setHeaderVisible(true);
			grid.setCellSelectionEnabled(true);
			grid.setLinesVisible(false);


			gridTreeViewer = new GridTreeViewer(grid);

			invoicesAndPositionsColumn = new GridViewerColumn(gridTreeViewer, SWT.NONE);
			invoicesAndPositionsColumn.getColumn().setWidth(300);
			invoicesAndPositionsColumn.getColumn().setText(
				InvoiceLabel.Invoice.getString() + " / " + UtilI18N.Position);
			invoicesAndPositionsColumn.getColumn().setTree(true);

			GridViewerColumn amountColumn = new GridViewerColumn(gridTreeViewer, SWT.RIGHT);
			amountColumn.getColumn().setWidth(90);
			amountColumn.getColumn().setText(InvoiceLabel.Amount.getString());
			amountColumn.getColumn().setTree(false);

			GridViewerColumn openAmountColumn = new GridViewerColumn(gridTreeViewer, SWT.RIGHT);
			openAmountColumn.getColumn().setWidth(90);
			openAmountColumn.getColumn().setText(InvoiceLabel.Open.getString());
			openAmountColumn.getColumn().setTree(false);

			accountancyContentProvider = new AccountancyContentProvider();
			gridTreeViewer.setContentProvider(accountancyContentProvider);
			gridTreeViewer.setLabelProvider(new ColumnLabelProvider());
			InvoicesAndPositionsColumnLabelProvider ipColumnLabelProvider =
				new InvoicesAndPositionsColumnLabelProvider(accountancyHelper);
			invoicesAndPositionsColumn.setLabelProvider(ipColumnLabelProvider);
			amountLabelProvider = new AmountLabelProvider(accountancyHelper);
			amountColumn.setLabelProvider(amountLabelProvider);

			openAmountLabelProvider = new OpenAmountLabelProvider(accountancyHelper);
			openAmountColumn.setLabelProvider(openAmountLabelProvider);


			// Left-Bottom
			Composite leftBottomComposite = new Composite(leftComposite, SWT.NONE);
			leftBottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			leftBottomComposite.setLayout(new GridLayout(4, true));


			createPaymentButton = CommandButtonFactory.createButton(leftBottomComposite, SWT.PUSH, CreatePaymentCommandHandler.COMMAND_ID);
			createPaymentButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			chargePaymentButton = CommandButtonFactory.createButton(leftBottomComposite, SWT.PUSH, ChargePaymentCommandHandler.COMMAND_ID);
			chargePaymentButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));


			/* On the right pane, there is a composite with a stack layout, which has
			 * an InvoiceDetailsComposite, InvoicePositionDetailsComposite and a
			 * PaymentDetailsComposite on top of each other, of which one gets shown at a time.
			 */

			Composite rightComposite = new Composite(sashForm, SWT.BORDER);
			rightComposite.setLayout(new GridLayout(1, false));

			detailsComposite = new Composite(rightComposite, SWT.BORDER);
			detailsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

			stackLayout = new StackLayout();
			detailsComposite.setLayout(stackLayout);
			invoiceDetailsComposite = new InvoiceDetailsComposite(detailsComposite, SWT.NONE);
			invoicePositionDetailsComposite = new InvoicePositionDetailsComposite(detailsComposite, SWT.NONE);
			paymentDetailsComposite = new PaymentDetailsComposite(detailsComposite, SWT.NONE);
			emptyComposite = new Composite(detailsComposite, SWT.NONE);
			stackLayout.topControl = invoiceDetailsComposite;

			financeStatusGroup = new Group(rightComposite, SWT.NONE);
			financeStatusGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			financeStatusGroup.setLayout(new GridLayout(2, false));
			financeStatusGroup.setText(I18N.ParticipantOverviewForm_AccountancyStatus);
			if (SystemHelper.isMacOSX()) {
				/* Reset the color of the backgroupd.
				 * Other wise there will be a little difference between the background color
				 * of the Composite and the Text widgets.
				 * The reason is yet unknown.
				 */
				Color bg = financeStatusGroup.getBackground();
				Color newBackground = new Color(bg.getDevice(), bg.getRed(), bg.getGreen(), bg.getBlue());
				financeStatusGroup.setBackground(newBackground);
			}

			{
				// Sum of all invoices
				Label financeStatusInvoiceLabel = new Label(financeStatusGroup, SWT.RIGHT | SWT.WRAP);
				financeStatusInvoiceLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				financeStatusInvoiceLabel.setText(I18N.ParticipantOverviewForm_SumOfAllInvoices + ":");

				financeStatusInvoice = new Label(financeStatusGroup, SWT.NONE);
				financeStatusInvoice.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));


				// Sum of all incoming payments
				Label financeStatusIncomingPaymentLabel = new Label(financeStatusGroup, SWT.RIGHT | SWT.WRAP);
				financeStatusIncomingPaymentLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				financeStatusIncomingPaymentLabel.setText(I18N.ParticipantOverviewForm_SumOfAllIncomingPayments + ":");

				financeStatusIncomingPayment = new Label(financeStatusGroup, SWT.NONE);
				financeStatusIncomingPayment.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));


				// Sum of all incoming refunds
				Label financeStatusIRefundLabel = new Label(financeStatusGroup, SWT.RIGHT | SWT.WRAP);
				financeStatusIRefundLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				financeStatusIRefundLabel.setText(I18N.ParticipantOverviewForm_SumOfAllRefunds + ":");

				financeStatusRefund = new Label(financeStatusGroup, SWT.NONE);
				financeStatusRefund.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));


				// Sum of all payments
				Label financeStatusPaymentLabel = new Label(financeStatusGroup, SWT.RIGHT | SWT.WRAP);
				financeStatusPaymentLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				financeStatusPaymentLabel.setText(I18N.ParticipantOverviewForm_SumOfAllPayments + ":");

				financeStatusPayment = new Label(financeStatusGroup, SWT.NONE);
				financeStatusPayment.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

				// separator
				final Label separator = new Label(financeStatusGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
				separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));


				// Open amaount
				Label financeStatusBalanceLabel = new Label(financeStatusGroup, SWT.RIGHT | SWT.WRAP);
				financeStatusBalanceLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				financeStatusBalanceLabel.setText(I18N.ParticipantOverviewForm_OpenAmount + ":");

				financeStatusBalance = new Label(financeStatusGroup, SWT.NONE);
				financeStatusBalance.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			}


			sashForm.setWeights(new int[] { 3, 1 });

			gridTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					if ( ! ignoreGridSelection) {
						try {
							updateDetailComposites();
						}
						catch (Exception e) {
							com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
						}
					}
				}
			});



			MenuManager menuManager = new MenuManager();
			menuManager.setRemoveAllWhenShown(true);

			grid.setMenu(menuManager.createContextMenu(grid));
			site.registerContextMenu("finance", menuManager, gridTreeViewer);
			menuManager.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager menuManager) {
					addClearingActions(menuManager);
				}
			});


			syncWidgetsToEntity();
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

	}


	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (gridTreeViewer != null) {
			gridTreeViewer.addSelectionChangedListener(listener);
		}
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (gridTreeViewer != null) {
			gridTreeViewer.removeSelectionChangedListener(listener);
		}
	}


	public CurrencyAmount getSelectedOpenAmount() {
		CurrencyAmount openAmount = null;

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// Check if there is a selected openAmountColumn
    			if (point.x <= 2) {
    				GridItem gridItem = grid.getItems()[point.y];
    				Object rowData = gridItem.getData();

    				if (rowData instanceof InvoiceVO) {
    					// select all IPs of the Invoice
    					InvoiceVO invoiceVO = (InvoiceVO) rowData;
    					if (invoiceVO != null) {
    						CurrencyAmount currencyAmount = invoiceVO.getAmountOpenAsCurrencyAmount();
							if (openAmount == null) {
								openAmount = currencyAmount;
							}
							else if (openAmount.getCurrency().equals(currencyAmount.getCurrency())) {
								openAmount = openAmount.add(currencyAmount.getAmount());
							}
    					}
    				}
    				else if (rowData instanceof InvoicePositionVO) {
    					InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    					if (invoicePositionVO != null) {
    						CurrencyAmount currencyAmount = invoicePositionVO.getAmountOpenAsCurrencyAmount();
							if (openAmount == null) {
								openAmount = currencyAmount;
							}
							else if (openAmount.getCurrency().equals(currencyAmount.getCurrency())) {
								openAmount = openAmount.add(currencyAmount.getAmount());
							}
    					}
    				}
    			}
    		}
		}
		return openAmount;
	}


	public Collection<PaymentVO> getSelectedPaymentVOs() {
		HashSet<PaymentVO> paymentVOs = new HashSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// Check if there is a selected paymentColumn
    			if (point.x > 2) {
    				int index = point.x - 3;
    				paymentVOs.add(accountancyHelper.getPaymentVOs().get(index));
    			}
    		}
		}

		return paymentVOs;
	}


	public Collection<InvoiceVO> getSelectedInvoiceVOs() {
		ListSet<InvoiceVO> invoiceVOs = new ListSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside invoice columns
    			if (point.x <= 2) {
    				GridItem gridItem = grid.getItems()[point.y];
    				Object rowData = gridItem.getData();

    				if (rowData instanceof InvoiceVO) {
    					// select all IPs of the Invoice
    					InvoiceVO invoiceVO = (InvoiceVO) rowData;
    					if (invoiceVO != null) {
    						invoiceVOs.add(invoiceVO);
    					}
    				}
    				else if (rowData instanceof InvoicePositionVO) {
    					InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    					if (invoicePositionVO != null) {
    						Long invoicePK = invoicePositionVO.getInvoicePK();
    						InvoiceVO invoiceVO = accountancyHelper.getInvoiceByPK(invoicePK);
    						invoiceVOs.add(invoiceVO);
    					}
    				}
    			}
    		}
		}

		return invoiceVOs;
	}


	public Collection<InvoiceVO> getDirectSelectedInvoiceVOs() {
		ListSet<InvoiceVO> invoiceVOs = new ListSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside invoice columns
    			if (point.x <= 2) {
    				GridItem gridItem = grid.getItems()[point.y];
    				Object rowData = gridItem.getData();

    				if (rowData instanceof InvoiceVO) {
    					// select all IPs of the Invoice
    					InvoiceVO invoiceVO = (InvoiceVO) rowData;
    					if (invoiceVO != null) {
    						invoiceVOs.add(invoiceVO);
    					}
    				}
    			}
    		}
		}

		return invoiceVOs;
	}


	public Collection<InvoicePositionVO> getSelectedInvoicePositionVOs() {
		ListSet<InvoicePositionVO> invoicePositionVOs = new ListSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside invoice columns
    			if (point.x <= 2) {
    				GridItem gridItem = grid.getItems()[point.y];
    				Object rowData = gridItem.getData();

    				if (rowData instanceof InvoicePositionVO) {
    					InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    					if (invoicePositionVO != null) {
    						invoicePositionVOs.add(invoicePositionVO);
    					}
    				}
    				else if (rowData instanceof InvoiceVO) {
    					// select all IPs of the Invoice
    					InvoiceVO invoiceVO = (InvoiceVO) rowData;
    					if (invoiceVO != null) {
    						List<InvoicePositionVO> ipVOs = invoiceVO.getInvoicePositionVOs();
    						if (ipVOs != null) {
    							invoicePositionVOs.addAll(ipVOs);
    						}
    					}
    				}
    			}
    		}
		}

		return invoicePositionVOs;
	}


	public Collection<InvoicePositionVO> getDirectSelectedInvoicePositionVOs() {
		ListSet<InvoicePositionVO> invoicePositionVOs = new ListSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside invoice columns
    			if (point.x <= 2) {
    				GridItem gridItem = grid.getItems()[point.y];
    				Object rowData = gridItem.getData();

    				if (rowData instanceof InvoicePositionVO) {
    					InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    					if (invoicePositionVO != null) {
    						invoicePositionVOs.add(invoicePositionVO);
    					}
    				}
    			}
    		}
		}

		return invoicePositionVOs;
	}

	/**
	 * Checks if selected cells are only those in crossings of payments and invoicePositions
	 * for which combination a clearingVO is present as well.
	 */
	public List<ClearingVO> getSelectedPaymentClearingVOs() {
		ListSet<ClearingVO> clearingVOs = new ListSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside paymentColumn
    			if (point.x >= 3) {
    				PaymentVO paymentVO = accountancyHelper.getPaymentVOs().get(point.x - 3);
    				if (!paymentVO.isCanceled() && !paymentVO.isClearing()) {
    					GridItem gridItem = grid.getItems()[point.y];
    					Object rowData = gridItem.getData();

    					if (rowData instanceof InvoicePositionVO) {
    						InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    						List<ClearingVO> ipClearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
    						if (ipClearingVOs != null) {
    							clearingVOs.addAll(ipClearingVOs);
    						}
    					}
    					else if (rowData instanceof InvoiceVO) {
    						// select all clearings of all IPs of the Invoice
    						InvoiceVO invoiceVO = (InvoiceVO) rowData;
    						if (invoiceVO != null) {
    							List<InvoicePositionVO> ipVOs = invoiceVO.getInvoicePositionVOs();
    							if (ipVOs != null) {
    								for (InvoicePositionVO invoicePositionVO : ipVOs) {
    									List<ClearingVO> ipClearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
    									if (ipClearingVOs != null) {
    										clearingVOs.addAll(ipClearingVOs);
    									}
    								}
    							}
    						}

    					}
    				}
    			}
    		}
		}

		return clearingVOs;
	}


	/**
	 * Checks if selected cells are only those in crossings of payments and invoicePositions
	 * for which combination a clearingVO is present as well.
	 */
	public List<ClearingVO> getSelectedInvoicePositionClearingVOs() {
		ListSet<ClearingVO> clearingVOs = new ListSet<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside paymentColumn
    			if (point.x >= 3) {
    				PaymentVO paymentVO = accountancyHelper.getPaymentVOs().get(point.x - 3);
    				if (!paymentVO.isCanceled() && paymentVO.isClearing()) {
    					GridItem gridItem = grid.getItems()[point.y];
    					Object rowData = gridItem.getData();

    					if (rowData instanceof InvoicePositionVO) {
    						InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    						List<ClearingVO> ipClearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
    						if (ipClearingVOs != null) {
    							clearingVOs.addAll(ipClearingVOs);
    						}
    					}
    					else if (rowData instanceof InvoiceVO) {
    						// select all clearings of all IPs of the Invoice
    						InvoiceVO invoiceVO = (InvoiceVO) rowData;
    						if (invoiceVO != null) {
    							List<InvoicePositionVO> ipVOs = invoiceVO.getInvoicePositionVOs();
    							if (ipVOs != null) {
    								for (InvoicePositionVO invoicePositionVO : ipVOs) {
    									List<ClearingVO> ipClearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
    									if (ipClearingVOs != null) {
    										clearingVOs.addAll(ipClearingVOs);
    									}
    								}
    							}
    						}

    					}
    				}
    			}
    		}
		}

		return clearingVOs;
	}


	/**
	 * Checks if selected cells are only those in crossings of payments and invoicePositions
	 * for which combination no clearingVO is present.
	 */
	public List<ClearingVO> getSelectedClearingCandidates() {
		List<ClearingVO> candidateClearingVOs = new ArrayList<>();

		if (grid != null) {
    		Point[] cellSelection = grid.getCellSelection();
    		for (int i = 0; i < cellSelection.length; i++) {
    			Point point = cellSelection[i];

    			// ignore selection outside paymentColumn
    			if (point.x >= 3) {
    				PaymentVO paymentVO = accountancyHelper.getPaymentVOs().get(point.x - 3);
    				if (!paymentVO.isCanceled() && !paymentVO.isClearing()) {
    					GridItem gridItem = grid.getItems()[point.y];
    					Object rowData = gridItem.getData();

    					if (rowData instanceof InvoicePositionVO) {
    						InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
    						List<ClearingVO> ipClearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
    						if (ipClearingVOs == null || ipClearingVOs.isEmpty()) {
    							ClearingVO clearingVO = new ClearingVO();
    							clearingVO.setPaymentPK(paymentVO.getID());
    							clearingVO.setInvoicePositionPK(invoicePositionVO.getID());
    							candidateClearingVOs.add(clearingVO);
    						}
    					}
    					else if (rowData instanceof InvoiceVO) {
    						// select all clearings of all IPs of the Invoice
    						InvoiceVO invoiceVO = (InvoiceVO) rowData;
    						if (invoiceVO != null) {
    							List<InvoicePositionVO> ipVOs = invoiceVO.getInvoicePositionVOs();
    							if (ipVOs != null) {
    								for (InvoicePositionVO invoicePositionVO : ipVOs) {
    									List<ClearingVO> ipClearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
    									if (ipClearingVOs == null || ipClearingVOs.isEmpty()) {
    										ClearingVO clearingVO = new ClearingVO();
    										clearingVO.setPaymentPK(paymentVO.getID());
    										clearingVO.setInvoicePositionPK(invoicePositionVO.getID());
    										candidateClearingVOs.add(clearingVO);
    									}
    								}
    							}
    						}

    					}
    				}
    			}
    		}
		}

		return candidateClearingVOs;
	}


	protected void updateDetailComposites() {
		InvoiceVO invoiceVO = null;
		Collection<InvoiceVO> invoiceVOs = getDirectSelectedInvoiceVOs();
		if (invoiceVOs != null && invoiceVOs.size() == 1) {
			invoiceVO = invoiceVOs.iterator().next();
		}

		InvoicePositionVO invoicePositionVO = null;
		Collection<InvoicePositionVO> invoicePositionVOs = getDirectSelectedInvoicePositionVOs();
		if (invoicePositionVOs != null && invoicePositionVOs.size() == 1) {
			invoicePositionVO = invoicePositionVOs.iterator().next();
		}

		PaymentVO paymentVO = null;
		Collection<PaymentVO> paymentVOs = getSelectedPaymentVOs();
		if (paymentVOs != null && paymentVOs.size() == 1) {
			paymentVO = paymentVOs.iterator().next();
		}


		Composite newTopComposite;
		if (invoicePositionVO != null) {
			invoicePositionDetailsComposite.setInvoicePositionVO(invoicePositionVO);
			newTopComposite = invoicePositionDetailsComposite;
		}
		else if (invoiceVO != null) {
			invoiceDetailsComposite.setInvoiceVO(invoiceVO);
			newTopComposite = invoiceDetailsComposite;
		}
		else if (paymentVO != null) {
			paymentDetailsComposite.setPaymentVO(paymentVO);
			newTopComposite = paymentDetailsComposite;
		}
		else {
			newTopComposite = emptyComposite;
		}
		stackLayout.topControl = newTopComposite;
		detailsComposite.layout();
	}


	private void syncWidgetsToEntity() {
		if (isDisposed()) {
			return;
		}
		if (participant == null) {
			return;
		}
		if (grid == null) {
			return;
		}
		if (sync) {
			return;
		}
		if (!serverModel.isLoggedIn()) {
			// the Editor will be closed, so update is not necessary
			return;
		}

		// get data
		try {
			sync = true;

			AccountancyCVO accountancyCVO = accountancyModel.getAccountancyCVO(participant.getID());


			List<ProgrammeBookingCVO> programmeBookingCVOs =
				programmeBookingModel.getProgrammeBookingCVOsByRecipient(participant.getID());

			List<HotelBookingCVO> hotelBookingCVOs =
				hotelBookingModel.getHotelBookingCVOsByRecipient(participant.getID());


			// set data to accountancyCVO
			accountancyHelper.setAccountancyCVO(accountancyCVO);
			accountancyHelper.setProgrammeBookingCVOs(programmeBookingCVOs);
			accountancyHelper.setHotelBookingCVOs(hotelBookingCVOs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			sync = false;
		}


		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					// Store current cell selection in an immuted clone for later restoration
					Point[] tmpSelectedCells = grid.getCellSelection();
					Point[] selectedCells = new Point[tmpSelectedCells.length];
					for (int i = 0; i < tmpSelectedCells.length; i++) {
						Point tmpCell = tmpSelectedCells[i];
						Point cell = new Point(tmpCell.x, tmpCell.y);
						selectedCells[i] = cell;
					}

					// Store expanded rows and focus for later restoration
					Object[] expandedElements = gridTreeViewer.getExpandedElements();

					/* Get Point of the cell that has the focus.
					 * This might lead to a WidgetDisposedException when the focus is set on a disposed cell.
					 */
					Point focusCell = null;
					try {
						focusCell = grid.getFocusCell();
					}
					catch (Exception e) {
						// ignore
					}

					// Dispose and remove previous payment columns
					for (GridViewerColumn paymentColumn : paymentColumns) {
						paymentColumn.getColumn().dispose();
					}
					paymentColumns.clear();

					// Setup new payment columns
					List<PaymentVO> paymentVOs = accountancyHelper.getPaymentVOs();
					for (int i = 0; i < paymentVOs.size(); i++) {
						GridViewerColumn paymentColumn = new GridViewerColumn(gridTreeViewer, SWT.RIGHT);
						PaymentVO paymentVO = paymentVOs.get(i);
						paymentColumn.getColumn().setWidth(90);

						if (paymentVO.isClearing()) {
							paymentColumn.getColumn().setText(InvoiceLabel.Clearing.getString());
						}
						else if (paymentVO.getBookingDate() != null) {
							// show bookingDate
							Date bookingDate = paymentVO.getBookingDate();
							paymentColumn.getColumn().setText(formatHelper.formatDate(bookingDate));
						}
						else {
							// show newTime as Date in brackets
							Date newTime = paymentVO.getNewTime();
							String text = formatHelper.formatDate(newTime);
							text = "(" + text + ")";
							paymentColumn.getColumn().setText(text);
						}

						paymentColumn.getColumn().setTree(false);
						paymentColumn.getViewer().setData("payment", paymentVO);
						boolean even = (i % 2 == 0);
						paymentColumn.setLabelProvider(
							new PaymentLabelProvider(paymentVO, accountancyHelper, even)
						);
						paymentColumns.add(paymentColumn);
					}

					// gridTreeViewer.setSelection(new StructuredSelection());

					// Clear cell selection to avoid exception during the refresh (MIRCP 816)
					grid.setCellSelection(new Point[0]);
					if (gridTreeViewer.getInput() == null) {
						gridTreeViewer.setInput(accountancyHelper);
						gridTreeViewer.expandAll();
					}
					else {
						gridTreeViewer.refresh();
					}

					// Try to restore the previously expanded elements (if any)
					if (expandedElements == null || expandedElements.length > 0) {
						gridTreeViewer.setExpandedElements(expandedElements);
					}

					// **************************************************************************
					// * Restore cell selection
					// *

					/* Try to restore the selection on the previously selected cells
					 * In some cases there may be cells selected, which doesn't exist anymore.
					 * If this happens an Exception is thrown (see MIRCP-498).
					 * Therefore we check all previously selected cells if they're yet valid.
					 * Unfortunately the method Grid.isValidCell(Point) is private so we canot
					 * use it and have duplicated the code.
					 *
					 *  1.
					 *  Check all cells in selectedCells if they are valid. If not delete them and
					 *  increment the counter.
					 */
					int counter = 0;
					for (int i = 0; i < selectedCells.length; i++) {
						Point cell = selectedCells[i];

						if (cell.x < 0 ||
							cell.x >= grid.getColumnCount() ||
							cell.y < 0 ||
							cell.y >= grid.getItemCount()
						) {
							selectedCells[i] = null;
							counter++;
						}
					}
					/* If there have been invalid cells, create a new selectedCells-Array
					 * with no null values.
					 */
					if (counter > 0) {
						Point[] newSelectedCells = new Point[selectedCells.length - counter];
						int newIndex = 0;
						for (int i = 0; i < selectedCells.length; i++) {
							if (selectedCells[i] != null) {
								newSelectedCells[newIndex++] = selectedCells[i];
							}
						}
						selectedCells = newSelectedCells;
					}
					// set the cell selection
					grid.setCellSelection(selectedCells);

					// *
					// * Restore cell selection
					// **************************************************************************

					// Try to restore the focus on the previously focussed cell
					// which is done independently for item (=row) and column
					// (if it is still there, see MIRCP-450)
					if (focusCell != null) {
						if (focusCell.y > -1 && focusCell.y < grid.getItemCount()) {
							GridItem focusItem = grid.getItem(focusCell.y);
							if (focusItem != null) {
								grid.setFocusItem(focusItem);
							}
						}
						if (focusCell.x > -1 && focusCell.x < grid.getColumnCount()) {
							GridColumn focusColumn = grid.getColumn(focusCell.x);
							if (focusColumn != null) {
								grid.setFocusColumn(focusColumn);
							}
						}
					}

					StringBuilder invoiceSB = new StringBuilder();
					StringBuilder incomingPaymentSB = new StringBuilder();
					StringBuilder refundSB = new StringBuilder();
					StringBuilder paymentSB = new StringBuilder();
					StringBuilder balanceSB = new StringBuilder();
					boolean anyBalanceNegative = false;
					for (String currency : accountancyHelper.getCurrencyList()) {

						append(invoiceSB, accountancyHelper.getTotalInvoiceAmountByCurrency(currency));
						append(incomingPaymentSB, accountancyHelper.getTotalIncomingPaymentAmountByCurrency(currency));
						append(refundSB, accountancyHelper.getTotalRefundAmountByCurrency(currency));
						append(paymentSB, accountancyHelper.getTotalPaymentAmountByCurrency(currency));

						BigDecimal balance = accountancyHelper.getTotalInvoiceAmountByCurrency(currency).getAmount()
							.subtract(accountancyHelper.getTotalPaymentAmountByCurrency(currency).getAmount());
						CurrencyAmount totalBalanceAmount = new CurrencyAmount(balance, currency);
						append(balanceSB, totalBalanceAmount);

						if (totalBalanceAmount.getAmount().signum() != 0) {
							anyBalanceNegative = true;
						}
					}
					financeStatusInvoice.setText(invoiceSB.toString());
					financeStatusIncomingPayment.setText(incomingPaymentSB.toString());
					financeStatusRefund.setText(refundSB.toString());
					financeStatusPayment.setText(paymentSB.toString());
					financeStatusBalance.setText(balanceSB.toString());

					int color = SWT.COLOR_BLACK;
					if (anyBalanceNegative) {
						color = SWT.COLOR_RED;
					}
					financeStatusBalance.setForeground(Display.getCurrent().getSystemColor(color));

					financeStatusGroup.layout();

					updateDetailComposites();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}

			public void append(StringBuilder invoiceSB, CurrencyAmount totalInvoiceAmount) {
				if (invoiceSB.length() > 0) {
					invoiceSB.append('\n');
				}
				invoiceSB.append(totalInvoiceAmount.toString());
			}
		});
	}


	public void setParticipant(Participant participant) {
		Long oldPK = this.participant == null ? null : this.participant.getID();
		Long newPK =      participant == null ? null : 		participant.getID();
		boolean pkChanged = !EqualsHelper.isEqual(oldPK, newPK);

		Long oldEventPK = this.participant == null ? null : this.participant.getEventId();
		Long newEventPK =      participant == null ? null : 	participant.getEventId();
		boolean eventChanged = !EqualsHelper.isEqual(oldEventPK, newEventPK);


		if (pkChanged && oldPK != null) {
			accountancyModel.removeListener(this, oldPK);
			programmeBookingModel.removeForeignKeyListener(this, oldPK);
			hotelBookingModel.removeForeignKeyListener(this, oldPK);
		}
		if (eventChanged && oldEventPK != null) {
			eventModel.removeListener(this, oldEventPK);
		}


		this.participant = participant;


		if (pkChanged && newPK != null) {
			accountancyModel.addListener(this, newPK);
			programmeBookingModel.addForeignKeyListener(this, newPK);
			hotelBookingModel.addForeignKeyListener(this, newPK);
		}
		if (eventChanged && newEventPK != null) {
			eventModel.addListener(this, newEventPK);
			dataChange(new CacheModelEvent<>(this, CacheModelOperation.REFRESH, newEventPK));
		}


		syncWidgetsToEntity();
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		try {
			if (accountancyModel != null && participant != null && participant.getID() != null) {
				accountancyModel.removeListener(this, participant.getID());
			}
		}
		catch (Exception e) {
			// ignore
		}


		try {
			if (programmeBookingModel != null && participant != null && participant.getID() != null) {
				programmeBookingModel.removeForeignKeyListener(this, participant.getID());
			}
		}
		catch (Exception e) {
			// ignore
		}


		try {
			if (hotelBookingModel != null && participant != null && participant.getID() != null) {
				hotelBookingModel.removeForeignKeyListener(this, participant.getID());
			}
		}
		catch (Exception e) {
			// ignore
		}


		try {
			if (eventModel != null && participant != null && participant.getEventId() != null) {
				eventModel.removeListener(this, participant.getEventId());
			}
		}
		catch (Exception e) {
			// ignore
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == eventModel) {
				if (event.getOperation() == CacheModelOperation.REFRESH ||
    				event.getOperation() == CacheModelOperation.UPDATE
    			) {
					if (chargePaymentButton != null) {
	    				// enable/disable chargePaymentButton depending on Event.paymentSystem
	    				Long eventPK = event.getFirstKey();
	    				EventVO eventVO = eventModel.getEventVO(eventPK);
	    				if (eventVO != null) {
	    					final PaymentSystem paymentSystem = eventVO.getPaymentSystem();
	    					SWTHelper.syncExecDisplayThread(new Runnable() {
	    						@Override
								public void run() {
	    							try {
	    								chargePaymentButton.setVisible(paymentSystem != null);
	    							}
	    							catch (Exception e) {
	    								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
	    							}
	    						}
	    					});
	    				}
					}
				}
			}
			else if (event.getSource() == accountancyModel) {
				syncWidgetsToEntity();
			}
			else if (event.getSource() == programmeBookingModel) {
				syncWidgetsToEntity();
			}
			else if (event.getSource() == hotelBookingModel) {
				syncWidgetsToEntity();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public AccountancyHelper getAccountancyHelper() {
		return accountancyHelper;
	}


	public void selectInvoice(Long invoicePK) {
		if (grid != null) {
    		InvoiceVO invoiceVO = accountancyHelper.getInvoiceByPK(invoicePK);
    		// select the invoice to determine the index of the row to select
    		ignoreGridSelection = true;
    		gridTreeViewer.setSelection(new StructuredSelection(invoiceVO));
    		Point[] selectedCells = grid.getCellSelection();
    		gridTreeViewer.setSelection(new StructuredSelection());
    		ignoreGridSelection = false;
    		int invoiceIndex = selectedCells[0].y;

    //		int invoiceIndex = accountancyHelper.getInvoiceIndex(invoiceCVO.getPK());
    		Point focusCell = new Point(0, invoiceIndex);

    		grid.setCellSelection(focusCell);
    		updateDetailComposites();
		}
	}


	@SuppressWarnings("unused")
	private void internalSelectionTests() {
		StructuredSelection selection = (StructuredSelection) gridTreeViewer.getSelection();
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		System.out.println("======================================================");
		System.out.println("FinanceComposite.handle(StructuredSelection selection)");
		System.out.println("------------------------------------------------------");
		System.out.println("Var1: Die Selection des gridTreeViewer (size=" + selection.size() + ")");

		int index = 0;
		Iterator<?> iterator = selection.iterator();
		while (iterator.hasNext()) {
			System.out.println(index++ + ": " + iterator.next().getClass().getName());
		}

		System.out.println("------------------------------------------------------");
		Grid grid = gridTreeViewer.getGrid();
		System.out.println("Var2: Die Selected GridItems des Grids (size=" + grid.getSelectionCount() + ")");
		GridItem[] gridItems = grid.getSelection();
		for (int i = 0; i < gridItems.length; i++) {
			GridItem gridItem = gridItems[i];
			System.out.println(i + ": " + gridItem.getText());
		}

		System.out.println("------------------------------------------------------");
		System.out.println("Var3: Die Selected Cells des Grids (size=" + grid.getCellSelectionCount() + ")");
		Point[] cellSelection = grid.getCellSelection();
		for (int i = 0; i < cellSelection.length; i++) {
			Point point = cellSelection[i];
			System.out.println(i + ": " + point);
		}

		System.out.println("------------------------------------------------------");
		System.out.println("Var4: Die Selection des Viewers der 1. paymentColumn");

		ISelection iselection = paymentColumns.get(0).getViewer().getSelection();
		if (iselection instanceof StructuredSelection) {
			StructuredSelection ss = (StructuredSelection) iselection;
			System.out.println(" size = " + ss.size());
			index = 0;
			iterator = ss.iterator();
			while (iterator.hasNext()) {
				System.out.println(index++ + ": " + iterator.next().getClass().getName());
			}
		}

	}


	private void addClearingActions(IMenuManager menuManager) {

		if (grid != null && grid.getCellSelectionCount() == 1) {
			Point point = grid.getCellSelection()[0];
			GridItem gridItem = grid.getItems()[point.y];
			Object rowData = gridItem.getData();


			Collection<PaymentVO> paymentVOs = getSelectedPaymentVOs();
			if (paymentVOs.isEmpty()) {
				// No payment below selected cell
				return;
			}
			PaymentVO paymentVO = paymentVOs.iterator().next();
			int paymentSignum = paymentVO.getAmount().signum();

			CurrencyAmount openCurrencyAmountOfPayment = new CurrencyAmount(paymentVO.getOpenAmount(), paymentVO.getCurrency());


			if (rowData instanceof InvoicePositionVO) {
				InvoicePositionVO invoicePositionVO = (InvoicePositionVO) rowData;
				CurrencyAmount openCurrencyAmountOfInvoicePosition = invoicePositionVO.getAmountOpenAsCurrencyAmount();

				boolean atLeastOneCondition = false;

				/* Create Clearing with <Open amount of InvoicePosition>
				 * Appears only, if
				 * - <Open amount of InvoicePosition> is not 0
				 * - The signums of <Open amount of InvoicePosition> is the same as of the Payment
				 * - The absolute openAmount of the InvoicePosition must less or equal than the absolute openAmount of the Payment
				 */
				int ipSignum = openCurrencyAmountOfInvoicePosition.getAmount().signum();
				if (ipSignum != 0 &&
					ipSignum == paymentSignum &&
					openCurrencyAmountOfInvoicePosition.getAmount().abs().compareTo(paymentVO.getOpenAmount().abs()) <= 0
				) {
					CreateClearingAction createClearingAction = CreateClearingAction.getInstance(
						openCurrencyAmountOfInvoicePosition,
						paymentVO,
						invoicePositionVO
					);
					menuManager.insertBefore(COMMAND_ID, createClearingAction);
					atLeastOneCondition = true;
				}

				// Create Clearing with <Open amount of Payment>
				// Appears only, if <Open amount of Payment> is not 0
				// and different from the first value above if it was added.
				int openPaymentSignum = openCurrencyAmountOfPayment.getAmount().signum();
				if (
					openPaymentSignum != 0
					&&
					(
						!atLeastOneCondition
						||
						openCurrencyAmountOfPayment.getAmount().compareTo(openCurrencyAmountOfInvoicePosition.getAmount()) != 0
					)
				) {
					CreateClearingAction createClearingAction = CreateClearingAction.getInstance(
						openCurrencyAmountOfPayment,
						paymentVO,
						invoicePositionVO
					);
					menuManager.insertBefore(COMMAND_ID, createClearingAction);
					atLeastOneCondition = true;
				}

				//  "Create Clearing ..."  if at least one of the other menu items appears.
				if (atLeastOneCondition) {
					CreateClearingAction createClearingAction = CreateClearingAction.getInstance(
						null,
						paymentVO,
						invoicePositionVO
					);
					menuManager.insertBefore(COMMAND_ID, createClearingAction);
				}

			}
			else if (rowData instanceof InvoiceVO) {
				InvoiceVO invoiceVO = (InvoiceVO) rowData;
				CurrencyAmount openCurrencyAmountOfInvoice = invoiceVO.getAmountOpenAsCurrencyAmount();

				/* Create Clearing with <Open amount of Invoice>
				 * Appears only, if
				 * - <Open amount of InvoicePosition> is not 0
				 * - The signums of <Open amount of InvoicePosition> is the same as of the Payment
				 * - The absolute openAmount of the Invoice must less or equal than the absolute openAmount of the Payment
				 */
				int invoiceSignum = openCurrencyAmountOfInvoice.getAmount().signum();
				if (invoiceSignum != 0 &&
					invoiceSignum == paymentSignum &&
					openCurrencyAmountOfInvoice.getAmount().abs().compareTo(paymentVO.getOpenAmount().abs()) <= 0
				) {
					CreateClearingAction createClearingAction = CreateClearingAction.getInstance(
						openCurrencyAmountOfInvoice,
						paymentVO,
						invoiceVO,
						getSelectedClearingCandidates()
					);
					menuManager.insertBefore(COMMAND_ID, createClearingAction);
				}
			}


		}
	}

}
