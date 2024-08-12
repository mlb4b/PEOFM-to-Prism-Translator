package EOFMCtoPRISMwUberError;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mlb4b
 */

public class PRISMWriter {
    
    //PRISM model types
    public final static class ModelType {
        public static final String DTMC = "dtmc";
        public static final String CTMC = "ctmc";
        public static final String MDP = "mdp";
        private ModelType() {}
    }
    
    //PRISM variable qualifiers
    public static final String GLOBAL   = "global";
    public static final String CONSTANT = "const";
    
    //Variable types
    public final static class VariableType {
        public static final String INTEGER = "int";
        public static final String DOUBLE = "double";
        public static final String BOOL = "bool";
        private VariableType() {}
        public final static String getBasicType(String eofmTypeName) {
            if (eofmTypeName.equals("INTEGER")) {
                return INTEGER;
            }
            else if  (eofmTypeName.equals("REAL")) {
                return DOUBLE;
            }
            else {
                return BOOL;
            }
        }
    }
    
    //Boolean values
    public final static class BooleanValues {
        public static final String TRUE = "true";
        public static final String FALSE = "false";
        private BooleanValues() {}
    }
    
    //PRISM operators
    public final static String BOOLEAN_AND = " & ";
    public final static String BOOLEAN_OR  = " | ";
    public final static String BOOLEAN_NOT  = " ! ";
    public final static String BOOLEAN_NEQ  = " != ";
    public final static String BOOLEAN_EQ  = " = ";
    public final static String PLUS = " + ";
    public final static String MINUS = " - ";
    public final static String DIVIDE = " / ";
    public final static String MULTIPLY = " * ";

    //default main module name
    //public static final String MAIN_MODULE = "main";
    
    //default reachability property name
    //public static final String REACH_PROP = "reach";
    
    private ArrayList<String> writtenVariables = new ArrayList<String>();       //keeps track of variable names in a module: the class wont write variable name duplications
    private ArrayList<String> writtenInitializations = new ArrayList<String>();
    private boolean firstTrans = true;
    private boolean firstInit = true;
    private boolean firstProb = true;
    private String prefix = "";
    
    File outFile;
    FileWriter fWriter;
    PrintWriter pWriter;
    
    PRISMWriter() {
    }
    
    PRISMWriter(String fileName) {
        openFile(fileName);
    }
    
    public static String prismExpression(String eofmExpression) {
        return eofmExpression //the regular expressions may not cover every SAL synatx possibility, but should be fine for EOFM
                .replaceAll("( |\\))(AND|and)( |\\()", BOOLEAN_AND)
                .replaceAll("( |\\))(OR|or)( |\\()",  BOOLEAN_OR)
                .replace("/=",    BOOLEAN_NEQ.trim())
                .replaceAll("(^| |=|\\()(NOT|not)($| |\\()", BOOLEAN_NOT.trim())
                .replaceAll("(^| |=|\\(|\\))(TRUE|true)($| |=|\\(|\\))", BooleanValues.TRUE)
                .replaceAll("(^| |=|\\(|\\))(FALSE|false)($| |=|\\(|\\))", BooleanValues.TRUE);
    }
    
    public static String getAnd(String left, String right) {
        return "(" + left + ")" + BOOLEAN_AND + "(" + right + ")";
    }
    
    public static String getOr(String left, String right) {
        return "(" + left + ")" + BOOLEAN_OR + "(" + right + ")";
    }
    
    public static String getEq(String left, String right) {
        return "(" + left + ") = (" + right + ")";
    }
    
    public static String getNeq(String left, String right) {
        return "(" + left + ")" + BOOLEAN_NEQ + "((" + right + ")";
    }
    
    public static String getNot(String val) {
        return BOOLEAN_NOT + "(" + val + ")";
    }
    
    
    public void openFile(String fileName) {
        outFile = new File(fileName);
        try {
            fWriter = new FileWriter(outFile);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(3);
        }
        pWriter = new PrintWriter(fWriter);
    }
    
