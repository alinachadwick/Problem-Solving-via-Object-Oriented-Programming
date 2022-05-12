import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Alina Chadwick and Aspen Anderson, CS10, 21W
 */
public class HMMPS5 {

    //method to perform Viterbi decoding to find the best sequence of tags for a line
    public  static List<String> viterbi(String line, Map<String, Map<String, Double>> observations, Map<String, Map<String, Double>> transitions){
        List<String> tags = new ArrayList<>();      //keeps track of tags
        List<Map<String, String>> backPointer = new ArrayList<>();      //keeps track of previous values

        Set<String> currStates = new HashSet<>();
        currStates.add("#");        //initial state

        Map<String, Double> currScores = new HashMap<>();
        currScores.put("#", 0.0); //initial state mapped to the score

        String[] split = line.split(" ");
        double observationScore;
        double nextScore;

        for (int i=0; i< split.length; i++){    //loops through the file
            Set nextStates = new HashSet<>();
            Map<String, Double> nextScores = new HashMap<>();

            Map<String, String> backMap = new HashMap<>();
            backPointer.add(backMap);

            for (String currState: currStates) {     //loops through each state
                    //loops through the next state via transitions
                    for (String nextState : transitions.get(currState).keySet()) {
                        nextStates.add(nextState);

                        //check to see if we had viewed the state
                        if (observations.get(nextState).containsKey(split[i])) {
                            observationScore = observations.get(nextState).get(split[i]);       //creates current score

                            //updates next score based on current score and transitions map
                            nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) + observationScore;

                        } else {
                            //sets the current score to -100 with unseen word penalty
                            nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) - 100;
                        }

                        //updates the next scores map based on calculations above
                        //updates backPointer
                        if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                            nextScores.put(nextState, nextScore);
                            backMap.put(nextState, currState);
                        }

                    }
                }

            //updates states and scores
            currStates = nextStates;
            currScores = nextScores;
//            System.out.println(currScores);

        }

        //deciding which tag is best
        String bestTag = "";       //start with an empty string

        for (String state: currStates){     //loop through possible states
            //set the best tag based on score map value
            if (bestTag.equals("") || currScores.get(state)> currScores.get(bestTag)){
                bestTag = state;        //updates best tag
            }
        }

        //we look at the possible states for the last observation, so we use lifo and loop through the file backwards
        Stack<String> evaluate = new Stack<>();     //lifo
        evaluate.push(bestTag);     //add the best tag

        //loop through file backwards
        for (int i=split.length-1; i>=0; i--) {
            //use back pointer to get previous state's map
            Map<String, String> currMap = backPointer.get(i);
            String nextBest = currMap.get(bestTag);
            evaluate.push(nextBest);

            //reset the tag
            bestTag = nextBest;
        }

        while (!evaluate.isEmpty()) {
            String popped = evaluate.pop();
            //once we reach the end in back tracing, add to tags
            if (!popped.equals("#")) {
                tags.add(popped);
            }
        }
        return tags;

    }

    //used to test a line coded into main
    public static void testLine (String line, HashMap<String, Map<String, Double>> observations, HashMap<String, Map<String, Double>> transitions){
        System.out.println(viterbi(line, observations, transitions));
    }

    //training involving observation map
    public static HashMap<String, Map<String, Double>> observe(String wordFile, String speechPartFile){
        HashMap<String, Map<String, Double>> observations = new HashMap<>();

        try{
            BufferedReader wordInput = new BufferedReader(new FileReader(wordFile));
            BufferedReader speechInput = new BufferedReader(new FileReader(speechPartFile));
             String wordReader = wordInput.readLine();
             String speechReader = speechInput.readLine();

             //reads both files
            while (wordReader != null && speechReader != null){
                 wordReader = wordReader.toLowerCase();
                 String [] words = wordReader.split(" ");
                 String [] speech = speechReader.split(" ");

                 // if the initial state exists in the observation map
                    if (observations.containsKey("#")){
                        Map<String, Double> obsMap = new HashMap<>();
                        obsMap = observations.get("#");

                        //updates the inner map based on whether it contains the word
                        if (obsMap.containsKey(words[0])){
                            obsMap.put(words[0], obsMap.get(words[0]) + 1);
                        }
                        //adds the word to the inner map
                        else{
                            obsMap.put(words[0], 1.0);
                        }
                        //updates outer map
                        observations.put("#", obsMap);
                    }

                    //if the initial state does not exist in the observation map
                    else{
                        HashMap<String, Double> elseMap = new HashMap<>();
                        elseMap.put(words[0], 1.0);
                        observations.put("#", elseMap);     //creates base map
                    }

                    //loops through the file
                    for (int i = 0; i< words.length - 1; i++){
                        Map<String, Double> keepTrack = new HashMap<>();

                        // checks to see if the initial part of speech is in the map
                            if (observations.containsKey(speech[i])){
                                keepTrack = observations.get(speech[i]);

                                if(keepTrack.containsKey(words[i])){
                                    keepTrack.put(words[i], keepTrack.get(words[i]) + 1);
                                }

                                else{
                                    keepTrack.put(words[i], 1.0);
                                }
                            }

                            // if not in the map, set a base value of 1
                            else{
                                keepTrack.put(words[i], 1.0);
                            }

                            //add the inner map to the observation map
                            observations.put(speech[i], keepTrack);
                    }
                    //updates the file reader
                    wordReader = wordInput.readLine();
                    speechReader = speechInput.readLine();
             }
            //closes files
             wordInput.close();
             speechInput.close();
    }

   catch(FileNotFoundException exception){
       System.out.println("file not found!");
   }
        catch(IOException e){
            System.out.println("cannot read!");
        }
        return observations;
    }

