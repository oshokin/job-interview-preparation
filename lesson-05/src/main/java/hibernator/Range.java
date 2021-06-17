package hibernator;

import lombok.*;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Range {

    @Getter
    @Setter
    private int low;

    @Getter
    @Setter
    private int high;

    public boolean contains(int number) {
        return (number >= low && number <= high);
    }

}