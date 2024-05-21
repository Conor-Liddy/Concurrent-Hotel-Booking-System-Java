public class Main {
    public static void main(String[] args) {
        try {
            int[] rooms = {101, 102, 103, 104, 105};
            Hotel hotel = new Hotel(rooms);

            // Test booking multiple rooms
            int[] days1 = {1, 2, 3};
            int[] roomNums1 = {101, 102};
            System.out.println("Booking rooms 101 and 102 on days 1, 2, and 3: " + hotel.bookRooms("BR1", days1, roomNums1)); // true

            // Test checking if rooms are booked
            int[] checkDays = {2, 4};
            int[] checkRooms = {101, 103};
            System.out.println("Checking if rooms 101 and 103 are booked on days 2 and 4: " + hotel.roomsBooked(checkDays, checkRooms)); // true, because room 101 is booked on day 2

            // Test updating the booking
            int[] newDays = {4, 5, 6};
            int[] newRooms = {103, 104};
            System.out.println("Updating booking BR1 to rooms 103 and 104 on days 4, 5, and 6: " + hotel.updateBooking("BR1", newDays, newRooms)); // true

            // Check if old rooms are no longer booked
            System.out.println("Checking if rooms 101 and 102 are booked on days 1, 2, and 3: " + hotel.roomsBooked(days1, roomNums1)); // false, because booking was updated

            // Check if new rooms are booked
            System.out.println("Checking if rooms 103 and 104 are booked on days 4, 5, and 6: " + hotel.roomsBooked(newDays, newRooms)); // true, because booking was updated

            // Test canceling the booking
            hotel.cancelBooking("BR1");
            System.out.println("Checking if rooms 103 and 104 are booked on days 4, 5, and 6 after cancellation: " + hotel.roomsBooked(newDays, newRooms)); // false, because booking was canceled

            // Test exception for non-existent booking cancellation
            try {
                hotel.cancelBooking("BR2");
            } catch (NoSuchBookingException e) {
                System.out.println(e.getMessage()); // "No booking with reference: BR2"
            }

            // Test booking that overlaps with existing booking
            hotel.bookRooms("BR3", newDays, new int[]{101});
            System.out.println("Booking room 102 on days 4, 5, and 6 should fail: " + hotel.bookRooms("BR4", newDays, new int[]{102})); // false, because room 101 is already booked on those days

        } catch (NoSuchBookingException e) {
            e.printStackTrace();
        }
    }
}