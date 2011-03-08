package org.protege.editor.owl.diff.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.algorithms.MatchByIdFragment;
import org.protege.owl.diff.align.algorithms.MatchLoneSiblings;
import org.protege.owl.diff.align.algorithms.MatchSiblingsWithSimilarIds;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.align.algorithms.SuperSubClassPinch;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.present.algorithms.IdentifyAxiomAnnotationChanged;
import org.protege.owl.diff.present.algorithms.IdentifyChangedAnnotation;
import org.protege.owl.diff.present.algorithms.IdentifyChangedDefinition;
import org.protege.owl.diff.present.algorithms.IdentifyChangedSuperclass;
import org.protege.owl.diff.present.algorithms.IdentifyRenameOperation;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class ConfigureDifferenceRun extends JDialog {
	private static final long serialVersionUID = -2882654202196117453L;
	private OWLEditorKit eKit;
	private File baseline;
	private JCheckBox openBaselineInSeparateWindow;
	private JCheckBox useRdfIdFragments;
	private JCheckBox ignoreRefactors;
	private JCheckBox useLabel;
	private JComboBox labelBox;
	private boolean ok = false;

	public ConfigureDifferenceRun(OWLEditorKit eKit) {
		super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, eKit.getOWLWorkspace()), true);
		this.eKit = eKit;
		createGui();
		pack();
	}
	
	public File getBaseline() {
		return baseline;
	}

	public boolean getOpenBaselineInSeparateWindow() {
		return openBaselineInSeparateWindow.isSelected();
	}
	
	public boolean isCommit() {
		return ok;
	}

	public Configuration getConfiguration() {
		Configuration config = new Configuration();
		if (useLabel.isSelected()) {
			config.addAlignmentAlgorithm(MatchByCode.class);
			config.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, ((OWLAnnotationProperty) labelBox.getSelectedItem()).toString());
		}
		config.addAlignmentAlgorithm(MatchById.class);
		config.addAlignmentAlgorithm(MatchStandardVocabulary.class);
		if (useRdfIdFragments.isSelected()) {
			config.addAlignmentAlgorithm(MatchByIdFragment.class);
		}
		if (!ignoreRefactors.isSelected()) {
			config.addAlignmentAlgorithm(SuperSubClassPinch.class);
			config.addAlignmentAlgorithm(MatchLoneSiblings.class);
			config.addAlignmentAlgorithm(MatchSiblingsWithSimilarIds.class);
		}
		
		config.addPresentationAlgorithm(IdentifyChangedAnnotation.class);
		config.addPresentationAlgorithm(IdentifyChangedDefinition.class);
		config.addPresentationAlgorithm(IdentifyChangedSuperclass.class);
		config.addPresentationAlgorithm(IdentifyRenameOperation.class);
		config.addPresentationAlgorithm(IdentifyAxiomAnnotationChanged.class);
		
		return config;
	}
	
	
	private void createGui() {
		setLayout(new BorderLayout());
		addCenterPanel();
		addButtons();
	}
	
	private void addCenterPanel() {
		JPanel centerPanel = new JPanel();
		BoxLayout layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
		centerPanel.setLayout(layout);
		centerPanel.add(createFilePanel());
		addBooleanOptions(centerPanel);
		centerPanel.add(createAlignByLabelComponent());
		add(centerPanel, BorderLayout.CENTER);
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
		panel.setAlignmentX(LEFT_ALIGNMENT);
		return panel;
	}
	
	private void addBooleanOptions(JPanel centerPanel) {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		
		openBaselineInSeparateWindow = new JCheckBox("Open original ontology in separate workspace");
		openBaselineInSeparateWindow.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(openBaselineInSeparateWindow);
		
		ignoreRefactors = new JCheckBox("Don't look for refactor operations");
		ignoreRefactors.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(ignoreRefactors);

		useRdfIdFragments = new JCheckBox("Use rdf:id fragments to align ontologies");
		useRdfIdFragments.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(useRdfIdFragments);
		
		panel.setAlignmentX(0.1f);
		centerPanel.add(panel);
	}
	
	private JComponent createAlignByLabelComponent() {
		JPanel panel = new JPanel(new FlowLayout());
		
		useLabel = new JCheckBox("Align entities using an annotation property");
		useLabel.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(useLabel);
		
		labelBox = new JComboBox(getAnnotationProperties().toArray());
		final OWLModelManager p4Manager = eKit.getOWLModelManager();
		labelBox.setRenderer(new BasicComboBoxRenderer() {
			private static final long serialVersionUID = 6962612886718094978L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel rendering = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				rendering.setText(p4Manager.getRendering((OWLAnnotationProperty) value));
				return rendering;
			}
		});
		labelBox.setSelectedItem(p4Manager.getOWLDataFactory().getRDFSLabel());
		panel.add(labelBox);
		panel.setAlignmentX(LEFT_ALIGNMENT);
		
		return panel;
	}

	private Set<OWLAnnotationProperty> getAnnotationProperties() {
		Set<OWLAnnotationProperty> annotationProperties = new HashSet<OWLAnnotationProperty>();
		Set<OWLOntology> ontologies = eKit.getOWLModelManager().getActiveOntologies();
		for (OWLOntology ontology : ontologies) {
			annotationProperties.addAll(ontology.getAnnotationPropertiesInSignature());
		}
		return annotationProperties;
	}
	
	
	private void addButtons() {
		JPanel panel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		panel.add(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});
		panel.add(cancelButton);
		add(panel, BorderLayout.SOUTH);
		
	}
	
}
