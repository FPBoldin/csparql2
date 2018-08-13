package it.polimi.jasper.engine;

import it.polimi.jasper.engine.querying.formatter.ResponseFormatterFactory;
import it.polimi.jasper.engine.spe.CSPARQLEngine;
import it.polimi.jasper.engine.streaming.GraphStream;
import it.polimi.yasper.core.quering.ContinuousQuery;
import it.polimi.yasper.core.quering.execution.ContinuousQueryExecution;
import it.polimi.yasper.core.utils.EngineConfiguration;
import it.polimi.yasper.core.utils.QueryConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Riccardo on 03/08/16.
 */
public class TestConfig2 {

    static CSPARQLEngine sr;

    public static void main(String[] args) throws InterruptedException, IOException, ConfigurationException {

        URL resource = TestConfig2.class.getResource("/jasper.properties");
        QueryConfiguration config = new QueryConfiguration(resource.getPath());
        EngineConfiguration ec = EngineConfiguration.loadConfig("/jasper.properties");

        sr = new CSPARQLEngine(0, ec);

        GraphStream writer = new GraphStream("Writer", "http://streamreasoning.org/jasper/streams/stream2", 5);

        writer.setRSPEngine(sr);

        sr.register(writer);

        ContinuousQuery q2 = sr.parseQuery(getQuery(".rspql"));

        System.out.println("<<------>>");
        System.out.println(q2.toString());

        ContinuousQueryExecution ceq = sr.register(q2, config);
        ContinuousQuery cq = ceq.getContinuousQuery();

        sr.register(cq, ResponseFormatterFactory.getGenericResponseSysOutFormatter(ec.getResponseFormat(), true)); // attaches a new *RSP-QL query to the SDS

        //In real application we do not have to start the stream.
        (new Thread(writer)).start();


    }

    public static String getQuery(String suffix) throws IOException {
        URL resource = TestConfig2.class.getResource("/q52" + suffix);
        System.out.println(resource.getPath());
        File file = new File(resource.getPath());
        return FileUtils.readFileToString(file);
    }

}