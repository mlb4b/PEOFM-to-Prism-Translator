package EOFMCtoPRISMwUberError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMHumanComAction implements EOFMElement {

    private Element element;
    private EOFMParser parser;
    private String name;
    private String behavior;
    private String userDefinedTypeName;
    private String basicTypeName;
    private String localVariableName;
    private EOFMUserDefinedType type;
    private EOFMLocalVariable local;

    public EOFMHumanComAction() {
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

    //public String getBehavior() {
    //    return behavior;
    //}

    public void writeVariableDeclaration(PRISMWriter prismWriter) {
        //if (!behavior.equals(HUMANACTIONBEHAVIOR_SETVALUE))
            //prismWriter.writeVariableDeclaration(variableScope, name, SALWriter.BOOLEAN_TYPE);
        //else {
            if(!userDefinedTypeName.isEmpty())
                prismWriter.writeVariableDeclaration(name, type.GetPrismTypeConstruction());
            else if (!basicTypeName.isEmpty())
                prismWriter.writeVariableDeclaration(name, PRISMWriter.VariableType.getBasicType(basicTypeName));
            else
                prismWriter.writeVariableDeclaration(name, ((EOFMLocalVariable)(parser.getEOFMElement(localVariableName))).GetPrismTypeConstruction());
        //}
    }

    public void writeInitialValue(PRISMWriter prismWriter) {
        if (!behavior.equals(HUMANACTIONBEHAVIOR_SETVALUE))
            prismWriter.writeInitialValue(name, PRISMWriter.BooleanValues.FALSE);
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
    }

    public String getTypeName() {
        if (basicTypeName.isEmpty())
            return userDefinedTypeName;
        else
            return basicTypeName;
    }
   
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_HUMANCOMACTION);
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
