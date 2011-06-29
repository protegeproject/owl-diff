package org.protege.editor.owl.diff.ui.view;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.service.RenderingService;

public class EntityBasedDiffRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = -2257588249282053158L;
	private RenderingService renderer;
	
	public EntityBasedDiffRenderer(RenderingService renderer) {
		this.renderer = renderer;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
												  int index, boolean isSelected, boolean cellHasFocus) {
		JLabel text = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof EntityBasedDiff) {
			EntityBasedDiff diff = (EntityBasedDiff) value;
			text.setText(renderer.renderDiff(diff));
		}
		return this;
	}
	
}