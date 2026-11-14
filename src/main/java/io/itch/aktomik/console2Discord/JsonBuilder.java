package io.itch.aktomik.console2Discord;

import com.google.gson.JsonObject;

class JsonBuilder {
    private final JsonObject json = new JsonObject();

    public String toJson() {
        return json.toString();
    }

    public JsonBuilder add(String key, String value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder add(String key, JsonBuilder value) {
        json.add(key, value.json);
        return this;
    }
}