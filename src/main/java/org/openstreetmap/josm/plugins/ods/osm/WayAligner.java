package org.openstreetmap.josm.plugins.ods.osm;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.ods.osm.SegmentMatcher.MatchType;

/**
 * The WayAligner aligns two ways according to a given tolerance.
 * A point in the second ring that is within 'tolerance' distance from a point
 * from the first ring, will get the coordinates of that point.
 * A lineSegment from either of the rings, that is within 'tolerance'
 * distance from a point on the other ring, will be split by adding that
 * point in between the start and end point of that segment.
 *
 * @author gertjan
 *
 */
public class WayAligner {
    private final Way way1;
    private final Way way2;
    private final NodeDWithin dWithin;
    private final boolean undoable;
    private NodeIterator it1;
    private NodeIterator it2;
    private final SegmentMatcher matcher;

    public WayAligner(Way way1, Way way2,
            NodeDWithin dWithin, boolean undoable) {
        this.way1 = way1;
        this.way2 = way2;
        this.dWithin = dWithin;
        this.undoable = undoable;
        this.matcher = new SegmentMatcher(dWithin);
    }

    public void run() {
        it1 = new NodeIterator(way1, 0, false);
        it2 = new NodeIterator(way2, 0, false);
        while (it1.hasNextNode()) {
            it2.reset();
            boolean match = false;
            while (! match && it2.hasNextNode()) {
                match = matcher.match(it1, it2);
                if (!match) it2.next();
            }
            if (match) {
                if (matcher.isreversed()) {
                    it2.next();
                    it2.setReversed(true);
                }
                alignEdge(matcher.getStartMatch(), matcher.getEndMatch());
            }
            it1.next();
        }
        it1.close(undoable);
        it2.close(undoable);
        if (MainApplication.getMap() != null) {
            MainApplication.getMap().mapView.repaint();
        }
    }

    private void alignEdge(MatchType matchStart, MatchType matchEnd) {
        alignSegmentStart(matchStart);
        MatchType end = matchEnd;
        while(end != MatchType.NoMatch) {
            alignSegmentEnd(matchEnd);
            if (it1.hasNextNodes(2) && it2.hasNextNodes(2)) {
                it1.next();
                it2.next();
                end = matchEnd();
            }
            else {
                end = MatchType.NoMatch;
            }
        }
    }

    /**
     * For the first pair of matching segments, we will have to align the starting point.
     * There are 3 possible cases:
     * 1. the starting point of the segments are within tolerance distance of each other
     * 2. the starting point of the first segment is within tolerance distance of the second segment
     * 3. the starting point of the second segment is within tolerance distance of the first segment
     */
    private void alignSegmentStart(MatchType matchType) {
        Node node;
        // Align start
        switch (matchType) {
        case NodeToNode:
            alignStartNodes(it1, it2);
            break;
        case NodeToSegment:
            node = it1.peek();
            it2.insertNodeAfter(node);
            break;
        case SegmentToNode:
            node = it2.peek();
            it1.insertNodeAfter(node);
            break;
        case NoMatch:
            break;
        }
    }

    /**
     * For every pair of matching segments, we will have to align the end point.
     * There are 3 possible cases:
     * 1. the end points of the segments are within tolerance distance of each other
     * 2. the end points of the first segment is within tolerance distance of the second segment
     * 3. the end points of the second segment is within tolerance distance of the first segment
     */
    private void alignSegmentEnd(MatchType matchType) {
        Node node;
        switch (matchType) {
        case NodeToNode:
            alignEndNodes(it1, it2);
            break;
        case NodeToSegment:
            node = it1.peekNext();
            it2.insertNodeAfter(node);
            it2.previous();
            break;
        case SegmentToNode:
            node = it2.peekNext();
            it1.insertNodeAfter(node);
            it1.previous();
            break;
        case NoMatch:
            break;
        }
    }

    private static void alignStartNodes(NodeIterator it11, NodeIterator it21) {
        alignNodes(it11, it11.getIndex(), it21, it21.getIndex());
    }

    private static void alignEndNodes(NodeIterator it11, NodeIterator it21) {
        alignNodes(it11, it11.nextIndex(), it21, it21.nextIndex());
    }

    private static void alignNodes(NodeIterator it1, int index1, NodeIterator it2, int index2) {
        Node n1 = it1.getNode(index1);
        Node n2 = it2.getNode(index2);
        if (n1 == n2) return;
        /*
         * n1 is a new node. Merge the nodes, keeping the location of the new node and the id and tags of the old one.
         */
        if (n1.getUniqueId() < 0) {
            it2.moveNode(n2, n1.getCoor());
            it1.updateNode(index1, n2);
            return;
        }
        if (n2.getUniqueId() < 0) {
            it1.moveNode(n1, n2.getCoor());
            it2.updateNode(index2, n1);
            return;
        }
        if (!n1.hasKeys()) {
            it2.updateNode(index2, n1);
            return;
        }
        if (!n2.hasKeys()) {
            it1.updateNode(index1, n2);
            return;
        }
        throw new UnsupportedOperationException("Don't know how to merge 2 nodes that both have tags");
    }

    //    private void _alignSegments(NodeIterator it1, NodeIterator it2) {
    //    	Integer index1 = it1.nextIndex();
    //    	Integer index2 = it2.nextIndex();
    //        Node n1 = it1.peekNext();
    //        Node n2 = it2.peekNext();
    //        if (n1 == n2) return;
    //        // Check for coordinate equality
    //        // Determine geometry source
    //        // Determine preferred node
    //        if (n1.getUniqueId() < 0) {
    //            if (!n1.getCoor().equals(n2.getCoor())) {
    //                it2.moveNode(index2, n1.getCoor());
    //            }
    //            it1.updateNode(index1, n2);
    //            return;
    //        }
    //        if (n2.getUniqueId() < 0) {
    //            if (!n1.getCoor().equals(n2.getCoor())) {
    //                it1.moveNode(index1, n2.getCoor());
    //            }
    //            it2.updateNode(index2, n1);
    //            return;
    //        }
    //        if (!n1.hasKeys()) {
    //            it2.updateNode(index2, n1);
    //            nodesToDelete.add(n1);
    //            return;
    //        }
    //        if (!n2.hasKeys()) {
    //            it1.updateNode(index1, n2);
    //            nodesToDelete.add(n2);
    //            return;
    //        }
    //        throw new UnsupportedOperationException("Don't know how to merge 2 nodes that both have tags");
    //    }

    /**
     * Once we found a matching start point, let's check if we can find a matching end point as well.
     *
     * @return
     */
    private MatchType matchEnd() {
        Node end1 = it1.peekNext();
        Node end2 = it2.peekNext();
        if (match(end1, end2)) {
            return MatchType.NodeToNode;
        }
        if (matchToSegment(it2, end1)) {
            return MatchType.NodeToSegment;
        }
        if (matchToSegment(it1, end2)) {
            return MatchType.SegmentToNode;
        }
        return MatchType.NoMatch;
    }

    private boolean match(Node n1, Node n2) {
        return n1.equals(n2) || dWithin.check(n1, n2);
    }

    private boolean matchToSegment(NodeIterator it, Node n) {
        return (it.dSegmentWithin(dWithin, n)) && !match(it.peek(), n) &&
                !match(it.peekNext(), n);
    }

    enum MatchTypeV2 {
        NoMatch,
        NodeToNode,
        NodeToSegment,
        SegmentToNode
    }
}
