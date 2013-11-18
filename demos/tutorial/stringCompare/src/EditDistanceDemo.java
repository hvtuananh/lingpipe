import com.aliasi.spell.EditDistance;

import com.aliasi.util.Distance;
import com.aliasi.util.Proximity;

public class EditDistanceDemo {

    static final Distance<CharSequence> D1
        = new EditDistance(false);

    static final Distance<CharSequence> D2
        = new EditDistance(true);

    static final Proximity<CharSequence> P1
        = new EditDistance(false);

    static final Proximity<CharSequence> P2
        = new EditDistance(true);

    public static void main(String[] args) {
        System.out.printf("\n%12s  %12s  %5s  %5s   %5s  %5s\n",
                          "String1", "String2",
                          "Dist1",
                          "Dist2",
                          "Prox1",
                          "Prox2");
        for (String s1 : args)
            for (String s2 : args)
                System.out.printf("%12s  %12s  %5.1f  %5.1f   %5.1f  %5.1f\n",
                                  s1,
                                  s2,
                                  D1.distance(s1,s2),
                                  D2.distance(s1,s2),
                                  P1.proximity(s1,s2),
                                  P2.proximity(s1,s2));

    }

}