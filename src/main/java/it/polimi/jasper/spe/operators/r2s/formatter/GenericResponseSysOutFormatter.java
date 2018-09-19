package it.polimi.jasper.spe.operators.r2s.formatter;

import it.polimi.jasper.spe.operators.r2s.results.ConstructResponse;
import it.polimi.jasper.spe.operators.r2s.results.SelectResponse;
import it.polimi.yasper.core.spe.operators.r2s.result.QueryResultFormatter;
import org.apache.jena.query.ResultSetFormatter;

import java.io.OutputStream;
import java.util.Observable;

/**
 * Created by riccardo on 03/07/2017.
 */

public class GenericResponseSysOutFormatter extends QueryResultFormatter {

    long last_result = -1L;

    private final OutputStream os;

    public GenericResponseSysOutFormatter(String format, boolean distinct, OutputStream os) {
        super(format, distinct);
        this.os = os;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof SelectResponse) {
            SelectResponse sr = (SelectResponse) arg;
            if (sr.getCep_timestamp() != last_result && distinct) {
                last_result = sr.getCep_timestamp();
                System.out.println("[" + System.currentTimeMillis() + "] Result at [" + last_result + "]");
                ResultSetFormatter.out(os, sr.getResults());
            }
        } else if (arg instanceof ConstructResponse) {
            ConstructResponse sr = (ConstructResponse) arg;

            if (sr.getCep_timestamp() != last_result && distinct) {
                sr.getResults().write(os, format);
                last_result = sr.getCep_timestamp();
            }
        }

    }
}