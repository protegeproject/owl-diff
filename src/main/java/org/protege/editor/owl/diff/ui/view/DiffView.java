package org.protege.editor.owl.diff.ui.view;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.diff.ui.DifferencesByEntity;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.owl.diff.util.Util;

public class DiffView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -953820310817783007L;
	public static final Logger LOGGER = Logger.getLogger(DiffView.class);
	
	private DifferencesByEntity view;
	private JLabel status;
	
	private DifferenceListener listener = new DifferenceListener() {

		public void statusChanged(DifferenceEvent event) {
			updateStatus();
		}
		
	};
	
	protected void initialiseOWLView() {
		setLayout(new BorderLayout());

		view = new DifferencesByEntity(getOWLEditorKit()) {
			private static final long serialVersionUID = -88036681191093269L;

			public boolean isSynchronizing() {
				return DiffView.this.isSynchronizing();
			}
			
			public void setSynchronizing(boolean isSynchronized) {
				throw new UnsupportedOperationException();
			}
		};
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
			if (view != null) {
				view.dispose();
			}
		}
		catch (Exception e) {
			ProtegeApplication.getErrorLog().logError(e);
		}
	}

	private void updateStatus() {
		DifferenceManager diffs = DifferenceManager.get(getOWLModelManager());
		if (diffs.isReady()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Displaying differences.  ");
			sb.append(Util.getStats(diffs.getEngine()));
			status.setText(sb.toString());
		}
		else {
			status.setText("Differences not ready");
		}
	}

}
