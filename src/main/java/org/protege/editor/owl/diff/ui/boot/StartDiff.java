package org.protege.editor.owl.diff.ui.boot;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.ProgressMonitor;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.core.ui.workspace.WorkspaceTabPlugin;
import org.protege.editor.core.ui.workspace.WorkspaceTabPluginLoader;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class StartDiff extends ProtegeOWLAction {
	private static final long serialVersionUID = -5400122637724517976L;

	public static RenderingService getRenderingService(OWLModelManager modelManager) {
		DifferenceManager differenceManager = DifferenceManager.get(modelManager);
		return RenderingService.get(differenceManager.getEngine());
	}
	
	public static OWLEditorKit getAltEditorKit(Engine e) {
		OntologyInAltWorkspaceFactory factory = e.getService(OntologyInAltWorkspaceFactory.class);
		return factory != null ? factory.getAltEditorKit() : null;
	}
	
	

	public void initialise() {
	}

	
	public void dispose() {

	}

	
	public void actionPerformed(ActionEvent e) {
		ConfigureDifferenceRun confWindow = new ConfigureDifferenceRun(getOWLEditorKit());
		confWindow.setVisible(true);
		final File f = confWindow.getBaseline();
		final boolean loadInSeparateWorkspace = confWindow.getOpenBaselineInSeparateWindow();
		final Configuration configuration = confWindow.getConfiguration();
		if (confWindow.isCommit() && f != null) {
			final ProgressMonitor monitor = new ProgressMonitor(getOWLWorkspace(), "Calculating Differences", "", 0, 2);
			monitor.setMillisToPopup(100);
			new Thread(new Runnable() {
				
				public void run() {
					calculateDiffs(f, configuration, monitor, loadInSeparateWorkspace);
				}
			}).start();
		}

	}
	
	private void calculateDiffs(File baselineOntologyLocation, Configuration configuration, ProgressMonitor monitor, boolean loadInSeparateWorkspace) {
		try {
			monitor.setNote("Loading ontology for comparison");
			OWLOntology baselineOntology;
			OntologyInAltWorkspaceFactory factory = null;
			if (loadInSeparateWorkspace) {
				factory = new OntologyInAltWorkspaceFactory(getOWLEditorKit());
				baselineOntology = factory.loadInSeparateSynchronizedWorkspace(IRI.create(baselineOntologyLocation));
			}
			else {
				OWLOntologyLoaderConfiguration ontologyLoaderConfig = new OWLOntologyLoaderConfiguration();
				ontologyLoaderConfig.setSilentMissingImportsHandling(true);
				OWLOntologyManager baselineManager = OWLManager.createOWLOntologyManager();
				baselineOntology = baselineManager.loadOntologyFromOntologyDocument(new FileDocumentSource(baselineOntologyLocation), ontologyLoaderConfig);
			}
			monitor.setProgress(1);
			
			monitor.setNote("Calculating differences");
			DifferenceManager diffs = DifferenceManager.get(getOWLModelManager());
			diffs.run(baselineOntology, configuration);
			monitor.setProgress(2);
			if (loadInSeparateWorkspace) {
				SynchronizeDifferenceListener.synchronize(diffs, factory.getAltEditorKit(), false);
				diffs.getEngine().addService(factory);
			}
			SynchronizeDifferenceListener.synchronize(diffs, getOWLEditorKit(), true);
			
			selectTab();
		}
		catch (Throwable t) {
			ProtegeApplication.getErrorLog().logError(t);
		}
		finally {
			monitor.close();
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
