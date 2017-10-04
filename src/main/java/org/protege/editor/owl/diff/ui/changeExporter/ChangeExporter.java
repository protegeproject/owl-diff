package org.protege.editor.owl.diff.ui.changeExporter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import javax.swing.JFileChooser;

import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeExporter implements ActionListener {
	private DifferenceManager diffManager;
	public static final Logger LOGGER = LoggerFactory.getLogger(ChangeExporter.class.getName());
	private Component parent;

	public ChangeExporter(DifferenceManager dm, Component parent) {
		diffManager = dm;
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Changes changes = diffManager.getEngine().getChanges();

		// DEBUG CHANGES

		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			String sourceStr = diff.getSourceEntity() == null ? "null" : diff.getSourceEntity().getIRI().toString();
			String targeStr = diff.getTargetEntity() == null ? "null" : diff.getTargetEntity().getIRI().toString();
			LOGGER.info(diff.getDiffTypeDescription() + " " + sourceStr + " " + targeStr);
			for (MatchedAxiom axiom : diff.getAxiomMatches()) {
				String sourceStrAx;
				OWLAxiom sourceAx = axiom.getSourceAxiom();
				if (sourceAx != null) {
					sourceStrAx = sourceAx.toString();
				} else {
					sourceStrAx = "null";
				}
				String targetStrAx;
				OWLAxiom targetAx = axiom.getTargetAxiom();
				if (targetAx != null) {
					targetStrAx = targetAx.toString();
				} else {
					targetStrAx = "null";
				}
				LOGGER.info(axiom.getDescription() + " " + sourceStrAx + " " + targetStrAx);
			}
		}

		// END DEBUG CHANGES

		// select file

		JFileChooser chooser = new JFileChooser();
		if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();

			try {
				file.createNewFile();
				OutputStream out = new FileOutputStream(file);
				
				PrintStream outStream = new PrintStream(out); 
				for (EntityBasedDiff diff: changes.getEntityBasedDiffs()) {
					String sourceStr = diff.getSourceEntity() == null ? "null" : diff.getSourceEntity().getIRI().toString();
					String targeStr = diff.getTargetEntity() == null ? "null" : diff.getTargetEntity().getIRI().toString();
					outStream.println(diff.getDiffTypeDescription() + " " + sourceStr + " " + targeStr);
					for (MatchedAxiom axiom : diff.getAxiomMatches()) {
						String sourceStrAx;
						OWLAxiom sourceAx = axiom.getSourceAxiom();
						if (sourceAx != null) {
							sourceStrAx = sourceAx.toString();
						} else {
							sourceStrAx = "null";
						}
						String targetStrAx;
						OWLAxiom targetAx = axiom.getTargetAxiom();
						if (targetAx != null) {
							targetStrAx = targetAx.toString();
						} else {
							targetStrAx = "null";
						}
						outStream.println("# "+axiom.getDescription() + " " + sourceStrAx + " " + targetStrAx);
					}
				}
				outStream.close();
				
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

}
