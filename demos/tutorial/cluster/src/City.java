import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.SingleLinkClusterer;

import com.aliasi.util.Distance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class City {

    final String mName;
    final double mLatitudeDegrees;
    final double mLongitudeDegrees;

    public City(String name, 
                double latitudeDegrees,
                double longitudeDegrees) {
        mName = name;
        mLatitudeDegrees = latitudeDegrees;
        mLongitudeDegrees = longitudeDegrees;
    }

    public String toString() {
        return mName;
    }
    

    static final City[] CITIES = new City[] {
        new City("NewYork", 40 + 43/60.0, 74 + 0/60.0),
        new City("Rochester", 43 + 9/60.0, 77 + 37/60.0),
        new City("Toronto", 43 + 39/60.0, 79 + 23/60.0),
        new City("Philadelpha", 39 + 57/60.0, 75 + 10/60.0),
        new City("Boston", 42 + 22/60.0, 71 + 4/60.0),
        new City("PaloAlto", 37 + 27/60.0, 122 + 9/60.0),
        new City("Berkeley", 37 + 52/60.0, 122 + 16/60.0),
        new City("MarinaDelRey", 33 + 58/60.0, 118 + 27/60.0),
        new City("Boulder", 40 + 1/60.0, 105 + 16/60.0),
        new City("Baltimore", 39 + 17/60.0, 76 + 37/60.0),
        new City("Pittsburgh", 40 + 17/60.0, 80 + 37/60.0),
        new City("Chicago", 41 + 51/60.0, 87 + 39/60.0),
        new City("Austin", 30 + 16/60.0, 97 + 45/60.0),
        new City("Seattle", 47 + 36/60.0, 122 + 20/60.0),
        new City("Portland", 45 + 31/60.0, 122 + 41/60.0),
        new City("LasCruces", 32 + 19/60.0, 106 + 47/60.0),
        new City("AnnArbor", 42 + 17/60.0, 83 + 45/60.0),
        new City("Columbus",  39 + 58/60.0, 83 + 0/60.0),
        new City("Urbana", 40 + 7/60.0, 88 + 12/60.0),
        new City("Ithaca", 42 + 26/60.0, 76 + 30/60.0) 
    };
    
    static final Distance<City> GREAT_CIRCLE_DISTANCE
        = new Distance<City>() {
            static final double EARTH_RADIUS_MILES = 3963;
            public double distance(City c1, City c2) {
                double lat1 = Math.toRadians(c1.mLatitudeDegrees);
                double lon1 = Math.toRadians(c1.mLongitudeDegrees);
                double lat2 = Math.toRadians(c2.mLatitudeDegrees);
                double lon2 = Math.toRadians(c2.mLongitudeDegrees);
                return EARTH_RADIUS_MILES
                    * Math.acos( ( Math.cos(lat1) 
                                   * Math.cos(lat2) 
                                   * Math.cos(lon2-lon1) )
                                 + 
                                 ( Math.sin(lat1) 
                                   * Math.sin(lat2) ) );
            }
        };

    
    public static void main(String[] args) {
        Set<City> citySet = new HashSet<City>(Arrays.<City>asList(CITIES));
        for (City city1 : citySet) 
            for (City city2 : citySet)
                if (city1.mName.compareTo(city2.mName) < 0)
                    System.out.println("distance(" + city1 + "," + city2 + ")="
                                       + GREAT_CIRCLE_DISTANCE.distance(city1,city2));

        HierarchicalClusterer<City> clClusterer 
            = new CompleteLinkClusterer<City>(GREAT_CIRCLE_DISTANCE);
        Dendrogram<City> clDendrogram = clClusterer.hierarchicalCluster(citySet);
        System.out.println("\nCOMPLETE LINK");
        System.out.println(clDendrogram.prettyPrint());
        
        HierarchicalClusterer<City> slClusterer
            = new SingleLinkClusterer<City>(GREAT_CIRCLE_DISTANCE);
        Dendrogram<City> slDendrogram = slClusterer.hierarchicalCluster(citySet);
        System.out.println("\nSINGLE LINK");
        System.out.println(slDendrogram.prettyPrint());
    }


}
