package UninaFoodLab.Boundary;

import java.util.List;

/**
 * Supporto  al filtraggio per ID di argomenti tematici.
 *
 * Implementando questa interfaccia, un componente (per es. MyCoursesFrame)
 * può ricevere sia un filtro testuale search che un filtro
 * basato sulla selezione di argomenti, identificati dai loro ID.
 */
public interface ArgumentFilterable extends SearchFilterable
{
    /**
     * Applica un filtro basato sugli argomenti selezionati,
     * forniti come lista di ID.
     */
    void filterByArgumentsIds(List<Integer> idsArguments);
}