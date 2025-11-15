package com.airesume;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
public class SkillGap{   
    public static void main(String[] args){
        try{
            JsonArray resumes= JsonParser.parseReader(new FileReader("analysis_output.json")).getAsJsonArray();
            JsonArray jobs= JsonParser.parseReader(new FileReader("jd_input.json")).getAsJsonArray();
            JsonArray op= new JsonArray();
            for(JsonElement r:resumes){
                JsonObject res= r.getAsJsonObject();
                String name= res.has("Name") ? res.get("Name").getAsString():res.has("name") ? res.get("name").getAsString():"Unknown"; 
                Set<String> cdSkills= new HashSet<>();
                if(res.has("Skills") && res.get("Skills").isJsonArray()){
                    for(JsonElement s: res.getAsJsonArray("Skills")) 
                        cdSkills.add(s.getAsString().toLowerCase());
                }
                String resumeText= res.has("RawText") ? res.get("RawText").getAsString() : "";
                cdSkills.addAll(NLPUtils.findSkills(resumeText).stream().map(String::toLowerCase).collect(Collectors.toList()));                
                for(JsonElement j:jobs){
                    JsonObject job= j.getAsJsonObject();
                    String jobTitle= job.has("title") ?job.get("title").getAsString():job.has("Job title") ? job.get("Job title").getAsString() :"Unknown";
                    List<String> missSkills= new ArrayList<>();
                    if(job.has("requiredSkills") && job.get("requiredSkills").isJsonArray()){
                        JsonArray reqSkills= job.getAsJsonArray("requiredSkills");
                        for(JsonElement req:reqSkills){
                            String skill=req.getAsString().toLowerCase();
                            if(!cdSkills.contains(skill))
                                missSkills.add(req.getAsString());
                        }
                    }
                    if (job.has("description")){
                        String jdText= job.get("description").getAsString();
                        List<String> jdSkills= NLPUtils.findSkills(jdText);
                        for(String jdSkill:jdSkills){
                            if (!cdSkills.contains(jdSkill.toLowerCase()))
                                missSkills.add(jdSkill);
                        }
                    }
                    if(!missSkills.isEmpty()){
                        JsonObject result=new JsonObject();
                        result.addProperty("name",name);
                        result.addProperty("jobTitle",jobTitle);
                        result.add("missingSkills",new Gson().toJsonTree(missSkills));
                        result.add("candidateSkills",new Gson().toJsonTree(cdSkills));
                        op.add(result);
                    }
                }
            }
            try(Writer writer= new FileWriter("skill_gap.json")){
                new GsonBuilder().setPrettyPrinting().create().toJson(op,writer);
            }
            System.out.println("skill_gap.json generated successfully!");
            } 
            catch(Exception e){
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        }
    }
}

