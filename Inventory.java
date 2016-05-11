/*
- Design and implement API layer to model and support the below requirements. 
Please write a very simple UI to demonstrate the features.

- In memory data structure is okay (no database code needed)
- Write tests
- Please write production-quality code and provide a runnable solution.

- Provide a README file with instructions on how to run your application, 
as well as brief documentation about your design, code structure, assumptions, 
reasoning, and any notes you want to add for extensibility, 
maintainability, security, performance, etc, that you don't have time to implement.

- Please post your solution on GitHub. We will may ask questions and/or comment in your code as part of code review.

- Feel free to email me questions or send me descriptions of your understand of the requirements 
for clarification/validation or if you want me to look over your initial design.

Requirements

1. Add an item to the inventory:

When I add a new item to the inventory
Then inventory contains information about the newly added item, such as Label, Expiration, Type.

2. Take an item from the inventory by Label:

When I took an item out from the inventory
Then the item is no longer in the inventory

3. Notification that an item has been taken out:

When I took an item out from the inventory
Then there is a notification that an item has been taken out.

4. Notification that an item has expired:

When an item expires
Then there is a notification about the expired item.
*/

import java.util.*;
import java.time.*;

public class Inventory
{
    private List<String> notifications = new ArrayList<String>();

    private Timer timer = new Timer(true); // is daemon
    private TimerTask task;

    // using list - labels might not be unique
    private List<Item> items = new ArrayList<Item>();
    private TreeSet<Item> itemsSorted = new TreeSet<Item>(new Comparator<Item>() {
        public int compare(Item i1, Item i2) {
            if (i1.getExpiration().isBefore(i2.getExpiration()))
                return -1;
            else if (i1.getExpiration().isAfter(i2.getExpiration()))
                return 1;
            else
                return 0;
        }
    });

    public void add(Item item) {
        if (item != null) {
            items.add(item);
            checkExpirationTimer(item);
        }
        // System.out.println("added to items: " + items);
    }

    public Item remove(String label) {
        // System.out.println("removing label: " + label);
        for (Item i : items) {
            // System.out.println("label: " + i.getLabel());
            if (i.getLabel().equals(label)) {
                items.remove(i);
                checkExpirationTimer(i);
                notifications.add("Item removed: " + i);
                return i;
            }
        }
        return null;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    private void checkExpirationTimer(Item item) {

        if (!items.contains(item)) {

            // check if item begin remove is next to expire
            if (item.equals(itemsSorted.first())) {
                // remove it after checking but before rescheduling
                itemsSorted.remove(item);

                // reschedule new next expiring item
                rescheduleExpirationTimer();

            } else {
                // remove it after checking
                itemsSorted.remove(item);
            }

        } else {
            itemsSorted.add(item);

            if (item.equals(itemsSorted.first())) {
                // reschedule new next expiring item
                rescheduleExpirationTimer();
            }
        }
    }

    private void rescheduleExpirationTimer() {
        // adjust timer
        if (task != null) {
            task.cancel();
            timer.purge();
        }
        task = new TimerTask() {
            @Override
            public void run() {
                // remove and report expired items
                while (!itemsSorted.isEmpty() && itemsSorted.first().getExpiration().isBefore(LocalDateTime.now())) {
                    notifications.add("Item expired: " + itemsSorted.pollFirst());
                }
                // reschedule next one
                rescheduleExpirationTimer();
            }
        };
        if (!itemsSorted.isEmpty()) {
            Instant instant = itemsSorted.first().getExpiration().atZone(ZoneId.systemDefault()).toInstant();
            timer.schedule(task, Date.from(instant));
        }
    }

    public static void main(String[] args) throws Exception {
        runAsserts();
    }

    public static void runAsserts() throws Exception {
        // Check for notification of removal and empty notifications before removal.
        {
            Inventory inv = new Inventory();

            inv.add(new Item("first item", "first type", LocalDateTime.now().plusDays(1)));
            assert inv.getNotifications().isEmpty() : "notifications should be empty";

            assert inv.remove("second item") == null : "removed type should yield nothing";
            assert inv.getNotifications().isEmpty() : "notifications should be empty";

            assert inv.remove("first item") != null : "removed item shouldn't be null";
            List<String> notes = inv.getNotifications();
            assert notes.size() == 1 && notes.iterator().next().contains("Item removed") : "Should have one notification for item removal";
        }

        // Check for notification of removal when 2 items added.
        {
            Inventory inv = new Inventory();

            inv.add(new Item("first item", "first type", LocalDateTime.now().plusDays(1)));
            inv.add(new Item("first item", "first type", LocalDateTime.now().plusDays(1)));
            assert inv.getNotifications().isEmpty() : "notifications should be empty";

            assert inv.remove("first item") != null : "removed item shouldn't be null";
            List<String> notes = inv.getNotifications();
            assert notes.size() == 1 && notes.iterator().next().contains("Item removed") : "Should have one notification for item removal";
        }

        // Check for notification of expiration when 2 items added.
        {
            Inventory inv = new Inventory();

            inv.add(new Item("first item", "first type", LocalDateTime.now().plusSeconds(1)));
            inv.add(new Item("second item", "first type", LocalDateTime.now().plusSeconds(4)));
            assert inv.getNotifications().isEmpty() : "notifications should be empty";

            Thread.sleep(1100);
            {
                List<String> notes = inv.getNotifications();
                // System.out.println(notes);
                assert notes.size() == 1 && notes.iterator().next().contains("Item expired") : "Should have one notification for item expiration";
            }

            Thread.sleep(3300);
            List<String> notes = inv.getNotifications();
            Iterator<String> it = notes.iterator();
            String n1 = it.next();
            String n2 = it.next();
            // System.out.println(notes);
            assert notes.size() == 2 && 
                    n1.contains("Item expired") && n1.contains("first item") && 
                    n2.contains("Item expired") && n2.contains("second item") : "Should have one notification for item expiration";
        }
    }

}

