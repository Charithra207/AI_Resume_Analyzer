package com.airesume;
import java.util.*;
import java.util.regex.*;
public class RegexAnalyzer{
    public static String extractEmail(String text){
        String emailRegex="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        Pattern pattern=Pattern.compile(emailRegex);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find()){
            return matcher.group(); 
        }
        return null; 
    }
    public static String extractPhone(String text){
        String phoneRegex="(?:\\+91[-\\s]?)?\\d{10}";
        Pattern pattern=Pattern.compile(phoneRegex);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find()){
            return matcher.group(); 
        }
        return null;
    }
    public static List<String> extractSkills(String text){
        List<String> skills=new ArrayList<>();
        String skillsRegex="(?i)(java|python|sql|docker|spring|c\\+\\+|c#|javascript|html|css|react|node)";
        Pattern pattern=Pattern.compile(skillsRegex);
        Matcher matcher=pattern.matcher(text);
        while(matcher.find()){
            skills.add(matcher.group());
        }
        return skills;
    }
    public static String extractJobRole(String text) {
        String jobRegex="(?i)(developer|engineer|manager|analyst|intern|designer|consultant)";
        Pattern pattern=Pattern.compile(jobRegex);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find()){
            return matcher.group();
        }
        return null;
    }
    public static int extractExperience(String text){
        String expRegex="(\\d+)\\s*(years|yrs|year)";
        Pattern pattern=Pattern.compile(expRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find()){
            return Integer.parseInt(matcher.group(1));
        }
        return 0; 
    }
    public static int extractCareerGap(String text){
        String gapRegex="(?i)(gap|career break).*?(\\d+)\\s*(years|yrs|year|months|month)";
        Pattern pattern=Pattern.compile(gapRegex);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find()){
            return Integer.parseInt(matcher.group(2));
        }
        return 0;
    }
    public static Map<String,Object> analyze(String resumeText){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("Email",extractEmail(resumeText));
        result.put("Phone",extractPhone(resumeText));
        result.put("Skills",extractSkills(resumeText));
        result.put("Job Role",extractJobRole(resumeText));
        result.put("Experience (Years)",extractExperience(resumeText));
        result.put("Career Gap",extractCareerGap(resumeText));
        return result;
    }
}

    