//training involving transition map
public static HashMap<String, Map<String, Double>> transition (String transitionFile){
    HashMap<String, Map<String, Double>> transitions = new HashMap<>();

    try{
        BufferedReader trInput = new BufferedReader(new FileReader(transitionFile));
        String trLine = trInput.readLine();

        //reads through the file
        while (trLine != null){
            String [] state = trLine.split(" ");

            //loops through the file
            for (int i = 0; i< state.length - 1; i++){

                //first state
                if (i == 0){
                    //updates initial map, if it already exists
                    if (transitions.containsKey("#")){
                        Map<String, Double> tMap = transitions.get("#");

                        //updates inner map
                        if (tMap.containsKey(state[0])){
                            tMap.put(state[0], tMap.get(state[0]) + 1);
                        }

                        //sets inner map
                        else{
                            tMap.put(state[0], 1.0);
                            transitions.put("#", tMap);
                        }
                    }

                    //sets the initial map otherwise
                    else{
                        HashMap<String, Double> tMap = new HashMap<>();
                        tMap.put(state[0], 1.0);
                        transitions.put("#", tMap);
                    }
                }

                String nextState = state[i+1];

                //update map if the state exists
                if (transitions.containsKey(state[i])){
                    Map<String, Double> tMap = transitions.get(state[i]);

                    //updates next state if it exists
                    if (tMap.containsKey(nextState)) {
                    tMap.put(nextState, tMap.get(nextState) + 1);
                    transitions.put(state[i], tMap);
                    }
                    //sets next state if it does not exist
                    else{
                        tMap.put(nextState, 1.0);
                        transitions.put(state[i], tMap);
                    }
                    }

                // if the state does not exist, create it in the map
                else{
                    HashMap<String, Double> tMap = new HashMap<>();
                    tMap.put(nextState, 1.0);
                    transitions.put(state[i], tMap);
                }
                }

            //updates file reader
            trLine = trInput.readLine();
            }

        //close file
        trInput.close();

    }
    catch (FileNotFoundException e){
        System.out.println("file not found!");
    }
    catch (IOException e){
        System.out.println("cannot read!");
    }
    return transitions;
    }

    //uses java Math.log to convert to log probabilities
