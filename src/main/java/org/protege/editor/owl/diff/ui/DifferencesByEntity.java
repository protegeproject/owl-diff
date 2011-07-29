package org.protege.editor.owl.diff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.diff.model.EntityBasedDiffComparator;
import org.protege.editor.owl.diff.ui.boot.StartDiff;
import org.protege.editor.owl.diff.ui.render.EntityBasedDiffRenderer;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.algorithms.IdentifyDeprecatedAndReplaced;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLEntity;

public class DifferencesByEntity extends JPanel implements Disposable {
	private static final long serialVersionUID = -3297368551819068585L;
	
	private OWLEditorKit editorKit;
	
	private DifferenceManager diffs;
	private DifferenceTableModel diffModel;
	
	private JList  entityBasedDiffList;
	private JPanel differenceTablePanel;
	private JPanel explanationPanel;
	
	private boolean synchronizing = true;
	
	private DifferenceListener diffListener = new DifferenceListener() {
		public void statusChanged(DifferenceEvent event) {
			if (event == DifferenceEvent.DIFF_COMPLETED) {
				fillEntityBasedDiffList();
			}
			else if (event == DifferenceEvent.DIFF_RESET) {
				entityBasedDiffList.removeAll();
				diffModel.clear();
			}
			else if (event == DifferenceEvent.SELECTION_CHANGED && isSynchronizing()) {
				globalDiffSelectionChanged();
			}
		}
		
		private void globalDiffSelectionChanged() {
			final EntityBasedDiff diff = diffs.getSelection();
			if (diff != null) {
				entityBasedDiffList.setSelectedValue(diff, true);
				setDiffModelMatches(diff);
			}
		}
	};
	
	public DifferencesByEntity(OWLEditorKit editorKit) {
		this.editorKit = editorKit;
		setLayout(new BorderLayout());
		this.diffs = DifferenceManager.get(editorKit.getModelManager());
		add(createDifferenceListComponent(), BorderLayout.WEST);
		add(createDifferenceTable(), BorderLayout.CENTER);
		if (diffs.isReady()) {
			fillEntityBasedDiffList();
		}
		diffs.addDifferenceListener(diffListener);
	}
	
	public boolean isSynchronizing() {
		return synchronizing;
	}
	
	public void setSynchronizing(boolean synchronizing) {
		this.synchronizing = synchronizing;
		if (synchronizing) {
			Object o = entityBasedDiffList.getSelectedValue();
			if (o instanceof EntityBasedDiff) {
				EntityBasedDiff diff = (EntityBasedDiff) o;
				diffs.setSelection(diff);
			}
		}
	}
	
