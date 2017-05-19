package com.sciencedirect.qbs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

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


        final Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
        final Session session = driver.session();

        countryBorders.forEach(
                (k, v) ->
                        session.run("CREATE (a:Country {name: {name}})", parameters("name", k))
        );


        /*

                StatementResult result = session.run("MATCH (a:Person) WHERE a.name = {name} " +
                                             "RETURN a.name AS name, a.title AS title",
                                             parameters("name", "Arthur"));
        while (result.hasNext()) {
            Record record = result.next();
            System.out.println(record.get("title").asString() + " " + record.get("name").asString());
        }



         */


        countryBorders.forEach(
                (k, v) -> {
                    // Iterate over each border
                    v.forEach(border -> session.run("MATCH (a:Country),(b:Country)\n" +
                                                    "WHERE a.name = {aName} AND b.name = {bName}\n" +
                                                    "CREATE (a)-[r:Border]->(b)\n",
                                                    parameters("aName", k, "bName", border)));
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




        session.close();
        driver.close();
    }



    private static List<String> arrayNodeToList(ArrayNode arrayNode) {
        List<String> result = new ArrayList<>();

        arrayNode.forEach(
                node -> result.add(node.asText())
        );

        return result;
    }
}
