package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                fresher.put("Total Experience", 0.0);
                op.add(fresher);
                continue;
            }
            List<Map<String,Object>>expList = getExpList(cd.get("Experience"));
            if(expList.isEmpty()){
                Map<String,Object> fresher =new LinkedHashMap<>();
                fresher.put("Name",name);
                fresher.put("expTime", "Fresher");
                fresher.put("Total Experience", 0);
                op.add(fresher);
            } 
            else{
                Map<String,Object> out= new LinkedHashMap<>();
                out.put("Name",name);
                out.put("expTime",expList);                
                Object ty= cd.getOrDefault("Total Experience Years",cd.getOrDefault("Total Experience",
                            cd.getOrDefault("Experience Years", 0)));   
                double totalYears;             
                if(ty instanceof Number)
                    totalYears= ((Number)ty).doubleValue();
                else if(ty instanceof String && !((String)ty).trim().isEmpty())
                    totalYears= parseExperienceString((String)ty);                
                else{                    
                    totalYears= 0.0;
                    for(Map<String,Object> e: expList){
                        Object yrs= e.get("years");
                        if(yrs instanceof Number) totalYears+= ((Number)yrs).doubleValue();
                        else if(yrs instanceof String) totalYears+= parseExperienceString((String)yrs);
                    }
                }
                totalYears= Math.round(totalYears*10.0)/10.0;
                out.put("Total Experience Years",totalYears);
                op.add(out);
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
                    @SuppressWarnings("unchecked")
                    Map<String,Object> expMap= (Map<String,Object>) exp;
                    Map<String,Object> result= new LinkedHashMap<>();
                    String company= safeString(expMap.getOrDefault("company",expMap.getOrDefault("Company","Unknown")));
                    String role= safeString(expMap.getOrDefault("role",expMap.getOrDefault("Role","Unknown")));
                    List<String> detectedOrgs= NLPUtils.findOrganizations(company+" "+role);
                    if(!detectedOrgs.isEmpty())
                        company= detectedOrgs.get(0);
                    List<String> detectedRoles= NLPUtils.findJobTitles(role);
                    if(!detectedRoles.isEmpty())
                        role= detectedRoles.get(0);
                    Object yearsObj= expMap.getOrDefault("years",expMap.getOrDefault("duration",expMap.getOrDefault("durationYears", 0)));
                    double years= 0.0;
                    if(yearsObj instanceof Number) years= ((Number)yearsObj).doubleValue();
                    else if(yearsObj instanceof String) years= parseExperienceString((String)yearsObj);
                    result.put("company",company);
                    result.put("role",role);
                    result.put("years",Math.round(years*10.0)/10.0);
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
            List<Map<String,Object>> list= gson.fromJson(reader,listType);
            return list==null ? Collections.emptyList() : list;
        } 
        catch(Exception e){
            System.out.println("Error reading "+fname+": "+e.getMessage());
            return Collections.emptyList();
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
    private static double parseExperienceString(String s){
        if(s==null) return 0.0;
        String text= s.trim().toLowerCase();
        double years= 0.0;
        Pattern pYears= Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:years|year|yrs|yr)"); 
        Matcher my= pYears.matcher(text);
        if(my.find()){
            try{ years+= Double.parseDouble(my.group(1)); } 
            catch(Exception ignore){}
        }
        Pattern pMonths= Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:months|month|mos|mo|m)");
        Matcher mm= pMonths.matcher(text);
        if(mm.find()){
            try{ years += Double.parseDouble(mm.group(1))/12.0; } 
            catch(Exception ignore){}
        }
        if(years==0.0){
            Pattern pNum= Pattern.compile("^(\\d+(?:\\.\\d+)?)");
            Matcher mnum= pNum.matcher(text);
            if(mnum.find()){
                try{ years= Double.parseDouble(mnum.group(1)); } 
                catch(Exception ignore){}
            }
        }
        return Math.round(years*10.0)/10.0;
    }
    private static String safeString(Object o){
        return o==null ? "": o.toString();
    }
}

