package net.luxcube.minecraft.comparator;

import com.mattmalec.pterodactyl4j.application.entities.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class NodeComparator implements Comparator<Node> {

    private static final int MINIMAL_MEMORY = 2048;

    @Override
    public int compare(Node o1, Node o2) {
        // Skip nodes with maintenance mode
        if (o1.hasMaintanceMode()) {
            return 1;
        } else if (o2.hasMaintanceMode()) {
            return -1;
        }

        long o1UsedMemory = getUnusedMemory(o1),
            o2UsedMemory = getUnusedMemory(o2);

        // Check if allocated memory isn't already full
        if (o1UsedMemory < MINIMAL_MEMORY) {
            if (o2UsedMemory < MINIMAL_MEMORY) {
                // Both nodes have enough memory
                return Long.compare(o1UsedMemory, o2UsedMemory);
            } else {
                // o1 has enough memory, but o2 doesn't
                return -1;
            }
        } else if (o2UsedMemory < MINIMAL_MEMORY) {
            // o2 has enough memory, but o1 doesn't
            return 1;
        }

        // Retrieve the less node usage
        return Long.compare(o1UsedMemory, o2UsedMemory);
    }

    private long getUnusedMemory(@NotNull Node node) {
        long maxMemory;
        try {
            maxMemory = Long.parseLong(node.getMemory());
        } catch (@NotNull Exception e) {
            maxMemory = Long.MAX_VALUE;
        }

        long usedMemory;
        try {
            usedMemory = Long.parseLong(node.getMemoryOverallocate());
        } catch (@NotNull Exception e) {
            usedMemory = Long.MAX_VALUE;
        }

        return maxMemory - usedMemory;
    }
}
