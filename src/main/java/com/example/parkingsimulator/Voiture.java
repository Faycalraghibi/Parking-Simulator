package com.example.parkingsimulator;
import java.util.Random;

public class Voiture extends Thread {
	private final String nom;
	private final Parking parking;
	private int place;

	public Voiture(String nom, Parking parking) {
		this.nom = nom;
		this.parking = parking;
	}
	public String getNom() {
		return this.nom;
	}
	public void setPlace(int place) {
		this.place = place;
	}
	public int getPlace() {
		return this.place;
	}
	Random random = new Random();
	@Override
	public void run(){
		try {
		parking.stationner(this);
		
		Thread.sleep(random.nextInt(2000)+10000);
		parking.sortir(this);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
