package org.protege.editor.owl.diff.ui.render;

import org.protege.editor.owl.diff.model.DifferenceManager;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.present.algorithms.IdentifyDeprecatedEntity;
import org.protege.owl.diff.service.RenderingService;

import javax.swing.*;
import java.awt.*;

public class EntityBasedDiffRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = -2257588249282053158L;
	private DifferenceManager diffs;
	
	public EntityBasedDiffRenderer(DifferenceManager diffs) {
		this.diffs = diffs;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
												  int index, boolean isSelected, boolean cellHasFocus) {
		JLabel text = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof EntityBasedDiff) {
			RenderingService renderer = RenderingService.get(diffs.getEngine());
			EntityBasedDiff diff = (EntityBasedDiff) value;
			StringBuffer sb = new StringBuffer();
			appendHtmlHeader(sb, diff);
			sb.append(renderer.renderDiff(diff));
			appendHtmlFooter(sb, diff);
			text.setText(sb.toString());
		}
		return this;
	}
	
	private void appendHtmlHeader(StringBuffer sb, EntityBasedDiff diff) {
		sb.append("<html><body>");
		switch (diff.getDiffType()) {
		case CREATED:
			sb.append("<font color=\"blue\">");
			break;
		case DELETED:
			sb.append("<strike>");
			break;
		}
	}
	
	private void appendHtmlFooter(StringBuffer sb, EntityBasedDiff diff) {
		switch (diff.getDiffType()) {
		case CREATED:
			sb.append("</font>");
			break;
		case DELETED:
			sb.append("</strike>");
			break;
		}
		if (isDeprecation(diff)) {
			sb.append("<sup><b>D</b></sup>");
		}
		sb.append("</body></html>");
	}
	
	private boolean isDeprecation(EntityBasedDiff diff) {
		for (MatchedAxiom match : diff.getAxiomMatches()) {
			if (match.getDescription().equals(IdentifyDeprecatedEntity.AXIOM_IS_DEPRECATION)) {
				return true;
			}
		}
		return false;
	}
	
}