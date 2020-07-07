/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;

/**
 * Geospatial Utilities.
 *
 * @author cygnus_x-1
 */
public class Distance {

    /**
     * Convert degrees to radians.
     *
     * @param degrees an angle value in degrees as a double
     * @return the radian equivalent to the given degree value as a double
     */
    public static double degreesToRadians(final double degrees) {
        return (degrees * Math.PI) / 180;
    }

    /**
     * Convert radians to degrees.
     *
     * @param radians an angle value in radians as a double
     * @return the degree equivalent to the given radian value as a double
     */
    public static double radiansToDegrees(final double radians) {
        return (radians * 180) / Math.PI;
    }

    /**
     * Convert a degrees-minute-seconds to decimal degrees.
     *
     * @param dms A degrees-minute-seconds formatted coordinate as a
     * {@link String} in the format d:m:s
     * @return The coordinate in decimal degrees as a double
     */
    public static double dmsToDd(final String dms) {
        final String[] dmsComponents = dms.split(SeparatorConstants.COLON);

        assert dmsComponents.length == 3 : "DMS should always have 3 components";

        return Double.valueOf(!dmsComponents[0].isEmpty() ? dmsComponents[0] : "0")
                + (Double.valueOf(!dmsComponents[1].isEmpty() ? dmsComponents[1] : "0") / 60)
                + (Double.valueOf(!dmsComponents[2].isEmpty() ? dmsComponents[2] : "0") / 3600);
    }

    /**
     * Convert a decimal degrees to degrees-minute-seconds.
     *
     * @param dd A decimal degree formatted coordinate as a double
     * @return The coordinate in degrees-minute-seconds as a {@link String} in
     * the format d:m:s
     */
    public static String ddToDms(final double dd) {
        final int degrees = (int) Math.floor(dd);
        final int minutes = (int) Math.floor((dd - degrees) * 60);
        final int seconds = (int) Math.floor((dd - degrees - (minutes / 60.0)) * 3600);

        return String.format("%d:%d:%d", degrees, minutes, seconds);
    }

    public static class Haversine {

        private static final double EARTH_RADIUS_DD = 180 / Math.PI;
        private static final double EARTH_RADIUS_KM = 6371.0;
        private static final double EARTH_RADIUS_MI = 3959.0;
        private static final double EARTH_RADIUS_NMI = 3440.0;

        /**
         * Use the Haversine formula to calculate the angle between two
         * locations.
         *
         * @param latitudeA the latitude of location A in decimal degrees.
         * @param longitudeA the longitude of location A in decimal degrees.
         * @param latitudeB the latitude of location B in decimal degrees.
         * @param longitudeB the longitude of location B in decimal degrees.
         * @return the angle between location A and location B.
         */
        private static double calulateAngle(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
            double distance;

            if (latitudeA == latitudeB && longitudeA == longitudeB) {
                distance = 0.0;
            } else {
                double latitudeARadians = degreesToRadians(latitudeA);
                double longitudeARadians = degreesToRadians(longitudeA);
                double latitudeBRadians = degreesToRadians(latitudeB);
                double longitudeBRadians = degreesToRadians(longitudeB);

                double coordinateVectorDotProduct = (Math.sin(latitudeARadians) * Math.sin(latitudeBRadians))
                        + (Math.cos(latitudeARadians) * Math.cos(latitudeBRadians) * Math.cos(longitudeARadians - longitudeBRadians));

                if (coordinateVectorDotProduct > 1) {
                    distance = Math.acos(1);
                } else {
                    distance = Math.acos(coordinateVectorDotProduct);
                }
            }
            return distance;
        }

        /**
         * Calculate the haversine distance estimate between two geospatial
         * coordinates in decimal degrees.
         *
         * @param latitudeA the latitude of location A in decimal degrees.
         * @param longitudeA the longitude of location A in decimal degrees.
         * @param latitudeB the latitude of location B in decimal degrees.
         * @param longitudeB the longitude of location B in decimal degrees.
         * @return the distance between location A and location B.
         */
        public static double estimateDistanceInDecimalDegrees(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
            return EARTH_RADIUS_DD * calulateAngle(latitudeA, longitudeA, latitudeB, longitudeB);
        }

