package io.github.espresso4j.latte.internal;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class Path {

    private List<Segment> segments;

    private boolean tailingSlash;

    private Path(List<Segment> segments, boolean tailingSlash) {
        this.segments = segments;
        this.tailingSlash = tailingSlash;
    }

    static class Segment {
        String name;

        boolean catchAll;

        boolean wildCard;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public static Path from(@Nonnull String pathString) {
        String[] segs = pathString.split("/");

        List<Segment> segList = Arrays.stream(segs)
                .filter((seg) -> seg.length() > 0)
                .map((seg) -> {
                    Segment segment = new Segment();
                    if (seg.startsWith(":")) {
                        segment.wildCard = true;
                    } else if (seg.startsWith("*")) {
                        segment.catchAll = true;
                    }
                    segment.name = seg;

                    return segment;
        }).collect(Collectors.toList());

        return new Path(segList, pathString.endsWith("/"));
    }

    public Map<String, String> matchSpec(String path) {
        Iterator<Segment> pathSpecSegIter = this.getSegments().iterator();

        Path testingPath = Path.from(path);
        Iterator<Segment> pathSegIter = testingPath.getSegments().iterator();

        Map<String, String> catches = new HashMap<>();

        while (pathSpecSegIter.hasNext()) {
            Segment pathSpecSeg = pathSpecSegIter.next();

            if (pathSpecSeg.catchAll) {
                StringJoiner stringJoiner = new StringJoiner("/");
                while (pathSegIter.hasNext()) {
                    stringJoiner.add(pathSegIter.next().name);
                }
                if (testingPath.tailingSlash) {
                    stringJoiner.add("");
                }
                catches.put(pathSpecSeg.name.substring(1), stringJoiner.toString());
                return catches;
            } else if (pathSpecSeg.wildCard) {
                catches.put(pathSpecSeg.name.substring(1), pathSegIter.next().name);
            } else {
                pathSegIter.next();
            }
        }

        return catches;
    }
}
