package com.sciencedirect.qbs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        InputStream stream = App.class.getResourceAsStream("/countries.json");

        JsonNode jsonNode = mapper.readTree(stream);

        ArrayNode countries = (ArrayNode)jsonNode;

        Map<String, List<String>> countryBorders = new HashMap<>(300);

        Map<String, String> codeToName = new HashMap<>(300);

        countries.forEach(
                    node -> {
                        codeToName.put(node.path("cca3").asText(), node.path("name").path("common").asText());
                        countryBorders.put(node.path("name").path("common").asText(), arrayNodeToList((ArrayNode)node.path("borders")));
                    }
        );

        countryBorders.forEach(
                (k, v) -> {
                    if (v.size() > 0) {
                        List<String> borderCountriesNames = new ArrayList<>(v.size());
                        v.forEach(
                                code -> borderCountriesNames.add(codeToName.get(code))
                        );
                        countryBorders.put(k, borderCountriesNames);
                    }
                }
        );

        countryBorders.forEach(
                (k, v) -> System.out.println(String.format("CREATE (Country { name: '%s' })", k))
        );

        countryBorders.forEach(
                (k, v) -> {
                    // Iterate over each border
                    v.forEach(
                            border -> System.out.println(String.format("MATCH (a:Country),(b:Country)\n" +
                                    "WHERE a.name = '%s' AND b.name = '%s'\n" +
                                    "CREATE (a)-[r:BORDER]->(b)\n", k, border))
                    );
                }
        );

//        countryBorders.forEach(
//                (k, v) -> System.out.println(k + " -> " + v)
//        );

//        Map<Integer, List<String>> borderCountToCountries = new HashMap<>(15);


//        ArrayList[] countriesByCount = new ArrayList[16];
//        for (int i = 0; i < 16; i++) {
//            countriesByCount[i] = new ArrayList();
//        }
//
//        countryBorders.forEach(
//                (k, v) -> countriesByCount[v.size()].add(k)
//        );
//
//        for (int i = 0; i < 16; i++) {
//            System.out.println(i + " " + countriesByCount[i]);
//        }
    }



    private static List<String> arrayNodeToList(ArrayNode arrayNode) {
        List<String> result = new ArrayList<>();

        arrayNode.forEach(
                node -> result.add(node.asText())
        );

        return result;
    }
}
