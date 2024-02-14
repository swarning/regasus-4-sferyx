package de.regasus.email.template.variables;

import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.report.script.ScriptContext;
import com.lambdalogic.report.script.ScriptHelper;

/**
 * A simple holder for two Strings, the first one is needed, the second is optional. Is used in the
 * {@link VariablesTable}.
 * 
 * @author manfred
 * 
 */
public class VariableValuePair {

	private String variable;

	private String sampleValue;


	public VariableValuePair(String variable) {
		this.variable = variable;
	}


	public String getSampleValue() {
		return sampleValue;
	}


	public void setSampleValue(String sampleValue) {
		this.sampleValue = sampleValue;
	}


	public String getVariable() {
		return variable;
	}


	public void setVariable(String variable) {
		this.variable = variable;
	}


	@Override
	public String toString() {
		return "VariableValuePair[variable=" + variable + ",sampleValue=" + sampleValue + "]\n";
	}


	// *************************************************************************
	// * Static helper methods, acting on lists of such VariableValuePairs
	// *

	public static void cleanValues(List<VariableValuePair> list) {
		for (VariableValuePair variableValuePair : list) {
			variableValuePair.setSampleValue(null);
		}
	}


	public static List<String> getVariableList(List<VariableValuePair> list) {
		List<String> result = new ArrayList<String>(list.size());
		for (VariableValuePair variableValuePair : list) {
			result.add(variableValuePair.getVariable());
		}
		return result;
	}


	public static List<VariableValuePair> createFromVariableList(List<String> list) {
		List<VariableValuePair> result = new ArrayList<VariableValuePair>(list.size());
		for (String variable : list) {
			result.add(new VariableValuePair(variable));
		}
		return result;
	}


	/**
	 * The values are evaluated against the given context, any non-evaluated variables are removed from the sample
	 * value.
	 */
	public static void evaluateValuesWithContext(List<VariableValuePair> pairs, ScriptContext context) {

		for (VariableValuePair pair : pairs) {
			String var = pair.getVariable();
			String value = context.evaluateString(var);
			String valueWithoutVars = ScriptHelper.removeScripts(value);
			pair.setSampleValue(valueWithoutVars);
		}
	}

}
