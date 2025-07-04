package uz.pdp.util;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class FileUtils {
    private static final String PATH = "src/main/java/uz/pdp/dao/";

    private static final ObjectMapper objectMapper;
    private static final XmlMapper xmlMapper;

    static {
        objectMapper = JsonMapper.builder()
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .build();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        xmlMapper = XmlMapper.builder()
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .build();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private FileUtils() {
    }

    public static <T> void writeToJson(String fileName, T t) throws IOException {
        objectMapper
                .writeValue(new File(PATH + fileName), t);
    }


    public static <T> List<T> readFromJson(String fileName, Class<T> clazz) throws IOException {
        return objectMapper
                .readValue(new File(PATH + fileName),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }


    public static <T> void writeToXml(String fileName, T t) throws IOException {
        xmlMapper
                .writeValue(new File(PATH + fileName), t);
    }


    public static <T> List<T> readFromXml(String fileName, Class<T> clazz) throws IOException {
        return xmlMapper
                .readValue(new File(PATH + fileName),
                        xmlMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}