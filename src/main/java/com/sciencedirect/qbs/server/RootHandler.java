package com.sciencedirect.qbs.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

import static org.neo4j.driver.v1.Values.parameters;

public class RootHandler implements HttpHandler {

    private final Session session;

    public RootHandler(final Session session) {
        this.session = session;
    }

    @Override
    public void handle(final HttpExchange he) throws IOException {

        // Find a country with borders
        // Find a country that links to first country

        // return country 1, country 2, number of links and the path

        final String countryOne = "Portugal";

        final StatementResult result = session.run(
                "MATCH (a { name: {countryOne} })-[:Border*2..5]-(b:Country) RETURN COLLECT(DISTINCT b.name)",
                parameters("countryOne", countryOne));
//
//        while (result.hasNext()) {
//            Record record = result.next();
//            System.out.println(record);
//
//            // randomly select country two
//        }


        final String countryTwo = "dfgsdf";

        final Integer questionNumber = ThreadLocalRandom.current().nextInt(1, 10);


        final StringBuilder response = new StringBuilder("<html>")
                .append("<head><style>body { font: tahoma } p.white { color: white; } p.white:hover { color: black; weight: bold; }</style></head>")
                .append("<body><h1>Round 1 of 1</h1><h2> Question #")
                .append(questionNumber)
                .append(" (roughly):</h2>");

        response.append("<p>What is the lowest number of countries you have to go through to get from <b>")
                .append(countryOne)
                .append("</b> to <b>")
                .append(countryTwo)
                .append("</b>?</p>");

        response.append("<p class=\"white\">THE ANSWER IS HIDDEN!</p></body></html>");


        final String responseString = response.toString();

        he.sendResponseHeaders(200, responseString.getBytes().length);

        final OutputStream os = he.getResponseBody();
        os.write(responseString.getBytes());
        os.close();
    }
}