package org.protege.editor.owl.diff.model;

import org.protege.owl.diff.present.EntityBasedDiff;

public interface DifferenceListener {

	void statusChanged(DifferenceEvent event);
}
