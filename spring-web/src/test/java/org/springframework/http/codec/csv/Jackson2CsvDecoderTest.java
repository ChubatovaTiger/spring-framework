package org.springframework.http.codec.csv;

import java.util.Map;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.MimeType;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link Jackson2CsvDecoder}.
 */
class Jackson2CsvDecoderTest {
    private static final ResolvableType CSV_RECORD_TYPE =
            ResolvableType.forClassWithGenerics(Map.class, String.class, String.class);
    private static final MimeType MIME_TYPE_CSV_UTF_8 =
            MimeType.valueOf("text/csv;charset=UTF-8");

    /**
     * Test for {@link Jackson2CsvDecoder#decode(Publisher, ResolvableType, MimeType, Map)}.
     */
    @Test
    void decode() {
        var schema = CsvSchema.builder()
                .setColumnSeparator(';')
                .setLineSeparator('\n')
                .addColumn("header1")
                .addColumn("header2")
                .build();
        var decoder = decoder(schema);
        decoder.setMaxInMemorySize(16);

        var buffers = dataBuffer("""
                value1;value2
                value3;value4
                """);

        var rows = decoder.decode(buffers, CSV_RECORD_TYPE, MIME_TYPE_CSV_UTF_8, Map.of());

        assertThat(rows.toIterable()).containsExactly(
                Map.of("header1", "value1", "header2", "value2"),
                Map.of("header1", "value3", "header2", "value4"));
    }

    //
    // Helper.
    //

    private Jackson2CsvDecoder decoder(CsvSchema schema) {
        var csvMapper = CsvMapper.builder().build();
        return new Jackson2CsvDecoder(csvMapper, schema);
    }

    private Flux<DataBuffer> dataBuffer(String csv) {
        var factory = DefaultDataBufferFactory.sharedInstance;
        var buffer = factory.wrap(csv.replaceAll("\\R", "\n").getBytes(UTF_8));
        return Flux.just(buffer);
    }
}