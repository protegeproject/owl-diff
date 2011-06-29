package org.protege.editor.owl.diff.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLEntity;

public class DifferenceList extends JPanel implements Disposable {
	private static final long serialVersionUID = -3297368551819068585L;
	
	private DifferenceManager diffs;
	private DifferenceTableModel diffModel;
	private RenderingService renderer;
	
	private JList entityBasedDiffList;
	private JLabel explanationLabel;
	
	private DifferenceListener diffListener = new DifferenceListener() {
		public void statusChanged(DifferenceEvent event) {
			if (event == DifferenceEvent.DIFF_COMPLETED) {
				renderer = RenderingService.get(diffs.getEngine());
				fillEntityBasedDiffList();
			}
			else if (event == DifferenceEvent.DIFF_RESET) {
				entityBasedDiffList.removeAll();
				diffModel.clear();
			}
			else if (event == DifferenceEvent.SELECTION_CHANGED) {
				selectionChanged();
			}
		}
		
		private void selectionChanged() {
			explanationLabel.setText("");
			EntityBasedDiff diff = diffs.getSelection();
			if (diff != null) {
				entityBasedDiffList.setSelectedValue(diff, true);
				diffModel.setMatches(diff.getAxiomMatches());
				OWLEntity sourceEntity = diff.getSourceEntity();
				if (sourceEntity != null) {
					OwlDiffMap diffMap = diffs.getEngine().getOwlDiffMap();
					String explanation = diffMap.getExplanation(sourceEntity);
					if (explanation != null) {
						explanationLabel.setText(explanation);
					}
				}
			}
		}
	};

	public DifferenceList(OWLEditorKit editorKit) {
		setLayout(new BorderLayout());
		this.diffs = DifferenceManager.get(editorKit.getModelManager());
		if (diffs.getEngine() != null) {
			renderer = RenderingService.get(diffs.getEngine());
		}
		add(createDifferenceListComponent(), BorderLayout.WEST);
		add(createDifferenceTable(), BorderLayout.CENTER);
		if (diffs.isReady()) {
			fillEntityBasedDiffList();
		}
		diffs.addDifferenceListener(diffListener);
	}
	
	private void fillEntityBasedDiffList() {
		Changes changes = diffs.getEngine().getChanges();
		DefaultListModel model = (DefaultListModel) entityBasedDiffList.getModel();
		List<EntityBasedDiff> listOfDiffs = new ArrayList<EntityBasedDiff>(changes.getEntityBasedDiffs());
		Collections.sort(listOfDiffs, new Comparator<EntityBasedDiff>() {
			public int compare(EntityBasedDiff diff1, EntityBasedDiff diff2) {
				return renderer.renderDiff(diff1).compareTo(renderer.renderDiff(diff2));
			}
		});
		model.clear();
		for (EntityBasedDiff diff : listOfDiffs) {
			model.addElement(diff);
		}
		entityBasedDiffList.repaint();
	}
	
	private JComponent createDifferenceListComponent() {
		entityBasedDiffList = new JList();
		entityBasedDiffList.setModel(new DefaultListModel());
		entityBasedDiffList.setSelectionModel(new DefaultListSelectionModel());
		entityBasedDiffList.setCellRenderer(new EntityBasedDiffRenderer(renderer));
		entityBasedDiffList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityBasedDiffList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				Object o = entityBasedDiffList.getSelectedValue();
				if (o instanceof EntityBasedDiff) {
					diffs.setSelection((EntityBasedDiff) o);
				}
			}
		});
		
		Dimension textSize = new JLabel("Modified CheeseyPizza -> CheeseyPizza").getPreferredSize();	
		JScrollPane pane = new JScrollPane(entityBasedDiffList);
		pane.setPreferredSize(new Dimension((int) textSize.getWidth(), (int) (100 *textSize.getHeight())));	
		return pane;
	}
	
	private JComponent createDifferenceTable() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		explanationLabel = new JLabel();
		panel.add(explanationLabel, BorderLayout.NORTH);
		
		JTable table = new JTable();
		diffModel = new DifferenceTableModel(renderer);
		table.setModel(diffModel);
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
		table.setRowHeight(60);
		
		// table.setDefaultRenderer(OWLAxiom.class, new OWLCellRenderer(editorKit, false, false));
		
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}
	
	public void dispose() {
		diffs.removeDifferenceListener(diffListener);
	}

}
