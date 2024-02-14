package de.regasus.example;

import static com.lambdalogic.util.StringHelper.trim;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


/**
 * This is an example {@link Composite} that can be used as a template.
 */
public class FooComposite extends Composite {

	// entity
	private Foo foo;


	// **************************************************************************
	// * Widgets
	// *

	private Text fooText;
	private Button fooCheck;

	// *
	// * Widgets
	// **************************************************************************

	private ModifySupport modifySupport = new ModifySupport(this);

	private GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
	private GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);
	private GridDataFactory checkGridDataFactory = GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER);


	public FooComposite(Composite parent) {
		super(parent, SWT.NONE);

		addDisposeListener(disposeListener);

		createWidgets();
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			// TODO: free system resources
		}
	};


	public void createWidgets() {

		// layout without margin, because it works only as a container
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		Group fooGroup = buildFooGroup(this);
		groupGridDataFactory.applyTo(fooGroup);
	}


	private Group buildFooGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Foo"); // TODO: EmailI18N
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

		// foo text
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Foo Text"); // TODO: i18n

			fooText = new Text(group, SWT.BORDER);
			textGridDataFactory.applyTo(fooText);
			fooText.addModifyListener(modifySupport);
		}


		// foo check
		{
			new Label(group, SWT.NONE); // placeholder

			fooCheck = new Button(group, SWT.CHECK);
			checkGridDataFactory.applyTo(fooCheck);
			fooCheck.setText("Foo Check"); // TODO: i18n
			fooCheck.addSelectionListener(modifySupport);
		}

		return group;
	}


	/**
	 * Set a new entity.
	 * @param foo
	 */
	public void setFoo(Foo foo) {
		this.foo = foo;
		syncWidgetsToEntity();
	}


	/**
	 * Copy the values from the entity to the widgets.
	 */
	private void syncWidgetsToEntity() {
		if (foo != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						fooText.setText( StringHelper.avoidNull(foo.getFooText()) );
						fooCheck.setText( StringHelper.avoidNull(foo.isFooBool()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	/**
	 * Copy the values from the widgets to the entity.
	 */
	public void syncEntityToWidgets() {
		if (foo != null) {
			foo.setFooText( trim(fooText.getText()) );
			foo.setFooBool( fooCheck.getSelection() );
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
