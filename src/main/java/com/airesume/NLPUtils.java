package com.airesume;
import opennlp.tools.namefind.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.Span;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;
public class NLPUtils {
    private static SentenceDetectorME sentDetector;
    private static TokenizerME tokenizer;
    private static NameFinderME personFinder;
    private static NameFinderME orgFinder;
    private static NameFinderME dateFinder;
    private static boolean initialized = false;
    public static synchronized void init(){
        if(initialized) 
            return;
        try {
            ClassLoader cl= NLPUtils.class.getClassLoader();
            try(InputStream s= cl.getResourceAsStream("models/en-sent.bin")){
                if(s != null) 
                    sentDetector= new SentenceDetectorME(new SentenceModel(s));
                else 
                    System.err.println("[WARN] Sentence model not found!");
            }
            try(InputStream s= cl.getResourceAsStream("models/en-token.bin")){
                if(s != null) 
                    tokenizer= new TokenizerME(new TokenizerModel(s));
                else 
                    System.err.println("[WARN] Tokenizer model not found!");   
            }
            try(InputStream s= cl.getResourceAsStream("models/en-ner-person.bin")){
                if(s != null)
                    personFinder= new NameFinderME(new TokenNameFinderModel(s));
                else 
                    System.err.println("[WARN] Organization model not found!");
            }
            try(InputStream s= cl.getResourceAsStream("models/en-ner-organization.bin")){
                if(s != null) 
                    orgFinder = new NameFinderME(new TokenNameFinderModel(s));
                else 
                    System.err.println("[WARN] Organization model not found!");
            }
            try(InputStream s= cl.getResourceAsStream("models/en-ner-date.bin")){
                if(s != null) 
                    dateFinder = new NameFinderME(new TokenNameFinderModel(s));
                else 
                    System.err.println("[WARN] Date model not found!");    
            }
            initialized= true;
        } 
        catch(Exception e){
            System.out.println("NLP init failed: "+e.getMessage());
            initialized= false;
        }
    }
    public static String[] sentences(String text){
        init();
        return sentDetector != null ? sentDetector.sentDetect(text) : new String[]{text};
    }
    public static String[] tokens(String text){
        init();
        return tokenizer != null ? tokenizer.tokenize(text) : text.split("\\s+");
    }
    public static List<String> findPersons(String text){
        init();
        List<String> names= new ArrayList<>();
        if(personFinder == null) 
            return names;
        synchronized(personFinder){
            String[] tokens= tokens(text);
            Span[] spans= personFinder.find(tokens);
            for(Span s:spans) 
                names.add(String.join(" ",Arrays.copyOfRange(tokens,s.getStart(),s.getEnd())));
            personFinder.clearAdaptiveData();
        }
        return names;
    }
    public static List<String> findOrganizations(String text){
        init();
        List<String> orgs= new ArrayList<>();
        if(orgFinder == null) 
            return orgs;
        synchronized(orgFinder){
            String[] tokens= tokens(text);
            Span[] spans= orgFinder.find(tokens);
            for(Span s:spans) 
                orgs.add(String.join(" ",Arrays.copyOfRange(tokens,s.getStart(),s.getEnd())));
            orgFinder.clearAdaptiveData();
        }
        return orgs;
    }
    public static List<String> findDates(String text){
        init();
        List<String> dates= new ArrayList<>();
        if(dateFinder == null) 
            return dates;
        synchronized(dateFinder){
            String[] tokens= tokens(text);
            Span[] spans= dateFinder.find(tokens);
            for(Span s:spans) 
                dates.add(String.join(" ",Arrays.copyOfRange(tokens,s.getStart(),s.getEnd())));
            dateFinder.clearAdaptiveData();
        }
        if(dates.isEmpty()){
            Matcher m= Pattern.compile("(\\d{4}|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-zA-Z0-9\\s,-]*").matcher(text);
            while(m.find()) 
                dates.add(m.group().trim());
        }
        return dates;
    }
    public static String findEmail(String text){       
        Matcher matcher= Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(text);
        return matcher.find() ? matcher.group() :"";
    }
    public static String findPhone(String text){        
        Matcher matcher= Pattern.compile("(\\+\\d{1,3}[- ]?)?\\d{10,13}").matcher(text);
        return matcher.find() ? matcher.group() :"";
    }
    public static List<String> findSkills(String text){
        init();
        Set<String> skills= new LinkedHashSet<>();
        text= text.replace("/", " ").replace("-", " ");
        String skillsRegex="(?i)\\b(" +
        "java|python|c\\+\\+|c#|javascript|typescript|html|css|react|angular|node(?:\\.js)?|" +
        "express|spring|django|flask|dotnet|php|ruby|go|swift|kotlin|" +
        "sql|mysql|postgresql|mongodb|oracle|firebase|nosql|" +
        "aws|azure|gcp|docker|kubernetes|jenkins|git|github|jira|agile|scrum|" +
        "tensorflow|pytorch|machine learning|power bi|tableau|data analysis|powerbi|tableau|" +
        "excel|nlp|artificial intelligence|ai|deep learning|cloud computing" +
        ")\\b";
        Matcher matcher= Pattern.compile(skillsRegex).matcher(text);
        while(matcher.find()) 
            skills.add(matcher.group().trim());
        return new ArrayList<>(skills);
    }
    public static List<Map<String,String>> findEducation(String text){
        List<Map<String,String>> education= new ArrayList<>();        
        List<String> orgs= findOrganizations(text);
        List<String> dates= findDates(text);
        Pattern pattern= Pattern.compile("(?i)(B\\.?E|B\\.?Tech|M\\.?E|M\\.?Tech|MBA|BSc|MSc|PhD)[^\\d]*(\\d{4})?");
        Matcher matcher= pattern.matcher(text);
        while(matcher.find()){
            Map<String,String> edu= new HashMap<>();
            edu.put("degree",matcher.group(1).trim());
            edu.put("year",matcher.group(2)!= null ? matcher.group(2):(dates.isEmpty() ?"": dates.get(0)));            
            edu.put("university",orgs.isEmpty() ?"": orgs.get(0));
            education.add(edu);
        }
        return education;
    }
    public static List<Map<String,String>> findExperience(String text){
        List<Map<String,String>> experiences= new ArrayList<>();
        List<String> orgs= findOrganizations(text);
        List<String> titles= findJobTitles(text);
        List<String> dates= findDates(text);
        Pattern pattern= Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(year|years|yr|yrs|month|months)");
        Matcher matcher= pattern.matcher(text);
        while(matcher.find()){
            Map<String,String> exp= new HashMap<>();
            exp.put("company",orgs.isEmpty() ? "": orgs.get(0));
            exp.put("role",titles.isEmpty() ? "": titles.get(0));
            exp.put("years",matcher.group(1));
            exp.put("durationText",dates.isEmpty() ?"": String.join(" - ", dates));
            experiences.add(exp);
        }
        return experiences;
    }
    public static List<String> findJobTitles(String text){
        init();
        Set<String> titles= new LinkedHashSet<>();
        String titleRegex="(?i)\\b(developer|backend developer|frontend developer|ml engineer|engineer|data analyst|manager|analyst|intern|designer|consultant|administrator|specialist|architect|coordinator|executive|officer|scientist|technician|advisor|lead|head|software engineer|full stack developer|data scientist)\\b";
        Matcher matcher= Pattern.compile(titleRegex).matcher(text);
        while(matcher.find()) 
            titles.add(matcher.group().trim());
        return new ArrayList<>(titles);
    }
    public static String normalizeText(String text){
        if(text == null) 
            return "";
        return text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }
    public static boolean containsWord(String text,String word) {
        if(text==null || word==null) 
            return false;
        String pattern= "\\b"+Pattern.quote(word.toLowerCase())+"\\b";
        return Pattern.compile(pattern).matcher(text.toLowerCase()).find();
    }
    public static double semanticSimilarity(String text1,String text2){
        if(text1==null || text2==null) 
            return 0.0;
        text1= normalizeText(text1);
        text2= normalizeText(text2);
        if(text1.isEmpty() || text2.isEmpty()) 
            return 0.0;
        Set<String> words1= new HashSet<>(Arrays.asList(text1.split(" ")));
        Set<String> words2= new HashSet<>(Arrays.asList(text2.split(" ")));
        Set<String> intersection= new HashSet<>(words1);
        intersection.retainAll(words2);
        return (double)intersection.size()/Math.sqrt(words1.size()*words2.size());
    }
}
