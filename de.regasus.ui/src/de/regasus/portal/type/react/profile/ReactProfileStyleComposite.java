package de.regasus.portal.type.react.profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.ZipHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.portal.PageLayout;
import de.regasus.portal.portal.editor.StyleParameterComposite;
import de.regasus.portal.type.react.ReactStyleColorComposite;
import de.regasus.portal.type.react.ReactStyleFontComposite;
import de.regasus.portal.type.react.ReactStyleSizeComposite;
import de.regasus.portal.type.standard.LessToCssCompiler;
import de.regasus.ui.Activator;



public class ReactProfileStyleComposite extends Composite implements StyleParameterComposite {

	private static final String REACT_PROFILE_LESS_RESOURCES_FILE_NAME = "reactprofile-less-resources.zip";

	private PageLayout pageLayout;

	private ModifySupport modifySupport = new ModifySupport(this);

	private boolean widgetsCreated = false;


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private ReactStyleColorComposite colorComposite;
	private ReactStyleFontComposite fontComposite;
	private ReactStyleSizeComposite sizeComposite;

	// *
	// * Widgets
	// **************************************************************************


	public ReactProfileStyleComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void createWidgets() throws Exception {
		if (widgetsCreated) {
			return;
		}

		// layout without margin, because it works only as a container for the TabFolder
		setLayout( new FillLayout() );

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);

