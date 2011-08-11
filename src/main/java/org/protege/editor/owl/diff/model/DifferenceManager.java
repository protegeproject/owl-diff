package org.protege.editor.owl.diff.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.diff.ui.boot.ProtegeShortFormProvider;
import org.protege.editor.owl.diff.ui.boot.StartDiff;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.service.RenderingService;
import org.protege.owl.diff.util.StopWatch;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;

public class DifferenceManager implements Disposable {
	public static final String ID = DifferenceManager.class.getCanonicalName();
	
	public static final Logger LOGGER = Logger.getLogger(DifferenceManager.class);
	
	private OWLModelManager manager;
	private OWLOntology workspaceOntology;
	private OWLOntology baselineOntology;
	private Engine engine;
	
	private Set<DifferenceListener> listeners = new HashSet<DifferenceListener>();
	private EntityBasedDiff selection;
	
	private List<AlignmentAlgorithm> diffAlgorithms;
	private List<PresentationAlgorithm> presentationAlgorithms;
	private Map<String, String> parameters;
	
	public static DifferenceManager get(OWLModelManager manager) {
		DifferenceManager configuration = manager.get(DifferenceManager.class);
		if (configuration == null) {
			configuration = new DifferenceManager(manager);
			manager.put(DifferenceManager.class, configuration);
		}
		return configuration;
	}
	
	private DifferenceManager(OWLModelManager manager) {
		this.manager = manager;
	}

	public void run(OWLOntology baselineOntology, Configuration configuration) throws OWLOntologyCreationException, InstantiationException, IllegalAccessException {
		reset();
		workspaceOntology = manager.getActiveOntology();
		this.baselineOntology = baselineOntology;
		
		StopWatch stopWatch = new StopWatch(LOGGER);
		LOGGER.info("Starting Difference calculation...");
		engine = new Engine(baselineOntology, workspaceOntology);
		setupRendering(engine);
		configuration.configure(engine);
		engine.phase1();
		stopWatch.measure();
		LOGGER.info("Calculating presentation...");
		engine.phase2();
		stopWatch.finish();
		fireStatusChanged(DifferenceEvent.DIFF_COMPLETED);
	}
	
	private void setupRendering(Engine e) {		
		RenderingService renderer = RenderingService.get(e);
		renderer.setSourceShortFormProvider(new ProtegeShortFormProvider(StartDiff.getAltEditorKit(manager).getModelManager()));
		renderer.setSourceShortFormProvider(new ProtegeShortFormProvider(manager));
	}
	
	/* TODO - set  with preferences and presets */
	public List<AlignmentAlgorithm> getDiffAlgorithms() {
		return diffAlgorithms;
	}
	
	public void setDiffAlgorithms(List<AlignmentAlgorithm> diffAlgorithms) {
		this.diffAlgorithms = diffAlgorithms;
	}

	/* TODO - set  with preferences and presets */
	public List<PresentationAlgorithm> getPresentationAlgorithms() {
		return presentationAlgorithms;
	}
	
	public void setPresentationAlgorithms(List<PresentationAlgorithm> presentationAlgorithms) {
		this.presentationAlgorithms = presentationAlgorithms;
	}

	/* TODO - set  with preferences and presets */
	public Map<String,String> getParameters() {
		return parameters;
	}
	
	public OWLModelManager getManager() {
		return manager;
	}

	public OWLOntology getWorkspaceOntology() {
		return workspaceOntology;
	}

	public OWLOntology getBaselineOntology() {
		return baselineOntology;
	}

	public Engine getEngine() {
		return engine;
	}
	
	public EntityBasedDiff getSelection() {
		return selection;
	}
	
	public void setSelection(EntityBasedDiff selection) {
		this.selection = selection;
		fireStatusChanged(DifferenceEvent.SELECTION_CHANGED);
	}
	
	public void addDifferenceListener(DifferenceListener listener) {
		listeners.add(listener);
	}
	
	public void removeDifferenceListener(DifferenceListener listener) {
		listeners.remove(listener);
	}
	
	private void fireStatusChanged(DifferenceEvent event) {
		for (DifferenceListener listener : new ArrayList<DifferenceListener>(listeners)) {
			try {
				listener.statusChanged(event);
			}
			catch (Exception e) {
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}
	
	public boolean isReady() {
		return engine != null;
	}
	
	public void reset() {
		fireStatusChanged(DifferenceEvent.DIFF_RESET);
		baselineOntology = null;
		engine = null;
	}
	
	public void dispose() {
		
	}
}
