import com.aliasi.spell.WeightedEditDistance;

public class BestCaseEditDistance extends WeightedEditDistance {

    public BestCaseEditDistance() {
    }

    public double matchWeight(char c) {
        return 0;
    }

    public double substituteWeight(char cDeleted, char cInserted) {
        return ( Character.toLowerCase(cDeleted)
                 == Character.toLowerCase(cInserted))
            ? 0.0
            : Double.NEGATIVE_INFINITY;
    }

    public double deleteWeight(char c) {
        return Double.NEGATIVE_INFINITY;
    }

    public double insertWeight(char c) {
        return Double.NEGATIVE_INFINITY;
    }

    public double transposeWeight(char cFirst, char cSecond) {
        return Double.NEGATIVE_INFINITY;
    }

}
