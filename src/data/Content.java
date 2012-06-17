package data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.Onderdeel;
import logic.Onderwerp;
import logic.Vraag;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import exceptions.DataException;

/**
 * @author nanne
 *
 */
public class Content {
	// verkrijg vragen (met onderwerp)
	private Document	doc	= null;

	/**
	 * Maak de contentparser aan en bereidt voor op inlezen vragen bestand.
	 * 
	 * @throws DataException Als de vragen niet gevonden kunnen worden.
	 */
	public Content() throws DataException {
		File file;
		try {
			file = new File(getClass().getResource("/resource/Vragen.xml").toURI());
		} catch (Exception e1) {
			throw new DataException("Het vragenbestand kan niet worden gevonden.");
		}

		try {
			doc = new Builder().build(file);
		} catch (ValidityException e) {
			throw new DataException("Fout bij validatie van vragenbestand.\r\n" + e.getMessage());
		} catch (ParsingException e) {
			throw new DataException("Fout bij parsen van vragenbestand.\r\n" + e.getMessage());
		} catch (IOException e) {
			throw new DataException("Het vragenbestand kan niet worden gevonden.");
		}
	}

	/**
	 * Haal alle onderwerpen uit resource/Vragen.xml.
	 * 
	 * @return Lijst met onderwerpen.
	 * @throws DataException Wanneer het path van het plaatje verkeerd is.
	 */
	public List<Onderwerp> getOnderwerpen() throws DataException {
		List<Onderwerp> onderwerpen = new ArrayList<Onderwerp>();
		Elements elements = doc.getRootElement().getChildElements();

		for (int i = 0; i < elements.size(); i++) {
			Element node = elements.get(i);

			String naam = node.getAttribute("name").getValue();
			String path = node.getAttribute("image").getValue();

			File plaatje;
			try {
				plaatje = new File(getClass().getResource("/resource/images/" + path).toURI());
			} catch (Exception e) {
				throw new DataException("Plaatje \"" + path + "\" is onvindbaar.");
			}
			if (!plaatje.canRead())
				throw new DataException("Plaatje \"" + plaatje.getAbsolutePath() + "\" is onvindbaar.");
			Onderwerp onderwerp = new Onderwerp(naam, plaatje);
			onderwerpen.add(onderwerp);
		}
		return onderwerpen;
	}

	/**
	 * Krijg 4 vragen terug.
	 * @param onderwerpNaam onderwerpfilter
	 * @return Lijst met vragen
	 * @throws DataException wanneer Vragen.xml niet gelezen kan worden
	 */
	public List<Vraag> getVragen(String onderwerpNaam) throws DataException {
		return getVragen(onderwerpNaam, 4);
	}

	/**
	 * Krijg N vragen terug.
	 * @param onderwerpNaam onderwerpfilter
	 * @param hoeveel hoeveelheid vragen dat je terug wilt krijgen
	 * @return Lijst met vragen
	 * @throws DataException wanneer Vragen.xml niet gelezen kan worden
	 */
	public List<Vraag> getVragen(String onderwerpNaam, int hoeveel) throws DataException {
		// Laad onderdelen in uit document
		Elements elements = doc.getRootElement().getChildElements();

		// Vind onderwerp.
		Element onderwerp = null;
		for (int i = 0; i < elements.size(); i++) {
			Element node = elements.get(i);
			if (node.getAttribute("name").getValue().equals(onderwerpNaam)) onderwerp = node;
		}

		// Bij niks gevonden foutmelding
		if (onderwerp == null) throw new DataException("Onderwerp bestaat niet");

		// Haal alle vragen uit het onderwerp.
		Elements nodes = onderwerp.getChildElements();
		
		// Gooi alle vragen willekeurig door elkaar, ook wanneer er te weinig vragen zijn.
		ArrayList<Integer> vragenKeys = new ArrayList<Integer>();
		for (int i = 0; i < Math.max(nodes.size(), hoeveel); i++)
			vragenKeys.add(i);
		Collections.shuffle(vragenKeys);
		
		// Verkrijg alle vragen en gooi ze in een lijst
		List<Vraag> vragen = new ArrayList<Vraag>();
		for (int i = 0; i < vragenKeys.size(); i++) {
			Element node = nodes.get(vragenKeys.get(i) % nodes.size());
			String tekst = node.getFirstChildElement("tekst").getValue();
			
			// Haal onderdelen op
			Elements nOnderdelen = node.getFirstChildElement("onderdelen").getChildElements();
			
			ArrayList<Integer> onderdelenKeys = new ArrayList<Integer>();
			for (int k = 0; k < nOnderdelen.size(); k++)
				onderdelenKeys.add(k);
			Collections.shuffle(onderdelenKeys);
			
			ArrayList<Onderdeel> onderdelen = new ArrayList<Onderdeel>();
			for (int j = 0; j < 9; j++) {
				Element nodeOnderdeel = nOnderdelen.get(onderdelenKeys.get(j));
				String antwoord = nodeOnderdeel.getValue();
				String path = nodeOnderdeel.getAttribute("image").getValue();
				File plaatje = null;
				try {
					plaatje = new File(getClass().getResource("/resource/images/" + path).toURI());
				} catch (URISyntaxException e) {
					throw new DataException("Fout in filepath van plaatje van onderdeel \"" + antwoord + "\" met path \""+path+"\"");
				} catch (java.lang.NullPointerException e) {
					throw new DataException("Fout in filepath van plaatje van onderdeel \"" + antwoord + "\" met path \""+path+"\"");
				}

				onderdelen.add(new Onderdeel(antwoord, plaatje));
			}
			
			// Voeg vraag toe aan lijst.
			vragen.add(new Vraag(tekst, onderdelen));
		}
		return vragen;
	}
}
