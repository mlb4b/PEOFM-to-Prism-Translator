package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMActivity implements EOFMAct {
    private Element element;
    private EOFMParser parser;

    private String name;
    private String preCondition;
    private String completionCondition;
    private String repeatCondition;
    private Element decompostionElement;
    private String decompositionOperator;
    private ArrayList<EOFMAct> acts;
    
    private static Hashtable<String, Class> tagsToClasses = new Hashtable<String, Class>();
    
    private double cpcSum = NO_CPCSUM;
    
    private EOFMAct parent;
    private String replacedby;
    private String  cognitiveSuffix = "";

    public EOFMActivity() {
        tagsToClasses.put(EOFM_ACTIVITY, EOFMActivity.class);
        tagsToClasses.put(EOFM_ACTIVITYLINK, EOFMActivityLink.class);
        tagsToClasses.put(EOFM_ACTION, EOFMAction.class);
    }

    public EOFMActivity getCopyWithNewName(String nameSuffix) {
        EOFMActivity returnActivity = new EOFMActivity();
        returnActivity.name = name + nameSuffix;
        returnActivity.preCondition = preCondition;
        returnActivity.completionCondition = completionCondition;
        returnActivity.repeatCondition = repeatCondition;
        returnActivity.decompositionOperator = decompositionOperator;
        returnActivity.acts = new ArrayList();

        for (EOFMAct iChild: acts) {
            returnActivity.acts.add(iChild.getCopyWithNewName(nameSuffix));
        }
        
        return returnActivity;
    }

    public String getName() {
        return name;
    }

    public String getDecomposition() {
        return decompositionOperator;
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

    public void writeVariableDeclarations(PRISMWriter prismWriter) {
        prismWriter.writeVariableDeclaration(name, ACT_TYPE, ACT_READY);
        for(EOFMAct iAct: acts) {
            iAct.writeVariableDeclarations(prismWriter);
        }
    }

    public void writeInitialValues(PRISMWriter prismWriter) {
        prismWriter.writeInitialValue(name, ACT_READY);
        for(EOFMAct iAct: acts) {
            iAct.writeInitialValues(prismWriter);
        }
    }

    public void writeTransitions(PRISMWriter prismWriter, ArrayList<EOFMAct> peers, String readyVariableName, String submittedVariableName) {
        // NEW TODO: if has replace by, treat ready->exeucting transition differently
        // get cpc sum from replacement so that you can children can use it and you can modify for your interprepretation error
        final String NO_PLANNING_ERROR       = "(" + EOFMElement.EOFM_PLANNING_VAR(getCognitiveSuffix())       + PRISMWriter.BOOLEAN_EQ  + EOFM_NOERROR_NAME       + ")";
        final String PLANNING_ERROR          = "(" + EOFMElement.EOFM_PLANNING_VAR(getCognitiveSuffix())       + PRISMWriter.BOOLEAN_EQ  + EOFM_ERROR_NAME         + ")";
        final String NO_INTERPRETATION_ERROR = "(" + EOFMElement.EOFM_INTERPRETATION_VAR(getCognitiveSuffix()) + PRISMWriter.BOOLEAN_EQ  + EOFM_NOERROR_NAME       + ")";
        final String INTERPRETATION_ERROR    = "(" + EOFMElement.EOFM_INTERPRETATION_VAR(getCognitiveSuffix()) + PRISMWriter.BOOLEAN_EQ  + EOFM_ERROR_NAME         + ")";
        final String NO_ACTIVITY_ERROR       = "(" + NO_PLANNING_ERROR                                         + PRISMWriter.BOOLEAN_AND + NO_INTERPRETATION_ERROR + ")";
        
        //write start condition
        String startCondition = getReady();
        int peerIndex = 0;
        String parentDecomposition = "";
        if (parent instanceof EOFMActivity) {
            startCondition += PRISMWriter.BOOLEAN_AND + parent.getName() + PRISMWriter.BOOLEAN_EQ + ACT_EXECUTING;
            parentDecomposition = ((EOFMActivity)parent).getDecomposition();
        }
        if (peers != null && peers.size() > 1) {
            if (!parentDecomposition.equals(DECOMP_ORDERED)) {
                if (parentDecomposition.contains(DECOMP_XOR)) {
                    for (EOFMAct iPeer : peers) {
                        startCondition += PRISMWriter.BOOLEAN_AND + iPeer.getName() + PRISMWriter.BOOLEAN_EQ + ACT_READY;
                    }
                }
                else if (!parentDecomposition.contains(DECOMP_PAR)) {
                    for (EOFMAct iPeer : peers) {
                        startCondition += PRISMWriter.BOOLEAN_AND + iPeer.getName() + PRISMWriter.BOOLEAN_NEQ + ACT_EXECUTING;
                    }
                } 
            }
            else {
                for (EOFMAct iPeer : peers) {
                    if (name.equals(iPeer.getName())) {
                        peerIndex = peers.indexOf(iPeer);
                    }
                }
                if (peerIndex > 0) {
                    startCondition += PRISMWriter.BOOLEAN_AND + peers.get(peerIndex - 1).getName() + PRISMWriter.BOOLEAN_EQ + ACT_DONE;
                }
            }
        }
        startCondition = "(" + startCondition + ")";

        //write end condition
        String endCondition = getExecuting();
        if (decompositionOperator.contains(DECOMP_AND) 
                || decompositionOperator.contains(DECOMP_ORDERED)) {
            for (EOFMAct iAct : acts) {
                endCondition += PRISMWriter.BOOLEAN_AND + iAct.getName() + PRISMWriter.BOOLEAN_EQ + ACT_DONE;
            }
        } else {
            for (EOFMAct iAct : acts) {
                endCondition += PRISMWriter.BOOLEAN_AND + iAct.getName() + PRISMWriter.BOOLEAN_NEQ + ACT_EXECUTING;
            }
            if (decompositionOperator.equals(DECOMP_OR_SEQ) || decompositionOperator.equals(DECOMP_OR_PAR) || decompositionOperator.equals(DECOMP_XOR)) {
                endCondition += PRISMWriter.BOOLEAN_AND + "(";
                for (EOFMAct iAct : acts) {
                    if (acts.indexOf(iAct) != 0) {
                        endCondition += PRISMWriter.BOOLEAN_OR;
                    }
                    endCondition += iAct.getName() + PRISMWriter.BOOLEAN_EQ + ACT_DONE;
                }
                endCondition += ")";
            }
        }
        endCondition = "(" + endCondition + ")";
        
        //write the transitions
        prismWriter.writeBlankLine();
        prismWriter.writeComment("Normative transitions for " + name);
        String errorCondition; //variable for tracking error conditions
        
        // Ready -> Executing
        String readyToExecuting = startCondition;
        if (!preCondition.isEmpty()) {
            readyToExecuting += " " + PRISMWriter.BOOLEAN_AND + " (" + preCondition + ")";
        }
        if (!completionCondition.isEmpty()) {
            readyToExecuting += PRISMWriter.BOOLEAN_AND + PRISMWriter.BOOLEAN_NOT + " (" + completionCondition + ")";
        }
        readyToExecuting += " " + PRISMWriter.BOOLEAN_AND + " (" + getRecursiveNotExecuting() + ")";
        
        if (!preCondition.isEmpty() || !completionCondition.isEmpty()) {
            errorCondition = NO_ACTIVITY_ERROR;
        }
        else {
            errorCondition = NO_PLANNING_ERROR;
        }
        prismWriter.writeComment(name + ": Normative Ready -> Executing");
        prismWriter.writeGuard(errorCondition + PRISMWriter.BOOLEAN_AND + "(" + readyToExecuting + ")", EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
        prismWriter.writeEndTransition();
        
        //Executing -> Done
        String executingToDone = endCondition;
        if (!completionCondition.isEmpty()) {
            executingToDone += " " + PRISMWriter.BOOLEAN_AND + "(" + completionCondition + ")";
        }
        else if (!repeatCondition.isEmpty()) {
            executingToDone += " " + PRISMWriter.BOOLEAN_AND + " " + PRISMWriter.BOOLEAN_NOT + " (" + repeatCondition + ")";
        }
        /*String executingToDoneFunctionFailures = "(" + EOFM_Planning_Var + PRISMWriter.BOOLEAN_EQ + EOFM_NoError_Name + ")";
        if (!completionCondition.isEmpty() || !repeatCondition.isEmpty()) {
            executingToDoneFunctionFailures += PRISMWriter.BOOLEAN_AND + "(" + EOFM_Interpretation_Var + PRISMWriter.BOOLEAN_EQ + EOFM_NoError_Name + ")";
        }*/
        if (!completionCondition.isEmpty() || !repeatCondition.isEmpty()) {
            errorCondition = NO_ACTIVITY_ERROR;
        }
        else {
            errorCondition = NO_PLANNING_ERROR;
        }
        prismWriter.writeComment(name + ": Normative Executing -> Done");
        prismWriter.writeGuard(errorCondition + PRISMWriter.BOOLEAN_AND + "(" + executingToDone + ")", EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_DONE);
        prismWriter.writeEndTransition();
        
        //Ready -> Done
        String readyToDone = startCondition;
        if(parent instanceof EOFMActivity && !completionCondition.isEmpty()) {
            readyToDone += " " + PRISMWriter.BOOLEAN_AND + "(" + completionCondition +")";
            prismWriter.writeComment(name + ": Normative Executing -> Executing with Reset");
            prismWriter.writeGuard(NO_ACTIVITY_ERROR + PRISMWriter.BOOLEAN_AND + "(" + readyToDone + ")", EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_DONE);
            prismWriter.writeEndTransition();
        }
        
        //Executing -> Executing
        if (!repeatCondition.isEmpty()) {
            String executingToExecuting = endCondition + " " + PRISMWriter.BOOLEAN_AND + " (" + repeatCondition + ")";
            if (!completionCondition.isEmpty()) {
                executingToExecuting += " " + PRISMWriter.BOOLEAN_AND + " " + PRISMWriter.BOOLEAN_NOT + " (" + completionCondition + ")";
            }
            prismWriter.writeComment(name + ":  Normative Executing -> Executing with Reset");
            prismWriter.writeGuard(NO_ACTIVITY_ERROR + PRISMWriter.BOOLEAN_AND + "(" +executingToExecuting + ")", EOFM_TASK_TRANS);
            prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
            for (EOFMAct iChild : acts) {
                iChild.writeRecursiveAssignment(prismWriter, ACT_READY);
            }
            prismWriter.writeEndTransition();
        }
        
        //Done -> Ready (roots only)
        if (parent instanceof EOFM) {
            String doneToReady = getDone();
            prismWriter.writeComment(name + ":  Normative Done->Ready");
            prismWriter.writeGuard(NO_PLANNING_ERROR + PRISMWriter.BOOLEAN_AND + "(" + doneToReady + ")", EOFM_TASK_TRANS);
            writeRecursiveAssignment(prismWriter, ACT_READY);
            prismWriter.writeEndTransition();
        }
        
        ////////////////////////////////////////////////////////////////////////
        //write the erroneous transitions
        ////////////////////////////////////////////////////////////////////////
        
        startCondition = startCondition.replaceFirst(Pattern.quote(getReady()     + PRISMWriter.BOOLEAN_AND), "");
        endCondition   = endCondition  .replaceFirst(Pattern.quote(getExecuting() + PRISMWriter.BOOLEAN_AND), "");

        prismWriter.writeBlankLine();
        prismWriter.writeComment("Erroneous transitions for " + name);

        //Ready->Done
        String readyToDoneError = getReady(); // = getReady();
        if (completionCondition.isEmpty()) {
            readyToDoneError += PRISMWriter.BOOLEAN_AND + PLANNING_ERROR;
        }
        else {
            readyToDoneError += PRISMWriter.BOOLEAN_AND + "(" + PLANNING_ERROR + PRISMWriter.BOOLEAN_OR + INTERPRETATION_ERROR + ")";
        }
        if (!startCondition.isEmpty()) {
            readyToDoneError += PRISMWriter.BOOLEAN_AND + startCondition;
        }
        if (!completionCondition.isEmpty()) {
            readyToDoneError += PRISMWriter.BOOLEAN_AND + PRISMWriter.BOOLEAN_NOT + "(" + completionCondition + ")";
        }
        prismWriter.writeComment(name + ": Erroneous Ready->Done Omission");
        prismWriter.writeGuard(readyToDoneError, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_DONE);
        prismWriter.writeEndTransition();

        //Ready->Ready
        String readyToReadyError = getReady();
        if (preCondition.isEmpty() && completionCondition.isEmpty()) {
            readyToReadyError += PRISMWriter.BOOLEAN_AND + PLANNING_ERROR ;
        }
        else {
            readyToReadyError += PRISMWriter.BOOLEAN_AND + "(" + PLANNING_ERROR + PRISMWriter.BOOLEAN_OR + INTERPRETATION_ERROR + ")";
        }

        if (!startCondition.isEmpty()) {
            readyToReadyError += PRISMWriter.BOOLEAN_AND +  startCondition;
        }
        if (!preCondition.isEmpty()) {
            readyToReadyError += PRISMWriter.BOOLEAN_AND + "(" + preCondition + ")";
        }
        if (!completionCondition.isEmpty()) {
            readyToReadyError += PRISMWriter.BOOLEAN_AND + PRISMWriter.BOOLEAN_NOT + "(" + completionCondition + ")";
        }
        prismWriter.writeComment(name + ": Erroenous Ready->Ready Delay");
        prismWriter.writeGuard(readyToReadyError, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_READY);
        prismWriter.writeEndTransition();

        //Ready->Executing
        String readyToExecutingError1 = "";
        if (!startCondition.isEmpty() || !preCondition.isEmpty() || !completionCondition.isEmpty()) {        
            readyToExecutingError1 = PLANNING_ERROR + PRISMWriter.BOOLEAN_AND + "(";
            if (!startCondition.isEmpty()) {
                readyToExecutingError1 += PRISMWriter.BOOLEAN_NOT + startCondition;
            }
            if (!preCondition.isEmpty()) {
                readyToExecutingError1 += PRISMWriter.BOOLEAN_OR + PRISMWriter.BOOLEAN_NOT + "(" + preCondition + ")";
            }
            if (!completionCondition.isEmpty()) {
                readyToExecutingError1 += PRISMWriter.BOOLEAN_OR + "(" + completionCondition + ")";
            }
            readyToExecutingError1 += ")";
            readyToExecutingError1 = readyToExecutingError1.replace("(" + PRISMWriter.BOOLEAN_OR + " ", "(")
                                                           .replace(PRISMWriter.BOOLEAN_AND + "()", "");
            readyToExecutingError1 = "(" + readyToExecutingError1 + ")";
        }
        String readyToExecutingError2 = "";
        if (!preCondition.isEmpty() || !completionCondition.isEmpty()) {
            readyToExecutingError2 = INTERPRETATION_ERROR + PRISMWriter.BOOLEAN_AND + "(";
            if (!preCondition.isEmpty()) {
                readyToExecutingError2 += PRISMWriter.BOOLEAN_NOT + "(" + preCondition + ")";
            }
            if (!completionCondition.isEmpty()) {
                if (!preCondition.isEmpty()) {
                    readyToExecutingError2 += PRISMWriter.BOOLEAN_OR;
                }
                readyToExecutingError2 += "(" + completionCondition + ")";
            }
            readyToExecutingError2 += ")";
            readyToExecutingError2 = readyToExecutingError2.replace("(" + PRISMWriter.BOOLEAN_OR + " ", "(")
                                                           .replace(PRISMWriter.BOOLEAN_AND + "()", "");
            readyToExecutingError2 = "(" + readyToExecutingError2 + ")";
        }
        String theGaurd = getReady();
        if (!readyToExecutingError1.isEmpty() || !readyToExecutingError2.isEmpty()) {
            theGaurd += PRISMWriter.BOOLEAN_AND + "(";
            if (readyToExecutingError1.isEmpty()) {
                theGaurd += readyToExecutingError2;
            }
            else if (readyToExecutingError2.isEmpty()) {
                theGaurd += readyToExecutingError1;
            }
            else {
                theGaurd += readyToExecutingError1 + PRISMWriter.BOOLEAN_OR + readyToExecutingError2;
            }
            theGaurd += ")";
        }
        prismWriter.writeComment(name + ": Erroneous Ready->Exeucting Intrusion");
        prismWriter.writeGuard(theGaurd, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
        prismWriter.writeEndTransition();

        //Executing->Executing
        String executingToExecutingError = getExecuting() + PRISMWriter.BOOLEAN_AND + "((" + PLANNING_ERROR;
        if (!preCondition.isEmpty() || !completionCondition.isEmpty() || !repeatCondition.isEmpty()) {
            executingToExecutingError += PRISMWriter.BOOLEAN_OR + INTERPRETATION_ERROR;
        }
        executingToExecutingError += ")";
        if (!endCondition.isEmpty()) {
            executingToExecutingError += PRISMWriter.BOOLEAN_AND + "(" + endCondition + ")";
        }
        if (!repeatCondition.isEmpty() && !completionCondition.isEmpty()) {
            executingToExecutingError += PRISMWriter.BOOLEAN_AND + "((" + completionCondition + ")" + PRISMWriter.BOOLEAN_OR +  "(" + repeatCondition + "))";
        }
        else if (!completionCondition.isEmpty()) {
            executingToExecutingError += PRISMWriter.BOOLEAN_AND + "(" + completionCondition + ")";
        }
        else if (!repeatCondition.isEmpty()) {
            executingToExecutingError += PRISMWriter.BOOLEAN_AND + "(" + repeatCondition + ")";
        }
        executingToExecutingError += ")";
        prismWriter.writeComment(name + ": Erroneous Executing->Executing Delay");
        prismWriter.writeGuard(executingToExecutingError, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
        prismWriter.writeEndTransition();

        //Executing->Executing w/ reset
        String executToExecutingResetError1 = PLANNING_ERROR + PRISMWriter.BOOLEAN_AND + "(";
        if (!endCondition.isEmpty()) {
            executToExecutingResetError1 +=  PRISMWriter.BOOLEAN_NOT + "(" + endCondition + ")";
        }
        if (!repeatCondition.isEmpty()) {
            executToExecutingResetError1 += PRISMWriter.BOOLEAN_OR + PRISMWriter.BOOLEAN_NOT + "(" + repeatCondition + ")";
        }
        if (!completionCondition.isEmpty()) {
            executToExecutingResetError1 += PRISMWriter.BOOLEAN_OR + "(" + completionCondition + ")";
        }
        executToExecutingResetError1 += ")";
        executToExecutingResetError1 = executToExecutingResetError1.replace("(" + PRISMWriter.BOOLEAN_OR + "", "(")
                                                                   .replace(PRISMWriter.BOOLEAN_AND + "()", "");
        String executToExecutingResetError2 = INTERPRETATION_ERROR + PRISMWriter.BOOLEAN_AND + "(";
        if (!repeatCondition.isEmpty()) {
            executToExecutingResetError2 += PRISMWriter.BOOLEAN_OR + PRISMWriter.BOOLEAN_NOT + "(" + repeatCondition + ")";
        }
        if (!completionCondition.isEmpty()) {
            executToExecutingResetError2 += PRISMWriter.BOOLEAN_OR + "(" + completionCondition + ")";
        }
        executToExecutingResetError2 += ")";
        executToExecutingResetError2 = executToExecutingResetError2.replace("(" + PRISMWriter.BOOLEAN_OR + "", "(")
                                                                   .replace(PRISMWriter.BOOLEAN_AND + "()", "");
        theGaurd = getExecuting();
        if (!executToExecutingResetError1.isEmpty() || !executToExecutingResetError2.isEmpty()) {
            theGaurd += PRISMWriter.BOOLEAN_AND + "(";
            if (executToExecutingResetError1.isEmpty()) {
                theGaurd += executToExecutingResetError2;
            }
            else if (executToExecutingResetError2.isEmpty()) {
                theGaurd += executToExecutingResetError1;
            }
            else {
                theGaurd += executToExecutingResetError1 + PRISMWriter.BOOLEAN_OR + executToExecutingResetError2;
            }
            theGaurd += ")";
        }
        prismWriter.writeComment(name + ": Erroneous Executing->Executing with Reset (Restart)");
        prismWriter.writeGuard(theGaurd, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
        for (EOFMAct iChild : acts) {
            iChild.writeRecursiveAssignment(prismWriter, ACT_READY);
        }
        prismWriter.writeEndTransition();

        //Executing->Done
        String executToDonetError = getExecuting();
        if (!endCondition.isEmpty() || !completionCondition.isEmpty()) {
            executToDonetError += PRISMWriter.BOOLEAN_AND + "(" + PLANNING_ERROR + PRISMWriter.BOOLEAN_AND + "(";
            if (!endCondition.isEmpty()) {
                executToDonetError += PRISMWriter.BOOLEAN_NOT + endCondition;
            }
            if (!completionCondition.isEmpty()) {
                executToDonetError += PRISMWriter.BOOLEAN_OR + PRISMWriter.BOOLEAN_NOT + "(" + completionCondition + ")";
            }
            executToDonetError += ")";
        }
        if (!completionCondition.isEmpty()) {
            executToDonetError += PRISMWriter.BOOLEAN_OR + "(" + INTERPRETATION_ERROR + PRISMWriter.BOOLEAN_AND + PRISMWriter.BOOLEAN_NOT + "(" + completionCondition + "))";
        }
        executToDonetError += ")";
        prismWriter.writeComment(name + ": Erroneous Executing->Done Omission");
        prismWriter.writeGuard(executToDonetError, EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_DONE);
        prismWriter.writeEndTransition();

        //Done-->Executing
        prismWriter.writeComment(name + ": Erroneous Done->Executing Intrusion");
        prismWriter.writeGuard(getDone() + PRISMWriter.BOOLEAN_AND + "(" + PLANNING_ERROR + ")", EOFM_TASK_TRANS);
        prismWriter.writeTransitionAssignment(name, ACT_EXECUTING);
        for (EOFMAct iChild : acts) {
            iChild.writeRecursiveAssignment(prismWriter, ACT_READY);
        }
        prismWriter.writeEndTransition();

        //Done-->Ready allows the actions to be reset "normatively" for activities that are done erroneously when the parent is ready
        if (parent instanceof EOFMActivity) {
            prismWriter.writeComment(name + ": allows the actions to be reset normatively for activities that are done erroneously when the parent is ready");
            prismWriter.writeGuard(getDone() + PRISMWriter.BOOLEAN_AND + "(" + parent.getName() + PRISMWriter.BOOLEAN_EQ + ACT_READY + ")", EOFM_TASK_TRANS);
            writeRecursiveAssignment(prismWriter, ACT_READY);
            prismWriter.writeEndTransition();
        }

        for (EOFMAct iAct : acts) {
            iAct.writeTransitions(prismWriter, acts, readyVariableName, submittedVariableName);
        }
    }
    
    public void writeRecursiveAssignment(PRISMWriter prismWriter, String assignment) {
        prismWriter.writeTransitionAssignment(name, assignment);
        for(EOFMAct iAct: acts) {
            iAct.writeRecursiveAssignment(prismWriter, assignment);
        }
    }

    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter) {
        for (EOFMAct iAct: acts) {
            iAct.writePassThroughActionDoneAssignments(prismWriter);
        }
    }
    
    /*private boolean getErrors() {
        return     readytodone 
                || readytoready  
                || readytoexecuting 
                || executingtoexecuting 
                || executingtoexecutingreset   
                || executingtodone  
                || donetoexecuting 
                || all;
    }*/
    
    @Override
    public String getRecursiveNotExecuting() {
        String notExecuting = name + PRISMWriter.BOOLEAN_NEQ + ACT_EXECUTING;
        for (EOFMAct iAct : acts) {
            notExecuting += PRISMWriter.BOOLEAN_AND + iAct.getRecursiveNotExecuting();
        }
        return notExecuting;
    }

    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(name, this);
        parser.addVariable(name,  ACT_READY, ACT_DONE);
        try {
            cpcSum = Integer.parseInt(parser.extractAttributeData(element, EOFM_CPC_SUM));
        }
        catch (NumberFormatException e) {
            cpcSum = NO_CPCSUM;
        }
        
        preCondition = PRISMWriter.prismExpression(parser.extractChildElementData(element, EOFM_PRECONDITION));
        completionCondition = PRISMWriter.prismExpression(parser.extractChildElementData(element, EOFM_COMPLETIONCONDITION));
        repeatCondition = PRISMWriter.prismExpression(parser.extractChildElementData(element, EOFM_REPEATCONDITION));
        decompostionElement = parser.getXMLChildXMLElment(element, EOFM_DECOMPOSITION);
        decompositionOperator = parser.extractAttributeData(decompostionElement, EOFM_OPERATOR);
        acts = parser.extractMultipleElementTypes(tagsToClasses, decompostionElement);
    }

    public EOFMAct getChild(int index) {
        return acts.get(index);
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_ACTIVITY);
        returnElement.setAttribute(EOFM_NAME, name);
        if (!preCondition.isEmpty()) {
            Element preConditionElement = doc.createElement(EOFM_PRECONDITION);
            preConditionElement.setTextContent(preCondition);
            returnElement.appendChild(preConditionElement);
        }
        if (!repeatCondition.isEmpty()) {
            Element repeatConditionElement = doc.createElement(EOFM_REPEATCONDITION);
            repeatConditionElement.setTextContent(repeatCondition);
            returnElement.appendChild(repeatConditionElement);
        }
        if (!completionCondition.isEmpty()) {
            Element completionConditionElement = doc.createElement(EOFM_COMPLETIONCONDITION);
            completionConditionElement.setTextContent(completionCondition);
            returnElement.appendChild(completionConditionElement);
        }
        Element decompElement = doc.createElement(EOFM_DECOMPOSITION);
        decompElement.setAttribute(EOFM_OPERATOR, decompositionOperator);
        for (EOFMAct iAct: acts) {
            decompElement.appendChild(iAct.BuildXMLDoc(doc));
        }
        returnElement.appendChild(decompElement);
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
    
    public String getIfNotBlank(String theOperator, String theString) {
        return (theString == null || theString.isEmpty()) ? "" : theOperator + "(" + theString +")";
    }   

    @Override
    public void writeCognitiveFunctions(PRISMWriter prismWriter) {
        /*if ((parent instanceof EOFM) || (cpcSum != NO_CPCSUM && cpcSum != parent.getCPCSum())) {
            EOFMs.writeCognitiveFunctions(prismWriter, cpcSum, getCognitiveSuffix());
        }
        for (EOFMAct iChild: acts) {
            iChild.writeCognitiveFunctions(prismWriter);
        }*/
        if (cpcSum != NO_CPCSUM) {
            EOFMs.writeCognitiveFunctions(prismWriter, cpcSum, getCognitiveSuffix());
        }
        for (EOFMAct iChild: acts) {
            iChild.writeCognitiveFunctions(prismWriter);
        }
        // NEW TODO: if has replace by, then create special interpretation function with similarity scaling
    }

    @Override
    public double getCPCSum() {
        /*if (cpcSum == NO_CPCSUM) {
            return parent.getCPCSum();
        }
        else {
            return cpcSum;
        }*/
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
        if (cpcSum == NO_CPCSUM) {
            return parent.getCPCSum();
        }
        else {
            return cpcSum;
        }
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

    @Override
    public void setParent(EOFMAct parent) {
        this.parent = parent;
        for (EOFMAct iChild: acts) {
            iChild.setParent(this);
        }
    }
    
    public void setReplacedBy(String replacedby) {
        this.replacedby = replacedby;
    }

    @Override
    public EOFMAct getParent() {
        return parent;
    }
}