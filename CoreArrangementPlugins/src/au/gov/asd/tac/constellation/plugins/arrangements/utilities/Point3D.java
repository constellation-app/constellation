/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import java.io.Serializable;

/**
 * The <code>Point3D</code> class defines a point representing a location in
 * {@code (x,y,z)} coordinate space.
 * <p>
 * This class is only the abstract superclass for all objects that store a 3D
 * coordinate. The actual storage representation of the coordinates is left to
 * the subclass.
 *
 * Based on the Java Point2D class.
 *
 * @author algol
 */
public abstract class Point3D implements Cloneable {

    /**
     * The <code>Float</code> class defines a point specified in float
     * precision.
     */
    public static class Float extends Point3D implements Serializable {

        /**
         * The X coordinate of this <code>Point3D</code>.
         *
         * @serial
         */
        private float x;
        /**
         * The Y coordinate of this <code>Point3D</code>.
         *
         * @serial
         */
        private float y;
        /**
         * The Z coordinate of this <code>Point3D</code>.
         *
         * @serial
         */
        private float z;

        /**
         * Constructs and initializes a <code>Point3D</code> with coordinates
         * (0,&nbsp;0,&nbsp;0).
         *
         * @since 1.2
         */
        public Float() {
        }

        /**
         * Constructs and initializes a <code>Point3D</code> with the specified
         * coordinates.
         *
         * @param x the X coordinate of the newly * constructed
         * <code>Point3D</code>
         * @param y the Y coordinate of the newly * constructed
         * <code>Point3D</code>
         * @param z the Z coordinate of the newly * constructed
         * <code>Point3D</code>
         */
        public Float(final float x, final float y, final float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX() {
            return getFloatX();
        }

        public float getFloatX() {
            return x;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getY() {
            return getFloatY();
        }

        public float getFloatY() {
            return y;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getZ() {
            return getFloatZ();
        }

        public float getFloatZ() {
            return z;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setLocation(final double x, final double y, final double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
        }

        /**
         * Sets the location of this <code>Point3D</code> to the specified
         * <code>float</code> coordinates.
         *
         * @param x the new X coordinate of this {@code Point3D}
         * @param y the new Y coordinate of this {@code Point3D}
         * @param z the new Z coordinate of this {@code Point3D}
         */
        public void setLocation(final float x, final float y, final float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Returns a <code>String</code> that represents the value of this
         * <code>Point3D</code>.
         *
         * @return a string representation of this <code>Point3D</code>.
         */
        @Override
        public String toString() {
            return "Point3D.Float[" + x + ", " + y + ", " + z + "]";
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = -2870572449815403713L;
    }

    /**
     * The <code>Double</code> class defines a point specified in
     * <code>double</code> precision.
     */
    public static class Double extends Point3D implements Serializable {

        /**
         * The X coordinate of this <code>Point3D</code>.
         *
         * @serial
         */
        private double x;
        /**
         * The Y coordinate of this <code>Point3D</code>.
         *
         * @serial
         */
        private double y;
        /**
         * The Z coordinate of this <code>Point3D</code>.
         *
         * @serial
         */
        private double z;

        /**
         * Constructs and initializes a <code>Point3D</code> with coordinates
         * (0,&nbsp;0,&nbsp;0).
         */
        public Double() {
        }

        /**
         * Constructs and initializes a <code>Point3D</code> with the specified
         * coordinates.
         *
         * @param x the X coordinate of the newly * constructed
         * <code>Point3D</code>
         * @param y the Y coordinate of the newly * constructed
         * <code>Point3D</code>
         * @param z the Z coordinate of the newly * constructed
         * <code>Point3D</code>
         */
        public Double(final double x, final double y, final double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX() {
            return x;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getY() {
            return y;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getZ() {
            return z;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setLocation(final double x, final double y, final double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Returns a <code>String</code> that represents the value of this
         * <code>Point3D</code>.
         *
         * @return a string representation of this <code>Point3D</code>.
         */
        @Override
        public String toString() {
            return "Point3D.Double[" + x + ", " + y + ", " + z + "]";
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 6150783262733311327L;
    }

    /**
     * This is an abstract class that cannot be instantiated directly.
     * Type-specific implementation subclasses are available for instantiation
     * and provide a number of formats for storing the information necessary to
     * satisfy the various accessor methods below.
     *
     * @see java.awt.geom.Point2D.Float
     * @see java.awt.geom.Point2D.Double
     * @see java.awt.Point
     */
    protected Point3D() {
    }

    /**
     * Returns the X coordinate of this <code>Point3D</code> in
     * <code>double</code> precision.
     *
     * @return the X coordinate of this <code>Point3D</code>.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of this <code>Point3D</code> in
     * <code>double</code> precision.
     *
     * @return the Y coordinate of this <code>Point3D</code>.
     */
    public abstract double getY();

    /**
     * Returns the Z coordinate of this <code>Point3D</code> in
     * <code>double</code> precision.
     *
     * @return the Z coordinate of this <code>Point3D</code>.
     */
    public abstract double getZ();

    /**
     * Sets the location of this <code>Point3D</code> to the specified
     * <code>double</code> coordinates.
     *
     * @param x the new X coordinate of this {@code Point3D}
     * @param y the new Y coordinate of this {@code Point3D}
     * @param z the new Z coordinate of this {@code Point3D}
     */
    public abstract void setLocation(final double x, final double y, final double z);

    /**
     * Sets the location of this <code>Point3D</code> to the same coordinates as
     * the specified <code>Point3D</code> object.
     *
     * @param p the specified <code>Point3D</code> to which to set * this
     * <code>Point3D</code>
     */
    public void setLocation(final Point3D p) {
        setLocation(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Returns the square of the distance between two points.
     *
     * @param x1 the X coordinate of the first specified point
     * @param y1 the Y coordinate of the first specified point
     * @param z1 the Z coordinate of the first specified point
     * @param x2 the X coordinate of the second specified point
     * @param y2 the Y coordinate of the second specified point
     * @param z2 the Z coordinate of the second specified point
     * @return the square of the distance between the two sets of specified
     * coordinates.
     */
    public static double distanceSq(double x1, double y1, double z1,
            final double x2, final double y2, final double z2) {
        x1 -= x2;
        y1 -= y2;
        z1 -= z2;
        return (x1 * x1 + y1 * y1 + z1 * z1);
    }

    /**
     * Returns the distance between two points.
     *
     * @param x1 the X coordinate of the first specified point
     * @param y1 the Y coordinate of the first specified point
     * @param z1 the Z coordinate of the first specified point
     * @param x2 the X coordinate of the second specified point
     * @param y2 the Y coordinate of the second specified point
     * @param z2 the Z coordinate of the second specified point
     * @return the distance between the two sets of specified coordinates.
     */
    public static double distance(double x1, double y1, double z1,
            final double x2, final double y2, final double z2) {
        x1 -= x2;
        y1 -= y2;
        z1 -= z2;
        return Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
    }

    /**
     * Returns the square of the distance from this <code>Point3D</code> to a
     * specified point.
     *
     * @param px the X coordinate of the specified point to be measured against
     * this <code>Point3D</code>
     * @param py the Y coordinate of the specified point to be measured against
     * this <code>Point3D</code>
     * @param pz the Z coordinate of the specified point to be measured against
     * this <code>Point3D</code>
     * @return the square of the distance between this <code>Point3D</code> and
     * the specified point.
     */
    public double distanceSq(double px, double py, double pz) {
        px -= getX();
        py -= getY();
        pz -= getZ();
        return (px * px + py * py + pz * pz);
    }

    /**
     * Returns the square of the distance from this <code>Point3D</code> to a
     * specified <code>Point3D</code>.
     *
     * @param pt the specified point to be measured against * this
     * <code>Point3D</code>
     * @return the square of the distance between this <code>Point3D</code> to a
     * specified <code>Point3D</code>.
     */
    public double distanceSq(final Point3D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        double pz = pt.getZ() - this.getZ();
        return (px * px + py * py + pz * pz);
    }

    /**
     * Returns the distance from this <code>Point3D</code> to a specified point.
     *
     * @param px the X coordinate of the specified point to be measured against
     * this <code>Point3D</code>
     * @param py the Y coordinate of the specified point to be measured against
     * this <code>Point3D</code>
     * @param pz the Z coordinate of the specified point to be measured against
     * this <code>Point3D</code>
     * @return the distance between this <code>Point3D</code> and a specified
     * point.
     */
    public double distance(double px, double py, double pz) {
        px -= getX();
        py -= getY();
        pz -= getZ();
        return Math.sqrt(px * px + py * py + pz * pz);
    }

    /**
     * Returns the distance from this <code>Point3D</code> to a specified
     * <code>Point3D</code>.
     *
     * @param pt the specified point to be measured against * this
     * <code>Point3D</code>
     * @return the distance between this <code>Point3D</code> and the *
     * specified <code>Point3D</code>.
     */
    public double distance(final Point3D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        double pz = pt.getZ() - this.getZ();
        return Math.sqrt(px * px + py * py + pz * pz);
    }

    /**
     * Creates a new object of the same class and with the same contents as this
     * object.
     *
     * @return a clone of this instance.
     * @exception OutOfMemoryError if there is not enough memory.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns the hashcode for this <code>Point3D</code>.
     *
     * @return a hash code for this <code>Point3D</code>.
     */
    @Override
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        bits ^= java.lang.Double.doubleToLongBits(getZ()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Determines whether or not two points are equal. Two instances of
     * <code>Point3D</code> are equal if the values of their <code>x</code>,
     * <code>y</code> and <code>z</code> member fields, representing their
     * position in the coordinate space, are the same.
     *
     * @param obj an object to be compared with this <code>Point3D</code>
     * @return <code>true</code> if the object to be compared is an instance *
     * of <code>Point3D</code> and has the same values; <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            final Point3D p3d = (Point3D) obj;
            return (getX() == p3d.getX()) && (getY() == p3d.getY() && (getZ() == p3d.getZ()));
        }
        return super.equals(obj);
    }
}
