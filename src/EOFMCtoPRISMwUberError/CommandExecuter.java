package EOFMCtoPRISMwUberError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandExecuter {
    
    private ArrayList<String> output;
    private ArrayList<String> errorOutput;
    
    CommandExecuter() {
        output =  new ArrayList<String>();
        errorOutput =  new ArrayList<String>();
    }

    public ArrayList<String> getErrorOutput() {
        return errorOutput;
    }

    public ArrayList<String> getOutput() {
        return output;
    }
    
    public String getOutputAsString() {
        String returnString = "";
        for (String iString: output) {
            returnString = returnString + iString + "\n";
        }
        return returnString;
    }
    
    public String getErrorOutputAsString() {
        String returnString = "";
        for (String iString: errorOutput) {
            returnString = returnString + iString + "\n";
        }
        return returnString;
    }
    
    public void executeCommand(String command) {
        output.clear();
        errorOutput.clear();
        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader ouputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = ouputReader.readLine()) != null) {
                output.add(line);
            }
            while ((line = errorReader.readLine()) != null) {
                errorOutput.add(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandExecuter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
