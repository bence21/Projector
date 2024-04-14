package com.bence.projector.server.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class DateSerializer2 extends DateTimeSerializerBase<Date> {


    protected DateSerializer2(Class<Date> type, Boolean useTimestamp, DateFormat customFormat) {
        super(type, useTimestamp, customFormat);
    }

    @Override
    public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
        return null;
    }

    @Override
    protected long _timestamp(Date value) {
        return 0;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String s;
        if (value == null) {
            s = "null";
        } else {
            s = value.getTime() + "";
        }
        gen.writeNumber(s);
    }
}
