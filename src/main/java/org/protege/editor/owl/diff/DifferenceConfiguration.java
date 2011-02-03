package org.protege.editor.owl.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.algorithms.IdentifyMergedConcepts;
import org.protege.owl.diff.present.algorithms.IdentifyRetiredConcepts;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.protege.owl.diff.service.RetirementClassService;
import org.protege.owl.diff.util.StopWatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DifferenceConfiguration implements Disposable {
	public static final String ID = DifferenceConfiguration.class.getCanonicalName();
	
	public static final Logger LOGGER = Logger.getLogger(DifferenceConfiguration.class);
	
	private OWLModelManager manager;
	private OWLOntology workspaceOntology;
	private OWLOntology baselineOntology;
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

	public void run(OWLOntology baselineOntology) throws OWLOntologyCreationException {
		reset();
		workspaceOntology = manager.getActiveOntology();
		this.baselineOntology = baselineOntology;
		
		StopWatch stopWatch = new StopWatch(LOGGER);
		LOGGER.info("Starting Difference calculation...");
		engine = new Engine(manager.getOWLDataFactory(), baselineOntology, workspaceOntology, getParameters());
		engine.setAlignmentAlgorithms(getDiffAlgorithms().toArray(new AlignmentAlgorithm[0]));
		engine.phase1();
		stopWatch.measure();
		LOGGER.info("Calculating presentation...");
		engine.setPresentationAlgorithms(getPresentationAlgorithms().toArray(new PresentationAlgorithm[0]));
		engine.phase2();
		stopWatch.finish();
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
			presentationAlgorithms.add(new IdentifyMergedConcepts());
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
	        parameters.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#code");
	        parameters.put(IdentifyMergedConcepts.MERGED_INTO_ANNOTATION_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Merge_Into");
	        parameters.put(RetirementClassService.RETIREMENT_STATUS_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Concept_Status");
	        parameters.put(RetirementClassService.RETIREMENT_STATUS_STRING, "Retired_Concept");
	        parameters.put(RetirementClassService.RETIREMENT_CLASS_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Retired_Concept_");
	        parameters.put(RetirementClassService.RETIREMENT_META_PROPERTIES + 0, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#OLD_PARENT");
	        parameters.put(RetirementClassService.RETIREMENT_META_PROPERTIES + 1, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#OLD_ASSOCIATION");
	        parameters.put(RetirementClassService.RETIREMENT_META_PROPERTIES + 2, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#OLD_CHILD");
	        parameters.put(RetirementClassService.RETIREMENT_META_PROPERTIES + 3, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#OLD_ROLE");
		}
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
		for (DifferenceListener listener : listeners) {
			listener.statusChanged(event);
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
