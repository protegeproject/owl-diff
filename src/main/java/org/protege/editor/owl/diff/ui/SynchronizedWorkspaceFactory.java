package org.protege.editor.owl.diff.ui;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKitManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceConfiguration;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class SynchronizedWorkspaceFactory {
	private OWLEditorKit eKit;
	
	public SynchronizedWorkspaceFactory(OWLEditorKit eKit) {
		this.eKit = eKit;
	}
	
	public OWLOntology loadInSeparateSynchronizedWorkspace(IRI ontologyLocation) throws OWLOntologyCreationException {
		OWLEditorKit altEditorKit;
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
		ProtegeManager.getInstance().getEditorKitManager().addEditorKit(altEditorKit);
		synchronizeWorkspaces(altEditorKit);
		eKit.getOWLWorkspace().requestFocusInWindow();
		return ontology;
	}
	
	/**
	 * Loads the ontology using the ontology manager.
	 * 
	 * This method is trivial.  The purpose of introducing this method is to allow the caller to make any 
	 * needed fixups to the ontology before the ontology is added to the separate workspace.
	 * 
	 * @param manager
	 * @param ontologyLocation
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	protected OWLOntology loadOntology(OWLOntologyManager manager, IRI ontologyLocation) throws OWLOntologyCreationException {
		return manager.loadOntologyFromOntologyDocument(ontologyLocation);
	}
	
	private void synchronizeWorkspaces(OWLEditorKit altEditorKit) {
		DifferenceConfiguration dc = DifferenceConfiguration.get(eKit.getModelManager());
		OWLSelectionModel selectionModel = altEditorKit.getOWLWorkspace().getOWLSelectionModel();
		dc.addDifferenceListener(new SynchronizingListener(dc, selectionModel));
	}
	
	private class SynchronizingListener implements DifferenceListener {
		private boolean ready = false;
		private OWLSelectionModel selectionModel;
		private DifferenceConfiguration diffConfig;
		
		public SynchronizingListener(DifferenceConfiguration diffConfig, OWLSelectionModel selectionModel) {
			this.diffConfig = diffConfig;
			this.selectionModel = selectionModel;
		}

		public void statusChanged(DifferenceEvent event) {
			switch (event) {
			case DIFF_COMPLETED:
				ready = true;
				break;
			case SELECTION_CHANGED:
				if (ready) {
					EntityBasedDiff diff = diffConfig.getSelection();
					OWLEntity entity = diff.getSourceEntity();
					if (entity != null) {
						selectionModel.setSelectedEntity(entity);
					}
				}
				break;
			case DIFF_RESET:
				if (ready) {
					diffConfig.removeDifferenceListener(SynchronizingListener.this);
				}
				break;
			default:
				throw new IllegalStateException("Programmer error");
			}
		}
		
	}
}
