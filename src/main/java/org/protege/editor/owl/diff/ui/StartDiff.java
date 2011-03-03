package org.protege.editor.owl.diff.ui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.ProgressMonitor;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.core.ui.workspace.WorkspaceTabPlugin;
import org.protege.editor.core.ui.workspace.WorkspaceTabPluginLoader;
import org.protege.editor.owl.diff.model.DifferenceConfiguration;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class StartDiff extends ProtegeOWLAction {
	private static final long serialVersionUID = -5400122637724517976L;
	private boolean loadInSeparateWorkspace = false;


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
						OWLOntology ontology;
						if (loadInSeparateWorkspace) {
							ontology = new SynchronizedWorkspaceFactory(getOWLEditorKit()).loadInSeparateSynchronizedWorkspace(IRI.create(f));
						}
						else {
							OWLOntologyManager baselineManager = OWLManager.createOWLOntologyManager();
							baselineManager.setSilentMissingImportsHandling(true);
							ontology = baselineManager.loadOntologyFromOntologyDocument(f);
						}
						monitor.setProgress(1);
						
						monitor.setNote("Calculating differences");
						DifferenceConfiguration diffs = DifferenceConfiguration.get(getOWLModelManager());
						diffs.run(ontology);
						monitor.setProgress(2);
						
						selectTab();
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
	
	private void selectTab() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		String tabId = "org.protege.editor.owl.diff.DifferenceTable";
		OWLWorkspace workspace = getOWLWorkspace();
		if (!workspace.containsTab(tabId)) {
			WorkspaceTabPluginLoader loader = new WorkspaceTabPluginLoader(workspace);
			for (WorkspaceTabPlugin plugin : loader.getPlugins()) {
				if (plugin.getId().equals(tabId)) {
					WorkspaceTab tab = plugin.newInstance();
					workspace.addTab(tab);
					break;
				}
			}
		}
		WorkspaceTab tab = workspace.getWorkspaceTab(tabId);
		workspace.setSelectedTab(tab);
	}

}
