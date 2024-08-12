/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author mlb4b
 */
public class EOFMSharedEOFM implements EOFMAct{
    private Element element;
    private EOFMParser parser;
    private EOFMActivity activity;
    private ArrayList<String> associates;
    private double cpcSum;

    public String getName() {
        return activity.getName();
    }

    public EOFMAct getChild(int index) {
        return (EOFMAct) activity.getChild(index);
    }

    public EOFMAct getCopyWithNewName(String nameSuffix) {
        EOFMSharedEOFM returnEOFM = new EOFMSharedEOFM();
        returnEOFM.activity = activity.getCopyWithNewName(nameSuffix);
        return (EOFMAct) returnEOFM;
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

    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter) {
        activity.writePassThroughActionDoneAssignments(prismWriter);
    }

    public void writeVariables(PRISMWriter prismWriter) {
        prismWriter.writeComment("Variables for Shared " + activity.getName());
        writeVariableDeclarations(prismWriter);
        prismWriter.writeBlankLine();
        /*prismWriter.writeInitialization(prefix + "\t");
        writeInitialValues(prefix + "\t\t", prismWriter, ACT_READY);
        prismWriter.writeBlankLine();*/
    }
    
    public String getRecursiveNotExecuting() {
        return activity.getRecursiveNotExecuting();
    }

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
        associates = parser.extractMultipleElementAttributes(EOFM_ASSOCIATE, element, EOFM_HUMANOPERATOR);
        activity = (EOFMActivity)parser.extractSingleElement(EOFM_ACTIVITY, element, EOFMActivity.class);
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_SHAREDEOFM);
        /*for (String iAssociate: associates) {
            Element associate = doc.createElement(EOFM_ASSOCIATE);
            associate.setAttribute(EOFM_HUMANOPERATOR, iAssociate);
            returnElement.appendChild(associate);
        }*/
        returnElement.appendChild(activity.BuildXMLDoc(doc));
        return returnElement;
    }
    
    @Override
    public void writeCognitiveFunctions(PRISMWriter prismWriter) {
        /*
        public static int DEFAULT_CPCSUM = -10;
        public static int NO_CPCSUM = -99;
        */
        double activityCPCSum = activity.getCPCSum();
        if (activityCPCSum == NO_CPCSUM) {
            activity.setCPCSum(cpcSum);
        }
        else if (activityCPCSum != cpcSum) {
            setCPCSum(activityCPCSum);
        }
        //EOFMs.writeCognitiveFunctions(prismWriter, cpcSum, activity.getName());
        activity.writeCognitiveFunctions(prismWriter);
    }

    @Override
    public double getCPCSum() {
        return cpcSum;
    }
    
    @Override
    public void setCPCSum(double cpcSum) {
        this.cpcSum = cpcSum;
        //activity.setCPCSum(cpcSum);
    }
    
    @Override
    public String getCognitiveSuffix() {
        return activity.getCognitiveSuffix();
    }

    @Override
    public void setParent(EOFMAct parent) {
        activity.setParent(this);
    }

    @Override
    public EOFMAct getParent() {
        return null;
    }

    @Override
    public double getCPCSumUp() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public double getCPCSumDown() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
