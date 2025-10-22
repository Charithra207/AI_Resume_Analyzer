package com.airesume;
import com.google.gson.*;
import java.io.*;
import java.util.*;
public class JsonHandler {
        public static void main(String[] args){
        String ipFilePath="resume_input.json";
        String opFilePath="analysis_output.json";
        List<String> resumeTexts=readResumes(ipFilePath);
        List<Map<String,Object>> allResults=new ArrayList<>();
        for(String resumeText:resumeTexts){
            Map<String,Object> result=RegexAnalyzer.analyze(resumeText);
            allResults.add(result);
        }
        writeResults(allResults,opFilePath);
        System.out.println("All resumes analyzed successfully!\nOutput file: "+opFilePath);
    }
    public static List<String> readResumes(String filePath){
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        List<String> resumeTexts=new ArrayList<>();
        try(FileReader reader=new FileReader(filePath)){
            JsonElement element= gson.fromJson(reader,JsonElement.class);
            if(element.isJsonArray()){
                for(JsonElement ele:element.getAsJsonArray()) {
                    if(ele.isJsonPrimitive())
                        resumeTexts.add(ele.getAsString());
                    else if (ele.isJsonObject() && ele.getAsJsonObject().has("resume")) 
                        resumeTexts.add(ele.getAsJsonObject().get("resume").getAsString());  
                }            
            }
            else if(element.isJsonPrimitive())
                resumeTexts.add(element.getAsString());
            System.out.println(resumeTexts.size()+" resumes loaded successfully from: "+filePath);
        }
        catch(IOException e){
            System.out.println("Could not read resumes from: "+filePath);
        }
        return resumeTexts;
    }
    public static void writeResults(List<Map<String,Object>> results,String filePath){
        if(results == null || results.isEmpty()) {  
            System.out.println("No analysis results to write.");
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer=new FileWriter(filePath)){
            gson.toJson(results,writer);
            System.out.println("Analysis results saved to: "+filePath);
        } 
        catch (IOException e){
            System.out.println("Error writing JSON file: "+e.getMessage());
        }
    }
}

