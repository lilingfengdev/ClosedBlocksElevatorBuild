package ml.karmaconfigs.closedblockselevator.storage.elevator;

import java.util.*;

public final class ElevatorLine {

    private final List<Elevator> original;
    private final ListIterator<Elevator> elevators;

    public ElevatorLine(final Elevator... orderedElevators) {
        original = Arrays.asList(orderedElevators);
        elevators = original.listIterator();
    }

    public Elevator up() {
        try {
            return elevators.next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public boolean hasUp() {
        return elevators.hasNext();
    }

    public Elevator down() {
        try {
            return elevators.previous();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public boolean hasDown() {
        return elevators.hasPrevious();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ElevatorLine@" + hashCode() + "{");
        original.forEach((elevator) -> {
            builder.append(elevator).append(";");
        });

        return builder.substring(0, builder.length() - 1) + "}";
    }
}
