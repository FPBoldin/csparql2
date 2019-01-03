package it.polimi.jasper.spe.esper;

import com.espertech.esper.common.client.soda.AnnotationPart;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.client.soda.View;
import it.polimi.yasper.core.spe.content.Maintenance;
import it.polimi.yasper.core.spe.operators.s2r.syntax.WindowType;
import it.polimi.yasper.core.spe.report.Report;
import it.polimi.yasper.core.spe.report.ReportImpl;
import it.polimi.yasper.core.spe.report.strategies.NonEmptyContent;
import it.polimi.yasper.core.spe.report.strategies.OnContentChange;
import it.polimi.yasper.core.spe.report.strategies.OnWindowClose;
import it.polimi.yasper.core.spe.tick.Tick;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EPLFactoryTest {

    private String stream = "teststream";
    private Maintenance naive = Maintenance.NAIVE;
    private Maintenance incremental = Maintenance.INCREMENTAL;
    private Tick timeDriven = Tick.TIME_DRIVEN;
    private Tick batchDriven = Tick.BATCH_DRIVEN;
    private Tick tupleDriven = Tick.TUPLE_DRIVEN;

    private WindowType logical = WindowType.Logical;
    private WindowType physical = WindowType.Physical;


    @Test
    public void timeWindow() {
        String unit = "Seconds";
        long step = 5;
        View window = EPLFactory.getWindow(step, unit, logical);
        StringWriter writer = new StringWriter();
        window.toEPL(writer);

        assertEquals("win:time(5 seconds)", writer.toString());
    }

    @Test
    public void tupleWindow() {
        String unit = "Seconds";
        long step = 5;
        View window = EPLFactory.getWindow(step, "events'", physical);
        StringWriter writer = new StringWriter();
        window.toEPL(writer);

        assertEquals("win:length(5L)", writer.toString());
    }

    @Test
    public void csparqlT5() {

        //TUMBLING 5 seconds

        Report report = new ReportImpl();
        report.add(new OnWindowClose());
        report.add(new NonEmptyContent());

        String unit = "Seconds";
        long step = 5;
        View window = EPLFactory.getWindow(step, unit, logical);

        EPStatementObjectModel epStatementObjectModel = EPLFactory.toEPL(timeDriven, report, naive, step, unit, logical, stream, window, Collections.EMPTY_LIST);

        System.out.println(epStatementObjectModel.toEPL());

        assertEquals("select * from " + stream + ".win:time(5 seconds) output snapshot every 5 seconds", epStatementObjectModel.toEPL());
    }

    @Test
    public void cqels5() {

        //TUMBLING 5 seconds

        Report report = new ReportImpl();
        report.add(new OnContentChange());

        String unit = "Seconds";
        long step = 5;
        View window = EPLFactory.getWindow(step, unit, logical);

        List<AnnotationPart> annotations = new ArrayList<>();

        AnnotationPart e = new AnnotationPart();
        e.setName("name");
        e.addValue("stmt1");
        annotations.add(e);

        EPStatementObjectModel epStatementObjectModel = EPLFactory.toEPL(tupleDriven, report, naive, step, unit, logical, stream, window, annotations);

        System.out.println(epStatementObjectModel.toEPL());

        assertEquals("@name('stmt1') select * from " + stream + ".win:time(5 seconds) output snapshot every 1 events", epStatementObjectModel.toEPL());
    }

}
