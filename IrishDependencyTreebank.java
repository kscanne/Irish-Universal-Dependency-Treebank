/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author fosterjen
 */
public class IrishDependencyTreebank {
    static ArrayList<Sentence> sentences = new ArrayList();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {   
        try
        {
            int lineCount = 0;
            BufferedReader b = new BufferedReader(new FileReader(new File(args[0])));
            String line = b.readLine();
            ArrayList<String> lines;
            while (line != null)
            {
                //System.out.println(line);
                lines = new ArrayList<String>();
                while (line != null && line.length() >1)
                {
                    lines.add(line);
                    line = b.readLine();
                    //System.out.println(line);
                }
                sentences.add(new Sentence(lines));
                line = b.readLine();
            }      
            b.close();
            //processPP();
            processCopula();
            printSentences();
        }
        catch (IOException io)
        {
            System.out.println(io.getMessage());
        }
       
    }
    
    public static void processPP() 
    {
        Sentence sentence;
                  
        for (int i=0; i < sentences.size(); i++)
        {
            sentence = (Sentence) sentences.get(i); 
            
            //does the sentence contain a case relation?
            //if it does, find the nmod that it is related to
            ArrayList<Word> caseWords = sentence.findWords("case");
            for (int j=0; j < caseWords.size(); j++)
            {
                Word caseWord = caseWords.get(j);
            
                ArrayList<Word> nmodWords = sentence.findDependents(caseWord);
                if (nmodWords.size() == 0)
                {
                    //no nmod, mark it
                    caseWord.setToken("***"+ caseWord.getToken()+"***");
                }
                else
                {
                    for (int k=0; k < nmodWords.size(); k++)
                    {
                        Word nmodWord = nmodWords.get(k);
                    
                       
                        if (nmodWord.getHead() == caseWord.getId())
                        {
                            //System.out.println(nmodWord.getToken());
                            //System.out.println(caseWord.getToken());
                            //System.out.println("changing the heads");
                            //sentence.swapHead(caseWord.getId(),nmodWord.getId());
                            nmodWord.setHead(caseWord.getHead());
                            caseWord.setHead(nmodWord.getId());
                        }
                    }       
                }
            }
        }        
    }
            
    public static void processCopula() 
    {
        Sentence sentence;
        for (int i=0; i < sentences.size(); i++)
        {
            sentence = (Sentence) sentences.get(i);
            //does the sentence contain a copula?
            ArrayList<Word> copWords = sentence.findWordsByLemma("is");
            for (int j=0; j < copWords.size(); j++)
            {
                Word copWord = (Word) copWords.get(j);
                //check that the word is not a mark:prt
                if (!copWord.getRelation().equals("mark:prt"))
                {
                    //look for the xcomp
                    ArrayList<Word> xcompWords = sentence.findWords("xcomp:pred");
                    xcompWords.addAll(sentence.findWords("xcomp"));
                    if (xcompWords.size() == 0)
                    {
                        //no xcomp, something strange going on
                        copWord.setToken("+++"+copWord.getToken()+"+++");
                    }
                    for (int k=0; k < xcompWords.size(); k++)
                    {
                        Word xcompWord = xcompWords.get(k);
                        if (xcompWord.getHead() == copWord.getId())
                        {
                            xcompWord.setHead(copWord.getHead());
                            xcompWord.setRelation(copWord.getRelation());
                            copWord.setHead(xcompWord.getId());
                            copWord.setRelation("cop");
                        }
                    }
                    //look for other dependents of the copula
                    ArrayList<Word> otherDeps = sentence.findDependents(copWord);
                    for (int x=0; x < otherDeps.size(); x++)
                    {
                        Word dep = otherDeps.get(x);
                        dep.setHead(copWord.getHead());
                    }
                }
                
            }
        }  
    }
    
    public static void printSentences()
    {
         for (int i=0; i < sentences.size(); i++)
        {
            System.out.println(sentences.get(i));
        }      
    }
    
}
