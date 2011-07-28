package org.protege.editor.owl.diff.model;

import java.util.Comparator;

import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.service.RenderingService;

public class EntityBasedDiffComparator implements Comparator<EntityBasedDiff> {
	private RenderingService renderer;

	public EntityBasedDiffComparator(RenderingService renderer) {
		this.renderer = renderer;
	}
	
	public int compare(EntityBasedDiff diff1, EntityBasedDiff diff2) {
		return renderer.renderDiff(diff1).compareTo(renderer.renderDiff(diff2));
	}

}
