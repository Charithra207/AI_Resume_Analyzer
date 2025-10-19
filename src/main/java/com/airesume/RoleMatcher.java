package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
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
            String name= cd.getOrDefault("name",cd.getOrDefault("Name","N/A")).toString();
            List<String> cdSkills =getList(cd.get("skills"),cd.get("Skills"));
            double bestMatch= 0.0;
            String bestRole= "None";
            for(Map<String,Object> job:jobData){
                String title= job.getOrDefault("title","N/A").toString();
                List<String> reqSkills= getList(job.get("requiredSkills"),null);
                double matchPer= calMatch(cdSkills,reqSkills);
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
                    result.add(item.toString());
                return result;
            }
        }
        return new ArrayList<>();
    }
    private static double calMatch(List<String>cdSkills ,List<String>reqSkills){
        if(reqSkills.isEmpty()) 
            return 0.0;
        int cmn= 0;
        for(String skill:reqSkills){
            for(String sk:cdSkills){
                if(sk.equalsIgnoreCase(skill)) {
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
