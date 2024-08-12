package EOFMCtoPRISMwUberError;

import java.util.ArrayList;

/**
 *
 * @author mlb4b
 */
public interface EOFMAct extends EOFMElement{
    public String getName();
    public EOFMAct getChild(int index);
    public EOFMAct getCopyWithNewName(String nameSuffix);
    public void writeTransitions(PRISMWriter prismWriter, ArrayList<EOFMAct> peers, String readyVariableName, String submittedVariableName);
    public void writeVariableDeclarations(PRISMWriter prismWriter);
    public void writeInitialValues(PRISMWriter prismWriter);
    public void writeRecursiveAssignment(PRISMWriter prismWriter, String assignment);
    public void writePassThroughActionDoneAssignments(PRISMWriter prismWriter);
    public String getRecursiveNotExecuting();
    
    public void setParent(EOFMAct parent);
    public EOFMAct getParent();
    
    public void writeCognitiveFunctions(PRISMWriter prismWriter);
    public double getCPCSum();
    public double getCPCSumUp();
    public double getCPCSumDown();
    public void setCPCSum(double cpcSum);
    public String getCognitiveSuffix();
}
