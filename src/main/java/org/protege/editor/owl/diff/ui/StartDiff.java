package org.protege.editor.owl.diff.ui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.ProgressMonitor;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.diff.model.DifferenceConfiguration;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class StartDiff extends ProtegeOWLAction {
	private static final long serialVersionUID = -5400122637724517976L;


	public void initialise() throws Exception {

	}

	
	public void dispose() throws Exception {

	}

	
	public void actionPerformed(ActionEvent e) {
		final File f = new UIHelper(getOWLEditorKit()).chooseOWLFile("Choose File to compare");
		if (f != null) {
			final ProgressMonitor monitor = new ProgressMonitor(getOWLWorkspace(), "Calculating Differences", "", 0, 2);
			monitor.setMillisToPopup(100);
			new Thread(new Runnable() {
				
				public void run() {
					try {
						monitor.setNote("Loading ontology for comparison");
						OWLOntologyManager baselineManager = OWLManager.createOWLOntologyManager();
						baselineManager.setSilentMissingImportsHandling(true);
						OWLOntology ontology = baselineManager.loadOntologyFromOntologyDocument(f);
						monitor.setProgress(1);
						
						monitor.setNote("Calculating differences");
						DifferenceConfiguration diffs = DifferenceConfiguration.get(getOWLModelManager());
						diffs.run(ontology);
						monitor.setProgress(2);
					}
					catch (Throwable t) {
						ProtegeApplication.getErrorLog().logError(t);
					}
					finally {
						monitor.close();
					}
				}
			}).start();
		}

	}

}
