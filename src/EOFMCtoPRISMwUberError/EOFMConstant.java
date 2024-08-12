package EOFMCtoPRISMwUberError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMConstant implements EOFMElement {
    
    private String name;
    private String userDefinedTypeName;
    private String basicTypeName;
    private String construction;
    private String parameter;
    private Element element;
    private EOFMParser parser;
    private EOFMUserDefinedType type;
    
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
    
    public String getConstuction() {
        return construction;
    }
    
     public void writeConstant(PRISMWriter prismWriter) {
         String outName = name;
         if (!parameter.isEmpty())
             outName += "(" + parameter + ")";
         if (basicTypeName.isEmpty())
             prismWriter.writeConstant(outName, type.GetPrismTypeConstruction(), construction);
         else
             prismWriter.writeConstant(outName, PRISMWriter.VariableType.getBasicType(basicTypeName), construction);
    }
    
    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(name, this);
        userDefinedTypeName = parser.extractAttributeData(element, EOFM_USERDEFINEDTYPE);
        if (!userDefinedTypeName.isEmpty()) {
            type = (EOFMUserDefinedType)parser.getEOFMElement(userDefinedTypeName);
        }
        basicTypeName = parser.extractAttributeData(element, EOFM_BASICTYPE);
        parameter  = parser.extractAttributeData(element, EOFM_PARAMETER);
        construction = parser.extractElementData(element);
        parser.addConstant(name, construction);
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_CONSTANT);
        if (!name.isEmpty())
            returnElement.setAttribute(EOFM_NAME, name);
        if (!parameter.isEmpty())
            returnElement.setAttribute(EOFM_NAME, parameter);
        if (!userDefinedTypeName.isEmpty())
            returnElement.setAttribute(EOFM_USERDEFINEDTYPE, userDefinedTypeName);
        else if (!basicTypeName.isEmpty())
            returnElement.setAttribute(EOFM_BASICTYPE, basicTypeName);
        if (!basicTypeName.isEmpty())
            returnElement.setTextContent(construction);
        return returnElement;
    }

}
