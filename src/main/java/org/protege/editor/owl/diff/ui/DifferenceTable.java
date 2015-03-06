package org.protege.editor.owl.diff.ui;

import org.protege.editor.owl.diff.ui.render.MultiLineCellRenderer;

import javax.swing.*;


public class DifferenceTable extends JTable {
	private static final long serialVersionUID = 8289059052215956578L;

	public DifferenceTable(DifferenceTableModel model) {
		setModel(model);
		setDefaultRenderer(String.class, new MultiLineCellRenderer());
		setRowHeight(60);
	}
}
