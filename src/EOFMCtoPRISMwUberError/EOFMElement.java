package EOFMCtoPRISMwUberError;

import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface EOFMElement {
    //EOFM element and attribute names
    public final static String EOFM_EOFMS               = "eofms";
    public final static String EOFM_CPC_SUM             = "cpcsum";
    public final static String EOFM_CONSTANT            = "constant";
    public final static String EOFM_USERDEFINEDTYPE     = "userdefinedtype";
    public final static String EOFM_BASICTYPE           = "basictype";
    public final static String EOFM_PARAMETER           = "parameter";
    public final static String EOFM_HUMANOPERATOR       = "humanoperator";
    public final static String EOFM_NAME                = "name";
    public final static String EOFM_INPUTVARIABLE       = "inputvariable";
    public final static String EOFM_INPUTVARIABLELINK   = "inputvariablelink";
    public final static String EOFM_LOCALVARIABLE       = "localvariable";
    public final static String EOFM_EOFM                = "eofm";
    public final static String EOFM_INITIALVALUE        = "initialvalue";
    public final static String EOFM_HUMANACTION         = "humanaction";
    public final static String EOFM_HUMANCOMACTION      = "humancomaction";
    public final static String EOFM_BEHAVIOR            = "behavior";
    public final static String EOFM_ACTIVITY            = "activity";
    public final static String EOFM_ACTIVITYLINK        = "activitylink";
    public final static String EOFM_ACTION              = "action";
    public final static String EOFM_PRECONDITION        = "precondition";
    public final static String EOFM_COMPLETIONCONDITION = "completioncondition";
    public final static String EOFM_REPEATCONDITION     = "repeatcondition";
    public final static String EOFM_DECOMPOSITION       = "decomposition";
    public final static String EOFM_OPERATOR            = "operator";
    public final static String EOFM_LINK                = "link";
    public final static String EOFM_SHAREDEOFM          = "sharedeofm";
    public final static String EOFM_ASSOCIATE           = "associate";
    public final static String EOFM_REPLACEDBY          = "replacedby";
    
    //decomposition operator attribute values
    public final static String DECOMP_PAR           = "_par";
    public final static String DECOMP_SEQ           = "_seq";
    public final static String DECOMP_OPTOR         = "optor";
    public final static String DECOMP_OR            = "or";
    public final static String DECOMP_AND           = "and";
    public final static String DECOMP_OPTOR_PAR     = DECOMP_OPTOR + DECOMP_PAR;
    public final static String DECOMP_OPTOR_SEQ     = DECOMP_OPTOR + DECOMP_SEQ;
    public final static String DECOMP_OR_PAR        = DECOMP_OR + DECOMP_PAR;
    public final static String DECOMP_OR_SEQ        = DECOMP_OR + DECOMP_SEQ;
    public final static String DECOMP_AND_PAR       = DECOMP_AND + DECOMP_PAR;
    public final static String DECOMP_AND_SEQ       = DECOMP_AND + DECOMP_SEQ;
    public final static String DECOMP_ORDERED       = "ord";
    public final static String DECOMP_XOR           = "xor";
   
    //humanaction behavior attribute values
    public final static String HUMANACTIONBEHAVIOR_AUTORESET    = "autoreset";
    public final static String HUMANACTIONBEHAVIOR_TOGGLE       = "toggle";
    public final static String HUMANACTIONBEHAVIOR_SETVALUE     = "setvalue";
    
    //
    public final static String HUMANACTION_NONACTIONSUFFIX  = "_Nothing";
    public final static String HUMANACTION_TYPENAMESUFFIX   = "_Actions";
    public final static String HA_VARNAMESUFFIX             = "_HAction";
    
    //
    public final static String ACT_TYPE                = "[actReady..actDone]";
    public final static String ACT_READY               = "actReady";
    public final static String ACT_EXECUTING           = "actExecuting";
    public final static String ACT_DONE                = "actDone";
    public final static String ACT_READY_VAL           = "0";
    public final static String ACT_EXECUTING_VAL       = "1";
    public final static String ACT_DONE_VAL            = "2";
    public final static String ACTIVITY_TYPECONSTRUCTION    = "[" + ACT_READY + ".." + ACT_DONE + "]";
    
    //public final static String EOFM_ERRORGEN = "errorgen";
    //public final static String EOFM_ERRORNUM = "errornum";
    //public final static String EOFM_ALLOWSPECIFIC = "allowspecific";
    
    public final static String EOFM_INIT_NAME           = "Initial";
    public final static String EOFM_INIT_VAL            = "-1";
    public final static String EOFM_NOERROR_NAME        = "NoError";
    public final static String EOFM_NOERROR_VAL         = "0";
    public final static String EOFM_ERROR_NAME          = "Error";
    public final static String EOFM_FUNCTION_TYPE       = "[" + EOFM_INIT_NAME + ".." + EOFM_ERROR_NAME + "]";
    public final static String EOFM_ERROR_VAL           = "1";
    
    /*public final static String EOFM_OBSERVATION_VAR     = "observation";
    public final static String EOFM_INTERPRETATION_VAR  = "interpretation";
    public final static String EOFM_PLANNING_VAR        = "planning";
    public final static String EOFM_EXECUTION_VAR       = "execution";*/
    public static String EOFM_OBSERVATION_VAR(String suffix) {
        return "observation_" + suffix;
    }
    public static String EOFM_INTERPRETATION_VAR(String suffix) {
        return "interpretation_" + suffix;
    }
    public static String EOFM_PLANNING_VAR(String suffix) {
        return "planning_" + suffix;
    }
    public static String EOFM_EXECUTION_VAR(String suffix) {
        return "execution_" + suffix;
    }
    
    //public final static String EOFM_CPC_SUM_NAME        = "CPCSum";
    
    public static String EOFM_CPC_SUM_NAME (String suffix) {
        return "CPCSum_" + suffix;
    }
    
    public static int DEFAULT_CPCSUM = -10;
    public static int NO_CPCSUM = -99;
    
    /*public final static String EOFM_P_OBS_ERROR_NAME        = "P_OBS_Error";
    public final static String EOFM_P_OBS_ERROR_FORMULA     = EOFM_CPC_SUM_NAME + " > -9 ? pow(10.0, -2.0775 + 0.0055 * pow(CPCSum, 2) - 0.2458 * CPCSum + 0.284) : pow(10.0, -2.0775)";
    public final static String EOFM_P_OBS_NO_ERROR_NAME     = "P_OBS_NoError";
    public final static String EOFM_P_OBS_NO_ERROR_FORMULA  = "1 - P_OBS_Error";*/
    public static String EOFM_P_OBS_ERROR_NAME (String suffix) {
        return "P_OBS_Error_" + suffix;
    }
    public static String EOFM_P_OBS_NO_ERROR_NAME (String suffix) {
        return "P_OBS_NoError_" + suffix;
    }
    public static String EOFM_P_OBS_NO_ERROR_FORMULA (String suffix) {
        return "1 - " + EOFM_P_OBS_ERROR_NAME(suffix);
    }
    public static String EOFM_P_OBS_ERROR_FORMULA (String suffix) {
        return EOFM_CPC_SUM_NAME(suffix) + " > -9 ? pow(10.0, -2.0775 + 0.0055 * pow(" + EOFM_CPC_SUM_NAME(suffix) + ", 2) - 0.2458 * " + EOFM_CPC_SUM_NAME(suffix) + " + 0.284) : pow(10.0, -2.0775)";
    }
    
    /*
    public final static String EOFM_P_INT_ERROR_NAME        = "P_INT_Error";
    public final static String EOFM_P_INT_ERROR_FORMULA     = EOFM_CPC_SUM_NAME + " > -9 ? pow(10.0, -1.3495 + 0.0041 * pow(CPCSum, 2) - 0.2046 * CPCSum + 0.2244) : pow(10.0, -1.3495)";
    public final static String EOFM_P_INT_NO_ERROR_NAME     = "P_INT_NoError";
    public final static String EOFM_P_INT_NO_ERROR_FORMULA  = "1 - P_INT_Error";
    */
    public static String EOFM_P_INT_ERROR_NAME (String suffix) {
        return "P_INT_Error_" + suffix;
    }
    public static String EOFM_P_INT_NO_ERROR_NAME (String suffix) {
        return "P_INT_NoError_" + suffix;
    }
    public static String EOFM_P_INT_NO_ERROR_FORMULA (String suffix) {
        return "1 - " + EOFM_P_INT_ERROR_NAME(suffix);
    }
    public static String EOFM_P_INT_ERROR_FORMULA (String suffix) {
        return EOFM_CPC_SUM_NAME(suffix) + " > -9 ? pow(10.0, -1.3495 + 0.0041 * pow(" + EOFM_CPC_SUM_NAME(suffix) + ", 2) - 0.2046 * " + EOFM_CPC_SUM_NAME(suffix) + " + 0.2244) : pow(10.0, -1.3495)";
    }
    
    /*public final static String EOFM_P_PLAN_ERROR_NAME       = "P_PLAN_Error";
    public final static String EOFM_P_PLAN_ERROR_FORMULA    = EOFM_CPC_SUM_NAME + " > -9 ? pow(10.0, -2 + 0.0052 * pow(CPCSum, 2) - 0.2828 * CPCSum + 0.4019) : pow(10.0, -2)";
    public final static String EOFM_P_PLAN_NO_ERROR_NAME    = "P_PLAN_NoError";
    public final static String EOFM_P_PLAN_NO_ERROR_FORMULA = "1 - P_PLAN_Error";*/
    public static String EOFM_P_PLAN_ERROR_NAME (String suffix) {
        return "P_PLAN_Error_" + suffix;
    }
    public static String EOFM_P_PLAN_NO_ERROR_NAME (String suffix) {
        return "P_PLAN_NoError_" + suffix;
    }
    public static String EOFM_P_PLAN_NO_ERROR_FORMULA (String suffix) {
        return "1 - " + EOFM_P_PLAN_ERROR_NAME(suffix);
    }
    public static String EOFM_P_PLAN_ERROR_FORMULA (String suffix) {
        return EOFM_CPC_SUM_NAME(suffix) + " > -9 ? pow(10.0, -2 + 0.0052 * pow(" + EOFM_CPC_SUM_NAME(suffix) + ", 2) - 0.2828 * " + EOFM_CPC_SUM_NAME(suffix) + " + 0.4019) : pow(10.0, -2)";
    }
    
    
    /*public final static String EOFM_P_EXE_ERROR_NAME        = "P_EXE_Error";
    public final static String EOFM_P_EXE_ERROR_FORMULA     = EOFM_CPC_SUM_NAME + " > -9 ? pow(10.0, -2.412 + 0.0065 * pow(CPCSum, 2) - 0.286 * CPCSum + 0.4079) : pow(10.0, -2.412)";
    public final static String EOFM_P_EXE_NO_ERROR_NAME     = "P_EXE_NoError";
    public final static String EOFM_P_EXE_NO_ERROR_FORMULA  = "1 - P_EXE_Error";*/
    public static String EOFM_P_EXE_ERROR_NAME (String suffix) {
        return "P_EXE_Error_" + suffix;
    }
    public static String EOFM_P_EXE_NO_ERROR_NAME (String suffix) {
        return "P_EXE_NoError_" + suffix;
    }
    public static String EOFM_P_EXE_NO_ERROR_FORMULA (String suffix) {
        return "1 - " + EOFM_P_EXE_ERROR_NAME(suffix);
    }
    public static String EOFM_P_EXE_ERROR_FORMULA (String suffix) {
        return EOFM_CPC_SUM_NAME(suffix) + " > -9 ? pow(10.0, -2.412 + 0.0065 * pow(" + EOFM_CPC_SUM_NAME(suffix) + ", 2) - 0.286 * " + EOFM_CPC_SUM_NAME(suffix) + " + 0.4079) : pow(10.0, -2.412)";
    }
    
    /*public final static double[] POINT_COCOM    = {-2.0775,     -1.3495,    -2,         -2.412};
    public final static double[] EF_COCOM       = {0.923,       0.651,      0,          0.89};
    public final static double[] EF_CPC         = {0.456,       0.521,      0.826,      0.223};
    public final static double[] A              = {0.0055,      0.0041,     0.0052,     0.0065};
    public final static double[] B              = {-0.2458,	-0.2046,    -0.2828,    -0.286};
    public final static double[] C              = {0.284,	0.2244,     0.4019,     0.4079};
    
     public enum COCOM{
        OBS(0), INT(1), PLAN(2), EXE(3);
        private final int value;

        COCOM(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }
    
    public static double getProbabilityOfError(COCOM mode, double r) {
        if (r >= -9) {
            return Math.pow(10, POINT_COCOM[mode.getValue()] +  A[mode.getValue()] * Math.pow(r, 2) + B[mode.getValue()] * r + C[mode.getValue()]);
        }
        else {
            return getProbabilityOfError(mode);
        }
    } 
    public static double getProbabilityOfError(COCOM mode) {
        return Math.pow(10, POINT_COCOM[mode.getValue()]);
    } */
    
    public final static String EOFM_READY_VAR_NAME       = "ready";
    public final static String EOFM_SUBMITTED_VAR_NAME   = "submitted"; 
    
    public final static String EOFM_TASK_TRANS      = "EOFMTaskTransition";
    public final static String EOFM_HANDSHAKE       = "EOFMHandshake";
    public final static String EOFM_PROCESS_ACTION  = "EOFMProcessAction";
    
    public final static String EOFM_TERMINATION_CONDITION       = "(" + PRISMWriter.BooleanValues.FALSE + ")";
    public final static String EOFM_TERMINATION_CONDITION_NAME  = "terminationCondition";
    
    public final static String EOFM_MODE_STRATEGIC      = "strategic";
    public final static String EOFM_MODE_TACTICAL       = "tactical";
    public final static String EOFM_MODE_OPPORTUNISTIC  = "opportunistic";
    public final static String EOFM_MODE_SCRAMBLED      = "scrambled";
    
    public final static double EOFM_MODE_STRATEGIC_MUTIPLIER      = 0.94;
    public final static double EOFM_MODE_TACTICAL_MUTIPLIER       = 1.9;
    public final static double EOFM_MODE_OPPORTUNISTIC_MUTIPLIER  = 7.5;
    public final static double EOFM_MODE_SCRAMBLED_MUTIPLIER      = 23;
    
    public void setElement(Element element);
    public Element getElement();
    public void setParser(EOFMParser parser);
    public EOFMParser getParser();
    public Element BuildXMLDoc(Document doc);
    
}  
