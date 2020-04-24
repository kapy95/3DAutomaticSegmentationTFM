package AutomaticSegmentation.limeSeg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.IntStream;

public class generationalChange {
	
	private ArrayList<Individuo> previousPopulation;// list of individuals which participate in the selection
	private ArrayList<Individuo> nextGeneration;//list of individuals which will generate the new population after mutation methods...etc-
	
	public generationalChange(ArrayList<Individuo> population, int nextPopulationSize) {
		super();
		
		this.nextGeneration =new ArrayList<Individuo>(nextPopulationSize);
		
		//First we get the two individuals with maximum score, they will pass directly to the next generation:
		Individuo bestIndividual = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		this.nextGeneration.add(bestIndividual);
		population.remove(bestIndividual);//the best individual is removed temporarily in order to find the second maximum value

		//we do the same for the second best individual:
		Individuo bestIndividual2 = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		this.nextGeneration.add(bestIndividual2);
		population.add(bestIndividual);//we add the bestIndividual again
				
				

		while(this.nextGeneration.size()<nextPopulationSize) {
			
			int selectedMethodForGeneration = 1 + (int)(Math.random() * ((2 - 1) + 1));//this variable represents the method selected to generate one individual of the next population
			//There two methods: only one parent(1) or two parents(2).
			if(selectedMethodForGeneration==1) {//the individual of the next population will be generated taking into account only one individual of the previous population
				
				//elegir la nueva mutacion de un individuo seleccionado por torneo o ruleta
				
			}else {//the individual of the next population will be generated taking into account two individuals of the previous generation
				
				//elegir el método para generar el nuevo individuo
				
			}
		
		}
		
			
	}
	
	
	public void main() {
		
		int numCandidates = Math.round(this.previousPopulation.size()/2);
		int i;
		ArrayList<Individuo> rouletteIndividuals=new ArrayList<Individuo>();
		ArrayList<Individuo> tournamentIndividuals=new ArrayList<Individuo>();
		
		//we will apply one of the selection method to the first half of the population and the other method to the other half:
		for(i=0;i<numCandidates;i++) {
			
			if(i < Math.round(numCandidates/2)){
				
				rouletteIndividuals.add(this.previousPopulation.get(i));
				
			}else {
				
				tournamentIndividuals.add(this.previousPopulation.get(i));
				
			}
		}
		
		//this.rouletteWheelSelection(rouletteIndividuals,)
		
		
		
	}
	
	
	
