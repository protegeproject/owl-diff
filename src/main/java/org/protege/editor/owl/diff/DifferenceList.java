package org.protege.editor.owl.diff;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;

public class DifferenceList extends JPanel {
	private static final long serialVersionUID = -3297368551819068585L;
	
	private DifferenceConfiguration diffs;
	
	private JList entityBasedDiffList;

	public DifferenceList(OWLModelManager manager) {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		this.diffs = DifferenceConfiguration.get(manager);
		add(createDifferenceListComponent());
		add(createDifferenceTable());
		if (diffs.isReady()) {
			fillEntityBasedDiffList();
		}
		diffs.addDifferenceListener(new DifferenceListener() {
			public void statusChanged(DifferenceEvent event) {
				if (event == DifferenceEvent.DIFF_COMPLETED) {
					fillEntityBasedDiffList();
				}
			}
		});
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
		entityBasedDiffList = new JList(new DefaultListModel());
		entityBasedDiffList.setCellRenderer(new EntityBasedDiffRenderer());
		entityBasedDiffList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane pane = new JScrollPane(entityBasedDiffList);
		return pane;
	}
	
	private JComponent createDifferenceTable() {
		JTable table = new JTable();
		return new JScrollPane(table);
	}
	
	private static class EntityBasedDiffRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -2257588249282053158L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
													  int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof EntityBasedDiff) {
				setText(((EntityBasedDiff) value).getShortDescription());
			}
			return this;
		}
		
	}

}
