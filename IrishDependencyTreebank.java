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
            ArrayList lines = new ArrayList();
            
            while (line != null)
            {
                lines.add(line);
                line = b.readLine();         
            }      
            b.close();
             process(lines);
        }
        catch (IOException io)
        {
            System.out.println(io.getMessage());
        }
       
    }
    
    public static void process(ArrayList sentences) 
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
                    i = prepLine+1;
                    lineCount = prepLine+1;
                }
            }
        }
        for (int i=0; i < sentences.size(); i++)
        {
            System.out.println(sentences.get(i));
        }
        
            
            
           
           
    }
    
}
