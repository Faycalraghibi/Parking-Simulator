package com.example.parkingsimulator;


import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.paint.Color;

public class Parking {
	private static final Logger LOGGER = Logger.getLogger(Parking.class.getName());
	private final Semaphore placesSemaphore;
	private final Lock placesLock = new ReentrantLock();
	private final List<Long> tempPlace = new ArrayList<>();
	private final List<Boolean> placeOccupe = new ArrayList<>();
	private final IStrategy strategy;
	private final int nbrPlaces;
	private final Main gui;
	private final int syncStrategy; // 1 pour Sémaphore, 2 pour Mutex

	public Parking(int nbrPlaces, IStrategy strategy, Main main, int syncStrategy) {
		// configuration du fichier journalisation
		try {
			FileHandler fileHandler = new FileHandler("../app.log", false); // Append mode = false
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
			LOGGER.info("Fichier journalisation configure avec succes");
		} catch (Exception e) {
			LOGGER.severe("Echec configuration fichier journalisation : " + e.getMessage());
			System.exit(1);
		}
		// Initialisation des sémaphores et des mutex
		this.placesSemaphore = new Semaphore(nbrPlaces, true);
		this.syncStrategy = syncStrategy;
		// Initialiser la liste contenant le temps nécessaire pour sortir de chaque place
		// Initialiser la liste des places occupées
		for (int i = 0; i < nbrPlaces; i++) {
			tempPlace.add((i % 5 + 1) * 1000L); // la liste des valeurs possibles est [1000, 2000, 3000, 4000, 5000]
			placeOccupe.add(false);
		}
		// Initialiser la stratégie
		this.strategy = strategy;
		// Initialiser le nombre de places
		this.nbrPlaces = nbrPlaces;
		// Initialiser l'interface graphique
		this.gui = main;
	}

	public void stationner(Voiture voiture) throws Exception {
		long startAttemptTime = System.currentTimeMillis(); // Enregistrer le début de l'essai de stationnement
		LOGGER.info("La voiture " + voiture.getNom() + " essaie de stationner");

		if (syncStrategy == 1) {
			placesSemaphore.acquire();
		} else if (syncStrategy == 2) {
			placesLock.lock();
		}

		try {
			int place = strategy.trouverPlace(tempPlace, placeOccupe, nbrPlaces);
			placeOccupe.set(place, true);
			int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
			int col = place % numColumns;
			int row = place / numColumns;
			gui.setRectangleColor(col, row, Color.RED);
			gui.setRectangleText(col, row, voiture.getNom());
			voiture.setPlace(place);
			LOGGER.info("La voiture " + voiture.getNom() + " a stationne !");
		} finally {
			if (syncStrategy == 2) {
				placesLock.unlock();
			}
		}

		long endAttemptTime = System.currentTimeMillis(); // Enregistrer la fin du stationnement (réussi ou non)
		long parkingWaitTime = endAttemptTime - startAttemptTime; // Calculer le temps d'attente
		LOGGER.info("Temps d'attente pour la voiture " + voiture.getNom() + " : " + parkingWaitTime + " millisecondes");
	}

	public void sortir(Voiture voiture) {
		LOGGER.info("La voiture " + voiture.getNom() + " essaie de sortir");

		if (syncStrategy == 2) {
			placesLock.lock();
		}

		try {
			Thread.sleep(tempPlace.get(voiture.getPlace()));
			placeOccupe.set(voiture.getPlace(), false);
			int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
			int col = voiture.getPlace() % numColumns;
			int row = voiture.getPlace() / numColumns;
			gui.setRectangleColor(col, row, Color.GREEN);
			gui.setRectangleText(col, row, "");
			LOGGER.info("La voiture " + voiture.getNom() + " a sorti !");
		} catch (Exception e) {
			LOGGER.warning("Exception " + e.getMessage());
		} finally {
			if (syncStrategy == 1) {
				placesSemaphore.release();
			} else if (syncStrategy == 2) {
				placesLock.unlock();
			}
		}
	}
}
