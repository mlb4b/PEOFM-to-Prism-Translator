package EOFMCtoPRISMwUberError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EOFMInputVariableLink implements EOFMElement {
    private String variableName;
    private EOFMInputVariable linkedVariable;
    private Element element;
    private EOFMParser parser;
    
    public EOFMInputVariableLink() {
    }

    public EOFMInputVariable getVariable() {
        return (EOFMInputVariable) parser.getEOFMElement(variableName);
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
        linkedVariable.writeVariableDeclaration(prismWriter);
    }
    
    private void extractElementData() {
        variableName = parser.extractAttributeData(element, EOFM_LINK);
        linkedVariable = (EOFMInputVariable)parser.getEOFMElement(variableName);
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_INPUTVARIABLELINK);
        returnElement.setAttribute(EOFM_LINK, variableName);
        return returnElement;
    }
}
