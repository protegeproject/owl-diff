package org.protege.editor.owl.diff.ui.view;

import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.diff.ui.UnattachedDifferencesPanel;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import java.awt.*;

public class UnattachedDifferencesView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = 4583995735944800739L;
	
	private UnattachedDifferencesPanel panel;


	@Override
	protected void initialiseOWLView() throws Exception {
		setLayout(new BorderLayout());
		DifferenceManager differenceManager = DifferenceManager.get(getOWLModelManager());
		panel = new UnattachedDifferencesPanel(differenceManager);
		add(panel, BorderLayout.CENTER);
	}

	@Override
	protected void disposeOWLView() {
		panel.dispose();
	}
}
