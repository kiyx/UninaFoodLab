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
	private List<Ingrediente> cacheIngredienti = new ArrayList<>();
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
		cacheIngredienti = null;
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
					if(c.getChef()==getLoggedUser())
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
	
	public void showCourseDetail(MyCoursesFrame myCoursesFrame, int idCorso)
	{
		
	}
	
	public void showCreateCourseDialog(MyCoursesFrame parentFrame)
	{
		 new CreateCourseDialog(parentFrame).setVisible(true);	
	}
	
	public void callRemoveSessionCard(CreateCourseDialog parent, CreateSessionPanel panel)
	{
		parent.removeSessionCard(panel);
	}
	
	public void clearMyCoursesCache()
	{
		cacheCorsi = null;
	}
	
	public void clearCourseDialogCache()
	{
		cacheArgomenti = null;
		cacheRicette = null;
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
	
	public void loadCorsiForMyCourses(List<Integer> idsCorsi, List<String> namesCorsi, List<List<Integer>> idsArguments,
						  List<List<String>> namesArguments, List<Date> startDates, List<Integer> sessionsNumbers)
	{
		try
		{
			if(cacheCorsi == null || cacheCorsi.isEmpty())
				cacheCorsi = (isChefLogged()) ? getCorsoDAO().getCorsiByIdChef(loggedUser.getId()) : 
										    	getCorsoDAO().getCorsiByIdPartecipante(loggedUser.getId());

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
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore loadCorsi da DB", e);
		}
	}
	
	public void createCourse(MyCoursesFrame courseFrame, CreateCourseDialog currDialog, String nomeCorso, LocalDate dataInizio, int numeroSessioni, String frequenza,
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
			courseFrame.addCourseCard(newCard);
			
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
	
	/**
	 * -------------------------
	 * 
	 * MyRecipesFrame
	 * CreateRecipesDialog
	 * DetailedRecipeFrame
	 * -------------------------
	 */
	// CreateRecipesDialog
				// DetailedRecipeFrame
			
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
	
	public void showChangeRecipeDialog(DetailedRecipeDialog currDialog, MyRecipesFrame parent, int idRicetta, String nomeRicetta, String provenienzaRicetta, int calorieRicetta, String difficoltaRicetta, String allergeniRicetta, int tempoRicetta, ArrayList<String> nomiIngredienti, ArrayList<Double> quantitaIngredienti, ArrayList<String> udmIngredienti)
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
	
	/*public void checkRicetta(int id)
	{
		getRicettaDAO
	}*/
	
	public void deleteRicetta(MyRecipesFrame parent,DetailedRecipeDialog currDialog, int id)
	{
		currDialog.dispose();
		try
		{
			getRicettaDAO().delete(id);
			LOGGER.log(Level.INFO, "Ricetta eliminata con successo");
			parent.showSuccess("Ricetta eliminata con successo");
			for(Ricetta r: ricette)
			{
				if(r.getId()==id)
				{
					ricette.remove(r);
					break;
				}
			}
			parent.deleteCard(id);
		}
		catch(DAOException e)
		{
			LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione nel DB della ricetta", e);
			parent.showError("Errore durante l'eliminazione nel DB della ricetta");
		}
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
		
		public void changeRicetta(MyRecipesFrame parent, ChangeRecipeDialog currDialog,  int idRicetta, String nameRicetta, String provenienzaRicetta, int tempoRicetta, int calorieRicetta,
				String difficoltaRicetta, String allergeniRicetta, ArrayList<Integer> newIdIngredienti, ArrayList<Double> newQuantitaIngredienti, ArrayList<String> newUdmIngredienti,
				ArrayList<Integer> oldIdIngredienti, ArrayList<Double> oldQuantitaIngredienti, ArrayList<String> oldUdmIngredienti, ArrayList<Integer> idIngredientiDeleted)
		{
			ArrayList<Utilizzo> newUtilizziRicetta = new ArrayList<>();
			ArrayList<Utilizzo> oldUtilizziRicetta = new ArrayList<>();
			ArrayList<Utilizzo> toDeleteUtilizzi = new ArrayList<>();
			
			Ingrediente ingredienteUtil=null;
			Ricetta original=null;
			
			for(Ricetta r: ricette) {
				if(r.getId()==idRicetta)
					original=r;
			}
			
			for(Utilizzo u: original.getUtilizzi())
			{
				for(int i=0; i<idIngredientiDeleted.size(); i++)
				{
					if(u.getIdIngrediente()==idIngredientiDeleted.get(i))
					{
						toDeleteUtilizzi.add(u);
						break;
					}					
				}				
			}
			
			for(int i=0; i<newIdIngredienti.size(); i++)
			{
	                boolean trovato = false;
	
	                for(Ingrediente u : ingredienti)
	                {
	                    if(u.getId() == newIdIngredienti.get(i))
	                    {
	                    	ingredienteUtil=u;
	                        trovato = true;
	                        break;
	                    }
	                if(!trovato)
	                	ingredienteUtil = getIngredienteDAO().getIngredienteById(newIdIngredienti.get(i));
	                }
	                
				Utilizzo u = new Utilizzo(newQuantitaIngredienti.get(i), UnitaDiMisura.valueOf(newUdmIngredienti.get(i)),
						ingredienteUtil);
				newUtilizziRicetta.add(u);
			}
			
			for(int i=0; i<oldIdIngredienti.size(); i++)
			{
	                boolean trovato = false;
	
	                for(Ingrediente u : ingredienti)
	                {
	                    if(u.getId() == oldIdIngredienti.get(i))
	                    {
	                    	ingredienteUtil=u;
	                        trovato = true;
	                        break;
	                    }
	                if(!trovato)
	                	ingredienteUtil = getIngredienteDAO().getIngredienteById(oldIdIngredienti.get(i));
	                }
	                
				Utilizzo u = new Utilizzo(oldQuantitaIngredienti.get(i), UnitaDiMisura.valueOf(oldUdmIngredienti.get(i)),
						ingredienteUtil);
				oldUtilizziRicetta.add(u);
			}
			
			try
			{
				newUtilizziRicetta.addAll(oldUtilizziRicetta);
				Ricetta toChangeRicetta = new Ricetta(nameRicetta, provenienzaRicetta, tempoRicetta, calorieRicetta,LivelloDifficolta.valueOf(difficoltaRicetta), allergeniRicetta, (Chef)getLoggedUser(), newUtilizziRicetta);
				if(original.getNome()!=toChangeRicetta.getNome()&& getRicettaDAO().existsRicettaByNome(toChangeRicetta, ((Chef)getLoggedUser()).getId()))
				{
					currDialog.showError(ERR_RECIPE_EXISTING);
					LOGGER.log(Level.SEVERE, "Ricetta con lo stesso nome per lo stesso chef");
				}
				else
				{
					getRicettaDAO().update(original, toChangeRicetta);
					LOGGER.log(Level.INFO, "Ricetta salvata con successo");	
					currDialog.showSuccess("Ricetta salvata con successo");
					currDialog.dispose();
					
					//parent.updateRecipeCard(new RecipeCardPanel(toChangeRicetta.getId(), toChangeRicetta.getNome(), toChangeRicetta .getDifficolta().toString(), toSaveRicetta.getCalorie()));
					//ricette.add(toSaveRicetta);
				}
				
			}
			catch(DAOException e)
			{
				LOGGER.log(Level.SEVERE, "Errore salvataggio ricetta nel DB", e);	
				currDialog.showError("Errore salvataggio ricetta nel DB");
			}
			
		}
		
		public void saveRicettaUtilizzi(MyRecipesFrame parent, CreateRecipesDialog currDialog,  String nameRicetta, String provenienzaRicetta, int tempoRicetta, int calorieRicetta,
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
					
					parent.newRecipeCard(new RecipeCardPanel(toSaveRicetta.getId(), toSaveRicetta.getNome(), toSaveRicetta.getDifficolta().toString(), toSaveRicetta.getCalorie()));
					ricette.add(toSaveRicetta);
				}
				
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
   
   public Chef getLoggedChef()
   {
   		return (Chef)getLoggedUser();
   }

   

  
}