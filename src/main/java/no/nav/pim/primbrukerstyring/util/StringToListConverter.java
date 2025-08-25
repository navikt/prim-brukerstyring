package no.nav.pim.primbrukerstyring.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringToListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if(list == null) return "";
        return String.join(",", list);
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if(joined == null || joined.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(joined.split(",")));
    }
}
