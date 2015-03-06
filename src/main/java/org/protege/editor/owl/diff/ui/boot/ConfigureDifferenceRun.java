package org.protege.editor.owl.diff.ui.boot;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.DifferenceActivator;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.algorithms.DeferDeprecationAlgorithm;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ConfigureDifferenceRun extends JDialog {
	private static final long serialVersionUID = -2882654202196117453L;
	private OWLEditorKit eKit;
	private JTextField baselineTextField;
	private JCheckBox openBaselineInSeparateWindow;
	private JCheckBox doDeprecationAndReplace;
	private JComboBox aggressiveness;
	private boolean ok = false;

	public ConfigureDifferenceRun(OWLEditorKit eKit) {
		super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, eKit.getOWLWorkspace()), true);
		this.eKit = eKit;
		createGui();
		pack();
	}
	
	public IRI getBaseline() {
		String baseline = baselineTextField.getText();
		File baselineFile = new File(baseline);
		if (baselineFile.exists()) {
			return IRI.create(baselineFile);
		}
		return IRI.create(baseline);
	}

	public boolean getOpenBaselineInSeparateWindow() {
		return openBaselineInSeparateWindow.isSelected();
	}
	
	public boolean isCommit() {
		return ok;
	}

	public Configuration getConfiguration() {
		Configuration config = new Configuration();

		AlignmentAggressiveness requestedEffort = (AlignmentAggressiveness) aggressiveness.getSelectedItem();
		for (Class<? extends AlignmentAlgorithm> algorithmClass : DifferenceActivator.createAlignmentAlgorithms()) {
			try {
				AlignmentAlgorithm algorithm = algorithmClass.newInstance();
				if (!algorithm.isCustom() && algorithm.getAggressiveness().compareTo(requestedEffort) <= 0) {
					config.addAlignmentAlgorithm(algorithmClass);
				}
			}
			catch (Exception e) {
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
		if (doDeprecationAndReplace.isSelected()) {
			config.addAlignmentAlgorithm(DeferDeprecationAlgorithm.class);
		}
		for (Class<? extends PresentationAlgorithm> alg : DifferenceActivator.createPresentationAlgorithms()) {
			config.addPresentationAlgorithm(alg);
		}
		
		return config;
	}
	
	private void createGui() {
		setTitle("Configure ontology comparison");
		setLayout(new BorderLayout());
		
		addCenterPanel();
		addButtons();
		
		updateDeprecateAndReplaceStatus();
	}
	
	/*
	 * TODO - gui needs work...
	 * I got it centered by making lots of sub-panels and marking everything as being left aligned...
	 */
	private void addCenterPanel() {
		JPanel centerPanel = new JPanel();
		LayoutManager layout = new GridLayout(0,1);
		centerPanel.setLayout(layout);
		centerPanel.setAlignmentY(LEFT_ALIGNMENT);
		centerPanel.add(createFilePanel());
		centerPanel.add(createOpenInSeparateWorkspace());
		centerPanel.add(createDeprecateAndReplace());
		centerPanel.add(chooseAggressivenessDropdown());
		add(centerPanel, BorderLayout.CENTER);
	}

	private JPanel createFilePanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setAlignmentY(LEFT_ALIGNMENT);
		JLabel label = new JLabel("Original Version: ");
		label.setAlignmentY(LEFT_ALIGNMENT);
		panel.add(label);
		baselineTextField = new JTextField();
		baselineTextField.setEditable(true);
		Dimension preferredTextFieldDimension = new JTextField("Thesaurus-101129-10.11e.owl").getPreferredSize();
		baselineTextField.setPreferredSize(preferredTextFieldDimension);
		baselineTextField.setEditable(true);
		panel.add(baselineTextField);
		JButton browseForBaseline = new JButton("Browse");
		panel.add(browseForBaseline);
		browseForBaseline.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				UIHelper utility = new UIHelper(eKit);
				File f = utility.chooseOWLFile("Choose the baseline ontology");
				if (f != null) {
					baselineTextField.setText(f.getAbsolutePath());
				}
			}
		});
		panel.setAlignmentY(LEFT_ALIGNMENT);
		return panel;
	}
	
	private JComponent createDeprecateAndReplace() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setAlignmentY(LEFT_ALIGNMENT);
		doDeprecationAndReplace = new JCheckBox("Search for the deprecate and replace pattern");
		doDeprecationAndReplace.setAlignmentY(LEFT_ALIGNMENT);
		panel.add(doDeprecationAndReplace);
		return panel;
	}
	
	private JComponent createOpenInSeparateWorkspace() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setAlignmentY(LEFT_ALIGNMENT);
		openBaselineInSeparateWindow = new JCheckBox("Open original ontology in separate workspace");
		openBaselineInSeparateWindow.setAlignmentY(LEFT_ALIGNMENT);
		panel.add(openBaselineInSeparateWindow);
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
	
	private JComponent chooseAggressivenessDropdown() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.setAlignmentY(LEFT_ALIGNMENT);
		aggressiveness = new JComboBox();
		aggressiveness.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 6340827743615126160L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel rendering = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				rendering.setText(((AlignmentAggressiveness) value).getDescription());
				return rendering;
			}
		});
		for (AlignmentAggressiveness effort : AlignmentAggressiveness.values()) {
			aggressiveness.addItem(effort);
		}
		aggressiveness.setSelectedItem(AlignmentAggressiveness.IGNORE_REFACTOR);
		aggressiveness.setAlignmentY(LEFT_ALIGNMENT);
		panel.add(aggressiveness);
		aggressiveness.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				updateDeprecateAndReplaceStatus();
			}
		});
		return panel;
	}
	
	private void updateDeprecateAndReplaceStatus() {
		doDeprecationAndReplace.setEnabled(aggressiveness.getSelectedItem() != AlignmentAggressiveness.IGNORE_REFACTOR);
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
