/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package irishdependencytreebank;

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
    static ArrayList sentences = new ArrayList();
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
            
            while (line != null)
            {
                sentences.add(line);
                line = b.readLine();         
            }      
            b.close();
             //processPP(sentences);
             processCopula(sentences);
             printSentences(sentences);
        }
        catch (IOException io)
        {
            System.out.println(io.getMessage());
        }
       
    }
    
    public static void processPP(ArrayList sentences) 
    {
        int lineCount = 0;
        String line;
                  
        for (int i=0; i < sentences.size(); i++)
        {
            line = (String) sentences.get(i); 
            boolean isWrongNMod = false;
            boolean isNotAnMod = false;
            boolean finishedSentence = false;
            boolean finishedFile = false;
            //System.out.println("LINE" + line);
            int prepLine;
            lineCount++;
            String nextLine;
            String [] tokens = line.split("\t");    
            if (tokens.length >= 7 && tokens[7].equals("case"))
            {
                String prepId = tokens[0];
                String prepHeadId = tokens[6];
                prepLine = i;
                lineCount = i;
                
                String [] nmodTokens = null;
                String nmodId = null;
                String nmodHead = null;
                
                do
                {
                    lineCount++;
                    isWrongNMod = false;
                    isNotAnMod = false;
                    nextLine = (String) sentences.get(lineCount);
                    //System.out.println("NEXTLINE " + nextLine);
                        
                    if (nextLine == null)
                    {
                        finishedFile = true;
                    }
                    else if (nextLine.length() <= 1)
                    {
                        finishedSentence = true;     
                    }
                    else if (nextLine.indexOf("nmod") == -1)
                    {
                        //System.out.println("Not an mod");
                        isNotAnMod = true;
                    }
                    else 
                    {
                        nmodTokens = nextLine.split("\t");
                        nmodId = nmodTokens[0];
                        nmodHead = nmodTokens[6];
                        if (!prepId.equals(nmodHead))
                        {
                            //System.out.println("Found the wrong nmod");
                            isWrongNMod = true; 
                        }    
                    }             
                }while ((isNotAnMod || isWrongNMod) && !finishedSentence && !finishedFile);
                   
                if (!finishedSentence && !finishedFile)
                {
                    //the new head of the prep is the nmod
                    tokens[6] = nmodId;
                    //the new head of the nmod is the previous head of the prep
                    nmodTokens[6] = prepHeadId;
                    line = ""; nextLine = "";
                    for (int k=0; k < tokens.length-1; k++)
                    {
                        line += tokens[k] + "\t";
                    }
                    line += tokens[tokens.length-1];
                    for (int j=0; j < nmodTokens.length-1; j++)
                    {
                        nextLine += nmodTokens[j] + "\t";
                    }
                    nextLine += nmodTokens[nmodTokens.length-1];
                    //System.out.println(line);
                    //System.out.println(prepLine);
                    sentences.set(prepLine,line);
                    sentences.set(lineCount,nextLine);
                }
                else if (finishedSentence || finishedFile)
                {
                    sentences.set(prepLine,"***"+line+"***");
                }
            }
        }
       
    }
    
    public static void processCopula(ArrayList sentences) 
    {
        int lineCount = 0, copLine = 0, predLine = 0;
        String line;
       
        for (int i=0; i < sentences.size(); i++)
        {
            line = (String) sentences.get(i); 
            boolean isNotAPred = false;
            boolean isNotASubj = false;
            boolean finishedSentence = false;
            boolean finishedFile = false;
            //System.out.println("LINE" + line);
            lineCount++;
            String nextLine;
            String [] tokens = line.split("\t");    
            if (tokens.length > 1 && tokens[2].equals("is") &&!tokens[7].equals("mark:prt"))
            {
                String copId = tokens[0];
                String copHeadId = tokens[6];
                copLine = i;
                lineCount = i;
                
                String [] predTokens = null;
                String predHead = null;
                String predId = null;
                String [] subjTokens = null;
                String subjHead = null;
                
                do
                {
                    lineCount++;
                    isNotAPred = false;
                    nextLine = (String) sentences.get(lineCount);
                    //System.out.println("NEXTLINE " + nextLine);
                        
                    if (nextLine == null)
                    {
                        finishedFile = true;
                    }
                    else if (nextLine.length() <= 1)
                    {
                        finishedSentence = true;     
                    }
                    else 
                    {
                        predTokens = nextLine.split("\t");
                        predId = predTokens[0];
                        predHead = predTokens[6];
                        if (!predTokens[7].startsWith("xcomp") || !predHead.equals(copId))
                        {
                            //System.out.println("not a pred");
                            isNotAPred = true; 
                        }    
                    }             
                }while (isNotAPred && !finishedSentence && !finishedFile);
                   
                if (!finishedSentence && !finishedFile)
                {
                    //the new head of the pred is the head of the cop
                    predTokens[6] = copHeadId;
                    //the relation associated with the cop is now associated with the pred
                    predTokens[7] = tokens[7];
                    //the new head of the cop is the pred
                    tokens[6] = predId;
                    //the cop's relation to the pred is "cop"
                    tokens[7] = "cop";
                    predLine = lineCount;
                    //now find the subj of the predicate
                    String anotherLine = null;
                    do
                    {
                        lineCount++;
                        isNotASubj = false;
                        anotherLine = (String) sentences.get(lineCount);
                        //System.out.println("NEXTLINE " + anotherLine);
                        
                        if (anotherLine == null)
                        {
                            finishedFile = true;
                        }
                        else if (anotherLine.length() <= 1)
                        {
                            finishedSentence = true;     
                        }
                        else 
                        {
                            subjTokens = anotherLine.split("\t");
                            subjHead = subjTokens[6];
                            if (!subjHead.equals(copId))
                            {
                                isNotASubj = true; 
                            }    
                        }             
                    }while (isNotASubj && !finishedSentence && !finishedFile);
                    if (!finishedSentence && !finishedFile)
                    {
                        subjTokens[6] = predId;
                        anotherLine = "";
                        for (int x=0; x < subjTokens.length-1; x++)
                        {
                            anotherLine += subjTokens[x] + "\t";
                        }
                        anotherLine += subjTokens[subjTokens.length-1];
                        sentences.set(lineCount,anotherLine);
                    }
                    else
                    {
                        sentences.set(copLine,"+++"+line+"+++");
                        i = copLine + 1;
                        lineCount = copLine + 1;
                    }
                    
                    line = ""; nextLine = "";
                    for (int k=0; k < tokens.length-1; k++)
                    {
                        line += tokens[k] + "\t";
                    }
                    line += tokens[tokens.length-1];
                    for (int j=0; j < predTokens.length-1; j++)
                    {
                        nextLine += predTokens[j] + "\t";
                    }
                    nextLine += predTokens[predTokens.length-1];
                    //System.out.println(line);
                    //System.out.println(prepLine);
                    sentences.set(copLine,line);
                    sentences.set(predLine,nextLine);
                }
                else if (finishedSentence || finishedFile)
                {
                    sentences.set(copLine,"+++"+line+"+++");
                }
            }
        }
       
    }
    public static void printSentences(ArrayList sentences)
    {
         for (int i=0; i < sentences.size(); i++)
        {
            System.out.println(sentences.get(i));
        }      
    }
    
}
