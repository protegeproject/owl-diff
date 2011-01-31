package org.protege.editor.owl.diff;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;

public class DifferenceList extends JPanel {
	private static final long serialVersionUID = -3297368551819068585L;
	
	private DifferenceConfiguration diffs;
	
	private JList entityBasedDiffList;

	public DifferenceList(OWLModelManager manager) {
		setLayout(new BorderLayout());
		this.diffs = DifferenceConfiguration.get(manager);
		add(createDifferenceListComponent(), BorderLayout.WEST);
		add(createDifferenceTable(), BorderLayout.CENTER);
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
		entityBasedDiffList = new JList();
		entityBasedDiffList.setModel(new DefaultListModel());
		entityBasedDiffList.setSelectionModel(new DefaultListSelectionModel());
		entityBasedDiffList.setCellRenderer(new EntityBasedDiffRenderer());
		entityBasedDiffList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityBasedDiffList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Object o = entityBasedDiffList.getSelectedValue();
				if (o instanceof EntityBasedDiff) {
					diffs.setSelection((EntityBasedDiff) o);
				}
			}
		});
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