    public void closeFile() {
        if (pWriter != null) {
            try {
                pWriter.close();
                fWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(PRISMWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void writeBlankLine() {
        pWriter.println("");
    }
    
    public void writeStartOfFile(String modelType) {
        pWriter.println(modelType);
        prefix = "";
    }
    
    /*public void writeEndOfFile(String prefix) {
        pWriter.println("");
    }*/
    
    void writeConstant(String name, String typeName, String construction) {
        pWriter.println(CONSTANT + " " + typeName + " " + name + " = " + construction + ";");
        prefix = "";
    }
    
    /*void writeConstant(String prefix, String name, Object typeName, Object construction) {
        writeConstant(prefix, name, typeName.toString(), construction.toString());
    }*/
    
    public void writeStartOfModule(String name) {
        writtenVariables.clear(); //the class wont write variable name duplications in a given module
        writtenInitializations.clear();
        pWriter.println("module " + name);
        prefix = "\t";
    }
    
    public void writeEndOfModule() {
        pWriter.println("endmodule");
        prefix = "";
    }
    
    /*public void writeModuleComposition(String prefix, String name, String composition) {
        pWriter.println(prefix + name + ": MODULE = " + composition + ";");
    }*/

    /*void writeReachabiltyProp(String prefix, String prop) {
        pWriter.println(prefix + REACH_PROP + " : THEOREM " + MAIN_MODULE + " |- " + prop + ";");
    }*/
    
    public static String getRangeConstruction(int min, int max) {
        return "[" + Integer.toString(min) + ".." + Integer.toString(max) + "]";
    }
    
    public static String getIfElseConstruction(String condition, String thenConstruction, String elseConstruction) { //these can be nested
        return condition + " ? " + thenConstruction +  " : " + elseConstruction;
    }
    
    public void writeGlobalVariableDeclaration(String name, String type, String value) {
        if (isUniqueVariable(name)) { //the class wont write variable name duplications in a given module
            writtenVariables.add(name);
            pWriter.println(GLOBAL + " " + type + " " + name + " = " + value + ";");
            prefix = "";
        }
    }
    public void writeGlobalVariableDeclaration(String name, String type, String value, String initialValue) {
        if (isUniqueVariable(name)) { //the class wont write variable name duplications in a given module
            writtenVariables.add(name);
            pWriter.println(GLOBAL + " " + type + " " + name + " = " + value + ";");
            prefix = "";
        }
    }
    
    public void writeVariableDeclaration(String name, String type) {
        if (isUniqueVariable(name)) { //the class wont write variable name duplications in a given module
            writtenVariables.add(name);
            pWriter.println("\t" + name + " : " + type + ";");
        }
    }
    
    public void writeVariableDeclaration(String name, String type, String initialValue) {
        if (isUniqueVariable(name)) { //the class wont write variable name duplications in a given module
            writtenVariables.add(name);
            pWriter.println("\t" + name + " : " + type + " init " + initialValue + ";");
        }
    }
    
    /*public void writeDefinitionValue(String prefix, String name, String definition) {
        writeInitialValue( prefix, name, definition);
    }
    
    public void writeIfThenElseIFElseDeffinition(String prefix, String variableName, String ifCondition, String ifAssignment, ArrayList<String> elseIfConditions, ArrayList<String> elseIfAssignments, String elseAssignment) {
        pWriter.write(prefix + variableName + " = IF " + ifCondition + " THEN " + ifAssignment);
        if (elseIfConditions != null) {
            for (int i = 0; i < elseIfConditions.size(); i++) {
                pWriter.write(" ELSIF " + elseIfConditions.get(i) + " THEN " + elseIfAssignments.get(i));
            }
        }
        pWriter.println(" ELSE " + elseAssignment + " ENDIF;");
    }
    
    public void writeIfThenElseDeffinition(String prefix, String variableName, String ifCondition, String ifAssignment, String elseAssignment) {
        writeIfThenElseIFElseDeffinition(prefix, variableName, ifCondition, ifAssignment, null, null, elseAssignment);
    }*/
    
    /*public void writeInitialization(String prefix){
        pWriter.println(prefix + "INITIALIZATION");
    }
    
    public void writeInitialValue(String prefix, String name, String initialValue) {
        if (isUniqueInitialization(name)) {
            pWriter.println(prefix + name + " = " + initialValue + ";");
            writtenInitializations.add(name);
        }
    }*/
    
    /*public void writeStartOfTransition(String prefix) {
        pWriter.println(prefix + "TRANSITION");
    }*/
    
    public void writeGuard(String condition, String label) {
        pWriter.println("\t" + "[" + label + "] (" + condition + ") -> ");
        firstTrans = true;
        firstProb = true;
        prefix = "\t\t";
    }
    
    public void writeProbibalisticTransition(double probability) {
        writeProbibalisticTransition(BigDecimal.valueOf(probability).toPlainString());
    }
    
    public void writeProbibalisticTransition(String probability) {
        pWriter.print("\t\t");
        if (firstProb) { 
            pWriter.print("  ");
            firstProb = false;
        }
        else {
            pWriter.print("+ ");
        }
        firstTrans = true;
        pWriter.print(probability + " :\n");
    }
    
    public void writeTransitionAssignment(String varName, String value) {
        pWriter.print("\t\t");
        if (firstTrans) { 
            pWriter.print("  ");
            firstTrans = false;
        }
        else {
            pWriter.print("& ");
        }
        pWriter.print(getTransitionAssignment(varName, value) + "\n");
    }
    
    public void writeEndTransition() {
        pWriter.println("\t;");
        prefix = "\t";
    }
    
    public static String getTransitionAssignment(String varName, String value) {
        return "(" + varName + "' = " + value + ")";
    }
    
    /*public void writeTransitionAssignment(String prefix, String variableName, String assignment) {
        pWriter.println(prefix + variableName + "' = " + assignment + ";");
    }

    public void writeTransitionInAssignment(String prefix, String variableName, String assignment) {
        pWriter.println(prefix + variableName + "' IN " + assignment + ";");
    }
    
    public void writeIfThenElseIFElseTransitionAssignment(String prefix, String variableName, String ifCondition, String ifAssignment, ArrayList<String> elseIfConditions, ArrayList<String> elseIfAssignments, String elseAssignment) {
        writeIfThenElseIFElseDeffinition(prefix, variableName + "'", ifCondition, ifAssignment, elseIfConditions, elseIfAssignments, elseAssignment);
    }*/
    
    /*public void writeIfThenElseTransitionAssignment(String prefix, String variableName, String ifCondition, String ifAssignment, String elseAssignment) {
        writeIfThenElseIFElseDeffinition(prefix, variableName + "'", ifCondition, ifAssignment, null, null, elseAssignment);
    }*/
    
    /*public void writeStartOfGuardedTransitions(String prefix) {
        pWriter.println(prefix + "[");
        first = true;
    }
    
    public void writeEndOfGuardedTransitions(String prefix) {
        pWriter.println(prefix + "];");
    }
    
    /*public void writeGuard(String prefix, String condition) {
        writeGuard(prefix, condition, "");
    }*/
    
    public void writeComment(String comment) {
         pWriter.println(prefix + "// " + comment);
    }
    
    public void writeFormula(String name, String construction) {
         pWriter.println("formula " + name + " = " + construction + ";");
    }
    
    public void writeStartOfInitialization() {
        pWriter.println("init");
        firstInit = true;
        prefix = "\t";
    }
    
    public void writeInitialValue(String name, String construction) {
        pWriter.print("\t");
        if (firstInit) { 
            pWriter.print("  ");
            firstInit = false;
        }
        else {
            pWriter.print("& ");
        }
        pWriter.print(name + " = " + construction + "\n");
    }
    
    public void writeEndOfInitialization() {
        pWriter.println("endinit");
        prefix = "";
    }
    
    public ArrayList<String> getModuleVariables(String prefix) {
        ArrayList<String> returnList = new ArrayList<String>();
        for (String iVariable: writtenVariables) {
            if (iVariable.startsWith(prefix))
                returnList.add(iVariable);
        }
        return returnList;
    }
    
    private boolean isUniqueVariable(String varName) { //the class wont write variable name duplications in a given module
        for(String iString: writtenVariables) {
            if (iString.equals(varName))
                return false;
        }
        return true;
    }
    
    public void write(String content) {
         pWriter.println(content);
    }

}