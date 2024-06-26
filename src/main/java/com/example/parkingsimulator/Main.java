package com.example.parkingsimulator;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Main extends Application {
	private CustomRectangle[][] matrix;
	private int syncStrategy; // Variable pour stocker la stratégie de synchronisation choisie
	private int numCars; // Nombre de voitures
	private int nbrPlaces; // Nombre de places

	@Override
	public void start(Stage primaryStage) {
		syncStrategy = getSyncStrategyFromUser();
		numCars = getNumFromUser("Nombre de voitures", "Entrez le nombre de voitures:");
		nbrPlaces = getNumFromUser("Nombre de places de parking", "Entrez le nombre de places de parking:");

		int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces)); // Calcul dynamique du nombre de colonnes
		int numRows = (int) Math.ceil((double) nbrPlaces / numColumns); // Calcul dynamique du nombre de lignes

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10));
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setAlignment(Pos.CENTER); // Centrer la grille
		matrix = new CustomRectangle[numColumns][numRows];

		// Initialiser et ajouter des rectangles personnalisés à la grille
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numColumns; col++) {
				matrix[col][row] = new CustomRectangle();
				grid.add(matrix[col][row], col, row);
				if (row * numColumns + col < nbrPlaces) {
					setRectangleColor(col, row, Color.GREEN);
				} else {
					setRectangleColor(col, row, Color.GREY);
				}
			}
		}

		Button statsButton = new Button("Afficher la moyenne d'attente");
		statsButton.setStyle(
				"-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-border-radius: 5px; -fx-background-radius: 5px;"
		);
		statsButton.setOnAction(e -> {
			String stats = Analyze("../app.log");
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Statistiques de temps d'attente");
			alert.setHeaderText(null);
			alert.setContentText(stats);
			alert.showAndWait();
		});

		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(15));
		vbox.setAlignment(Pos.CENTER); // Centrer le contenu du VBox
		vbox.getChildren().addAll(grid, statsButton);

		Scene scene = new Scene(vbox, 400, 400);
		primaryStage.setTitle("Parking system");
		primaryStage.setScene(scene);
		primaryStage.show();

		IStrategy defaultStrategy = new DefaultStrategy();
		Parking p = new Parking(nbrPlaces, defaultStrategy, this, syncStrategy);
		Random random = new Random();
		for (int t = 0; t < numCars; t++) {
			final int finalT = t;
			PauseTransition pause = new PauseTransition(Duration.millis(random.nextInt(3000) + 5000));
			pause.setOnFinished(event -> {
				Voiture v = new Voiture("Vec " + finalT, p);
				v.start();
			});
			pause.play();
		}
	}

	// Method to set the color of a specific rectangle
	public void setRectangleColor(int col, int row, Color color) {
		if (col >= 0 && col < matrix.length && row >= 0 && row < matrix[col].length) {
			matrix[col][row].setColor(color);
		}
	}

	// Method to set the text of a specific rectangle
	public void setRectangleText(int col, int row, String text) {
		if (col >= 0 && col < matrix.length && row >= 0 && row < matrix[col].length) {
			matrix[col][row].setText(text);
		}
	}

	private int getSyncStrategyFromUser() {
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Choix de la stratégie de synchronisation");
		dialog.setHeaderText("Sélectionnez la stratégie de synchronisation");
		dialog.setContentText("Entrez 1 pour Sémaphore ou 2 pour Mutex:");

		Optional<String> result = dialog.showAndWait();
		return result.map(Integer::parseInt).orElse(1); // Par défaut, utiliser le sémaphore
	}

	private int getNumFromUser(String title, String content) {
		TextInputDialog dialog = new TextInputDialog("10");
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(content);

		Optional<String> result = dialog.showAndWait();
		return result.map(Integer::parseInt).orElse(10); // Par défaut, utiliser 10
	}

	public static String Analyze(String filePath) {
		List<Double> tempsAttenteList = new ArrayList<>();
		double totalAttente = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("Temps d'attente pour la voiture")) {
					int startIndex = line.lastIndexOf(":") + 2;
					int endIndex = line.lastIndexOf(" millisecondes");
					String attenteStr = line.substring(startIndex, endIndex);
					// Remove any non-numeric characters
					attenteStr = attenteStr.replaceAll("[^\\d.]", "");
					double tempsAttente = Double.parseDouble(attenteStr);
					tempsAttenteList.add(tempsAttente);
					totalAttente += tempsAttente;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder result = new StringBuilder();
		if (!tempsAttenteList.isEmpty()) {
			result.append("Temps d'attente de chaque voiture :\n");
			for (int i = 0; i < tempsAttenteList.size(); i++) {
				result.append("Voiture ").append(i + 1).append(": ").append(tempsAttenteList.get(i)).append(" millisecondes\n");
			}

			double moyenneAttente = totalAttente / tempsAttenteList.size();
			result.append("La moyenne d'attente des voitures est de : ").append(moyenneAttente / 1000).append(" Secondes");
		} else {
			result.append("Aucune information sur l'attente des voitures trouvée dans le fichier journal.");
		}
		return result.toString();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
