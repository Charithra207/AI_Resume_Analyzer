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
        if(text==null) return "Unknown";     
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
            if(!raw.startsWith("+") && raw.length()==10)
                raw="+91"+raw;        
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
        "express|spring|spring boot|django|flask|dotnet|php|ruby|go|swift|kotlin|" +
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
        if(text==null || text.trim().isEmpty()) return eduList;
        String raw= text;
        String norm= raw.replaceAll("\\s+", " ");   
        String degreeTokens= "(b\\.tech|btech|b\\.e\\.?|be|m\\.tech|mtech|m\\.e\\.?|me|mba|bsc|b\\.sc\\.?|msc|m\\.sc\\.?|phd|bca|mca|diploma|bachelor|master)";
        Pattern degreeWithSpecUni = Pattern.compile("(?i)\\b" + degreeTokens +
        "\\b(?:\\s+in\\s+([^,;\\n]{2,120}))?(?:\\s*(?:,|\\s)\\s*(?:from|at)\\s+([^,;\\n]{2,120}))?(?:[^\\d]{0,50}(\\b(?:19|20)\\d{2}\\b))?");
        Pattern yearPat= Pattern.compile("\\b(19|20)\\d{2}\\b");    
        Matcher m= degreeWithSpecUni.matcher(norm);
        List<String> orgCandidates= NLPUtils.findOrganizations(raw);
        orgCandidates.removeIf(s->s==null || s.trim().length() <3 || s.toLowerCase().matches(".*\\b(education|experience|skills|profile|summary|projects)\\b.*"));
        while(m.find()){    
            String degCore= m.group(1)!= null ? m.group(1).replaceAll("\\.", "").toUpperCase():"Unknown";    
            int startPos= Math.max(0,m.start()-10);
            int endPos= Math.min(norm.length(),m.end()+200);
            String rawWindow= raw;
            try{
                String normSub= norm.substring(startPos, endPos);
                int rawIdx= raw.indexOf(normSub);
                if(rawIdx>=0){            
                    int rawStart= Math.max(0, rawIdx - 10);
                    int rawEnd= Math.min(raw.length(), rawIdx + normSub.length() + 200);
                    rawWindow= raw.substring(rawStart, rawEnd);
                } 
                else{            
                    int rpos= raw.toLowerCase().indexOf(degCore.toLowerCase());
                    if(rpos>=0){
                        int rawStart= Math.max(0, rpos - 10);
                        int rawEnd= Math.min(raw.length(), rpos + 200);
                        rawWindow= raw.substring(rawStart, rawEnd);
                    }
                }
            } 
            catch(Exception ignore) {}    
            String specialization= "";
            String uniCaptured= "";    
            Matcher specFrom= Pattern.compile("(?i)\\bin\\s+([^,;\\n]{2,200}?)\\s+(?:from|at)\\s+([^,;\\n]{2,200})").matcher(rawWindow);
            if(specFrom.find()){
                specialization= specFrom.group(1).trim();
                uniCaptured= specFrom.group(2).trim();
            } 
            else{        
                Matcher specOnly= Pattern.compile("(?i)\\bin\\s+([^,;\\n]{2,200})").matcher(rawWindow);
                if(specOnly.find())
                    specialization = specOnly.group(1).trim();      
                Matcher uniOnly = Pattern.compile("(?i)(?:from|at)\\s+([^,;\\n]{2,200})").matcher(rawWindow);
                if(uniOnly.find())
                    uniCaptured = uniOnly.group(1).trim();        
            }    
            if((uniCaptured==null || uniCaptured.isEmpty()) && m.groupCount()>= 2 && m.group(2) != null){        
                String g3 = m.group(2);
                if(g3 != null && !g3.trim().isEmpty()){            
                    String g3l = g3.toLowerCase();
                    if(g3l.contains(" from ")){
                        int idxf= g3l.indexOf(" from ");
                        specialization= g3.substring(0, idxf).trim();
                        uniCaptured= g3.substring(idxf + " from ".length()).trim();
                    } 
                    else{                
                        if(g3l.contains("university") || g3l.contains("institute") || g3l.contains("college") || g3l.matches(".*\\b(iit|nit|vit|bits|iiit)\\b.*"))
                            uniCaptured= g3.trim();
                        else if(specialization.isEmpty())
                            specialization = g3.trim();
                    }
                }
            }    
            if((uniCaptured==null || uniCaptured.isEmpty()) && !orgCandidates.isEmpty()){
                String cand= closestUniversity(raw, m.start(), orgCandidates);
                if(cand!=null && !cand.isEmpty()) uniCaptured = cand;
            }    
            if(specialization!=null){
                specialization= specialization.replaceAll("(?i)\\s+from\\s+.*$", "").trim();
                specialization= specialization.replaceAll("[\\.,;]+$", "").trim();
            } 
            else specialization="";
            uniCaptured= cleanUniversity(uniCaptured);    
            String year= "";
            Matcher ywin= Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(rawWindow);
            if(ywin.find()) year=ywin.group();
            else{
                Matcher yall= Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(raw);
                if (yall.find()) year=yall.group();
            }    
            String degreeFinal = degCore + (specialization.isEmpty() ? "":" in "+specialization);    
            if(degreeFinal==null || degreeFinal.trim().isEmpty()) degreeFinal= "Unknown";
            if(uniCaptured==null || uniCaptured.trim().isEmpty()) uniCaptured= "Unknown";
            if(year==null || year.trim().isEmpty()) year= "Unknown";
            Map<String, String> edu= new LinkedHashMap<>();
            edu.put("degree",degreeFinal);
            edu.put("university",uniCaptured);
            edu.put("year",year);    
            String key= (degreeFinal + "|" + uniCaptured + "|" + year).toLowerCase();
            boolean dup= false;
            for(Map<String,String> e:eduList) {
                String k2= (e.getOrDefault("degree", "") + "|" + e.getOrDefault("university", "") + "|" + e.getOrDefault("year", "")).toLowerCase();
                if(k2.equals(key)) dup= true; break; 
            }
            if(!dup) eduList.add(edu);
        }  
        if(eduList.isEmpty()){
            String[] lines= raw.split("\\r?\\n|;|\\.");
            Pattern simpleDeg= Pattern.compile("(?i)\\b" + degreeTokens + "\\b");
            for(String line:lines){            
                if(line==null) continue;
                Matcher sd= simpleDeg.matcher(line);
                if(sd.find()){
                    String degCore = sd.group(1).replaceAll("\\.", "").toUpperCase();               
                    Matcher spec= Pattern.compile("(?i)\\bin\\s+([^,;]+)").matcher(line);
                    String specialization = spec.find() ? spec.group(1).trim() : "";
                    Matcher uniM = Pattern.compile("(?i)(?:from|at)\\s+([^,;\\n]+)").matcher(line);
                    String uni= uniM.find() ? uniM.group(1).trim() : "";
                    Matcher yM= yearPat.matcher(line);
                    String year= yM.find() ? yM.group() : "";
                    if(uni.isEmpty() && !orgCandidates.isEmpty()){
                        String cand= closestUniversity(raw, raw.indexOf(line), orgCandidates);
                        if(cand!=null) uni = cand;
                    }
                    if(!specialization.isEmpty()){
                        specialization= specialization.replaceAll("(?i)\\s+from\\s+.*$", "").trim();
                    }
                    String degreeFinal= degCore+(!specialization.isEmpty() ? " in "+specialization:"");
                    eduList.add(Map.of(
                    "degree",degreeFinal,
                    "university",(cleanUniversity(uni).isEmpty() ? "Unknown":cleanUniversity(uni)),
                    "year",(year.isEmpty() ? "Unknown":year)
                    ));
                }
            }
        }
        return eduList;
    }
    private static String cleanUniversity(String uni){
        if(uni==null) return "";
        String u= uni.trim();
        u= u.replaceAll("[.,;]+$", "").trim();
        u= u.replaceAll("\\s{2,}", " ");    
        u= u.replaceAll("(?i)^(from|at|in)\\s+", "").trim();
        return u;
    }
    private static String closestUniversity(String rawText, int degreePos,List<String> orgCandidates){
        if(orgCandidates==null || orgCandidates.isEmpty()) return "";
        String best= "";
        int bestDist= Integer.MAX_VALUE;
        for(String o:orgCandidates){
            if(o==null) continue;
            String low= o.toLowerCase();
            boolean looksLikeUni= low.contains("university") || low.contains("institute") || low.contains("college") || low.matches(".*\\b(iit|nit|iiit|vit|bits)\\b.*");
            int idx= rawText.indexOf(o);
            if(idx<0) continue;
            int dist= Math.abs(idx-degreePos);
            if(looksLikeUni) dist -= 50;
            if(dist<bestDist){
                bestDist=dist;
                best=o;
            }
        }
        return best==null ? "" : best;
    }
    public static Map<String,Object> extractExperience(String text){
        Map<String,Object> result= new LinkedHashMap<>();
        List<Map<String,String>> expList= new ArrayList<>();
        double totalYears= 0.0;
        if(text==null || text.trim().isEmpty()){
            result.put("experience_details", expList);
            result.put("total_experience", "Unknown");
            return result;
        }
        String normalized= text.replaceAll("\\s+", " ").trim();
        Pattern p1= Pattern.compile("(?i)\\b(?:worked as|worked|serving as|currently|experience[:\\s]*)\\s+(.+?)\\s+at\\s+([^,\\.\\n]{2,80}?)(?:\\s+for\\s+(\\d+(?:\\.\\d+)?)(?:\\s*(years?|months?))?)?\\b");
        Pattern p2= Pattern.compile("(?i)([A-Za-z0-9 &/\\-]{2,80}?)\\s+at\\s+([^,\\.\\n]{2,80}?)(?:\\s+for\\s+(\\d+(?:\\.\\d+)?)(?:\\s*(years?|months?))?)?\\b");
        Pattern yearRange= Pattern.compile("(?i)\\b(\\d{4})\\s*(?:-|to|-|—)\\s*(\\d{4}|present|current)\\b");
        String[] chunks= normalized.split("\\.|;|\\n");
        Set<String> seen= new HashSet<>();
        for(String chunk:chunks){
            String c= chunk.trim();
            if(c.isEmpty()) continue;
            Matcher m1= p1.matcher(c);
            if(m1.find()){
                String rawRole= m1.group(1).trim();
                String company= m1.group(2).trim();
                String yrsTxt= m1.group(3);
                String yrsUnit= m1.group(4);
                company= company.replaceAll("\\s+(for\\s+\\d.*)$", "").trim();
                if(isNoiseOrg(company)) continue;
                String role = cleanRole(rawRole);
                double yrs= 0.0;
                if(yrsTxt!=null){
                    try{
                        yrs = Double.parseDouble(yrsTxt);
                        if(yrsUnit!=null && yrsUnit.toLowerCase().startsWith("month")) yrs/=12.0;
                    } 
                    catch(Exception ignored){}
                } 
                else{
                    Matcher yr= yearRange.matcher(c);
                    if(yr.find()){
                        try{
                            int s= Integer.parseInt(yr.group(1));
                            int e= yr.group(2).matches("(?i)present|current") ? Calendar.getInstance().get(Calendar.YEAR) : Integer.parseInt(yr.group(2));
                            yrs= Math.max(0, e - s);
                        } 
                        catch(Exception ignored){}
                    }
                }
                String key = (company + "|" + role + "|" + Math.round(yrs*10.0)/10.0);
                if(!seen.contains(key)){
                    seen.add(key);
                    Map<String,String> e = new LinkedHashMap<>();
                    e.put("company", company);
                    e.put("role", role);
                    e.put("duration", yrs > 0 ? formatDuration(yrs) : "Unknown");
                    e.put("raw_duration_text", c);
                    expList.add(e);
                    if(yrs > 0) totalYears += yrs;
                }
                continue;
            }
            Matcher m2= p2.matcher(c);
            if(m2.find()){
                String rawRole= m2.group(1).trim();
                String company= m2.group(2).trim();
                String yrsTxt= m2.group(3);
                String yrsUnit= m2.group(4);
                company = company.replaceAll("\\s+(for\\s+\\d.*)$", "").trim();
                if(isNoiseOrg(company)) continue;
                String role= cleanRole(rawRole);
                double yrs= 0.0;
                if(yrsTxt!= null){
                    try{
                        yrs= Double.parseDouble(yrsTxt);
                        if(yrsUnit != null && yrsUnit.toLowerCase().startsWith("month")) yrs /= 12.0;
                    } 
                    catch(Exception ignored){}
                } 
                else {
                    Matcher yr= yearRange.matcher(c);
                    if(yr.find()){
                        try{
                            int s= Integer.parseInt(yr.group(1));
                            int e= yr.group(2).matches("(?i)present|current") ? Calendar.getInstance().get(Calendar.YEAR) : Integer.parseInt(yr.group(2));
                            yrs= Math.max(0, e - s);
                        } 
                        catch(Exception ignored){}
                    }
                }
                String key= (company+"|"+role+"|"+Math.round(yrs*10.0)/10.0);
                if(!seen.contains(key)){
                    seen.add(key);
                    Map<String,String> e= new LinkedHashMap<>();
                    e.put("company",company);
                    e.put("role",role);
                    e.put("duration",yrs> 0 ? formatDuration(yrs) :"Unknown");
                    e.put("raw_duration_text", c);
                    expList.add(e);
                    if(yrs>0) totalYears += yrs;
                }
                continue;
            }
        }
        if(!expList.isEmpty()){
            result.put("experience_details", expList);
            result.put("total_experience", formatDuration(totalYears));
            return result;
        }    
        List<String> comp= new ArrayList<>(NLPUtils.findOrganizations(text));
        comp.removeIf(s->s==null || s.trim().length() <3 || isNoiseOrg(s));
        List<String> roles= NLPUtils.findJobTitles(text);
        Pattern comb= Pattern.compile("(?i)(\\d{4}\\s*(?:-|to|-|—)\\s*(?:\\d{4}|present|current))|(\\d+(?:\\.\\d+)?\\s*(?:years?|months?))");
        Matcher m= comb.matcher(text);
        List<String> durations = new ArrayList<>();
        while(m.find()){
            durations.add(m.group().trim());
        }
        int iter= Math.max(comp.size(),Math.max(roles.size(),durations.size()));
        if(iter==0 && (!comp.isEmpty() || !roles.isEmpty())) iter= Math.max(comp.size(),roles.size());
        Set<String> seen2= new HashSet<>();
        for(int i=0;i<iter;i++){
            String company= i< comp.size() ? comp.get(i) : "Unknown";
            String role=i < roles.size() ? roles.get(i) : "Unknown";
            String durText=i < durations.size() ? durations.get(i) : "";
            double yrs= calDuration(durText);
            String key= (company+"|"+role+"|"+Math.round(yrs*10.0)/10.0);
            if(seen2.contains(key)) continue;
            seen2.add(key);
            if(isNoiseOrg(company)) company= "Unknown";
            Map<String,String> e= new LinkedHashMap<>();
            e.put("company",company);
            e.put("role",role);
            e.put("duration",yrs > 0 ? formatDuration(yrs) :"Unknown");
            e.put("raw_duration_text",durText ==null ? "":durText);
            expList.add(e);
            if(yrs >0) totalYears+=yrs;
        }
        result.put("experience_details",expList);
        result.put("total_experience",expList.isEmpty() ? "Unknown": formatDuration(totalYears));
        return result;
    }
    private static String cleanRole(String raw){
        if(raw==null) return "Unknown";
        String r= raw.trim();
        r= r.replaceAll("(?i)^(worked as|worked|serving as|experience:|experience|role[:\\-]\\s*)\\s*", "");
        r= r.replaceAll("(?i)\\s+for\\s+\\d+(?:\\.\\d+)?\\s*(years?|months?)$", "");
        r= r.replaceAll("^[\\-:,\\.\\s]+|[\\-:,\\.\\s]+$", "");
        if(r.isEmpty()) return "Unknown";
        return r;
    }
    private static boolean isNoiseOrg(String s){
        if(s==null) return true;
        String t= s.trim().toLowerCase();
        if(t.length()< 3) return true;
        String[] noise= {"education","experience","skills","skill","phone","email","profile","personal","details","address","projects","summary"};
        for(String n:noise){
            if(t.equals(n) || t.startsWith(n+" ") || t.endsWith(" " + n) || t.contains(" " + n + " ")) return true;
        }
        String[] roleWords= {"engineer","developer","manager","analyst","intern","consultant","scientist","software","backend","frontend"};
        for(String rw:roleWords){
            if(t.contains(rw)) return true;
        }
        return false;
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
            String rawName= extractName(resumeText, nlp);
            if(rawName==null) rawName= "Unknown";
            rawName = rawName.replaceAll("[\\.,]+$", "").trim();
            result.put("Name",rawName);
            result.put("Email",extractEmail(resumeText));
            result.put("Phone",extractPhone(resumeText));
            result.put("Gender",extractGender(resumeText));
            result.put("Bias Indicators",extractBias(resumeText));
            result.put("Skills",extractSkills(resumeText,nlp));
            result.put("Job Role",extractJobRole(resumeText,nlp));
            result.put("Education",extractEducation(resumeText));
            Map<String,Object> expResult= extractExperience(resumeText);
            result.put("Experience",expResult.getOrDefault("experience_details",new ArrayList<>()));
            result.put("Total Experience",expResult.getOrDefault("total_experience","Unknown"));
            result.put("Career Gap",extractCareerGap(resumeText));
            result.put("RawText",resumeText);
        }
        catch(Exception e){
            e.printStackTrace();
            result.put("Error","Analysis failed: "+e.getMessage());
        }
        return result;
    }
}

    
