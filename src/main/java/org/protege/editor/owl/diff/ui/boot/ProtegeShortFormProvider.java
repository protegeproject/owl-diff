package org.protege.editor.owl.diff.ui.boot;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class ProtegeShortFormProvider implements ShortFormProvider {
	private OWLModelManager p4Manager;
	
	public ProtegeShortFormProvider(OWLModelManager p4Manager) {
		this.p4Manager = p4Manager;
	}

	public String getShortForm(OWLEntity entity) {
		return p4Manager.getRendering(entity);
	}

	public void dispose() {
	}

}
