package syncer.replica.parser.command.geo;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.ExistType;
import syncer.replica.datatype.command.geo.Geo;
import syncer.replica.datatype.command.geo.GeoAddCommand;
import syncer.replica.util.strings.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * GEOADD key [NX|XX] [CH] longitude latitude member [longitude latitude member ...]
 * Available since 3.2.0.
 *
 * Time complexity: O(log(N)) for each item added, where N is the number of elements in the sorted set.
 *
 * Adds the specified geospatial items (latitude, longitude, name) to the specified key. Data is stored into the key as a sorted set, in a way that makes it possible to query the items with the GEOSEARCH command.
 *
 * The command takes arguments in the standard format x,y so the longitude must be specified before the latitude. There are limits to the coordinates that can be indexed: areas very near to the poles are not indexable.
 *
 * The exact limits, as specified by EPSG:900913 / EPSG:3785 / OSGEO:41001 are the following:
 *
 * Valid longitudes are from -180 to 180 degrees.
 * Valid latitudes are from -85.05112878 to 85.05112878 degrees.
 * The command will report an error when the user attempts to index coordinates outside the specified ranges.
 *
 * Note: there is no GEODEL command because you can use ZREM to remove elements. The Geo index structure is just a sorted set.
 *
 * GEOADD options
 * GEOADD also provides the following options:
 *
 * XX: Only update elements that already exist. Never add elements.
 * NX: Don't update already existing elements. Always add new elements.
 * CH: Modify the return value from the number of new elements added, to the total number of elements changed (CH is an abbreviation of changed). Changed elements are new elements added and elements already existing for which the coordinates was updated. So elements specified in the command line having the same score as they had in the past are not counted. Note: normally, the return value of GEOADD only counts the number of new elements added.
 * Note: The XX and NX options are mutually exclusive.
 *
 * How does it work?
 * The way the sorted set is populated is using a technique called Geohash. Latitude and Longitude bits are interleaved to form a unique 52-bit integer. We know that a sorted set double score can represent a 52-bit integer without losing precision.
 *
 * This format allows for bounding box and radius querying by checking the 1+8 areas needed to cover the whole shape and discarding elements outside it. The areas are checked by calculating the range of the box covered, removing enough bits from the less significant part of the sorted set score, and computing the score range to query in the sorted set for each area.
 *
 * What Earth model does it use?
 * The model assumes that the Earth is a sphere since it uses the Haversine formula to calculate distance. This formula is only an approximation when applied to the Earth, which is not a perfect sphere. The introduced errors are not an issue when used, for example, by social networks and similar applications requiring this type of querying. However, in the worst case, the error may be up to 0.5%, so you may want to consider other systems for error-critical applications.
 *
 * Return value
 * Integer reply, specifically:
 *
 * When used without optional arguments, the number of elements added to the sorted set (excluding score updates).
 * If the CH option is specified, the number of elements that were changed (added or updated).
 * History
 * >= 6.2: Added the CH, NX and XX options.
 * Examples
 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669 "Catania"
 * (integer) 2
 * redis> GEODIST Sicily Palermo Catania
 * "166274.1516"
 * redis> GEORADIUS Sicily 15 37 100 km
 * 1) "Catania"
 * redis> GEORADIUS Sicily 15 37 200 km
 * 1) "Palermo"
 * 2) "Catania"
 * redis>
 * Related commands
 * GEOADD
 * GEODIST
 * GEOHASH
 * GEOPOS
 * GEORADIUS
 * GEORADIUSBYMEMBER
 * GEOSEARCH
 * GEOSEARCHSTORE
 */
public class GeoAddCommandParser implements CommandParser<GeoAddCommand> {
    @Override
    public GeoAddCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        List<Geo> list = new ArrayList<>();
        ExistType existType = ExistType.NONE;
        boolean ch = false;
        for (; idx < command.length; idx++) {
            String token = CommandParsers.toRune(command[idx]);
            if (Strings.isEquals(token, "NX")) {
                existType = ExistType.NX;
            } else if (Strings.isEquals(token, "XX")) {
                existType = ExistType.XX;
            } else if (Strings.isEquals(token, "CH")) {
                ch = true;
            } else {
                double longitude = CommandParsers.toDouble(command[idx++]);
                double latitude = CommandParsers.toDouble(command[idx++]);
                byte[] member = CommandParsers.toBytes(command[idx]);
                list.add(new Geo(member, longitude, latitude));
            }
        }
        Geo[] geos = new Geo[list.size()];
        list.toArray(geos);
        return new GeoAddCommand(key, geos, existType, ch);
    }

}
