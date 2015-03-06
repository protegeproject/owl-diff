package org.protege.editor.owl.diff.ui.boot;

import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;
import java.util.logging.Logger;

public class OntologyInAltWorkspaceFactory implements Disposable {
	public static final Logger LOGGER = Logger.getLogger(OntologyInAltWorkspaceFactory.class.getName());
	
	private OWLEditorKit eKit;
	private OWLEditorKit altEditorKit;
	private boolean display;
	
	public OntologyInAltWorkspaceFactory(OWLEditorKit eKit, boolean display) {
		this.eKit = eKit;
		this.display = display;
	}
	
	public OWLOntology loadInSeparateSynchronizedWorkspace(IRI ontologyLocation) throws OWLOntologyCreationException {
		long startTime = System.currentTimeMillis();
		try {
			altEditorKit = (OWLEditorKit) (eKit.getEditorKitFactory()).createEditorKit();
		}
		catch (Exception e) {
			throw new OWLOntologyCreationException("Could not create editor kit", e);
		}
		OWLOntology ontology = null;
		try {
			ontology = loadOntology(altEditorKit.getOWLModelManager().getOWLOntologyManager(), ontologyLocation);
		}
		finally {
			if (ontology == null) {
				altEditorKit.dispose();
			}
		}
		altEditorKit.getOWLModelManager().setActiveOntology(ontology);
		altEditorKit.put(UUID.randomUUID(), this); // ensure its disposal.
		if (display) {
			ProtegeManager.getInstance().getEditorKitManager().addEditorKit(altEditorKit);
			altEditorKit.getOWLWorkspace().setTitle("Workspace for original version of ontology");
			Point originalPoint = SwingUtilities.getAncestorOfClass(JFrame.class, eKit.getWorkspace()).getLocation();
			Point newPoint = new Point(originalPoint.x + 60, originalPoint.y + 20);
			SwingUtilities.getAncestorOfClass(JFrame.class, altEditorKit.getOWLWorkspace()).setLocation(newPoint);
		}
		eKit.getOWLWorkspace().requestFocusInWindow();
		LOGGER.info("Ontology load took " + (System.currentTimeMillis() - startTime) + "ms.");
		return ontology;
	}

	/**
	 *  Loads the ontology using the ontology manager.
	 *
	 * This method is trivial.  The purpose of introducing this method is to allow the caller to make any
	 * needed fixups to the ontology before the ontology is added to the separate workspace.
	 *
	 * @param manager The manager
	 * @param ontologyLocation The ontology location
	 * @return The ontology
	 * @throws OWLOntologyCreationException If there was a problem creating the ontology
	 */
	protected OWLOntology loadOntology(OWLOntologyManager manager, IRI ontologyLocation) throws OWLOntologyCreationException {
		return manager.loadOntologyFromOntologyDocument(ontologyLocation);
	}
	
	public OWLEditorKit getAltEditorKit() {
		return altEditorKit;
	}
	
	public void dispose() {
		eKit = null;
		altEditorKit = null;
	}
}
