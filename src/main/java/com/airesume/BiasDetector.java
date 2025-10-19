package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class BiasDetector{
    private static final List<String> biasWords = Arrays.asList("young","energetic","dominant","aggressive","female","male","girls only","boys only","youthful","mature","manpower");
    public static void main(String[] args){
        detectBias("jd_input.json", "analysis_dup.json", "bias_report.json");
    }
    public static void detectBias(String jdFile,String analysisFile,String opFile){
        List<Map<String,Object>> biasReports = new ArrayList<>();
        List<Map<String,Object>> jobs = readFile(jdFile);
        if(jobs != null){
            for(Map<String,Object> job:jobs){
                String desc = job.getOrDefault("description","").toString().toLowerCase();
                List<String>foundBias = findBias(desc);
                if(!foundBias.isEmpty()){
                    Map<String,Object> report =new LinkedHashMap<>();
                    report.put("source",jdFile);
                    report.put("jobId",job.getOrDefault("id", "N/A"));
                    report.put("title",job.getOrDefault("title", "N/A"));
                    report.put("biasDetected",foundBias);
                    report.put("severity",foundBias.size() >3 ?"High":"Medium");
                    biasReports.add(report);
                }
            }
        }
        List<Map<String,Object>> resumes= readFile(analysisFile);
        if(resumes != null){
            for(Map<String,Object> res:resumes){
                String summary= res.getOrDefault("summary", "").toString().toLowerCase();
                List<String>foundBias = findBias(summary);
                if (!foundBias.isEmpty()) {
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("source",analysisFile);
                    report.put("name",res.getOrDefault("name", "N/A"));
                    report.put("biasDetected",foundBias);
                    report.put("severity",foundBias.size() > 3 ? "High" : "Low");
                    biasReports.add(report);
                }
            }
        }
        writeFile(opFile,biasReports);
        System.out.println("Bias report generated: "+opFile);
    }
    private static List<String>findBias(String text){
        List<String>found = new ArrayList<>();
        for(String word:biasWords){
            if(text.contains(word)){
                found.add(word);
            }
        }
        return found;
    }
    private static List<Map<String,Object>>readFile(String filename){
        try(Reader reader =new FileReader(filename)){
            Gson gson= new GsonBuilder().create();
            Type listType =new TypeToken<List<Map<String,Object>>>(){}.getType();
            return gson.fromJson(reader,listType);
        } 
        catch(Exception e){
            System.out.println("Could not read "+filename+": "+e.getMessage());
            return null;
        }
    }
    private static void writeFile(String filename,List<Map<String,Object>>data){
        try(Writer writer= new FileWriter(filename)){
            Gson gson= new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data,writer);
        } 
        catch(Exception e){
            System.out.println("Could not write "+filename+": "+e.getMessage());
        }
    }
}
