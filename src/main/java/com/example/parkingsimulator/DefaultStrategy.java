package com.example.parkingsimulator;

import java.util.List;

public class DefaultStrategy implements IStrategy {
	@Override
	public int trouverPlace(List<Long> tempPlace, List<Boolean> placeOccupe, int nbrPlaces) {
		int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
		int numRows = (int) Math.ceil((double) nbrPlaces / numColumns);

		for (int col = 0; col < numColumns; col++) {
			for (int row = 0; row < numRows; row++) {
				int place = col + row * numColumns;
				if (place < nbrPlaces && !placeOccupe.get(place)) {
					return place;
				}
			}
		}
		return -1; // Return -1 if no place is found
	}
}
