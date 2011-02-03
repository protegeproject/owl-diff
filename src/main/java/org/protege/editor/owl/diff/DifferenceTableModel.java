package org.protege.editor.owl.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.table.AbstractTableModel;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;

public class DifferenceTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1919687859946498484L;

	public enum Column {
		DESCRIPTION("Description"), 
		SOURCE_AXIOM("Baseline Axiom"), 
		TARGET_AXIOM("New Axiom");
		
		private String name;
		
		private Column(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private OWLModelManager protegeModelManager;
	private List<MatchedAxiom> matches = new ArrayList<MatchedAxiom>();
	
	public DifferenceTableModel(OWLModelManager protegeModelManager) {
		this.protegeModelManager = protegeModelManager;
	}
	
	public void setMatches(SortedSet<MatchedAxiom> matches) {
		this.matches = new ArrayList<MatchedAxiom>(matches);
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return Column.values().length;
	}

	public int getRowCount() {
		return matches == null ? 0 : matches.size();
	}
	
	public String getColumnName(int column) {
		return Column.values()[column].toString();
	}
	
	public Class<?> getColumnClass(int col) {
		switch (Column.values()[col]) {
		case DESCRIPTION:
			return String.class;
		case SOURCE_AXIOM:
		case TARGET_AXIOM:
			return OWLAxiom.class;
		default:
			throw new IllegalStateException("Programmer error");
		}
	}


	public Object getValueAt(int row, int col) {
		OWLAxiom axiom;
		MatchedAxiom match = matches.get(row);
		switch (Column.values()[col]) {
		case DESCRIPTION:
			return match.getDescription();
		case SOURCE_AXIOM:
			axiom = match.getSourceAxiom();
			// return axiom == null ? "" : protegeModelManager.getRendering(axiom);
			return axiom;
		case TARGET_AXIOM:
			axiom = match.getTargetAxiom();
			// return axiom == null ? "" : protegeModelManager.getRendering(axiom);
			return axiom;
		default:
			throw new IllegalStateException("Programmer error");
		}
	}

}