        public static double decimalDegreesToKilometers(double distanceInDecimalDegrees) {
            return EARTH_RADIUS_KM * (distanceInDecimalDegrees / EARTH_RADIUS_DD);
        }

        public static double decimalDegreesToMiles(double distanceInDecimalDegrees) {
            return EARTH_RADIUS_MI * (distanceInDecimalDegrees / EARTH_RADIUS_DD);
        }

        public static double decimalDegreesToNauticalMiles(double distanceInDecimalDegrees) {
            return EARTH_RADIUS_NMI * (distanceInDecimalDegrees / EARTH_RADIUS_DD);
        }

        /**
         * Calculate the haversine distance estimate between two geospatial
         * coordinates in kilometers.
         *
         * @param latitudeA the latitude of location A in decimal degrees.
         * @param longitudeA the longitude of location A in decimal degrees.
         * @param latitudeB the latitude of location B in decimal degrees.
         * @param longitudeB the longitude of location B in decimal degrees.
         * @return the distance between location A and location B.
         */
        public static double estimateDistanceInKilometers(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
            return EARTH_RADIUS_KM * calulateAngle(latitudeA, longitudeA, latitudeB, longitudeB);
        }

        public static double kilometersToDecimalDegrees(double distanceInKilometers) {
            return EARTH_RADIUS_DD * (distanceInKilometers / EARTH_RADIUS_KM);
        }

        public static double kilometersToMiles(double distanceInKilometers) {
            return EARTH_RADIUS_MI * (distanceInKilometers / EARTH_RADIUS_KM);
        }

        public static double kilometersToNauticalMiles(double distanceInKilometers) {
            return EARTH_RADIUS_NMI * (distanceInKilometers / EARTH_RADIUS_KM);
        }

        /**
         * Calculate the haversine distance estimate between two geospatial
         * coordinates in miles.
         *
         * @param latitudeA the latitude of location A in decimal degrees.
         * @param longitudeA the longitude of location A in decimal degrees.
         * @param latitudeB the latitude of location B in decimal degrees.
         * @param longitudeB the longitude of location B in decimal degrees.
         * @return the distance between location A and location B.
         */
        public static double estimateDistanceInMiles(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
            return EARTH_RADIUS_MI * calulateAngle(latitudeA, longitudeA, latitudeB, longitudeB);
        }

        public static double milesToDecimalDegrees(double distanceInMiles) {
            return EARTH_RADIUS_DD * (distanceInMiles / EARTH_RADIUS_MI);
        }

        public static double milesToKilometers(double distanceInMiles) {
            return EARTH_RADIUS_KM * (distanceInMiles / EARTH_RADIUS_MI);
        }

        public static double milesToNauticalMiles(double distanceInMiles) {
            return EARTH_RADIUS_NMI * (distanceInMiles / EARTH_RADIUS_MI);
        }

        /**
         * Calculate the haversine distance estimate between two geospatial
         * coordinates in nautical miles.
         *
         * @param latitudeA the latitude of location A in decimal degrees.
         * @param longitudeA the longitude of location A in decimal degrees.
         * @param latitudeB the latitude of location B in decimal degrees.
         * @param longitudeB the longitude of location B in decimal degrees.
         * @return the distance between location A and location B.
         */
        public static double estimateDistanceInNauticalMiles(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
            return EARTH_RADIUS_NMI * calulateAngle(latitudeA, longitudeA, latitudeB, longitudeB);
        }

        public static double nauticalMilesToDecimalDegrees(double distanceInNauticalMiles) {
            return EARTH_RADIUS_DD * (distanceInNauticalMiles / EARTH_RADIUS_NMI);
        }

        public static double nauticalMilesToKilometers(double distanceInNauticalMiles) {
            return EARTH_RADIUS_KM * (distanceInNauticalMiles / EARTH_RADIUS_NMI);
        }

        public static double nauticalMilesToMiles(double distanceInNauticalMiles) {
            return EARTH_RADIUS_MI * (distanceInNauticalMiles / EARTH_RADIUS_NMI);
        }
    }
}
