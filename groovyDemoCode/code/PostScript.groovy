
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

handle(out,param,instance,global);

void handle(JsonNode out,ObjectNode param, JsonNode instance, JsonNode global) {
    JsonNode uniqueSet = param.path('unique')
    if (!uniqueSet.isObject()) {
        return
    }
    Iterator<String> uniqueFieldNames = uniqueSet.fieldNames();
    while (uniqueFieldNames.hasNext()) {
        String key = uniqueFieldNames.next();
        JsonNode waitForValidate = out.at(key)
        if (!waitForValidate.isArray() || waitForValidate.size() == 0) {
            continue;
        }
        JsonNode arrayNode = uniqueSet.path(key);
        if (!arrayNode.isArray() || arrayNode.size() == 0) {
            continue
        }
        Set<String> set = new HashSet<>()
        for (JsonNode item : arrayNode) {
            set.clear();
            for (JsonNode arrayItem : waitForValidate) {
                JsonNode arrayValue = arrayItem.path(item.asText());
                String value = '';
                if (arrayValue.isTextual()) {
                    value = arrayValue.asText();
                }else if (arrayValue.isInt()) {
                    value = '' + arrayValue.asInt()
                }else if (arrayValue.isLong()) {
                    value = '' + arrayValue.asLong()
                }else if (arrayValue.isDouble()) {
                    value = '' + arrayValue.asDouble()
                }else if (arrayValue.isFloat()) {
                    value = '' + arrayValue.asDouble()
                }else if (arrayValue.isBoolean()) {
                    value = '' + arrayValue.asBoolean()
                }else {
                    continue;
                }
                if (set.contains(value)) {
                    throw new Exception('field ' + item.asText() + ' duplicated');
                }else {
                    set.add(value)
                }
            }
        }
    }
}
