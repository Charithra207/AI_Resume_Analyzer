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
            }
            try(InputStream s= cl.getResourceAsStream("models/en-token.bin")){
                if(s != null) 
                    tokenizer= new TokenizerME(new TokenizerModel(s));
            }
            try(InputStream s= cl.getResourceAsStream("models/en-ner-person.bin")){
                if(s != null)
                    personFinder= new NameFinderME(new TokenNameFinderModel(s));
            }
            try(InputStream s= cl.getResourceAsStream("models/en-ner-organization.bin")){
                if(s != null) 
                    orgFinder = new NameFinderME(new TokenNameFinderModel(s));
            }
            try(InputStream s= cl.getResourceAsStream("models/en-ner-date.bin")){
                if(s != null) 
                    dateFinder = new NameFinderME(new TokenNameFinderModel(s));
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
        String[] tokens= tokens(text);
        Span[] spans= personFinder.find(tokens);
        for(Span s:spans) 
            names.add(String.join(" ",Arrays.copyOfRange(tokens,s.getStart(),s.getEnd())));
        personFinder.clearAdaptiveData();
        return names;
    }
    public static List<String> findOrganizations(String text){
        init();
        List<String> orgs= new ArrayList<>();
        if(orgFinder == null) 
            return orgs;
        String[] tokens= tokens(text);
        Span[] spans= orgFinder.find(tokens);
        for(Span s:spans) 
            orgs.add(String.join(" ",Arrays.copyOfRange(tokens,s.getStart(),s.getEnd())));
        orgFinder.clearAdaptiveData();
        return orgs;
    }
    public static List<String> findDates(String text){
        init();
        List<String> dates= new ArrayList<>();
        if(dateFinder == null) 
            return dates;
        String[] tokens= tokens(text);
        Span[] spans= dateFinder.find(tokens);
        for(Span s:spans) 
            dates.add(String.join(" ",Arrays.copyOfRange(tokens,s.getStart(),s.getEnd())));
        dateFinder.clearAdaptiveData();
        return dates;
    }
    public static List<String> findSkills(String text){
        init();
        Set<String> skills= new LinkedHashSet<>();
        String skillsRegex="(?i)\\b(" +
        "java|python|c\\+\\+|c#|javascript|typescript|html|css|react|angular|node(?:\\.js)?|" +
        "express|spring|django|flask|dotnet|php|ruby|go|swift|kotlin|" +
        "sql|mysql|postgresql|mongodb|oracle|firebase|nosql|" +
        "aws|azure|gcp|docker|kubernetes|jenkins|git|github|jira|agile|scrum|" +
        "tensorflow|pytorch|machine learning|data analysis|powerbi|tableau|" +
        "excel|nlp|artificial intelligence|ai|deep learning|cloud computing" +
        ")\\b";
        Matcher matcher= Pattern.compile(skillsRegex).matcher(text);
        while(matcher.find()) 
            skills.add(matcher.group().trim());
        return new ArrayList<>(skills);
    }
    public static List<String> findJobTitles(String text) {
        init();
        Set<String> titles= new LinkedHashSet<>();
        String titleRegex="(?i)\\b(developer|backend developer|frontend developer|ml engineer|engineer|data analyst|manager|analyst|intern|designer|consultant|administrator|specialist|architect|coordinator|executive|officer|scientist|technician|advisor|lead|head)\\b";
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
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        return (double)intersection.size()/Math.sqrt(words1.size()*words2.size());
    }
}
