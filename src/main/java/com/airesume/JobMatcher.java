package com.airesume;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.regex.Pattern;
import java.util.*;
public class JobMatcher{
    private static final double PENALTY_MY=5.0;
    private static final double PENALTY_GY=3.0;
    private static final double MIN_SCORE=0.0;
    private static final double MAX_SCORE=100.0;

    public static void main(String[] args){
        String analysisFile="analysis_output.json";
        String jdFile="jd_input.json";
        String outFile="eligibility_results.json";        
        List<Map<String,Object>> resumes=readResumes(analysisFile);
        List<Job> jobs=readJobs(jdFile);
        if(resumes.isEmpty()){
            System.out.println("No resumes loaded from: "+analysisFile);
            writeResults(Collections.emptyList(),outFile);
            return;
        }
        if(jobs.isEmpty()){
            System.out.println("No job descriptions loaded from: "+jdFile);
            writeResults(Collections.emptyList(),outFile);
            return;
        }
        List<JobResults> jResults=new ArrayList<>();
        for(Job job:jobs) {
            List<CandidateResult> results=new ArrayList<>();
            for(Map<String,Object> resume:resumes){
                CandidateResult r = calScore(resume,job);
                results.add(r);
            }
            results.sort((a,b)->Double.compare(b.scorePer,a.scorePer));
            JobResults jr=new JobResults(job.id!=null ? job.id:"N/A",job.title!=null ? job.title:"Unknown",results);
            jResults.add(jr);
            System.out.printf("Job '%s' → %d candidates scored.%n",job.title,results.size());
        }
        writeResults(jResults,outFile);
        System.out.println("Eligibility results saved to: "+outFile);
    }
    private static CandidateResult calScore(Map<String,Object> resume,Job job){
        String name=safeString(resume.get("Name"));
        String email=safeString(resume.get("Email"));        
        String resumeText= safeString(resume.get("RawText"));
        Set<String> candiSk=new HashSet<>();
        Object skillsObj=resume.get("Skills");
        if(skillsObj instanceof List<?>){
            for(Object s:(List<?>) skillsObj){
                if(s!=null) 
                    candiSk.add(s.toString().trim().toLowerCase());
            }
        } 
        else if(skillsObj!=null){
            candiSk.add(skillsObj.toString().trim().toLowerCase());
        }
        candiSk.addAll(NLPUtils.findSkills(resumeText).stream().map(String::toLowerCase).toList());        
        double expYears=parseNumber(resume.get("Total Experience Years"));
        double gap=parseNumber(resume.get("Career Gap"));
        if(!resume.containsKey("Total Experience Years"))
            System.out.println("Missing experience for: "+name);
        if(!resume.containsKey("Career Gap"))
            System.out.println("Missing career gap for: "+name);
        List<String> required=job.requiredSkills!=null?job.requiredSkills:Collections.emptyList();
        int reqCount=required.size();
        Set<String> matched=new LinkedHashSet<>();
        for(String req:required){
            if (req==null) 
                continue;
            String nreq=req.toLowerCase().trim();
            for(String skill:candiSk){
                if(skill.equalsIgnoreCase(nreq) || skill.matches(".*\\b"+Pattern.quote(nreq)+"\\b.*")){
                    matched.add(req);
                    break;
                }
            }
        }
        int mCount=matched.size();
        double baseScore= reqCount==0?0.0:((double)mCount/reqCount)*100.0;
        List<String> nlpRoles= NLPUtils.findJobTitles(resumeText);
        if(nlpRoles.stream().anyMatch(r -> r.equalsIgnoreCase(job.title))) 
            baseScore+= 5.0;
        double penaltyExp=0.0;
        boolean meetsExp=true;
        if(job.minExperience>0 && expYears<job.minExperience){
            double gapYears=job.minExperience-expYears;
            penaltyExp=Math.min(30.0,gapYears*PENALTY_MY);
            meetsExp=false;
        }
        double penaltyGap=Math.min(20.0,gap*PENALTY_GY);
        double raw=baseScore-penaltyExp-penaltyGap;
        double score=Math.max(MIN_SCORE,Math.min(MAX_SCORE,raw));
        CandidateResult res=new CandidateResult();
        res.name=name;
        res.email=email;
        res.matchedSkills=new ArrayList<>(matched);
        res.matchedCount=mCount;
        res.requiredCount=reqCount;
        res.experienceYears=expYears;
        res.careerGapYears=gap;
        res.meetsExperience=meetsExp;
        res.scorePer=Math.round(score*100.0)/100.0;
        if(res.scorePer >=80) 
            res.eligibility= "Highly Eligible";
        else if(res.scorePer >=50) 
            res.eligibility= "Moderately Eligible";
        else 
            res.eligibility= "Low Fit";
        return res;
    }

    private static List<Map<String, Object>> readResumes(String filePath){
        Gson gson=new GsonBuilder().create();
        try(FileReader reader=new FileReader(filePath)){
            Type t=new TypeToken<List<Map<String,Object>>>() {}.getType();
            List<Map<String, Object>> list=gson.fromJson(reader,t);
            if(list==null) 
                return Collections.emptyList();
            System.out.println(list.size()+" resumes loaded from: "+filePath);
            return list;
        } 
        catch(IOException e){
            System.out.println("Could not read resumes from: "+filePath);
            return Collections.emptyList();
        }
    }
    private static List<Job> readJobs(String filePath){
        Gson gson=new GsonBuilder().create();
        try(FileReader reader=new FileReader(filePath)){
            Type t=new TypeToken<List<Job>>() {}.getType();
            List<Job> list=gson.fromJson(reader,t);
            if(list==null) 
                return Collections.emptyList();
            System.out.println(list.size()+" job(s) loaded from: "+filePath);
            return list;
        } 
        catch(IOException e){
            System.out.println("Could not read jobs from: "+filePath);
            return Collections.emptyList();
        }
    }
    private static void writeResults(Object data, String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.out.println("Error writing results: " + e.getMessage());
        }
    }
    private static String safeString(Object o) {
        return o== null ? "":o.toString();
    }
    private static double parseNumber(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ex) {
            return 0.0;
        }
    }
    static class Job {
        String id;
        String title;
        List<String> requiredSkills;
        int minExperience;
    }
    static class CandidateResult {
        String name;
        String email;
        String eligibility;
        List<String> matchedSkills;
        int matchedCount;
        int requiredCount;
        double experienceYears;
        double careerGapYears;
        boolean meetsExperience;
        double scorePer;
    }
    static class JobResults{
        String jobId;
        String title;
        List<CandidateResult> candidates;
        JobResults(String jobId,String title,List<CandidateResult> candidates) {
            this.jobId=jobId;
            this.title=title;
            this.candidates=candidates;
        }
    }
}
