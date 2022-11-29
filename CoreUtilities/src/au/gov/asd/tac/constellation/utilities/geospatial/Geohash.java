/*
 * Copyright 2010-2021 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.utilities.geospatial;

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility for encoding and decoding geohashes, as well as performing various
 * functions on geohashes.
 *
 * @author cygnus_x-1
 */
public class Geohash {

    private static final Logger LOGGER = Logger.getLogger(Geohash.class.getName());

    public static final double EARTH_RADIUS_KM = 6371.01;

    /**
     * The base used for encoding/decoding a geohash.
     */
    public enum Base {

        B64("0123456789=ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz", -180.0, 180.0, -180.0, 180.0, 64),
        B32("0123456789bcdefghjkmnpqrstuvwxyz", -90.0, 90.0, -180.0, 180.0, 32),
        B16("0123456789abcdef", -180.0, 180.0, -180.0, 180.0, 16),
        B8("01234567", -90.0, 90.0, -180.0, 180.0, 8),
        B4("0123", -180.0, 180.0, -180.0, 180.0, 4);

        private final char[] charset;
        private final int[] inverseCharset = new int[128];
        private final double minLatitude;
        private final double maxLatitude;
        private final double minLongitude;
        private final double maxLongitude;
        private final int base;
        private final int[] bits;

        private Base(final String charset, final double minLatitude, final double maxLatitude,
                final double minLongitude, final double maxLongitude, final int base) {
            if (charset.length() != base) {
                throw new IllegalArgumentException(String.format("Charset: '%s' is not of the correct length (%d/%d)", charset, charset.length(), base));
            }

            this.charset = charset.toCharArray();
            Arrays.fill(this.inverseCharset, -1);
            for (int c = 0; c < this.charset.length; c++) {
                if ((c > 0) && (this.charset[c] < this.charset[c - 1])) {
                    throw new IllegalArgumentException(String.format("Characters in charset: '%s' are not lexigraphical ordered at position (%d)", charset, c));
                }
                this.inverseCharset[this.charset[c]] = c;
            }

            this.minLatitude = minLatitude;
            this.maxLatitude = maxLatitude;
            this.minLongitude = minLongitude;
            this.maxLongitude = maxLongitude;
            this.base = base;

            this.bits = new int[(int) Math.round(Math.log10(base) / Math.log10(2.0))];
            int idx = 0;
            for (int x = base / 2; x >= 1; x /= 2) {
                this.bits[idx++] = x;
            }
        }

        public char[] getCharset() {
            return charset;
        }

        public int[] getInverseCharset() {
            return inverseCharset;
        }

        public double getMinX() {
            return minLatitude;
        }

        public double getMinY() {
            return minLongitude;
        }

        public double getMaxX() {
            return maxLatitude;
        }

        public double getMaxY() {
            return maxLongitude;
        }

        public int getBase() {
            return base;
        }

        public int[] getBits() {
            return bits;
        }
    }

    /**
     * Generates a geohash for a given latitude and longitude.
     *
     * @param latitude a double representation of the latitude
     * @param longitude a double representation of the longitude
     * @param length the required length of the geohash
     * @param base the base to be used with the geohash algorithm
     * @return a String representation of the geohash
     */
    public static String encode(final double latitude, final double longitude, final int length, final Base base) {
        final char[] geohash = new char[length];
        final double[] latitudeRange = new double[]{base.minLatitude, base.maxLatitude};
        final double[] longitudeRange = new double[]{base.minLongitude, base.maxLongitude};

        double mid;
        int c = 0;
        int bit = 0;
        int position = 0;
        boolean longComponent = true;
        while (position < length) {
            if (longComponent) {
                mid = (longitudeRange[0] + longitudeRange[1]) / 2.0;
                if (longitude > mid) {
                    c |= base.bits[bit];
                }
                longitudeRange[longitude > mid ? 0 : 1] = mid;
            } else {
                mid = (latitudeRange[0] + latitudeRange[1]) / 2.0;
                if (latitude > mid) {
                    c |= base.bits[bit];
                }
                latitudeRange[latitude > mid ? 0 : 1] = mid;
            }

            if (bit < base.bits.length - 1) {
                bit++;
            } else {
                geohash[position++] = base.charset[c];
                bit = 0;
                c = 0;
            }

            longComponent = !longComponent;
        }

        return new String(geohash);
    }

    /**
     * Converts a geohash into the latitude and longitude it represents, as well
     * as the error associated with those values.
     *
     * @param geohash a String representation of the geohash
     * @param base the base to be used with the geohash algorithm
     * @return a double array containing the latitude at index 0, the longitude
     * at index 1, the maximum error in latitude at index 2 and the maximum
     * error on longitude at index 3
     * @throws IllegalArgumentException
     */
    public static double[] decode(final String geohash, final Base base) {
        if (geohash == null) {
            throw new IllegalArgumentException("The provided geohash is null");
        }

        final double[] latitudeRange = new double[]{base.minLatitude, base.maxLatitude};
        final double[] longitudeRange = new double[]{base.minLongitude, base.maxLongitude};
        double latitudeError = base.maxLatitude;
        double longitudeError = base.maxLongitude;

        int cd;
        boolean longitudeComponent = true;
        for (char c : geohash.toCharArray()) {
            cd = base.inverseCharset[c];
            if (cd == -1) {
                throw new IllegalArgumentException("Illegal character: " + c);
            }
            for (int mask : base.bits) {
                if (longitudeComponent) {
                    longitudeError /= 2.0;
                    longitudeRange[(cd & mask) != 0 ? 0 : 1] = (longitudeRange[0] + longitudeRange[1]) / 2.0;
                } else {
                    latitudeError /= 2.0;
                    latitudeRange[(cd & mask) != 0 ? 0 : 1] = (latitudeRange[0] + latitudeRange[1]) / 2.0;
                }

                longitudeComponent = !longitudeComponent;
            }
        }

        final double latitude = (latitudeRange[0] + latitudeRange[1]) / 2.0;
        final double longitude = (longitudeRange[0] + longitudeRange[1]) / 2.0;

        return new double[]{latitude, longitude, latitudeError, longitudeError};
    }

