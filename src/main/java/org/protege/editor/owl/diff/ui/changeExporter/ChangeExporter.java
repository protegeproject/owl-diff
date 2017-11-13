package org.protege.editor.owl.diff.ui.changeExporter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		// select file
		JFileChooser chooser = new JFileChooser();
		if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				// write output to file
				file.createNewFile();
				OutputStream out = new FileOutputStream(file);

				PrintStream outStream = new PrintStream(out);
				Gson gson = new GsonBuilder().setPrettyPrinting()
						.registerTypeAdapter(EntityBasedDiff.class, new EntityBasedDiffJSonSerializer()).create();
				outStream.println("[");
				Boolean first = true;
				for (EntityBasedDiff diff:changes.getEntityBasedDiffs()) {
					String jsonRep = gson.toJson(diff);
					if (first) {
						first = false;
					} else {
						outStream.println(",");
					}
					
					outStream.print(jsonRep);
				}
				outStream.println("]");
				
				outStream.close();
				JOptionPane.showMessageDialog(parent, "Saved Differences to "+file.getPath());
			} catch (IOException e1) {
				// error message
				JOptionPane.showMessageDialog(parent,
						"Something went wrong saving the file. Error description:" + e1.getMessage());
			}
		}

	}

}
