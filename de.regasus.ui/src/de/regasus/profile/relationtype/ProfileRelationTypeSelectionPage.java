package de.regasus.profile.relationtype;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.I18N;
import de.regasus.profile.ProfileRelationTypeModel;


public class ProfileRelationTypeSelectionPage extends WizardPage {

	static class ProfileRelationTypeElement {
		Long profileRelationTypeID;
		boolean reverse;
		String label;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ProfileRelationTypeElement [label=");
			builder.append(label);
			builder.append(", reverse=");
			builder.append(reverse);
			builder.append(", profileRelationTypeID=");
			builder.append(profileRelationTypeID);
			builder.append("]");
			return builder.toString();
		}
	}


	private ListViewer profileRelationTypeListViewer;

	/**
	 * List of ProfileRelationTypeElement that represent all ProfileRelationTypes.
	 * ProfileRelationTypes that are directed are represented by two ProfileRelationTypeElement,
	 * one for each direction.
	 */
	private java.util.List<ProfileRelationTypeElement> profileRelationTypeElements;

	/**
	 * The Profile for that the ProfileRelation is created.
	 * This is the primarily selected Profile. The second Profile was selected in the previous WizardPage.
	 */
	private String profile1;

	/**
	 * Name of the 2nd Profile, that was selected on the previous WizardPage
	 */
	private Label profile2NameLabel;


	public ProfileRelationTypeSelectionPage(String profile1) {
		super("ProfileRelationType");

		this.profile1 = profile1;

		setTitle(I18N.ProfileRelationTypeSelectionPage_Title);
		setDescription(I18N.ProfileRelationTypeSelectionPage_Description);

		initProfileRelationTypes();

	}


	private void initProfileRelationTypes() {
		try {
			// get all ProfileRelationTypes from model
			Collection<ProfileRelationType> profileRelationTypeList = ProfileRelationTypeModel.getInstance().getAllProfileRelationTypes();

			/* Create the Tuples:
			 * One Tuple for each undirected ProfileRelationType.
			 * Two Tuples for each directed ProfileRelationType.
			 */
			profileRelationTypeElements = new ArrayList<ProfileRelationTypeElement>(2 * profileRelationTypeList.size());
			for (ProfileRelationType profileRelationType : profileRelationTypeList) {
				ProfileRelationTypeElement element = new ProfileRelationTypeElement();
				element.profileRelationTypeID = profileRelationType.getID();
				element.reverse = false;
				element.label = profileRelationType.getDescription12().getString() + " (" + profileRelationType.getName().getString() + ")";

				profileRelationTypeElements.add(element);
				System.out.println("ProfileRelationTypeSelectionPage added " + element);

				if (profileRelationType.isDirected()) {
					element = new ProfileRelationTypeElement();
					element.profileRelationTypeID = profileRelationType.getID();
					element.reverse = true;
					element.label = profileRelationType.getDescription21().getString() + " (" + profileRelationType.getName().getString() + ")";

					profileRelationTypeElements.add(element);

					System.out.println("ProfileRelationTypeSelectionPage added " + element);
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		Label profile1Label = new Label(composite, SWT.NONE);
		profile1Label.setText(profile1);
		profile1Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		try {
			List profileRelationTypeList = new List(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
			layoutData.widthHint = 200;
			profileRelationTypeList.setLayoutData(layoutData);
			profileRelationTypeListViewer = new ListViewer(profileRelationTypeList);
			profileRelationTypeListViewer.setContentProvider(ArrayContentProvider.getInstance());
			profileRelationTypeListViewer.setLabelProvider(new ProfileRelationTypeLabelProvider());
			profileRelationTypeListViewer.setInput(profileRelationTypeElements);
			profileRelationTypeListViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection selection = profileRelationTypeListViewer.getSelection();
					setPageComplete(! selection.isEmpty());
				}
			});
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		profile2NameLabel = new Label(composite, SWT.NONE);
		profile2NameLabel.setText(""); // place holder
		profile2NameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));


		setPageComplete(false);
		setControl(composite);
	}


	public boolean isReverseRelation() {
		ProfileRelationTypeElement profileRelationTypeElement = getSelectedObject();
		return profileRelationTypeElement.reverse;
	}


	public Long getProfileRelationTypeID() {
		Long profileRelationTypeID = null;

		ProfileRelationTypeElement profileRelationTypeElement = getSelectedObject();
		if (profileRelationTypeElement != null) {
			System.out.println("ProfileRelationTypeSelectionPage returning ProfileRelationTypeID from selected " + profileRelationTypeElement);
			profileRelationTypeID = profileRelationTypeElement.profileRelationTypeID;
		}

		return profileRelationTypeID;
	}


	private ProfileRelationTypeElement getSelectedObject() {
		return SelectionHelper.getUniqueSelected(profileRelationTypeListViewer.getSelection());
	}


	public void setProfile2Name(String profile2Name) {
		profile2NameLabel.setText(profile2Name);
		profile2NameLabel.getParent().layout();
	}

}
