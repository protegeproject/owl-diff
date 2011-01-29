package org.protege.editor.owl.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.algorithms.IdentifyRetiredConcepts;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DifferenceConfiguration implements Disposable {
	public static final String ID = DifferenceConfiguration.class.getCanonicalName();
	
	private OWLModelManager manager;
	private OWLOntology workspaceOntology;
	private OWLOntology altOntology;
	private Engine engine;
	
	private Set<DifferenceListener> listeners = new HashSet<DifferenceListener>();
	private EntityBasedDiff selection;
	
	private List<AlignmentAlgorithm> diffAlgorithms;
	private List<PresentationAlgorithm> presentationAlgorithms;
	private Properties parameters;
	
	public static DifferenceConfiguration get(OWLModelManager manager) {
		DifferenceConfiguration configuration = manager.get(DifferenceConfiguration.class);
		if (configuration == null) {
			configuration = new DifferenceConfiguration(manager);
			manager.put(DifferenceConfiguration.class, configuration);
		}
		return configuration;
	}
	
	private DifferenceConfiguration(OWLModelManager manager) {
		this.manager = manager;
	}

	public void run(IRI ontologyIRI) throws OWLOntologyCreationException {
		reset();
		workspaceOntology = manager.getActiveOntology();
		OWLOntologyManager altManager = OWLManager.createOWLOntologyManager();
		altOntology = altManager.loadOntology(ontologyIRI);
		engine = new Engine(manager.getOWLDataFactory(), workspaceOntology, altOntology, getParameters());
		engine.setAlignmentAlgorithms(getDiffAlgorithms().toArray(new AlignmentAlgorithm[0]));
		engine.phase1();
		engine.phase2();
		fireStatusChanged(DifferenceEvent.DIFF_COMPLETED);
	}
	
	/* TODO - set  with preferences and presets */
	public List<AlignmentAlgorithm> getDiffAlgorithms() {
		if (diffAlgorithms == null) {
			diffAlgorithms = new ArrayList<AlignmentAlgorithm>();
			diffAlgorithms.add(new MatchByCode());
			diffAlgorithms.add(new MatchStandardVocabulary());
			diffAlgorithms.add(new MatchById());
		}
		return diffAlgorithms;
	}
	
	public void setDiffAlgorithms(List<AlignmentAlgorithm> diffAlgorithms) {
		this.diffAlgorithms = diffAlgorithms;
	}

	/* TODO - set  with preferences and presets */
	public List<PresentationAlgorithm> getPresentationAlgorithms() {
		if (presentationAlgorithms == null) {
			presentationAlgorithms = new ArrayList<PresentationAlgorithm>();
			presentationAlgorithms.add(new IdentifyRetiredConcepts());
		}
		return presentationAlgorithms;
	}
	
	public void setPresentationAlgorithms(List<PresentationAlgorithm> presentationAlgorithms) {
		this.presentationAlgorithms = presentationAlgorithms;
	}

	/* TODO - set  with preferences and presets */
	public Properties getParameters() {
		if (parameters == null) {
			parameters = new Properties();
			parameters.put("diff.by.code", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#code");
			parameters.put("retirement.class", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Retired_Concept");
		}
		return parameters;
	}
	
	public OWLModelManager getManager() {
		return manager;
	}

	public OWLOntology getWorkspaceOntology() {
		return workspaceOntology;
	}

	public OWLOntology getAltOntology() {
		return altOntology;
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
		for (DifferenceListener listener : listeners) {
			listener.statusChanged(event);
		}
	}
	
	public boolean isReady() {
		return engine != null;
	}
	
	public void reset() {
		fireStatusChanged(DifferenceEvent.DIFF_RESET);
		altOntology = null;
		engine = null;
	}
	
	public void dispose() {
		
	}
}
