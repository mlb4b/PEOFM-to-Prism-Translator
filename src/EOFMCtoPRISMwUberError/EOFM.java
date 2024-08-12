/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author mlb4b
 */
public class EOFM implements EOFMAct{
    private Element element;
    private EOFMParser parser;
    private EOFMActivity activity;
    private EOFMHumanOperator human;
    private String replacedby;
    //private double cpcSum;

    public void setElement(Element element) {
        this.element = element;
        if (parser != null)
            extractElementData();
    }

    public Element getElement() {
        return element;
    }

    public void setParser(EOFMParser parser) {
        this.parser = parser;
        if (element != null)
            extractElementData();
    }

    public EOFMParser getParser() {
        return parser;
    }
    
    private void extractElementData() {
        replacedby = parser.extractAttributeData(element, EOFM_REPLACEDBY);
        activity = (EOFMActivity)parser.extractSingleElement(EOFM_ACTIVITY, element, EOFMActivity.class);
        activity.setParent(this);
        if (!replacedby.isBlank()) {
            activity.setReplacedBy(replacedby);
        }
        parser.addConstant(ACT_READY, ACT_READY_VAL);
        parser.addConstant(ACT_EXECUTING, ACT_EXECUTING_VAL);
        parser.addConstant(ACT_DONE, ACT_DONE_VAL);
    }

    public String getName() {
        return activity.getName();
    }

    public void writeTransitions(PRISMWriter prismWriter, ArrayList<EOFMAct> peers, String readyVariableName, String submittedVariableName) {
        activity.writeTransitions(prismWriter, peers, readyVariableName, submittedVariableName);
    }

    public void writeVariableDeclarations(PRISMWriter prismWriter) {
        activity.writeVariableDeclarations(prismWriter);
    }

    public void writeInitialValues(PRISMWriter prismWriter) {
        activity.writeInitialValues(prismWriter);
    }

    public void writeRecursiveAssignment(PRISMWriter prismWriter, String assignment) {
        activity.writeRecursiveAssignment(prismWriter, assignment);
    }

    public EOFM getCopyWithNewName(String nameSuffix) {
        EOFM returnEOFM = new EOFM();
        returnEOFM.activity = activity.getCopyWithNewName(nameSuffix);
        return returnEOFM;
    }
    
    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter) {
        activity.writePassThroughActionDoneAssignments(prismWriter);
    }
    
    public String getRecursiveNotExecuting() {
        return activity.getRecursiveNotExecuting();
    }

    public EOFMAct getChild(int index) {
         return (EOFMAct) activity.getChild(index);
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_EOFM);
        returnElement.appendChild(activity.BuildXMLDoc(doc));
        return returnElement;
    }

    @Override
    public void writeCognitiveFunctions(PRISMWriter prismWriter) {
        /*
        public static int DEFAULT_CPCSUM = -10;
        public static int NO_CPCSUM = -99;
        
        double activityCPCSum = activity.getCPCSum();
        if (activityCPCSum == NO_CPCSUM) {
            setCPCSum(DEFAULT_CPCSUM);
        }*/
        //EOFMs.writeCognitiveFunctions(prismWriter, cpcSum, activity.getName());
        activity.writeCognitiveFunctions(prismWriter);
    }

    @Override
    public double getCPCSum() {
        return DEFAULT_CPCSUM;
    }
    
    @Override
    public double getCPCSumUp() {
        return human.getCPCSum();
    }
    
    @Override
    public double getCPCSumDown() {
        return activity.getCPCSum();
    }
    
    @Override
    public void setCPCSum(double cpcSum) {
        activity.setCPCSum(cpcSum);
        //activity.setCPCSum(cpcSum);
    }
    
    @Override
    public String getCognitiveSuffix() {
        return human.getCognitiveSuffix();
    }

    @Override
    public void setParent(EOFMAct parent) {
        activity.setParent(this);
    }

    @Override
    public EOFMAct getParent() {
        return null;
    }

    public void setHumanOperator(EOFMHumanOperator theHumanOperator) {
        human = theHumanOperator;
    }

}
