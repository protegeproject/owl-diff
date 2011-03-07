package org.protege.editor.owl.diff.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class ConfigureDifferenceRun extends JDialog {
	private static final long serialVersionUID = -2882654202196117453L;
	private OWLEditorKit eKit;
	private File baseline;
	private JCheckBox openBaselineInSeparateWindow;
	private JCheckBox useRdfIdFragments;
	

	public ConfigureDifferenceRun(OWLEditorKit eKit) {
		this.eKit = eKit;
		createGui();
	}
	
	public File getBaseline() {
		return baseline;
	}

	public boolean getOpenBaselineInSeparateWindow() {
		return openBaselineInSeparateWindow.isSelected();
	}
	
	public boolean getUseRdfIdFragments() {
		return useRdfIdFragments.isSelected();
	}

	private void createGui() {
		setLayout(new BorderLayout());
	
	}
	
	private void addCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.add(createFilePanel());
		addBooleanOptions(centerPanel);
	}
	
	private JPanel createFilePanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("Original Version of the file: "));
		final JTextField baselineTextField = new JTextField();
		Dimension preferredTextFieldDimension = new JTextField("Thesaurus-101129-10.11e.owl").getPreferredSize();
		baselineTextField.setPreferredSize(preferredTextFieldDimension);
		baselineTextField.setEditable(false);
		panel.add(baselineTextField);
		JButton browseForBaseline = new JButton("Browse");
		panel.add(browseForBaseline);
		browseForBaseline.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				UIHelper utility = new UIHelper(eKit);
				File f = utility.chooseOWLFile("Choose the baseline ontology");
				if (f != null) {
					baseline = f;
					baselineTextField.setText(f.getName());
				}
			}
		});
		return panel;
	}
	
	private void addBooleanOptions(JPanel centerPanel) {
		centerPanel.add(openBaselineInSeparateWindow = new JCheckBox("Open original ontology in separate workspace"));
		centerPanel.add(useRdfIdFragments = new JCheckBox("Use rdf:id fragments to align ontologies"));
	}

	
	private Set<OWLAnnotationProperty> getAnnotationProperties() {
		Set<OWLAnnotationProperty> annotationProperties = new HashSet<OWLAnnotationProperty>();
		Set<OWLOntology> ontologies = eKit.getOWLModelManager().getActiveOntologies();
		for (OWLOntology ontology : ontologies) {
			annotationProperties.addAll(ontology.getAnnotationPropertiesInSignature());
		}
		return annotationProperties;
	}
}