	private void fillEntityBasedDiffList() {
		final RenderingService renderer = StartDiff.getRenderingService(editorKit.getModelManager());
		Changes changes = diffs.getEngine().getChanges();
		final DefaultListModel model = (DefaultListModel) entityBasedDiffList.getModel();
		final List<EntityBasedDiff> listOfDiffs = new ArrayList<EntityBasedDiff>(changes.getEntityBasedDiffs());
		Collections.sort(listOfDiffs, new EntityBasedDiffComparator(renderer));
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.clear();
					for (EntityBasedDiff diff : listOfDiffs) {
						model.addElement(diff);
					}
					entityBasedDiffList.repaint();
				}
			});
		}
		catch (Exception e) {
			ProtegeApplication.getErrorLog().logError(e);
		}

	}
	
	private JComponent createDifferenceListComponent() {
		entityBasedDiffList = new JList();
		entityBasedDiffList.setModel(new DefaultListModel());
		entityBasedDiffList.setSelectionModel(new DefaultListSelectionModel());
		entityBasedDiffList.setCellRenderer(new EntityBasedDiffRenderer(diffs));
		entityBasedDiffList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityBasedDiffList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				Object o = entityBasedDiffList.getSelectedValue();
				if (o instanceof EntityBasedDiff) {
					EntityBasedDiff diff = (EntityBasedDiff) o;
					if (isSynchronizing()) {
						diffs.setSelection(diff);
					}
					else {
						setDiffModelMatches(diff);
					}
				}
			}
		});
		
		Dimension textSize = new JLabel("Modified CheeseyPizza -> CheeseyPizza").getPreferredSize();	
		JScrollPane pane = new JScrollPane(entityBasedDiffList);
		pane.setPreferredSize(new Dimension((int) textSize.getWidth(), (int) (100 *textSize.getHeight())));	
		return pane;
	}
	
	private JComponent createDifferenceTable() {
		differenceTablePanel = new JPanel();
		differenceTablePanel.setLayout(new BorderLayout());
		differenceTablePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		explanationPanel = new JPanel(new BorderLayout());
		explanationPanel.setAlignmentX(LEFT_ALIGNMENT);
		differenceTablePanel.add(explanationPanel, BorderLayout.NORTH);

		diffModel = new DifferenceTableModel(diffs);

		differenceTablePanel.add(new JScrollPane(new DifferenceTable(diffModel)), BorderLayout.CENTER);
		return differenceTablePanel;
	}
	
	private void setDiffModelMatches(final EntityBasedDiff diff) {
		explanationPanel.removeAll();
		diffModel.setMatches(diff.getAxiomMatches());
		OWLEntity sourceEntity = diff.getSourceEntity();
		if (sourceEntity != null) {
			OwlDiffMap diffMap = diffs.getEngine().getOwlDiffMap();
			addExplanation(diffMap, diff, sourceEntity);
		}
		differenceTablePanel.validate();
		differenceTablePanel.repaint();
	}
	
	private void addExplanation(OwlDiffMap diffMap, final EntityBasedDiff diff, final OWLEntity sourceEntity) {
		final AlignmentExplanation explanation = diffMap.getExplanation(sourceEntity);
		if (explanation != null) {
			JLabel label = new JLabel(explanation.getExplanation());
			explanationPanel.add(label, BorderLayout.WEST);
			if (explanation.hasDetailedExplanation(sourceEntity)) {
				JButton detailsButton = new JButton("Details...");
				detailsButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JDialog dialog = new JDialog();
						dialog.setTitle("Detailed explanation");
						JTextPane detailsPane = new JTextPane();
						detailsPane.setText(explanation.getDetailedExplanation(sourceEntity));
						detailsPane.setEditable(false);
						dialog.add(detailsPane);
						dialog.setLocation(new Point(40,40));
						dialog.pack();
						dialog.setVisible(true);
					}
				});
				explanationPanel.add(detailsButton, BorderLayout.EAST);
			}
		}
		// Can the following hack be removed?
		if (diff.getDiffTypeDescription().equals(IdentifyDeprecatedAndReplaced.DEPRECATED_AND_REPLACED_DIFF_TYPE)) {
			JButton explain = new JButton("Explain This!");
			explanationPanel.add(explain, BorderLayout.EAST);
			explain.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					explainDeprecateAndReplace(diff);
				}
			});
		}
	}

	private void explainDeprecateAndReplace(EntityBasedDiff diff) {
		RenderingService renderer = StartDiff.getRenderingService(editorKit.getModelManager());
		OWLEntity deprecatedEntity = diff.getTargetEntity();
		final EntityBasedDiff altDiff = diffs.getEngine().getChanges().getSourceDiffMap().get(deprecatedEntity);
		OWLEntity replacementEntity = altDiff.getTargetEntity();
		
		final JDialog dialog = new JDialog();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocation(getLocation());
        
		JPanel panel = new JPanel(new BorderLayout());
		
		JTextArea explanationText = new JTextArea();
		explanationText.setLineWrap(true);
		explanationText.setWrapStyleWord(true);
		explanationText.setText(generateDeprecateAndReplaceText(renderer, deprecatedEntity, replacementEntity));
		panel.add(explanationText, BorderLayout.NORTH);
		
		DifferenceTableModel myModel = new DifferenceTableModel(diffs);
		myModel.setMatches(altDiff.getAxiomMatches());
		panel.add(new JScrollPane(new DifferenceTable(myModel)), BorderLayout.CENTER);
		
		JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton jumpToDiff = new JButton("Jump There");
		jumpToDiff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				diffs.setSelection(altDiff);
				if (isSynchronizing()) {
					dialog.dispose();
				}
			}
		});
		bottomButtons.add(jumpToDiff);
		panel.add(bottomButtons, BorderLayout.SOUTH);
		
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public String generateDeprecateAndReplaceText(RenderingService renderer, OWLEntity deprecatedEntity, OWLEntity replacementEntity) {
		StringBuffer sb = new StringBuffer();
		sb.append("At some point the entity, ");
		sb.append(renderer.renderSourceObject(deprecatedEntity));
		sb.append(", was deprecated and replaced with a new entity, ");
		sb.append(renderer.renderTargetObject(replacementEntity));
		sb.append(".  The difference engine has detected this and responded by mapping the old entity, ");
		sb.append(renderer.renderSourceObject(deprecatedEntity));
		sb.append(", to its replacement, ");
		sb.append(renderer.renderTargetObject(replacementEntity));
		sb.append(".  This is slightly confusing because it means that in the second ontology the deprecated entity, ");
		sb.append(renderer.renderSourceObject(deprecatedEntity));
		sb.append(", and its axioms appear to be new.\n\nThe explanation provided for why ");
		sb.append(renderer.renderSourceObject(deprecatedEntity));
		sb.append(" maps to ");
		sb.append(renderer.renderTargetObject(replacementEntity));
		sb.append(" is \n\n");
		sb.append(diffs.getEngine().getOwlDiffMap().getExplanation(deprecatedEntity));
		return sb.toString();
	}
	
	public void dispose() {
		diffs.removeDifferenceListener(diffListener);
	}

}
