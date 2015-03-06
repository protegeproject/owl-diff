package org.protege.editor.owl.diff;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.editor.core.ProtegeApplication;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.util.ClassLoaderWrapper;
import org.protege.owl.diff.util.Util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class DifferenceActivator implements BundleActivator {
	private static BundleContext context;
	
	public static List<Class<? extends AlignmentAlgorithm>> createAlignmentAlgorithms() {
		List<Class<? extends AlignmentAlgorithm>> algorithms = new ArrayList<Class<? extends AlignmentAlgorithm>>();
    	for (Bundle b : context.getBundles()) {
    		try {
    			algorithms.addAll(Util.createDeclaredAlignmentAlgorithms(wrapBundle(b)));
    		}
    		catch (IOException ioe) {
    			ProtegeApplication.getErrorLog().logError(ioe);
    		}
    	}
		return algorithms;
	}
	
	public static List<Class<? extends PresentationAlgorithm>> createPresentationAlgorithms() {
		List<Class<? extends PresentationAlgorithm>> algorithms = new ArrayList<Class<? extends PresentationAlgorithm>>();
    	for (Bundle b : context.getBundles()) {
    		try {
    			algorithms.addAll(Util.createDeclaredPresentationAlgorithms(wrapBundle(b)));
    		}
    		catch (IOException ioe) {
    			ProtegeApplication.getErrorLog().logError(ioe);
    		}
    	}
		return algorithms;		
	}
	
	private static ClassLoaderWrapper wrapBundle(final Bundle b) {
		return new ClassLoaderWrapper() {
			
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				return b.loadClass(name);
			}
			
			@SuppressWarnings("unchecked")
			public Enumeration<URL> getResources(String name) throws IOException {
				return b.getResources(name);
			}
		};
	}
	
	public void start(BundleContext context) {
		DifferenceActivator.context = context;
	}

	
	public void stop(BundleContext context) {
	}
}
