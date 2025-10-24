package UninaFoodLab.Controller;

import UninaFoodLab.Boundary.*;
import UninaFoodLab.DTO.*;
import UninaFoodLab.DAO.Postgres.*;
import UninaFoodLab.Exceptions.*;

import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.stream.*;
import javax.swing.*;
import org.jdesktop.swingx.*;
import org.mindrot.jbcrypt.*;
import com.formdev.flatlaf.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.sql.*;
import java.sql.Date;
import java.time.*;

public class Controller
{
	/** Logger per tracciamento eventi e errori */
	private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());
	private static final String ERR_CF_EXISTING = "Esiste già un account associato a questo codice fiscale.";
	private static final String ERR_EMAIL_EXISTING = "Esiste già un account associato a questa email";
	private static final String ERR_RECIPE_EXISTING = "Esiste già una ricetta con questo nome per questo chef";

	/** Istanza Singleton */
	private static Controller instance = null;

	/** Utente attualmente autenticato */
	private Utente loggedUser;

	/** DAO per le varie entità */
	private AdesioneDAO_Postgres adesioneDAO;
	private ArgomentoDAO_Postgres argomentoDAO;
	private ChefDAO_Postgres chefDAO;
	private CorsoDAO_Postgres corsoDAO;
	private IngredienteDAO_Postgres ingredienteDAO;
	private PartecipanteDAO_Postgres partecipanteDAO;
	private ReportMensileDAO_Postgres reportDAO;
	private RicettaDAO_Postgres ricettaDAO;
	private SessioneOnlineDAO_Postgres sessioneOnlineDAO;
	private SessionePraticaDAO_Postgres sessionePraticaDAO;
	private UtilizzoDAO_Postgres utilizzoDAO;

	/**
	 * Cache per i load fatti da GUI (principalmente per la save successiva su DB,
	 * per non rifare query ai DAO)
	 */
	private List<Argomento> cacheArgomenti = new ArrayList<>();
	private List<Ricetta> cacheRicette = new ArrayList<>();
	private List<Corso> cacheCorsi = new ArrayList<>();
	
	/** Costruttore privato per pattern Singleton */
	private Controller() {}

	/**
	 * Restituisce l'unica istanza di Controller (Singleton).
	 * 
	 * @return istanza del Controller
	 */
	public static Controller getController()
	{
		if(instance == null)
			instance = new Controller();
		return instance;
	}

	/**
	 * Entry point dell'applicazione. Inizializza il Look&Feel e apre il LoginFrame.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(() -> {
			try
			{
				UIManager.setLookAndFeel(new FlatLightLaf());
			} 
			catch(UnsupportedLookAndFeelException e)
			{
				e.printStackTrace();
			}

			new LoginFrame().setVisible(true);
		});
	}

	/** @return Utente attualmente autenticato */
	public Utente getLoggedUser()
	{
		return loggedUser;
	}

	/** @return true se l'utente loggato è uno Chef */
	public boolean isChefLogged()
	{
		return loggedUser instanceof Chef;
	}

	/** @return true se l'utente loggato è un Partecipante */
	public boolean isPartecipanteLogged()
	{
		return loggedUser instanceof Partecipante;
	}

	/** @return Chef attualmente autenticato */
	public Chef getLoggedChef()
	{
	    return (Chef)getLoggedUser();
	}
	
	/** Metodi getter Lazy-loaded per ogni DAO */
	public AdesioneDAO_Postgres getAdesioneDAO()
	{
		if(adesioneDAO == null)
			adesioneDAO = new AdesioneDAO_Postgres();

		return adesioneDAO;
	}

	public ArgomentoDAO_Postgres getArgomentoDAO()
	{
		if(argomentoDAO == null)
			argomentoDAO = new ArgomentoDAO_Postgres();

		return argomentoDAO;
	}

	public ChefDAO_Postgres getChefDAO()
	{
		if(chefDAO == null)
			chefDAO = new ChefDAO_Postgres();

		return chefDAO;
	}

	public CorsoDAO_Postgres getCorsoDAO()
	{
		if(corsoDAO == null)
			corsoDAO = new CorsoDAO_Postgres();

		return corsoDAO;
	}

	public IngredienteDAO_Postgres getIngredienteDAO()
	{
		if(ingredienteDAO == null)
			ingredienteDAO = new IngredienteDAO_Postgres();

		return ingredienteDAO;
	}

	public PartecipanteDAO_Postgres getPartecipanteDAO()
	{
		if(partecipanteDAO == null)
			partecipanteDAO = new PartecipanteDAO_Postgres();

		return partecipanteDAO;
	}

	public ReportMensileDAO_Postgres getReportDAO()
	{
		if(reportDAO == null)
			reportDAO = new ReportMensileDAO_Postgres();
		return reportDAO;
	}

	public RicettaDAO_Postgres getRicettaDAO()
	{
		if(ricettaDAO == null)
			ricettaDAO = new RicettaDAO_Postgres();

		return ricettaDAO;
	}

	public SessioneOnlineDAO_Postgres getSessioneOnlineDAO()
	{
		if(sessioneOnlineDAO == null)
			sessioneOnlineDAO = new SessioneOnlineDAO_Postgres();

		return sessioneOnlineDAO;
	}

	public SessionePraticaDAO_Postgres getSessionePraticaDAO()
	{
		if(sessionePraticaDAO == null)
			sessionePraticaDAO = new SessionePraticaDAO_Postgres();

		return sessionePraticaDAO;
	}

	public UtilizzoDAO_Postgres getUtilizzoDAO()
	{
		if(utilizzoDAO == null)
			utilizzoDAO = new UtilizzoDAO_Postgres();

		return utilizzoDAO;
	}

	/**
	 * --------------------------------------------
	 *          Metodi di Navigazione UI
	 * 
	 *  @param currFrame frame attuale da chiudere
	 * --------------------------------------------
	 */

	/**
	 * Naviga alla schermata di login.
	 */
	public void goToLogin(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			currFrame.dispose();
			new LoginFrame().setVisible(true);
		});
	}

	/**
	 * Naviga alla schermata di registrazione.
	 */
	public void goToRegister(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			currFrame.dispose();
			new RegisterFrame().setVisible(true);
		});
	}

	/**
	 * Naviga alla homepage.
	 */
	public void goToHomepage(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			if(!(currFrame instanceof HomepageFrame))
			{
				currFrame.dispose();
				new HomepageFrame().setVisible(true);
			}
			else
				((HomepageFrame) currFrame).resetView();
		});
	}

	/**
	 * Naviga alla schermata dei corsi.
	 */
	public void goToMyCourses(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			if(!(currFrame instanceof MyCoursesFrame))
			{
				currFrame.dispose();
				new MyCoursesFrame().setVisible(true);
			} 
			else
				((MyCoursesFrame) currFrame).resetView();
		});
	}

	/**
	 * Naviga alla schermata delle ricette personali dello Chef.
	 */
	public void goToMyRecipes(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			if(!(currFrame instanceof MyRecipesFrame))
			{
				currFrame.dispose();
				new MyRecipesFrame().setVisible(true);
			}
			else
				((MyRecipesFrame) currFrame).resetView();
		});
	}

	/**
	 * Naviga alla schermata del report mensile dello Chef.
	 */
	public void goToReport(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			if(!(currFrame instanceof ReportFrame))
			{
				currFrame.dispose();
				new ReportFrame().setVisible(true);
			}
		});
	}

	/**
	 * Naviga alla schermata del profilo personale.
	 */
	public void goToProfile(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			if(!(currFrame instanceof ProfileFrame))
			{
				currFrame.dispose();
				new ProfileFrame().setVisible(true);
			}
		});
	}

	/**
	 * Effettua il logout dell'utente e torna alla schermata di login.
	 */
	public void logout(JFrame currFrame)
	{
		EventQueue.invokeLater(() -> {
			currFrame.dispose();
			loggedUser = null;
			new LoginFrame().setVisible(true);
		});
		
		cacheArgomenti = null;
		cacheRicette = null;
		cacheCorsi = null;
	}

	/**
	 * -------------------------
	 * 
	 * 		RegisterFrame
	 * 
	 * -------------------------
	 */

	/**
	 * Cifra la password in modo sicuro usando BCrypt.
	 * 
	 * @param plainPassword array di caratteri della password in chiaro
	 * @return stringa con hash cifrato
	 */
	public String hashPassword(char[] plainPassword)
	{
		String hashed = BCrypt.hashpw(new String(plainPassword), BCrypt.gensalt(11));
		Arrays.fill(plainPassword, ' ');
		return hashed;
	}

	/**
	 * Notifica il successo della registrazione loggando l'evento e tornando alla
	 * schermata di login per l'autenticazione.
	 * 
	 * @param currFrame il frame di registrazione da chiudere
	 * @param username  l'username dell'utente appena registrato
	 */
	private void registerSuccess(RegisterFrame currFrame, String username)
	{
		LOGGER.log(Level.INFO, "Registrazione riuscita per utente: {0}", username);
		goToLogin(currFrame);
	}

	/**
	 * Notifica il fallimento della registrazione loggando l'errore e mostrando un
	 * messaggio all'utente.
	 * 
	 * @param currFrame il frame di registrazione
	 * @param message   messaggio di errore da mostrare
	 */
	private void registerFailed(RegisterFrame currFrame, String message)
	{
		LOGGER.log(Level.WARNING, "Tentativo di registrazione fallito: {0}", message);
		currFrame.showError(message);
	}

	/**
	 * Registra un nuovo partecipante dopo aver verificato che:
	 * <ul>
	 * <li>non esista già un account con lo stesso codice fiscale</li>
	 * <li>non esista già un account con la stessa email</li>
	 * <li>non esista già un utente con lo stesso username</li>
	 * </ul>
	 * Se tutte le verifiche hanno esito positivo, il partecipante viene salvato nel
	 * database e l’utente viene reindirizzato alla schermata di login.
	 *
	 * @param currFrame il frame di registrazione attuale
	 * @param username  lo username scelto
	 * @param nome      il nome del partecipante
	 * @param cognome   il cognome del partecipante
	 * @param codFisc   il codice fiscale
	 * @param data      la data di nascita
	 * @param luogo     il luogo di nascita
	 * @param email     l'indirizzo email
	 * @param pass      la password in chiaro (verrà hashata e svuotata)
	 * @throws DAOException se si verifica un errore durante l'accesso al database
	 */
	private void registerPartecipante(RegisterFrame currFrame, String username, String nome, String cognome,
			String codFisc, LocalDate data, String luogo, String email, char[] pass) throws DAOException
	{
		if(getPartecipanteDAO().existsPartecipanteByCodiceFiscale(codFisc))
			registerFailed(currFrame, ERR_CF_EXISTING);
		else if(getPartecipanteDAO().existsPartecipanteByEmail(email))
			registerFailed(currFrame, ERR_EMAIL_EXISTING);
		else
		{
			try
			{
				tryGetUser(username);
				registerFailed(currFrame, "Username già utilizzato.");
			} 
			catch(RecordNotFoundException e)
			{
				Partecipante p = new Partecipante(username, nome, cognome, codFisc, data, luogo, email,
						hashPassword(pass), null, null);
				getPartecipanteDAO().save(p);
				registerSuccess(currFrame, username);
			}
		}
	}

	/**
	 * Registra un nuovo chef dopo aver verificato che:
	 * <ul>
	 * <li>non esista già un account con lo stesso codice fiscale</li>
	 * <li>non esista già un account con la stessa email</li>
	 * <li>non esista già un utente con lo stesso username</li>
	 * </ul>
	 * Se tutte le verifiche hanno esito positivo:
	 * <ul>
	 * <li>il curriculum viene salvato nella cartella
	 * {@code resources/<username>/Curriculum/}</li>
	 * <li>lo chef viene inserito nel database</li>
	 * <li>l’utente viene reindirizzato alla schermata di login</li>
	 * </ul>
	 *
	 * @param currFrame    il frame di registrazione attuale
	 * @param username     lo username scelto
	 * @param nome         il nome dello chef
	 * @param cognome      il cognome dello chef
	 * @param codFisc      il codice fiscale
	 * @param data         la data di nascita
	 * @param luogo        il luogo di nascita
	 * @param email        l'indirizzo email
	 * @param pass         la password in chiaro (verrà hashata e svuotata)
	 * @param selectedFile il file PDF del curriculum da salvare
	 * @throws DAOException se si verifica un errore durante l’accesso al database
	 * @throws IOException  se si verifica un errore durante il salvataggio del
	 *                      curriculum
	 */
	private void registerChef(RegisterFrame currFrame, String username, String nome, String cognome, String codFisc,
			LocalDate data, String luogo, String email, char[] pass, File selectedFile) throws DAOException, IOException
	{
		if(getChefDAO().existsChefByCodiceFiscale(codFisc))
			registerFailed(currFrame, ERR_CF_EXISTING);
		else if(getChefDAO().existsChefByEmail(email))
			registerFailed(currFrame, ERR_EMAIL_EXISTING);
		else
		{
			try
			{
				tryGetUser(username);
				registerFailed(currFrame, "Username già utilizzato.");
			} 
			catch(RecordNotFoundException e)
			{
				String curriculumPath = saveCurriculumFile(username, selectedFile);
				Chef c = new Chef(username, nome, cognome, codFisc, data, luogo, email, hashPassword(pass),
						curriculumPath, null, null);
				getChefDAO().save(c);
				registerSuccess(currFrame, username);
			}
		}
	}

	/**
	 * Salva il file PDF del curriculum per uno Chef in una directory locale del
	 * progetto. Sovrascrive eventuali file esistenti con lo stesso nome.
	 *
	 * @param username     lo username dell’utente
	 * @param selectedFile file PDF selezionato da salvare
	 * @return percorso relativo del file salvato, da memorizzare nel database
	 * @throws IOException in caso di errori durante la scrittura
	 */
	private String saveCurriculumFile(String username, File selectedFile) throws IOException
	{
		String relativePath = "resources" + File.separator + username + File.separator + "Curriculum";
		String fullPathString = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
				+ File.separator + relativePath;

		Path destinationDir = Paths.get(fullPathString);
		Files.createDirectories(destinationDir);

		Path destinationPath = destinationDir.resolve(selectedFile.getName());
		Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

		LOGGER.log(Level.INFO, "File salvato con successo in: {0}", destinationPath);
		return relativePath + File.separator + selectedFile.getName();
	}

	/**
	 * Gestisce la registrazione di un nuovo utente, determinando dinamicamente se
	 * registrarlo come Chef o Partecipante. Effettua tutte le validazioni di
	 * unicità (username, email, codice fiscale) e salva i dati nel database.
	 * <p>
	 * In caso di Chef, salva anche il curriculum su disco prima dell'inserimento.
	 * Mostra messaggi d'errore in caso di dati duplicati, problemi di I/O o errori
	 * sul database.
	 *
	 * @param currFrame      il frame attivo da chiudere dopo la registrazione
	 * @param isPartecipante true se l'utente è un partecipante, false se è uno chef
	 * @param nome           nome dell’utente
	 * @param cognome        cognome dell’utente
	 * @param data           data di nascita
	 * @param luogo          luogo di nascita
	 * @param codFisc        codice fiscale (univoco)
	 * @param email          email dell’utente
	 * @param username       username scelto
	 * @param pass           password dell’utente (in chiaro, verrà subito hashata)
	 * @param selectedFile   curriculum in PDF, obbligatorio solo per Chef (null per
	 *                       Partecipanti)
	 */
	public void checkRegister(RegisterFrame currFrame, boolean isPartecipante, String nome, String cognome,
			LocalDate data, String luogo, String codFisc, String email, String username, char[] pass, File selectedFile)
	{
		try
		{
			if(isPartecipante)
				registerPartecipante(currFrame, username, nome, cognome, codFisc, data, luogo, email, pass);
			else
				registerChef(currFrame, username, nome, cognome, codFisc, data, luogo, email, pass, selectedFile);
		} 
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore registrazione DB", e);
			registerFailed(currFrame, "Errore di accesso al database.");
		} 
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore salvataggio file", e);
			registerFailed(currFrame, "Errore di salvataggio file.");
		}
	}

	/**
	 * -------------------------
	 * 
	 * LoginFrame
	 * 
	 * -------------------------
	 */

	/**
	 * Verifica se la password fornita corrisponde all'hash salvato.
	 * 
	 * @param hashedPassword hash della password da DB
	 * @param inputPassword  password inserita da GUI
	 * @return true se corrispondono
	 */
	private boolean checkPassword(String hashedPassword, char[] inputPassword)
	{
		boolean match = BCrypt.checkpw(new String(inputPassword), hashedPassword);
		Arrays.fill(inputPassword, ' ');
		return match;
	}

	/**
	 * Recupera l'utente associato allo username fornito, cercando prima tra i
	 * partecipanti, poi tra gli chef. Usato sia per la validazione in fase di login
	 * che in fase di registrazione.
	 *
	 * @param username username da cercare
	 * @return istanza di {@link Utente} (può essere {@link Chef} o
	 *         {@link Partecipante})
	 * @throws DAOException            se si verifica un errore durante l'accesso al
	 *                                 database
	 * @throws RecordNotFoundException se non esiste nessun utente con lo username
	 *                                 fornito
	 */
	private Utente tryGetUser(String username) throws DAOException, RecordNotFoundException
	{
		try
		{
			return getPartecipanteDAO().getPartecipanteByUsername(username);
		} 
		catch(PartecipanteNotFoundException e1)
		{
			try
			{
				return getChefDAO().getChefByUsername(username);
			} 
			catch(ChefNotFoundException e2)
			{
				throw new RecordNotFoundException("Utente non trovato");
			}
		}
	}

	/**
	 * Gestisce il successo del login: aggiorna lo stato e naviga alla homepage.
	 *
	 * @param currFrame il frame di login da chiudere
	 * @param currUser  l'utente autenticato
	 */
	private void loginSuccess(LoginFrame currFrame, Utente currUser)
	{
		LOGGER.log(Level.INFO, "Login riuscito per utente: {0}", currUser.getUsername());
		this.loggedUser = currUser;
		goToHomepage(currFrame);
	}

	/**
	 * Gestisce il fallimento del login: mostra errore e logga l'evento.
	 *
	 * @param currFrame il frame di login
	 * @param message   messaggio di errore da mostrare
	 */
	private void loginFailed(LoginFrame currFrame, String message)
	{
		LOGGER.log(Level.WARNING, "Tentativo di Login fallito: {0}", message);
		currFrame.showError(message);
	}

	/**
	 * Controlla se le credenziali inserite sono corrette. In caso positivo,
	 * effettua login e naviga alla homepage.
	 *
	 * @param currFrame il frame di login corrente
	 * @param username  username inserito
	 * @param pass      password inserita
	 */
	public void checkLogin(LoginFrame currFrame, String username, char[] pass)
	{
		try
		{
			Utente user = tryGetUser(username);

			if(checkPassword(user.getHashPassword(), pass))
				loginSuccess(currFrame, user);
			else
				loginFailed(currFrame, "Username o password errati.");
		} 
		catch(RecordNotFoundException e)
		{
			loginFailed(currFrame, "Username o password errati."); // Evitiamo User Enumeration
		} 
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore durante login nel DB: " + e.getMessage(), e);
			loginFailed(currFrame, "Errore di accesso al database.");
		} 
		finally
		{
			Arrays.fill(pass, ' '); // It is recommended that the returned character array be cleared after use by
									// setting each character to zero.
		}
	}

	/**
	 * -------------------------
	 * 
	 * 		HomepageFrame
	 * 
	 * -------------------------
	 */

	public void loadAllCorsiHomepage(List<Integer> idsCorsi, List<String> namesCorsi, List<List<Integer>> idsArguments,
			  List<List<String>> namesArguments, List<Date> startDates, List<Integer> sessionsNumbers)
	{
		try
		{
			if(cacheCorsi == null || cacheCorsi.isEmpty())
				cacheCorsi = getCorsoDAO().getAllCorsi();

			if(isChefLogged())
			{
				for(Corso c : cacheCorsi)
				{			
					if(c.getChef().getId()!=getLoggedUser().getId())
					{
						idsCorsi.add(c.getId());
						namesCorsi.add(c.getNome());

			            List<Integer> thisCourseArgIds = new ArrayList<>();
			            List<String> thisCourseArgNames = new ArrayList<>();
			            
						for(Argomento a : c.getArgomenti())	
						{
							thisCourseArgIds.add(a.getId());
				            thisCourseArgNames.add(a.getNome());
						}	
						
						idsArguments.add(thisCourseArgIds);
			            namesArguments.add(thisCourseArgNames);
			            
						startDates.add(c.getDataInizio());
						sessionsNumbers.add(c.getNumeroSessioni());
					}					
				}
			}
			else
			{
				for(Corso c : cacheCorsi)
				{				
					idsCorsi.add(c.getId());
					namesCorsi.add(c.getNome());
	
		            List<Integer> thisCourseArgIds = new ArrayList<>();
		            List<String> thisCourseArgNames = new ArrayList<>();
		            
					for(Argomento a : c.getArgomenti())	
					{
						thisCourseArgIds.add(a.getId());
			            thisCourseArgNames.add(a.getNome());
					}	
					
					idsArguments.add(thisCourseArgIds);
		            namesArguments.add(thisCourseArgNames);
		            
					startDates.add(c.getDataInizio());
					sessionsNumbers.add(c.getNumeroSessioni());
				}
			}
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore loadCorsi da DB", e);
		}
	}

	/**
     *  -------------------------
     * 
     *   	  MyCoursesFrame
     *   	  CreateCourseDialog
     *   	  DetailedCourseFrame
     *   
     *  -------------------------
	*/
	
	public void loadCorsiForMyCourses(List<Integer> idsCorsi, List<String> namesCorsi, List<List<Integer>> idsArguments,
			  List<List<String>> namesArguments, List<Date> startDates, List<Integer> sessionsNumbers)
	{
		try
		{	
			List<Integer> ids = (isChefLogged()) ? getCorsoDAO().getCorsiByIdChef(getLoggedUser().getId()) : 
									 getCorsoDAO().getIdCorsiIscrittiByIdPartecipante(getLoggedUser().getId());
			
			for(Corso c : cacheCorsi)
			{				
				for(Integer id : ids)
				{
					if(c.getId() == id.intValue())
					{
						idsCorsi.add(c.getId());
						namesCorsi.add(c.getNome());
			
			          List<Integer> thisCourseArgIds = new ArrayList<>();
			          List<String> thisCourseArgNames = new ArrayList<>();
			          
						for(Argomento a : c.getArgomenti())	
						{
							thisCourseArgIds.add(a.getId());
				            thisCourseArgNames.add(a.getNome());
						}	
						
						idsArguments.add(thisCourseArgIds);
			          namesArguments.add(thisCourseArgNames);
			          
						startDates.add(c.getDataInizio());
						sessionsNumbers.add(c.getNumeroSessioni());
					}
				}
			}
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore loadCorsi da DB", e);
		}
	}
	
	public String[] loadFrequenza()
	{
		FrequenzaSessioni[] freqs = FrequenzaSessioni.values();
		String[] namesFrequenze = new String[freqs.length];
		
		for(int i = 0; i < freqs.length; i++)
			namesFrequenze[i] = freqs[i].name();
		
		return namesFrequenze;
	}
	
	public void loadArgomenti(List<Integer> idsArgomenti, List<String> namesArgomenti)
	{
		try
		{
			if(cacheArgomenti == null || cacheArgomenti.isEmpty())
				cacheArgomenti = getArgomentoDAO().getAllArgomenti();

			for(Argomento a : cacheArgomenti)
			{
				idsArgomenti.add(a.getId());
				namesArgomenti.add(a.getNome());
			}
		} 
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore loadArgomenti da DB", e);
		}
	}
	
	public void loadRicette(List<Integer> idsRicette, List<String> namesRicette)
	{
		try
		{
			cacheRicette = ((Chef)loggedUser).getRicette();
			
			if(cacheRicette == null || cacheRicette.isEmpty())
				cacheRicette = getRicettaDAO().getRicetteByIdChef(loggedUser.getId());

			for(Ricetta r : cacheRicette)
			{
				idsRicette.add(r.getId());
				namesRicette.add(r.getNome());
			}
		} 
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore loadRicette da DB", e);
		}
	}
	
	public void registerIscrizione(int idCorso)
	{
		try
		{
			getCorsoDAO().saveIscrizione(idCorso, getLoggedUser().getId());
			((Partecipante)getLoggedUser()).getCorsi().add(getCorsoDAO().getCorsoById(idCorso));
		}
		catch(DAOException e)
		{		
			LOGGER.log(Level.SEVERE, "Errore registerIscrizione da DB", e);
		}
	}
	
	public void disiscriviCorso(Window owner, DetailedCourseFrame card, int idCorso)
	{
		try
		{
			getCorsoDAO().deleteIscrizione(idCorso, getLoggedUser().getId());
			((Partecipante)getLoggedUser()).getCorsi().remove(getCorsoDAO().getCorsoById(idCorso));
			card.dispose();
			if(owner instanceof MyCoursesFrame) ((MyCoursesFrame)owner).removeCourseCard(idCorso);
		}
		catch(DAOException e)
		{
			card.showMessage("Impossibile disiscriversi dal corso!! ");
			LOGGER.log(Level.SEVERE, "Errore disiscriviCorso da DB", e);
		}
	}
	
	public Boolean checkIscritto(int idCorso)
	{
		try
		{
			return getCorsoDAO().checkIscrizione(idCorso, getLoggedUser().getId());
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore checkIscritto da DB", e);
			return null;
		}
	}
	
	public Integer getNumeroIscritti(int idCourse)
	{
		try
		{
			return getCorsoDAO().getNumeroIscrittiById(idCourse);
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore getNumeroIscritti da DB", e);
			return null;
		}
	}
	
	public void saveAdesione(SessionInfoPanel panel, int idSession)
	{
		try
		{
			Adesione toSave = new Adesione(LocalDate.now(), (Partecipante)getLoggedUser(), getSessionePraticaDAO().getSessionePraticaById(idSession));
			getAdesioneDAO().save(toSave);
			((Partecipante)getLoggedUser()).aggiungiAdesione(toSave);
		}
		catch(DAOException e)
		{
			panel.showMessage("Non puoi aderire alla sessione se sei a meno di 3 giorni dall'avvenimento di essa!");
			LOGGER.log(Level.SEVERE, "Errore nel salvataggio dell'adesione alla sessione pratica " + idSession, e);
		}
	}
	
	public Boolean checkAdesione(int idSessionePratica)
	{
		try
		{
			return getAdesioneDAO().checkAdesione(idSessionePratica, getLoggedUser().getId());
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore checkAderito da DB", e);
			return null;
		}
	}
	
	public void removeAdesione(SessionInfoPanel panel, int idSession)
	{
		try
		{
			getAdesioneDAO().delete(getLoggedUser().getId(), idSession);
			((Partecipante)getLoggedUser()).getAdesioni().remove(new Adesione(LocalDate.now(), (Partecipante)getLoggedUser(), getSessionePraticaDAO().getSessionePraticaById(idSession)));
		}
		catch(DAOException e)
		{
			panel.showMessage("Non puoi eliminare l'adesione alla sessione se sei a meno di 3 giorni dall'avvenimento di essa!");
			LOGGER.log(Level.SEVERE, "Errore nel salvataggio dell'adesione alla sessione pratica " + idSession, e);
		}
	}
	
	public Integer getNumeroAdesioni(int idSessione)
	{
		try
		{
			return getAdesioneDAO().getNumeroAdesioniByIdSessionePratica(idSessione);
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore getNumeroAdesionida DB", e);
			return null;
		}
	}
	
	public void showCourseDetail(JFrame parentFrame, int idCorso)
	{
		if(cacheCorsi == null || cacheCorsi.isEmpty())
			cacheCorsi = getCorsoDAO().getAllCorsi();
		
		DetailedCourseFrame corso = new DetailedCourseFrame(parentFrame);
		
		for(Corso c : cacheCorsi)
		{
			if(c.getId() == idCorso)
				corso.setCourseData(c.getId(), c.getNome(), c.getDescrizione(), c.getDataInizio().toLocalDate(), c.getNumeroSessioni(), 
									c.getFrequenzaSessioni().toString(), (c.getIsPratico()) ? c.getLimite() : null, c.getCosto(), 
									c.getChef().getNome(), c.getChef().getCognome(), 
									(parentFrame instanceof HomepageFrame) ? "Homepage": "MyCourses");
		}
		
		List<Sessione> sessioni = new ArrayList<>();
		sessioni.addAll(getSessionePraticaDAO().getSessioniPraticheByIdCorso(idCorso));
		sessioni.addAll(getSessioneOnlineDAO().getSessioniOnlineByIdCorso(idCorso));
		
		corso.setSessions(sessioni);
		corso.setVisible(true);
	}
	
	public void showCreateCourseDialog(MyCoursesFrame parentFrame)
	{
		 new CreateCourseDialog(parentFrame, false).setVisible(true);	
	}
	
	public void callRemoveSessionCard(CreateCourseDialog parent, CreateSessionPanel panel)
	{
		parent.removeSessionCard(panel);
		parent.setFrequenzaToLibera();
	}	
	
	public void createCourse(JFrame courseFrame, CreateCourseDialog currDialog, String nomeCorso, LocalDate dataInizio, int numeroSessioni, String frequenza,
							 int limite, String descrizione, BigDecimal costo, boolean isPratico, List<Integer> idArgomenti,
							 List<Integer> durateOnline, List<Time>  orariOnline, List<LocalDate> dateOnline,
							 List<String> linksOnline, List<Integer> duratePratiche, List<Time> orariPratiche,
							 List<LocalDate> datePratiche, List<String> indirizziPratiche, 
							 List<ArrayList<Integer>> idsRicettePratiche) 
	{
		try
		{
			ArrayList<Argomento> argomenti = new ArrayList<>();
			
			// Costruisco gli argomenti da salvare guardando prima in cache e poi fallback su dao
			for(Integer id : idArgomenti)
			{
				boolean trovato = false;
				
				for(Argomento a : cacheArgomenti)
				{
					if(a.getId() == id)
					{
						argomenti.add(a);
						trovato = true;
						break;
					}		
				}
				if(!trovato)
					argomenti.add(getArgomentoDAO().getArgomentoById(id));
			}
			
			ArrayList<ArrayList<Ricetta>> ricette = new ArrayList<>();
			
			// Costruisco le ricette da salvare guardando prima in cache e poi fallback su dao
			for(List<Integer> ricetteSessione : idsRicettePratiche)
			{
				ArrayList<Ricetta> ricettePerSessione = new ArrayList<>();
				
				for(Integer id : ricetteSessione)
				{
					boolean trovato = false;
					
					for(Ricetta r : cacheRicette)
					{
						if(r.getId() == id)
						{
							ricettePerSessione.add(r);
							trovato = true;
							break;
						}
					}
						
					if(!trovato)
						ricettePerSessione.add(getRicettaDAO().getRicettaById(id));
				}
				
				ricette.add(ricettePerSessione);
			}
			
			// Costruisco le sessioni da salvare guardando prima in cache e poi fallback su dao		
			ArrayList<Sessione> sessioni = new ArrayList<>();
			for(int i = 0; i < durateOnline.size(); i++)
				sessioni.add(new SessioneOnline(durateOnline.get(i), orariOnline.get(i), dateOnline.get(i), linksOnline.get(i)));
			for(int i = 0; i < duratePratiche.size(); i++)
				sessioni.add(new SessionePratica(duratePratiche.get(i), orariPratiche.get(i), datePratiche.get(i), indirizziPratiche.get(i),
												 ricette.get(i)));
			
			Corso toSaveCorso = new Corso(nomeCorso, dataInizio, numeroSessioni, FrequenzaSessioni.valueOf(frequenza), limite, descrizione, 
					  costo, isPratico, (Chef) getLoggedUser(), argomenti, sessioni);
			
			getCorsoDAO().save(toSaveCorso);
			cacheCorsi.add(toSaveCorso);
			
			List<String> namesArgs = new ArrayList<>();
	        for(Argomento a : argomenti)
	        	namesArgs.add(a.getNome());
			
			CourseCardPanel newCard = new CourseCardPanel
			(
			        toSaveCorso.getId(),
			        toSaveCorso.getNome(),
			        idArgomenti,            
			        namesArgs,           
			        Date.valueOf(dataInizio),
			        numeroSessioni
			);
			((MyCoursesFrame)courseFrame).addCourseCard(newCard);

			JOptionPane.showMessageDialog(currDialog, "Corso " + nomeCorso + " salvato con successo");
			LOGGER.log(Level.INFO, "Salvataggio del corso " + nomeCorso + " dello chef " + getLoggedUser().getUsername() + " effettuato ");
			currDialog.dispose();
		} 
		catch(DAOException e)
		{
			JOptionPane.showMessageDialog(currDialog, "Errore nel salvataggio del corso: " + e.getMessage());
			LOGGER.log(Level.SEVERE, "Errore nel salvataggio del corso su DB", e);
		}
	}

	public void openEditCourseDialog(Window owner, DetailedCourseFrame dialog, int idCorso)
	{
	    Corso corso = null;
	    List<Sessione> sessioni = new ArrayList<>();
	    boolean hasPastSessions = false;

	    try
	    {
	        corso = getCorsoDAO().getCorsoById(idCorso);
	        if(corso == null)
	        {
	             throw new CorsoNotFoundException("Corso con ID " + idCorso + " non trovato per la modifica.");
	        }

	        List<SessioneOnline> online = getSessioneOnlineDAO().getSessioniOnlineByIdCorso(idCorso);
	        List<SessionePratica> pratiche = getSessionePraticaDAO().getSessioniPraticheByIdCorso(idCorso);
	        sessioni.addAll(online);
	        sessioni.addAll(pratiche);
	        sessioni.sort(Comparator.comparing(Sessione::getData).thenComparing(Sessione::getOrario));

	        LocalDate today = LocalDate.now();
	        hasPastSessions = sessioni.stream().anyMatch(s -> s.getData().toLocalDate().isBefore(today));

	    }
	    catch(DAOException e)
	    {
	        JOptionPane.showMessageDialog(owner, "Errore nel caricamento dei dati del corso: " + e.getMessage(), "Errore Caricamento", JOptionPane.ERROR_MESSAGE);
	        LOGGER.log(Level.SEVERE, "Errore fetch dati per edit corso ID: " + idCorso, e);
	        return;
	    }

	    CreateCourseDialog editDialog = new CreateCourseDialog((JFrame)owner, true);

	    List<Integer> idArgomentiSelezionati = new ArrayList<>();
	    if(corso.getArgomenti() != null)
	    {
	        for(Argomento a : corso.getArgomenti())
	            idArgomentiSelezionati.add(a.getId());
	    }
	    else
	    {
	         LOGGER.log(Level.WARNING, "Il corso con ID {0} caricato dal DAO non ha argomenti.", idCorso);
	    }

	    boolean canChangeStartDate = !hasPastSessions;
	    editDialog.popolaDatiCorso(
	        corso.getId(),
	        corso.getNome(),
	        corso.getDescrizione(),
	        corso.getDataInizio().toLocalDate(),
	        corso.getCosto().doubleValue(),
	        corso.getFrequenzaSessioni().toString(),
	        corso.getIsPratico(),
	        corso.getLimite(),
	        idArgomentiSelezionati,
	        canChangeStartDate	        
	    );

	    for(Sessione s : sessioni)
	    {
	        String link = null;
	        String indirizzo = null;
	        List<Integer> idRicette = new ArrayList<>();
	        boolean isPratica = false;

	        if(s instanceof SessionePratica)
	        {
	            isPratica = true;
	            SessionePratica sp = (SessionePratica) s;
	            indirizzo = sp.getIndirizzo();
	            if(sp.getRicette() != null)
	            {
	                for(Ricetta r : sp.getRicette())
	                {
	                    idRicette.add(r.getId());
	                }
	            }
	        }
	        else
	        {
	            SessioneOnline so = (SessioneOnline) s;
	            link = so.getLinkRiunione();
	        }

	        editDialog.aggiungiSessionePopolata(
	            s.getData().toLocalDate(),
	            s.getOrario().toLocalTime(),
	            s.getDurata(),
	            isPratica,
	            link,
	            indirizzo,
	            idRicette
	        );
	    }

	    // Applica le regole iniziali delle date DOPO aver aggiunto tutte le sessioni
	    editDialog.triggerInitialReschedule();

	    editDialog.setVisible(true);
	}

	public void editCourse(JFrame courseFrame, CreateCourseDialog currDialog, int idCorso,
            String nomeCorso, LocalDate dataInizioInput, // Ignorato, ricalcolato
            int numeroSessioni, String frequenza, // 'frequenza' ora è cruciale (Rule 4)
            int limite, String descrizione, BigDecimal costo, boolean isPratico, // 'limite' e 'isPratico' ignorati (Rule 1)
            List<Integer> idArgomenti, 
            List<Integer> durateOnline, List<Time>  orariOnline, List<LocalDate> dateOnline,
            List<String> linksOnline,
            List<Integer> duratePratiche, List<Time> orariPratiche,
            List<LocalDate> datePratiche, List<String> indirizziPratiche,
            List<ArrayList<Integer>> idsRicettePratiche)
	{
		Connection conn = null;
		Corso corsoOriginale = null;
		List<SessioneOnline> oldOnlineSessions = null;
		List<SessionePratica> oldPraticaSessions = null;
		ArrayList<Argomento> argomentiOriginali = null; 
		LocalDate today = LocalDate.now();
		
		try
		{
			// 0. Recupera dati originali necessari
			corsoOriginale = getCorsoDAO().getCorsoById(idCorso); 
			if(corsoOriginale == null)
			{
			  throw new DAOException("Corso originale non trovato per l'aggiornamento.");
			}
			
			oldOnlineSessions = getSessioneOnlineDAO().getSessioniOnlineByIdCorso(idCorso);
			oldPraticaSessions = getSessionePraticaDAO().getSessioniPraticheByIdCorso(idCorso);
			argomentiOriginali = (ArrayList<Argomento>) getArgomentoDAO().getArgomentiByIdCorso(idCorso);
			
			// Controlla se esistono sessioni passate (Rule 3)
			boolean hadPastSessions = oldOnlineSessions.stream().anyMatch(s -> s.getData().toLocalDate().isBefore(today)) ||
			                       oldPraticaSessions.stream().anyMatch(s -> s.getData().toLocalDate().isBefore(today));
			
			
			// 1. Costruisci DTO Ricette (solo per sessioni future, Rule 5)
			int ricettaIdx = 0;
			ArrayList<ArrayList<Ricetta>> ricettePerSessioniFuture = new ArrayList<>();
			for(int i = 0; i < datePratiche.size(); i++)
			{
			 // Costruisci le ricette solo se la sessione pratica è futura (o oggi)
			 if(!datePratiche.get(i).isBefore(today))
			 {
			     List<Integer> ricetteSessione = idsRicettePratiche.get(i);
			     ArrayList<Ricetta> ricettePerSessione = new ArrayList<>();
			     for(Integer id : ricetteSessione)
			     {
			         Ricetta r = cacheRicette.stream()
			                                 .filter(rec -> rec.getId() == id)
			                                 .findFirst()
			                                 .orElseGet(() -> getRicettaDAO().getRicettaById(id));
			         if(r != null)
			             ricettePerSessione.add(r);
			         else
			              throw new DAOException("Ricetta con ID " + id + " non trovata durante la modifica del corso.");
			     }
			     ricettePerSessioniFuture.add(ricettePerSessione);
			 }
			}			
			
			// 2. Costruisci DTO NUOVE Sessioni (solo quelle future, Rule 5)
			ArrayList<SessioneOnline> nuoveSessioniOnlineFuture = new ArrayList<>();
			ArrayList<SessionePratica> nuoveSessioniPraticheFuture = new ArrayList<>();
			
			for(int i = 0; i < durateOnline.size(); i++)
			{
				 if(!dateOnline.get(i).isBefore(today)) // Solo sessioni future (o oggi)
				     nuoveSessioniOnlineFuture.add(new SessioneOnline(durateOnline.get(i), orariOnline.get(i), dateOnline.get(i), linksOnline.get(i)));
			}
			
			ricettaIdx = 0; // Resetta l'indice per le ricette
			for(int i = 0; i < duratePratiche.size(); i++)
			{
				 if(!datePratiche.get(i).isBefore(today)) // Solo sessioni future (o oggi)
				 {
				     nuoveSessioniPraticheFuture.add(
				         new SessionePratica(
				             duratePratiche.get(i), 
				             orariPratiche.get(i), 
				             datePratiche.get(i), 
				             indirizziPratiche.get(i), 
				             ricettePerSessioniFuture.get(ricettaIdx++) // Usa l'indice separato
				         )
				     );
				 }
			}
			
			// 3. Determina la data d'inizio effettiva da salvare (Rule 3)
			LocalDate dataInizioDaSalvare;
			
			if(hadPastSessions) // Se c'erano sessioni passate, la data d'inizio NON cambia
			{
				 dataInizioDaSalvare = corsoOriginale.getDataInizio().toLocalDate();
				 LOGGER.log(Level.INFO, "Data inizio non modificata (corso ID {0}) perché esistono sessioni passate. Rimane: {1}", new Object[]{idCorso, dataInizioDaSalvare});
			}
			else // Se NON c'erano sessioni passate, la data d'inizio si adegua alla prima sessione futura
			{
			  // Raccogli *tutte* le date delle sessioni future (online e pratiche) dal dialog
			  List<LocalDate> allFutureDates = new ArrayList<>();
			  for(int i = 0; i < dateOnline.size(); i++)
			  {
			      if(!dateOnline.get(i).isBefore(today)) 
			          allFutureDates.add(dateOnline.get(i));
			  }
			  for(int i = 0; i < datePratiche.size(); i++)
			  {
			      if(!datePratiche.get(i).isBefore(today)) 
			          allFutureDates.add(datePratiche.get(i));
			  }
			
			  if(allFutureDates.isEmpty())
			  {
			      throw new RequiredSessioneException(); 
			  }
			
			  // Trova la data più vicina nel tempo (la minima)
			  LocalDate primaSessioneData = Collections.min(allFutureDates);
			  dataInizioDaSalvare = primaSessioneData;
			  LOGGER.log(Level.INFO, "Data inizio corso ID {0} impostata alla prima sessione futura: {1}", new Object[]{idCorso, dataInizioDaSalvare});
			}
			
			
			// 4. Costruisci DTO Corso Aggiornato (per il DAO.update)
			Corso corsoAggiornato = new Corso(
			 nomeCorso,                    
			 dataInizioDaSalvare,         
			 numeroSessioni,                
			 FrequenzaSessioni.valueOf(frequenza),
			 corsoOriginale.getLimite(),    
			 descrizione,                   
			 corsoOriginale.getCosto(),     
			 corsoOriginale.getIsPratico(),  
			 (Chef) getLoggedUser(), 
			 argomentiOriginali,            
			 new ArrayList<Sessione>()                         
			);
			corsoAggiornato.setId(idCorso);
			
			
			// 5. Associa il Corso aggiornato alle NUOVE Sessioni FUTURE
			for(SessioneOnline so : nuoveSessioniOnlineFuture)
				so.setCorso(corsoOriginale); // Associa all'oggetto originale

			for(SessionePratica sp : nuoveSessioniPraticheFuture)
				sp.setCorso(corsoOriginale); // Associa all'oggetto originale
			
			// --- OPERAZIONI DB ---
			
			// 6. Delete SOLO le Sessioni *future* esistenti (Rule 5)
			for(SessioneOnline oldSo : oldOnlineSessions)
			 if(!oldSo.getData().toLocalDate().isBefore(today)) // Elimina solo future e odierne
			    getSessioneOnlineDAO().delete(oldSo.getId());

			for(SessionePratica oldSp : oldPraticaSessions)
			 if(!oldSp.getData().toLocalDate().isBefore(today)) // Elimina solo future e odierne
			    getSessionePraticaDAO().delete(oldSp.getId());
	
			// --- INIZIO TRANSAZIONE ---
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
	        boolean corsoDetailsChanged = false;
	        
	        if(!corsoOriginale.getNome().equals(corsoAggiornato.getNome()))
	            corsoDetailsChanged = true;
	        if(!corsoOriginale.getDescrizione().equals(corsoAggiornato.getDescrizione()))
	            corsoDetailsChanged = true;
	        if(!corsoOriginale.getDataInizio().toLocalDate().equals(dataInizioDaSalvare))
	            corsoDetailsChanged = true;
	        if(!corsoOriginale.getFrequenzaSessioni().toString().equals(frequenza))
	            corsoDetailsChanged = true;

	        if(corsoDetailsChanged) 
	        {
	            LOGGER.log(Level.INFO, "Modifica dati base corso ID {0}...", idCorso);
	            getCorsoDAO().update(corsoOriginale, corsoAggiornato, conn);
	        } 
	        else 
	        {
	            LOGGER.log(Level.INFO, "Dati base del corso ID {0} non modificati. Salto l'update della tabella Corso.", idCorso);
	        }
			
			// 8. Recreate SOLO le Sessioni *future* (Rule 5)
			for(SessioneOnline so : nuoveSessioniOnlineFuture)
			 getSessioneOnlineDAO().save(so, conn);
			
			for(SessionePratica sp : nuoveSessioniPraticheFuture)
			 getSessionePraticaDAO().save(sp, conn); 
			
			// --- COMMIT TRANSAZIONE ---
			conn.commit();
			
			// 9. Aggiorna cache
			cacheCorsi.removeIf(c -> c.getId() == idCorso);
			// Aggiorniamo il DTO in cache (corsoOriginale) con i nuovi dati
			corsoOriginale.setNome(corsoAggiornato.getNome());
			corsoOriginale.setDescrizione(corsoAggiornato.getDescrizione());
			corsoOriginale.setDataInizio(dataInizioDaSalvare);
            corsoOriginale.setFrequenzaSessioni(FrequenzaSessioni.valueOf(frequenza));
            corsoOriginale.setNumeroSessioni(numeroSessioni);
			cacheCorsi.add(corsoOriginale);		
			
			// 10. Aggiorna UI e chiudi finestre
			List<String> namesArgs = new ArrayList<>();
			for(Argomento a : argomentiOriginali)
				namesArgs.add(a.getNome());
			
			CourseCardPanel updatedCard = new CourseCardPanel
			(
				corsoAggiornato.getId(),
				corsoAggiornato.getNome(),
				idArgomenti, 
				namesArgs,
				Date.valueOf(dataInizioDaSalvare),
				numeroSessioni
			);
			
			if(courseFrame instanceof MyCoursesFrame)
				((MyCoursesFrame)courseFrame).updateCourseCard(updatedCard);

			JOptionPane.showMessageDialog(currDialog, "Corso " + nomeCorso + " modificato con successo");
			LOGGER.log(Level.INFO, "Modifica del corso " + nomeCorso + " effettuata ");
			currDialog.dispose();
			
			Window[] windows = Window.getWindows();
			for(Window window : windows)
			{
				 if(window instanceof DetailedCourseFrame && window.isShowing())
				 {
				     window.dispose();
				     break; 
				 }
			}
		}
		catch(RequiredSessioneException reqEx)
		{
			// --- ROLLBACK TRANSAZIONE ---
			if(conn != null)
			{
				try { conn.rollback(); } catch(SQLException ex) { LOGGER.log(Level.SEVERE, "Errore rollback", ex); }
			}
			JOptionPane.showMessageDialog(currDialog, reqEx.getMessage(), "Errore Dati Corso", JOptionPane.ERROR_MESSAGE);
			LOGGER.log(Level.WARNING, "Tentativo di salvare corso senza sessioni: " + reqEx.getMessage());
		}
		catch(DAOException | SQLException e)
		{
			System.err.println("##### ERRORE ORIGINALE in editCourse: " + e.getMessage());
            e.printStackTrace();
			// --- ROLLBACK TRANSAZIONE ---
			if(conn != null)
			{
			  try { conn.rollback(); } catch(SQLException ex) { LOGGER.log(Level.SEVERE, "Errore rollback", ex); }
			}
			JOptionPane.showMessageDialog(currDialog, "Errore nella modifica del corso: " + e.getMessage());
			LOGGER.log(Level.SEVERE, "Errore modifica corso DB", e);
		}
		finally
		{
			// --- CHIUSURA CONNESSIONE ---
			if(conn != null)
			{
			  try { conn.setAutoCommit(true); conn.close(); } catch(SQLException ex) { LOGGER.log(Level.SEVERE, "Errore chiusura connessione", ex); }
			}
		}
	}
	
	public void eliminaCorso(Window owner, DetailedCourseFrame card, int idCorso)
	{
		try
		{
			getCorsoDAO().delete(idCorso);
			
			Corso toRemove = null;
			for(Corso c : cacheCorsi)
			{
				if(c.getId() == idCorso)
				{
					toRemove = c;
					break;
				}	
			}

			cacheCorsi.remove(toRemove);
			card.showMessage("Corso eliminato!");
            card.dispose();
            if(owner instanceof MyCoursesFrame) ((MyCoursesFrame)owner).removeCourseCard(idCorso);
            
		}
		catch(DAOException e)
		{
			card.showMessage(e.getMessage());
			LOGGER.log(Level.SEVERE, "Errore eliminaCorso da DB", e);
		}
	}
	
	/**
	 * -------------------------
	 * 
	 * MyRecipesFrame
	 * CreateRecipesDialog
	 * DetailedRecipeFrame
	 * -------------------------
	 */
			
	private ArrayList<Ricetta> ricette;
	private ArrayList<Ingrediente> ingredienti ;
	
	public void showCreateIngredienteDialog(JDialog parent)
	{
		new CreateIngredienteDialog(parent).setVisible(true);
	}
	
	public void showCreateRecipeDialog(MyRecipesFrame parent)
	{
		new CreateRecipesDialog(parent).setVisible(true);
	}
	
	public void showChangeRecipeDialog(DetailedRecipeDialog currDialog, JXFrame parent, int idRicetta, String nomeRicetta, String provenienzaRicetta, int calorieRicetta, String difficoltaRicetta, String allergeniRicetta, int tempoRicetta, ArrayList<String> nomiIngredienti, ArrayList<Double> quantitaIngredienti, ArrayList<String> udmIngredienti)
	{
		currDialog.dispose();				
		new ChangeRecipeDialog(parent, idRicetta, nomeRicetta, provenienzaRicetta, calorieRicetta, difficoltaRicetta, allergeniRicetta, tempoRicetta, nomiIngredienti, quantitaIngredienti, udmIngredienti).setVisible(true);
	}
	
	public void loadIdIngredientiUtil(ArrayList<String> nomiUtil, ArrayList<Integer> idUtil)
	{
		for(Ingrediente i: ingredienti)
		{
			for(int y=0; y<nomiUtil.size(); y++)
			{
				if(i.getNome().equals(nomiUtil.get(y)))
				{
					idUtil.add(i.getId());
				}
			}
		}
	}
	public void showDetailRecipe(int id, MyRecipesFrame currDialog)
	{
		ArrayList<String> nomiIngredienti= new ArrayList<>();
		ArrayList<Double> quantitaIngredienti= new ArrayList<>();
		ArrayList<String> udmIngredienti= new ArrayList<>();
		for(Ricetta r: ricette)
		{
			if(r.getId()==id)
			{
				for(Utilizzo u: r.getUtilizzi())
				{
					nomiIngredienti.add(u.getIngrediente().getNome());
					quantitaIngredienti.add(u.getQuantita());
					udmIngredienti.add(u.getUdm().toString());
				}
			        
				new DetailedRecipeDialog(currDialog,r.getId(), r.getNome(), r.getProvenienza(), r.getCalorie(), r.getDifficolta().toString(), r.getAllergeni(), r.getTempo(),nomiIngredienti, quantitaIngredienti, udmIngredienti).setVisible(true);
			}						
		}
	}
	
	public void deleteRicetta(JXFrame parent,DetailedRecipeDialog currDialog, int id)
	{
		if(parent instanceof MyRecipesFrame)
		{
			currDialog.dispose();
			try
			{
				getRicettaDAO().delete(id);
				LOGGER.log(Level.INFO, "Ricetta eliminata con successo");
				((MyRecipesFrame)parent).showSuccess("Ricetta eliminata con successo");
				for(Ricetta r: ricette)
				{
					if(r.getId()==id)
					{
						ricette.remove(r);
						break;
					}
				}
				((MyRecipesFrame)parent).deleteCard(id);
			}
			catch(DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione nel DB della ricetta", e);
				((MyRecipesFrame)parent).showError("Errore durante l'eliminazione nel DB della ricetta");
			}
		}
		else
			LOGGER.log(Level.SEVERE, "Questo metodo può essere chiamato solo da un MyRecipesFrame");
	}
	
	
	public void loadAllRicette(ArrayList<String> nomiRicette, ArrayList<String> difficoltaRicette, ArrayList<Integer> calorieRicette, ArrayList<Integer> idRicette)
	{
		ricette = new ArrayList<>();
		try
		{
			for(Ricetta r: getRicettaDAO().getRicetteByIdChef(getLoggedChef().getId()))
			{
				ricette.add(r);
				nomiRicette.add(r.getNome());
				difficoltaRicette.add(r.getDifficolta().toString());
				calorieRicette.add(r.getCalorie());
				idRicette.add(r.getId());
			}
		} 
		catch (DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore loadRicette da DB", e);
		}
	}
	
		
		public void loadIngredienti(ArrayList<String> nomiIngredienti, ArrayList<Integer> idIngredienti)
		{
			ingredienti = new ArrayList<>();
			try
			{
				for(Ingrediente i: getIngredienteDAO().getAllIngredienti())
				{
					ingredienti.add(i);
					nomiIngredienti.add(i.getNome());
					idIngredienti.add(i.getId());
				}
			} 
			catch (DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore loadIngredienti da DB", e);
			}
		}
		
		public String[] loadDifficolta()
	    {
			LivelloDifficolta[] diff = LivelloDifficolta.values();
	        String[] namesDiff = new String[diff.length];
	
	        for(int i = 0; i < diff.length; i++)
	            namesDiff[i] = diff[i].name();
	
	        return namesDiff;
	    }
		
		public String[] loadOrigine()
	    {
			NaturaIngrediente[] orig = NaturaIngrediente.values();
	        String[] namesOrig = new String[orig.length];
	
	        for(int i = 0; i < orig.length; i++)
	        	namesOrig[i] = orig[i].name();
	
	        return namesOrig;
	    }
		public String[] loadUnita()
	    {
			UnitaDiMisura[] unit = UnitaDiMisura.values();
	        String[] namesUnit = new String[unit.length];
	
	        for(int i = 0; i < unit.length; i++)
	        	namesUnit[i] = unit[i].name();
	
	        return namesUnit;
	    }
		
		public void createNewIngredient(CreateIngredienteDialog currentDialog, JDialog parent, String nome, String ni)
		{
	
			Ingrediente i = new Ingrediente(nome, NaturaIngrediente.valueOf(ni));
			try
			{
				ingredienti.add(i);
				getIngredienteDAO().save(i);
				if(parent instanceof CreateRecipesDialog)
					((CreateRecipesDialog)parent).addIngrediente(i.getNome(), i.getId());
				else
					((ChangeRecipeDialog)parent).addIngrediente(i.getNome(), i.getId());
	
				currentDialog.dispose();
				
				if(parent instanceof CreateRecipesDialog)
				{
					((CreateRecipesDialog)parent).showSuccess("Ingrediente creato correttamente, cercalo oppure selezionalo dal fondo della lista.");
					((CreateRecipesDialog)parent).setAllResearch();
				}
				else
				{
					((ChangeRecipeDialog)parent).showSuccess("Ingrediente creato correttamente, cercalo oppure selezionalo dal fondo della lista.");
					((ChangeRecipeDialog)parent).setAllResearch();
				}
				
			}
			catch(DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore salvataggio ingrediente nel DB", e);	
				currentDialog.showError("Errore salvataggio ingrediente nel DB, controllare se l'ingrediente è già presente");
			}
			
		}
		
		public void changeRicetta(JXFrame parent, ChangeRecipeDialog currDialog,  int idRicetta, String nameRicetta, String provenienzaRicetta, int tempoRicetta, int calorieRicetta,
				String difficoltaRicetta, String allergeniRicetta, ArrayList<Integer> newIdIngredienti, ArrayList<Double> newQuantitaIngredienti, ArrayList<String> newUdmIngredienti,
				ArrayList<Integer> oldIdIngredienti, ArrayList<Double> oldQuantitaIngredienti, ArrayList<String> oldUdmIngredienti, ArrayList<Integer> idIngredientiDeleted)
		{
			ArrayList<Utilizzo> toAddUtilizzi = new ArrayList<>();   // Corrisponde a newUtilizziRicetta
			ArrayList<Utilizzo> toUpdateUtilizzi = new ArrayList<>(); // I vecchi ingredienti con valori cambiati
			ArrayList<Utilizzo> toDeleteUtilizzi = new ArrayList<>(); // Gli ingredienti originali da eliminare
			
			ArrayList<Utilizzo> oldUtilizziInput = new ArrayList<>(); // La nuova versione degli ingredienti non eliminati
			ArrayList<Utilizzo> allUtilizzi = new ArrayList<>();      // La lista completa per l'oggetto 'toChangeRicetta'
			
			Ingrediente ingredienteUtil=null;
			Ricetta original=null;
			
			// ricerco la ricetta nella lista del controller
			for(Ricetta r: ricette) {
				if(r.getId()==idRicetta) {
					original=r;
					break; 
				}
			}
			
			if (original == null) {
				currDialog.showError("Ricetta originale non trovata.");
				return;
			}
			
			// prendo gli utilizzi da eliminare
			for(Utilizzo u: original.getUtilizzi())
			{
				for(int idDeleted : idIngredientiDeleted)
				{
					if(u.getIngrediente().getId() == idDeleted)
					{
						toDeleteUtilizzi.add(u); 
						break;
					}					
				}				
			}
			
			// prendo gli utilizzi nuovi
			for(int i=0; i<newIdIngredienti.size(); i++)
			{
				ingredienteUtil = findOrCreateIngrediente(newIdIngredienti.get(i)); // Uso una funzione helper
					
				Utilizzo u = new Utilizzo(newQuantitaIngredienti.get(i), UnitaDiMisura.valueOf(newUdmIngredienti.get(i)),
						ingredienteUtil);
				toAddUtilizzi.add(u);
			}
			
			// prendo gli utilizzi vecchi che non sono stati eliminati, con i nuovi valori
			for(int i=0; i<oldIdIngredienti.size(); i++)
			{
				ingredienteUtil = findOrCreateIngrediente(oldIdIngredienti.get(i)); // Uso una funzione helper
				
				Utilizzo u = new Utilizzo(oldQuantitaIngredienti.get(i), UnitaDiMisura.valueOf(oldUdmIngredienti.get(i)),
						ingredienteUtil);
				oldUtilizziInput.add(u);
			}
			
			//confronto utilizzi da aggiornare
			
			for(Utilizzo newU : oldUtilizziInput)
			{
				for(Utilizzo oldU : original.getUtilizzi())
				{
					// Troviamo la corrispondenza
					if(newU.getIngrediente().getId() == oldU.getIngrediente().getId())
					{
						// E ora confrontiamo i valori per vedere se l'aggiornamento è necessario
						boolean quantitaChanged = newU.getQuantita() != oldU.getQuantita();
						boolean udmChanged = !newU.getUdm().equals(oldU.getUdm());
						
						if(quantitaChanged || udmChanged)
						{
							// Se almeno un campo è cambiato, questo Utilizzo deve essere aggiornato
							// Assegniamo l'IdRicetta e lo aggiungiamo alla lista 'toUpdateUtilizzi'
							newU.setIdRicetta(original.getId()); 
							toUpdateUtilizzi.add(newU);
						}
						break;
					}
				}
			}
			
			try
			{
				// Costruiamo la lista completa per l'oggetto DTO 'toChangeRicetta'
				allUtilizzi.addAll(toAddUtilizzi); 
				allUtilizzi.addAll(oldUtilizziInput); // I vecchi utilizzi (anche se non aggiornati)
				
				Ricetta toChangeRicetta = new Ricetta(nameRicetta, provenienzaRicetta, tempoRicetta, calorieRicetta,LivelloDifficolta.valueOf(difficoltaRicetta), allergeniRicetta, (Chef)getLoggedUser(), allUtilizzi);
				toChangeRicetta.setId(original.getId()); 
				
				Ricetta existingRicetta = getRicettaDAO().getRicettaByNome(toChangeRicetta, ((Chef)getLoggedUser()).getId());
				
				if (!original.getNome().equals(toChangeRicetta.getNome()) && existingRicetta != null && existingRicetta.getId() != original.getId()) {
				    
				    currDialog.showError(ERR_RECIPE_EXISTING);
				    LOGGER.log(Level.SEVERE, "Ricetta con lo stesso nome per lo stesso chef");
				}
				else
				{
					// *** CHIAMATA CORRETTA AL DAO CON TUTTI I PARAMETRI ***
					getRicettaDAO().update(original, toChangeRicetta, toAddUtilizzi, toUpdateUtilizzi, toDeleteUtilizzi);
		
					// Le operazioni DB di delete e update sugli utilizzi sono ora all'interno del DAO e transazionali.
					// Il codice di delete e update sui Utilizzi è stato Rimosso da qui.
					
					LOGGER.log(Level.INFO, "Ricetta salvata con successo");	
					currDialog.showSuccess("Ricetta salvata con successo");
					currDialog.dispose();
					
					// Aggiornamento della UI e della lista locale (ricette)
					((MyRecipesFrame)parent).updateRecipeCard(idRicetta, new RecipeCardPanel(idRicetta, toChangeRicetta.getNome(), toChangeRicetta .getDifficolta().toString(), toChangeRicetta.getCalorie()));
					ricette.remove(original);
					ricette.add(toChangeRicetta);
				}
				
			}
			catch(DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore salvataggio ricetta nel DB", e);	
				// Il rollback avviene nel DAO se è stato implementato correttamente
				currDialog.showError("Errore salvataggio ricetta nel DB");
			}
		}
		
		private Ingrediente findOrCreateIngrediente(int idIngrediente) throws DAOException {
		    Ingrediente ingredienteUtil = null;
		    
		    // Cerca prima nella lista locale (più veloce)
		    for (Ingrediente u : ingredienti) {
		        if (u.getId() == idIngrediente) {
		            ingredienteUtil = u;
		            return ingredienteUtil;
		        }
		    }
		
		    // Se non trovato localmente, lo cerca nel DB e lo restituisce
		    ingredienteUtil = getIngredienteDAO().getIngredienteById(idIngrediente);   
		    return ingredienteUtil;
		}
		
		public void saveRicettaUtilizzi(JXFrame parent, CreateRecipesDialog currDialog,  String nameRicetta, String provenienzaRicetta, int tempoRicetta, int calorieRicetta,
				String difficoltaRicetta, String allergeniRicetta, ArrayList<Integer> idIngredientiRicetta, ArrayList<Double> quantitaIngredienti, ArrayList<String> udmIngredienti)
		{
			ArrayList<Utilizzo> utilizziRicetta = new ArrayList<>();
			Ingrediente ingredienteUtil=null;
			
			for(int i=0; i<idIngredientiRicetta.size(); i++)
			{
	                boolean trovato = false;
	
	                for(Ingrediente u : ingredienti)
	                {
	                    if(u.getId() == idIngredientiRicetta.get(i))
	                    {
	                    	ingredienteUtil=u;
	                        trovato = true;
	                        break;
	                    }
	                if(!trovato)
	                	ingredienteUtil = getIngredienteDAO().getIngredienteById(idIngredientiRicetta.get(i));
	                }
	                
				Utilizzo u = new Utilizzo(quantitaIngredienti.get(i), UnitaDiMisura.valueOf(udmIngredienti.get(i)),
						ingredienteUtil);
				utilizziRicetta.add(u);
			}
			try
			{
				if (parent instanceof MyRecipesFrame)
					{
					Ricetta toSaveRicetta = new Ricetta(nameRicetta, provenienzaRicetta, tempoRicetta, calorieRicetta, 	LivelloDifficolta.valueOf(difficoltaRicetta), allergeniRicetta, (Chef)getLoggedUser(), utilizziRicetta);
					if(getRicettaDAO().existsRicettaByNome(toSaveRicetta, ((Chef)getLoggedUser()).getId()))
					{
						currDialog.showError(ERR_RECIPE_EXISTING);
						LOGGER.log(Level.SEVERE, "Ricetta con lo stesso nome per lo stesso chef");
					}
					else
					{
						getRicettaDAO().save(toSaveRicetta, getLoggedUser().getId());
						LOGGER.log(Level.INFO, "Ricetta salvata con successo");	
						currDialog.showSuccess("Ricetta salvata con successo");
						currDialog.dispose();
						
						((MyRecipesFrame)parent).newRecipeCard(new RecipeCardPanel(toSaveRicetta.getId(), toSaveRicetta.getNome(), toSaveRicetta.getDifficolta().toString(), toSaveRicetta.getCalorie()));
						ricette.add(toSaveRicetta);
					}
				}
				else
					LOGGER.log(Level.SEVERE, "Questo metodo può essere chiamato solo da un MyRecipesFrame");
			}
			catch(DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore salvataggio ricetta nel DB", e);	
				currDialog.showError("Errore salvataggio ricetta nel DB");
			}
			
		}
		
		public void clearRecipeDialogCache()
		{
			ingredienti=null;
		}
		
		public void clearMyRecipesCache()
		{
			ricette = null;
		}
		
	/**
	 * -------------------------
	 * 
	 * ReportFrame
	 * 
	 * -------------------------
	 */

	/**
	 * Carica e apre il report mensile dello Chef autenticato.
	 * 
	 * @param parent frame genitore da cui si accede
	 */
	public void openMonthlyReport(JFrame parent, JXButton reportBtn)
	{
		EventQueue.invokeLater(() -> {
			try
			{
				ReportMensile report = getReportDAO().getMonthlyReportByIdChef(loggedUser.getId());
				ReportFrame rFrame = new ReportFrame();
				rFrame.setReportData(report.getTotCorsi(), report.getTotOnline(), report.getTotPratiche(),
						report.getMinRicette(), report.getMaxRicette(), report.getAvgRicette());
				rFrame.setLocationRelativeTo(parent);

				// Se viene chiuso il frame Report bisogna riattivare il bottone nella sidebar
				WindowAdapter wl = new WindowAdapter()
				{
					@Override
					public void windowClosed(WindowEvent e)
					{
						reportBtn.setEnabled(true);
						((Window) e.getSource()).removeWindowListener(this);
					}
				};
				rFrame.addWindowListener(wl);
				rFrame.setVisible(true);
			} 
			catch(DAOException e)
			{
				JOptionPane.showMessageDialog(parent, "Errore durante il caricamento del report.", "Errore",
						JOptionPane.ERROR_MESSAGE);
				LOGGER.log(Level.SEVERE, "Errore durante il report nel DB: " + e.getMessage(), e);
				reportBtn.setEnabled(true);
			}
		});
	}

	/**
	 * -------------------------
	 * 
	 * ProfileFrame
	 * 
	 * -------------------------
	 */
	
	 private void modifySuccess(ProfileFrame currFrame, Utente u) 
	    {
			LOGGER.log(Level.INFO, "Registrazione modifica per utente: {0}", u.getUsername());
			currFrame.showSuccess("Modifica avvenuta con successo!");
			this.loggedUser=u;
	    	goToProfile(currFrame);
	    }
		
	    private void modifyFailed(ProfileFrame currFrame, String message) 
	    {
	    	LOGGER.log(Level.WARNING, "Tentativo di modifica fallito: {0}", message);
	        currFrame.showError(message);
	    }
	    
	    private void modifyPartecipante(ProfileFrame currFrame, String username, String nome, String cognome,LocalDate data, String luogo, String email) throws DAOException
	    {
	        if((!loggedUser.getEmail().equals(email)) && getPartecipanteDAO().existsPartecipanteByEmail(email))
	            modifyFailed(currFrame, ERR_EMAIL_EXISTING);
       	if(!loggedUser.getUsername().equals(username))
       	{
       		try
	            {
	                tryGetUser(username);
	                modifyFailed(currFrame, "Username già utilizzato.");
	            }
	        	catch(RecordNotFoundException e)
	        	{
	        		Partecipante p = new Partecipante(username, nome, cognome, loggedUser.getCodiceFiscale(), data, luogo, email, loggedUser.getHashPassword(), null, null);
	                getPartecipanteDAO().update((Partecipante)loggedUser, p);
	                p.setId(loggedUser.getId());
	                modifySuccess(currFrame, p);
	        	}
       	}
       	else
       	{
       		Partecipante p = new Partecipante(username , nome, cognome, loggedUser.getCodiceFiscale(), data, luogo, email, loggedUser.getHashPassword(), null, null);
               getPartecipanteDAO().update((Partecipante)loggedUser, p);
               p.setId(loggedUser.getId());
               modifySuccess(currFrame, p);
       	}	        	
	    }
	    
	    private void modifyChef(ProfileFrame currFrame, String username, String nome, String cognome,LocalDate data, String luogo, String email, File selectedFile) 
	    		 				  throws DAOException, IOException
	    {
	        if((!loggedUser.getEmail().equals(email)) && getChefDAO().existsChefByEmail(email))
	        	modifyFailed(currFrame, ERR_EMAIL_EXISTING);

       	if(!loggedUser.getUsername().equals(username))
       	{
       		try
       		{
       			tryGetUser(username);
       			modifyFailed(currFrame, "Username già utilizzato.");
       		}
       		catch(RecordNotFoundException e)
       		{
       			String oldPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + loggedUser.getUsername();
	            	File oldFile = new File(oldPath);
	            	File newFile = new File(oldFile.getParent(), username);
	            	//oldFile.renameTo(newFile);
       			Path oldPathNIO = Paths.get(oldPath); // oldPath deve essere il percorso completo
       			Path newPathNIO = Paths.get(newFile.getAbsolutePath());

       			try {
       			    Files.move(oldPathNIO, newPathNIO, StandardCopyOption.REPLACE_EXISTING);
       			    LOGGER.log(Level.INFO, "Cartella username rinominata da {0} a {1}", new Object[]{oldPath, newFile.getName()});
       			} catch (IOException e1) {
       			    LOGGER.log(Level.SEVERE, "Errore durante la rinomina della cartella username da {0} a {1}: {2}", new Object[]{oldPath, newFile.getName(), e.getMessage()});
       			    // Questo è un errore grave che dovrebbe probabilmente bloccare la modifica del profilo
       			    // o almeno farla fallire nell'interfaccia utente.
       			    throw new IOException("Impossibile rinominare la cartella del profilo. " + e.getMessage(), e);
       			}
	            	
	                String curriculumPath = changeCurriculumFile(username, selectedFile);
	                Chef c = new Chef(username, nome, cognome, loggedUser.getCodiceFiscale(), data, luogo, email, loggedUser.getHashPassword(), curriculumPath, null, null);
	                getChefDAO().update((Chef)loggedUser, c);
	                c.setId(loggedUser.getId());
	                modifySuccess(currFrame, c);
	                
       		}
       	}
       	else
       	{
       		String curriculumPath = changeCurriculumFile(username, selectedFile);
   			Chef c = new Chef(username, nome, cognome, loggedUser.getCodiceFiscale(), data, luogo, email, loggedUser.getHashPassword(), curriculumPath, null, null);
               getChefDAO().update((Chef)loggedUser, c);
               c.setId(loggedUser.getId());
               modifySuccess(currFrame, c);
       	}

	    }    
	    
	    private String changeCurriculumFile(String username, File selectedFile) throws IOException
	    {
	        String relativePath = "resources" + File.separator + username + File.separator + "Curriculum";
	        File curr = new File (((Chef) loggedUser).getCurriculum());
	        String toDeleteString = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" +File.separator+ username+File.separator+ "Curriculum" + File.separator + curr.getName(); 
	        String fullPathString = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + relativePath;
	        try
	        {
	        	Files.delete(Paths.get(toDeleteString));
	        }
	        catch(IOException e)
	        {
	        	LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione di {0}", toDeleteString + ": " + e.getMessage());
	        }
	        
	        Path destinationDir = Paths.get(fullPathString);
	        Files.createDirectories(destinationDir); 
	        
	        Path destinationPath = destinationDir.resolve(selectedFile.getName());
	        if (!selectedFile.exists()) {
	            LOGGER.log(Level.SEVERE, "Source file does not exist: {0}", selectedFile.getAbsolutePath());
	            return null; // Or throw a custom exception
	        }
	        if (!selectedFile.isFile()) {
	            LOGGER.log(Level.SEVERE, "Source is not a file: {0}", selectedFile.getAbsolutePath());
	            return null;
	        }
	        try
	        {
	        	Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
	        }
	        catch(IOException e)
	        {
	            LOGGER.log(Level.SEVERE, "File non copiato: " + e.getMessage(), e); // Log the exception message and the exception itself
	        }

	        LOGGER.log(Level.INFO, "File salvato con successo in: {0}", destinationPath);
	        return relativePath + File.separator + selectedFile.getName();
	    }
	    
           
	    public void checkmodifyProfile(ProfileFrame currFrame,
	    		String nome, String cognome,LocalDate data, String luogo,
	    		String email, String username, 
	    		File selectedFile)
	    {
			try
			{
				if(isPartecipanteLogged())
					modifyPartecipante(currFrame, username, nome, cognome, data, luogo, email);
				else
					modifyChef(currFrame, username, nome, cognome, data, luogo, email, selectedFile);
			}
			catch(DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore registrazione DB", e);
				modifyFailed(currFrame, "Errore di accesso al database.");
			}
			catch(IOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore modifica", e);
				modifyFailed(currFrame, "Errore di modifica.");
			}
	    }
	    
	    
	public void showChangePasswordDialog(JXFrame parentFrame)
	{
		new ChangePasswordDialog(parentFrame).setVisible(true);
	}
	    
	public void checkNewPassword(ChangePasswordDialog currDialog, JXFrame parent, char[] oldPass, char[] newPass)
	{
		try
		{
			if(checkPassword(loggedUser.getHashPassword(), oldPass))
			{
				if(isChefLogged())
				{
					Chef c = new Chef(loggedUser.getUsername(), loggedUser.getNome(), loggedUser.getCognome(), loggedUser.getCodiceFiscale(), loggedUser.getDataDiNascita().toLocalDate(), loggedUser.getLuogoDiNascita(), loggedUser.getEmail(), hashPassword(newPass), ((Chef) loggedUser).getCurriculum(), null, null);
	                getChefDAO().update((Chef)loggedUser, c);
				}
				else
				{
					Partecipante p = new Partecipante(loggedUser.getUsername(), loggedUser.getNome(), loggedUser.getCognome(), loggedUser.getCodiceFiscale(), loggedUser.getDataDiNascita().toLocalDate(), loggedUser.getLuogoDiNascita(), loggedUser.getEmail(), hashPassword(newPass), null, null);
	                getPartecipanteDAO().update((Partecipante)loggedUser, p);
				}
				changePassSuccess(parent, currDialog, loggedUser.getUsername());
				currDialog.dispose();
			}				
			else
				changePassFailed(currDialog, "Password errata.");		
		}
		catch(RecordNotFoundException e) 
		{
			changePassFailed(currDialog, "Password errata.");
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore durante login nel DB: " + e.getMessage(), e);
			changePassFailed(currDialog, "Errore di accesso al database.");
		}
		finally
	    {
	        Arrays.fill(newPass, ' '); // It is recommended that the returned character array be cleared after use by setting each character to zero.
	    }
	}
	
	private void changePassSuccess(JXFrame parent, ChangePasswordDialog currDialog, String username) 
   {
		LOGGER.log(Level.INFO, "Password modifica per utente: {0}", username);
		currDialog.showSuccess("Password modificata con successo!");
		goToProfile(parent);
   }
	
   private void changePassFailed(ChangePasswordDialog currDialog, String message) 
   {
   	LOGGER.log(Level.WARNING, "Tentativo di modifica fallito: {0}", message);
       currDialog.showError(message);
   }
   
   public void checkDelete(JXFrame parentFrame, ConfirmEliminationDialog currDialog, char[]pass)
   {
   	try
		{
			if(checkPassword(loggedUser.getHashPassword(), pass))
			{
				if(isChefLogged())
				{
					getChefDAO().delete(loggedUser.getId());
					if (Files.exists(Paths.get(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" +File.separator+ loggedUser.getUsername()))) {
			            try (Stream<Path> walk = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" +File.separator+ loggedUser.getUsername()))) {
			                walk.sorted(Comparator.reverseOrder()) // Ordina al contrario per eliminare prima i file e le sottocartelle interne
			                    .forEach(path -> {
			                        try {
			                            Files.delete(path);
			                            LOGGER.log(Level.INFO, "Eliminato: {0}", path);
			                        } catch (DirectoryNotEmptyException e) {
			                        	LOGGER.log(Level.WARNING, "Errore: La cartella non è vuota e non può essere eliminata: {0}", path);
			                        	currDialog.showError("Errore: La cartella non è vuota e non può essere eliminata: " + path);
			                        } catch (IOException e) {
			                        	LOGGER.log(Level.WARNING, "Errore durante l'eliminazione di {0}", path + ":" + e.getMessage());
			                        	currDialog.showError("Errore durante l'eliminazione di " + path + ":" + e.getMessage());
			                        }
			                    });
			                System.out.println("Cartella svuotata e eliminata con successo: " + (Paths.get(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" +File.separator+ loggedUser.getUsername())));
			            }
			            catch(IOException e)
			            {
			            	LOGGER.log(Level.WARNING, "Errori IO");
			                currDialog.showError("Errori IO");
			            }
			        } else {
			            System.out.println("La directory specificata non esiste o non è una directory: " + (Paths.get(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" +File.separator+ loggedUser.getUsername())));
			        }
				}					
				else
					getPartecipanteDAO().delete(loggedUser.getId());
				deleteSuccess(parentFrame, loggedUser.getUsername());
			}				
			else
				deleteFailed(currDialog, "Password errata.");		
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore durante login nel DB: " + e.getMessage(), e);
			deleteFailed(currDialog, "Errore di accesso al database.");
		}
		finally
	    {
	        Arrays.fill(pass, ' '); // It is recommended that the returned character array be cleared after use by setting each character to zero.
	    }
   }
   
   private void deleteSuccess(JXFrame parentFrame, String username) 
   {
		LOGGER.log(Level.INFO, "Eliminazione avvenuta con successo: {0}", username);
		goToLogin(parentFrame);
   }
	
   private void deleteFailed(ConfirmEliminationDialog currDialog, String message) 
   {
   	LOGGER.log(Level.WARNING, "Eliminazione fallita: {0}", message);
       currDialog.showError(message);
   }
}