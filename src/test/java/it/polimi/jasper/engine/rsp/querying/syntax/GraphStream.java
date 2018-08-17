package it.polimi.jasper.engine.rsp.querying.syntax;

import it.polimi.jasper.engine.rsp.streams.RegisteredEPLStream;
import it.polimi.jasper.engine.rsp.streams.items.GraphStreamItem;
import it.polimi.jasper.engine.rsp.streams.schema.GraphStreamSchema;
import it.polimi.yasper.core.stream.rdf.RDFStream;
import it.polimi.yasper.core.stream.schema.StreamSchema;
import lombok.extern.log4j.Log4j;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.Random;

/**
 * Created by Riccardo on 13/08/16.
 */
@Log4j
public class GraphStream extends RDFStream implements Runnable {

    private StreamSchema schema = new GraphStreamSchema();

    @Override
    public StreamSchema getSchema() {
        return schema;
    }

    protected int grow_rate;
    private RegisteredEPLStream s;

    private String type;

    public GraphStream(String name, String stream_uri, int grow_rate) {
        super(stream_uri);
        this.type = name;
        this.grow_rate = grow_rate;
    }

    public void setWritable(RegisteredEPLStream e) {
        this.s = e;
    }

    public void run() {
        int i = 1;
        int j = 1;
        while (true) {
            Model m = ModelFactory.createDefaultModel();
            Random r = new Random();

            String uri = "http://www.streamreasoning/it.polimi.jasper.test/artist#";
            Resource person = ResourceFactory.createResource(stream_uri + "/artist1");
            Resource type = ResourceFactory.createResource(uri + this.type);
            Property hasAge = ResourceFactory.createProperty(uri + "hasAge");
            Property hasTimestamp = ResourceFactory.createProperty(uri + "generatedAt");
            Literal age = m.createTypedLiteral(r.nextInt(99));
            Literal ts = m.createTypedLiteral(new Integer(i * 1000));

            //m.apply(m.createStatement(person, RDF.type, type));
            // m.apply(m.createStatement(person, hasAge, age));
            m.add(m.createStatement(person, hasTimestamp, ts));
            m.add(m.createStatement(person, RDF.type, type));
            m.add(m.createStatement(person, hasAge, age));

            GraphStreamItem t = new GraphStreamItem(i * 1000, m.getGraph(), stream_uri);
            System.out.println("[" + System.currentTimeMillis() + "] Sending [" + t + "] on " + stream_uri + " at " + i * 1000);

            if (s != null)
                this.s.put(t);
            try {
                log.info("Sleep");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i += grow_rate;
            j++;
        }
    }
}