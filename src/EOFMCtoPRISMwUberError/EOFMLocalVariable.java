package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author mlb4b
 */
public class EOFMLocalVariable implements EOFMElement {
    
    private Element element;
    private EOFMParser parser;
    
    private String name;
    private String userDefinedTypeName;
    private String basicTypeName;
    private String initialValue;
    private EOFMUserDefinedType type;
    
    public EOFMLocalVariable() {
    }

    public String getName() {
        return name;
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
    
    public boolean hasSameType(EOFMLocalVariable theLocalVariable) {
        return theLocalVariable.getTypeName().equals(this.getTypeName());
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
        return null;
    }
    
    public void writeVariableDeclaration(PRISMWriter prismWriter) {
        /*if (basicTypeName.isEmpty()) 
            prismWriter.writeVariableDeclaration(variableScope, name, userDefinedTypeName);
        else
            prismWriter.writeVariableDeclaration(variableScope, name, basicTypeName);*/
        /*if (basicTypeName.isEmpty()) {
            prismWriter.writeVariableDeclaration(name, type.GetPrismTypeConstruction());
        }
        else {
            prismWriter.writeVariableDeclaration(name, PRISMWriter.VariableType.getBasicType(basicTypeName));
        }*/
        prismWriter.writeVariableDeclaration(name, GetPrismTypeConstruction(), initialValue);
    }
    
    public void writeInitialValue(PRISMWriter prismWriter) {
        prismWriter.writeInitialValue(name, initialValue);
    }

    public String getInitialValue() {
        return initialValue;
    }

    public String getTypeName() {
        if (basicTypeName.isEmpty())
            return userDefinedTypeName;
        else
            return basicTypeName;
    }
    
    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(name, this);
        userDefinedTypeName = parser.extractAttributeData(element, EOFM_USERDEFINEDTYPE);
        if (!userDefinedTypeName.isEmpty()) {
            type = (EOFMUserDefinedType)parser.getEOFMElement(userDefinedTypeName);
        }
        basicTypeName = parser.extractAttributeData(element, EOFM_BASICTYPE);
        initialValue = PRISMWriter.prismExpression(parser.extractChildElementData(element, EOFM_INITIALVALUE));
        if (basicTypeName.toUpperCase().equals("BOOLEAN")) {
            parser.addBooleanVariable(name);
        }
        else {
            parser.addVariable(name, GetPrismTypeConstruction());
        }
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_LOCALVARIABLE);
        returnElement.setAttribute(EOFM_NAME, name);
        if (!userDefinedTypeName.isEmpty()) {
            returnElement.setAttribute(EOFM_USERDEFINEDTYPE, userDefinedTypeName);
        }
        else if (!basicTypeName.isEmpty()) {
            returnElement.setAttribute(EOFM_BASICTYPE, basicTypeName);
        }
        Element initialValueElement = doc.createElement(EOFM_INITIALVALUE);
        initialValueElement.setTextContent(initialValue);
        returnElement.appendChild(initialValueElement);
        return returnElement;
    }
    
    public String GetPrismTypeConstruction() {
        if (!basicTypeName.isEmpty())
            return PRISMWriter.VariableType.getBasicType(basicTypeName);
        else
            return type.GetPrismTypeConstruction();
    }
}
