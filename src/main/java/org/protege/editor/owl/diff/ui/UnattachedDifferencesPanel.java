package org.protege.editor.owl.diff.ui;

import java.awt.BorderLayout;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;

public class UnattachedDifferencesPanel extends JPanel {
	private static final long serialVersionUID = 4039090974349587237L;
	private DifferenceManager differenceManager;
	private DifferenceTableModel model;
	private DifferenceListener listener = new DifferenceListener() {
		public void statusChanged(DifferenceEvent event) {
			switch (event) {
			case DIFF_COMPLETED:
				updateModel();
				break;
			case DIFF_RESET:
				model.clear();
				break;
			}
		}
	};

	public UnattachedDifferencesPanel(DifferenceManager differenceManager) {
		setLayout(new BorderLayout());
		this.differenceManager = differenceManager;
		model = new DifferenceTableModel(differenceManager);
		add(new JScrollPane(new DifferenceTable(model)), BorderLayout.CENTER);
		differenceManager.addDifferenceListener(listener);
		if (differenceManager.isReady()) {
			updateModel();
		}
	}
	
	private void updateModel() {
		model.setMatches(getUnattachedMatchedAxioms(differenceManager));
	}
	
	private SortedSet<MatchedAxiom> getUnattachedMatchedAxioms(DifferenceManager differenceManager) {
		Changes changes = differenceManager.getEngine().getChanges();
		SortedSet<MatchedAxiom> matches = new TreeSet<MatchedAxiom>();
		for (OWLAxiom axiom : changes.getUnmatchedSourceAxiomsWithNoSubject()) {
			matches.add(new MatchedAxiom(axiom, null, MatchedAxiom.AXIOM_DELETED));
		}
		for (OWLAxiom axiom : changes.getUnmatchedTargetAxiomsWithNoSubject()) {
			matches.add(new MatchedAxiom(null, axiom, MatchedAxiom.AXIOM_ADDED));
		}
		return matches;
	}
	
	public void dispose() {
		differenceManager.removeDifferenceListener(listener);
		differenceManager = null;
		model = null;
	}
	
}
