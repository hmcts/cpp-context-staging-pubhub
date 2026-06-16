package uk.gov.moj.cpp.staging.pubhub.event.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    private JsonUtil() {
    }

    public static void renameKey(final ObjectNode node, final String oldKeyName, final String newKeyName) {
        LOGGER.info("Renaming {} to {}", oldKeyName, newKeyName);

        final List<String> keysToRemove = new ArrayList<>();
        final Map<String, JsonNode> newEntries = new HashMap<>();

        node.fields().forEachRemaining(entry -> {
            final JsonNode childNode = entry.getValue();
            if (entry.getKey().equals(oldKeyName)) {
                keysToRemove.add(oldKeyName);
                newEntries.put(newKeyName, childNode);
            } else if (childNode.isObject()) {// If the child is an object, recurse into it
                renameKey((ObjectNode) childNode, oldKeyName, newKeyName);
            } else if (childNode.isArray()) {
                for (final JsonNode arrayItem : childNode) {
                    if (arrayItem.isObject()) {
                        renameKey((ObjectNode) arrayItem, oldKeyName, newKeyName);
                    }
                }
            }
        });

        keysToRemove.forEach(node::remove);
        newEntries.forEach(node::set);
    }

}
