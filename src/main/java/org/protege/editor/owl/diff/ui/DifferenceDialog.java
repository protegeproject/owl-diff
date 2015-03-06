package org.protege.editor.owl.diff.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.diff.model.DifferenceEvent;
import org.protege.editor.owl.diff.model.DifferenceListener;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.owl.diff.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DifferenceDialog extends JDialog {
	private static final long serialVersionUID = -7057739761303778176L;
	private DifferenceManager differenceManager;
	private DifferencesByEntity differenceList;
	private JLabel summary;
	private DifferenceListener listener = new DifferenceListener() {
		
		public void statusChanged(DifferenceEvent event) {
			switch (event) {
			case DIFF_RESET:
				summary.setText("");
				break;
			case DIFF_COMPLETED:
				update();
				break;
			}
		}
	};

	public DifferenceDialog(OWLEditorKit eKit) {
		setLayout(new BorderLayout());
		setTitle("Ontology Differences");
		differenceManager = DifferenceManager.get(eKit.getModelManager());
		
		add(createNorth(), BorderLayout.NORTH);
		add(createCenter(eKit), BorderLayout.CENTER);
        add(createSouth(), BorderLayout.SOUTH);
        
        differenceManager.addDifferenceListener(listener);
        if (differenceManager.isReady()) {
        	update();
        }
	}
	
	public JPanel createNorth() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(new Finder(differenceManager));
		return panel;
	}
	
	
	public JPanel createCenter(OWLEditorKit eKit) {
		differenceList = new DifferencesByEntity(eKit);
		differenceList.setSynchronizing(false);
		return differenceList;
	}
	
	public JPanel createSouth() {
        JPanel panel = new JPanel(new BorderLayout());

        summary = new JLabel();
        summary.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(summary, BorderLayout.WEST);

        final JCheckBox cb = new JCheckBox();
        cb.setAlignmentX(RIGHT_ALIGNMENT);
        panel.add(cb, BorderLayout.EAST);
        cb.setAction(new AbstractAction("Synchronising") {

            /**
             * 
             */
            private static final long serialVersionUID = -4131922452059512538L;

            public void actionPerformed(ActionEvent e) {
                differenceList.setSynchronizing(cb.isSelected());
            }
        });
        return panel;
	}
	
	private void update() {
		summary.setText(Util.getStats(differenceManager.getEngine()));
	}
	
	public void dispose() {
		differenceManager.removeDifferenceListener(listener);
		differenceManager = null;
		differenceList.dispose();
		differenceList = null;
	}
}
