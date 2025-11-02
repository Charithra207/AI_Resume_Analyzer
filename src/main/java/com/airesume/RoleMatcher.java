package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern; 
import java.util.stream.Collectors;
public class RoleMatcher {
     public static void main(String[] args){
        matchRoles("jd_input.json","analysis_output.json","role_matches.json");
    }
    public static void matchRoles(String jdFile,String analysisFile,String opFile){
        List<Map<String,Object>> jobData =readFile(jdFile);
        List<Map<String,Object>> analysisData =readFile(analysisFile);
        if(jobData==null || analysisData==null){
            System.out.println("Could not read one or both input files!");
            return;
        }
        List<Map<String,Object>>matches =new ArrayList<>();
        for(Map<String,Object> cd:analysisData){
            String name= cd.containsKey("Name")? cd.get("Name").toString(): cd.containsKey("name") ?cd.get("name").toString():"Unknown";
            String resumeText= cd.getOrDefault("RawText","").toString();            
            Set<String> cdSkills = new HashSet<>(getList(cd.get("Skills")));
            cdSkills.addAll(NLPUtils.findSkills(resumeText).stream().map(String::toLowerCase).collect(Collectors.toSet()));
            double bestMatch= 0.0;
            String bestRole= "None";
            for(Map<String,Object> job:jobData){
                String title= job.containsKey("title")? job.get("title").toString(): job.containsKey("Job title")? job.get("Job title").toString():"Unknown";
                List<String> reqSkills= getList(job.get("requiredSkills"));
                double matchPer= calMatch(new ArrayList<>(cdSkills),reqSkills);
                List<String> nlpRoles = NLPUtils.findJobTitles(resumeText);
                if(nlpRoles.stream().anyMatch(r -> r.equalsIgnoreCase(title)))
                 matchPer += 5.0;
                if(matchPer > bestMatch){
                    bestMatch= matchPer;
                    bestRole= title;
                }
            }
            Map<String,Object> result= new LinkedHashMap<>();
            result.put("name",name);
            result.put("bestRole",bestRole);
            result.put("matchPercent",Math.round(bestMatch*100.0)/100.0);
            matches.add(result);
        }
        writeFile(opFile,matches);
        System.out.println("Job role matching completed."+opFile);
    }
    private static List<String> getList(Object... posLists){
        for(Object obj:posLists){
            if(obj instanceof List){
                List<?>list = (List<?>)obj;
                List<String>result =new ArrayList<>();
                for(Object item:list)
                    if(item != null)
                        result.add(item.toString().toLowerCase());
                return result;
            }
        }
        return new ArrayList<>();
    }
    private static double calMatch(List<String>cdSkills ,List<String>reqSkills){
        if(reqSkills.isEmpty() || cdSkills.isEmpty()) 
            return 0.0;
        int cmn= 0;
        for(String skill:reqSkills){
            for(String sk:cdSkills){
                if(sk.equalsIgnoreCase(skill) || Pattern.compile("\\b"+Pattern.quote(skill)+"\\b").matcher(sk).find()){
                    cmn++;
                    break;
                }
            }
        }
        return (double) cmn/reqSkills.size()*100;
    }
    private static List<Map<String,Object>> readFile(String filename){
        try(Reader reader =new FileReader(filename)){
            Gson gson = new GsonBuilder().create();
            Type listType =new TypeToken<List<Map<String,Object>>>(){}.getType();
            return gson.fromJson(reader,listType);
        } 
        catch(Exception e){
            System.out.println("Could not read " +filename+": "+e.getMessage());
            return null;
        }
    }
    private static void writeFile(String filename,List<Map<String,Object>> data){
        try(Writer writer =new FileWriter(filename)){
            Gson gson =new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data,writer);
        } 
        catch(Exception e){
            System.out.println("Could not write "+filename+": "+e.getMessage());
        }
    }
}
