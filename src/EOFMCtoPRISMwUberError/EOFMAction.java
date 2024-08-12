package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMAction implements EOFMAct {
    private Element             element;
    private EOFMParser          parser;
    private EOFMHumanAction     humanAction;
    private EOFMLocalVariable   localVariable;
    private EOFMHumanComAction  humanComAction;
    private String              assignedValue;
    private String              name;
    private double              cpcSum = NO_CPCSUM;
    private EOFMActivity        parent;
    private String              cognitiveSuffix = "";
    
    public EOFMAction() {
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
    
    public String getName() {
        return name;
    }

    public String getVariableName() {
        if (isHumanAction())
            return humanAction.getName();
        else if (isHumanComAction())
            return humanComAction.getName();
        else
            return localVariable.getName();
    }
    
    public void writeVariableDeclarations(PRISMWriter prismWriter) {
        prismWriter.writeVariableDeclaration(name, ACT_TYPE, ACT_READY);
    }
    
    public void writeTransitions(PRISMWriter prismWriter, ArrayList<EOFMAct> peers, String readyVariableName, String submittedVariableName) {
        final String NO_OBSERVATION_ERROR = "(" + EOFMElement.EOFM_OBSERVATION_VAR(getCognitiveSuffix()) + PRISMWriter.BOOLEAN_EQ + EOFM_NOERROR_NAME + ")";
        final String OBSERVATION_ERROR    = "(" + EOFMElement.EOFM_OBSERVATION_VAR(getCognitiveSuffix()) + PRISMWriter.BOOLEAN_EQ + EOFM_ERROR_NAME   + ")";
        final String NO_EXECUTION_ERROR   = "(" + EOFMElement.EOFM_EXECUTION_VAR(getCognitiveSuffix())   + PRISMWriter.BOOLEAN_EQ + EOFM_NOERROR_NAME + ")";
        final String EXECUTION_ERROR      = "(" + EOFMElement.EOFM_EXECUTION_VAR(getCognitiveSuffix())   + PRISMWriter.BOOLEAN_EQ + EOFM_ERROR_NAME   + ")";
        
        //write start condition
        String startCondition =  parent.getName() + PRISMWriter.BOOLEAN_EQ + ACT_EXECUTING;
        int peerIndex = 0;
        if (peers.size() > 1) {
            if (!parent.getDecomposition().equals(DECOMP_ORDERED)) {
                if (parent.getDecomposition().contains(DECOMP_XOR)) {
                    for (EOFMAct iPeer: peers) {
                        startCondition  += PRISMWriter.BOOLEAN_AND + iPeer.getName() + PRISMWriter.BOOLEAN_EQ + ACT_READY;
                    }
                }
                else if (!parent.getDecomposition().contains(DECOMP_PAR)) {
                    for (EOFMAct iPeer: peers) {
                        startCondition += PRISMWriter.BOOLEAN_AND + iPeer.getName() + PRISMWriter.BOOLEAN_NEQ + ACT_EXECUTING;
                    }
                }
            }
            else {
                for (EOFMAct iPeer: peers) {
                    if (name.equals(iPeer.getName()))
                        peerIndex = peers.indexOf(iPeer);
                }
                if (peerIndex > 0)
                    startCondition  += PRISMWriter.BOOLEAN_AND + peers.get(peerIndex - 1).getName() + PRISMWriter.BOOLEAN_EQ + ACT_DONE;
            }
        }
        startCondition = "(" + startCondition + ")";
        
        prismWriter.writeBlankLine();

        //ready -> executing (or done if it is a local variable action)
        prismWriter.writeComment("Normative transitions for " + name);
        if (isHumanAction()) {
            prismWriter.writeComment(name + ": Normative Ready->Executing");
            prismWriter.writeGuard(NO_EXECUTION_ERROR + PRISMWriter.BOOLEAN_AND + getReady() + PRISMWriter.BOOLEAN_AND  + startCondition + PRISMWriter.BOOLEAN_AND + readyVariableName, EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
            prismWriter.writeTransitionAssignment(humanAction.getName(), assignedValue);
            prismWriter.writeTransitionAssignment(submittedVariableName, PRISMWriter.BooleanValues.TRUE);
            prismWriter.writeEndTransition();
        }
        else {
            prismWriter.writeComment(name + ": Normative Ready->Done (with presumed execution)");
            prismWriter.writeGuard(NO_OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + getReady() + PRISMWriter.BOOLEAN_AND + startCondition, EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_DONE);
            prismWriter.writeTransitionAssignment(localVariable.getName(), assignedValue);
            prismWriter.writeEndTransition();
        }
        
        prismWriter.writeBlankLine();
        
        //write the erroneous transitions
        prismWriter.writeComment("Erroneous transitions for " + name);
        
        if (isHumanAction()) {
            // action out of sequence / Ready->Executing or Done->Executing without startcondition / intruding the action when the start condition is violated
            prismWriter.writeComment(name + ": Erroenous Ready->Executing or Done->Executing sequence error / intrusion");
            prismWriter.writeGuard(EXECUTION_ERROR 
                    + PRISMWriter.BOOLEAN_AND 
                    + "(" 
                        + "(" + getReady() + PRISMWriter.BOOLEAN_AND + PRISMWriter.BOOLEAN_NOT + startCondition + ")" 
                        + PRISMWriter.BOOLEAN_OR 
                        + getDone()
                    + ")"
                    + PRISMWriter.BOOLEAN_AND 
                    + readyVariableName
                    , EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
            prismWriter.writeTransitionAssignment(humanAction.getName(), assignedValue);
            prismWriter.writeTransitionAssignment(submittedVariableName, PRISMWriter.BooleanValues.TRUE);
            prismWriter.writeEndTransition();
            
            //action of wrong type / Ready->Executing / doing the a wrong action in place of the right one 
            for(EOFMElement eAction: parser.getElementsOfType(EOFMAction.class)) {
                if (((EOFMAction)eAction).isHumanAction() && eAction != this) {
                    prismWriter.writeComment(name + ": Erroenous Ready->Executing or Done->Executing doing the a wrong action in place of the right one");
                    prismWriter.writeGuard(EXECUTION_ERROR + PRISMWriter.BOOLEAN_AND + getReady() + PRISMWriter.BOOLEAN_AND  + startCondition + PRISMWriter.BOOLEAN_AND + readyVariableName, EOFM_TASK_TRANS);
                    prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
                    prismWriter.writeTransitionAssignment(((EOFMAction)eAction).getVariableName(), ((EOFMAction)eAction).getAssignedValue());
                    prismWriter.writeTransitionAssignment(submittedVariableName, PRISMWriter.BooleanValues.TRUE);
                    prismWriter.writeEndTransition();
                }
            }

            //Ready->Done : all omissions
            //String readyStart = "(" + getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")";
            prismWriter.writeComment(name + ": Erroenous Ready->Done Omission");
            prismWriter.writeGuard(EXECUTION_ERROR + PRISMWriter.BOOLEAN_AND + getReady() + PRISMWriter.BOOLEAN_AND + startCondition + PRISMWriter.BOOLEAN_AND + readyVariableName, EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_DONE);
            prismWriter.writeEndTransition();
            
            //Ready->Ready
            prismWriter.writeComment(name + ": Erroenous Ready->Ready Delay");
            prismWriter.writeGuard(EXECUTION_ERROR + PRISMWriter.BOOLEAN_AND + getReady() + PRISMWriter.BOOLEAN_AND + startCondition + PRISMWriter.BOOLEAN_AND + readyVariableName, EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_READY);
            prismWriter.writeTransitionAssignment(submittedVariableName, PRISMWriter.BooleanValues.TRUE);
            prismWriter.writeEndTransition();
            
            //Done->Ready allows the actions to be reset "normatively" for activities that are done erroneously when the parent is ready
            prismWriter.writeComment(name + ": Done->Ready  allows the actions to be reset normatively for activities that are done erroneously when the parent is ready");
            prismWriter.writeGuard(getDone() + PRISMWriter.BOOLEAN_AND + "(" + parent.getName() + " = " + ACT_READY + ")", EOFM_TASK_TRANS);
            writeRecursiveAssignment(prismWriter, ACT_READY);
            prismWriter.writeEndTransition();
            
        }
        if (isSetValueAction()) {
            //wrong value /  Action on Wrong Object 
            for(String val: humanAction.getTypeValues()) {
                if (!val.equals(assignedValue)) {
                    //setvalue
                    prismWriter.writeComment(name + ": Wrong Value / Action on Wrong Object with value " + val);
                    prismWriter.writeGuard(EXECUTION_ERROR + PRISMWriter.BOOLEAN_AND
                            + "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                            + PRISMWriter.BOOLEAN_AND + readyVariableName
                            , EOFM_TASK_TRANS);
                    prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
                    prismWriter.writeTransitionAssignment(humanAction.getName(), val);
                    prismWriter.writeTransitionAssignment(submittedVariableName, PRISMWriter.BooleanValues.TRUE);
                    prismWriter.writeEndTransition();
                }
            }
            //wrong target /  Action of Wrong Type 
            for(EOFMElement hAction: parser.getElementsOfType(EOFMHumanAction.class)) {
                if (humanAction.hasSameType((EOFMHumanAction)hAction) && hAction != humanAction) {
                    prismWriter.writeComment(name + ": Wrong Target / Action of Wrong Type  with " + ((EOFMHumanAction)hAction).getName() + " as target");
                    prismWriter.writeGuard(EXECUTION_ERROR + PRISMWriter.BOOLEAN_AND
                            + "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                            + PRISMWriter.BOOLEAN_AND + readyVariableName
                            , EOFM_TASK_TRANS);
                    prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
                    prismWriter.writeTransitionAssignment(((EOFMHumanAction)hAction).getName(), assignedValue);
                    prismWriter.writeTransitionAssignment(submittedVariableName, PRISMWriter.BooleanValues.TRUE);
                    prismWriter.writeEndTransition();
                }
            }
        }
            
        if (isLocalVariableAssignment()) {
            
            //observation not made
            prismWriter.writeComment(name + ": Observation Not Made");
            prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + 
                    "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                    , EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_DONE);
            prismWriter.writeEndTransition();
            
            //observation delay
            prismWriter.writeComment(name + ": Observation Delay");
            prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + 
                    "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                    , EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_READY);
            prismWriter.writeEndTransition();
            
            //observation intrusion from ready
            prismWriter.writeComment(name + ": Observation Intrusion (observation at wrong time) from Ready");
            prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + 
                    "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                    , EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_DONE);
            prismWriter.writeTransitionAssignment(localVariable.getName(), assignedValue);
            prismWriter.writeEndTransition();
            
            //observation intrusion from done
            prismWriter.writeComment(name + ": Observation Intrusion (observation at wrong time) from Done");
            prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + getDone(), EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_DONE);
            prismWriter.writeTransitionAssignment(localVariable.getName(), assignedValue);
            prismWriter.writeEndTransition();
            
            //wrong observation 
            for(EOFMElement eAction: parser.getElementsOfType(EOFMAction.class)) {
                if (((EOFMAction)eAction).isLocalVariableAssignment() && eAction != this) {
                    prismWriter.writeComment(name + ": Wrong Observation of " + ((EOFMAction)eAction).getName());
                    prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + 
                        "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                        , EOFM_TASK_TRANS);
                    prismWriter.writeTransitionAssignment(name, ACT_DONE);
                    prismWriter.writeTransitionAssignment(((EOFMAction)eAction).getVariableName(), ((EOFMAction)eAction).getAssignedValue());
                    prismWriter.writeEndTransition();
                }
            }
            
            //wrong value /   Wrong Object Observed 
            for(String val: localVariable.getTypeValues()) {
                if (!val.equals(assignedValue)) {
                    //setvalue
                    prismWriter.writeComment(name + ": Wrong Value / Action on Wrong Object with value " + val);
                    prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + "(" + assignedValue + PRISMWriter.BOOLEAN_NEQ + val + ")" + PRISMWriter.BOOLEAN_AND + 
                            "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                            , EOFM_TASK_TRANS);
                    prismWriter.writeTransitionAssignment(name, ACT_DONE);
                    prismWriter.writeTransitionAssignment(localVariable.getName(), val);
                    prismWriter.writeEndTransition();
                }
            }
            
            //wrong target /   Wrong Identification 
            for(EOFMElement lVar: parser.getElementsOfType(EOFMLocalVariable.class)) {
                if (localVariable.hasSameType((EOFMLocalVariable)lVar)) {
                    //prismWriter.writeComment(name + ": Wrong Value / Action on Wrong Object with value " + val);
                    prismWriter.writeComment(name + ": Wrong Target / Action of Wrong Type  with " + ((EOFMLocalVariable)lVar).getName() + " as target");
                    prismWriter.writeGuard(OBSERVATION_ERROR + PRISMWriter.BOOLEAN_AND + 
                            "(" +  getReady() + PRISMWriter.BOOLEAN_AND + startCondition + ")"
                            , EOFM_TASK_TRANS);
                    prismWriter.writeTransitionAssignment(name, ACT_DONE);
                    prismWriter.writeTransitionAssignment(((EOFMLocalVariable)lVar).getName(), assignedValue);
                    prismWriter.writeEndTransition();
                }
            }
            
        }
        
    }
    
    public void writeInitialValues(PRISMWriter prismWriter) {
        prismWriter.writeInitialValue(name, ACT_READY);
    }
    
    public void writeRecursiveAssignment(PRISMWriter prismWriter, String assignment) {
        prismWriter.writeTransitionAssignment(name, assignment);
    }

    public EOFMAction getCopyWithNewName(String nameSuffix) {
        EOFMAction returnAction = new EOFMAction();
        returnAction.humanAction  = humanAction;
        returnAction.humanComAction  = humanComAction;
        returnAction.localVariable = localVariable;
        returnAction.name = name + nameSuffix;
        returnAction.assignedValue = assignedValue;
        return returnAction;
    }

    public String getAssignedValue() {
        return assignedValue;
    }

    public String getDefaultValue() {
        if (isHumanAction())
            return PRISMWriter.BooleanValues.FALSE;
        else
            return localVariable.getInitialValue();
    }

    public boolean isHumanAction () {
        return humanAction != null;
    }

    public boolean isHumanComAction () {
        return humanComAction != null;
    }

    public boolean isLocalVariableAssignment () {
        return localVariable != null;
    }
    
    public boolean isSetValueAction() {
        return isHumanAction() && humanAction.getBehavior().equals(HUMANACTIONBEHAVIOR_SETVALUE);
    }
    
    public boolean isAutoResetAction() {
        return isHumanAction() && humanAction.getBehavior().equals(HUMANACTIONBEHAVIOR_AUTORESET);
    }
    
    public boolean isToggleAction() {
        return isHumanAction() && humanAction.getBehavior().equals(HUMANACTIONBEHAVIOR_TOGGLE);
    }

    private void extractElementData() {
        humanAction = (EOFMHumanAction)parser.getEOFMElement(parser.extractAttributeData(element, EOFM_HUMANACTION));
        localVariable = (EOFMLocalVariable)parser.getEOFMElement(parser.extractAttributeData(element, EOFM_LOCALVARIABLE));
        humanComAction = (EOFMHumanComAction)parser.getEOFMElement(parser.extractAttributeData(element, EOFM_HUMANCOMACTION));
        
        try {
            cpcSum = Integer.parseInt(parser.extractAttributeData(element, EOFM_CPC_SUM));
        }
        catch (NumberFormatException e) {
            cpcSum = NO_CPCSUM;
        }
        
        /*
        if (isHumanAction())
            return humanAction.getName();
        else if (isHumanComAction())
            return humanComAction.getName();
        else
            return localVariable.getName();
        */

        if (isHumanAction()) {
            name = humanAction.getName() + parser.getUniqueSuffix();
            if (humanAction.getBehavior().equals(HUMANACTIONBEHAVIOR_SETVALUE)) {
                if (!isLocalVariableAssignment())
                    assignedValue = PRISMWriter.prismExpression(parser.extractElementData(element));
                else
                    assignedValue  = humanAction.getLocalVariableName();
                //parser.addVariable(name,  localVariable.GetPrismTypeConstruction());
            }
            else if (humanAction.getBehavior().equals(HUMANACTIONBEHAVIOR_AUTORESET)) {
                assignedValue = PRISMWriter.BooleanValues.TRUE;
                //parser.addBooleanVariable(name);
            }
            else {
                assignedValue = PRISMWriter.BOOLEAN_NOT + "" + humanAction.getName();
                //parser.addBooleanVariable(name);
            }
            parser.addVariable(name,  ACT_READY_VAL, ACT_DONE_VAL);
            
        }
        else if (isHumanComAction()) {
            name = humanComAction.getName() + parser.getUniqueSuffix();
            if (!isLocalVariableAssignment())
                assignedValue = PRISMWriter.prismExpression(parser.extractElementData(element));
            else
                assignedValue  = humanAction.getLocalVariableName();
        }
        else {
            name = localVariable.getName() + parser.getUniqueSuffix();
            assignedValue = PRISMWriter.prismExpression(parser.extractElementData(element));
        }
        parser.registerEOFMElement(name, this);
    }

    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter) {
        prismWriter.writeTransitionAssignment(name, prismWriter.getIfElseConstruction(getExecuting(), ACT_DONE, name));
    }

    public EOFMAct getChild(int index) {
        return null;
    }

    public String getTypeName() {
        if (isHumanAction())
            return humanAction.getTypeName();
        else if (isHumanComAction())
            return humanComAction.getTypeName();
        else
            return localVariable.getTypeName();
    }
    
    @Override
    public String getRecursiveNotExecuting() {
        return name + PRISMWriter.BOOLEAN_NEQ + ACT_EXECUTING;
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_ACTION);
        returnElement.setAttribute(EOFM_NAME, name);
        if (humanAction != null) {
            returnElement.setAttribute(EOFM_HUMANACTION, humanAction.getName());
            returnElement.setTextContent(assignedValue);
        }
        else if (localVariable != null) {
            returnElement.setAttribute(EOFM_LOCALVARIABLE, localVariable.getName());
            returnElement.setTextContent(assignedValue);
        }
        else if (humanComAction != null) {
            returnElement.setAttribute(EOFM_HUMANCOMACTION, humanComAction.getName());
            returnElement.setTextContent(assignedValue);
        }
        return returnElement;
    }
    
    public String getReady() {
        return "(" + name + PRISMWriter.BOOLEAN_EQ + ACT_READY + ")";
    }
    
    public String getExecuting() {
        return "(" + name + PRISMWriter.BOOLEAN_EQ + ACT_EXECUTING + ")";
    }
    
    public String getDone() {
        return "(" + name + PRISMWriter.BOOLEAN_EQ + ACT_DONE + ")";
    }

    @Override
    public void setParent(EOFMAct parent) {
        this.parent = (EOFMActivity)parent;
    }

    @Override
    public EOFMAct getParent() {
        return parent;
    }

    @Override
    public void writeCognitiveFunctions(PRISMWriter prismWriter) {
        if (cpcSum != NO_CPCSUM && cpcSum != parent.getCPCSum()) {
            EOFMs.writeCognitiveFunctions(prismWriter, cpcSum, getCognitiveSuffix());
        }
    }

    @Override
    public double getCPCSum() {
        return cpcSum;
    }
    
    @Override
    public double getCPCSumUp() {
        if (cpcSum == NO_CPCSUM) {
            return parent.getCPCSum();
        }
        else {
            return cpcSum;
        }
    }
    
    @Override
    public double getCPCSumDown() {
        return getCPCSum();
    }

    @Override
    public void setCPCSum(double cpcSum) {
        this.cpcSum = cpcSum;
    }

    @Override
    public String getCognitiveSuffix() {
        if (cognitiveSuffix.isEmpty()) {
            if (cpcSum == NO_CPCSUM) {
                cognitiveSuffix =  parent.getCognitiveSuffix();
            }
            else {
                cognitiveSuffix = name;
            }
        }
        return cognitiveSuffix;
    }
}
