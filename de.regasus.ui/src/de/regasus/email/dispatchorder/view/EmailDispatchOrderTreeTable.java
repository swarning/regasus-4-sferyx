package de.regasus.email.dispatchorder.view;

import static com.lambdalogic.util.model.CacheModelOperation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.email.EmailDispatchModel;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.email.EmailTemplateModel;

/**
 * A tree table that has the columns
 * <ul>
 * <li>Template / Dispatch Order / Dispatch</li>
 * <li>Name of Recipient</li>
 * <li>To</li>
 * <li>Status</li>
 * <li>Information / Error</li>
 * </ul>
 *
 * It shows the following 3 level structure:
 * <pre>
 *  +-{@link EmailTemplate}
 *  |  +-{@link EmailDispatchOrder}
 *  |      +-{@link EmailDispatch}
 *  |      +-{@link EmailDispatch}
 *  |  +-{@link EmailDispatchOrder}
 *  |      +-{@link EmailDispatch}
 *  |      +-{@link EmailDispatch}
 *  +-{@link EmailTemplate}
 *  |  +-{@link EmailDispatchOrder}
 *  |      +-{@link EmailDispatch}
 * </pre>
 * The structure is actually provided by the {@link EmailDispatchOrderTreeContentProvider}, evaluated from the
 * three email models.
 *
 * @author manfred
 *
 */
