package me.luizotavio.minecraft.comparator;

import com.mattmalec.pterodactyl4j.application.entities.Node;
import com.mattmalec.pterodactyl4j.entities.Limit;

import java.util.Comparator;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class NodeComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        int o1Memory, o2Memory;

        try {
            o2Memory = Integer.parseInt(o2.getMemory());
        } catch (NumberFormatException e) {
            o2Memory = 0;
        }

        try {
            o1Memory = Integer.parseInt(o1.getMemory());
        } catch (NumberFormatException e) {
            o1Memory = 0;
        }

        Node targetNode;

        // Let's sum the memory of all o1's servers
        int o1Sum = o1.retrieveServers()
            .map(collection -> {
                int memory = 0;

                for (var server : collection) {
                    Limit limit = server.getLimits();

                    int serverMemory;
                    try {
                        serverMemory = Integer.parseInt(limit.getMemory());
                    } catch (NumberFormatException e) {
                        serverMemory = 0;
                    }

                    memory += serverMemory;
                }

                return memory;
            }).execute();

        // Let's sum the memory of all o2's servers
        int o2Sum = o2.retrieveServers()
            .map(collection -> {
                int memory = 0;

                for (var server : collection) {
                    Limit limit = server.getLimits();

                    int serverMemory;
                    try {
                        serverMemory = Integer.parseInt(limit.getMemory());
                    } catch (NumberFormatException e) {
                        serverMemory = 0;
                    }

                    memory += serverMemory;
                }

                return memory;
            }).execute();

        // Let's compare the memory of all o1's servers with the memory of all o2's servers
        return Integer.compare(o1Sum, o2Sum);
    }
}
