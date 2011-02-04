package org.protege.editor.owl.diff.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.diff.model.DifferenceConfiguration;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DiffView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -953820310817783007L;
	public static Logger LOGGER = Logger.getLogger(DiffView.class);
	
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
		DifferenceConfiguration diffs = DifferenceConfiguration.get(getOWLModelManager());
		diffs.addDifferenceListener(listener);
		updateStatus();
	}
	
	
	
	protected void disposeOWLView() {
		DifferenceConfiguration diffs = DifferenceConfiguration.get(getOWLModelManager());
		diffs.removeDifferenceListener(listener);
		try {
			view.dispose();
		}
		catch (Exception e) {
			ProtegeApplication.getErrorLog().logError(e);
		}
	}

	private void updateStatus() {
		DifferenceConfiguration diffs = DifferenceConfiguration.get(getOWLModelManager());
		if (diffs.isReady()) {
			status.setText("Displaying differences");
		}
		else {
			status.setText("Differences not ready");
		}
	}

}
