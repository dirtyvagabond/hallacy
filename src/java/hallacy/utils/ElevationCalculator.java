package hallacy.utils;

import java.util.HashMap;
import java.util.List;

public interface ElevationCalculator {

  //Each String in coordinates is of the form "<LATITUDE>,<LONGITUDE>"; e.g. "34.1234,-118.1243"
  public HashMap<String, Double> getElevations(List<String> coordinates);

}
