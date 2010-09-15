package org.protege.editor.owl.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.analyzer.AnalyzerAlgorithm;
import org.protege.owl.diff.analyzer.ChangeAnalyzer;
import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.Engine;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class DifferenceConfiguration {
	private static Map<OWLModelManager, DifferenceConfiguration> instanceMap = new HashMap<OWLModelManager, DifferenceConfiguration>();
	private OWLModelManager manager;
	private OWLOntology workspaceOntology;
	private OWLOntology altOntology;
	private Engine engine;
	private ChangeAnalyzer analyzer;
	
	public static DifferenceConfiguration getInstance(OWLModelManager manager) {
		DifferenceConfiguration instance = instanceMap.get(manager);
		if (instance == null) {
			instance = new DifferenceConfiguration(manager);
			instanceMap.put(manager, instance);
		}
		return instance;
	}
	
	public DifferenceConfiguration(OWLModelManager manager) {
		this.manager = manager;
	}

	public void run(IRI ontologyIRI) {
		
	}
	
	/* TODO - set  with preferences and presets */
	public List<DiffAlgorithm> getDiffAlgorithms() {
		List<DiffAlgorithm> ret = new ArrayList<DiffAlgorithm>();
		return ret;
	}

	/* TODO - set  with preferences and presets */
	public List<AnalyzerAlgorithm> getAnalyzerAlgoritms() {
		List<AnalyzerAlgorithm> ret = new ArrayList<AnalyzerAlgorithm>();
		return ret;
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
