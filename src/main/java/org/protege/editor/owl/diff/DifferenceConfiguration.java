package org.protege.editor.owl.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.algorithms.IdentifyRetiredConcepts;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DifferenceConfiguration {
	public static final String ID = DifferenceConfiguration.class.getCanonicalName();
	
	private OWLModelManager manager;
	private OWLOntology workspaceOntology;
	private OWLOntology altOntology;
	private Engine engine;
	
	public DifferenceConfiguration(OWLModelManager manager) {
		this.manager = manager;
	}

	public void run(IRI ontologyIRI) throws OWLOntologyCreationException {
		workspaceOntology = manager.getActiveOntology();
		OWLOntologyManager altManager = OWLManager.createOWLOntologyManager();
		altOntology = altManager.loadOntology(ontologyIRI);
		engine = new Engine(manager.getOWLDataFactory(), workspaceOntology, altOntology, getParameters());
		engine.setAlignmentAlgorithms(getDiffAlgorithms().toArray(new AlignmentAlgorithm[0]));
		engine.phase1();
		engine.phase2();
	}
	
	/* TODO - set  with preferences and presets */
	public List<AlignmentAlgorithm> getDiffAlgorithms() {
		List<AlignmentAlgorithm> ret = new ArrayList<AlignmentAlgorithm>();
		ret.add(new MatchByCode());
		ret.add(new MatchStandardVocabulary());
		return ret;
	}

	/* TODO - set  with preferences and presets */
	public List<PresentationAlgorithm> getPresentationAlgorithms() {
		List<PresentationAlgorithm> ret = new ArrayList<PresentationAlgorithm>();
		ret.add(new IdentifyRetiredConcepts());
		return ret;
	}

	/* TODO - set  with preferences and presets */
	public Properties getParameters() {
		Properties p = new Properties();
		p.put("diff.by.code", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#code");
		p.put("retirement.class", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Retired_Concept");
		return p;
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
}
