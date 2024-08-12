package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMActivityLink implements EOFMAct {
    private Element element;
    private EOFMParser parser;

    private String link;
    private EOFMActivity linkedActivity;
    private EOFMActivity activityCopy;
    
    private EOFMAct parent;

    public EOFMActivityLink() {
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element xmlElement) {
        this.element = xmlElement;
        if (parser != null)
            extractElementData();
    }

    public void setParser(EOFMParser parser) {
        this.parser = parser;
        if (element != null)
            extractElementData();
    }

    public EOFMParser getParser() {
        return parser;
    }

    public EOFMActivity getActivity() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy;
    }

    public String getName() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getName();
    }

    public void writeTransitions(PRISMWriter prismWriter, ArrayList<EOFMAct> peers, String readyVariableName, String submittedVariableName) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.writeTransitions(prismWriter, peers, readyVariableName, submittedVariableName);
    }

    public void writeVariableDeclarations(PRISMWriter prismWriter) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.writeVariableDeclarations(prismWriter);
    }

    public void writeInitialValues(PRISMWriter prismWriter) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.writeInitialValues(prismWriter);
    }

    public void writeRecursiveAssignment(PRISMWriter prismWriter, String assignment) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.writeRecursiveAssignment(prismWriter, assignment);
    }

    private void extractElementData() {
        link = parser.extractAttributeData(element, EOFM_LINK);
        linkedActivity = (EOFMActivity)parser.getEOFMElement(link);
        if (linkedActivity != null){
            activityCopy = linkedActivity.getCopyWithNewName(parser.getUniqueSuffix());
        }
        parser.registerEOFMElement(activityCopy.getName(), activityCopy);
    }

    public EOFMActivity getCopyWithNewName(String nameSuffix) {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getCopyWithNewName(nameSuffix);
    }

    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.writePassThroughActionDoneAssignments(prismWriter);
    }

    public EOFMAct getChild(int index) {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getChild(index);
    }
    
    public String getRecursiveNotExecuting() {
        return activityCopy.getRecursiveNotExecuting();
    }
    
    public Element BuildXMLDoc(Document doc) {
        return activityCopy.BuildXMLDoc(doc);
    }

    @Override
    public void setParent(EOFMAct parent) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.setParent(parent);
    }

    @Override
    public EOFMAct getParent() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getParent();
    }

    @Override
    public void writeCognitiveFunctions(PRISMWriter prismWriter) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.writeCognitiveFunctions(prismWriter);
    }

    @Override
    public double getCPCSum() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getCPCSum();
    }
    
    @Override
    public double getCPCSumUp() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getCPCSumUp();
    }

    @Override
    public double getCPCSumDown() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getCPCSumDown();
    }

    @Override
    public void setCPCSum(double cpcSum) {
        if (activityCopy == null)
            extractElementData();
        activityCopy.setCPCSum(cpcSum);
    }

    @Override
    public String getCognitiveSuffix() {
        if (activityCopy == null)
            extractElementData();
        return activityCopy.getCognitiveSuffix();
    }
}
