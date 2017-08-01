package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.nlp.ColumnFormat;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ColumnFormatReader;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Xuanyu on 7/10/2017.
 * This is the Tester Class
 * It requires untrained classifiers generated directly by LBJava
 */
public class BIOTester {
    public static String getPath(String mode, String corpus, int fold){
        if (corpus.equals("ERE")) {
            if (mode.equals("train")) {
                return "data/ere/cv/train/" + fold;
            }
            else if (mode.equals("eval")) {
                return "data/ere/cv/eval/" + fold;
            } else {
                return "INVALID_PATH";
            }
        }
        else if (corpus.equals("ACE")){
            if (mode.equals("train")) {
                return "data/partition_with_dev/train/" + fold;
            }
            else if (mode.equals("eval")) {
                return "data/partition_with_dev/eval/" + fold;
            }
            else if (mode.equals("dev")){
                return "data/partition_with_dev/dev";
            }
            else{
                return "INVALID_PATH";
            }
        }
        else {
            return "INVALID CORPUS";
        }
    }

    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }


    public static bio_classifier_nam train_nam_classifier(Parser train_parser){
        bio_classifier_nam classifier = new bio_classifier_nam();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)train_parser).id;
        classifier.setLexiconLocation("tmp/bio_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();
        return classifier;
    }

    public static bio_classifier_nom train_nom_classifier(Parser train_parser){
        bio_classifier_nom classifier = new bio_classifier_nom();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)train_parser).id;
        classifier.setLexiconLocation("tmp/bio_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        train_parser.reset();
        classifier.doneWithRound();
        classifier.doneLearning();
        return classifier;
    }

    public static bio_classifier_pro train_pro_classifier(Parser train_parser){
        bio_classifier_pro classifier = new bio_classifier_pro();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)train_parser).id;
        classifier.setLexiconLocation("tmp/bio_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        train_parser.reset();
        classifier.doneWithRound();
        classifier.doneLearning();
        return classifier;
    }

    public static Constituent add_joint_score(Learner a, Learner b, Learner c, Constituent cur){
        double[] scoresToAdd = new double[9];
        ScoreSet scores_a = a.scores(cur);
        Score[] scoresArray_a = scores_a.toArray();
        ScoreSet scores_b = b.scores(cur);
        Score[] scoresArray_b = scores_b.toArray();
        ScoreSet scores_c = c.scores(cur);
        Score[] scoresArray_c = scores_c.toArray();
        for (Score score : scoresArray_a){
            if (score.value.startsWith("B")){
                scoresToAdd[0] = score.score;
            }
            else if (score.value.startsWith("I")){
                scoresToAdd[1] = score.score;
            }
            else{
                scoresToAdd[2] = score.score;
            }
        }
        for (Score score : scoresArray_b){
            if (score.value.startsWith("B")){
                scoresToAdd[3] = score.score;
            }
            else if (score.value.startsWith("I")){
                scoresToAdd[4] = score.score;
            }
            else{
                scoresToAdd[5] = score.score;
            }
        }
        for (Score score : scoresArray_c){
            if (score.value.startsWith("B")){
                scoresToAdd[6] = score.score;
            }
            else if (score.value.startsWith("I")){
                scoresToAdd[7] = score.score;
            }
            else{
                scoresToAdd[8] = score.score;
            }
        }
        String scoreString = "";
        for (double s : scoresToAdd){
            scoreString += Double.toString(s) + ",";
        }
        cur.addAttribute("BIOScores", scoreString);
        cur.addAttribute("A_prediction", a.discreteValue(cur));
        cur.addAttribute("B_prediction", b.discreteValue(cur));
        cur.addAttribute("C_prediction", c.discreteValue(cur));
        return cur;
    }


    public static bio_joint_classifier train_joint_classifier (bio_classifier_nam a, bio_classifier_nom b, bio_classifier_pro c, Parser parser){
        List<Constituent> examples = new ArrayList<>();
        int exampleCount = 0;
        for (Object example = parser.next(); example != null; example = parser.next()){
            Constituent cur = (Constituent)example;
            examples.add(add_joint_score(a, b, c, cur));
            exampleCount ++;
        }
        Parser train_parser = new ArrayToParser(examples);

        bio_joint_classifier classifier = new bio_joint_classifier();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)parser).id;
        classifier.setLexiconLocation("tmp/bio_joint_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_joint_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);

        train_parser.reset();
        classifier.initialize(exampleCount, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        return classifier;
    }

    public static Pair<String, Integer> joint_inference(Constituent t, Learner[] candidates, Classifier classifier){
        if (classifier != null) {
            Constituent target = add_joint_score(candidates[0], candidates[1], candidates[2], t);
            return new Pair<>(classifier.discreteValue(target), -1);
        }

        double highest_start_score = -10.0;

        Map<Integer, Double> remaining = new ConcurrentHashMap<>();
        String[] preBIOLevel1 = new String[3];
        String[] preBIOLevel2 = new String[3];
        for (int i = 0; i < 3; i++){
            preBIOLevel2[i] = "O";
        }
        int chosen = -1;

        for (int i = 0; i < candidates.length; i++){
            String prediction = candidates[i].discreteValue(t);
            preBIOLevel1[i] = prediction;
            if (prediction.startsWith("B") || prediction.startsWith("U")){
                ScoreSet scores = candidates[i].scores(t);
                Score[] scoresArray = scores.toArray();
                for (Score s : scoresArray){
                    if (s.value.equals(prediction)){
                        remaining.put(i, s.score);
                        if (s.score > highest_start_score){
                            highest_start_score = s.score;
                            chosen = i;
                        }
                    }
                }
            }
        }
        if (chosen == -1){
            return new Pair<>("O", -1);
        }
        else{
            return new Pair<>(candidates[chosen].discreteValue(t), chosen);
        }
        /*
        int next = t.getStartSpan() + 1;
        while (next < t.getTextAnnotation().getSentenceFromToken(t.getStartSpan()).getEndSpan()){
            Constituent current = t.getTextAnnotation().getView("BIO").getConstituentsCoveringToken(next).get(0);
            current.addAttribute("preBIOLevel1", preBIOLevel1[chosen]);
            current.addAttribute("preBIOLevel2", preBIOLevel2[chosen]);
            String chosenPrediction = candidates[chosen].discreteValue(current);
            if (!(chosenPrediction.startsWith("I") || chosenPrediction.startsWith("L"))){
                break;
            }
            List<Integer> remaining_to_remove = new ArrayList<>();
            for (int r : remaining.keySet()){
                if (r == chosen){
                    continue;
                }
                current.addAttribute("preBIOLevel1", preBIOLevel1[r]);
                current.addAttribute("preBIOLevel2", preBIOLevel2[r]);
                String currentPrediction = candidates[r].discreteValue(current);
                if (!(currentPrediction.startsWith("I") || currentPrediction.startsWith("L"))){
                    //remaining.remove(r);
                    remaining_to_remove.add(r);
                }
            }
            for (int tr : remaining_to_remove){
                remaining.remove(tr);
            }
            if (remaining.size() < 2){
                break;
            }
            double highest = -10.0;
            for (int r : remaining.keySet()){
                String prediction = candidates[r].discreteValue(current);
                ScoreSet scores = candidates[r].scores(current);
                Score[] scoresArray = scores.toArray();
                for (Score s : scoresArray){
                    if (s.value.equals(prediction)){
                        double new_score = remaining.get(r) + s.score;
                        remaining.put(r, new_score);
                        if (new_score > highest){
                            highest = new_score;
                            chosen = r;
                        }
                    }
                }
            }
            for (int i = 0; i < 3; i++){
                preBIOLevel2[i] = preBIOLevel1[i];
                preBIOLevel1[i] = candidates[i].discreteValue(current);
            }
            next ++;
        }
        if (chosen == -1){
            return new Pair<>("O", -1);
        }
        return new Pair<>(candidates[chosen].discreteValue(t), chosen);
        //return new Pair<>(output, highest_start_cands);
        */
    }

    public static String inference(Constituent c, Classifier classifier){
        View posView = c.getTextAnnotation().getView(ViewNames.POS);
        List<Constituent> posCons = posView.getConstituentsCoveringToken(c.getStartSpan());
        if (posCons.size() > 0){
            String posLabel = posCons.get(0).getLabel();
            if (posLabel.contains("PRP") || posLabel.contains("WP")){
                //return "B";
            }
        }
        if (!BIOFeatureExtractor.isInPronounList(c).equals("")){
            //return "B";
        }
        return classifier.discreteValue(c);
    }

    public static Constituent getConstituent(Constituent curToken, Classifier classifier, boolean isGold) {
        View bioView = curToken.getTextAnnotation().getView("BIO");
        String goldType = "NA";
        if (!curToken.getAttribute("BIO").startsWith("O")) {
            goldType = (curToken.getAttribute("BIO").split("-"))[1];
        }
        List<String> predictedTypes = new ArrayList<>();
        predictedTypes.add((inference(curToken, classifier).split("-"))[1]);
        int startIdx = curToken.getStartSpan();
        int endIdx = startIdx + 1;
        if (endIdx < bioView.getEndSpan()) {
            String preBIOLevel2_dup = curToken.getAttribute("preBIOLevel1");
            String preBIOLevel1_dup = inference(curToken, classifier);
            Constituent pointerToken = null;
            while (endIdx < bioView.getEndSpan()) {
                pointerToken = bioView.getConstituentsCoveringToken(endIdx).get(0);
                pointerToken.addAttribute("preBIOLevel1", preBIOLevel1_dup);
                pointerToken.addAttribute("preBIOLevel2", preBIOLevel2_dup);
                if (isGold) {
                    String curGold = pointerToken.getAttribute("BIO");
                    if (!(curGold.startsWith("I") || curGold.startsWith("L"))) {
                        break;
                    }
                }
                else {
                    String curPrediction = inference(pointerToken, classifier);
                    if (!(curPrediction.startsWith("I") || curPrediction.startsWith("L"))) {
                        break;
                    }
                    predictedTypes.add(curPrediction.split("-")[1]);
                }
                preBIOLevel2_dup = preBIOLevel1_dup;
                preBIOLevel1_dup = inference(pointerToken, classifier);
                endIdx ++;
            }
        }

        Constituent wholeMention = new Constituent(curToken.getLabel(), 1.0f, "BIO_Mention", curToken.getTextAnnotation(), startIdx, endIdx);
        if (isGold){
            wholeMention.addAttribute("EntityType", goldType);
        }
        else{
            wholeMention.addAttribute("EntityType", mostCommon(predictedTypes));
        }
        return wholeMention;
    }

    public static void test_cv(){
        boolean isBIO = false;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        int violations = 0;

        for (int i = 0; i < 1; i++){

            Parser test_parser = new BIOReader(getPath("eval", "ERE", i), "ERE-EVAL", "ALL", isBIO);
            bio_label output = new bio_label();
            System.out.println("Start training fold " + i);
            Parser train_parser_nam = new BIOReader(getPath("train", "ERE", i), "ERE-TRAIN", "NAM", isBIO);
            Parser train_parser_nom = new BIOReader(getPath("train", "ERE", i), "ERE-TRAIN", "NOM", isBIO);
            Parser train_parser_pro = new BIOReader(getPath("train", "ERE", i), "ERE-TRAIN", "PRO", isBIO);

            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

            Learner[] candidates = new Learner[3];
            candidates[0] = classifier_nam;
            candidates[1] = classifier_nom;
            candidates[2] = classifier_pro;

            int labeled_mention = 0;
            int predicted_mention = 0;
            int correct_mention = 0;

            System.out.println("Start evaluating fold " + i);
            String preBIOLevel1 = "";
            String preBIOLevel2 = "";

            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

                Pair<String, Integer> cands = joint_inference((Constituent)example, candidates, null);

                String bioTag = cands.getFirst();
                if (bioTag.equals("I") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }
                if (bioTag.equals("L") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }
                if (bioTag.equals("U") && (preBIOLevel1.equals("B") || preBIOLevel1.equals("I"))){
                    violations ++;
                }
                if (bioTag.equals("B") && preBIOLevel1.equals("I")){
                    violations ++;
                }
                if (bioTag.equals("O") && (preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }

                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;

                boolean goldStart = false;
                boolean predictedStart = false;

                if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                    predicted_mention ++;
                    predictedStart = true;
                }
                String correctTag = output.discreteValue(example);

                if (correctTag.startsWith("B") || correctTag.startsWith("U")){
                    labeled_mention ++;
                    goldStart = true;
                }

                if (goldStart && predictedStart) {
                    int candidateIdx = cands.getSecond();
                    Constituent goldMention = getConstituent((Constituent)example, candidates[candidateIdx], true);
                    Constituent predictMention = getConstituent((Constituent)example, candidates[candidateIdx], false);
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        //if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                            correct_mention++;
                        //}
                    }
                }
            }
            total_labeled_mention += labeled_mention;
            total_predicted_mention += predicted_mention;
            total_correct_mention += correct_mention;
        }

        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
        System.out.println("violations: " + violations);
    }

    public static void test_ts(){
        boolean isBIO = false;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        int total_correct_nam = 0;
        int total_false_type_nam = 0;
        int total_correct_nom = 0;
        int total_false_type_nom = 0;
        int total_correct_pro = 0;
        int total_false_type_pro = 0;

        Parser test_parser = new BIOReader("data/partition_with_dev/dev", "ACE05-EVAL", "ALL", isBIO);
        Parser train_parser_nam = new BIOReader("data/all", "ACE05-TRAIN", "NAM", isBIO);
        Parser train_parser_nom = new BIOReader("data/all", "ACE05-TRAIN", "NOM", isBIO);
        Parser train_parser_pro = new BIOReader("data/all", "ACE05-TRAIN", "PRO", isBIO);
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

        Learner[] candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        for (Object example = test_parser.next(); example != null; example = test_parser.next()){

            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            Pair<String, Integer> cands = joint_inference((Constituent)example, candidates, null);

            String bioTag = cands.getFirst();
            int learnerIdx = cands.getSecond();

            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = bioTag;

            boolean goldStart = false;
            boolean predictedStart = false;

            if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            String correctTag = ((Constituent)example).getAttribute("BIO");

            if (correctTag.startsWith("B") || correctTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }

            if (goldStart && predictedStart) {
                Constituent goldMention = getConstituent((Constituent)example, candidates[learnerIdx], true);
                Constituent predictMention = getConstituent((Constituent)example, candidates[learnerIdx], false);
                boolean boundaryCorrect = false;
                boolean typeCorrect = false;
                if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                    boundaryCorrect = true;
                }
                if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                    typeCorrect = true;
                }
                if (boundaryCorrect){
                    total_correct_mention ++;
                    if (learnerIdx == 0){
                        total_correct_nam ++;
                    }
                    if (learnerIdx == 1){
                        total_correct_nom ++;
                    }
                    if (learnerIdx == 2){
                        total_correct_pro ++;
                    }
                }
                if (boundaryCorrect && !typeCorrect){
                    if (learnerIdx == 0){
                        total_false_type_nam ++;
                    }
                    if (learnerIdx == 1){
                        total_false_type_nom ++;
                    }
                    if (learnerIdx == 2){
                        total_false_type_pro ++;
                        System.out.println(goldMention.getTextAnnotation().getSentenceFromToken(goldMention.getStartSpan()).toString());
                        System.out.println(goldMention.toString() + " " + goldMention.getAttribute("EntityType") + " " + predictMention.getAttribute("EntityType"));
                        System.out.println();
                    }
                }
            }
        }

        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("NAM: " + total_false_type_nam + "/" + total_correct_nam);
        System.out.println("NOM: " + total_false_type_nom + "/" + total_correct_nom);
        System.out.println("PRO: " + total_false_type_pro + "/" + total_correct_pro);
    }

    public static void test_ere(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        int total_correct_type_match = 0;

        Parser test_parser = new BIOReader("data/ere/data", "ERE-EVAL", "ALL", false);
        Parser train_parser_nam = new BIOReader("data/all", "ACE05-TRAIN", "NAM", false);
        Parser train_parser_nom = new BIOReader("data/all", "ACE05-TRAIN", "NOM", false);
        Parser train_parser_pro = new BIOReader("data/all", "ACE05-TRAIN", "PRO", false);
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        Learner[] candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;

        Map<String, Integer> mentionTypeErrorMap = new HashedMap();
        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            Pair<String, Integer> prediction = joint_inference((Constituent)example, candidates, null);
            String goldTag = ((Constituent)example).getAttribute("BIO");
            String predictedTag = prediction.getFirst();
            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = predictedTag;
            boolean goldStart = false;
            if (goldTag.startsWith("B") || goldTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }
            boolean predictedStart = false;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            boolean correct = false;
            boolean type_match = false;
            if (goldStart && predictedStart){
                Constituent goldMention = getConstituent((Constituent)example, candidates[prediction.getSecond()], true);
                Constituent predictedMention = getConstituent((Constituent)example, candidates[prediction.getSecond()], false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    correct = true;
                }
                if (goldMention.getAttribute("EntityType").equals(predictedMention.getAttribute("EntityType"))){
                    type_match = true;
                }
                if (correct){
                    total_correct_mention ++;
                    if (type_match){
                        total_correct_type_match ++;
                    }
                }
            }
        }
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        System.out.println("Total Correct Type Match: " + total_correct_type_match);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    public static void test_tac(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        Parser train_parser = new BIOReader("data/all", "ACE05", "NOM", false);
        Parser test_parser = new BIOReader("data/tac/2016.nom", "ColumnFormat", "ALL", false);
        bio_classifier_nom classifier = train_nom_classifier(train_parser);
        String preLevel1 = "";
        String preLevel2 = "";
        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preLevel2);
            String predictedTag = inference((Constituent)example, classifier);
            String goldTag = ((Constituent)example).getAttribute("BIO");
            boolean predictedStart = false;
            boolean goldStart = false;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            if (goldTag.startsWith("B") || goldTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }
            if (predictedStart && goldStart){
                Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                Constituent predictedMention = getConstituent((Constituent)example, classifier, false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    total_correct_mention ++;
                }
            }
            preLevel2 = preLevel1;
            preLevel1 = predictedTag;
        }
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    public static void calculateAvgMentionLength(){
        ACEReader aceReader = null;
        try{
            aceReader = new ACEReader("data/all", false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        double nam = 0.0;
        double nom = 0.0;
        double pro = 0.0;
        double namcount = 0.0;
        double nomcount = 0.0;
        double procount = 0.0;
        for (TextAnnotation ta : aceReader){
            for (Constituent c : ta.getView(ViewNames.MENTION_ACE)){
                Constituent ch = ACEReader.getEntityHeadForConstituent(c, ta, "A");
                if (ch.getAttribute("EntityMentionType").equals("NAM")){
                    nam += (double)(ch.getEndSpan() - ch.getStartSpan());
                    namcount += 1.0;
                }
                if (ch.getAttribute("EntityMentionType").equals("NOM")){
                    nom += (double)(ch.getEndSpan() - ch.getStartSpan());
                    nomcount += 1.0;
                }
                if (ch.getAttribute("EntityMentionType").equals("PRO")){
                    pro += (double)(ch.getEndSpan() - ch.getStartSpan());
                    procount += 1.0;
                }
            }
        }
        System.out.println("NAM LENGTH: " + nam / namcount);
        System.out.println("NOM LENGTH: " + nom / nomcount);
        System.out.println("PRO LENGTH: " + pro / procount);
    }

    public static void test_hybrid(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        int total_ace_labeled_mention = 0;
        int total_ere_labeled_mention = 0;
        int total_ace_predicted_mention = 0;
        int total_ere_predicted_mention = 0;
        int total_ace_correct_mention = 0;
        int total_ere_correct_mention = 0;
        int total_ace_type_correct = 0;
        int total_ere_type_correct = 0;


        for (int i = 0; i < 5; i++) {
            Parser test_parser = new BIOCombinedReader(i, "EVAL", "ALL");
            Parser train_parser_nam = new BIOCombinedReader(i, "TRAIN", "NAM");
            Parser train_parser_nom = new BIOCombinedReader(i, "TRAIN", "NOM");
            Parser train_parser_pro = new BIOCombinedReader(i, "TRAIN", "PRO");
            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

            Learner[] candidates = new Learner[3];
            candidates[0] = classifier_nam;
            candidates[1] = classifier_nom;
            candidates[2] = classifier_pro;

            String preBIOLevel1 = "";
            String preBIOLevel2 = "";

            for (Object example = test_parser.next(); example != null; example = test_parser.next()) {

                ((Constituent) example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent) example).addAttribute("preBIOLevel2", preBIOLevel2);

                Pair<String, Integer> cands = joint_inference((Constituent) example, candidates, null);

                String bioTag = cands.getFirst();
                int learnerIdx = cands.getSecond();

                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;

                boolean goldStart = false;
                boolean predictedStart = false;

                if (bioTag.startsWith("B") || bioTag.startsWith("U")) {
                    total_predicted_mention++;
                    if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                            ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                        total_ace_predicted_mention ++;
                    }
                    else {
                        total_ere_predicted_mention ++;
                    }
                    predictedStart = true;
                }
                String correctTag = ((Constituent) example).getAttribute("BIO");

                if (correctTag.startsWith("B") || correctTag.startsWith("U")) {
                    total_labeled_mention++;
                    if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                            ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                        total_ace_labeled_mention ++;
                    }
                    else {
                        total_ere_labeled_mention ++;
                    }
                    goldStart = true;
                }

                if (goldStart && predictedStart) {
                    Constituent goldMention = getConstituent((Constituent) example, candidates[learnerIdx], true);
                    Constituent predictMention = getConstituent((Constituent) example, candidates[learnerIdx], false);
                    boolean boundaryCorrect = false;
                    boolean typeCorrect = false;
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        boundaryCorrect = true;
                    }
                    if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                        typeCorrect = true;
                    }
                    if (boundaryCorrect) {
                        total_correct_mention++;
                        if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                                ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                            total_ace_correct_mention ++;
                        }
                        else {
                            total_ere_correct_mention ++;
                        }
                        if (typeCorrect){
                            if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                                    ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                                total_ace_type_correct ++;
                            }
                            else {
                                total_ere_type_correct ++;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("Total Labeled Mention ACE: " + total_ace_labeled_mention);
        System.out.println("Total Predicted Mention ACE: " + total_ace_predicted_mention);
        System.out.println("Total Correct Mention ACE: " + total_ace_correct_mention);
        System.out.println("Total Type Correct ACE: " + total_ace_type_correct);
        p = (double)total_ace_correct_mention / (double)total_ace_predicted_mention;
        r = (double)total_ace_correct_mention / (double)total_ace_labeled_mention;
        f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("Total Labeled Mention ERE: " + total_ere_labeled_mention);
        System.out.println("Total Predicted Mention ERE: " + total_ere_predicted_mention);
        System.out.println("Total Correct Mention ERE: " + total_ere_correct_mention);
        System.out.println("Total Type Correct ERE: " + total_ere_type_correct);
        p = (double)total_ere_correct_mention / (double)total_ere_predicted_mention;
        r = (double)total_ere_correct_mention / (double)total_ere_labeled_mention;
        f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

    }

    public static void main(String[] args){
        //test_ts();
        test_cv();
        //test_ere();
        //calculateAvgMentionLength();
        //BIOCombinedReader.generateNewSplit();
        //test_hybrid();
    }
}
