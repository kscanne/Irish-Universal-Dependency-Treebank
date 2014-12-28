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
            //processCopula();
            //processProPreps();
	    processPPXComp();
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
                            nmodWord.setHead(caseWord.getHead());
                            caseWord.setHead(nmodWord.getId());
                        }
                    }       
                }
            }
        }        
    }


    public static void processAspectualPhrases()
    {
	Sentence sentence;

	for (int i=0; i < sentences.size(); i++)
	    {
		sentence = (Sentence) sentences.get(i);
		ArrayList<Word> xcompWords = sentence.findWords("xcomp");
		for (int j=0; j < xcompWords.size(); j++)
		    {
			Word xcompWord = xcompWords.get(j);
			if (xcompWord.getLemma().equals("ag"))
			    {
				ArrayList<Word> dependentWords = sentence.findDependents(xcompWord);
				if (dependentWords.size() == 0)
				    {

					//something strange going on - mark it
					xcompWord.setToken("@@@"+ xcompWord.getToken()+"@@@");
				    }
			   
		    
		else if (dependentWords.size() == 1)
		    {

			Word dependentWord = dependentWords.get(0);
			if (dependentWord.getRelation().equals("dobj"))
			    {
				dependentWord.setRelation(xcompWord.getRelation());
				dependentWord.setHead(xcompWord.getHead());
				xcompWord.setRelation("case");
				xcompWord.setHead(dependentWord.getId());
			    }
			else
			    {
				//something strange going on - mark it
				xcompWord.setToken("@@@"+ xcompWord.getToken()+"@@@");

			    }


		    }
		else {

		    for (int k=0; k < dependentWords.size(); k++)
			{
			    Word dependentWord = dependentWords.get(k);
			    dependentWord.setHead(xcompWord.getHead());
			    if (dependentWord.getRelation().equals("dobj"))
				{
				    xcompWord.setHead(dependentWord.getId());
				    dependentWord.setRelation(xcompWord.getRelation());
				}
			}
		    xcompWord.setRelation("case");
		}
	    }

	    }
	    }
    }

    
    public static void processPPXComp()
    {
	Sentence sentence;

	for (int i=0; i < sentences.size(); i++)
	    {
		sentence = (Sentence) sentences.get(i);

		//does the sentence contain a case relation?
		//if it does, find the nmod that it is related to
		ArrayList<Word> xcompWords = sentence.findWords("xcomp:pred");
		for (int j=0; j < xcompWords.size(); j++)
		    {
			Word xcompWord = xcompWords.get(j);
			if (xcompWord.getPosCoarse().equals("ADP"))
			    {

				ArrayList<Word> dependentWords = sentence.findDependents(xcompWord);
				if (dependentWords.size() == 0)
				    {
					//no nmod
					if (isProPrep(xcompWord.getToken(),xcompWord.getLemma()))
					    {
						xcompWord.setToken("%%%"+ xcompWord.getToken()+"%%%");
					    }
					    else
					    {
						//something strange going on - mark it
						xcompWord.setToken("$$$"+ xcompWord.getToken()+"$$$");
					    }
				    }
				else if (dependentWords.size() == 1)
				    {
					Word dependentWord = dependentWords.get(0);
					dependentWord.setRelation(xcompWord.getRelation());
					dependentWord.setHead(xcompWord.getHead());
					xcompWord.setRelation("case");
					xcompWord.setHead(dependentWord.getId());
				 
					
				    }
				else {
				    
					for (int k=0; k < dependentWords.size(); k++)
					    {
						Word dependentWord = dependentWords.get(k);
						dependentWord.setHead(xcompWord.getHead());
						if (dependentWord.getRelation().equals("nmod") || dependentWord.getRelation().startsWith("xcomp"))
						    {
							xcompWord.setHead(dependentWord.getId());
							dependentWord.setRelation(xcompWord.getRelation());
						    }
						
						
					    }
					xcompWord.setRelation("case");
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
    
    public static void processProPreps()
    {
        for (int i=0; i < sentences.size(); i++)
        {
            Sentence sentence = sentences.get(i);
            ArrayList<Word> caseWords = sentence.findWords("case");
            for (int j=0; j < caseWords.size(); j++)
            { 
                Word caseWord = caseWords.get(j);
                if (caseWord != null && caseWord.getToken().startsWith("***"))
                {
                    if (isProPrep(caseWord.getToken().substring(3,caseWord.getToken().length()-3),caseWord.getLemma()))
                    {
                         caseWord.setToken(caseWord.getToken().substring(3,caseWord.getToken().length()-3));
                         caseWord.setRelation("nmod:prep");
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

    private static boolean isProPrep(String token, String lemma) 
    {
        //String [] proPrepList = {"agam","agat","aige","aici","againn","agaibh","acu","liom","leat","leis","leí","linn","libh","leo"
                 //,"orm","ort","air","uirthi","orainn","oraibh","orthu","dom","duit","dó","dúinn","daoibh","dóibh","uaim","uait",
        //"uaidh","domsa"};
        String [] prepList = {"do","ag","ar","le","de","faoi","ó","chuig","roimh","i","trí","as","thar"};
        for (int i=0; i < prepList.length;i++)
        {
            if (prepList[i].equalsIgnoreCase(lemma) && !lemma.equalsIgnoreCase(token) && !token.equalsIgnoreCase("lena") && !token.equalsIgnoreCase("lenar") &&
		!token.equalsIgnoreCase("don") && !token.equalsIgnoreCase("den") && !token.equalsIgnoreCase("d'"))
            {
                return true;
            }
        }
        return false; //To change body of generated methods, choose Tools | Templates.
    }

    
}
