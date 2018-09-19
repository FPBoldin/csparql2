package it.polimi.jasper.spe.operators.r2r.syntax;

import it.polimi.yasper.core.spe.operators.r2r.ContinuousQuery;
import it.polimi.yasper.core.spe.operators.r2r.QueryConfiguration;
import it.polimi.yasper.core.spe.operators.r2s.StreamOperator;
import it.polimi.yasper.core.spe.operators.s2r.syntax.WindowNode;
import it.polimi.yasper.core.spe.time.Time;
import it.polimi.yasper.core.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryVisitor;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.system.IRIResolver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RSPQLJenaQuery extends Query implements ContinuousQuery {
    public static String RSTREAM = "RSTREAM";
    public static String ISTREAM = "ISTREAM";
    public static String DSTREAM = "DSTREAM";
    public static String defaultStreamType = RSTREAM;

    private String streamType = defaultStreamType;
    private String outputStreamUri;
    private List<NamedWindow> namedWindows = new ArrayList<>();
    private List<ElementNamedWindow> elementNamedWindows = new ArrayList<>();
    private Map<WindowNode, Stream> windowMap = new HashMap<>();
    @Getter
    @Setter
    private QueryConfiguration configuration;

    public RSPQLJenaQuery() {
    }

    public RSPQLJenaQuery(IRIResolver resolver) {
        super.setResolver(resolver);
    }

    public void addNamedWindow(Node windowUri, Node streamUri, Duration range, Duration step) {
        NamedWindow namedWindow = new NamedWindow(this, windowUri, streamUri, NamedWindow.LOGICAL_WINDOW);
        namedWindow.setLogicalRange(range);
        namedWindow.setLogicalStep(step);
        namedWindows.add(namedWindow);
        windowMap.put(namedWindow, new StreamNode(namedWindow.getStreamUri()));
    }

    public void addNamedWindow(Node windowUri, Node streamUri, int range, int step) {
        NamedWindow namedWindow = new NamedWindow(this, windowUri, streamUri, NamedWindow.PHYSICAL_WINDOW);
        namedWindow.setPhysicalRange(range);
        namedWindow.setPhysicalStep(step);
        namedWindows.add(namedWindow);
        windowMap.put(namedWindow, new StreamNode(namedWindow.getStreamUri()));
    }

    @Override
    public void addNamedWindow(String streamUri, WindowNode wo) {
        StreamNode value = new StreamNode(NodeFactory.createURI(streamUri));
        windowMap.put(wo, value);
    }

    public void setIstream() {
        streamType = ISTREAM;
    }

    public void setRstream() {
        streamType = RSTREAM;
    }

    public void setDstream() {
        streamType = DSTREAM;
    }

    @Override
    public boolean isIstream() {
        return false;
    }

    @Override
    public boolean isRstream() {
        return false;
    }

    @Override
    public boolean isDstream() {
        return false;
    }

    @Override
    public void setSelect() {

    }

    @Override
    public void setConstruct() {

    }

    public void setOutputStream(String uri) {
        outputStreamUri = uri;
    }

    @Override
    public String getOutputStream() {
        return null;
    }

    public String getStreamType() {
        return streamType;
    }

    public String getOutputStreamUri() {
        return outputStreamUri;
    }

    public List<NamedWindow> getNamedWindows() {
        return namedWindows;
    }

    public List<ElementNamedWindow> getElementNamedWindows() {
        return elementNamedWindows;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public StreamOperator getR2S() {
        return null;
    }

    @Override
    public boolean isRecursive() {
        return false;
    }

    @Override
    public Map<WindowNode, Stream> getWindowMap() {
        return windowMap;
    }

    @Override
    public List<String> getNamedwindowsURIs() {
        return getNamedWindows().stream().map(NamedWindow::getWindowUri).map(Node::getURI).collect(Collectors.toList());
    }

    @Override
    public String getSPARQL() {
        return ((Query) this).serialize(Syntax.syntaxARQ);
    }

    @Override
    public Time getTime() {
        return null;
    }


    public void visit(QueryVisitor visitor) {
        visitor.startVisit(this);
        visitor.visitResultForm(this);
        visitor.visitPrologue(this);
        if (this.isSelectType())
            visitor.visitSelectResultForm(this);
        if (this.isConstructType())
            visitor.visitConstructResultForm(this);
        if (this.isDescribeType())
            visitor.visitDescribeResultForm(this);
        if (this.isAskType())
            visitor.visitAskResultForm(this);
        visitor.visitDatasetDecl(this);
        visitor.visitQueryPattern(this);
        visitor.visitGroupBy(this);
        visitor.visitHaving(this);
        visitor.visitOrderBy(this);
        visitor.visitOffset(this);
        visitor.visitLimit(this);
        visitor.visitValues(this);
        visitor.finishVisit(this);
    }

    public void addElementNamedWindow(ElementNamedWindow elementNamedWindow) {
        elementNamedWindows.add(elementNamedWindow);
    }
}