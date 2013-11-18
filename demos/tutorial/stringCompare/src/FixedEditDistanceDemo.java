import com.aliasi.spell.FixedWeightEditDistance;

import com.aliasi.util.Distance;

public class FixedEditDistanceDemo {


    public static void main(String[] args) {
        double matchWeight = Double.valueOf(args[0]);
        double deleteWeight = Double.valueOf(args[1]);
        double insertWeight = Double.valueOf(args[2]);
        double substituteWeight = Double.valueOf(args[3]);
        double transposeWeight = Double.valueOf(args[4]);

        System.out.printf("match=%4.1f  del=%4.1f  ins=%4.1f  subst=%4.1f  trans=%4.1f\n",
                          matchWeight,
                          deleteWeight,
                          insertWeight,
                          substituteWeight,
                          transposeWeight);

        Distance<CharSequence> fixedEd
            = new FixedWeightEditDistance(matchWeight,
                                          deleteWeight,
                                          insertWeight,
                                          substituteWeight,
                                          transposeWeight);

        System.out.printf("\n%12s  %12s  %5s\n",
                          "String1", "String2",
                          "Dist");
        for (int i = 5; i < args.length; ++i)
            for (int j = 5; j < args.length; ++j)
                System.out.printf("%12s  %12s  %5.1f\n",
                                  args[i],
                                  args[j],
                                  fixedEd.distance(args[i],args[j]));

    }

}