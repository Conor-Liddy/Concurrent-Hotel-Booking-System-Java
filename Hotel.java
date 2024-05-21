import java.util.*;
import java.util.concurrent.locks.*;

class NoSuchBookingException extends Exception {
    public NoSuchBookingException (String bookingRef) {
        super("There is no booking with reference " + bookingRef);
    }
}

public class Hotel {
    private final int[] roomNums;
    private final Map<String, List<Booking>> bookings;
    private final Map<Integer, Set<Integer>> roomBookings;

    private final ReadWriteLock lock;   //ReadWriteLock will be used to allow for concurrency

    public Hotel (int[] roomNums){
        //  constructs a hotel with room numbers as specified in roomNums. The rooms are initially unbooked.

        this.roomNums = roomNums;
        this.bookings = new HashMap<>();
        this.roomBookings = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();   //initialize lock

        for (int roomNum : roomNums) {
            roomBookings.put(roomNum, new HashSet<>());
        }
    }

    public boolean roomBooked(int[] days, int roomNum) {
        //  returns true if the room "roomNum" is booked on any of the days specified in "days", otherwise returns false.

        lock.readLock().lock(); // check lock for critical section - enter and lock if available

        try {
            if (!roomBookings.containsKey(roomNum)) {
                return false;   //if the room does not exist then there can be no booking
            }

            Set<Integer> bookedDays = roomBookings.get(roomNum);    //get days that the room is booked

            for (int day : days) {  //check each day and compare against the day looking to be booked
                if (bookedDays.contains(day)) {
                    return true;    //If any day in the days array is found in the bookedDays set, return true, the room is booked on at least one of the specified days.
                }
            }

            return false;   //If none of the specified days are found in the bookedDays set, return false, indicating that the room is not booked on any of the specified days.

        }
        finally {
            lock.readLock().unlock();   //release lock when finished.
        }
    }

    public boolean bookRooms(String bookingRef, int[] days, int[] roomNums) {
        lock.writeLock().lock();
        try {
            // Check if all rooms can be booked for the specified days
            for (int roomNum : roomNums) {
                if (!roomBookings.containsKey(roomNum) || roomBooked(days, roomNum)) {
                    return false;
                }
            }

            // Create a new booking for each room
            List<Booking> bookingList = new ArrayList<>();
            for (int roomNum : roomNums) {
                Booking booking = new Booking(bookingRef, days, roomNum);
                bookingList.add(booking);
                Set<Integer> bookedDays = roomBookings.get(roomNum);
                for (int day : days) {
                    bookedDays.add(day);
                }
            }
            bookings.put(bookingRef, bookingList); // Correctly store the booking list under the bookingRef
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean updateBooking(String bookingRef, int[] days, int[] roomNums) throws NoSuchBookingException {
        //      updates the booking with reference "bookingRef" so that it now refers to the specified "roomNum" for
        //  each of the days specified in "days". Returns true if it is possible to update the booking (i.e., the new booking
        //  does not clash with an existing booking), otherwise returns false and leaves the original
        //  booking unchanged. If there is no booking with the specified reference throw "NoSuchBookingException".

        lock.writeLock().lock();

        try {
            // Check if the booking reference exists
            if (!bookings.containsKey(bookingRef)) {
                throw new NoSuchBookingException(bookingRef);
            }

            List<Booking> currentBookings = bookings.get(bookingRef);

            // Temporarily free the current booking days
            for (Booking currentBooking : currentBookings) {
                for (int day : currentBooking.days) {
                    roomBookings.get(currentBooking.roomNum).remove(day);
                }
            }

            // Check if the new booking days are available for all rooms
            for (int roomNum : roomNums) {
                if (roomBooked(days, roomNum)) {
                    // Restore the original booking days if the update is not possible
                    for (Booking currentBooking : currentBookings) {
                        for (int day : currentBooking.days) {
                            roomBookings.get(currentBooking.roomNum).add(day);
                        }
                    }
                    return false;
                }
            }

            // Update the booking for each room
            for (Booking currentBooking : currentBookings) {

                currentBooking.days = days;

                for (int day : days) {
                    roomBookings.get(currentBooking.roomNum).add(day);
                }
            }
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void cancelBooking(String bookingRef) throws NoSuchBookingException {
        //      cancels the booking with reference bookingRef. The room booked under this booking
        //  reference becomes unbooked for the days of the booking. If there is no booking with the
        //  specified reference throws NoSuchBookingException.
        //  Note that the methods do not throw any checked exceptions other than those specified.

        lock.writeLock().lock();

        try {
            if (!bookings.containsKey(bookingRef)) {
                throw new NoSuchBookingException(bookingRef);
            }

            List<Booking> bookingList = bookings.remove(bookingRef);

            for (Booking booking : bookingList) {
                Set<Integer> bookedDays = roomBookings.get(booking.roomNum);
                for (int day : booking.days) {
                    bookedDays.remove(day);
                }
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    // rooms booked extra functionality
    public boolean roomsBooked(int[] days, int[] roomNums) {

        lock.readLock().lock();

        try {
            for (int roomNum : roomNums) {
                if (roomBooked(days, roomNum)) {    //check every room for a booking on every day
                    return true;                    //if any are booked return true
                }
            }
            return false;   //otherwise return false
        }
        finally {
        lock.readLock().unlock();
        }
    }

    private static class Booking {  //create a booking object
        String bookingRef;
        int[] days;
        int roomNum;

        Booking(String bookingRef, int[] days, int roomNum) {
            this.bookingRef = bookingRef;
            this.days = days;
            this.roomNum = roomNum;
        }
    }
}
 