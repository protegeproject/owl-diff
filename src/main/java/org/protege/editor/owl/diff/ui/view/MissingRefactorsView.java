package org.protege.editor.owl.diff.ui.view;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.editor.owl.diff.ui.boot.StartDiff;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.service.RenderingService;

public class MissingRefactorsView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = 184136904457609043L;
	
	private DifferenceManager differenceManager;
	
	private JList deletedList;
	private JList createdList;
	private RenderingService renderer;
	
	private DifferenceListener listener = new DifferenceListener() {
		public void statusChanged(DifferenceEvent event) {
			switch (event) {
			case DIFF_RESET:
				deletedList.removeAll();
				createdList.removeAll();
				break;
			case DIFF_COMPLETED:
				refill();
				break;
			}
		}
	};
	
	@Override
	protected void initialiseOWLView() throws Exception {
		differenceManager = DifferenceManager.get(getOWLModelManager());
		differenceManager.addDifferenceListener(listener);
		createUI();
	}
	
	private void createUI() {
		setLayout(new GridLayout(0, 2));
		deletedList = createEntityBasedDiffList();
		deletedList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				deletedSelectionChanged();
			}
		});
		add(new JScrollPane(deletedList));
		
		createdList = createEntityBasedDiffList();
		createdList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				createdSelectionChanged();
			}
		});
		add(new JScrollPane(createdList));
		refill();
	}
	
	private JList createEntityBasedDiffList() {
		JList l = new JList();
		l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		l.setModel(new DefaultListModel());
		l.setSelectionModel(new DefaultListSelectionModel());
		// l.setPreferredSize(new Dimension(Double.MAX_VALUE, Double.MAX_VALUE));
		return l;
	}
	
	private void createdSelectionChanged() {
		Object o = createdList.getSelectedValue();
		if (o instanceof EntityBasedDiff && ((EntityBasedDiff) o).getSourceEntity() == null) {
			getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(((EntityBasedDiff) o).getTargetEntity());
		}
	}
	
	private void deletedSelectionChanged() {
		Object o = deletedList.getSelectedValue();
		OWLEditorKit altOWLEditorKit = StartDiff.getAltEditorKit(differenceManager.getEngine());
		if (altOWLEditorKit != null && o instanceof EntityBasedDiff && ((EntityBasedDiff) o).getTargetEntity() == null) {
			altOWLEditorKit.getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(((EntityBasedDiff) o).getSourceEntity());
		}
	}
	
	private void refill() {
		if (differenceManager.isReady()) {
			renderer = StartDiff.getRenderingService(getOWLModelManager());
			createdList.removeAll();
			createdList.setCellRenderer(new EntityBasedDiffRenderer(renderer));
			deletedList.removeAll();
			deletedList.setCellRenderer(new EntityBasedDiffRenderer(renderer));
			Changes changes = differenceManager.getEngine().getChanges();
			List<EntityBasedDiff> listOfDiffs = new ArrayList<EntityBasedDiff>(changes.getEntityBasedDiffs());
			Collections.sort(listOfDiffs, new Comparator<EntityBasedDiff>() {
				public int compare(EntityBasedDiff diff1, EntityBasedDiff diff2) {
					return renderer.renderDiff(diff1).compareTo(renderer.renderDiff(diff2));
				}
			});
			for (EntityBasedDiff diff : listOfDiffs) {
				if (diff.getTargetEntity() == null) {
					((DefaultListModel) deletedList.getModel()).addElement(diff);
				}
				else if (diff.getSourceEntity() == null) {
					((DefaultListModel) createdList.getModel()).addElement(diff);
				}
			}
		}
	}

	@Override
	protected void disposeOWLView() {
		differenceManager.removeDifferenceListener(listener);
	}

}
