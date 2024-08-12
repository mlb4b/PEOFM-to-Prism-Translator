package EOFMCtoPRISMwUberError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMInputVariable implements EOFMElement {
    
    private Element element;
    private EOFMParser parser;
    private EOFMUserDefinedType type;
    
    private String name;
    private String userDefinedTypeName;
    private String basicTypeName;
    
    public EOFMInputVariable() {
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
    
    public void writeVariableDeclaration(PRISMWriter prismWriter) {
        if (basicTypeName.isEmpty()) {
            prismWriter.writeVariableDeclaration(name, type.GetPrismTypeConstruction());
        }
        else {
            prismWriter.writeVariableDeclaration(name, PRISMWriter.VariableType.getBasicType(basicTypeName));
        }
    }
    
    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(name, this);
        basicTypeName = parser.extractAttributeData(element, EOFM_BASICTYPE);
        userDefinedTypeName = parser.extractAttributeData(element, EOFM_USERDEFINEDTYPE);
        if (!userDefinedTypeName.isEmpty()) {
            type = (EOFMUserDefinedType)parser.getEOFMElement(userDefinedTypeName);
            parser.addVariable(name, type.GetPrismTypeConstruction());
        }
        else {
            //this may not always be correct, but I can't really think of an exception
            parser.addBooleanVariable(name);
        }
    }
    
     public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_INPUTVARIABLE);
        returnElement.setAttribute(EOFM_NAME, name);
        if (!userDefinedTypeName.isEmpty()) {
            returnElement.setAttribute(EOFM_USERDEFINEDTYPE, userDefinedTypeName);
        }
        else if (!basicTypeName.isEmpty()) {
            returnElement.setAttribute(EOFM_BASICTYPE, basicTypeName);
        }
        return returnElement;
    }
}
