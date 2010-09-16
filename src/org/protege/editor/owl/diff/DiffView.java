package org.protege.editor.owl.diff;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DiffView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -953820310817783007L;
	private JTextField ontologyLocationField;

	protected void initialiseOWLView() throws Exception {
		setLayout(new FlowLayout());
		
		ontologyLocationField = new JTextField();
		ontologyLocationField.setPreferredSize(new JTextField("/home/tredmond/Shared/ontologies/simple/pizza-baseline.owl").getPreferredSize());
		add(ontologyLocationField);
		
		JButton chooseOntology = new JButton("Browse");
		chooseOntology.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				File ont = new UIHelper(getOWLEditorKit()).chooseOWLFile("Choose an ontology");
				try {
					ontologyLocationField.setText(ont.getCanonicalPath());
				}
				catch (IOException ioe) {
					ProtegeApplication.getErrorLog().logError(ioe);
				}
			}
		});
		add(chooseOntology);
		
		final JButton run = new JButton("Calculate Differences");
		run.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				DifferenceConfiguration dc = DifferenceConfiguration.getInstance(getOWLModelManager());
				try {
					dc.run(IRI.create(getOntologyFile()));
				}
				catch (OWLOntologyCreationException ooce) {
					ProtegeApplication.getErrorLog().logError(ooce);
				}
			}
		});
		ontologyLocationField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run.setEnabled(getOntologyFile() != null);
			}
		});
		add(run);
	}
	
	private File getOntologyFile() {
		String fileString = ontologyLocationField.getText();
		File ret = new File(fileString);
		if (!ret.exists() || !ret.isFile())  {
			ret = null;
		}
		return ret;
	}


	protected void disposeOWLView() {
		
	}

}
