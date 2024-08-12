package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMHumanAction implements EOFMElement {
    
    private Element element;
    private EOFMParser parser;
    private String name;
    private String behavior;
    private String userDefinedTypeName;
    private String basicTypeName;
    private String localVariableName;
    private String initialValue;
   
    private EOFMUserDefinedType type;
    private EOFMLocalVariable local;

    private ArrayList<EOFMAction> actions = new ArrayList<EOFMAction>();
    
    public EOFMHumanAction() {
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

    public String getBehavior() {
        return behavior;
    }

    public String getLocalVariableName() {
        return localVariableName;
    }
    
    public String getTypeName() {
        if (!userDefinedTypeName.isEmpty()) {
            return userDefinedTypeName;
        }
        else if (!basicTypeName.isEmpty()) {
            return basicTypeName;
        }
        else {
            return "BOOLEAN";
        }
    }
    
    public boolean hasSameType(EOFMHumanAction theHumanAction) {
        return theHumanAction.getTypeName().equals(this.getTypeName());
    }
    
    public ArrayList<String> getTypeValues() {
        if (!userDefinedTypeName.isEmpty()) {
            return type.getEnumerations();
        }
        if (basicTypeName.toUpperCase().equals("BOOLEAN")) {
            return new ArrayList<String>() { 
                { 
                    add(PRISMWriter.BooleanValues.TRUE); 
                    add(PRISMWriter.BooleanValues.FALSE);
                } 
            };
        }
        //if (typ)
        return null;
    }
    
    public void writeVariableDeclaration(PRISMWriter prismWriter) {
        /*if (!behavior.equals(HUMANACTIONBEHAVIOR_SETVALUE))
            prismWriter.writeVariableDeclaration(name, PRISMWriter.VariableType.BOOL, initialValue);
        else {
            if(!userDefinedTypeName.isEmpty())
                prismWriter.writeVariableDeclaration(name, type.GetPrismTypeConstruction(), initialValue); 
            else if (!basicTypeName.isEmpty())
                prismWriter.writeVariableDeclaration(name, basicTypeName, initialValue);
            else
                prismWriter.writeVariableDeclaration(name, local.GetPrismTypeConstruction(), initialValue);
        }*/
        prismWriter.writeVariableDeclaration(name, getPrismConstruction(), initialValue);
    }
    
     public String getPrismConstruction() {
        if (!behavior.equals(HUMANACTIONBEHAVIOR_SETVALUE))
            return PRISMWriter.VariableType.BOOL;
        else {
            if(!userDefinedTypeName.isEmpty())
                return type.GetPrismTypeConstruction(); 
            else if (!basicTypeName.isEmpty())
               return basicTypeName;
            else
                return local.GetPrismTypeConstruction();
        }
    }
    
    public void writeInitialValue(PRISMWriter prismWriter) {
        prismWriter.writeInitialValue(name, initialValue);
        /*if (!behavior.equals(HUMANACTIONBEHAVIOR_SETVALUE)) {
            prismWriter.writeInitialValue(name, PRISMWriter.BooleanValues.FALSE);
        }*/
    }
    
    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(name, this);
        behavior = parser.extractAttributeData(element, EOFM_BEHAVIOR);
        userDefinedTypeName = parser.extractAttributeData(element, EOFM_USERDEFINEDTYPE);
        if (!userDefinedTypeName.isEmpty()) {
            type = (EOFMUserDefinedType)parser.getEOFMElement(userDefinedTypeName);
        }
        basicTypeName = parser.extractAttributeData(element, EOFM_BASICTYPE);
        localVariableName = parser.extractAttributeData(element, EOFM_LOCALVARIABLE);
        if (!localVariableName.isEmpty()) {
            local = (EOFMLocalVariable)parser.getEOFMElement(localVariableName);
        }
        if (behavior.equals(HUMANACTIONBEHAVIOR_SETVALUE)) {
            initialValue = PRISMWriter.prismExpression(parser.extractChildElementData(element, EOFM_INITIALVALUE));
        }
        else if (!localVariableName.isEmpty()) {
            initialValue = localVariableName;
        }
        else {
            basicTypeName = "BOOLEAN";
            initialValue = PRISMWriter.BooleanValues.FALSE;
        }
        
        //if (basicTypeName.toUpperCase().equals("BOOLEAN")) {
        //    parser.addBooleanVariable(name);
        //}
        //else {
        if (!basicTypeName.toUpperCase().equals("BOOLEAN")){
            parser.addVariable(name, getPrismConstruction());
        } else {
            parser.addBooleanVariable(name);
        }
        //todo: maybe need to add some error checking to give information when this fails
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_HUMANACTION);
        returnElement.setAttribute(EOFM_NAME, name);
        returnElement.setAttribute(EOFM_BEHAVIOR, behavior);
        if (!userDefinedTypeName.isEmpty()) {
            returnElement.setAttribute(EOFM_USERDEFINEDTYPE, userDefinedTypeName);
        }
        else if (!basicTypeName.isEmpty()) {
            returnElement.setAttribute(EOFM_BASICTYPE, basicTypeName);
        }
        else if (!localVariableName.isEmpty()) {
            returnElement.setAttribute(EOFM_LOCALVARIABLE, localVariableName);
        }
        
        //returnElement.setAttribute(name, name);
        return returnElement;
    }
}