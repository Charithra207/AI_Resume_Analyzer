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
        List<String> nlpNames= NLPUtils.findPersons(text);
        if(!nlpNames.isEmpty()) 
            return nlpNames.get(0).trim();

        Pattern nameRegex= Pattern.compile("(?im)(?:^|\\b)(?:name\\s*[:\\-]\\s*)?"+"([A-Z][A-Za-z\\.]{0,20}"+"(?:\\s+[A-Z][A-Za-z\\.]{0,20}){0,3})"+"(?=\\s*(?:$|\\n|,|\\.|\\b(?:email|phone|mobile|contact|address)\\b))");
        Matcher matcher= nameRegex.matcher(text);
        if(matcher.find())
         return matcher.group(1).trim();
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
    public static List<String> extractSkills(String text,NLPUtils nlp){        
        Set<String> skills= new LinkedHashSet<>();
        List<String> nlpSkills = NLPUtils.findSkills(text);
        if(nlpSkills != null) 
            skills.addAll(nlpSkills);
        
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
        return new ArrayList<>(skills);
    }
    public static String extractJobRole(String text,NLPUtils nlp){        
        List<String> nlpRoles= NLPUtils.findJobTitles(text);
        if(nlpRoles != null && !nlpRoles.isEmpty()) 
            return nlpRoles.get(0).trim();
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
    public static List<Map<String,Object>> extractExperience(String text,NLPUtils nlp) {       
        List<Map<String,Object>> expList= new ArrayList<>();               
        List<String> nlpOrgs= NLPUtils.findOrganizations(text);
        List<String> nlpRoles= NLPUtils.findJobTitles(text);
        List<String> nlpDates= NLPUtils.findDates(text);
        if(nlpOrgs != null){
            for(int i=0;i<nlpOrgs.size();i++){
                Map<String,Object> exp= new LinkedHashMap<>();
                String company= nlpOrgs.get(i).trim();
                String role= (nlpRoles != null && i<nlpRoles.size()) ? nlpRoles.get(i).trim():"Unknown";
                double years= 0.0;
                if(nlpDates != null && i<nlpDates.size()) 
                    years= parseDateRangeToYears(nlpDates.get(i));
                exp.put("company",company);
                exp.put("role",role);
                exp.put("years",years);
                expList.add(exp);
            }
        }
    
        String expRegex1=  "(?i)(?:worked|serving|employed|experience|job|role).*?(?:at|in|for|with)\\s*([A-Z][A-Za-z0-9&\\s]+)\\s*(?:as|role|position|working as)?\\s*([A-Za-z\\s]+)?\\s*(?:for)?\\s*(\\d+(?:\\.\\d+)?|one|two|three|four|five|six|seven|eight|nine|ten|half)\\s*(years?|yrs?|months?)?";
        Matcher matcher1= Pattern.compile(expRegex1).matcher(text);
        while(matcher1.find()){
            Map<String,Object> exp= new LinkedHashMap<>();
            exp.put("company",matcher1.group(1).trim());
            exp.put("role",matcher1.group(2)!= null ? matcher1.group(2).trim():"Unknown");
            exp.put("years",parseExperienceValue(matcher1.group(3),matcher1.group(4)));
            expList.add(exp);
        }
        String expRegex2= "(?i)(?:experience|worked|serving|employed)[:\\s-]*([0-9]+(?:\\.[0-9]+)?|one|two|three|four|five|six|seven|eight|nine|ten|half)\\s*(years?|yrs?|months?)?\\s*(?:at|in|for|with)\\s*([A-Z][A-Za-z0-9&\\s]+)\\s*(?:as|role|position|working as)?\\s*([A-Za-z\\s]+)?";
        Matcher matcher2= Pattern.compile(expRegex2).matcher(text);
        while(matcher2.find()){
            Map<String,Object> exp= new LinkedHashMap<>();
            exp.put("company",matcher2.group(3).trim());
            exp.put("role",matcher2.group(4) != null ? matcher2.group(4).trim() : "Unknown");
            exp.put("years",parseExperienceValue(matcher2.group(1),matcher2.group(2)));
            expList.add(exp);
        }
        String rangeRegex= "(?i)(?:worked|serving|was|employed|experience|joined).*?(?:at|in|for|with)\\s*([A-Z][A-Za-z0-9&\\s]+).*?(?:from)?\\s*(\\d{4})\\s*(?:to|-|_)\\s*(\\d{4})";
        Matcher range= Pattern.compile(rangeRegex).matcher(text);
        while(range.find()){
            double yrs=0.0;
            try{
                yrs= Double.parseDouble(range.group(3))-Double.parseDouble(range.group(2));
                if(yrs<0)
                    yrs=0;
            }
            catch(Exception ignored) {}
            Map<String,Object>exp =new LinkedHashMap<>();
            exp.put("company",range.group(1).trim());
            exp.put("role", "Unknown");
            exp.put("years",yrs);
            expList.add(exp);
        }
        if(expList.isEmpty() && text.toLowerCase().contains("experience")){
                Map<String,Object> exp= new LinkedHashMap<>();
                exp.put("company","Unknown");
                exp.put("role","General Experience Mentioned");
                exp.put("years", 0);
                expList.add(exp);
        }
        return expList;
    }  
    private static double parseDateRangeToYears(String dateRange){
        try{
            String[] parts= dateRange.split("[-_]");
            if(parts.length < 2)
                return 0.0;
            int startYear= extractYear(parts[0]);
            int endYear= extractYear(parts[1]);
            double years= endYear - startYear;
            return years<0 ? 0 : years;
        } 
        catch(Exception e){ 
            return 0.0; 
        }
    }
    private static double parseExperienceValue(String value,String unit){
        double yrs= 0.0;
        if(value==null) 
            return 0.0;
        value= value.toLowerCase();
        if(RegexAnalyzer.wordToNum.containsKey(value))
        yrs= RegexAnalyzer.wordToNum.get(value);
        else{
            try{ yrs= Double.parseDouble(value); } 
            catch(Exception ignored){}
        }
        if(unit != null && unit.toLowerCase().contains("month")) 
            yrs/= 12.0;
        return yrs;
    }
    private static int extractYear(String s){
        Matcher m= Pattern.compile("(\\d{4})").matcher(s);
        if(m.find()) 
            return Integer.parseInt(m.group(1));
        return 0;
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
    public static Map<String,Object> analyze(String resumeText,NLPUtils nlp){
        Map<String,Object> result=new LinkedHashMap<>();
        try{
            if(resumeText==null || resumeText.trim().isEmpty()){
            Map<String,Object> empty= new LinkedHashMap<>();
            empty.put("Error", "Empty resume text");
            return empty;
            }             
            result.put("Name",extractName(resumeText,nlp));
            result.put("Email",extractEmail(resumeText));
            result.put("Phone",extractPhone(resumeText));
            result.put("Gender",extractGender(resumeText));
            result.put("Bias Indicators",extractBias(resumeText));
            result.put("Skills",extractSkills(resumeText,nlp));
            result.put("Job Role",extractJobRole(resumeText,nlp));
            List<Map<String,Object>> expList= extractExperience(resumeText,nlp);
            double yrs= 0.0;
            Set<String> seenCompanies = new HashSet<>();
            for(Map<String,Object> exp:expList){
                String company= String.valueOf(exp.get("company")).toLowerCase();
                if(seenCompanies.contains(company)) 
                    continue;
                seenCompanies.add(company);
                Object yrsObj= exp.get("years");
                if(yrsObj instanceof Number)
                    yrs+= ((Number)yrsObj).doubleValue();
            }
            result.put("Experience",expList);
            result.put("Total Experience Years",Math.round(yrs*10.0)/10.0);
            result.put("Career Gap",extractCareerGap(resumeText));
        }
        catch(Exception e){
            e.printStackTrace();
            result.put("Error","Analysis failed: "+e.getMessage());
        }
        return result;
    }
}

    
