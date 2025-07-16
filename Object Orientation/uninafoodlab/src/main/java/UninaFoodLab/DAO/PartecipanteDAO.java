package UninaFoodLab.DAO;

import java.util.List;

import UninaFoodLab.DTO.Partecipante;

public interface PartecipanteDAO
{
    void save(Partecipante toSavePartecipante);
    Partecipante getPartecipanteById(int idPartecipante);
    Partecipante getPartecipanteByUsername(String username);
    List<Partecipante> getPartecipantiIscrittiByIdCorso(int idCorso);
    boolean existsPartecipanteByCodiceFiscale(String codFisc);
    boolean existsPartecipanteByEmail(String email);
    void update(Partecipante previousPartecipante, Partecipante updatedPartecipante);
    void delete(int idPartecipante);
}