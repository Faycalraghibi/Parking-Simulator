package com.example.parkingsimulator;
import java.util.List;

public interface IStrategy {
	public int trouverPlace(List<Long> tempPlace, List<Boolean> placeOccupe, int nbrPlaces);
}
