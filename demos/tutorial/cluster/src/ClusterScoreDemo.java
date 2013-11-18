import com.aliasi.cluster.AbstractHierarchicalClusterer;
import com.aliasi.cluster.ClusterScore;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.CompleteLinkClusterer;

import com.aliasi.matrix.Matrix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.classify.PrecisionRecallEvaluation;

public class ClusterScoreDemo {

    public static void main(String[] args) {
        demoBasicScoring();
    }

    static void demoBasicScoring() {
        System.out.println("\nCLUSTER SCORE DEMO");

        Set<String> referenceCluster1 = new HashSet<String>();
        referenceCluster1.add("1");
        referenceCluster1.add("2");
        referenceCluster1.add("3");

        Set<String> referenceCluster2 = new HashSet<String>();
        referenceCluster2.add("4");
        referenceCluster2.add("5");

        Set<String> referenceCluster3 = new HashSet<String>();
        referenceCluster3.add("6");

        Set<Set<String>> referencePartition = new HashSet<Set<String>>();
        referencePartition.add(referenceCluster1);
        referencePartition.add(referenceCluster2);
        referencePartition.add(referenceCluster3);

        Set<String> responseCluster1 = new HashSet<String>();
        responseCluster1.add("1");
        responseCluster1.add("2");

        Set<String> responseCluster2 = new HashSet<String>();
        responseCluster2.add("3");
        responseCluster2.add("4");
        responseCluster2.add("5");
        responseCluster2.add("6");

        HashSet<Set<String>> responsePartition = new HashSet<Set<String>>();
        responsePartition.add(responseCluster1);
        responsePartition.add(responseCluster2);

        ClusterScore<String> score 
            = new ClusterScore<String>(referencePartition,responsePartition);

        System.out.println("\nReference Partition = " + referencePartition);
        System.out.println("Response Partition = " + responsePartition);

        PrecisionRecallEvaluation prEval = score.equivalenceEvaluation();
        System.out.println();
        System.out.println("\nEquivalence Relation Evaluation");
        System.out.println(prEval.toString());

        System.out.println("\nMUC Measures");
        System.out.println("  MUC Precision = " 
                           + score.mucPrecision());
        System.out.println("  MUC Recall = " 
                           + score.mucRecall());
        System.out.println("  MUC F(1) = " 
                           + score.mucF());
        
        System.out.println("\nB-Cubed Measures");
        System.out.println("  Cluster Averaged Precision = " 
                           + score.b3ClusterPrecision());
        System.out.println("  Cluster Averaged Recall = " 
                           + score.b3ClusterRecall());
        System.out.println("  Cluster Averaged F(1) = " 
                           + score.b3ClusterF());
        System.out.println("  Element Averaged Precision = " 
                           + score.b3ElementPrecision());
        System.out.println("  Element Averaged Recall = " 
                           + score.b3ElementRecall());
        System.out.println("  Element Averaged F(1) = " 
                           + score.b3ElementF());

    }

    /*

    static void demoScatterCophenetic() {
        Matrix cityProximityMatrix = CityDistances.getDistanceMatrix();

        System.out.println("\nComplete Link Within-Cluster Point Scatters");
        demoScatterCophenetic(new CompleteLinkClusterer(Double.MAX_VALUE),
                              cityProximityMatrix);

        System.out.println("\nSingle Link Within-Cluster Point Scatters");
        demoScatterCophenetic(new SingleLinkClusterer(Double.MAX_VALUE),
                              cityProximityMatrix);
    }

    static void demoScatterCophenetic(AbstractHierarchicalClusterer clusterer,
                                      Matrix matrix) {
        Dendrogram dendrogram 
            = clusterer.completeCluster(matrix);

        demoScatters(dendrogram,matrix);

        double cc = dendrogram.copheneticCorrelation(matrix);
        System.out.println("Cophenetic Correlation=" + cc);
    }

    static void demoScatters(Dendrogram dendrogram, Matrix matrix) {
        int maxPartitions = dendrogram.size();
        // short form
        for (int i = 2; i < maxPartitions; ++i) {
            double scatter = dendrogram.withinClusterScatter(i,matrix);
            System.out.println(i + " " + scatter);
            Dendrogram[] eqClasses = dendrogram.partition(i);
        }
        System.out.println();

        // long form
        for (int i = 2; i < maxPartitions; ++i) {
            double scatter = dendrogram.withinClusterScatter(i,matrix);
            System.out.println(i + " clusters with scatter=" + scatter);
            Dendrogram[] eqClasses = dendrogram.partition(i);
            for (int j = 0; j < i; ++j)
                System.out.println("    " +
                                   + eqClasses[j].totalScatter(matrix)
                                   + ": " + eqClasses[j].memberSet());
        }
    }

    */
}
