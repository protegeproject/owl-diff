package org.protege.editor.owl.diff;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
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
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;

public class DifferenceList extends JPanel implements Disposable {
	private static final long serialVersionUID = -3297368551819068585L;
	
	private DifferenceConfiguration diffs;
	private DifferenceTableModel diffModel;
	
	private JList entityBasedDiffList;
	
	private DifferenceListener diffListener = new DifferenceListener() {
		public void statusChanged(DifferenceEvent event) {
			if (event == DifferenceEvent.DIFF_COMPLETED) {
				fillEntityBasedDiffList();
			}
			else if (event == DifferenceEvent.SELECTION_CHANGED) {
				EntityBasedDiff diff = diffs.getSelection();
				if (diff != null) {
					diffModel.setMatches(diff.getAxiomMatches());
				}
			}
		}
	};

	public DifferenceList(OWLModelManager manager) {
		setLayout(new BorderLayout());
		this.diffs = DifferenceConfiguration.get(manager);
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
		model.clear();
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			model.addElement(diff);
		}
		entityBasedDiffList.repaint();
	}
	
	private JComponent createDifferenceListComponent() {
		entityBasedDiffList = new JList();
		entityBasedDiffList.setModel(new DefaultListModel());
		entityBasedDiffList.setSelectionModel(new DefaultListSelectionModel());
		entityBasedDiffList.setCellRenderer(new EntityBasedDiffRenderer());
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
		JTable table = new JTable();
		diffModel = new DifferenceTableModel(diffs.getManager());
		table.setModel(diffModel);
		return new JScrollPane(table);
	}
	
	private static class EntityBasedDiffRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -2257588249282053158L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
													  int index, boolean isSelected, boolean cellHasFocus) {
			JLabel text = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof EntityBasedDiff) {
				text.setText(((EntityBasedDiff) value).getShortDescription());
			}
			return this;
		}
		
	}
	
	public void dispose() throws Exception {
		diffs.removeDifferenceListener(diffListener);
	}

}
