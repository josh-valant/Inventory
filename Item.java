import java.time.LocalDateTime;

public class Item
{
    final private String label;
    final private String type;
    final private LocalDateTime expiration;

    public Item(final String label, final String type, final LocalDateTime expiration) {
        this.label = label;
        this.type = type;
        this.expiration = expiration;
    }

    public String getLabel() { return label; }
    public String getType() { return type; }
    public LocalDateTime getExpiration() { return expiration; }

    @Override
    public String toString() {
        return label + ", " + type + ", " + expiration;
    }
}
