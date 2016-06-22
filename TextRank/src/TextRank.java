
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.*;
import org.jgrapht.graph.*;


public class TextRank {
    public static void main(String args[]) {
    	String[] documents = {"Document 1 text over here", "Document 2 text over here"};
		
    	StanfordLemmatizer slem = new StanfordLemmatizer();
    	List<ArrayList<String>> lemmatizedTickets = new ArrayList<ArrayList<String>>();
    	List<String> filteredWords = new ArrayList<String>();
    	
    	
    	// Stopwords removal
		Scanner sc = null;
		
		try {
			
			sc = new Scanner(new File("SmartStoplist.txt"));
			sc.nextLine();
						
			Map<String, Integer> stopwords = new HashMap<String, Integer>();
			while(sc.hasNextLine()){
				stopwords.put(sc.nextLine(),1);
			} 
			
			for(String ticket:documents){
				ArrayList<String> lemmatizedTicket = slem.lemmatize(ticket);
	    		lemmatizedTickets.add(lemmatizedTicket);
				for(int i=0; i < lemmatizedTicket.size(); i++){
					if(stopwords.get(lemmatizedTicket.get(i)) == null){
						String word = lemmatizedTicket.get(i).toLowerCase();
						if(!filteredWords.contains(word))
							filteredWords.add(word);
						//System.out.print(stemmed_words[i]+" ");
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// view the filtered words,has only Nouns & Verbs
    	System.out.println(filteredWords);	
    	
        // constructs a directed graph with the specified vertices and edges
        DirectedGraph<String, DefaultEdge> directedGraph =
            new DefaultDirectedGraph<String, DefaultEdge>
            (DefaultEdge.class);
        
        // initialize vertices
        Map<String, Double> scores = new HashMap<String, Double>();
        for(String s: filteredWords){
        	directedGraph.addVertex(s);
        	scores.put(s, 1.0);
        }
        	
        // initialize edges
        for(int j=0; j< lemmatizedTickets.size(); j++){
        	ArrayList<String> lemmaT = lemmatizedTickets.get(j);
        	for(String word1 : lemmaT){
        		if(filteredWords.contains(word1)){
        			int index = lemmaT.indexOf(word1);
        			int startIndex = index- 4;
        			int endIndex = index + 4;
        			if(startIndex < 0)
        				startIndex = 0;
        			if(endIndex >= lemmaT.size())
        				endIndex = lemmaT.size()-1;
        			for(int i=startIndex; i<=endIndex; i++){
        				String word2 = lemmaT.get(i);
        				if(filteredWords.contains(word2) && !word1.equals(word2)){
        					directedGraph.addEdge(word1, word2);
        					directedGraph.addEdge(word2, word1);
        				}
        			}
        		}
        	}
        }
        
        //TextRank algorithm
        double dampingFactor = 0.85;
        Set<String> vertexSet = directedGraph.vertexSet();
        for(int i=0; i<20; i++){
        	for(String vertex : vertexSet){
        		Set<DefaultEdge> incomingEdges = directedGraph.incomingEdgesOf(vertex);
        		double score = 0.0;
        		for(Iterator<DefaultEdge> it = incomingEdges.iterator(); it.hasNext();){
        			String sourceVertex = it.next().toString().replaceAll("[\\p{Punct}]", " ").split(" ")[1];
//        			System.out.println(sourceVertex);
        			score += scores.get(sourceVertex)/directedGraph.outDegreeOf(sourceVertex);
        		}
        		score = score*dampingFactor + (1-dampingFactor);
        		scores.put(vertex, score);
        	}
        }
        
        List<Map.Entry<String, Double>> sortedScores = new LinkedList<Map.Entry<String, Double>>(scores.entrySet());
        sortedScores.sort(new Comparator<Map.Entry<String, Double>>(){

			@Override
			public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
        	
        });
        for(Map.Entry<String, Double> entry : sortedScores){
        	System.out.println(entry.getKey()+" "+entry.getValue());
        }
    }
}
