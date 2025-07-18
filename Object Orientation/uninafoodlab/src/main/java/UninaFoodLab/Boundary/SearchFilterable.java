package UninaFoodLab.Boundary;

/**
 * Interfaccia per componenti che supportano il filtraggio di una lista di corsi o ricette
 * in base a un criterio testuale.
 *
 * Implementare questa interfaccia consente all'HeaderPanel di notificare
 * le variazioni nel campo di ricerca, tramite il metodo {@link #filterCorsi(String)}.
 */
public interface SearchFilterable
{
	/**
     * Metodo invocato dall'HeaderPanel quando il testo di ricerca cambia
     * o viene premuto il pulsante Cerca.
     *
     * @param filter il testo corrente da usare per filtrare i corsi e le ricette
     */
	void filter(String filter);
}