package UninaFoodLab.Boundary;

/**
 * Interfaccia per componenti che supportano il filtraggio di una lista
 * in base a un criterio testuale.
 *
 * Implementare questa interfaccia consente all'HeaderPanel di notificare
 * le variazioni nel campo di ricerca, tramite il metodo filter.
 */
public interface SearchFilterable
{
	/**
     * Metodo invocato dall'HeaderPanel quando il testo di ricerca cambia
     * o viene premuto il pulsante Cerca.
     *
     */
	void filter(String filter);
}