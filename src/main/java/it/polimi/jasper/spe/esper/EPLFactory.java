package it.polimi.jasper.spe.esper;

import com.espertech.esper.common.client.soda.*;
import it.polimi.jasper.spe.EncodingUtils;
import it.polimi.jasper.spe.operators.s2r.EsperWindowAssigner;
import it.polimi.yasper.core.spe.content.Maintenance;
import it.polimi.yasper.core.spe.operators.s2r.syntax.WindowType;
import it.polimi.yasper.core.spe.report.Report;
import it.polimi.yasper.core.spe.tick.Tick;
import it.polimi.yasper.core.stream.Stream;
import lombok.extern.log4j.Log4j;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by riccardo on 04/09/2017.
 */
@Log4j
public class EPLFactory {

    public static EPStatementObjectModel toEPL(Tick tick, Report report, Maintenance maintenance, long step, String unitStep, WindowType type, String s, View window, List<AnnotationPart> annotations) {
        EPStatementObjectModel stmt = new EPStatementObjectModel();

        stmt.setAnnotations(annotations);

        //MAINTENANCE ISTREAM/DSTREAM

        if (Maintenance.NAIVE.equals(maintenance)) {
            SelectClause selectClause1 = SelectClause.create();
            SelectClause selectClause = selectClause1.addWildcard();
            stmt.setSelectClause(selectClause);
        } else {
            SelectClause selectClause1 = SelectClause.createWildcard(StreamSelector.RSTREAM_ISTREAM_BOTH);
            stmt.setSelectClause(selectClause1);
        }

        FromClause fromClause = FromClause.create();
        FilterStream stream = FilterStream.create(EncodingUtils.encode(s));
        stream.addView(window);
        fromClause.add(stream);
        stmt.setFromClause(fromClause);


        //SETTING TICK
        OutputLimitClause outputLimitClause;
        OutputLimitSelector snapshot = OutputLimitSelector.SNAPSHOT;
        OutputLimitUnit events = OutputLimitUnit.EVENTS;

        if (Tick.TIME_DRIVEN.equals(tick)) {
            TimePeriodExpression timePeriod = getTimePeriod((int) step, unitStep);
            outputLimitClause = OutputLimitClause.create(snapshot, timePeriod);
            stmt.setOutputLimitClause(outputLimitClause);
        } else if (Tick.BATCH_DRIVEN.equals(tick)) {
            outputLimitClause = new OutputLimitClause(snapshot, (double) step);
            stmt.setOutputLimitClause(outputLimitClause);
        } else if (Tick.TUPLE_DRIVEN.equals(tick)) {
            outputLimitClause = new OutputLimitClause(snapshot, 1D);
            stmt.setOutputLimitClause(outputLimitClause);
        }

        return stmt;
    }


    public static List<AnnotationPart> getAnnotations(String name1, int range1, int step1, String s) {
        AnnotationPart name = new AnnotationPart();
        name.setName("Name");
        name.addValue(EncodingUtils.encode(name1));

        AnnotationPart range = new AnnotationPart();
        range.setName("Tag");
        range.addValue("name", "range");
        range.addValue("value", range1 + "");

        AnnotationPart slide = new AnnotationPart();
        slide.setName("Tag");
        slide.addValue("name", "step");
        slide.addValue("value", step1 + "");

        AnnotationPart stream_uri = new AnnotationPart();
        stream_uri.setName("Tag");
        stream_uri.addValue("name", "stream");
        stream_uri.addValue("value", (EncodingUtils.encode(s)));

        return Arrays.asList(name, stream_uri, range, slide);
    }


    public static View getWindow(long range, String unitRange, WindowType type) {
        View view;
        ArrayList<Expression> parameters = new ArrayList<>();
        if (WindowType.Physical.equals(type)) {
            parameters.add(Expressions.constant(range));
            view = View.create("win", "length", parameters);
        } else {
            parameters.add(getTimePeriod((int) range, unitRange));
            view = View.create("win", "time", parameters);
        }
        return view;
    }

    private static TimePeriodExpression getTimePeriod(Integer omega, String unit_omega) {
        String unit = unit_omega.toLowerCase();
        if ("ms".equals(unit) || "millis".equals(unit) || "milliseconds".equals(unit)) {
            return Expressions.timePeriod(null, null, null, null, omega);
        } else if ("s".equals(unit) || "seconds".equals(unit) || "sec".equals(unit)) {
            return Expressions.timePeriod(null, null, null, omega, null);
        } else if ("m".equals(unit) || "minutes".equals(unit) || "min".equals(unit)) {
            return Expressions.timePeriod(null, null, omega, null, null);
        } else if ("h".equals(unit) || "hours".equals(unit) || "hour".equals(unit)) {
            return Expressions.timePeriod(null, omega, null, null, null);
        } else if ("d".equals(unit) || "days".equals(unit)) {
            return Expressions.timePeriod(omega, null, null, null, null);
        }
        return null;
    }

    public static String toEPLSchema(Stream s) {
        CreateSchemaClause schema = new CreateSchemaClause();
        schema.setSchemaName(EncodingUtils.encode(s.getURI()));
        schema.setInherits(new HashSet<String>(Arrays.asList(new String[]{"TStream"})));
        List<SchemaColumnDesc> columns = Arrays.asList(
                new SchemaColumnDesc("sys_timestamp", "long"),
                new SchemaColumnDesc("app_timestamp", "long"),
                new SchemaColumnDesc("content", Object.class.getTypeName()));
        schema.setColumns(columns);
        StringWriter writer = new StringWriter();
        schema.toEPL(writer);
        return writer.toString();
    }

    public static EsperWindowAssigner getWindowAssigner(Tick tick, Maintenance maintenance, Report report, boolean time, String win_iri, String stream_iri, long step, long range, String unitStep, String unitRange, WindowType type) {
        List<AnnotationPart> annotations = new ArrayList<>();//EPLFactory.getAnnotations(stream_iri, range, step, stream_iri);

        AnnotationPart e = new AnnotationPart();
        e.setName("name");
        String window_iri_encoded = EncodingUtils.encode(win_iri);
        e.addValue(window_iri_encoded);
        annotations.add(e);

        View window = EPLFactory.getWindow((int) range, unitRange, type);
        EPStatementObjectModel epStatementObjectModel = EPLFactory.toEPL(tick, report, maintenance, step, unitStep, type, stream_iri, window, annotations);
        log.info(epStatementObjectModel.toEPL());
        return new EsperWindowAssigner(EncodingUtils.encode(stream_iri), window_iri_encoded,tick, report, time, maintenance, epStatementObjectModel);
    }


}
