package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMHumanOperator implements EOFMElement {
    private String name;
    private ArrayList inputVariables;
    private ArrayList inputVariableLinks;
    private ArrayList humanActions;
    private ArrayList humanComActions;
    private ArrayList localVariables;
    private ArrayList eofms;
    //private String readyVariableName;
    //private String submittedVariableName;

    private Element element;
    private EOFMParser parser;
    
    private double cpcSum = NO_CPCSUM; 

    public EOFMHumanOperator() {
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
    
    public void writeInputVariables(PRISMWriter prismWriter) {
        for (Object iInputVariable: inputVariables) {
            ((EOFMInputVariable)iInputVariable).writeVariableDeclaration(prismWriter);
        }
    }

    public void writeVariables(PRISMWriter prismWriter) {
        prismWriter.writeComment("Variables for " + name);
        //input variables
        /*for (Object iInputVariable: inputVariables) {
            ((EOFMInputVariable)iInputVariable).writeVariableDeclaration(prismWriter);
        }
        for (Object iInputVariableLink: inputVariableLinks) {
            ((EOFMInputVariableLink)iInputVariableLink).writeVariableDeclaration(prismWriter);
        }*/
        //prismWriter.writeVariableDeclaration(SALWriter.INPUT_VARIABLE, readyVariableName, SALWriter.BOOLEAN_TYPE);
        //prismWriter.writeBlankLine();

        //output variables
        for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeVariableDeclaration(prismWriter);
        }

        //prismWriter.writeVariableDeclaration(SALWriter.OUTPUT_VARIABLE, submittedVariableName, SALWriter.BOOLEAN_TYPE);
        //prismWriter.writeBlankLine();

        //local variables
        for (Object iHumanComAction: humanComActions) {
            ((EOFMHumanComAction)iHumanComAction).writeVariableDeclaration(prismWriter);
        }
        for (Object iLocalVariable: localVariables) {
            ((EOFMLocalVariable)iLocalVariable).writeVariableDeclaration(prismWriter);
        }
        for(Object iEOFM: eofms) {
            ((EOFM)iEOFM).writeVariableDeclarations(prismWriter);
        }

        prismWriter.writeBlankLine();

        //initialization of output and local variables
        //prismWriter.writeInitialization(prefix + "\t");
        //todo: move this
        /*for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeInitialValue(prismWriter);
        }
        for (Object iHumanComAction: humanComActions) {
            ((EOFMHumanComAction)iHumanComAction).writeInitialValue(prismWriter);
        }
        //prismWriter.writeInitialValue(submittedVariableName, SALWriter.BOOLEAN_FALSE);
        for (Object iLocalVariable: localVariables) {
            ((EOFMLocalVariable)iLocalVariable).writeInitialValue(prismWriter);
        }*/
        /*for(Object iEOFM: eofms) {
            ((EOFMAct)iEOFM).writeInitialValues(prismWriter, ACT_READY);
        }
        prismWriter.writeBlankLine();*/
    }
    
    public void writeInitialValues(PRISMWriter prismWriter) {
        // PRISMWriter.writeInitialization(prefix + "\t");
        for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeInitialValue(prismWriter);
        }
        for (Object iLocalVariable: localVariables) {
            ((EOFMLocalVariable)iLocalVariable).writeInitialValue(prismWriter);
        }
        for(Object iEOFM: eofms) {
            ((EOFMAct)iEOFM).writeInitialValues(prismWriter);
        }
    }

    public void writeTransitions(PRISMWriter prismWriter, EOFMActivity parent, ArrayList<EOFMAct> peers, String readyVariableName, String submittedVariableName) {
        for (Object iEOFM: eofms) {
            ((EOFMAct)iEOFM).writeTransitions(prismWriter, eofms, readyVariableName, submittedVariableName);
        }
    }

    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter) {
        for (Object iEOFM: eofms) {
             ((EOFMAct)iEOFM).writePassThroughActionDoneAssignments(prismWriter);
        }
    }

    //todo: deal with first
    public void writeActionAutoReset(PRISMWriter prismWriter) {
        for(Object iHumanAction: humanActions) {
            if (((EOFMHumanAction)iHumanAction).getBehavior().equals(HUMANACTIONBEHAVIOR_AUTORESET))
                prismWriter.writeTransitionAssignment(((EOFMHumanAction)iHumanAction).getName(), PRISMWriter.BooleanValues.FALSE);
        }
    }

    /*public void writeActionsAsInputs(PRISMWriter prismWriter) {
        for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeVariableDeclaration(prismWriter, PRISMWriter.INPUT_VARIABLE);
        }
    }*/

    /*public void writeNormativeSalModel(SALWriter prismWriter) {
        prismWriter.writeStartOfModule(name);

        //input variables
        for (Object iInputVariable: inputVariables) {
            ((EOFMInputVariable)iInputVariable).writeVariableDeclaration(prismWriter, SALWriter.INPUT_VARIABLE);
        }
        for (Object iInputVariableLink: inputVariableLinks) {
            ((EOFMInputVariableLink)iInputVariableLink).writeVariableDeclaration(prismWriter, SALWriter.INPUT_VARIABLE);
        }
        prismWriter.writeVariableDeclaration(SALWriter.INPUT_VARIABLE, readyVariableName, SALWriter.BOOLEAN_TYPE);
        prismWriter.writeBlankLine();

        //output variables
        for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeVariableDeclaration(prismWriter, SALWriter.OUTPUT_VARIABLE);
        }
        prismWriter.writeVariableDeclaration(SALWriter.OUTPUT_VARIABLE, submittedVariableName, SALWriter.BOOLEAN_TYPE);
        prismWriter.writeBlankLine();

        //local variables
        for (Object iLocalVariable: localVariables) {
            ((EOFMLocalVariable)iLocalVariable).writeVariableDeclaration(prismWriter, SALWriter.LOCAL_VARIABLE);
        }
        for(Object iEOFM: eofms) {
            ((EOFM)iEOFM).writeVariableDeclarations(prismWriter, SALWriter.LOCAL_VARIABLE);
        }
        prismWriter.writeBlankLine();

        //initialization of output and local variables
        prismWriter.writeInitialization(prefix + "\t");
        for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeInitialValue(prismWriter);
        }
        prismWriter.writeInitialValue(submittedVariableName, SALWriter.BOOLEAN_FALSE);
        for (Object iLocalVariable: localVariables) {
            ((EOFMLocalVariable)iLocalVariable).writeInitialValue(prismWriter);
        }
        for(Object iEOFM: eofms) {
            ((EOFMAct)iEOFM).writeInitialValues(prismWriter, ACT_READY);
        }
        prismWriter.writeBlankLine();

        //transitions
        prismWriter.writeStartOfTransition(prefix + "\t");
        prismWriter.writeStartOfGuardedTransitions(prefix + "\t");

        //write eofm/activity transitions
        for (Object iEOFM: eofms) {
            ((EOFMAct)iEOFM).writeTransitions(prismWriter, null, eofms, readyVariableName, submittedVariableName);
        }

        //write handshake transition: if submitted and not ready -> set ready to false and reset inputs
        prismWriter.writeGuard(submittedVariableName + PRISMWriter.BOOLEAN_AND + PRISMWriter.BOOLEAN_NOT + "" + readyVariableName);
        prismWriter.writeTransitionAssignment(prefix + "\t\t\t", submittedVariableName, SALWriter.BOOLEAN_FALSE);
        for(Object iEOFM: eofms) {
            ((EOFMAct)iEOFM).writePassThroughActionDoneAssignments(prefix + "\t\t\t", prismWriter);
        }
        for(Object iHumanAction: humanActions) {
            if (((EOFMHumanAction)iHumanAction).getBehavior().equals(HUMANACTIONBEHAVIOR_AUTORESET))
                prismWriter.writeTransitionAssignment(prefix + "\t\t\t", ((EOFMHumanAction)iHumanAction).getName(), SALWriter.BOOLEAN_FALSE);
        }

        //end the module
        prismWriter.writeEndOfGuardedTransitions(prefix + "\t");
        prismWriter.writeEndOfModule(prefix);
        prismWriter.writeBlankLine();
    }*/

    /*public void writeInterfaceModule(SALWriter prismWriter) {
        //start of the module
        prismWriter.writeStartOfModule(name + "Interface");

        //input variables
        for (Object iHumanAction: humanActions) {
            ((EOFMHumanAction)iHumanAction).writeVariableDeclaration(prismWriter, SALWriter.INPUT_VARIABLE);
        }
        prismWriter.writeVariableDeclaration(SALWriter.INPUT_VARIABLE, submittedVariableName, SALWriter.BOOLEAN_TYPE);
        prismWriter.writeBlankLine();

        //output variables
        for (Object iInputVariable: inputVariables) {
            ((EOFMInputVariable)iInputVariable).writeVariableDeclaration(prismWriter, SALWriter.OUTPUT_VARIABLE);
        }
        prismWriter.writeVariableDeclaration(SALWriter.OUTPUT_VARIABLE, readyVariableName, SALWriter.BOOLEAN_TYPE);
        prismWriter.writeBlankLine();
        prismWriter.writeInitialization(prefix + "\t");
        prismWriter.writeInitialValue(readyVariableName, SALWriter.BOOLEAN_TRUE);

        prismWriter.writeStartOfTransition(prefix + "\t");
        prismWriter.writeStartOfGuardedTransitions(prefix + "\t");
        prismWriter.writeGuard(PRISMWriter.BOOLEAN_NOT + "(" + readyVariableName + PRISMWriter.BOOLEAN_OR + submittedVariableName + ")");
        prismWriter.writeTransitionAssignment(prefix + "\t\t\t", readyVariableName, SALWriter.BOOLEAN_TRUE);

        prismWriter.writeGuard(readyVariableName + PRISMWriter.BOOLEAN_AND + submittedVariableName);
        prismWriter.writeTransitionAssignment(prefix + "\t\t\t", readyVariableName, SALWriter.BOOLEAN_FALSE);
        prismWriter.writeEndOfGuardedTransitions(prefix + "\t");

        prismWriter.writeEndOfModule(prefix); 
    }*/

    /*public void writeMainModule(SALWriter prismWriter) {
        prismWriter.writeModuleComposition(SALWriter.MAIN_MODULE, name + SALWriter.ASYNCHRONOUS_COMP + name + "Interface");
        prismWriter.writeReachabiltyProp("G(NOT(" + ((EOFMAct)eofms.get(0)).getChild(0).getName() + " = " + ACT_EXECUTING + PRISMWriter.BOOLEAN_AND + ((EOFMAct)eofms.get(0)).getName() + " != " + ACT_EXECUTING + "))");
    }*/

    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(name, this);
        
        try {
            cpcSum = Integer.parseInt(parser.extractAttributeData(element, EOFM_CPC_SUM));
        }
        catch (NumberFormatException e) {
            cpcSum = NO_CPCSUM;
        }
        
        inputVariables = parser.extractMultipleElements(EOFM_INPUTVARIABLE, element, EOFMInputVariable.class);
        inputVariableLinks = parser.extractMultipleElements(EOFM_INPUTVARIABLELINK, element, EOFMInputVariableLink.class);
        localVariables = parser.extractMultipleElements(EOFM_LOCALVARIABLE, element, EOFMLocalVariable.class);
        humanActions = parser.extractMultipleElements(EOFM_HUMANACTION, element, EOFMHumanAction.class);
        humanComActions = parser.extractMultipleElements(EOFM_HUMANCOMACTION, element, EOFMHumanComAction.class);
        eofms = parser.extractMultipleElements(EOFM_EOFM, element, EOFM.class);
        for(Object iEOFM: eofms) {
            ((EOFM)iEOFM).setHumanOperator(this);
        }
        //readyVariableName = name + "_Ready";
        //submittedVariableName = name + "_Submitted";
    }
    
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_HUMANOPERATOR);
        returnElement.setAttribute(EOFM_NAME, name);
        for (Object iInputVariable: inputVariables) {
            returnElement.appendChild(((EOFMInputVariable)iInputVariable).BuildXMLDoc(doc));
        }
        for (Object iInputVariableLink: inputVariableLinks) {
            returnElement.appendChild(((EOFMInputVariableLink)iInputVariableLink).BuildXMLDoc(doc));
        }
        for (Object iLocalVariable: localVariables) {
             returnElement.appendChild(((EOFMLocalVariable)iLocalVariable).BuildXMLDoc(doc));
        }
        for (Object iHumanAction: humanActions) {
             returnElement.appendChild(((EOFMHumanAction)iHumanAction).BuildXMLDoc(doc));
        }
        for (Object iHumanComAction: humanComActions) {
             returnElement.appendChild(((EOFMHumanComAction)iHumanComAction).BuildXMLDoc(doc));
        }
        for(Object iEOFM: eofms) {
            returnElement.appendChild(((EOFMAct)iEOFM).BuildXMLDoc(doc));
        }
        return returnElement;
    }
    
    public double getCPCSum() {
        return cpcSum;
    }
    
    public void writeCognitiveFunctions(PRISMWriter prismWriter) {
        boolean subsBlank = true;
        for(Object iEOFM: eofms) {
            subsBlank = subsBlank && (((EOFMAct)iEOFM).getCPCSumDown() == NO_CPCSUM);
            ((EOFMAct)iEOFM).writeCognitiveFunctions(prismWriter);
        }
        if (subsBlank) {
            EOFMs.writeCognitiveFunctions(prismWriter, cpcSum, name);
        }
        
    }

    String getCognitiveSuffix() {
        return name;
    }
}