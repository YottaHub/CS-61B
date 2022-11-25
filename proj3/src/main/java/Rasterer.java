import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel
     *     (LonDPP) possible, while still covering less than or equal to the amount of
     *     longitudinal distance per pixel in the query box for the user viewport size. </li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the
     *     above condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        double ullon = params.get("ullon"), lrlon = params.get("lrlon"),
                ullat = params.get("ullat"), lrlat = params.get("lrlat");
        // invalid query box
        if (ullon > lrlon || ullat < lrlat) {
            results.put("query_success", false);
            return results;
        }
        // query box is completely outside of boundary
        if (ullon > MapServer.ROOT_LRLON || lrlon < MapServer.ROOT_ULLON
                || ullat < MapServer.ROOT_LRLAT || lrlat > MapServer.ROOT_ULLAT) {
            results.put("query_success", false);
            return results;
        }
        double width = params.get("w");
        double precision = (MapServer.ROOT_ULLON - MapServer.ROOT_LRLON) / MapServer.TILE_SIZE;
        double lonDPP = (ullon - lrlon) / width;
        // calculate depth
        int depth = 0, numOfIntervals = 1;
        while (precision <= lonDPP && depth < 7) {
            precision /= 2;
            numOfIntervals *= 2;
            depth++;
        }
        results.put("depth", depth);
        // calculate four corner grids
        double lonInterval = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / numOfIntervals;
        double latInterval = (MapServer.ROOT_LRLAT - MapServer.ROOT_ULLAT) / numOfIntervals;
        int lonStart = Math.max(0, (int) Math.floor((ullon
                - MapServer.ROOT_ULLON) / lonInterval));
        results.put("raster_ul_lon", MapServer.ROOT_ULLON + lonInterval * lonStart);
        int lonEnd = Math.min(numOfIntervals, (int) Math.ceil((lrlon
                - MapServer.ROOT_ULLON) / lonInterval));
        results.put("raster_lr_lon", MapServer.ROOT_ULLON + lonInterval * lonEnd);
        int latStart = Math.max(0, (int) Math.floor((ullat
                - MapServer.ROOT_ULLAT) / latInterval));
        results.put("raster_ul_lat", MapServer.ROOT_ULLAT + latStart * latInterval);
        int latEnd = Math.min(numOfIntervals, (int) Math.ceil((lrlat
                - MapServer.ROOT_ULLAT) / latInterval));
        results.put("raster_lr_lat", MapServer.ROOT_ULLAT + latInterval * latEnd);
        // form required render grids by row
        String[][] ids = new String[latEnd - latStart][lonEnd - lonStart];
        for (int i = 0; i < lonEnd - lonStart; i++) {
            for (int j = 0; j < latEnd - latStart; j++) {
                ids[j][i] = String.format("d%d_x%d_y%d.png", depth, lonStart + i, latStart + j);
            }
        }
        results.put("render_grid", ids);
        results.put("query_success", true);
        return results;
    }
}
