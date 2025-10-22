package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
public class Exp {
    public static void main(String[] args){
        expTime("analysis_output.json","exp.json");
    }
    public static void expTime(String ipFile,String opFile){
        List<Map<String,Object>>analysis =readFile(ipFile);
        if(analysis==null || analysis.isEmpty()){
            System.out.println("No data found in "+ipFile);
            return;
        }
        List<Map<String,Object>>op =new ArrayList<>();
        for(Map<String,Object> cd:analysis){
            String name= cd.getOrDefault("Name","N/A").toString();
            if(!cd.containsKey("Experience") || cd.get("Experience")==null){
                Map<String,Object> fresher =new LinkedHashMap<>();
                fresher.put("Name",name);
                fresher.put("expTime","Fresher");
                op.add(fresher);
                continue;
            }
            List<Map<String,Object>>expList = getExpList(cd.get("Experience"));
            if(expList.isEmpty()){
                Map<String,Object> fresher =new LinkedHashMap<>();
                fresher.put("Name",name);
                fresher.put("expTime", "Fresher");
                op.add(fresher);
            } 
            else{
                Map<String,Object> exp= new LinkedHashMap<>();
                exp.put("Name",name);
                exp.put("expTime",expList);
                Object ty= cd.getOrDefault("Total Experience Years", 0);
                exp.put("Total Experience Years",ty);
                op.add(exp);
            }
        }
        writeFile(opFile,op);
        System.out.println("Experience timeline generated "+opFile);
    }
    private static List<Map<String,Object>> getExpList(Object expObj){
        List<Map<String,Object>>expList =new ArrayList<>();
        if(expObj instanceof List<?>){
            for(Object exp:(List<?>) expObj){
                if(exp instanceof Map<?,?>){
                    Map<String,Object> expMap= (Map<String,Object>) exp;
                    Map<String,Object> result= new LinkedHashMap<>();
                    result.put("company",expMap.getOrDefault("company", "Unknown"));
                    result.put("role", expMap.getOrDefault("role", "Unknown"));
                    result.put("years", expMap.getOrDefault("years", 0));
                    expList.add(result);                    
                }
            }
        }
        return expList;
    }
    private static List<Map<String,Object>> readFile(String fname){
        try(Reader reader =new FileReader(fname)){
            Gson gson =new Gson();
            Type listType =new TypeToken<List<Map<String,Object>>>(){}.getType();
            return gson.fromJson(reader,listType);
        } 
        catch(Exception e){
            System.out.println("Error reading "+fname+": "+e.getMessage());
            return null;
        }
    }
    private static void writeFile(String fname,List<Map<String,Object>> data){
        try(Writer writer =new FileWriter(fname)){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data,writer);
        } 
        catch(Exception e){
            System.out.println("Error writing "+fname+": "+e.getMessage());
        }
    }
}

