package com.airesume;
import java.util.*;
import java.util.regex.*;
public class RegexAnalyzer{    
    private static final Map<String, Double> wordToNum= Map.ofEntries(
        Map.entry("one", 1.0),Map.entry("two", 2.0),Map.entry("three", 3.0),
        Map.entry("four", 4.0),Map.entry("five", 5.0),Map.entry("six", 6.0),
        Map.entry("seven", 7.0),Map.entry("eight", 8.0),Map.entry("nine", 9.0),
        Map.entry("ten", 10.0),Map.entry("eleven", 11.0),Map.entry("twelve", 12.0),
        Map.entry("half", 0.5)
    );
    public static String extractName(String text){
        Pattern nameRegex= Pattern.compile("(?i)\\b([A-Z][A-Za-z'.-]+(?:\\s+[A-Z][A-Za-z'.-]+)+)\\b");
        Matcher matcher= nameRegex.matcher(text);
        if(matcher.find())
         return matcher.group(1);
        return "Unknown";
    }
    public static String extractEmail(String text){
        String emailRegex="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        Pattern pattern=Pattern.compile(emailRegex);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find())
            return matcher.group(); 
        return null; 
    }
    public static String extractPhone(String text){
        String phoneRegex="(?:\\+91[-\\s]?)?\\d{10}";
        Pattern pattern=Pattern.compile(phoneRegex);
        Matcher matcher=pattern.matcher(text);
        if(matcher.find())
            return matcher.group(); 
        return null;
    }
    public static String extractGender(String text) {
        text= text.toLowerCase();
        if(text.matches(".*\\b(she|her|hers|mrs\\.?|ms\\.?|madam|female)\\b.*") || text.matches(".*gender[:\\s-]*(female).*"))
            return "Female";
        if(text.matches(".*\\b(he|him|his|mr\\.?|sir|male)\\b.*") || text.matches(".*gender[:\\s-]*(male).*")) 
            return "Male";
        return "Not Specified";
    }
    public static List<String> extractBias(String text){
        List<String> indicators= new ArrayList<>();
        String[] biasWords = {
        "young","energetic","dynamic","aggressive","female-only","male-only","male-dominated",
        "mature","fast-paced","fresh graduate","leadership","youthful","age limit",
        "under 30","over 40","team player","adaptable","single","outgoing"
        };
        text= text.toLowerCase();
        for(String w:biasWords) 
            if(text.contains(w)) 
                indicators.add(w);
        return indicators;
    }
    public static List<String> extractSkills(String text){
        List<String> skills=new ArrayList<>();
        String skillsRegex="(?i)\\b("+
        "java|python|c\\+\\+|c#|javascript|typescript|html|css|react|angular|node(?:\\.js)?|" +
        "express|spring|django|flask|dotnet|php|ruby|go|swift|kotlin|" +
        "sql|mysql|postgresql|mongodb|oracle|firebase|nosql|" +
        "aws|azure|gcp|docker|kubernetes|jenkins|git|github|jira|agile|scrum|" +
        "tensorflow|pytorch|machine learning|data analysis|powerbi|tableau|" +
        "excel|nlp|artificial intelligence|ai|deep learning|cloud computing" +
        ")\\b";
        Pattern pattern = Pattern.compile(skillsRegex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) 
            skills.add(matcher.group().trim());
        return new ArrayList<>(new LinkedHashSet<>(skills));
    }
    public static String extractJobRole(String text){
        String applyRegex= "(?i)(applying for|seeking|looking for|desired role|position applied for|interested in)[:\\s-]*([A-Za-z /&]+)";
        Matcher applyMatcher= Pattern.compile(applyRegex).matcher(text);
        if(applyMatcher.find()) 
            return applyMatcher.group(2).trim();
        String roleRegex= "(?i)(worked as|serving as|currently|previously)[:\\s-]*([A-Za-z /&]+?)(?=\\bfor|\\bwith|\\bat|\\.|,|$)";
        Matcher roleMatcher= Pattern.compile(roleRegex).matcher(text);
        if(roleMatcher.find())
            return roleMatcher.group(2).trim();
        String fallbackRegex= "(?i)(developer|engineer|manager|analyst|intern|designer|consultant|administrator|specialist|architect|coordinator|executive|officer|scientist|technician|advisor|lead|head)";
        Matcher matcher= Pattern.compile(fallbackRegex).matcher(text);
        if(matcher.find())
            return matcher.group();
        return "Not Specified";
    }
    public static List<Map<String,Object>> extractExperience(String text) {
        List<Map<String,Object>> expList= new ArrayList<>();
        String expRegex= "(?i)(?:at|in|for|with)\\s*([A-Z][A-Za-z0-9&\\s]+)\\s*(?:as|role|position|working as)?\\s*([A-Za-z\\s]+)?\\s*(?:for)?\\s*(\\d+(?:\\.\\d+)?)\\s*(years?|yrs?|months|month)?";
        Pattern pattern= Pattern.compile(expRegex);
        Matcher matcher= pattern.matcher(text);
        while(matcher.find()){
            Map<String,Object> exp= new LinkedHashMap<>();
            exp.put("company",matcher.group(1).trim());
            exp.put("role",matcher.group(2)!= null ? matcher.group(2).trim():"N/A");
            double yrs= 0;
            if(matcher.group(3)!= null){
                yrs= Double.parseDouble(matcher.group(3));
                String unit= matcher.group(4)!= null ? matcher.group(4).toLowerCase():"";
                if(unit.contains("month"))
                    yrs/= 12; 
            }
            exp.put("years",yrs);
            expList.add(exp);
        }
        String rangeRegex= "(?i)(?:from)?\\s*(\\d{4})\\s*(?:to|-|–)\\s*(\\d{4})";
        Matcher range= Pattern.compile(rangeRegex).matcher(text);
        while(range.find()){
            double yrs= Double.parseDouble(range.group(2))-Double.parseDouble(range.group(1));
            Map<String,Object>exp =new LinkedHashMap<>();
            exp.put("company", "Unknown");
            exp.put("role", "Unknown");
            exp.put("years",yrs);
            expList.add(exp);
        }
        return expList;
    }           
    public static int extractCareerGap(String text){
        double tg= 0;
        String gapRegex="(?i)(gap|career break).*?(\\d+(?:\\.\\d+)?|one|two|three|four|five|six|seven|eight|nine|ten|half)\\s*(years?|yrs?|months?|month)";        Pattern pattern=Pattern.compile(gapRegex);
        Matcher matcher=pattern.matcher(text);
        while(matcher.find()){
            String val= matcher.group(2).toLowerCase();
            double num = 0;
            if(wordToNum.containsKey(val)) 
                num= wordToNum.get(val);
            else{
            try{num= Double.parseDouble(val);}
            catch(Exception ignored){}
            }
            if(matcher.group(3) != null && matcher.group(3).toLowerCase().contains("month"))
                num/= 12.0;
            tg+= num;
        }
        return (int)Math.round(tg);
    }
    public static String extractSummary(String text){
        String[] lines= text.split("\\.\\s+");
        return lines.length>0 ? lines[0].trim():text.substring(0,Math.min(text.length(), 120));
    }
    public static Map<String,Object> analyze(String resumeText){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("Name",extractName(resumeText));
        result.put("Email",extractEmail(resumeText));
        result.put("Phone",extractPhone(resumeText));
        result.put("Gender",extractGender(resumeText));
        result.put("Bias Indicators",extractBias(resumeText));
        result.put("Skills",extractSkills(resumeText));
        result.put("Job Role",extractJobRole(resumeText));
        List<Map<String,Object>> expList= extractExperience(resumeText);
        double yrs= 0.0;
        for(Map<String,Object> exp:expList){
            Object yrsObj= exp.get("years");
            if(yrsObj instanceof Number)
                yrs+= ((Number)yrsObj).doubleValue();
        }
        result.put("Experience",extractExperience(resumeText));
        result.put("Total Experience",Math.round(yrs*10.0)/10.0);
        result.put("Career Gap",extractCareerGap(resumeText));
        result.put("Summary",extractSummary(resumeText));
        return result;
    }
}

    
