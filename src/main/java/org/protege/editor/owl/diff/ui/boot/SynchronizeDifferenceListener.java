package org.protege.editor.owl.diff.ui.boot;

import java.util.UUID;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.semanticweb.owlapi.model.OWLEntity;

public class SynchronizeDifferenceListener implements DifferenceListener, Disposable {
	private DifferenceManager differenceManager;
	private OWLEditorKit eKit;
	private boolean isTarget;
	
	public static void synchronize(DifferenceManager differenceManager, OWLEditorKit eKit, boolean isTarget) {
		differenceManager.addDifferenceListener(new SynchronizeDifferenceListener(differenceManager, eKit, isTarget));
	}
	
	public SynchronizeDifferenceListener(DifferenceManager differenceManager, OWLEditorKit eKit, boolean isTarget) {
		this.differenceManager = differenceManager;
		this.eKit = eKit;
		this.isTarget = isTarget;
		// ensure my disposal
		eKit.put(UUID.randomUUID(), this);
	}
	
	public void statusChanged(DifferenceEvent event) {
		switch (event) {

		case SELECTION_CHANGED:
			OWLSelectionModel selectionModel =  eKit.getOWLWorkspace().getOWLSelectionModel();
			EntityBasedDiff diff = differenceManager.getSelection();
			if (diff == null) {
				return;
			}
			OWLEntity targetEntity = isTarget ? diff.getTargetEntity() : diff.getSourceEntity();
			if (targetEntity != null) {
				selectionModel.setSelectedEntity(targetEntity);
			}
			break;
		case DIFF_RESET:
			differenceManager.removeDifferenceListener(this);
		}
	}
	
	public void dispose() throws Exception {
		differenceManager.removeDifferenceListener(this);  // twice is ok. minor memory leak.
	}
}
