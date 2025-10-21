package com.airesume;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Ranker{
    private static final double MIN_SCORE = 40.0;
    private static final double MIN_EXP = 1.0;
    public static void main(String[] args){
        String ipFile = "eligibility_results.json";
        List<JobResults> jobList = readResults(ipFile);
        if(jobList.isEmpty()){
            System.out.println("No job data found!");
            return;
        }
        for(JobResults job:jobList){
            System.out.println("Processing Job ID: "+job.jobId);
            List<CdResult> filtered = new ArrayList<>();          
            for(CdResult c:job.candidates){
                if(c.scorePer >= MIN_SCORE && c.experienceYears >= MIN_EXP){
                    filtered.add(c);
                }
            }
            Collections.sort(filtered,new Comparator<CdResult>(){
                public int compare(CdResult a,CdResult b){
                    if(b.scorePer != a.scorePer)
                        return Double.compare(b.scorePer,a.scorePer);
                    else if(b.experienceYears != a.experienceYears)
                        return Double.compare(b.experienceYears,a.experienceYears);
                    else
                        return Double.compare(a.careerGapYears,b.careerGapYears);
                }
            });
            int total = filtered.size();
            int rank =1;
            for(CdResult c:filtered){
                c.rank = rank;
                c.tc = total;
                rank++;
            }
            String opFile = "ranked_candidates_"+job.jobId+".json";
            writeResults(filtered,opFile);
            System.out.println("Ranked file saved: "+opFile);
        }
    }
    private static List<JobResults> readResults(String path){
        Gson gson = new GsonBuilder().create();
        try(FileReader reader = new FileReader(path)){
            Type t = new TypeToken<List<JobResults>>(){}.getType();
            List<JobResults> list = gson.fromJson(reader,t);
            if(list == null) 
                return Collections.emptyList();
            System.out.println("Loaded "+list.size()+" job(s) from "+path);
            return list;
        } 
        catch(IOException e){
            System.out.println("Error reading file: "+path);
            return Collections.emptyList();
        }
    }
    private static void writeResults(Object data,String path){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter(path)){
            gson.toJson(data,writer);
        } 
        catch(IOException e){
            System.out.println("Error writing: "+e.getMessage());
        }
    }
    static class JobResults{
        String jobId;
        String title;
        List<CdResult> candidates;
    }
    static class CdResult{
        String name;
        String email;
        List<String> matchedSkills;
        int matchedCount;
        int requiredCount;
        double experienceYears;
        double careerGapYears;
        boolean meetsExperience;
        double scorePer;
        int rank;
        int tc;
    }
}

