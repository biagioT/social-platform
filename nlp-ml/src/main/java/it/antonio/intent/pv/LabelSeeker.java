package it.antonio.intent.pv;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.primitives.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LabelSeeker {
    private Set<String> intentList;
    private WeightLookupTable<VocabWord> lookupTable;

    public LabelSeeker(Set<String> intentList, WeightLookupTable<VocabWord> weightLookupTable) {
        if (intentList.isEmpty()) throw new IllegalStateException("You can't have 0 labels used for ParagraphVectors");
        this.lookupTable = weightLookupTable;
        this.intentList = intentList;
    }

    public Map<String, Double> getScores(INDArray vector) {
        Map<String, Double> result = new HashMap<String, Double>();
        for (String intent: intentList) {
            INDArray vecLabel = lookupTable.vector(intent);
            if (vecLabel == null) throw new IllegalStateException("Intent '"+ intent+"' has no known vector!");

            double sim = Transforms.cosineSim(vector, vecLabel);
            result.put(intent, sim);
        }
        return result;
    }
}