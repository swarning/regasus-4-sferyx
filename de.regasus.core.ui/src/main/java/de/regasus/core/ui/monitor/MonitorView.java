package de.regasus.core.ui.monitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * For this view to show resource data you need to start the application with the
 * Tracing options for "eclipse.org.ui" being: debug=true and trace/graphics=true.
 * 
 * @author manfred
 *
 */
public class MonitorView extends ViewPart {

	private Composite parent;
	
	private int colors = 0;
	private int cursors = 0;
	private int fonts = 0;
	private int gcs = 0;
	private int images = 0;
	private int paths = 0;
	private int patterns = 0;
	private int regions = 0;
	private int textLayouts; 
	private int transforms= 0;
	
	private Label colorsLabel;
	private Label cursorsLabel;
	private Label fontsLabel;
	private Label gcsLabel;
	private Label imagesLabel;
	private Label pathsLabel;
	private Label patternsLabel;
	private Label regionsLabel;
	private Label textLayoutsLabel;
	private Label transformsLabel;

	private Label widgetsLabel;
	
	private IPartListener listener = new IPartListener(){

		public void partActivated(IWorkbenchPart part) {}

		public void partBroughtToTop(IWorkbenchPart part) {}

		public void partClosed(IWorkbenchPart part) {
			update();
		}

		public void partDeactivated(IWorkbenchPart part) {}

		public void partOpened(IWorkbenchPart part) {
			update();
		}};
	
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		
		parent.setLayout(new GridLayout(1, false));
		
		
		widgetsLabel = new Label(parent, SWT.NONE);
		widgetsLabel.setText("Widgets: ");
		widgetsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Group group = new Group(parent, SWT.NONE);
		group.setText("Device Data");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true));
		
		new Label(group, SWT.NONE).setText("Colors: ");
		colorsLabel = new Label(group, SWT.NONE);
		colorsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(group, SWT.NONE).setText("Cursors: ");
		cursorsLabel = new Label(group, SWT.NONE);
		cursorsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("Fonts: ");
		fontsLabel = new Label(group, SWT.NONE);
		fontsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("GCs: ");
		gcsLabel = new Label(group, SWT.NONE);
		gcsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("Images: ");
		imagesLabel = new Label(group, SWT.NONE);
		imagesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("Pathes: ");
		pathsLabel = new Label(group, SWT.NONE);
		pathsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("Patterns: ");
		patternsLabel = new Label(group, SWT.NONE);
		patternsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("Regions: ");
		regionsLabel = new Label(group, SWT.NONE);
		regionsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("TextLayouts: ");
		textLayoutsLabel = new Label(group, SWT.NONE);
		textLayoutsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(group, SWT.NONE).setText("Transform: ");
		transformsLabel = new Label(group, SWT.NONE);
		transformsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		getSite().getPage().addPartListener(listener);
}


	@Override
	public void setFocus() {
		parent.setFocus();

	}
	
	@Override
	public void dispose() {
		getSite().getPage().addPartListener(listener);
		super.dispose();
	}
	
	
	private void update() {
		if (! parent.isDisposed() && parent.isVisible()) {

			colors = 0;
			cursors = 0;
			fonts = 0;
			gcs = 0;
			images = 0;
			paths = 0;
			patterns = 0;
			regions = 0;
			textLayouts =0; 
			transforms= 0;
			
			DeviceData deviceData = Display.getCurrent().getDeviceData();
			if (deviceData == null) {
				return;
			}
			Object[] objects = deviceData.objects;
			if (objects == null) {
				return;
			}
			for (Object object : objects) {
				if (object instanceof Color) colors++;
				if (object instanceof Cursor) cursors++;
				if (object instanceof Font) fonts++;
				if (object instanceof GC) gcs++;
				if (object instanceof Image) images++;
				if (object instanceof Path) paths++;
				if (object instanceof Pattern) patterns++;
				if (object instanceof Region) regions++;
				if (object instanceof TextLayout) textLayouts++;
				if (object instanceof Transform) transforms++;

			}
			
			colorsLabel.setText(String.valueOf(colors));
			cursorsLabel.setText(String.valueOf(cursors));
			fontsLabel.setText(String.valueOf(fonts));
			gcsLabel.setText(String.valueOf(gcs));
			imagesLabel.setText(String.valueOf(images));
			pathsLabel.setText(String.valueOf(paths));
			patternsLabel.setText(String.valueOf(patterns));
			regionsLabel.setText(String.valueOf(regions));
			textLayoutsLabel.setText(String.valueOf(textLayouts));
			transformsLabel.setText(String.valueOf(transforms));
			
			
		}
		
	}

}
