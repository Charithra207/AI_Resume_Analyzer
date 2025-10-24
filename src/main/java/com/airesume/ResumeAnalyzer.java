package com.airesume;
public class ResumeAnalyzer {
    public static void main(String[] args){
         try {
            System.out.println("<===== AI Resume Analyzer Started =====>");
            System.out.println("\nRunning RegexAnalyzer and generating analysis_output.json...");
            JsonHandler.main(null);            
            System.out.println("\nGenerating experience timeline...");
            Exp.main(null);            
            System.out.println("\nMatching jobs and calculating scores...");
            JobMatcher.main(null);            
            System.out.println("\nMatching roles...");
            RoleMatcher.main(null);            
            System.out.println("\nDetecting bias...");
            BiasDetector.main(null);          
            System.out.println("\nIdentifying skill gaps...");
            SkillGap.main(null);            
            System.out.println("\nRanking candidates...");
            Ranker.main(null);
            System.out.println("\n<===== AI Resume Analyzer Completed Successfully =====>");
        } 
        catch(Exception e){
            System.out.println("Error during analysis: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
