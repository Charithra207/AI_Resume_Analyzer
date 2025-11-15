package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;
public class BiasDetector{
    private static final List<String> biasWords = Arrays.asList("young","energetic","dominant","aggressive","female","male","girls only","boys only","youthful","mature","manpower");
    public static void main(String[] args){
        detectBias("jd_input.json", "analysis_output.json", "bias_report.json");
    }
    public static void detectBias(String jdFile,String analysisFile,String opFile){
        List<Map<String,Object>> biasReports = new ArrayList<>();
        List<Map<String,Object>> jobs = readFile(jdFile);
        if(jobs != null){
            for(Map<String,Object> job:jobs){
                String desc= safeString(job.get("description"));
                if(desc.isEmpty()) continue;
                String normDesc= NLPUtils.normalizeText(desc);
                List<String>foundBias = findBias(normDesc);
                if(!foundBias.isEmpty()){
                    Map<String,Object> report =new LinkedHashMap<>();
                    report.put("source",jdFile);
                    report.put("jobId",job.getOrDefault("id", "N/A"));
                    report.put("title",job.getOrDefault("title", "N/A"));
                    report.put("biasDetected",foundBias);
                    report.put("severity",severityLevel(foundBias.size()));
                    biasReports.add(report);
                }
            }
        }
        List<Map<String,Object>> resumes= readFile(analysisFile);
        if(resumes != null){
            for(Map<String,Object> res:resumes){
                String summary= safeString(res.getOrDefault("summary", res.get("RawText")));
                if(summary.isEmpty()) 
                    continue;
                String normSummary= NLPUtils.normalizeText(summary);
                List<String>foundBias = findBias(normSummary);
                if (!foundBias.isEmpty()) {
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("source",analysisFile);
                    report.put("name",res.getOrDefault("Name", "N/A"));
                    report.put("biasDetected",foundBias);
                    report.put("severity",severityLevel(foundBias.size()));
                    biasReports.add(report);
                }
            }
        }
        writeFile(opFile,biasReports);
        System.out.println("Bias report generated: "+opFile);
    }
    private static List<String>findBias(String text){
        List<String>found = new ArrayList<>();
        String lowerText= text.toLowerCase();
        for(String word:biasWords){
            String w= word.toLowerCase().trim();
            String pattern= "\\b"+ Pattern.quote(word) +"\\b";
            if(Pattern.compile(pattern,Pattern.CASE_INSENSITIVE).matcher(lowerText).find())
                found.add(w);
        }
        return new ArrayList<>(found);
    }    
    private static String severityLevel(int count){
        if(count>5) 
            return "Critical";
        if(count>3) 
            return "High";
        if(count>0) 
            return "Medium";
        return 
            "None";
    }
    private static List<Map<String,Object>>readFile(String filename){
        try(Reader reader =new FileReader(filename)){
            Gson gson= new GsonBuilder().create();
            Type listType =new TypeToken<List<Map<String,Object>>>(){}.getType();
            List<Map<String,Object>> list= gson.fromJson(reader,listType);
            return list==null ? Collections.emptyList() : list;
        } 
        catch(Exception e){
            System.out.println("Could not read "+filename+": "+e.getMessage());
            return Collections.emptyList();
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
    private static String safeString(Object o){
        return o==null ? "": o.toString();
    }
}
