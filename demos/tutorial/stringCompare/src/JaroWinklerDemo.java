import com.aliasi.spell.JaroWinklerDistance;

public class JaroWinklerDemo {

    public static void main(String[] args) {
        JaroWinklerDistance jaroWinkler = JaroWinklerDistance.JARO_WINKLER_DISTANCE;
        System.out.printf("\n%18s  %18s  %5s  %5s\n",
                          "String1", "String2", "Dist", "Prox");
        for (String s : args) {
            String[] pair = s.split("\\|");
            String s1 = pair[0];
            String s2 = pair[1];
            System.out.printf("%18s  %18s  %5.3f  %5.3f\n",
                              s1, s2,
                              jaroWinkler.distance(s1,s2),
                              jaroWinkler.proximity(s1,s2));
        }
    }

}