    /**
     * Get error components for a geohash of given length.
     *
     * @param base the base of the geohash
     * @param length the length of the geohash
     * @return a double array containing the maximum error in latitude at index
     * 0 and the maximum error in longitude at index 1, in decimal degrees
     */
    public static double[] getErrorForLength(final Base base, final int length) {
        double latitudeError = base.maxLatitude;
        double longitudeError = base.maxLongitude;
        boolean longitudeComponent = true;
        for (int count = 0; count < length; count++) {
            for (int mask : base.bits) {
                if (longitudeComponent) {
                    longitudeError /= 2.0;
                } else {
                    latitudeError /= 2.0;
                }

                longitudeComponent = !longitudeComponent;
            }
        }

        return new double[]{latitudeError, longitudeError};
    }

    /**
     * Get the number of hashes needed to traverse to get from one hash to
     * another, where one hash represents a distance of 1. An assumption is made
     * that the earth is a rectangular plane, however the distance between
     * hashes on either side of the international date line (180.0E / -180.0W)
     * is measured using the shortest distance.
     *
     * @param sourceGeohash the source geohash
     * @param destinationgeoHash the desination geohash
     * @param base the base of the geohash
     * @return an integer representing the number of hashes between the source
     * and destination geohashes
     */
    public static final int getDistanceInGrids(final String sourceGeohash,
            final String destinationgeoHash, final Base base) {
        final double[] sourceLocation = decode(sourceGeohash, base);
        final double[] destinationLocation = decode(destinationgeoHash, base);

        return getDistanceInGrids(sourceLocation, destinationLocation);
    }

    /**
     * Calculate the number of geohashes between two geohash centroids.
     *
     * @param sourceCoordinates source geohash centroid
     * @param destinationCoordinates destination geohash centroid
     * @return an integer representing the number of hashes between the source
     * and destination geohash centroids
     */
    private static int getDistanceInGrids(final double[] sourceCoordinates, final double[] destinationCoordinates) {
        final double latitudeDiff = Math.abs(sourceCoordinates[0] - destinationCoordinates[0]);
        double longitudeDiff = Math.abs(sourceCoordinates[1] - destinationCoordinates[1]);
        if (longitudeDiff > 180.0) {
            longitudeDiff -= 180.0;
            longitudeDiff = 180.0 - longitudeDiff;
        }

        final double xGrids = (longitudeDiff / (sourceCoordinates[3] * 2));
        final double yGrids = (latitudeDiff / (sourceCoordinates[2] * 2));

        return (int) Math.sqrt((xGrids * xGrids) + (yGrids * yGrids));
    }

    /**
     * Get the approximate (Haversine) distance in kilometers between the
     * centroids of two geohashes.
     *
     * @param sourceGeohash the source geohash
     * @param destinationGeohash the destination geohash
     * @param base the base of the geohashes
     * @return the approximate distance, in km, between the centroids of the
     * source and destination geohashes
     */
    public static double getDistanceKm(final String sourceGeohash, final String destinationGeohash, final Base base) {
        final double[] sourceLocation = decode(sourceGeohash, base);
        final double[] destinationLocation = decode(destinationGeohash, base);

        return getDistanceKm(sourceLocation, destinationLocation);
    }

    /**
     * Get the approximate (Haversine) distance in kilometers between the
     * centroids of two geohashes.
     *
     * @param sourceLocation the source geohash centroid
     * @param destinationLocation the destination geohash centroid
     * @return the approximate distance, in km, between the source and
     * destination geohash centroids
     */
    private static double getDistanceKm(final double[] sourceLocation, final double[] destinationLocation) {
        return Distance.Haversine.estimateDistanceInKilometers(sourceLocation[0], sourceLocation[1], destinationLocation[0], destinationLocation[1]);
    }

    public static String getGeoJson(final String geohash) throws IOException {
        if (geohash == null) {
            return null;
        }
        final double[] geohashDecoded = Geohash.decode(geohash, Geohash.Base.B16);
        final double centroidLatitude = geohashDecoded[0];
        final double centroidLongitude = geohashDecoded[1];
        final double errorLatitude = geohashDecoded[2];
        final double errorLongitude = geohashDecoded[3];
        final double minLatitude = centroidLatitude - errorLatitude;
        final double maxLatitude = centroidLatitude + errorLatitude;
        final double minLongitude = centroidLongitude - errorLongitude;
        final double maxLongitude = centroidLongitude + errorLongitude;

        final List<Tuple<Double, Double>> coordinates = new ArrayList<>();
        coordinates.add(Tuple.create(minLongitude, minLatitude));
        coordinates.add(Tuple.create(maxLongitude, maxLatitude));
        return Shape.generateShape(geohash, GeometryType.BOX, coordinates);
    }
}
