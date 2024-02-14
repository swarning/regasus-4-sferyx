package de.regasus.portal.page.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.Portal;
import de.regasus.portal.component.Component;


public class PageWidgetBuilder {

	private Composite parent;
	private ModifySupport modifySupport;
	private Portal portal;

	private GridDataFactory fullWidthGridDataFactory;
	private GridDataFactory fullWidthMinusOneGridDataFactory;


	public PageWidgetBuilder(
		Composite parent,
		int colCount,
		ModifySupport modifySupport,
		Portal portal
	) {
		this.parent = parent;
		this.modifySupport = modifySupport;
		this.portal = portal;

		fullWidthGridDataFactory = GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(colCount, 1);

		fullWidthMinusOneGridDataFactory = fullWidthGridDataFactory.copy().span(colCount - 1, 1);
	}


	public void buildTypeLabel(String label) {
		Font font = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(com.lambdalogic.util.rcp.Activator.BIG_FONT);

		Label typeLabel = new Label(parent, SWT.NONE);
		fullWidthGridDataFactory.applyTo(typeLabel);
		typeLabel.setFont(font);
		typeLabel.setText(label);

		Label distanceLabel = new Label(parent, SWT.NONE);
		fullWidthGridDataFactory.applyTo(distanceLabel);
	}


	public Text buildHtmlId() {
   		SWTHelper.createLabel(parent, Component.HTML_ID.getString(), true);

   		Text htmlIdText = new Text(parent, SWT.BORDER);
   		fullWidthMinusOneGridDataFactory.applyTo(htmlIdText);
		SWTHelper.makeBold(htmlIdText);
		htmlIdText.setTextLimit( Component.HTML_ID.getMaxLength() );
		htmlIdText.addModifyListener(modifySupport);
		return htmlIdText;
	}


	public Text buildRender() {
		SWTHelper.createLabel(parent, Component.RENDER.getString());

		Text renderText = new Text(parent, SWT.BORDER);
		fullWidthMinusOneGridDataFactory.applyTo(renderText);
		renderText.setTextLimit( Component.RENDER.getMaxLength() );
		renderText.addModifyListener(modifySupport);

		return renderText;
	}


	public ConditionGroup buildConditionGroup(String label, boolean showYesIfNotNewButton) {
		ConditionGroup conditionGroup = new ConditionGroup(
			parent,
			SWT.NONE,
			showYesIfNotNewButton,
			portal
		);
		fullWidthGridDataFactory.copy().indent(SWT.NONE, 10).applyTo(conditionGroup);
		conditionGroup.setText(label);
		conditionGroup.addModifyListener(modifySupport);

		return conditionGroup;
	}


	public ConditionGroup buildConditionGroup(String label) {
		return buildConditionGroup(label, false /*showYesIfNotNewButton*/);
	}

}
