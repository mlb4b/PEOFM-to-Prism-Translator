package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class EOFMs implements EOFMElement {

    private Element element;
    private EOFMParser parser;
    private ArrayList constants;
    private ArrayList userDefinedTypes;
    private ArrayList humanOperators;

    private double cpcSum;
    
    public EOFMs() {
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
    
    public void writeConstants(PRISMWriter prismWriter) {
        for (Object iConstants: constants) {
            ((EOFMConstant)iConstants).writeConstant(prismWriter);
        }
    }
    
    public void writeTypes(PRISMWriter prismWriter, Boolean writeActivityStateType) {
        if (writeActivityStateType) {
            prismWriter.writeConstant(ACT_READY,     PRISMWriter.VariableType.INTEGER, ACT_READY_VAL);
            prismWriter.writeConstant(ACT_EXECUTING, PRISMWriter.VariableType.INTEGER, ACT_EXECUTING_VAL);
            prismWriter.writeConstant(ACT_DONE,      PRISMWriter.VariableType.INTEGER, ACT_DONE_VAL);
        }
        for (Object iType : userDefinedTypes) {
            ((EOFMUserDefinedType)iType).writeType(prismWriter);
        }
    }
    
    public void writeConstantsAndTypes(PRISMWriter prismWriter, Boolean writeActivityStateType) {
        prismWriter.writeComment("Initial State and No Error");
        prismWriter.writeConstant(EOFM_INIT_NAME, PRISMWriter.VariableType.INTEGER, EOFM_INIT_VAL);
        prismWriter.writeConstant(EOFM_NOERROR_NAME, PRISMWriter.VariableType.INTEGER, EOFM_NOERROR_VAL);
        prismWriter.writeConstant(EOFM_ERROR_NAME, PRISMWriter.VariableType.INTEGER, EOFM_ERROR_VAL);
        prismWriter.writeBlankLine();
        
        prismWriter.writeComment("Constants from EOFM");
        writeConstants(prismWriter);
        prismWriter.writeBlankLine();
        
        prismWriter.writeComment("Constants for EOFM User Defined Types");
        writeTypes(prismWriter, writeActivityStateType);
        prismWriter.writeBlankLine();
    }

    void writeModulesAndInitializations(PRISMWriter prismWriter) {
        //double p;
        
        prismWriter.writeFormula(EOFM_TERMINATION_CONDITION_NAME, EOFM_TERMINATION_CONDITION);
        for (Object iOperator : humanOperators) {
            ((EOFMHumanOperator)iOperator).writeCognitiveFunctions(prismWriter);
        }

        prismWriter.writeStartOfModule("humanOperators");        
        //variable declariation and initialization
        prismWriter.writeVariableDeclaration(EOFM_SUBMITTED_VAR_NAME, PRISMWriter.VariableType.BOOL, PRISMWriter.BooleanValues.FALSE);
        for (Object iOperator : humanOperators) {
            ((EOFMHumanOperator)iOperator).writeVariables(prismWriter);
        }

        //activity and action transitions
        for (Object iOperator : humanOperators) {
            ((EOFMHumanOperator)iOperator).writeTransitions(prismWriter, null, null, EOFM_READY_VAR_NAME, EOFM_SUBMITTED_VAR_NAME);
        }
        
        //else transiton to prevent deadlock due to 
        prismWriter.writeGuard(PRISMWriter.BooleanValues.TRUE, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(EOFM_SUBMITTED_VAR_NAME, EOFM_SUBMITTED_VAR_NAME);
        prismWriter.writeEndTransition();

        //handshake transition: if submitted and not ready -> set ready to false and reset inputs
        prismWriter.writeGuard(EOFM_SUBMITTED_VAR_NAME + " " + prismWriter.BOOLEAN_AND + " " + prismWriter.BOOLEAN_NOT + " " + EOFM_READY_VAR_NAME, EOFM_HANDSHAKE);
        prismWriter.writeTransitionAssignment(EOFM_SUBMITTED_VAR_NAME, PRISMWriter.BooleanValues.FALSE);
        for (Object iOperator : humanOperators) {
            ((EOFMHumanOperator)iOperator).writePassThroughActionDoneAssignments(prismWriter);
        }
        for (Object iOperator : humanOperators) {
            ((EOFMHumanOperator)iOperator).writeActionAutoReset(prismWriter);
        }
        prismWriter.writeEndTransition();
        
        

        //end the module
        //prismWriter.writeEndOfGuardedTransitions(prefix + "\t");
        prismWriter.writeEndOfModule();
        prismWriter.writeBlankLine();

        //create basic system module
        prismWriter.writeStartOfModule("sys");
        
        prismWriter.writeBlankLine();
        prismWriter.writeVariableDeclaration(EOFM_READY_VAR_NAME, PRISMWriter.VariableType.BOOL, PRISMWriter.BooleanValues.TRUE);
        //writeInputVariables
        for (Object iOperator : humanOperators) {
            ((EOFMHumanOperator)iOperator).writeInputVariables(prismWriter);
        }
        prismWriter.writeGuard(EOFM_SUBMITTED_VAR_NAME + " " + prismWriter.BOOLEAN_AND + " " + prismWriter.BOOLEAN_NOT + " " + EOFM_READY_VAR_NAME, EOFM_HANDSHAKE);
        //EOFM_Handshake
        prismWriter.writeTransitionAssignment(EOFM_READY_VAR_NAME, PRISMWriter.BooleanValues.TRUE);
        prismWriter.writeEndTransition();
        
        prismWriter.writeGuard(EOFM_READY_VAR_NAME + PRISMWriter.BOOLEAN_AND + EOFM_SUBMITTED_VAR_NAME, EOFM_PROCESS_ACTION);
        prismWriter.writeTransitionAssignment(EOFM_READY_VAR_NAME, PRISMWriter.BooleanValues.FALSE);
        prismWriter.writeEndTransition();

        prismWriter.writeEndOfModule();
        
        prismWriter.writeBlankLine();
    }

    public static void writeCognitiveFunctions(PRISMWriter prismWriter, double cpcSum, String suffix) {
        prismWriter.writeComment("Cognitive Function Modules");
        
        prismWriter.writeConstant(EOFMElement.EOFM_CPC_SUM_NAME(suffix), PRISMWriter.VariableType.DOUBLE, Double.toString(cpcSum));
        
        prismWriter.writeBlankLine();
        
        prismWriter.writeConstant(EOFMElement.EOFM_P_OBS_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_OBS_ERROR_FORMULA(suffix));
        prismWriter.writeConstant(EOFMElement.EOFM_P_OBS_NO_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_OBS_NO_ERROR_FORMULA(suffix));
        prismWriter.writeStartOfModule("observationFunction_" + suffix);
        prismWriter.writeVariableDeclaration(EOFMElement.EOFM_OBSERVATION_VAR(suffix), EOFM_FUNCTION_TYPE, EOFM_INIT_NAME);
        prismWriter.writeGuard(PRISMWriter.BOOLEAN_NOT + EOFM_TERMINATION_CONDITION_NAME, EOFM_TASK_TRANS);
        //p = EOFMElement.getProbabilityOfError(COCOM.OBS, cpcSum);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_OBS_NO_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_OBSERVATION_VAR(suffix), EOFM_NOERROR_NAME);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_OBS_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_OBSERVATION_VAR(suffix), EOFM_ERROR_NAME);
        prismWriter.writeEndTransition();
        prismWriter.writeEndOfModule();
        prismWriter.writeBlankLine();
        
        prismWriter.writeConstant(EOFMElement.EOFM_P_INT_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_INT_ERROR_FORMULA(suffix));
        prismWriter.writeConstant(EOFMElement.EOFM_P_INT_NO_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_INT_NO_ERROR_FORMULA(suffix));
        prismWriter.writeStartOfModule("interpretationFunction_" + suffix);
        prismWriter.writeVariableDeclaration(EOFMElement.EOFM_INTERPRETATION_VAR(suffix), EOFM_FUNCTION_TYPE, EOFM_INIT_NAME);
        prismWriter.writeGuard(PRISMWriter.BOOLEAN_NOT + EOFM_TERMINATION_CONDITION_NAME, EOFM_TASK_TRANS);
        //p = EOFMElement.getProbabilityOfError(COCOM.INT, cpcSum);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_INT_NO_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_INTERPRETATION_VAR(suffix), EOFM_NOERROR_NAME);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_INT_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_INTERPRETATION_VAR(suffix), EOFM_ERROR_NAME);
        prismWriter.writeEndTransition();
        prismWriter.writeEndOfModule();
        prismWriter.writeBlankLine();
        
        prismWriter.writeConstant(EOFMElement.EOFM_P_PLAN_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_PLAN_ERROR_FORMULA(suffix));
        prismWriter.writeConstant(EOFMElement.EOFM_P_PLAN_NO_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_PLAN_NO_ERROR_FORMULA(suffix));
        prismWriter.writeStartOfModule("planningFunction_" + suffix);
        prismWriter.writeVariableDeclaration(EOFMElement.EOFM_PLANNING_VAR(suffix), EOFM_FUNCTION_TYPE, EOFM_INIT_NAME);
        prismWriter.writeGuard(PRISMWriter.BOOLEAN_NOT + EOFM_TERMINATION_CONDITION_NAME, EOFM_TASK_TRANS);
        //p = EOFMElement.getProbabilityOfError(COCOM.PLAN, cpcSum);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_PLAN_NO_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_PLANNING_VAR(suffix), EOFM_NOERROR_NAME);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_PLAN_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_PLANNING_VAR(suffix), EOFM_ERROR_NAME);
        prismWriter.writeEndTransition();
        prismWriter.writeEndOfModule();
        prismWriter.writeBlankLine();
        
        prismWriter.writeConstant(EOFMElement.EOFM_P_EXE_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_EXE_ERROR_FORMULA(suffix));
        prismWriter.writeConstant(EOFMElement.EOFM_P_EXE_NO_ERROR_NAME(suffix), PRISMWriter.VariableType.DOUBLE, EOFMElement.EOFM_P_EXE_NO_ERROR_FORMULA(suffix));
        prismWriter.writeStartOfModule("executionFunction_" + suffix);
        prismWriter.writeVariableDeclaration(EOFMElement.EOFM_EXECUTION_VAR(suffix), EOFM_FUNCTION_TYPE, EOFM_INIT_NAME);
        prismWriter.writeGuard(PRISMWriter.BOOLEAN_NOT + EOFM_TERMINATION_CONDITION_NAME, EOFM_TASK_TRANS);
        //p = EOFMElement.getProbabilityOfError(COCOM.EXE, cpcSum);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_EXE_NO_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_EXECUTION_VAR(suffix), EOFM_NOERROR_NAME);
        prismWriter.writeProbibalisticTransition(EOFMElement.EOFM_P_EXE_ERROR_NAME(suffix));
            prismWriter.writeTransitionAssignment(EOFMElement.EOFM_EXECUTION_VAR(suffix), EOFM_ERROR_NAME);
        prismWriter.writeEndTransition();
        prismWriter.writeEndOfModule();
        prismWriter.writeBlankLine();
    }
    
    private void extractElementData() {
        /*try {
            cpcSum = Integer.parseInt(parser.extractChildElementData(element, EOFM_CPC_SUM));
        }
        catch (NumberFormatException e) {
            cpcSum = -10;
        }*/
        constants = parser.extractMultipleElements(EOFM_CONSTANT, element, EOFMConstant.class);
        userDefinedTypes = parser.extractMultipleElements(EOFM_USERDEFINEDTYPE, element, EOFMUserDefinedType.class);
        humanOperators = parser.extractMultipleElements(EOFM_HUMANOPERATOR, element, EOFMHumanOperator.class);
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_EOFMS);
        for (Object iConstants: constants) {
            returnElement.appendChild(((EOFMConstant)iConstants).BuildXMLDoc(doc));
        }
        for (Object iType : userDefinedTypes) {
            returnElement.appendChild(((EOFMUserDefinedType)iType).BuildXMLDoc(doc));
        }
        for (Object iHumanOperator: humanOperators) {
            returnElement.appendChild(((EOFMHumanOperator)iHumanOperator).BuildXMLDoc(doc));
        }
        doc.appendChild(returnElement);
        return returnElement;
    }
}
