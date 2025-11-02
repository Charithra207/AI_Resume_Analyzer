package com.airesume;
import java.util.*;
import java.util.regex.*;
public class RegexAnalyzer{    
    protected static final Map<String,Double> wordToNum;
    static {
    wordToNum= new HashMap<>();
    wordToNum.put("one", 1.0);
    wordToNum.put("two", 2.0);
    wordToNum.put("three", 3.0);
    wordToNum.put("four", 4.0);
    wordToNum.put("five", 5.0);
    wordToNum.put("six", 6.0);
    wordToNum.put("seven", 7.0);
    wordToNum.put("eight", 8.0);
    wordToNum.put("nine", 9.0);
    wordToNum.put("ten", 10.0);
    wordToNum.put("eleven", 11.0);
    wordToNum.put("twelve", 12.0);
    wordToNum.put("half", 0.5);
    }
    public static String extractName(String text,NLPUtils nlp){        
        if(nlp!=null){
            List<String> nlpNames= NLPUtils.findPersons(text);
            if(nlpNames!=null && !nlpNames.isEmpty()) 
                return nlpNames.get(0).trim();
        }
        Pattern nameRegex= Pattern.compile("(?im)(?:^|\\b)(?:name\\s*[:\\-]\\s*)?"+"([A-Z][A-Za-z\\.]{0,20}"+"(?:\\s+[A-Z][A-Za-z\\.]{0,20}){0,3})"+"(?=\\s*(?:$|\\n|,|\\.|\\b(?:email|phone|mobile|contact|address)\\b))");
        Matcher matcher= nameRegex.matcher(text);
        if(matcher.find())
         return matcher.group(1).trim();
        return "Unknown";
    }
    public static String extractEmail(String text){
        if(text==null) return "";
        String emailRegex="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        Matcher matcher= Pattern.compile(emailRegex).matcher(text);
        return matcher.find() ? matcher.group().trim() :"";
    }
    public static String extractPhone(String text){
        if(text==null) return "";
        String phoneRegex="(?:\\+\\d{1,3}[-\\s]?)?(?:\\d{10,13}|\\(\\d{3}\\)\\s*\\d{3}[-\\s]\\d{4}|\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4})";
        Matcher matcher= Pattern.compile(phoneRegex).matcher(text);
        if(matcher.find()){
            String raw= matcher.group().replaceAll("[^+0-9]", "");
            if(!raw.startsWith("+") && raw.length()>10)
                raw="+"+raw;        
            return raw; 
        }
        return "";
    }
    public static String extractGender(String text) {
        if(text==null) return "Not Specified";
        text= text.toLowerCase();
        if(text.matches(".*\\b(she|her|hers|mrs\\.?|ms\\.?|madam|female)\\b.*") || text.matches(".*gender[:\\s-]*(female).*"))
            return "Female";
        if(text.matches(".*\\b(he|him|his|mr\\.?|sir|male)\\b.*") || text.matches(".*gender[:\\s-]*(male).*")) 
            return "Male";
        return "Not Specified";
    }
    public static List<String> extractBias(String text){
        List<String> list= new ArrayList<>();
        if(text==null) return list;
        String[] biasWords = {
        "young","energetic","dynamic","aggressive","female-only","male-only","male-dominated",
        "mature","fast-paced","fresh graduate","leadership","youthful","age limit",
        "under 30","over 40","team player","adaptable","single","outgoing"
        };
        text= text.toLowerCase();
        for(String w:biasWords) 
            if(text.contains(w)) 
                list.add(w);
        return list;
    }
    public static List<String> extractSkills(String text,NLPUtils nlp){        
        Set<String> skills= new LinkedHashSet<>();
        if(text==null) return new ArrayList<>(skills);
        if(nlp != null){
            List<String> nlpSkills= NLPUtils.findSkills(text);
            if(nlpSkills!=null)
                skills.addAll(nlpSkills);
        }
        String skillsRegex="(?i)\\b("+
        "java|python|c\\+\\+|c#|javascript|typescript|html|css|react|angular|node(?:\\.js)?|" +
        "express|spring|django|flask|dotnet|php|ruby|go|swift|kotlin|" +
        "sql|mysql|postgresql|mongodb|oracle|firebase|nosql|" +
        "aws|azure|gcp|docker|kubernetes|jenkins|git|github|jira|agile|scrum|" +
        "tensorflow|pytorch|machine learning|data analysis|powerbi|tableau|" +
        "excel|nlp|artificial intelligence|ai|deep learning|cloud computing" +
        ")\\b";
        Matcher matcher= Pattern.compile(skillsRegex).matcher(text);
        while (matcher.find()) 
            skills.add(matcher.group().trim());
        return new ArrayList<>(skills);
    }
    public static String extractJobRole(String text,NLPUtils nlp){  
        if(text==null) return "Not Specified";   
        if(nlp!=null){ 
            List<String> nlpRoles= NLPUtils.findJobTitles(text);
            if(nlpRoles!=null && !nlpRoles.isEmpty())
                return nlpRoles.get(0).trim();
        }
        String[] regexes={"(?i)(applying for|seeking|looking for|desired role|position applied for|interested in)[:\\s-]*([A-Za-z /&]+)",
        "(?i)(worked as|serving as|currently|previously)[:\\s-]*([A-Za-z /&]+?)(?=\\bfor|\\bwith|\\bat|\\.|,|$)",
        "(?i)(developer|engineer|manager|analyst|intern|designer|consultant|administrator|specialist|architect|coordinator|executive|officer|scientist|technician|advisor|lead|head)"};
        for(String r:regexes){
            Matcher m= Pattern.compile(r).matcher(text);
            if(m.find()){
                if(m.groupCount() >= 2) 
                    return m.group(2).trim();
                return m.group(1).trim();
            }
        }
        return "Not Specified";
    }
    public static List<Map<String,String>> extractEducation(String text){
        List<Map<String,String>> eduList= new ArrayList<>();
        if(text==null) return eduList;
        String eduRegex ="(?i)(b\\.tech|btech|be|b\\.e\\.?|m\\.tech|mtech|me|m\\.e\\.?|mba|bsc|msc|b\\.sc\\.?|m\\.sc\\.?|phd|bca|mca|diploma|associate degree|bachelor|master|doctorate|engineering|science|commerce|arts|technology|computer application)[^\\n,;.]*";
        Matcher matcher= Pattern.compile(eduRegex).matcher(text);
        while(matcher.find()){
            String degreeLine= matcher.group().trim();
            String degree="";
            String university="";
            String year="";
            Matcher degreeMatcher= Pattern.compile("(?i)(b\\.tech|btech|be|m\\.tech|mtech|me|mba|bsc|msc|phd|bca|mca|diploma)").matcher(degreeLine);
            if(degreeMatcher.find()) 
                degree= degreeMatcher.group().toUpperCase();
            Matcher universityMatcher= Pattern.compile("(?i)([^\\n,;]*\\b(university|college|institute|iit|nit|iiit|vit|bits|anna university|amity|ignou|osmania|delhi university)[^,;\\n]*)").matcher(degreeLine);
            if(universityMatcher.find()) 
                university= universityMatcher.group().trim();
            Matcher yearMatcher= Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(degreeLine);
            if(yearMatcher.find()) 
                year= yearMatcher.group();
            Map<String,String> edu= new LinkedHashMap<>();
            edu.put("degree",degree.isEmpty() ? "Unknown": degree);
            edu.put("university",university.isEmpty() ? "Unknown": university);
            edu.put("year",year.isEmpty() ? "Unknown" :year);
            eduList.add(edu);
        }
        return eduList;
    }   
    public static Map<String,Object> extractExperience(String text){                         
        Map<String,Object> result= new LinkedHashMap<>();           
        List<Map<String,String>> expList= new ArrayList<>();
        double tyrs= 0.0;
        if(text==null){
            result.put("experience_details",expList);
            result.put("total_experience", "Unknown");
            return result;
        }
        List<String> comp= NLPUtils.findOrganizations(text);
        List<String> roles= NLPUtils.findJobTitles(text); 
        String[] regexes={"(?i)(\\d{4})\\s*(?:-|to|–|—)\\s*(\\d{4}|present|current)",
        "(?i)(\\d{1,2})/(\\d{4})\\s*(?:-|to|–|—)\\s*(\\d{1,2})/(\\d{4}|present|current)",
        "(?i)for\\s+(\\d+(?:\\.\\d+)?)\\s*(years?|months?)",
        "(?i)(\\d+(?:\\.\\d+)?)\\s*(years?|months?)\\s+(?:experience|exp)"};
        Pattern comb= Pattern.compile(String.join("|",regexes));
        Matcher m= comb.matcher(text);
        List<String> dur= new ArrayList<>();
        while(m.find()) 
            dur.add(m.group().trim());
        int max= Math.max(comp.size(),Math.max(roles.size(),dur.size()));
        if(max==0){
            result.put("experience_details",expList);
            result.put("total_experience", "Unknown");
            return result;
        }
        for(int i=0;i<max;i++){
            String c= i< comp.size() ? comp.get(i) :"Unknown Company";
            String r= i< roles.size() ? roles.get(i) :"Unknown Role";
            String d= i< dur.size() ? dur.get(i) :"";
            double yrs= calDuration(d);
            if(yrs>0) 
                tyrs+=yrs;
            String durStr =formatDuration(yrs);
            Map<String,String> expEntry= new LinkedHashMap<>();
            expEntry.put("company",c);
            expEntry.put("role",r);
            expEntry.put("duration",durStr);
            expEntry.put("raw_duration_text",d== null ?"": d);
            expList.add(expEntry);
        }        
        result.put("experience_details",expList);
        result.put("total_experience",formatDuration(tyrs));
        return result;        
    }  
    private static double calDuration(String text){
        if(text==null || text.isEmpty()) 
            return 0.0;
        text= text.toLowerCase();
        Matcher m1= Pattern.compile("for\\s+(\\d+(?:\\.\\d+)?)\\s*(years?|months?)").matcher(text);
        if(m1.find()){
            try{
                double value= Double.parseDouble(m1.group(1));
                return m1.group(2).startsWith("month") ? value/12.0: value;
            }
            catch(Exception ignore){}
        }
        Matcher m1b= Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(years?|months?)\\s+(?:experience|exp)").matcher(text);
        if(m1b.find()){
            try{
                double value= Double.parseDouble(m1b.group(1));
                return m1b.group(2).startsWith("month")? value/12.0 :value;
            } 
            catch(Exception ignored){}
        }
        Matcher m2= Pattern.compile("(\\d{4})\\s*(?:-|to|–|—)\\s*(\\d{4}|present|current)").matcher(text);
        if(m2.find()){
            try{
                int start= Integer.parseInt(m2.group(1));
                int end= Calendar.getInstance().get(Calendar.YEAR);
                String g2= m2.group(2);
                if(g2!=null && !g2.matches("present|current")) 
                    end= Integer.parseInt(g2);
                return Math.max(0,end-start);
            } 
            catch(Exception ignored) {} 
        }
        Matcher m3= Pattern.compile("(\\d{1,2})/(\\d{4})\\s*(?:-|to|–|—)\\s*(\\d{1,2})/(\\d{4}|present|current)").matcher(text);
        if(m3.find()){
            try{
                int ys= Integer.parseInt(m3.group(2));
                String g4= m3.group(4);
                int ye= Calendar.getInstance().get(Calendar.YEAR);
                if(g4!=null && !g4.matches("present|current")) 
                    ye= Integer.parseInt(g4);
                int ms= Integer.parseInt(m3.group(1));
                int me= Integer.parseInt(m3.group(3));                
                int months=(ye-ys)*12+(me-ms);
                return Math.max(0,months/12.0);
            } 
            catch(Exception ignored) {}
        }  
        Matcher m4= Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(years?|months?)").matcher(text);
        if(m4.find()){
            try{
                double val= Double.parseDouble(m4.group(1));
                return m4.group(2).startsWith("month") ?val/12.0 :val;
            } 
            catch(Exception ignored) {}
        }
        return 0.0;
    }
    private static String formatDuration(double yrs){
        if(yrs==0) 
            return "Unknown";
        int y= (int) yrs;
        int m= (int) Math.round((yrs-y)*12);
        if(y>0 && m>0) 
            return y+" years "+m+" months";
        if(y>0)
            return y+" years";
        return m+" months";
    }  
    public static int extractCareerGap(String text){
        if(text==null) return 0;
        double tg= 0;
        String gapRegex="(?i)(gap|career break).*?(\\d+(?:\\.\\d+)?|one|two|three|four|five|six|seven|eight|nine|ten|half)\\s*(years?|yrs?|months?|month)";        
        Matcher matcher=Pattern.compile(gapRegex).matcher(text);
        while(matcher.find()){
            String val= matcher.group(2).toLowerCase();
            double num =wordToNum.getOrDefault(val, 0.0);
            if(num==0){
                try{num= Double.parseDouble(val);}
                catch(Exception ignored){}
            }
            if(matcher.group(3) != null && matcher.group(3).toLowerCase().contains("month"))
                num/= 12.0;
            tg+= num;
        }
        return (int)Math.round(tg);
    }    
    public static Map<String,Object> analyze(String resumeText,NLPUtils nlp){
        Map<String,Object> result=new LinkedHashMap<>();
        try{
            if(resumeText==null || resumeText.trim().isEmpty()){
                result.put("Error", "Empty resume text");
                return result;
            }             
            result.put("Name",extractName(resumeText,nlp));
            result.put("Email",extractEmail(resumeText));
            result.put("Phone",extractPhone(resumeText));
            result.put("Gender",extractGender(resumeText));
            result.put("Bias Indicators",extractBias(resumeText));
            result.put("Skills",extractSkills(resumeText,nlp));
            result.put("Job Role",extractJobRole(resumeText,nlp));
            result.put("Education",extractEducation(resumeText));
            Map<String,Object> expResult= extractExperience(resumeText);
            result.put("Experience",expResult.get("experience_details"));
            result.put("Total Experience",expResult.get("total_experience"));
            result.put("Career Gap",extractCareerGap(resumeText));
        }
        catch(Exception e){
            e.printStackTrace();
            result.put("Error","Analysis failed: "+e.getMessage());
        }
        return result;
    }
}

    