public class EmailDispatchOrderTreeTable
extends Composite
implements CacheModelListener<Long>, DisposeListener {

	private Long eventPK;

	// models
	private EmailTemplateModel emailTemplateSearchModel = EmailTemplateModel.getInstance();
	private EmailDispatchOrderModel emailDispatchOrderModel = EmailDispatchOrderModel.getInstance();
	private EmailDispatchModel emailDispatchModel = EmailDispatchModel.getInstance();


	// *************************************************************************
	// * Widgets
	// *

	/**
	 * The SWT widget showing the tree table
	 */
	private Tree tree;

	/**
	 * The JFace viewer wrapped around the {@link #tree}
	 */
	private TreeViewer treeViewer;



	// *************************************************************************
	// * Constructor
	// *


	public TreeViewer getTreeViewer() {
		return treeViewer;
	}


	public EmailDispatchOrderTreeTable(Composite parent, int style) {
		super(parent, style);

		addDisposeListener(this);

		TreeColumnLayout layout = new TreeColumnLayout();
		setLayout(layout);

		// SWT Widget
		tree = new Tree(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		// The columns for the tree table
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText(EmailLabel.TemplateDispatch.getString());
		layout.setColumnData(column1, new ColumnWeightData(10));

		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setAlignment(SWT.LEFT);
		column2.setText(EmailLabel.NameOfRecipient.getString());
		layout.setColumnData(column2, new ColumnWeightData(10));

		TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
		column3.setAlignment(SWT.LEFT);
		column3.setText(UtilI18N.Status);
		layout.setColumnData(column3, new ColumnWeightData(6));

		TreeColumn column4 = new TreeColumn(tree, SWT.LEFT);
		column4.setAlignment(SWT.LEFT);
		column4.setText(UtilI18N.ErrorInfo);
		layout.setColumnData(column4, new ColumnWeightData(14));

		// JFace Viewer
		treeViewer = new TreeViewer(tree);
		treeViewer.setLabelProvider(new EmailDispatchOrderTreeLabelProvider());
		treeViewer.setContentProvider(new EmailDispatchOrderTreeContentProvider());

		emailDispatchOrderModel.addListener(this);
		emailTemplateSearchModel.addListener(this);
		emailDispatchModel.addListener(this);
	}


	public void setEventPK(Long eventPK) throws Exception {
		this.eventPK = eventPK;

		List<EmailTemplate> emailTemplateSearchData = emailTemplateSearchModel.getEmailTemplateSearchDataByEvent(eventPK);

		// order by name and id
		emailTemplateSearchData = new ArrayList<>(emailTemplateSearchData);
		Collections.sort(emailTemplateSearchData, EmailTemplateComparator.getInstance());

		treeViewer.setInput(emailTemplateSearchData);
	}


	@Override
	public void dataChange(final CacheModelEvent<Long> event) {
		try {
//			System.out.println(">>>>>>>>>> EmailDispatchOrderTreeTable.dataChange()\n" + event);

			if (event.getSource() == emailTemplateSearchModel) {
				if (event.getOperation() == CacheModelOperation.REFRESH) {
					final List<EmailTemplate> templateList = emailTemplateSearchModel.getEmailTemplateSearchDataByEvent(eventPK);
					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Setting completely new input list");
							treeViewer.setInput(templateList);
						}
					});
				}
				else if (event.getOperation() == CREATE) {
					// A new EmailTemplate was created via the model, get it (including the others)
					// from there without a new server call and show it in the table
					final List<EmailTemplate> templateList = emailTemplateSearchModel.getEmailTemplateSearchDataByEvent(eventPK);
					final EmailTemplate createdEmailTemplate = emailTemplateSearchModel.getEmailTemplateSearchData(event.getFirstKey());

					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Revealing created emailTemplate" + createdEmailTemplate.getName());
							treeViewer.setInput(templateList);
							treeViewer.reveal(createdEmailTemplate);
						}
					});
				}
				else if (event.getOperation() == UPDATE) {
					// An EmailTemplate might have been renamed, telling the tree to update labels
					final EmailTemplate updatedEmailTemplate = emailTemplateSearchModel.getEmailTemplateSearchData(event.getFirstKey());

					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Updating emailTemplate in tree" + updatedEmailTemplate.getName());
							treeViewer.update(updatedEmailTemplate, null);
						}
					});
				}
				else if (event.getOperation() == DELETE) {
					// An EmailTemplate has been deleted, cannot remove it from the tree because it doesn't exist
					// anymore, therefore just setting the currently known templates in the tree (without server call)
					final List<EmailTemplate> undeletedTemplateList = new ArrayList<EmailTemplate>();
					List<EmailTemplate> cacheTemplateList = emailTemplateSearchModel.getEmailTemplateSearchDataByEvent(eventPK);
					for (EmailTemplate cachedTemplate : cacheTemplateList) {
						if (! event.getKeyList().contains(cachedTemplate.getID())) {
							undeletedTemplateList.add(cachedTemplate);
						}
					}

					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Showing (smaller) list of emailTemplates");
							treeViewer.setInput(undeletedTemplateList);
						}
					});
				}
			}
			else if (event.getSource() == emailDispatchOrderModel) {
				if (event.getOperation() == CREATE) {
					// An EmailDispatchOrder has been created, tell the parent EmailTemplate node
					// to refresh its children and also to reveal the new child
					final EmailDispatchOrder emDisOrd = emailDispatchOrderModel.getEmailDispatchOrder(event.getFirstKey());
					final EmailTemplate parent = emailTemplateSearchModel.getEmailTemplateSearchData(emDisOrd.getEmailTemplatePK());

					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Revealing created emailDispatchOrder");
							treeViewer.refresh(parent);
							treeViewer.reveal(emDisOrd);
						}
					});
				}
				else if (event.getOperation() == UPDATE) {
					// An EmailDispatchOrder might have been cancelled, telling the tree to update labels
					final EmailDispatchOrder emDisOrd = emailDispatchOrderModel.getEmailDispatchOrder(event.getFirstKey());
					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Showing changed state of updated emailDispatchOrder");
							treeViewer.update(emDisOrd, null);
						}
					});
				}

			}
			else if (event.getSource() == emailDispatchModel) {
				if (event.getOperation() == REFRESH) {
					// EmailDispatches have been refreshed, possibly cancelled, tell
					// their treeviewer to update their labels
					final List<EmailDispatch> emDisList = emailDispatchModel.getEmailDispatches(event.getKeyList());

					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
//							System.out.println(">> Updating existing emailDispatches");
							treeViewer.update(emDisList.toArray(), null);
						}
					});
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		emailTemplateSearchModel.removeListener(this);
		emailDispatchOrderModel.removeListener(this);
		emailDispatchModel.removeListener(this);
	}

}
