package com.github.angoothachap.kafka.connect.smt;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class SearchReplaceFieldTest {
    private SearchReplaceField<SinkRecord> xform = new SearchReplaceField.Value<>();
    @After
    public void teardown() {
        xform.close();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void schemaless() {
        final Map<String, String> props = new HashMap<>();
        props.put("search.replace", "abc:xyz");

        xform.configure(props);

        final Map<String, Object> value = new HashMap<>();
        value.put("abc", "This is abc. abc is a good start for an abc");
        value.put("etc", "etc");

        final SinkRecord record = new SinkRecord("test", 0, null, null, null, value, 0);
        final SinkRecord transformedRecord = xform.apply(record);

        final Map<String, Object> updatedValue = (Map<String, Object>) transformedRecord.value();
        assertEquals(2, updatedValue.size());
        assertEquals("This is xyz. xyz is a good start for an xyz", updatedValue.get("abc"));
        assertEquals("etc", updatedValue.get("etc"));
    }

    @Test
    public void withSchema() {
        final Map<String, String> props = new HashMap<>();
        props.put("search.replace", "abc:xyz,foo:bar");

        xform.configure(props);

        final Schema schema = SchemaBuilder.struct()
                .field("abc", Schema.STRING_SCHEMA)
                .field("dont", Schema.STRING_SCHEMA)
                .build();

        final Struct value = new Struct(schema);
        value.put("abc", "This is abc. abc is a good start for an abc");
        value.put("dont", "foo");

        final SinkRecord record = new SinkRecord("test", 0, null, null, schema, value, 0);
        final SinkRecord transformedRecord = xform.apply(record);

        final Struct updatedValue = (Struct) transformedRecord.value();

        assertEquals(2, updatedValue.schema().fields().size());
        assertEquals("This is xyz. xyz is a good start for an xyz", updatedValue.getString("abc"));
        assertEquals("bar", updatedValue.getString("dont"));
    }

}
