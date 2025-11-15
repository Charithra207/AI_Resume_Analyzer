package com.airesume;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

public class Ranker{
    private static final double MIN_SCORE = 40.0;
    private static final double MIN_EXP = 1.0;
    private static final double W_BASE = 0.65;
    private static final double W_SEMANTIC = 0.20;
    private static final double W_SKILL = 0.15;
    public static void main(String[] args){
        String eligibilityFile= "eligibility_results.json";
        String analysisFile= "analysis_output.json";
        String jdFile= "jd_input.json";
        List<JobResults> jobList= readResults(eligibilityFile);
        if(jobList.isEmpty()){
            System.out.println("No job data found!");
            return;
        }
        Map<String,String> resumeTexts= loadResumeTexts(analysisFile);
        Map<String,String> jobDescriptions= loadJobDescriptions(jdFile);
        for(JobResults job:jobList){
            String jobIdSafe= job.jobId==null ? "unknown": job.jobId;
            System.out.println("Processing Job ID: "+jobIdSafe);
            if (job.candidates==null || job.candidates.isEmpty()){
                System.out.println("No candidates for Job ID: " + jobIdSafe);
                continue;
            }
            List<CdResult> filtered = new ArrayList<>();          
            for(CdResult c:job.candidates){
                if(c.scorePer >= MIN_SCORE && c.experienceYears >= MIN_EXP){
                    String resumeText= resumeTexts.getOrDefault(c.name.toLowerCase(), "");
                    String jdText= jobDescriptions.getOrDefault(job.jobId,jobDescriptions.getOrDefault(job.title, ""));
                    double semanticScore= 0.0;
                    try{
                        semanticScore=NLPUtils.semanticSimilarity(resumeText, jdText)*100;
                    }
                    catch(Exception e){
                        System.out.println("Semantic similarity error for "+c.name+ ": "+e.getMessage());
                        semanticScore = 0.0;
                    }
                    List<String> resumeSkills= NLPUtils.findSkills(resumeText);
                    List<String> jdSkills= NLPUtils.findSkills(jdText);
                    double skillOverlap= calculateSkillOverlap(resumeSkills,jdSkills);
                    c.combinedScore= clamp(W_BASE*c.scorePer +W_SEMANTIC *semanticScore+W_SKILL *skillOverlap,0.0,100.0);
                    c.semanticScore= Math.round(semanticScore * 100.0) / 100.0;
                    c.skillOverlap= Math.round(skillOverlap * 100.0) / 100.0;
                    filtered.add(c);
                }
            }
            filtered.sort((a,b)->{
                int comp= Double.compare(b.combinedScore,a.combinedScore);            
                if(comp!=0) return comp;
                comp= Double.compare(b.experienceYears,a.experienceYears);
                if(comp!=0) return comp;
                return Double.compare(a.careerGapYears,b.careerGapYears);                
            });
            int total = filtered.size();
            int rank =1;
            for(CdResult c:filtered){
                c.rank = rank++;
                c.tc = total;
            }
            String outSafe = "ranked_candidates_"+safeFilename(jobIdSafe)+".json";
            writeResults(filtered,outSafe);
            System.out.println("Ranked file saved: "+outSafe);
        }
    }
    private static double clamp(double v,double lo,double hi){
        return Math.max(lo,Math.min(hi,v));
    }  
    private static String safeFilename(String s){
        if(s==null) return "unknown";
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
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
    private static Map<String,String> loadResumeTexts(String analysisFile){
        Map<String,String> map= new HashMap<>();
        try(Reader reader= new FileReader(analysisFile)){
            Gson gson= new Gson();
            Type listType= new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String,Object>> resumes= gson.fromJson(reader,listType);
            if(resumes!= null){
                for(Map<String,Object> res:resumes) {
                    String name= safe(res.get("Name")).toLowerCase();
                    String text= safe(res.get("RawText"));
                    map.put(name,text);
                }
            }
        } 
        catch(Exception e){
            System.out.println("Error loading analysis_output.json: "+e.getMessage());
        }
        return map;
    }
    private static Map<String,String> loadJobDescriptions(String jdFile){
        Map<String,String> map= new HashMap<>();
        try(Reader reader= new FileReader(jdFile)){
            Gson gson= new Gson();
            Type listType= new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String,Object>> jobs= gson.fromJson(reader,listType);
            if(jobs!=null){
                for(Map<String,Object> j:jobs){
                    String id= safe(j.get("id"));
                    String title= safe(j.get("title"));
                    String desc= safe(j.get("description"));
                    if(!id.isEmpty()) 
                        map.put(id, desc);
                    if (!title.isEmpty()) 
                        map.put(title, desc);
                }
            }
        } 
        catch(Exception e){
            System.out.println("Error loading jd_input.json: " + e.getMessage());
        }
        return map;
    }
    private static double calculateSkillOverlap(List<String> resumeSkills,List<String> jdSkills){
        if(jdSkills==null ||jdSkills.isEmpty()) 
            return 0.0;
        Set<String> rs= new HashSet<>();
        for(String s:resumeSkills) 
            rs.add(s.toLowerCase());
        int common= 0;
        for(String s:jdSkills)
            if(rs.contains(s.toLowerCase())) 
                common++;
        return ((double)common/jdSkills.size())*100.0;
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
    private static String safe(Object o){
        return o == null ? "" : o.toString();
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
        double combinedScore;
        double semanticScore;
        double skillOverlap;
        int rank;
        int tc;
    }
}

