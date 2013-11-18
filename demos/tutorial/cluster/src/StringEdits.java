import com.aliasi.cluster.Clusterer;
import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.SingleLinkClusterer;

import com.aliasi.spell.EditDistance;
import com.aliasi.spell.FixedWeightEditDistance;

import com.aliasi.util.Distance;

import java.util.Set;
import java.util.HashSet;

public class StringEdits {

    static final Distance<CharSequence> EDIT_DISTANCE
        = new EditDistance(false);

    public static void main(String[] args) {
        // parse out input set
        Set<String> inputSet = new HashSet<String>();
        for (String s : args[0].split(",")) 
            inputSet.add(s);

        // set up max distance
        int maxDistance = args.length == 1
            ? Integer.MAX_VALUE
            : Integer.valueOf(args[1]);

        // dump off-diagonal upper triangular distance matrix
        for (String s1 : inputSet) 
            for (String s2: inputSet)
                if (s1.compareTo(s2) < 0)
                    System.out.println("distance(" + s1 + "," + s2 + ")="
                                       + EDIT_DISTANCE.distance(s1,s2));
        

        // Single-Link Clusterer
        HierarchicalClusterer<String> slClusterer 
            = new SingleLinkClusterer<String>(maxDistance,
                                              EDIT_DISTANCE);

        // Complete-Link Clusterer
        HierarchicalClusterer<String> clClusterer
            = new CompleteLinkClusterer<String>(maxDistance,
                                                EDIT_DISTANCE);

        // Hierarchical Clustering
        Dendrogram<String> slDendrogram
            = slClusterer.hierarchicalCluster(inputSet);
        System.out.println("\nSingle Link Dendrogram");
        System.out.println(slDendrogram.prettyPrint());

        Dendrogram<String> clDendrogram
            = clClusterer.hierarchicalCluster(inputSet);
        System.out.println("\nComplete Link Dendrogram");
        System.out.println(clDendrogram.prettyPrint());

        // Dendrograms to Clusterings
        System.out.println("\nComplete Link Clusterings");
        for (int k = 1; k <= clDendrogram.size(); ++k) {
            Set<Set<String>> clKClustering = clDendrogram.partitionK(k);
            System.out.println(k + "  " + clKClustering);
        }

        System.out.println("\nSingle Link Clusterings");
        for (int k = 1; k <= slDendrogram.size(); ++k) {
            Set<Set<String>> slKClustering = slDendrogram.partitionK(k);
            System.out.println(k + "  " + slKClustering);
        }



        Set<Set<String>> clClustering
            = clClusterer.cluster(inputSet);
        System.out.println("\n\nComplete Link Clustering");
        System.out.println(clClustering);

        Set<Set<String>> slClustering
            = slClusterer.cluster(inputSet);
        System.out.println("\nSingle Link Clustering");
        System.out.println(slClustering);

        
    }

}