		// Color Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(I18N.PageLayoutEditor_StandardStyleComposite_Colors);
    		colorComposite = new ReactStyleColorComposite(tabFolder);
    		tabItem.setControl(colorComposite);;
    		colorComposite.addModifyListener(modifySupport);
		}

		// Font Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageLayoutEditor_StandardStyleComposite_Fonts);
			fontComposite = new ReactStyleFontComposite(tabFolder);
			tabItem.setControl(fontComposite);;
			fontComposite.addModifyListener(modifySupport);
		}

		// Size Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageLayoutEditor_StandardStyleComposite_Dimensions);
			sizeComposite = new ReactStyleSizeComposite(tabFolder);
			tabItem.setControl(sizeComposite);;
			sizeComposite.addModifyListener(modifySupport);
		}

		widgetsCreated = true;
	}


	@Override
	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;

		colorComposite.setPageLayout(pageLayout);
		fontComposite.setPageLayout(pageLayout);
		sizeComposite.setPageLayout(pageLayout);
	}


	@Override
	public void syncEntityToWidgets() {
		try {
			colorComposite.syncEntityToWidgets();
			fontComposite.syncEntityToWidgets();
			sizeComposite.syncEntityToWidgets();


			// generate a CSS based on the template of the selected style and the values in the widgets
			String css = getCSS();
			// set CSS to PageLayout
			pageLayout.setStyle(css);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Generate the CSS based on the template of the selected style and the current user input.
	 * @return
	 * @throws Exception
	 */
	private String getCSS() throws Exception {
		final String[] resultCSS = new String[1];

		// read values from widgets while still in display thread
		final Properties styleParameters = pageLayout.getStyleParameters();

		BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				File tmpDir = null;
				try {
					monitor.beginTask(UtilI18N.Working, 3);
					monitor.subTask(I18N.PageLayoutEditor_StandardStyleComposite_GenerateCss);

					// create tmp directory
					String prefix = "regasus-less";
					String suffix = ".tmp";
					File directory = null;
//					directory = new File("/Users/sacha/tmp"); // comment-in for debug
					tmpDir = FileHelper.createTempDirectory(prefix, suffix, directory);


					String less = generateLESS(styleParameters);

		    		// save generated less file to tmp directory
		    		File lessFile = new File(tmpDir, "_application_custom_variables.less");
		    		FileHelper.writeFile(lessFile, less.getBytes());

		    		monitor.worked(1);


		    		// copy zipped resources to tmp directory
		    		monitor.subTask(I18N.PageLayoutEditor_StandardStyleComposite_CopyResources);

		    		copyLessResources(tmpDir);
		    		monitor.worked(1);

		    		// compile application.less file to CSS file
		    		monitor.subTask(I18N.PageLayoutEditor_StandardStyleComposite_CompileLessToCss);
		    		File applicationLessFile = new File(tmpDir, "application.less");
		    		if ( ! applicationLessFile.exists()) {
		    			throw new RuntimeException(REACT_PROFILE_LESS_RESOURCES_FILE_NAME + " does not contain the expected file application.less.");
		    		}
		    		File cssFile = new File(tmpDir, "standard.css");
		    		LessToCssCompiler.compileLessToCss(applicationLessFile, cssFile);

		    		monitor.worked(1);


		    		// read CSS file
		    		if (cssFile.exists() && cssFile.length() > 0) {
    		    		byte[] cssBytes = FileHelper.readFile(cssFile);
    		    		resultCSS[0] = new String(cssBytes);
    		    		log("CSS successfully generated");
		    		}
		    		else {
		    			log("CSS could not be generated");
		    			throw new ErrorMessageException(I18N.PageLayoutEditor_StandardStyleComposite_CssCouldNotBeGenerated);
		    		}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
				finally {
					FileHelper.deleteRecursively(tmpDir);
				}

				monitor.done();
			}
		});

		return resultCSS[0];

//		log("Generate CSS");
//
//		// create tmp directory
//		String prefix = "regasus-less";
//		String suffix = ".tmp";
//		File tmpDir = FileHelper.createTempDirectory(prefix, suffix);
//
//		log("Created temporary directory: " + tmpDir.getAbsolutePath());
//
//		try {
//    		// generate LESS file
//    		String less = generateLESS();
//
//    		// save generated less file to tmp directory
//    		File lessFile = new File(tmpDir, "_application_custom_variables.less");
//    		FileHelper.writeFile(lessFile, less.getBytes());
//
//    		log("LESS file stored to " + lessFile.getAbsolutePath());
//
//    		// copy zipped resources to tmp directory
//    		copyLessResources(tmpDir);
//
//    		// compile application.less file to CSS file
//    		File applicationLessFile = new File(tmpDir, "application.less");
//    		if ( ! applicationLessFile.exists()) {
//    			throw new RuntimeException(LESS_RESOURCES_FILE_NAME + " does not contain the expected file application.less.");
//    		}
//    		File cssFile = new File(tmpDir, "standard.css");
//    		compileLessToCss(applicationLessFile, cssFile);
//
//    		// read CSS file
//    		byte[] cssBytes = FileHelper.readFile(cssFile);
//    		String cssStr = new String(cssBytes);
//
//    		log("CSS successfully generated");
//
//    		// return CSS
//    		return cssStr;
//		}
//		finally {
//			FileHelper.deleteRecursively(tmpDir);
//		}
	}


	/**
	 * Generate the LESS file based on the template taken from {@link PageLayout#getStyleTemplateContent()} and
	 * the parameters based on the values from the the widgets of this {@link Composite}.
	 * @return
	 */
	private String generateLESS(Properties styleParameters) {
		log("Generating LESS file based on template " + pageLayout.getStyleTemplate() + " and user input");
		String lessFileStr = null;
		try {
    		String template = pageLayout.getStyleTemplateContent();

    		if (template != null && styleParameters != null) {
       			lessFileStr = replaceVariables(template, styleParameters);
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		log("LESS file successfully generated");

		return lessFileStr;
	}


	/**
	 * Replace variables in <code>template</code> with values in <code>parameters</code>.
	 * @param template
	 * @param parameters
	 * @return
	 */
	private String replaceVariables(String template, Properties parameters) {
		Objects.requireNonNull(template);
		Objects.requireNonNull(parameters);

		List<String> variableList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();

		StringBuilder variable = new StringBuilder(128);

		for (Map.Entry<?, ?> entry : parameters.entrySet()) {
			variable.setLength(0);
			variable.append("${").append( entry.getKey() ).append("}");
			String value = entry.getValue() != null ? entry.getValue().toString() : "";

			variableList.add( variable.toString() );
			valueList.add(value);
		}

		String less = StringHelper.replace(template, variableList, valueList);
		return less;
	}


	/**
	 * Copy the resources that are necessary to compile the LESS file to the directory <code>dir</code>.
	 * The resources are stored in <code>less-resources.zip</code> which is contained in the entity-JAR.
	 * @param dir
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void copyLessResources(File dir) throws ClassNotFoundException, IOException {
		log("Copy resources from JAR to " + dir.getAbsolutePath());
		Class<?> styleTemplateClass = pageLayout.getStyleTemplateClass();
		if (styleTemplateClass != null) {
			InputStream inputStream = styleTemplateClass.getResourceAsStream(REACT_PROFILE_LESS_RESOURCES_FILE_NAME);
			if (inputStream != null) {
				ZipHelper.unzip(inputStream, dir);
			}
		}
		log("Resources successfully copied");
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	private void log(String text) {
		System.out.print( getClass().getSimpleName() );
		System.out.print(": ");
		System.out.println(text);
	}

}
