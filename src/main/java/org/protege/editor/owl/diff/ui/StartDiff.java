package org.protege.editor.owl.diff.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ProgressMonitor;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.core.ui.workspace.WorkspaceTabPlugin;
import org.protege.editor.core.ui.workspace.WorkspaceTabPluginLoader;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;


public class StartDiff extends ProtegeOWLAction {
	private static final long serialVersionUID = -5400122637724517976L;


	public void initialise() throws Exception {

	}

	
	public void dispose() throws Exception {

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
					try {
						monitor.setNote("Loading ontology for comparison");
						OWLOntology baselineOntology;
						OntologyInAltWorkspaceFactory factory = null;
						if (loadInSeparateWorkspace) {
							factory = new OntologyInAltWorkspaceFactory(getOWLEditorKit());
							baselineOntology = factory.loadInSeparateSynchronizedWorkspace(IRI.create(f));
						}
						else {
							OWLOntologyManager baselineManager = OWLManager.createOWLOntologyManager();
							baselineManager.setSilentMissingImportsHandling(true);
							baselineOntology = baselineManager.loadOntologyFromOntologyDocument(f);
						}
						monitor.setProgress(1);
						
						monitor.setNote("Calculating differences");
						DifferenceManager diffs = DifferenceManager.get(getOWLModelManager());
						diffs.run(baselineOntology, configuration);
						monitor.setProgress(2);
						if (loadInSeparateWorkspace) {
							SynchronizeDifferenceListener.synchronize(diffs, factory.getAltEditorKit(), false);
						}
						SynchronizeDifferenceListener.synchronize(diffs, getOWLEditorKit(), true);
						setupRendering(diffs);
						
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
	
	private void setupRendering(DifferenceManager diffs) {
		Engine e = diffs.getEngine();
		OwlDiffMap diffMap = e.getOwlDiffMap();
		OWLDataFactory factory = e.getOWLDataFactory();
		RenderingService renderer = RenderingService.get(e);
		OWLRendererPreferences preferences = OWLRendererPreferences.getInstance();
		List<String> langs = preferences.getAnnotationLangs();
		Map<OWLAnnotationProperty, List<String>> langMap = new HashMap<OWLAnnotationProperty, List<String>>();
		List<OWLAnnotationProperty> annotationProperties = new ArrayList<OWLAnnotationProperty>();
		for (IRI iri : preferences.getAnnotationIRIs()) {
			OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(iri);
			annotationProperties.add(annotationProperty);
			langMap.put(annotationProperty, langs);
		}
		
		OWLOntology sourceOntology = diffMap.getSourceOntology();
		OWLOntologySetProvider sourceOntologies = new OWLOntologyImportsClosureSetProvider(sourceOntology.getOWLOntologyManager(), sourceOntology);
		renderer.setSourceShortFormProvider(new AnnotationValueShortFormProvider(annotationProperties, langMap, sourceOntologies));
		
		OWLOntology targetOntology = diffMap.getTargetOntology();
		OWLOntologySetProvider targetOntologies = new OWLOntologyImportsClosureSetProvider(targetOntology.getOWLOntologyManager(), targetOntology);
		renderer.setTargetShortFormProvider(new AnnotationValueShortFormProvider(annotationProperties, langMap, targetOntologies));
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
