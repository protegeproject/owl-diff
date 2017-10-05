package org.protege.editor.owl.diff.ui.changeExporter;

import java.lang.reflect.Type;

import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchedAxiom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EntityBasedDiffJSonSerializer implements JsonSerializer<EntityBasedDiff> {
	public static String CHANGETYPE = "changeType";
	public static String SOURCE = "source";
	public static String TARGET = "target";
	public static String AXIOM_MATCHES = "axiomMatches";
	public static String MATCH_DESCRIPTION = "description";

	@Override
	public JsonElement serialize(EntityBasedDiff arg0, Type arg1, JsonSerializationContext arg2) {
		JsonObject result = new JsonObject();
		result.add(CHANGETYPE, new JsonPrimitive(arg0.getDiffTypeDescription()));

		if (arg0.getSourceEntity() != null) {
			result.add(SOURCE, new JsonPrimitive(arg0.getSourceEntity().getIRI().toString()));
		}

		if (arg0.getTargetEntity() != null) {
			result.add(TARGET, new JsonPrimitive(arg0.getTargetEntity().getIRI().toString()));
		}

		if (!arg0.getAxiomMatches().isEmpty()) {
			JsonArray arr = new JsonArray();
			for (MatchedAxiom ax : arg0.getAxiomMatches()) {
				JsonObject match = new JsonObject();
				match.add(MATCH_DESCRIPTION, new JsonPrimitive(ax.getDescription().toString()));
				if (ax.getSourceAxiom() != null) {
					match.add(SOURCE, new JsonPrimitive(ax.getSourceAxiom().toString()));
				}
				if (ax.getTargetAxiom() != null) {
					match.add(TARGET, new JsonPrimitive(ax.getTargetAxiom().toString()));
				}
				arr.add(match);
			}

			result.add(AXIOM_MATCHES, arr);

		}
		return result;
	}

}
