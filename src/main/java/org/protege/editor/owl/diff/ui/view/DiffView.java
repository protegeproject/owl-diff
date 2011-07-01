package org.protege.editor.owl.diff.ui.view;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.semanticweb.owlapi.model.OWLEntity;

public class DiffView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -953820310817783007L;
	public static final Logger LOGGER = Logger.getLogger(DiffView.class);
	
	private DifferenceList view;
	private JLabel status;
	
	private DifferenceListener listener = new DifferenceListener() {

		public void statusChanged(DifferenceEvent event) {
			updateStatus();
		}
		
	};
	
	protected void initialiseOWLView() throws Exception {
		setLayout(new BorderLayout());

		view = new DifferenceList(getOWLEditorKit());
		add(view, BorderLayout.CENTER);
		
		status = new JLabel();
		add(status, BorderLayout.SOUTH);
		DifferenceManager diffs = DifferenceManager.get(getOWLModelManager());
		diffs.addDifferenceListener(listener);
		updateStatus();
	}
	
	
	
	protected void disposeOWLView() {
		DifferenceManager diffs = DifferenceManager.get(getOWLModelManager());
		diffs.removeDifferenceListener(listener);
		try {
			view.dispose();
		}
		catch (Exception e) {
			ProtegeApplication.getErrorLog().logError(e);
		}
	}

	private void updateStatus() {
		DifferenceManager diffs = DifferenceManager.get(getOWLModelManager());
		if (diffs.isReady()) {
			OwlDiffMap diffMap = diffs.getEngine().getOwlDiffMap();
			StringBuffer sb = new StringBuffer();
			sb.append("Displaying differences.  ");
			sb.append(diffMap.getUnmatchedTargetEntities().size());
			sb.append(" entities created, ");
			sb.append(diffMap.getUnmatchedSourceEntities().size());
			sb.append(" entities deleted, ");
			int refactored = 0;
			int otherwiseChanged = 0;
			Changes changes = diffs.getEngine().getChanges();
			for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
				OWLEntity sourceEntity = diff.getSourceEntity();
				OWLEntity targetEntity = diff.getTargetEntity();
				if (sourceEntity != null && targetEntity != null) {
					if (!sourceEntity.getIRI().equals(targetEntity.getIRI())) {
						refactored++;
					}
					else {
						otherwiseChanged++;
					}
				}
			}
			sb.append(refactored);
			sb.append(" entities renamed, ");
			sb.append(otherwiseChanged);
			sb.append(" entities otherwise altered.");
			status.setText(sb.toString());
		}
		else {
			status.setText("Differences not ready");
		}
	}

}
