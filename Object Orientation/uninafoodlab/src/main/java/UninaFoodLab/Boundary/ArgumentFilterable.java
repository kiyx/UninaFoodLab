package UninaFoodLab.Boundary;

import java.util.List;

/**
 * Estende {@link SearchFilterable} aggiungendo il supporto
 * al filtraggio per ID di argomenti tematici.
 *
 * <p>Implementando questa interfaccia, un componente (per es. MyCoursesFrame)
 * può ricevere sia un filtro testuale <em>search</em> che un filtro
 * basato sulla selezione di argomenti, identificati dai loro ID.</p>
 */
public interface ArgumentFilterable extends SearchFilterable
{
    /**
     * Applica un filtro basato sugli argomenti selezionati,
     * forniti come lista di ID.
     *
     * @param topicIds lista di ID degli argomenti selezionati (può essere vuota per nessun filtro)
     */
    void filterByArgumentsIds(List<Integer> idsArguments);
}