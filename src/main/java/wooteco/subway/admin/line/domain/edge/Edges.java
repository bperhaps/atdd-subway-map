package wooteco.subway.admin.line.domain.edge;

import org.springframework.data.relational.core.mapping.MappedCollection;
import wooteco.subway.admin.common.exception.SubwayException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Edges {
    @MappedCollection(idColumn = "line_id", keyColumn = "line_key")
    private List<Edge> edges;

    public Edges(final List<Edge> edges) {
        this.edges = edges;
    }

    public static Edges empty() {
        return new Edges(new ArrayList<>());
    }

    public List<Long> getStationsId() {
        return edges.stream()
                .map(Edge::getStationId)
                .collect(Collectors.toList());
    }

    public void add(final Edge edge) {
        if (this.edges.isEmpty() && edge.isNotStartStation()) {
            this.edges.add(Edge.startEdge(edge));
        }

        List<Edge> updateEdges = new ArrayList<>();

        for (Edge savedEdge : this.edges) {
            insertEdge(updateEdges, savedEdge, edge);
        }

        if (!updateEdges.contains(edge)) {
            updateEdges.add(edge);
        }

        this.edges = updateEdges;
    }

    private void insertEdge(final List<Edge> updateEdges, final Edge savedEdge, final Edge edge) {
        if (savedEdge.hasSamePreStation(edge)) {
            savedEdge.changePreStationToStationOf(edge);
            updateEdges.add(edge);
        }
        updateEdges.add(savedEdge);
    }

    public void removeByStationId(final Long stationId) {
        Edge beforeEdge = findByStationId(edge -> edge.isSameStationId(stationId))
                .orElseThrow(() -> new SubwayException(stationId + " : 지우려는 역이 존재하지 않습니다."));
        findByStationId(edge -> edge.isSamePreStationId(stationId))
                .ifPresent(edge -> edge.replacePreStation(beforeEdge));
        this.edges.remove(beforeEdge);
    }

    private Optional<Edge> findByStationId(Predicate<Edge> edgePredicate) {
        return this.edges.stream()
                .filter(edgePredicate)
                .findFirst();
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public boolean contain(final Edge edge) {
        return edges.stream()
                .anyMatch(aEdge -> aEdge.isSame(edge));
    }
}