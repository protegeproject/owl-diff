package org.protege.editor.owl.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.analyzer.AnalyzerAlgorithm;
import org.protege.owl.diff.analyzer.ChangeAnalyzer;
import org.protege.owl.diff.analyzer.algorithms.IdentifyRetiredConcepts;
import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.Engine;
import org.protege.owl.diff.raw.algorithms.MatchByCode;
import org.protege.owl.diff.raw.algorithms.MatchById;
import org.protege.owl.diff.raw.algorithms.MatchStandardVocabulary;
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
	private ChangeAnalyzer analyzer;
	
	public DifferenceConfiguration(OWLModelManager manager) {
		this.manager = manager;
	}

	public void run(IRI ontologyIRI) throws OWLOntologyCreationException {
		workspaceOntology = manager.getActiveOntology();
		OWLOntologyManager altManager = OWLManager.createOWLOntologyManager();
		altOntology = altManager.loadOntology(ontologyIRI);
		engine = new Engine(manager.getOWLDataFactory(), workspaceOntology, altOntology, getParameters());
		engine.setDiffAlgorithms(getDiffAlgorithms().toArray(new DiffAlgorithm[0]));
		engine.run();
		analyzer = new ChangeAnalyzer(engine.getOwlDiffMap(), getParameters());
		analyzer.setAlgorithms(getAnalyzerAlgoritms().toArray(new AnalyzerAlgorithm[0]));
		analyzer.runAlgorithms();
	}
	
	/* TODO - set  with preferences and presets */
	public List<DiffAlgorithm> getDiffAlgorithms() {
		List<DiffAlgorithm> ret = new ArrayList<DiffAlgorithm>();
		ret.add(new MatchByCode());
		ret.add(new MatchStandardVocabulary());
		return ret;
	}

	/* TODO - set  with preferences and presets */
	public List<AnalyzerAlgorithm> getAnalyzerAlgoritms() {
		List<AnalyzerAlgorithm> ret = new ArrayList<AnalyzerAlgorithm>();
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

	public ChangeAnalyzer getAnalyzer() {
		return analyzer;
	}
}
