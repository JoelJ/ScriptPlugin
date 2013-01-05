package com.attask.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * I couldn't figure out a way to get Jelly/Stapler to let me auto-bind a List&lt;String&gt; so I created this as a workaround.
 * Both the equals and hashcode simply check the string value.
 *
 * User: Joel Johnson
 * Date: 11/28/12
 * Time: 4:00 PM
 */
public class Parameter extends AbstractDescribableImpl<Parameter> implements Serializable {
	private String parameter;

	@DataBoundConstructor
	public Parameter(String parameter) {
		this.parameter = parameter;
	}

	public String getParameter() {
		return parameter;
	}

	public static List<String> toStringList(Collection<Parameter> parameters, EnvVars environment) {
		List<String> result = new ArrayList<String>(parameters.size());
		for (Parameter parameter : parameters) {
			result.add(environment.expand(parameter.getParameter()));
		}
		return result;
	}

	@Override
	public String toString() {
		return parameter;
	}

	@Override
	public boolean equals(Object o) {
		if(parameter == null) {
			return o == null;
		}
		return o != null && o instanceof Parameter && parameter.equals(((Parameter) o).getParameter());
	}

	@Override
	public int hashCode() {
		return parameter != null ? parameter.hashCode() : 0;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<Parameter> {
		@Override
		public String getDisplayName() {
			return "Parameter";
		}
	}
}
