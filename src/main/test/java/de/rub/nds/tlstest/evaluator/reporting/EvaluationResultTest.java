package de.rub.nds.tlstest.evaluator.reporting;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationResultTest {


    @Test
    public void serializeSingleValue() {
        EvaluationResult a = new EvaluationResult("test1", 1);

        ObjectMapper mapper = new ObjectMapper(JsonFactory.builder().build());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            mapper.writeValue(output, a);
            EvaluationResult b = mapper.readValue(output.toByteArray(), EvaluationResult.class);
            assertEquals(a.getExitCode(), b.getExitCode());
            assertEquals(a.getImageName(), b.getImageName());
        } catch (Exception e) {
            throw new AssertionError("serialization failed");
        }
    }

    @Test
    public void serializeList() {
        EvaluationResult[] a = new EvaluationResult[]{
                new EvaluationResult("test1", 1),
                new EvaluationResult("test2", 2)
        };

        ObjectMapper mapper = new ObjectMapper(JsonFactory.builder().build());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            mapper.writeValue(output, a);
            EvaluationResult[] b = mapper.readValue(output.toByteArray(), EvaluationResult[].class);
            assertEquals(2, b.length);
            assertEquals(a[0].getExitCode(), b[0].getExitCode());
            assertEquals(a[0].getImageName(), b[0].getImageName());
            assertEquals(a[1].getExitCode(), b[1].getExitCode());
            assertEquals(a[1].getImageName(), b[1].getImageName());
        } catch (Exception e) {
            throw new AssertionError("serialization failed");
        }
    }
}