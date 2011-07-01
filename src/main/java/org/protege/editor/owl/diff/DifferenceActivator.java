package org.protege.editor.owl.diff;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.editor.core.ProtegeProperties;

public class DifferenceActivator implements BundleActivator {
	
	public void start(BundleContext context) {
		ProtegeProperties.getInstance().put("org.protege.differencecategory", "Ontology Comparison");
	}

	
	public void stop(BundleContext context) {
	}
}