public static HashMap<String, Map<String, Double>> log(HashMap<String, Map<String, Double>> map){

        //loops through the map
        for (String key : map.keySet()){

            //creates a sub map
            Map<String, Double> subMap = map.get(key);
            Double sum = 0.0;

            //loops through sub map
            for (String subKey : subMap.keySet()){
                //updates sum based on the key
                sum += subMap.get(subKey);

                // uses Math.log to update the sub map
                subMap.put(subKey, Math.log(subMap.get(subKey)/ sum));
//                System.out.println(Math.log(subMap.get(subKey)/ sum));
            }
        }
        return map;
}

    //allows the user to make sentences and obtain their parts of speech
    public static void consoleInput(HashMap<String, Map<String, Double>> observations, HashMap<String, Map<String, Double>> transitions) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Play if you dare. Press q if you don't.");
        String input = scan.nextLine();

        if (input.equals("q")) {
            scan.close();       //quitters
        }
        else {
            System.out.println(viterbi(input, observations, transitions));
        }

    }

    // a file-based test method to evaluate the performance on a pair of test files
    public static void fileInput(String words, String speechParts, HashMap<String, Map<String, Double>> observations, HashMap<String, Map<String, Double>> transitions) {
        BufferedReader wordsTest;
        BufferedReader speechTest;

        try {
            double all = 0.0;       // all of the words in file
            double right = 0.0;     // success rate

            // open both files for reading
            wordsTest = new BufferedReader(new FileReader(words));
            String word;

            speechTest = new BufferedReader(new FileReader(speechParts));
            String speech;

            // ends when the file reader reaches the end of the file
            while (( word = wordsTest.readLine()) != null && (speech = speechTest.readLine()) != null) {
                    word = word.toLowerCase();
                    List<String> vit = viterbi(word, observations, transitions);
                    String[] split = speech.split(" ");
                    all += split.length; //cumulative sum of all labels in file

                    //loops through the tags list
                    for (int i=0; i<vit.size(); i++) {
                        //if the label is correct, add to right sum
                        if (vit.get(i).equals(split[i])) {
                                right += 1.0;
                            }
                        }

                }
            System.out.println("Our ratio is as follows: " + (right/all) + " Nice.");
            System.out.println("We hit " + right + " of " + all + ". Take the W, pretty effective.");

            wordsTest.close();
            speechTest.close();
            }
        catch (IOException e) {
            System.out.println("IO! You did something wrong kid!");
            }
        }



    public static void main(String[] args){
//        HashMap<String, Map<String, Double>> simpleob = observe("inputs/simple-test-sentences.txt", "inputs/simple-test-tags.txt");
//        HashMap<String, Map<String, Double>> simpletr = transition("inputs/simple-test-tags.txt");
//        simpleob = log(simpleob);
//        simpletr = log(simpletr);
//        testLine("my dog trains to bark .", simpleob, simpletr);
//        testLine(" i watch your dog for money .", simpleob, simpletr);
//        fileInput("inputs/simple-test-sentences.txt", "inputs/simple-test-tags.txt", simpleob, simpletr);

        HashMap<String, Map<String, Double>> observations = observe("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt");
        HashMap<String, Map<String, Double>>  transitions = transition("inputs/brown-test-tags.txt");
        observations = log(observations);
        transitions = log(transitions);
        testLine("I want to prance around Europe for an undefined period of time .", observations, transitions);
        testLine("I want to go to Dartmouth for spring .", observations, transitions);
        testLine("Alina is indecisive", observations, transitions);
        testLine("Aspen is from Boulder, Colorado .", observations, transitions);
        testLine("Aspen does not know how to ski", observations, transitions);
//        consoleInput(observations, transitions);
        fileInput("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt", observations, transitions);
    }




}