	public Individuo rouletteWheelSelection(ArrayList<Individuo> pob,int maxRange){//maxRange is a parameter to determine the range of numbers of the wheel selection
		Individuo selectedIndividual = null;
		
		
		int [] numbers=new int[maxRange];//an array will be created with the size of maxRange, whose values will go from 0 to maxRange.
		int z;
		
		for(z=0;z<maxRange;z++) {
			numbers[z]=z;
		}
		
		int globalIndex= 0;
		
		double sum=pob.stream().mapToDouble(a -> a.getScore()).sum();//the sum of all scores is calculated to create likelihoods
		
		int[][]probabilities=new int[pob.size()][];//array with the likelihoods of all candidates;
		
		int i;
		
		for(i=0;i<pob.size();i++) { 
			//for each individual his probability is calcualted:pob.get(i).getScore()/sum)
			//depending on the likelihood they will get more numbers of the array:Math.round( ( (pob.get(i).getScore()/sum) *maxRange) )
			//For example, if there is a population with three individuals: I1 (Score:10), I2 (Score:20), I3(Score:30), the scores sum is 60
			//therefore: P(I1)=10/60=0.16667=0.17, P(I2)=20/60=0.33333...=0.33, P(I3)=30/60=1/2=0.5, where P is the probability of an individual
			 int range=(int) Math.round( ( (pob.get(i).getScore()/sum) *maxRange) );
			 
			//Finally, if we create an array of 10 numbers representing the likelihoods [0,1,2,3...10] their corresponding numbers are:
			//I1:10 x 0.2= 2 numbers (1,2), I2:10 X0.33=3.3=3 3 numbers (3,4,5), and I3: 10 x 0.5 = 5 numbers (6,7,8,9,10)
			 probabilities[i]=Arrays.copyOfRange(numbers, globalIndex, range);//calculus of the corresponding numbers of an individual
			 globalIndex=range+1;
			 
			 //Thus, the higher is the fitness of the individuals, the more is the likelihood of being selected
		}
		
		
		int j=0;
		//Now we create a number between 0 and 100. This number determines where the roulette will stop, the individual with that number will be selected
		int rng = (int) Math.round((Math.random()*((100-0)+1))+0); //it generates a number between 0 and 100,which will establish the candidate to be chosen
		
		while(selectedIndividual == null) {
			
			int[] a = probabilities[j];
			boolean contains = IntStream.of(a).anyMatch(x -> x == rng);
			
			if(contains == true) {
				selectedIndividual=pob.get(j);
			}
			
			
			j=j+1;
		}
		
	
		return selectedIndividual;
	}
	
	
	public ArrayList<Individuo> tournamentSelection(ArrayList<Individuo> pob, int numIndividuals){
		//numIndividuals is the variable to represent the number of individuals selected for the new population after the tournaments
		
		//The fittest individuals of the tournaments will be stored in this variable:
		ArrayList<Individuo> fittestIndividuals = new ArrayList<Individuo>();
		
		//the lower numIndividuals is, the lower it is the likelihood of weak individuals to pass to the next population,
		//since the tournaments will include more individuals fitter than them.
		
		//the comparison will be done bearing in mind tournamentSize, which establishes how many individuals will be involved in one tournament;
		int tournamentSize= Math.round(pob.size()/numIndividuals);
		
		int i=1;
		int j=0;
		
		for(i=0;i<=numIndividuals;i=i+1){
			
				//tournamentIndividuals represents the individuals which will participate in the tournament:
				ArrayList<Individuo> tournamentIndividuals = new ArrayList<Individuo>();
			
				//now we select the individuals using the size of the tournament to select the individuals:
				//for example the selection of 40 candidates of a population of 200 candidates (tournamentSize=5): 
				//tournament 1(starts in individual 0 and finishes the selection in the individual 4):0,1,2,3,4
				//tournament 3(starts in individual 5 and finishes the selection in the individual 9):5,6,7,8,9
				//etc
			
				for(j=i*tournamentSize; j<(j+tournamentSize);j++) {
					tournamentIndividuals.add(pob.get(j));
				}
				
				//Individuo fittestIndividual = Collections.max(tournamentIndividuals, Comparator.comparingDouble(Individuo::getScore));->calcula el maximo 
				
				//now we filter the Individuals basing on the score:
				ArrayList <Individuo>fittestIndividual= new ArrayList<Individuo>();
				tournamentIndividuals.stream().filter(a-> a.getScore()>75).forEach(a->fittestIndividual.add(a));
				
				if(fittestIndividual.isEmpty()) {
					
					 tournamentIndividuals.stream().filter(a-> a.getScore()>25).forEach(a->fittestIndividual.add(a));
					 
				}else if(fittestIndividual.size()>1) {
					
					//if there is more than one individual of the tournament with equal score, it will be selected the fittest individual 
					//depending on the standard deviation:
					
					Collections.sort(fittestIndividual, new Comparator<Individuo>() {
						public int compare(Individuo i1, Individuo i2) {

							return i1.getStdVertex().compareTo(i2.getStdVertex());
						}
			        });
					
					fittestIndividuals.add(fittestIndividual.get(0));
				
				}else {
					//if there is only one candidate, it will be in the first position
					fittestIndividuals.add(fittestIndividual.get(0));
				}
				
	
		}
		
		
		return fittestIndividuals;
		
	}
	
	
	public ArrayList<Individuo> getPreviousPopulation() {
		return previousPopulation;
	}
	
	
	public void setPreviousPopulation(ArrayList<Individuo> previousPopulation) {
		this.previousPopulation = previousPopulation;
	}
	
	
	

}
