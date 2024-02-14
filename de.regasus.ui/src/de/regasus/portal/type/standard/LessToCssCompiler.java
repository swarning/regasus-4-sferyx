package de.regasus.portal.type.standard;

import java.io.File;
import java.io.IOException;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import com.lambdalogic.util.FileHelper;


/**
 * Encapsulation of the LESS compiler.
 */
public class LessToCssCompiler {

	/**
	 * Compile the LESS file <code>lessFile</code>to a CSS and save it to <code>cssFile</code>.
	 * @param lessFile
	 * @param cssFile
	 * @throws Less4jException
	 * @throws IOException
	 * @throws LessException
	 */
	public static void compileLessToCss(File lessFile, File cssFile) throws Less4jException, IOException {
		log("Compile " + lessFile.getAbsolutePath() + " to " + cssFile.getAbsolutePath());

		// instantiate the LESS compiler
		LessCompiler lessCompiler = new ThreadUnsafeLessCompiler();


		// compile LESS input file to CSS output file
		CompilationResult result = lessCompiler.compile(lessFile);

		String css = result.getCss();
		if (css != null && css.length() > 0) {
			log("CSS successfully compiled");
			FileHelper.writeFile(cssFile, css.getBytes());
		}
		else {
			log("CSS could not be compiled");
		}
	}


	private static void log(String text) {
		System.out.print( LessToCssCompiler.class.getSimpleName() );
		System.out.print(": ");
		System.out.println(text);
	}


	public static void main(String[] args) throws Exception {
		File dir = new File("/Users/sacha/tmp/regasus-less-1");
//		File dir = new File("/Users/sacha/tmp/regasus-less-2");

		File applicationLessFile = new File(dir, "application.less");
		File cssFile = new File(dir, "standard.css");
		LessToCssCompiler.compileLessToCss(applicationLessFile, cssFile);
	}





//	static void compileLessToCss(File lessFile, File cssFile) throws IOException, LessException {
//		log("Compile " + lessFile.getAbsolutePath() + " to " + cssFile.getAbsolutePath());
//
//		// instantiate the LESS compiler
//		LessCompiler lessCompiler = new LessCompiler();
//
//		// compile LESS input file to CSS output file
//		lessCompiler.compile(lessFile, cssFile);
//
//		if ( cssFile.exists() && cssFile.length() > 0) {
//			log("CSS successfully compiled");
//		}
//		else {
//			log("CSS could not be compiled");
//		}
//	}

}
