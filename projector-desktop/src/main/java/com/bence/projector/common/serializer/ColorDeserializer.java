package com.bence.projector.common.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;

public class ColorDeserializer implements JsonDeserializer<Color> {

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Color.web(json.getAsString());
    }
}
