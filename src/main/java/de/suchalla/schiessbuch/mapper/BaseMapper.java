package de.suchalla.schiessbuch.mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Basis-Interface für alle Mapper mit gemeinsamen Mapping-Methoden.
 * Reduziert Code-Duplikation durch generische Default-Implementierungen.
 *
 * @param <E> Entity-Typ
 * @param <D> DTO-Typ
 * @author Markus Suchalla
 * @version 1.0.0
 */
public interface BaseMapper<E, D> {

    /**
     * Konvertiert eine Entity zu einem DTO.
     *
     * @param entity Die zu konvertierende Entity
     * @return Das DTO oder null
     */
    D toDTO(E entity);

    /**
     * Konvertiert eine Liste von Entities zu einer Liste von DTOs.
     * Standard-Implementierung für alle Mapper.
     *
     * @param entities Die Liste der Entities
     * @return Liste der DTOs (leere Liste bei null-Input)
     */
    default List<D> toDTOList(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
