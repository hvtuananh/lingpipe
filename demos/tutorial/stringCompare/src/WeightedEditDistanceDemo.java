import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.Distance;

public class WeightedEditDistanceDemo {

    public static void main(String[] args) {
        Distance<CharSequence> d = new CasePunctuationDistance();
        for (String s1 : args)
            for (String s2 : args)
                System.out.printf("%12s  %12s  %5.1f\n",
                                  s1,s2, d.distance(s1,s2));
    }


    static class CasePunctuationDistance extends WeightedEditDistance {

        public double deleteWeight(char c) {
            return (Character.isLetter(c) || Character.isDigit(c))
                ? -1
                : 0;
        }

        public double insertWeight(char c) {
            return deleteWeight(c);
        }

        public double substituteWeight(char cDeleted, char cInserted) {
            return (Character.toLowerCase(cDeleted) == Character.toLowerCase(cInserted))
                ? 0
                : -1;
        }

        public double matchWeight(char cMatched) {
            return 0;
        }

        public double transposeWeight(char cFirst, char cSecond) {
            return Double.NEGATIVE_INFINITY;
        }
    }

}